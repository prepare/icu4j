/**
 * 
 */
package com.ibm.icu.text;

import com.ibm.icu.util.ULocale;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PluralRules implements Serializable {
    private static final long serialVersionUID = 1;

    private static final Map ruleMap; // from locale string to PluralRules

    private final RuleList rules;
    private final Set keywords;
    private int repeatLimit; // for equality test

    /**
     * Standard keywords.
     */
    public static final String KEYWORD_ZERO = "zero";
    public static final String KEYWORD_ONE = "one";
    public static final String KEYWORD_TWO = "two"; // aka dual
    public static final String KEYWORD_FEW = "few"; // aka paucal, special
    public static final String KEYWORD_MANY = "many"; // aka 11..99

    /**
     * The reserved keyword for defining the default case.
     */
    public static final String KEYWORD_OTHER = "other";

    /**
     * The set of all characters a valid keyword can start with.
     */
    private static final UnicodeSet START_CHARS = 
        new UnicodeSet("[[:ID_Start:][_]]");

    /**
     * The set of all characters a valid keyword can contain after 
     * the first character.
     */
    private static final UnicodeSet CONT_CHARS = 
        new UnicodeSet("[:ID_Continue:]");

    /**
     * The default constraint that is always satisfied.
     */
    private static final Constraint NO_CONSTRAINT = new Constraint() {
        public boolean isFulfilled(long n) {
            return true;
        }
        public String toString() {
            return "n is any";
        }

        public int updateRepeatLimit(int limit) {
            return limit;
        }
      };

    /**
     * The default rule that always returns "other".
     */
    private static final Rule DEFAULT_RULE = new Rule() {
        public String getKeyword() {
            return KEYWORD_OTHER;
        }

        public boolean appliesTo(long n) {
            return true;
        }

        public String toString() {
            return "(" + KEYWORD_OTHER + ")";
        }

        public int updateRepeatLimit(int limit) {
            return limit;
        }
      };


    /**
     * The default rules that accept any number and return "other".
     */
    public static final PluralRules DEFAULT =
        new PluralRules(new RuleChain(DEFAULT_RULE));

    /**
     * Parses a plural rules description and returns a PluralRules.
     * @throws ParseException if the description cannot be parsed.
     *    The exception index is typically not set, it will be -1.
     */
    public static PluralRules parseDescription(String description) 
        throws ParseException {

        description = description.trim();
        if (description.length() == 0) {
          return DEFAULT;
        }

        return new PluralRules(parseRuleChain(description));
    }

    /**
     * Creates a PluralRules from a description if it is parsable,
     * otherwise returns null.
     */
    public static PluralRules createRules(String description) {
        try {
            return parseDescription(description);
        } catch(ParseException e) {
            return null;
        }
    }

    /** 
     * A constraint on a number.
     */
    private interface Constraint extends Serializable {
        /** Returns true if the number fulfills the constraint. */
        boolean isFulfilled(long n);
        /** Returns the larger of limit or the limit of this constraint. */
        int updateRepeatLimit(int limit);
    }

    /**
     * A pluralization rule.  .
     */
    private interface Rule extends Serializable {
        /** Returns the keyword that names this rule. */
        String getKeyword();
        /** Returns true if the rule applies to the number. */
        boolean appliesTo(long n);
        /** Returns the larger of limit and this rule's limit. */
        int updateRepeatLimit(int limit);
    }

    /**
     * A list of rules to apply in order.
     */
    private interface RuleList extends Serializable {
        /** Returns the keyword of the first rule that applies to the number. */
        String select(long n);

        /** Returns the set of defined keywords. */
        Set getKeywords();

        /** Return the value at which this rulelist starts repeating. */
        int getRepeatLimit();
    }

    // default data
    static {
        String[] ruledata = {
          "other: n/ja,ko,tr,vi",  // not strictly necessary, default for all
            "one: n is 1/da,de,el,en,eo,es,et,fi,fo,he,hu,it,nb,nl,nn,no,pt,sv",
            "one: n in 0..1/fr,pt_BR",
            "zero: n is 0; one: n mod 10 is 1 and n mod 100 is not 11/lv",
            "one: n is 1; two: n is 2/ga",
            "zero: n is 0; one: n is 1; zero: n mod 100 in 1..19/ro",
            "other: n mod 100 in 11..19; one: n mod 10 is 1; " + 
                "few: n mod 10 in 2..9/lt",
            "one: n mod 10 is 1 and n mod 100 is not 11; " +
                "few: n mod 10 in 2..4 " +
                "and n mod 100 not in 12..14/hr,ru,sr,uk",
            "one: n is 1; few: n in 2..4/cs,sk",
            "one: n is 1; few: n mod 10 in 2..4 and n mod 100 not in 12..14/pl",
            "one: n mod 100 is 1; two: n mod 100 is 2; " +
                "few: n mod 100 in 3..4/sl",
        };

        HashMap map = new HashMap();
        for (int i = 0; i < ruledata.length; ++i) {
            String[] data = ruledata[i].split("/");
            try {
              PluralRules pluralRules = parseDescription(data[0]);
              String[] locales = data[1].split(",");
              for (int j = 0; j < locales.length; ++j) {
                map.put(locales[j].trim(), pluralRules);
              }
            } catch (Exception e) {
              System.err.println("PluralRules init failure, " + 
                                 e.getMessage() + " at line " + i);
            }
        }
           
        ruleMap = map;
    }

    /**
     * syntax:
     * condition :     or_condition
     *                 and_condition
     * or_condition :  and_condition 'or' condition
     * and_condition : relation
     *                 relation 'and' relation
     * relation :      is_relation
     *                 in_relation 
     *                 'n' EOL
     * is_relation :   expr 'is' value
     *                 expr 'is' 'not' value
     * in_relation :   expr 'in' range
     *                 expr 'not' 'in' range
     * expr :          'n'
     *                 'n' 'mod' value
     * value :         digit+
     * digit :         0|1|2|3|4|5|6|7|8|9
     * range :         value'..'value
     */
    private static Constraint parseConstraint(String description) 
        throws ParseException {

        description = description.trim().toLowerCase(Locale.ENGLISH);

        Constraint result = null;
        String[] or_together = description.split("or");
        for (int i = 0; i < or_together.length; ++i) {
            Constraint andConstraint = null;
            String[] and_together = or_together[i].split("and");
            for (int j = 0; j < and_together.length; ++j) {
                Constraint newConstraint = NO_CONSTRAINT;

                String condition = and_together[j].trim();
                String[] tokens = condition.split("\\s+");

                int mod = 0;
                boolean within = true;
                long lowBound = -1;
                long highBound = -1;

                boolean isRange = false;

                int x = 0;
                String t = tokens[x++];
                if (!"n".equals(t)) {
                    throw unexpected(t, condition);
                }
                if (x < tokens.length) {
                    t = tokens[x++];
                    if ("mod".equals(t)) {
                        mod = Integer.parseInt(tokens[x++]);
                        t = nextToken(tokens, x++, condition);
                    }
                    if ("is".equals(t)) {
                        t = nextToken(tokens, x++, condition);
                        if ("not".equals(t)) {
                            within = false;
                            t = nextToken(tokens, x++, condition);
                        }
                    } else {
                        isRange = true;
                        if ("not".equals(t)) {
                            within = false;
                            t = nextToken(tokens, x++, condition);
                        }
                        if ("in".equals(t)) {
                            t = nextToken(tokens, x++, condition);
                        } else {
                            throw unexpected(t, condition);
                        }
                    }

                    if (isRange) {
                        String[] pair = t.split("\\.\\.");
                        if (pair.length == 2) {
                            lowBound = Long.parseLong(pair[0]);
                            highBound = Long.parseLong(pair[1]);
                        } else {
                            throw unexpected(t, condition);
                        }
                    } else {
                        lowBound = highBound = Long.parseLong(t);
                    }

                    if (x != tokens.length) {
                        throw unexpected(tokens[x], condition);
                    }

                    newConstraint = 
                        new RangeConstraint(mod, within, lowBound, highBound);
                }

                if (andConstraint == null) {
                    andConstraint = newConstraint;
                } else {
                    andConstraint = new AndConstraint(andConstraint, 
                                                      newConstraint);
                }
            }

            if (result == null) {
                result = andConstraint;
            } else {
                result = new OrConstraint(result, andConstraint);
            }
        }

        return result;
    }

    /** Returns a parse exception wrapping the token and context strings. */
    private static ParseException unexpected(String token, String context) {
        return new ParseException("unexpected token '" + token +
                                  "' in '" + context + "'", -1);
    }

    /** 
     * Returns the token at x if available, else throws a parse exception.
     */
    private static String nextToken(String[] tokens, int x, String context) 
        throws ParseException {
        if (x < tokens.length) {
            return tokens[x];
        }
        throw new ParseException("missing token at end of '" + context + "'", -1);
    }

    /**
     * Syntax:
     * rule : keyword ':' condition
     * keyword: <identifier>
     */
    private static Rule parseRule(String description) throws ParseException {
        int x = description.indexOf(':');
        if (x == -1) {
            throw new ParseException("missing ':' in rule description '" +
                                     description + "'", 0);
        }

        String keyword = description.substring(0, x).trim();
        if (!isValidKeyword(keyword)) {
          throw new ParseException("keyword '" + keyword +
                                   " is not valid", 0);
        }

        description = description.substring(x+1).trim();
        if (description.length() == 0) {
          throw new ParseException("missing constraint in '" + 
                                   description + "'", x+1);
        }
        Constraint constraint = parseConstraint(description);
        Rule rule = new ConstrainedRule(keyword, constraint);
        return rule;
    }

    /**
     * Syntax:
     * rules : rule
     *         rule ';' rules
     */
    private static RuleChain parseRuleChain(String description) 
        throws ParseException {

        RuleChain rc = null;
        String[] rules = description.split(";");
        for (int i = 0; i < rules.length; ++i) {
            Rule r = parseRule(rules[i].trim());
            if (rc == null) {
                rc = new RuleChain(r);
            } else {
                rc = rc.addRule(r);
            }
        }
        return rc;
    }

    /** 
     * An implementation of Constraint representing a modulus, 
     * a range of values, and include/exclude. Provides lots of
     * convenience factory methods.
     */
    private static class RangeConstraint implements Constraint, Serializable {
        private static final long serialVersionUID = 1;
      
        private int mod;
        private boolean within;
        private long lowerBound;
        private long upperBound;

        public boolean isFulfilled(long n) {
            if (mod != 0) {
                n = n % mod;
            }
            return within == (n >= lowerBound && n <= upperBound);
        }

        RangeConstraint(int mod, boolean within, long lowerBound, 
                        long upperBound) {
            this.mod = mod;
            this.within = within;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public int updateRepeatLimit(int limit) {
          int mylimit = mod == 0 ? (int)upperBound : mod;
          return Math.max(mylimit, limit);
        }

        public String toString() {
            return "[mod: " + mod + " within: " + within + " low: " + lowerBound + 
                " high: " + upperBound + "]";
        }
    }

    /** Convenience base class for and/or constraints. */
    private static abstract class BinaryConstraint implements Constraint, 
                                                   Serializable {
        private static final long serialVersionUID = 1;
        protected final Constraint a;
        protected final Constraint b;
        private final String conjunction;

        protected BinaryConstraint(Constraint a, Constraint b, String c) {
            this.a = a;
            this.b = b;
            this.conjunction = c;
        }

        public int updateRepeatLimit(int limit) {
            return a.updateRepeatLimit(b.updateRepeatLimit(limit));
        }

        public String toString() {
            return a.toString() + conjunction + b.toString();
        }
    }
      
    /** A constraint representing the logical and of two constraints. */
    private static class AndConstraint extends BinaryConstraint {
        AndConstraint(Constraint a, Constraint b) {
            super(a, b, " && ");
        }

        public boolean isFulfilled(long n) {
            return a.isFulfilled(n) && b.isFulfilled(n);
        }
    }

    /** A constraint representing the logical or of two constraints. */
    private static class OrConstraint extends BinaryConstraint {
        OrConstraint(Constraint a, Constraint b) {
            super(a, b, " || ");
        }

        public boolean isFulfilled(long n) {
            return a.isFulfilled(n) || b.isFulfilled(n);
        }
    }

    /** 
     * Implementation of Rule that uses a constraint.
     * Provides 'and' and 'or' to combine constraints.  Immutable.
     */
  private static class ConstrainedRule implements Rule, Serializable {
        private static final long serialVersionUID = 1;
        private final String keyword;
        private final Constraint constraint;

        public ConstrainedRule(String keyword, Constraint constraint) {
            this.keyword = keyword;
            this.constraint = constraint;
        }

        public Rule and(Constraint c) {
            return new ConstrainedRule(keyword, new AndConstraint(constraint, c));
        }

        public Rule or(Constraint c) {
            return new ConstrainedRule(keyword, new OrConstraint(constraint, c));
        }

        public String getKeyword() {
            return keyword;
        }

        public boolean appliesTo(long n) {
            return constraint.isFulfilled(n);
        }

        public int updateRepeatLimit(int limit) {
            return constraint.updateRepeatLimit(limit);
        }

        public String toString() { 
            return keyword + ": " + constraint;
        }
    }

    /**
     * Implementation of RuleList that is itself a node in a linked list.
     * Immutable, but supports chaining with 'addRule'.
     */
  private static class RuleChain implements RuleList, Serializable {
        private static final long serialVersionUID = 1;
        private final Rule rule;
        private final RuleChain next;

        /** Creates a rule chain with the single rule. */
        public RuleChain(Rule rule) {
            this(rule, null);
        }

        private RuleChain(Rule rule, RuleChain next) {
            this.rule = rule;
            this.next = next;
        }

        public RuleChain addRule(Rule nextRule) {
            return new RuleChain(nextRule, this);
        }

        private Rule selectRule(long n) {
            Rule r = null;
            if (next != null) {
                r = next.selectRule(n);
            }
            if (r == null && rule.appliesTo(n)) {
                r = rule;
            }
            return r;
        }

        public String select(long n) {
            Rule r = selectRule(n);
            if (r == null) {
                return KEYWORD_OTHER;
            }
            return r.getKeyword();
        }

        public Set getKeywords() {
            Set result = new HashSet();
            result.add(KEYWORD_OTHER);
            RuleChain rc = this;
            while (rc != null) {
                result.add(rc.rule.getKeyword());
                rc = rc.next;
            }
            return result;
        }

        public int getRepeatLimit() {
          int result = 0;
          RuleChain rc = this;
          while (rc != null) {
            result = rc.rule.updateRepeatLimit(result);
            rc = rc.next;
          }
          return result;
        }

        public String toString() {
            String s = rule.toString();
            if (next != null) {
                s = next.toString() + "; " + s;
            }
            return s;
        }
    }

    // -------------------------------------------------------------------------
    // Static class methods.
    // -------------------------------------------------------------------------

    /**
     * Provides access to the predefined <code>PluralRules</code> for a given
     * locale.
     * 
     * @param ulocale The locale for which a <code>PluralRules</code> object is
     *   returned.
     * @return The predefined <code>PluralRules</code> object for this locale.
     *   If there's no predefined rules for this locale, the rules
     *   for the closest parent in the locale hierarchy that has one will
     *   be returned.  The final fallback always returns the default 'other' 
     *   rules.
     */
    public static PluralRules forLocale(ULocale locale) {
        PluralRules result = null;
        while (null == (result = (PluralRules) ruleMap.get(locale.getName()))) {
            locale = locale.getFallback();
            if (locale == null) {
              return DEFAULT;
            }
        }
        return result;
    }

    /**
     * Checks whether a token is a valid keyword.
     * 
     * @param token the token to be checked
     * @return true if the token is a valid keyword.
     */
     private static boolean isValidKeyword(String token) {
         if (token.length() > 0 && START_CHARS.contains(token.charAt(0))) {
             for (int i = 1; i < token.length(); ++i) {
                 if (!CONT_CHARS.contains(token.charAt(i))) {
                     return false;
                 }
             }
             return true;
         }
         return false;
     }

    /**
     * Creates a new <code>PluralRules</code> object.  Immutable.
     */
     private PluralRules(RuleList rules) {
         this.rules = rules;
         this.keywords = Collections.unmodifiableSet(rules.getKeywords());
     }

    /**
     * Given a number, returns the keyword of the first rule that applies to
     * the number.
     * 
     * @param number The number for which the rule has to be determined.
     * @return The keyword of the selected rule.
     */
     public String select(long number) {
         return rules.select(number);
     }

    /**
     * Returns a set of all rule keywords used in this <code>PluralRules</code>
     * object.  The rule "other" is always present by default.
     * 
     * @return The set of keywords.
     */
    public Set getKeywords() {
        return keywords;
    }


    public String toString() {
      return "keywords: " + keywords + " rules: " + rules.toString() + 
          " limit: " + getRepeatLimit();
    }

    public int hashCode() {
      return keywords.hashCode();
    }

    public boolean equals(Object rhs) {
        return rhs instanceof PluralRules && equals((PluralRules)rhs);
    }
    
    public boolean equals(PluralRules rhs) {
      if (rhs == null) {
        return false;
      }
      if (rhs == this) { 
        return true;
      }
      if (!rhs.getKeywords().equals(keywords)) {
        return false;
      }

      int limit = Math.max(getRepeatLimit(), rhs.getRepeatLimit());
      for (int i = 0; i < limit; ++i) {
        if (!select(i).equals(rhs.select(i))) {
          return false;
        }
      }
      return true;
    }

    private int getRepeatLimit() {
      if (repeatLimit == 0) {
        repeatLimit = rules.getRepeatLimit() + 1;
      }
      return repeatLimit;
    }

  public static void main(String[] args) {
    System.out.println(PluralRules.createRules(
        "b: n is 13; a: n in 12..13; b: n mod 10 is 2 or n mod 10 is 3"));
  }
}
