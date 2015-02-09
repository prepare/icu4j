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
 * ValueObject and its subclasses follow the contract of the freezable interface.
 * ValueObject adds on the following features to the freezable interface:<br>
 * <ul>
 *   <li>cloneAsThawed does only a shallow copy unless the class contains fields that
 *        are plain old mutable JAVA objects. It accomplishes this by freezing any
 *        freezable fields when cloning.</li>
 *   <li>getMutableXXX() methods to modify the contents of freezable fields in place.</li>
 *   <li>Handles some of the boilerplate code associated with freezable objects</li>
 *   <li>Shallow clones coupled with getMutableXXX methods provide copy-on-write semantics</li>
 * </ul>
 * <br><br>
 * getMutableXXX() methods:<br>
 * <br>
 * A getMutableXXX() method returns a reference whereby the caller can safely modify
 * the corresponding freezable field in place. Think of a getMutableXXX() method as
 * a C++ non-const getter method that returns a non-const pointer.
 
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
 *   private int primitive = 0;
 *   private ImmutablePoint point = ImmutablePoint.valueOf(2, 3);
 *   private FreezableClass value = FreezableClass.DEFAULT;
 *   private FreezableClass optionalValue = null;
 *   private MutableClass pojo = new MutableClass();
 *   
 *   // Default instance. This has to be defined after all the fields because of
 *   // the way JAVA initializes a class. Declaring this before any of the fields
 *   // may result in exception in initialization errors caused by null pointer exception.
 *   public static final MyClass DEFAULT = new MyClass().freeze();
 *   
 *   public int getPrimitive() { return primitive; }
 *   
 *   public void doSomeMutations() {
 *       // Throw an exception if this object is frozen.
 *       checkThawed();
 *       
 *       // do a mutation.
 *       primitive = 3;
 *       
 *       // do another mutation. Always thaw a freezable field first before modifying it.
 *       value = thaw(value);
 *       value.setFoo(7);     
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
 *       // throw exception if this object is frozen.
 *       checkThawed();
 *       point = p;
 *   }
 *   
 *   // getXXX methods on freezable fields return a direct reference to that field.
 *   // If this object is frozen, the returned field is frozen; if this object
 *   // is not frozen, the returned field may or may not be frozen because of
 *   // shallow cloning. Therefore, a caller should use getMutableXXX to modify a
 *   // freezable field in place and should use getXXX only to get a read-only view
 *   // of the corresponding freezable field. If a caller needs to hold onto the
 *   // returned reference, it should freeze it. Note that setter methods on
 *   // ValueObjects freeze their parameter for the caller.
 *   public FreezableClass getValue() { return value; }
 *   
 *   // getMutableXXX methods guarantee that their corresponding
 *   // field is unfrozen and that the caller can modify it through the
 *   // returned reference. This guarantee holds only until this object is
 *   // frozen or cloned. getMutableXXX methods only work if this object is
 *   // not frozen.
 *   public FreezableClass getMutableValue() {
 *       // Be sure this object is not frozen
 *       checkThawed();
 *       value = thaw(value);
 *       return value;
 *   }
 *   
 *   // Setters of freezable fields always freeze their parameter to
 *   // help prevent shared, unfrozen objects.
 *   public void setValue(FreezableClass v) {
 *       // Be sure this object is not frozen.
 *       checkThawed();
 *       this.value = v.freeze();
 *   }
 *   
 *   public FreezableClass getOptionalValue() { return optionalValue; }
 *   
 *   // Setters of freezable fields always freeze their parameter to
 *   // help prevent shared, unfrozen objects. 
 *   public void setOptionalValue(FreezableClass v) {
 *       checkThawed();
 *       this.optionalValue = optFreeze(v);
 *   }
 *   
 *   // A plain old mutable field getter must always return either clone of the
 *   // field or an umodifiable view of the field. If this method ever returned a
 *   // direct reference to its field even while unfrozen, the caller could use
 *   // that reference to make changes even after this object is frozen.
 *   public MutableClass getPojo() {
 *     return pojo.clone();
 *   }
 *   
 *   // A setter of a plain old mutable field always makes a defensive copy.
 *   public void setPojo(MutableClass o) {
 *       checkThawed();
 *       pojo = o.clone();
 *   }
 *   
 *   // If MyClass contained no ordinary mutable fields,
 *   // then it would not be necessary to override clone.
 *   public MyClass clone() {
 *      MyClass result = super.clone();
 *      // Clone only the mutable fields. The base class clone takes care of freezing
 *      // any freezable fields.
 *      result.pojo = result.pojo.clone();
 *      return result;
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

    /** Returns a thawed clone.
     * Subclasses override only if they contain plain old mutable fields that are not
     * freezable.
     */
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
    public final boolean isFrozen() {
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
        if (!bFrozen) {
            freezeFields();
        }
        bFrozen = true;
        return (T) this;
    }
    
    /**
     * freeze() calls this to freeze any fields that implement freezable if
     * this object was not previously frozen already.
     * Subclasses that have freezable fields override this method to freeze
     * those fields. subclasses can use optFreeze to freeze fields that
     * could be null. Subclasses should override this method to do nothing more than
     * freeze fields. Mutating other state may create data races.
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
     * Call within the freezeFields method or a setXXX method.
     * to freeze an optional, freezable field.
     * See coding example in documentation.
     * @param value The value to freeze. value may be null.
     * @return value itself
     */
    protected static <U extends Freezable<U>> U optFreeze(U value) {
        if (value != null) {
            return value.freeze();
        }
        return value;
    }
    
    /**
     * Call from a mutating method to thaw a freezable a field so that it can be
     * modified like this.<br>
     * <pre>
     *   fieldToBeMutated = thaw(fieldToBeMutated);
     * </pre>
     * <br>
     * If fieldToBeMutated is frozen, thaw returns a thawed clone of it; if
     * fieldToBeMutated is not frozen, thaw returns fieldToBeMutated unchanged.
     * fieldToBeMutated must be non-null.
     */
    @SuppressWarnings("unchecked")
    protected static <U extends Freezable<U>> U thaw(U fieldToBeMutated) {
        if (!fieldToBeMutated.isFrozen()) {
            return fieldToBeMutated;
        }
        return fieldToBeMutated.cloneAsThawed();
    }

    private volatile boolean bFrozen = false;
}
