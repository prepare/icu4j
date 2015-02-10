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
 * ValueObject and its subclasses follow the contract of the Freezable interface.
 * ValueObject adds on the following features to the Freezable interface:<br>
 * <ul>
 *   <li>cloneAsThawed does only a shallow copy unless the class contains fields that
 *        are plain old mutable JAVA objects. It accomplishes this by freezing any
 *        ValueObject fields when cloning and ensuring that other Freezable objects
 *        are always frozen.</li>
 *   <li>getMutableXXX() methods to modify the contents of ValueObject fields in place.</li>
 *   <li>Handles some of the boilerplate code associated with Freezable objects</li>
 *   <li>Shallow clones coupled with getMutableXXX methods provide copy-on-write semantics</li>
 * </ul>
 * <br><br>
 * getMutableXXX() methods:<br>
 * <br>
 * A getMutableXXX() method returns a reference whereby the caller can safely modify
 * the corresponding ValueObject field in place. Think of a getMutableXXX() method as
 * a C++ non-const getter method that returns a non-const pointer.
 
 * <br><br>
 * Writing your own ValueObject class:<br>
 * <br>
 * To write your own ValueObject class consider the fields/attributes of the
 * class. Field types fall into the following categories.<br>
 * <ol>
 *   <li>Primitive types and immutable types</li>
 *   <li>ValueObject subclasses</li>
 *   <li>Freezable implementations that are not ValueObject</li>
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
 *   private ValueObjectClass value = ValueObjectClass.DEFAULT;
 *   private ValueObjectClass optionalValue = null;
 *   private FreezableClass freezable = new FreezableClass().freeze();
 *   private MutableClass pojo = new MutableClass();
 *   
 *   // Default instance. This has to be defined after all the fields because of
 *   // the way JAVA initializes a class. Declaring this before any of the fields
 *   // may result in exception in initialization errors caused by null pointer exception.
 *   public static final MyClass DEFAULT = new MyClass().freeze();
 *   
 *   public void doSomeMutations() {
 *       // Throw an exception if this object is frozen.
 *       checkThawed();
 *       
 *       // Modify a primitive.
 *       primitive = 3;
 *       
 *       // Modify a ValueObject. Always thaw a ValueObject field first before modifying it.
 *       value = thaw(value);
 *       value.setFoo(7);
 *       
 *       // Modify an optional ValueOjbect.
 *       if (optionalValue != null) {
 *           optionalValue = thaw(optionalValue);
 *           optionalValue.setFoo(11);
 *       }
 *       
 *       // Modify a Freezable field. Freezable fields must ALWAYS be frozen within
 *       // a ValueObject. 
 *       FreezableClass copy = freezable.cloneAsThawed();
 *       copy.doSomeMutation();
 *       freezable = copy.freeze();
 *       
 *       // Modify a plain old mutable object in place as these are deep
 *       // copied during cloning.
 *       pojo.doSomeMutation();
 *   }
 *   
 *   public int getPrimitive() { return primitive; }
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
 *   // getXXX methods on ValueObject fields return a direct reference to that field.
 *   // If this object is frozen, the returned field is frozen; if this object
 *   // is not frozen, the returned field may or may not be frozen.
 *   // Therefore, a caller should use getMutableXXX to modify a ValueObject
 *   // field in place and should use getXXX only to get a read-only view
 *   // of the corresponding ValueObject field. If a caller needs to hold onto the
 *   // returned reference, it should freeze it.
 *   public ValueObjectClass getValue() { return value; }
 *   
 *   // getMutableXXX methods guarantee that their corresponding ValueObject
 *   // field is unfrozen and that the caller can modify it through the
 *   // returned reference. This guarantee holds only until this object is
 *   // frozen or cloned. getMutableXXX methods only work if this object is
 *   // not frozen.
 *   public ValueObjectClass getMutableValue() {
 *       // Be sure this object is not frozen
 *       checkThawed();
 *       value = thaw(value);
 *       return value;
 *   }
 *   
 *   // Setters of ValueObject fields always freeze their parameter to
 *   // help prevent shared, unfrozen objects.
 *   public void setValue(ValueObjectClass v) {
 *       // Be sure this object is not frozen.
 *       checkThawed();
 *       this.value = v.freeze();
 *   }
 *   
 *   // Optional ValueObject fields work just like required ones except that their is
 *   // no getMutableXXX() method.
 *   public ValueObjectClass getOptionalValue() { return optionalValue; }
 *    
 *   public void setOptionalValue(ValueObjectClass v) {
 *       checkThawed();
 *       // We use optFreeze since v may be null.
 *       this.optionalValue = optFreeze(v);
 *   }
 *   
 *   // getXXX methods for Freezable fields return a direct reference to that field.
 *   // The returned Freezable field is ALWAYS frozen even if this object is not frozen.
 *   // This is because ValueObject.freeze() must not cause data races, yet the general
 *   // contract of Freezable does not mandate this constraint.
 *   public FreezableClass getFreezable() { return freezable; }
 *   
 *   // Setters of Freezable fields always freeze their parameter.
 *   public void setFreezable(FreezableClass f) {
 *       checkThawed();
 *       // If this field were optional, we would use optFreeze() here.
 *       this.freezable = f.freeze();
 *   }
 *   
 *   // A plain old mutable field getter must always return either clone of the
 *   // field or an umodifiable view of the field. If this method ever returned a
 *   // direct reference to its field even while this object is unfrozen,
 *   // the caller could continue to use that reference to make changes even after
 *   // this object is later frozen.
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
 *      // any ValueObject fields. Other Freezable fields are already frozen.
 *      result.pojo = result.pojo.clone();
 *      return result;
 *   }
 *   
 *   // Subclasses override only if they have ValueObject fields
 *   protected void freezeValueFields() {
 *       value.freeze();
 *       optFreeze(optionalValue);
 *       // No freezing ordinary Freezable fields in here.
 *       // Doing so may cause data races.
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
     * Freezable.
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
        c.freezeValueFields();
        return c;   
    }
    
    /**
     * Returns whether or not this object is frozen according to the contract of Freezable.
     */
    public final boolean isFrozen() {
        return bFrozen;
    }
    
    /**
     * Returns a thawed clone according to the contract of the Freezable interface.
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
            freezeValueFields();
        }
        bFrozen = true;
        return (T) this;
    }
    
    /**
     * freeze() calls this to freeze any fields that extend ValueObject if
     * this object was not previously frozen already.
     * Subclasses that have ValueObject fields override this method to freeze
     * those fields. subclasses can use optFreeze to freeze fields that
     * could be null. Subclasses should override this method to do nothing more than
     * freeze ValueObject fields. Mutating other state may create data races.
     */
    protected void freezeValueFields() {
        // Default implementation assumes there are no
        // fields that extend ValueObject
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
     * to freeze an optional, Freezable field.
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
     * Call from a mutating method to thaw a ValueObject field so that it can be
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
    protected static <U extends ValueObject<U>> U thaw(U fieldToBeMutated) {
        if (!fieldToBeMutated.isFrozen()) {
            return fieldToBeMutated;
        }
        return fieldToBeMutated.cloneAsThawed();
    }

    private volatile boolean bFrozen = false;
}
