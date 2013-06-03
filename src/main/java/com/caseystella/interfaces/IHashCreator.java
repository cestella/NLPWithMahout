package com.caseystella.interfaces;

import org.apache.commons.math.MathException;

import com.caseystella.lsh.interfaces.ILSH;


public interface IHashCreator
{
	public ILSH construct(long seed) throws MathException;
}