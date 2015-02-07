/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* SharedObject.java, ported from sharedobject.h/.cpp
*
* C++ version created on: 2013dec19
* created by: Markus W. Scherer
*/

package com.ibm.icu.impl;


import com.ibm.icu.util.Freezable;
import com.ibm.icu.util.ICUCloneNotSupportedException;

/**
 * ValueObject is the base class of objects that are designed mimic C++ value objects
 * (objects that override the assignment operator).
 * ValueObject and it subclasses follow the contract of the freezable interface.
 * ValueObject adds on the following features to the freezable interface:<br>
 * <ul>
 *   <li>cloneAsThawed does only a shallow copy unless the class contains fields that
 *        are plain old mutable JAVA objects. It accomplishes this by freezing any
 *        freezable fields.</li>
 *   <li>getMutableXXX() methods to modify the contents of freezable fields in place.</li>
 * </ul>
 * <br><br>
 * getMutableXXX() methods:<br>
 * <br>
 * A getMutableXXX() method returns a reference whereby the caller can safely modify
 * the corresponding freezable field in place. Think of a getMutableXXX() method as
 * a C++ non-const getter method that returns a non-const pointer.
 * A getMutableXXX() method accomplishes this by thawing the corresponding frozen
 * field in place and returning that thawed field.
 * Thawing a field means doing nothing if it is not frozen and
 * replacing the field with a thawed clone if it is frozen.
 * Calling a getMutableXXX() method on a frozen object throws an exception.
 * Callers should never attempt to modify an embedded object in
 * place via a getXXX() method as this could result in thrown exceptions if the embedded
 * object is frozen (Remember that cloneAsThawed() only does shallow copies).
 * <br><br>
 * Writing your own ValueObject class:<br>
 * <br>
 * To write your own ValueObject class consider the fields/attributes of the
 * class. Field types fall into the following categories.<br>
 * <ol>
 *   <li>Primitive types and immutable types</li>
 *   <li>Freezable types</li>
 *   <li>Other mutable types</li>
 * </ol><br>
 * <p>
 * The code below is a sample subclass of ValueObject.<br>
 * <p>
 * <pre>
 * public class MyClass extends ValueObject<MyClass> {
 * 
 *   // Default instance
 *   public static final MyClass DEFAULT = new MyClass().freeze();
 *   
 *   private int primitive = 0;
 *   private ImmutablePoint point = ImmutablePoint.valueOf(2, 3);
 *   private FreezableClass value = FreezableClass.DEFAULT;
 *   private FreezableClass optionalValue = null;
 *   private MutableClass pojo = new MutableClass();
 *   
 *   public int getPrimitive() { return primitive; }
 *   
 *   public void doSomeMutation() {
 *       // Throw an exception if this object is frozen.
 *       checkThawed();
 *       // do the mutation.
 *       primitive = 3;
 *       getMutableValue().setFoo(7);     
 *   }
 *   
 *   public void setPrimitive(int i) {
 *       // Throw an exception if this object is frozen.
 *       checkThawed();
 *       primitive = i;
 *   }
 *   
 *   public void getPoint() { return point; }
 *   public void setPoint(ImmutablePoint p) {
 *       checkThawed();
 *       point = p;
 *   }
 *   
 *   public FreezableClass getValue() { return value; }
 *   
 *   // Remember getMutableXXX methods cannot be called
 *   // on frozen objects.
 *   public FreezableClass getMutableValue() {
 *       // Throws exception if this object is frozen. Otherwise
 *       // thaws the 'value' field so caller can change it.
 *       value = safeThaw(value);
 *       return value;
 *   }
 *   public void setValue(FreezableClass v) {
 *       // Throws exception if this object is frozen.
 *       // or v is null. Otherwise
 *       // freezes v before setting it to value.
 *       this.value = safeSet(v);
 *   }
 *   
 *   public FreezableClass getOptionalValue() { return optionalValue; }
 *   
 *   public void setOptionalValue(FreezableClass v) {
 *       // like safeSet, but handles v == null.
 *       this.value = optSafeSet(v);
 *   }
 *   
 *   public MutableClass getPojo() { return pojo.clone(); }
 *   public void setPojo(MutableClass o) {
 *       checkThawed();
 *       pojo = o == null ? null : o.clone();
 *   }
 *   
 *   // If MyClass contained no ordinary mutable fields,
 *   // then it would not be necessary to override clone.
 *   public MyClass clone() {
 *     
 *       // For each mutable field, clone
 *       if (pojo != null)
 *           pojo = pojo.clone();
 *       }
 *   }
 *   
 *   // Subclasses override only if they have freezable fields
 *   protected void freezeFields() {
 *       value.freeze();
 *       optFreeze(optionalValue);
 *   }
 *   
 * }
 * </pre>
 * 
 * @param <T> The subclass of ValueObject.
 */
