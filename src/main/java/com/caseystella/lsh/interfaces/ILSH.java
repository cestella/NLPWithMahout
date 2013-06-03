package com.caseystella.lsh.interfaces;

import org.apache.commons.math.linear.RealVector;

import com.caseystella.interfaces.IDistanceMetric;

public interface ILSH 
{
	public IDistanceMetric getMetric();
	public long apply(RealVector vector);
}
