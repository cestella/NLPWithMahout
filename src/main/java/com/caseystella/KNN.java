package com.caseystella;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.RealVector;

import com.caseystella.interfaces.IBackingStore;
import com.caseystella.interfaces.IDistanceMetric;
import com.caseystella.interfaces.IHashCreator;
import com.caseystella.lsh.interfaces.ILSH;

public class KNN
{
   public static class Payload
   {
      private RealVector vector;
      private byte[] payload;

      public Payload(RealVector vector, byte[] payload)
      {
         this.vector = vector;
         this.payload = payload;
      }

      /**
       * Gets the vector for this instance.
       *
       * @return The vector.
       */
      public RealVector getVector()
      {
         return vector;
      }

      /**
       * Gets the payload for this instance.
       *
       * @return The payload.
       */
      public byte[] getPayload()
      {
         return payload;
      }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(payload);
		result = prime * result + ((vector == null) ? 0 : vector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Payload other = (Payload) obj;
		if (!Arrays.equals(payload, other.payload))
			return false;
		if (vector == null) {
			if (other.vector != null)
				return false;
		} else if (!vector.equals(other.vector))
			return false;
		return true;
	}


   }
   
   public static class Result
   {
	   private Iterable<Payload> payloads;
	   private int totalItemsReturned;
	   Result(Iterable<Payload> payloads, int totalItemsReturned)
	   {
		   this.payloads = payloads;
		   this.totalItemsReturned = totalItemsReturned;
	   }
	   
	   public Iterable<Payload> getPayloads() {
		return payloads;
	   }
	   
	   public int getTotalItemsReturned() {
		return totalItemsReturned;
	   }
   }
   private Iterable<ILSH> hashes;
   private IBackingStore backingStore;
   private IDistanceMetric underlyingMetric;
   public KNN( int numHashes
             , int hashDimension
             , long seed
             , IHashCreator creator
             , IBackingStore backingStore
             ) throws MathException
   {
      this.backingStore = backingStore;
      List<ILSH > hashList = new ArrayList<ILSH> (numHashes);
      for(int i = 0;i < numHashes;++i)
      {
    	  
         hashList.add(creator.construct(seed + i));
         if(i == 0)
         {
        	 underlyingMetric = hashList.get(0).getMetric();
         }
      }
      hashes = hashList;
   }
   
   public IDistanceMetric getUnderlyingMetric() {
	return underlyingMetric;
   }
   
   public Result query(RealVector q,  double limit)
   {
      Set<Payload> results = new HashSet<Payload>(); 
      int totalItemsReturned = 0;
      for(ILSH hash : hashes)
      {
         //find the thing in the bucket
         Iterable<Payload> values = backingStore.getBucket(hash.apply(q));
         for(Payload value : values)
         {
        	 totalItemsReturned++;
            if(hash.getMetric().apply(q, value.getVector()) < limit)
            {
               results.add(value);
            }
         } 
      }
      return new Result(results, totalItemsReturned);
   }
   
   public void insert(Payload payload)
   {
      for(ILSH hash : hashes)
      {
         long key = hash.apply(payload.getVector());
         backingStore.persist(key, payload);
      }
   }
}
