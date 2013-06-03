package com.caseystella.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.caseystella.KNN;
import com.caseystella.KNN.Payload;
import com.caseystella.interfaces.IBackingStore;
import com.google.common.base.Supplier;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

public class InMemoryBackingStore implements IBackingStore
{
	ListMultimap<Long, Payload> multimap;
	public InMemoryBackingStore()
	{
		Supplier<List<Payload>> supplier = new Supplier<List<Payload>>()
				{
			@Override
			public List<Payload> get() {
				return new ArrayList<Payload>();
			}
				};
		Map<Long, Collection<Payload>> backingMap = new HashMap<Long, Collection<Payload>>();
		multimap = Multimaps.<Long, Payload> newListMultimap( backingMap
	
											, supplier
											);
	}
	@Override
	public Iterable<Payload> getBucket(long key) {
		return multimap.get(key);
	}
	@Override
	public void persist(long key, Payload payload) {
		multimap.put(key, payload);
		
	}
}