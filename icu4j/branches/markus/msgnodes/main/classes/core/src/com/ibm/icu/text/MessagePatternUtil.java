/*
*******************************************************************************
*   Copyright (C) 2011, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2011jul14
*   created by: Markus W. Scherer
*/

package com.ibm.icu.text;

import com.ibm.icu.text.MessagePattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for working with a MessagePattern.
 * Intended for use in tools when convenience is more important than
 * minimizing runtime and object creations.
 *
 * <p>This class and its nested classes are not intended for public subclassing.
 * @draft ICU 49
 * @provisional This API might change or be removed in a future release.
 * @author Markus Scherer
 */
public final class MessagePatternUtil {
  /**
   * Factory method, builds and returns a MessageNode from a MessageFormat pattern string.
   * @param patternString a MessageFormat pattern string
   * @return a MessageNode or a ComplexArgStyleNode
   * @throws IllegalArgumentException if the MessagePattern is empty
   *         or does not represent a MessageFormat pattern
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static MessageNode buildMessageNode(String patternString) {
    return buildMessageNode(new MessagePattern(patternString));
  }

  /**
   * Factory method, builds and returns a MessageNode from a MessagePattern.
   * @param pattern a parsed MessageFormat pattern string
   * @return a MessageNode or a ComplexArgStyleNode
   * @throws IllegalArgumentException if the MessagePattern is empty
   *         or does not represent a MessageFormat pattern
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static MessageNode buildMessageNode(MessagePattern pattern) {
    int limit = pattern.countParts() - 1;
    if (limit < 0) {
      throw new IllegalArgumentException("The MessagePattern is empty");
    } else if (pattern.getPartType(0) != MessagePattern.Part.Type.MSG_START) {
      throw new IllegalArgumentException(
          "The MessagePattern does not represent a MessageFormat pattern");
    }
    return buildMessageNode(pattern, 0, limit);
  }

  /**
   * Common base class for all elements in a tree of nodes
   * returned by {@link MessagePatternUtil#buildMessageNode(MessagePattern)}.
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static class Node {
    /**
     * @return the MessagePattern's Part index for the start Part for this node;
     *         -1 if there is no corresponding Part (e.g., for TEXT).
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public int getStartPartIndex() {
      return start;
    }
    /**
     * Returns the same index as {@link #getStartPartIndex()} for simple nodes.
     * @return the MessagePattern's Part index for the limit Part for this node
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public int getLimitPartIndex() {
      return limit;
    }
    // TODO(API): see if the start/limit part indexes (and other Part indexes in other classes)
    // are useful
    // TODO(API): need/want getParent()?

    private Node(int start, int limit) {
      this.start = start;
      this.limit = limit;
    }

    // TODO(impl): should each Node have a reference to its MessagePattern?
    // TODO(impl): should field values be dynamically fetched from the MessagePattern as much as possible?
    int start;
    int limit;
  }

  /**
   * A Node representing a parsed MessageFormat pattern string.
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static class MessageNode extends Node {
    /**
     * @return the list of MessageContentsNode nodes that this message contains
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public List<MessageContentsNode> getContents() {
      return list;
    }
    /**
     * {@inheritDoc}
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    @Override
    public String toString() {
      return list.toString();
    }

    private MessageNode(int start, int limit) {
      super(start, limit);
    }
    private void addContentsNode(MessageContentsNode node) {
      if (node instanceof TextNode && !list.isEmpty()) {
        // Coalesce adjacent text nodes.
        MessageContentsNode lastNode = list.get(list.size() - 1);
        if (lastNode instanceof TextNode) {
          TextNode textNode = (TextNode)lastNode;
          textNode.text = textNode.text + ((TextNode)node).text;
          return;
        }
      }
      list.add(node);
    }
    private MessageNode freeze() {
      list = Collections.unmodifiableList(list);
      return this;
    }

    private List<MessageContentsNode> list = new ArrayList<MessageContentsNode>();
  }

  /**
   * A piece of MessageNode contents.
   * Use getType() to determine the type and the actual Node subclass.
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static class MessageContentsNode extends Node {
    /**
     * The type of a piece of MessageNode contents.
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public enum Type {
      /**
       * This is a TextNode containing literal text (downcast and call getText()).
       * @draft ICU 49
       * @provisional This API might change or be removed in a future release.
       */
      TEXT,
      /**
       * This is an ArgNode representing a message argument (downcast and use specific methods).
       * @draft ICU 49
       * @provisional This API might change or be removed in a future release.
       */
      ARG,
      /**
       * This Node represents a place in a plural argument's variant where
       * the formatted (plural-offset) value is to be put.
       * @draft ICU 49
       * @provisional This API might change or be removed in a future release.
       */
      REPLACE_NUMBER
    }
    /**
     * Returns the type of this piece of MessageNode contents.
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public Type getType() {
      return type;
    }
    /**
     * {@inheritDoc}
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    @Override
    public String toString() {
      return "{REPLACE_NUMBER}";
    }

    private MessageContentsNode(int start, int limit, Type type) {
      super(start, limit);
      this.type = type;
    }
    private static MessageContentsNode createReplaceNumberNode(int start) {
      return new MessageContentsNode(start, start, Type.REPLACE_NUMBER);
    }

    private Type type;
  }

  /**
   * Literal text, a piece of MessageNode contents.
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static class TextNode extends MessageContentsNode {
    /**
     * @return the literal text at this point in the message
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public String getText() {
      return text;
    }
    /**
     * {@inheritDoc}
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    @Override
    public String toString() {
      return "«" + text + "»";
    }

    private TextNode(String text) {
      super(-1, -1, Type.TEXT);
      this.text = text;
    }

    private String text;
  }

  /**
   * A piece of MessageNode contents representing a message argument and its details.
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static class ArgNode extends MessageContentsNode {
    /**
     * @return the argument type
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public MessagePattern.ArgType getArgType() {
      return argType;
    }
    /**
     * @return the argument name string (the decimal-digit string if the argument has a number)
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public String getName() {
      return name;
    }
    /**
     * @return the argument number, or -1 if none (for a named argument)
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public int getNumber() {
      return number;
    }
    /**
     * @return the argument type string, or null if none was specified
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public String getTypeName() {
      return typeName;
    }
    /**
     * @return the simple-argument style string,
     *         or null if no style is specified and for other argument types
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public String getSimpleStyle() {
      return style;
    }
    /**
     * @return the complex-argument-style object,
     *         or null if the argument type is NONE_ARG or SIMPLE_ARG
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public ComplexArgStyleNode getComplexStyle() {
      return complexStyle;
    }
    /**
     * {@inheritDoc}
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('{').append(name);
      if (argType != MessagePattern.ArgType.NONE) {
        sb.append(',').append(typeName);
        if (argType == MessagePattern.ArgType.SIMPLE) {
          if (style != null) {
            sb.append(',').append(style);
          }
        } else {
          sb.append(',').append(complexStyle.toString());
        }
      }
      return sb.append('}').toString();
    }

    private ArgNode(int start, int limit) {
      super(start, limit, Type.ARG);
    }
    private static ArgNode createArgNode(int start, int limit) {
      return new ArgNode(start, limit);
    }

    private MessagePattern.ArgType argType;
    private String name;
    private int number = -1;
    private String typeName;
    private String style;
    private ComplexArgStyleNode complexStyle;
  }

  /**
   * A Node representing details of the argument style of a complex argument.
   * (Which is a choice/plural/select argument which selects among nested messages.)
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static class ComplexArgStyleNode extends Node {
    /**
     * @return the argument type (same as getArgType() on the parent ArgNode)
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public MessagePattern.ArgType getArgType() {
      return argType;
    }
    /**
     * @return true if this is a plural style with an explicit offset
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public boolean hasExplicitOffset() {
      return offsetPartIndex >= 0;
    }
    /**
     * @return the plural offset, or 0 if this is not a plural style or
     *         the offset is explicitly or implicitly 0
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public double getOffset() {
      return offset;
    }
    /**
     * @return the plural offset Part index, or -1 if there is none
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public int getOffsetPartIndex() {
      // TODO(API): If part indexes are not otherwise useful,
      // then we only need hasExplicitOffset() (above) instead of this method.
      return offsetPartIndex;
    }
    /**
     * @return the list of variants: the nested messages with their selection criteria
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public List<VariantNode> getVariants() {
      return list;
    }
    /**
     * Separates the variants by type.
     * Intended for use with plural and select argument styles,
     * not useful for choice argument styles.
     *
     * <p>Both parameters are used only for output, and are first cleared.
     * @param numericVariants Variants with numeric-value selectors (if any) are added here.
     *        Can be null for a select argument style.
     * @param keywordVariants Variants with keyword selectors, except "other", are added here.
     *        For a plural argument, if this list is empty after the call, then
     *        all variants except "other" have explicit values and PluralRules need not be called.
     * @return the "other" variant (the first one if there are several, null if none [choice style])
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public VariantNode getVariantsByType(List<VariantNode> numericVariants,
                                         List<VariantNode> keywordVariants) {
      if (numericVariants != null) {
        numericVariants.clear();
      }
      keywordVariants.clear();
      VariantNode other = null;
      for (VariantNode variant : list) {
        if (variant.isSelectorNumeric()) {
          numericVariants.add(variant);
        } else if ("other".equals(variant.getSelector())) {
          if (other == null) {
            // Return the first "other" variant. (MessagePattern allows duplicates.)
            other = variant;
          }
        } else {
          keywordVariants.add(variant);
        }
      }
      return other;
    }
    /**
     * {@inheritDoc}
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('(').append(argType.toString()).append(" style) ");
      if (hasExplicitOffset()) {
        sb.append("offset:").append(offset).append(' ');
      }
      return sb.append(list.toString()).toString();
    }

    private ComplexArgStyleNode(int start, int limit, MessagePattern.ArgType argType) {
      super(start, limit);
      this.argType = argType;
    }
    private void addVariant(VariantNode variant) {
      list.add(variant);
    }
    private ComplexArgStyleNode freeze() {
      list = Collections.unmodifiableList(list);
      return this;
    }

    private MessagePattern.ArgType argType;
    private double offset;
    private int offsetPartIndex = -1;
    private List<VariantNode> list = new ArrayList<VariantNode>();
  }

  /**
   * A Node representing a nested message (nested inside an argument)
   * with its selection criterium.
   * @draft ICU 49
   * @provisional This API might change or be removed in a future release.
   */
  public static class VariantNode extends Node {
    /**
     * Returns the selector string.
     * For example: A plural/select keyword ("few"), a plural explicit value ("=1"),
     * a choice comparison operator ("#").
     * @return the selector string
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public String getSelector() {
      return selector;
    }
    /**
     * @return true for choice variants and for plural explicit values
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public boolean isSelectorNumeric() {
      return numericValue != MessagePattern.NO_NUMERIC_VALUE;
    }
    /**
     * @return the selector's numeric value, or NO_NUMERIC_VALUE if !isSelectorNumeric()
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public double getSelectorValue() {
      return numericValue;
    }
    /**
     * @return the nested message
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    public MessageNode getMessage() {
      return msgNode;
    }
    /**
     * {@inheritDoc}
     * @draft ICU 49
     * @provisional This API might change or be removed in a future release.
     */
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (isSelectorNumeric()) {
        sb.append(numericValue).append(" (").append(selector).append(") {");
      } else {
        sb.append(selector).append(" {");
      }
      return sb.append(msgNode.toString()).append('}').toString();
    }

    private VariantNode(int start, int limit) {
      super(start, limit);
    }
    // TODO: Do these part indexes make sense for this class?

    private String selector;
    private double numericValue = MessagePattern.NO_NUMERIC_VALUE;
    private MessageNode msgNode;
  }

  private static MessageNode buildMessageNode(MessagePattern pattern, int start, int limit) {
    int prevPatternIndex = pattern.getPart(start).getLimit();
    MessageNode node = new MessageNode(start, limit);
    for (int i = start + 1;; ++i) {
      MessagePattern.Part part = pattern.getPart(i);
      int patternIndex = part.getIndex();
      if (prevPatternIndex < patternIndex) {
        node.addContentsNode(new TextNode(pattern.getPatternString().substring(prevPatternIndex,
                                                                   patternIndex)));
      }
      if (i == limit) {
        break;
      }
      MessagePattern.Part.Type partType = part.getType();
      if (partType == MessagePattern.Part.Type.ARG_START) {
        int argLimit = pattern.getLimitPartIndex(i);
        node.addContentsNode(buildArgNode(pattern, i, argLimit));
        i = argLimit;
        part = pattern.getPart(i);
      } else if (partType == MessagePattern.Part.Type.REPLACE_NUMBER) {
        node.addContentsNode(MessageContentsNode.createReplaceNumberNode(i));
      // else: ignore SKIP_SYNTAX and INSERT_CHAR parts.
      }
      prevPatternIndex = part.getLimit();
    }
    return node.freeze();
  }

  private static ArgNode buildArgNode(MessagePattern pattern, int start, int limit) {
    ArgNode node = ArgNode.createArgNode(start, limit);
    MessagePattern.Part part = pattern.getPart(start);
    MessagePattern.ArgType argType = node.argType = part.getArgType();
    part = pattern.getPart(++start);  // ARG_NAME or ARG_NUMBER
    node.name = pattern.getSubstring(part);
    if (part.getType() == MessagePattern.Part.Type.ARG_NUMBER) {
      node.number = part.getValue();
    }
    ++start;
    switch(argType) {
    case SIMPLE:
      // ARG_TYPE
      node.typeName = pattern.getSubstring(pattern.getPart(start++));
      if (start < limit) {
        // ARG_STYLE
        node.style = pattern.getSubstring(pattern.getPart(start));
      }
      break;
    case CHOICE:
      node.typeName = "choice";
      node.complexStyle = buildChoiceStyleNode(pattern, start, limit);
      break;
    case PLURAL:
      node.typeName = "plural";
      node.complexStyle = buildPluralStyleNode(pattern, start, limit);
      break;
    case SELECT:
      node.typeName = "select";
      node.complexStyle = buildSelectStyleNode(pattern, start, limit);
      break;
    default:
      // NONE type, nothing else to do
      break;
    }
    return node;
  }

  private static ComplexArgStyleNode buildChoiceStyleNode(MessagePattern pattern,
                                                          int start, int limit) {
    ComplexArgStyleNode node = new ComplexArgStyleNode(start, limit, MessagePattern.ArgType.CHOICE);
    while (start < limit) {
      int valueIndex = start;
      MessagePattern.Part part = pattern.getPart(start);
      double value = pattern.getNumericValue(part);
      start += 2;
      int msgLimit = pattern.getLimitPartIndex(start);
      VariantNode variant = new VariantNode(valueIndex, msgLimit);
      variant.selector = pattern.getSubstring(pattern.getPart(valueIndex + 1));
      variant.numericValue = value;
      variant.msgNode = buildMessageNode(pattern, start, msgLimit);
      node.addVariant(variant);
      start = msgLimit + 1;
    }
    return node.freeze();
  }

  private static ComplexArgStyleNode buildPluralStyleNode(MessagePattern pattern,
                                                          int start, int limit) {
    ComplexArgStyleNode node = new ComplexArgStyleNode(start, limit, MessagePattern.ArgType.PLURAL);
    MessagePattern.Part offset = pattern.getPart(start);
    if (offset.getType().hasNumericValue()) {
      node.offsetPartIndex = start++;
      node.offset = pattern.getNumericValue(offset);
    }
    while (start < limit) {
      int selectorIndex = start;
      MessagePattern.Part selector = pattern.getPart(start++);
      double value = MessagePattern.NO_NUMERIC_VALUE;
      MessagePattern.Part part = pattern.getPart(start);
      if (part.getType().hasNumericValue()) {
        value = pattern.getNumericValue(part);
        ++start;
      }
      int msgLimit = pattern.getLimitPartIndex(start);
      VariantNode variant = new VariantNode(selectorIndex, msgLimit);
      variant.selector = pattern.getSubstring(selector);
      variant.numericValue = value;
      variant.msgNode = buildMessageNode(pattern, start, msgLimit);
      node.addVariant(variant);
      start = msgLimit + 1;
    }
    return node.freeze();
  }

  private static ComplexArgStyleNode buildSelectStyleNode(MessagePattern pattern,
                                                          int start, int limit) {
    ComplexArgStyleNode node = new ComplexArgStyleNode(start, limit, MessagePattern.ArgType.SELECT);
    while (start < limit) {
      int selectorIndex = start;
      MessagePattern.Part selector = pattern.getPart(start++);
      int msgLimit = pattern.getLimitPartIndex(start);
      VariantNode variant = new VariantNode(selectorIndex, msgLimit);
      variant.selector = pattern.getSubstring(selector);
      variant.msgNode = buildMessageNode(pattern, start, msgLimit);
      node.addVariant(variant);
      start = msgLimit + 1;
    }
    return node.freeze();
  }
}
