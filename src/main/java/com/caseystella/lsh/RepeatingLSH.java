package com.caseystella.lsh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.RealVector;

import com.caseystella.interfaces.IDistanceMetric;
import com.caseystella.interfaces.IHashCreator;
import com.caseystella.lsh.interfaces.ILSH;

public class RepeatingLSH implements ILSH 
{
	public static class Creator implements IHashCreator
	{
		int numRepetitions;
		IHashCreator underlyingCreator;
		
		
		public Creator(int numRepetitions, IHashCreator underlyingCreator)
		{
			this.numRepetitions = numRepetitions;
			this.underlyingCreator = underlyingCreator;
			
		}
		@Override
		public ILSH construct(long seed) throws MathException {
			return new RepeatingLSH(numRepetitions, underlyingCreator, seed);
		}
		
	}
	private List<ILSH> lshList;
	private int[] coefficients;
	public RepeatingLSH(int numRepetitions, IHashCreator pHashCreator, long seed) throws MathException
	{
		lshList = new ArrayList<ILSH>();
		coefficients = new int[numRepetitions];
		Random r = new Random(seed);
		for(int i = 0;i < numRepetitions;++i)
		{
			lshList.add(pHashCreator.construct(r.nextLong()));
			coefficients[i] = Math.abs(r.nextInt());
		}
		
		
	}
	
	@Override
	public IDistanceMetric getMetric() {
		return lshList.get(0).getMetric();
	}

	@Override
	public long apply(RealVector vector) {
		long ret = 0;
		for(int i = 0;i < lshList.size();++i)
		{
			ret += coefficients[i] * lshList.get(i).apply(vector);
		}
		return ret;
	}

}
