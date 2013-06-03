package com.caseystella.lsh;

import org.apache.commons.math.MathException;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;

import com.caseystella.interfaces.IDistanceMetric;
import com.caseystella.interfaces.IHashCreator;
import com.caseystella.lsh.interfaces.ILSH;
import com.caseystella.math.L2DistanceMetric;
import com.caseystella.math.stabledistribution.AbstractStableDistributionFunction;
import com.google.common.base.Function;

public class L2LSH extends AbstractStableDistributionFunction {

	protected static class L2Sampler implements ISampler
	   {

	      /**
	       * {@inheritDoc}
	       * @see Function#apply(RandomDataImpl)
	       */
	      public double apply(RandomDataImpl randomData)
	      {
	            return randomData.nextGaussian(0,1); 
	      }
	         
	   }
	public static class Creator implements IHashCreator
	{
		int dim;
		float w;
		
		public Creator(int dim, float w)
		{
			this.dim = dim;
			this.w = w;
			
		}
		@Override
		public ILSH construct(long seed) throws MathException {
			return new L2LSH(dim, w, seed);
		}
		
	}
	private static ISampler sampler = new L2Sampler();
	public static IDistanceMetric metric = new L2DistanceMetric();
	/**
	 * Constructs a new instance.
	 * @throws MathException 
	 */
	public L2LSH(int dim, float w, RandomGenerator rand) throws MathException {
		super(dim, w, rand);
	}

	public L2LSH(int dim, float w, long seed) throws MathException {
		super(dim, w, seed);
	}
	

	@Override
	public IDistanceMetric getMetric() {
		return metric;
	}

	@Override
	protected ISampler getSampler(
			RandomDataImpl dataSampler) {
		return sampler;
	}


}
