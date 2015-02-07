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

import java.util.concurrent.atomic.AtomicBoolean;

import com.ibm.icu.util.ICUCloneNotSupportedException;

/**
 * ValueObject is the base class of JAVA objects that mimic C++ value objects
 * (objects that override the assignment operator).
 * Because with JAVA everything is a reference except for the primitive types,
 * JAVA provides built-in value type semantics with the assignment operator only
 * for primitive types and immutable types like Strings. This base class
 * provides efficient value type semantics for mutable objects.
 * <p>
 * Assignment: <br><br>
 * Just use clone like this:
 * 
 * <pre>
 *  // lhs can be modified without affecting rhs and vice versa.
 * MyObject lhs = rhs.clone();
 * </pre>
 * <p>
 * Calling clone on a ValueObject subclass is generally faster than calling
 * clone on a typical JAVA object because only a shallow copy is necessary
 * to clone as long as all the members are primitive types, immutable types,
 * or ValueObject types (more on this later).
 * <p>
 * If you know that you aren't going to immediately modify lhs or rhs,
 * the freeze method also can be used for assignment like this:<br>
 * 
 * <pre>
 * MyObject lhs = rhs.freeze();
 * </pre>
 * <p>
 * freeze is even faster than clone because freeze marks rhs as read-only and then
 * returns rhs itself instead of a clone. However with this latter method,
 * copying is deferred until either lhs or rhs needs to change.<br>
 * <p>
 * Accessing fields and "const" methods in a ValueObject:<br>
 * <p>
 * To access fields in a ValueObject, use the supplied getXXX() methods.
 * ValueObject getXXX() methods are like C++ const getXXX() methods that return
 * either const pointers or primitive types. Therefore, the caller must not
 * modify any object directly using the reference returned from a getXXX() method.
 * With no construct like "const" in JAVA, it is up to the caller to ensure
 * that they don't violate this rule (We aren't enforcing this
 * rule at runtime for performance reasons but that could change).
 * When a getXXX() method returns a mutable object, and the caller
 * needs to hold onto it, it should clone it
 * as the original returned object could change. Likewise, if the caller
 * needs its own copy of a returned object that it can change it should clone it.
 * However when a getXXX() method returns a ValueObject and the caller needs to hold
 * onto it without changing it, the caller can just call freeze() on it to prevent it
 * from changing.<br>
 * <p>
 * Modifying a ValueObject:<br>
 * <p>
 * To modify a ValueObject, clone it first to ensure you have a thawed clone.
 * If you know that your object is already thawed (for instance you just
 * constructed it or it is a precondition), then calling clone isn't necessary.
 * Attempting to call any method on a frozen object that mutates
 * it including setter methods will throw UnsupportedOperationException.
 * Here is an example:<br>
 * 
 * <pre>
 * objectToBeModified = originalObject.clone();
 * objectToBeModified.setFoo(foo);
 * objectToBeModified.setBar(bar);
 * objectToBeModified.doSomeMutation();
 * </pre>
 * 
 * <p>
 * Modifying an embedded ValueObject within a ValueObject:<br>
 * <p>
 * Use the corresponding, getMutableXXX() method which is like a C++ non-cost
 * get method that returns a non-const pointer. Callers can safely modify
 * the embedded ValueObject through the reference returned by getMutableXXX().
 * However, callers should hold onto such references only for a short time
 * such as during one short code block or one short function. Holding onto them
 * longer could enable a caller to make changes to the embedded ValueObject
 * even after the enclosing object has been frozen.<br>
 * <p>
 * Writing your own ValueObject class:<br>
 * <p>
 * To write your own ValueObject class consider the fields/attributes of the
 * class. Fields fall into the following categories.<br>
 * <ol>
 *   <li>Primitive types and immutable types</li>
 *   <li>Mutable ValueObject types</li>
 *   <li>Other mutable types</li>
 * </ol><br>
 * <p>
 * The code below demonstrates how.<br>
 * <p>
 * <pre>
 * public class MyClass extends ValueObject<MyClass> {
 * 
 *   // Default instance
 *   public static final MyClass DEFAULT = new MyClass().freeze();
 *   
 *   private int primitive = 0;
 *   private ImmutablePoint point = ImmutablePoint.valueOf(2, 3);
 *   private AValueObjectClass value = AValueObjectClass.DEFAULT;
 *   private AValueObjectClass optionalValue = null;
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
 *   public void setPoint(Point p) {
 *       checkThawed();
 *       point = p;
 *   }
 *   
 *   // Caller must take care not to modify returned
 *   // value directly.
 *   public AValueObjectClass getValue() { return value; }
 *   
 *   // Remember getMutableXXX methods can only be used on
 *   // thawed objects.
 *   public AValueObjectClass getMutableValue() {
 *       // Throws exception if this object is frozen. Otherwise
 *       // thaws the 'value' field so caller can change it.
 *       value = safeThaw(value);
 *       return value;
 *   }
 *   public void setValue(AValueObjectClass v) {
 *       // Throws exception if this object is frozen.
 *       // or v is null. Otherwise
 *       // freezes v before setting it to value.
 *       this.value = safeSet(v);
 *   }
 *   
 *   // Caller must take care not to modify returned
 *   // value directly.
 *   public AValueObjectClass getOptionalValue() { return optionalValue; }
 *   
 *   public void setOptionalValue(AValueObjectClass v) {
 *       // like safeSet, but handles v == null.
 *       this.value = optSafeSet(v);
 *   }
 *   
 *   // Caller must take care not to modify returned value directly.
 *   public MutableClass getPojo() { return pojo; }
 *   public void setPojo(MutableClass o) {
 *       checkThawed();
 *       pojo = o == null ? null : o.clone();
 *   }
 *   
 *   // If MyClass contained only primitive fields and immutable fields
 *   // then it would not be necessary to override clone.
 *   public MyClass clone() {
 *       MyClass result = super.clone();
 *       
 *       // For each required ValueObject field, call freeze
 *       value.freeze();
 *       
 *       // For each optional ValueObject filed, call optFreeze
 *       optFreeze(optionalValue);
 *       
 *       // For each mutable field, clone
 *       if (pojo != null)
 *           pojo = (MutableClass) pojo.clone();
 *       }
 *   }
 * }
 * </pre>
 * 
 * @param <T> The subclass of ValueObject.
 */
