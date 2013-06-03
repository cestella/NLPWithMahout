package com.caseystella.interfaces;

import com.caseystella.KNN.Payload;

public interface IBackingStore
   {
      public void persist(long key, Payload payload);
      public Iterable<Payload> getBucket(long key);
   }