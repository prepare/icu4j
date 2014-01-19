/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* SharedObject.java, ported from sharedobject.h/.cpp
*
* @since 2013dec19
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for shared, reference-counted, auto-deleted objects.
 * Java subclasses are mutable and must implement clone().
 *
 * <p>In C++, the SharedObject base class is used for both memory and ownership management.
 * In Java, memory management (deletion after last reference is gone)
 * is up to the garbage collector,
 * but the reference counter is still used to see whether the referent is the sole owner.
 *
 * <p>Usage:
 * <pre>
 * class S extends SharedObject {
 *     public clone() { ... }
 * }
 *
 * class U {
 *     // For read-only access, use s directly.
 *     // For writable access, use S ownedS = getOwnedS();
 *     private S s;
 *     // Returns a writable version of s.
 *     // If there is exactly one owner, then s itself is returned.
 *     // If there are multiple owners, then s is replaced with a clone,
 *     // and that is returned.
 *     private S getOwnedS() {
 *         if(s.getRefCount() > 1) {
 *             S ownedS = s.clone();
 *             s.removeRef();
 *             s = ownedS;
 *             ownedS.addRef();
 *         }
 *         return s;
 *     }
 *     public U clone() {
 *         ...
 *         s.addRef();
 *         ...
 *     }
 *     protected void finalize() {
 *         ...
 *         if(s != null) {
 *             s.removeRef();
 *             s = null;
 *         }
 *         ...
 *     }
 * }
 * </pre>
 *
 * Either use only Java memory management, or use addRef()/removeRef().
 * Sharing requires reference-counting.
 *
 * TODO: Consider making this more widely available inside ICU,
 * or else adopting a different model.
 */
class SharedObject implements Cloneable {
    /** Initializes refCount to 0. */
    public SharedObject() {}

    /** Initializes refCount to 0. */
    @Override
    public SharedObject clone() {
        SharedObject c;
        try {
            c = (SharedObject)super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen.
            throw new RuntimeException(e);
        }
        c.refCount = new AtomicInteger();
        return c;
    }

    /**
     * Increments the number of references to this object. Thread-safe.
     */
    public final void addRef() { refCount.incrementAndGet(); }
    /**
     * Decrements the number of references to this object,
     * and auto-deletes "this" if the number becomes 0. Thread-safe.
     */
    public final void removeRef() {
        // Deletion in Java is up to the garbage collector.
        refCount.decrementAndGet();
    }

    /**
     * Returns the reference counter. Uses a memory barrier.
     */
    public final int getRefCount() { return refCount.get(); }

    public final void deleteIfZeroRefCount() {
        // Deletion in Java is up to the garbage collector.
    }

    private AtomicInteger refCount = new AtomicInteger();
}