public abstract class ValueObject<T extends ValueObject<T>> implements Freezable<T>, Cloneable {

    /** Initially not frozen */
    public ValueObject() {}

    /** Returns a thawed clone. */
    @SuppressWarnings("unchecked")
    @Override
    public T clone() {
        T c;
        try {
            c = (T)super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen.
            throw new ICUCloneNotSupportedException(e);
        }
        c.bFrozen = false;
        c.freezeFields();
        return c;   
    }
    
    /**
     * Returns whether or not this object is frozen according to the contract of freezable.
     */
    public boolean isFrozen() {
        return bFrozen;
    }
    
    /**
     * Returns a thawed clone according to the contract of the freezable interface.
     * Same as clone().
     */
    public final T cloneAsThawed() {
        return clone();
    }
    
    /**
     * Freezes this object and returns this.
     */
    @SuppressWarnings("unchecked")
    public final T freeze() { 
        boolean wasFrozen = bFrozen;
        bFrozen = true;
        if (!wasFrozen) {
            freezeFields();
        }
        return (T) this;
    }
    
    /**
     * freeze() calls this to freeze any fields that implement freezable if
     * this object was not previously frozen already.
     * Subclasses that have freezable fields override this method to freeze
     * those fields. subclasses can use optFreeze to freeze fields that
     * could be null.
     */
    protected void freezeFields() {
        // Default implementation assumes there are no
        // fields that implement Freezable.
    }
    
    /**
     * Call first thing in a mutating method such as a setXXX method to
     * ensure this object is thawed.
     */
    protected final void checkThawed() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Cannot modify a frozen object");
        }
    }
    
    /**
     * Call within the setXXX method for an optional, freezable field. See coding
     * example in documentation.
     * @param newValue the new value. May be null.
     * @return Returns the new value or null.
     */
    protected final <U extends Freezable<U>> U optSafeSet(U newValue) {
        checkThawed();
        if (newValue == null) {
            return null;
        }
        return newValue.freeze();
    }
    
    /**
     * Call within the setXXX method for a required, freezable field. See coding
     * example in documentation.
     * @param newValue the new value.
     * @return Returns the new value.
     */
    protected final <U extends Freezable<U>> U safeSet(U newValue) {
        if (newValue == null) {
            throw new NullPointerException("null not allowed in field.");
        }
        checkThawed();
        return newValue.freeze();
    }
    
    /**
     * Call within a getMutableXXX method to thaw the corresponding
     * required ValueObject field. See coding example in documentation.
     * @param value the value to thaw
     * @return the thawed value.
     */
    protected final <U extends Freezable<U>> U safeThaw(U value) {
        checkThawed();
        return thaw(value);
    }
    
    /**
     * Call within the clone method or freezeFields method
     * to freeze an optional, freezable field.
     * See coding example in documentation.
     * @param value The value to freeze. May be null.
     */
    protected final void optFreeze(Freezable<?> value) {
        if (value != null) {
            value.freeze();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <U extends Freezable<U>> U thaw(U value) {
        if (!value.isFrozen()) {
            return value;
        }
        return value.cloneAsThawed();
    }

    private volatile boolean bFrozen = false;
}
