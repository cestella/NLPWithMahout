package com.caseystella.interfaces;

import org.apache.commons.math.linear.RealVector;

public interface IDistanceMetric
   {
	   public double apply(RealVector v1, RealVector v2);
   }