public abstract class ValueObject<T extends ValueObject<T>> implements Cloneable {

    /** Initially thawed */
    public ValueObject() {}

    /** Returns a thawed clone */
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
        c.frozen = new AtomicBoolean();
        return c;
    }
    
    /**
     * Freezes this object and returns this.
     */
    @SuppressWarnings("unchecked")
    public final T freeze() { 
        frozen.set(true);
        return (T) this;
    }
    
    /**
     * Call first thing in a mutating method such as a setXXX method to
     * ensure this object is thawed.
     */
    protected final void checkThawed() {
        if (!isThawed()) {
            throw new UnsupportedOperationException("Cannot modify a frozen object");
        }
    }
    
    /**
     * Call within the setXXX method for an optional ValueObject attribute. See coding
     * example in documentation.
     * @param newValue the new value. May be null.
     * @return Returns the new value or null.
     */
    protected final <U extends ValueObject<U>> U optSafeSet(U newValue) {
        checkThawed();
        if (newValue == null) {
            return null;
        }
        return newValue.freeze();
    }
    
    /**
     * Call within the setXXX method for a required ValueObject attribute. See coding
     * example in documentation.
     * @param newValue the new value.
     * @return Returns the new value.
     */
    protected final <U extends ValueObject<U>> U safeSet(U newValue) {
        if (newValue == null) {
            throw new NullPointerException("null not allowed in field.");
        }
        checkThawed();
        return newValue.freeze();
    }
    
    /**
     * Call within a getMutableXXX method to thaw the corresponding
     * attribute. See coding example in documentation.
     * @param value the value to thaw
     * @return the thawed value.
     */
    protected final <U extends ValueObject<U>> U safeThaw(U value) {
        checkThawed();
        return value.thaw();
    }
    
    /**
     * Call within the clone method to freeze an optional embedded ValueObject.
     * See coding example in documentation.
     * @param value The value to freeze. May be null.
     */
    protected final void optFreeze(ValueObject<?> value) {
        if (value != null) {
            value.freeze();
        }
    }
    
    private boolean isThawed() {
        return !frozen.get();
    }
    
    @SuppressWarnings("unchecked")
    private T thaw() {
        if (isThawed()) {
            return (T) this;
        }
        return clone();
    }

    private AtomicBoolean frozen = new AtomicBoolean();
}
