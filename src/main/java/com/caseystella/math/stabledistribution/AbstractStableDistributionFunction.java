package com.caseystella.math.stabledistribution;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;

import com.caseystella.lsh.interfaces.ILSH;
import com.google.common.base.Function;

public abstract class AbstractStableDistributionFunction implements ILSH
{
	public static interface ISampler
	{
		public double apply(RandomDataImpl randomData) throws MathException;
	}
   private double[] a;
   private double b;
   private float w;
   private int dim;

   /**
    * Constructs a new instance.
 * @throws MathException 
    */
   public AbstractStableDistributionFunction(int dim, float w, RandomGenerator rand) throws MathException
   {
      reset(dim, w, rand); 
   }

   public AbstractStableDistributionFunction(int dim, float w, long seed) throws MathException
   {
      RandomGenerator generator = new JDKRandomGenerator();
      generator.setSeed(seed);
      reset(dim
           ,w
           ,generator
           );
   }

   public void reset(int dim, float w, RandomGenerator rand) throws MathException
   {
      RandomDataImpl dataSampler = new RandomDataImpl(rand);
      ISampler sampler = getSampler(dataSampler);      
      this.a = new double[dim];
      this.dim = dim;
      this.w = w;
      for(int i = 0;i < dim;++i)
      {
         a[i] = sampler.apply(dataSampler);
      }
      b = dataSampler.nextUniform(0, w);
   }


   protected abstract ISampler 
   getSampler(RandomDataImpl dataSampler);
   
   public long apply(RealVector vector)
   {
      double ret = b;
      //inner product
      for(int i = 0;i < dim;++i)
      {
         ret += vector.getEntry(i)*a[i];
      }
      return (long)Math.floor(ret/w);
   } 
}
