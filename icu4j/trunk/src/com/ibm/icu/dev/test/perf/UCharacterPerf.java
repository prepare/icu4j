/**
 *******************************************************************************
 * Copyright (C) 2001-2002, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/perf/UCharacterPerf.java,v $
 * $Date: 2002/10/30 01:32:45 $
 * $Revision: 1.1 $
 *
 *******************************************************************************
 */

package com.ibm.icu.dev.test.perf;

import com.ibm.icu.lang.UCharacter;

/**
 * Base performance test that takes in a method name for testing with JDK.
 * To use 
 * <code>
 * java com.ibm.icu.dev.test.perf.UCharacterPerf $MethodName $LoopCount - 
 *                                  $START_TEST_CHARACTER $END_TEST_CHARACTER
 * </code>
 * where $*_TEST_CHARACTER are in hex decimals with a leading 0x
 */
public final class UCharacterPerf extends PerfTest 
{
    // public methods ------------------------------------------------------
    
	public static void main(String[] args) throws Exception
    {   
        new UCharacterPerf().run(args);
	    // new UCharacterPerf().TestPerformance();
	}
    
    protected void setup(String[] args) {
        // We only take one argument, the pattern
        if (args.length < 1) {
            MIN_ = Character.MIN_VALUE;
            MAX_ = Character.MAX_VALUE;
        }
        MIN_ = Integer.parseInt(args[0], 16);
        if (args.length >= 2) {
            MAX_ = Integer.parseInt(args[1], 16);
        }
    }
    
    PerfTest.Function testDigit() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.digit(ch, 10);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testJDKDigit() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.digit(ch, 10);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testGetNumericValue() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.getNumericValue(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }

    PerfTest.Function testJDKGetNumericValue() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.getNumericValue(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
     
    PerfTest.Function testGetType() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.getType(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }   

    PerfTest.Function testJDKGetType() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.getType(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }

    PerfTest.Function testIsDefined() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isDefined(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }    
    
    PerfTest.Function testJDKIsDefined() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isDefined(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }    

    PerfTest.Function testIsDigit() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isDigit(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }    
    
    PerfTest.Function testJDKIsDigit() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isDigit(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testIsIdentifierIgnorable() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isIdentifierIgnorable(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }    
    
    PerfTest.Function testJDKIsIdentifierIgnorable() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isIdentifierIgnorable(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testIsISOControl() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isISOControl(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testJDKIsISOControl() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isISOControl(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testIsLetter() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isLetter(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testJDKIsLetter() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isLetter(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testIsLetterOrDigit() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isLetterOrDigit(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testJDKIsLetterOrDigit() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isLetterOrDigit(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }

    PerfTest.Function testIsLowerCase() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isLowerCase(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }    
    
    PerfTest.Function testJDKIsLowerCase() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isLowerCase(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }    
    
    PerfTest.Function testIsSpaceChar() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isSpaceChar(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    } 
    
    PerfTest.Function testJDKIsSpaceChar() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isSpaceChar(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }  
    
    PerfTest.Function testIsTitleCase() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isTitleCase(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testJDKIsTitleCase() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isTitleCase(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }    
    
    PerfTest.Function testIsUnicodeIdentifierPart() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isUnicodeIdentifierPart(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    } 
    
    PerfTest.Function testJDKIsUnicodeIdentifierPart() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isUnicodeIdentifierPart(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    } 
    
    PerfTest.Function testIsUnicodeIdentifierStart() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isUnicodeIdentifierStart(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    } 
    
    PerfTest.Function testJDKIsUnicodeIdentifierStart() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isUnicodeIdentifierStart(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    } 
    
    PerfTest.Function testIsUpperCase() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    UCharacter.isUpperCase(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testJDKIsUpperCase() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isUpperCase(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }    
         
    PerfTest.Function testIsWhiteSpace() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (int ch = MIN_; ch < MAX_; ch ++) {
                    UCharacter.isWhitespace(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    PerfTest.Function testJDKIsWhiteSpace() 
    {
        return new PerfTest.Function() {
            public void call() {
                for (char ch = (char)MIN_; ch < (char)MAX_; ch ++) {
                    Character.isWhitespace(ch);
                }
            }

            public long getOperationsPerIteration() {
                return MAX_;
            }
        };
    }
    
    // private data member --------------------------------------------------
    
    /**
     * Minimum codepoint to do test. Test is ran from MIN_ to MAX_
     */
    private static int MIN_;
    /**
     * Minimum codepoint to do test. Test is ran from MIN_ to MAX_
     */
    private static int MAX_;
}
