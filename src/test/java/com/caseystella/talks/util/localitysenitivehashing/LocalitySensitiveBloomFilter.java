package com.caseystella.talks.util.localitysenitivehashing;

import com.google.common.base.Function;
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.bloom.Key;

/**
* Created with IntelliJ IDEA.
* User: cstella
* Date: 5/31/13
* Time: 6:33 PM
* To change this template use File | Settings | File Templates.
*/
public class LocalitySensitiveBloomFilter
{
    private BloomFilter[] filters;
    private Function<byte[], Integer> hash;
    public LocalitySensitiveBloomFilter(BloomFilter[] filters, Function<byte[], Integer> hash)
    {
       this.filters = filters;
        this.hash = hash;
    }

    public boolean membershipTest(Key key)
    {
        int filterIndex = hash.apply(key.getBytes());
        return filters[filterIndex].membershipTest(key);
    }

    public void add(Key key)
    {
        int filterIndex = hash.apply(key.getBytes());
        filters[filterIndex].add(key);
    }
}
