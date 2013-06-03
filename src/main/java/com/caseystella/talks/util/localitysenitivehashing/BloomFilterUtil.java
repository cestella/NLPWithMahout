package com.caseystella.talks.util.localitysenitivehashing;

import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.hash.Hash;

/**
 * Created with IntelliJ IDEA.
 * User: cstella
 * Date: 5/31/13
 * Time: 6:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class BloomFilterUtil {
    public static BloomFilter createBloomFilter(int m, int targetCount)
    {
        double ln_2 = Math.log(2.0);
        int k = (int)Math.round((ln_2 * m) / targetCount);
        System.out.println("Number of bits: " + m + ", num hash functions: " + k + ", n = " + targetCount + ", p = " + Math.pow(2.0, -(m*ln_2)/targetCount));
        return new BloomFilter(m,k, Hash.MURMUR_HASH);
    }

    public static int getNumBits(double targetRate, int targetCount)
    {
        double ln_2 = Math.log(2.0);
//        int m = -(int)Math.ceil((targetCount * Math.log(targetRate)) / (ln_2*ln_2));
        int m = (int)Math.ceil(targetCount*Math.log(1/targetRate)/(ln_2*ln_2));
        return m;
    }
    public static BloomFilter createBloomFilter(double targetRate, int targetCount)
    {
        double ln_2 = Math.log(2.0);
        int m = getNumBits(targetRate, targetCount);
        int k = (int)Math.round((ln_2 * m) / targetCount);
        System.out.println("Number of bits: " + m + ", num hash functions: " + k);
        return new BloomFilter(m,k, Hash.MURMUR_HASH);
    }
}
