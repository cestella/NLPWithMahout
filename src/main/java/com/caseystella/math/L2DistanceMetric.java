package com.caseystella.math;

import org.apache.commons.math.linear.RealVector;

import com.caseystella.interfaces.IDistanceMetric;

public class L2DistanceMetric implements IDistanceMetric {

	@Override
	public double apply(RealVector v1, RealVector v2) {
		return v1.getDistance(v2);
	}
	public static IDistanceMetric INSTANCE = new L2DistanceMetric();
}
