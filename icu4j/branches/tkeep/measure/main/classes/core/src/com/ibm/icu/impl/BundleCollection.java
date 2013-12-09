/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a collection of Bundles.
 */
public class BundleCollection {
   private final Map<Integer, List<Bundle>> bundleMap = new HashMap<Integer, List<Bundle>>();
   
   /**
    * Return references to all the bundles.
    */
   public List<Bundle> getAll() {
       List<Bundle> result = new ArrayList<Bundle>();
       for (List<Bundle> bundleList : bundleMap.values()) {
           result.addAll(bundleList);
       }
       return result;
   }
   
   /**
    * Returns reference to a bundle with given Id.
    */
   public Bundle getById(int id) {
       List<Bundle> result = getBundlesById(id);
       return result.size() == 0 ? null : result.get(0);
   }
   
   /**
    * Return references to all bundles with given Id.
    */
   public List<Bundle> getBundlesById(int id) {
       List<Bundle> result = bundleMap.get(id);
       return result != null ? Collections.unmodifiableList(result) : Collections.<Bundle>emptyList();  
   }
   
   /**
    * Clear all bundles from this object.
    */
   public void clear() {
       bundleMap.clear();
   }
   
   /**
    * Add a bundle to this object.
    */
   public void add(Bundle b) {
       Integer key = b.getId();
       List<Bundle> bundleList = bundleMap.get(key);
       if (bundleList == null) {
           bundleList = new ArrayList<Bundle>();
           bundleMap.put(key, bundleList);
       }
       bundleList.add(b);
   }
   
   /**
    * Add bundle b while removing other bundles with same Id.
    */
   public void put(Bundle b) {
       Integer key = b.getId();
       ArrayList<Bundle> bundleList = new ArrayList<Bundle>();
       bundleList.add(b);
       bundleMap.put(key, bundleList); 
   }
   
   /**
    * Removes all bundles with a particular key.
    */
   public void remove(int id) {
       bundleMap.remove(id);
   }
   
   /**
    * Replaces contents of this object with what is read from stream.
    */
   public void read(ObjectInput in) throws IOException {
       clear();
       int tagId = in.readByte() & 0xFF;
       while (tagId != 0) {
           Bundle bundle = create(tagId);
           
           // Read the size of the data in the bundle.
           int size = in.readShort() & 0xFFFF;
           bundle.read(in, size);
           
           add(bundle);
           
           // Read tagId of next bundle
           tagId = in.readByte() & 0xFF;
       }
   }
   
   /**
    * Writes this object to the stream.
    */
   public void write(ObjectOutput out) throws IOException {
       for (List<Bundle> bundleList : bundleMap.values()) {
           for (Bundle bundle : bundleList) {
               out.writeByte(bundle.getId());
               byte[] b = bundle.write();
               out.writeShort(b.length);
               out.write(b);
           }
       }
       out.writeByte(0);
   }
   
   protected Bundle create(int id) {
       return Bundle.getRawBundle(id);
   }
}
