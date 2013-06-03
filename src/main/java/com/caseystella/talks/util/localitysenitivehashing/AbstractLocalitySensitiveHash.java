package com.caseystella.talks.util.localitysenitivehashing;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.hash.Hash;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: cstella
 * Date: 5/31/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractLocalitySensitiveHash implements Function<byte[], Integer>
{
    protected int max;
    public AbstractLocalitySensitiveHash(int max)
    {
       this.max = max;
    }
    public static void printDistribution(int[] distribution)
    {
             int total = 0;
            for(int i = 0;i < distribution.length;++i)
            {
               total += distribution[i];
            }
            for(int i = 0;i < distribution.length;++i)
            {
                System.out.print(i + " - ");
                int numStars = (int)(100.0*distribution[i] / total);
                for(int j = 0;j < numStars;++j)
                {
                    System.out.print("*");
                }
                System.out.println(" = " + distribution[i] + " ~ " + (100.0*distribution[i])/total);
            }
            System.out.println("============================");
    }
     public int[] getDistribution(int sampleSize, Iterable<byte[]> data) {
            int[] distribution = new int[max];
            for (byte[] key : Iterables.limit(data, sampleSize)) {
                distribution[apply(key)]++;
            }
            return distribution;
        }

    /**
     * falsePositiveProb = E[X] = p(X_0)*|X_0| + ... + p(X_k)*|X_k|
     * @param distribution
     * @param falsePositiveProb
     * @param expectedNumEntries
     * @return
     */
        public static BloomFilter[] createBloomFilters(int[] distribution
                , double falsePositiveProb
                , int expectedNumEntries
        ) {
            BloomFilter[] filters = new BloomFilter[distribution.length];
            int totalCount = 0;
            int avg = 0;
            int[] medDist = new int[distribution.length];
            int k = 0;
            for (int cnt : distribution) {
                totalCount += cnt;
                medDist[k++] = cnt;
            }
            Arrays.sort(medDist);
            int median = medDist[medDist.length/2];
            avg = totalCount / distribution.length;
            double totalErrorRate = 0;
            int totalNumBits = 0;
            int totalHashFunctions;
            int idealNumBits = BloomFilterUtil.getNumBits(falsePositiveProb, expectedNumEntries );
            double targetPercentage = avg / totalCount;//1/distribution.length;
            for (int i = 0; i < filters.length; ++i) {
                double percentage = (1.0 * distribution[i]) / totalCount;
                int targetCount = (int) (expectedNumEntries * percentage);
                System.out.print("For bucket " + i + ": " );
                filters[i] = BloomFilterUtil.createBloomFilter((int)( percentage*idealNumBits), targetCount);
/*                double targetRate = (1.0 - (percentage - targetPercentage ) ) *falsePositiveProb;//percentage * falsePositiveProb;
                filters[i] = BloomFilterUtil.createBloomFilter(targetRate, targetCount);*/
                totalNumBits += filters[i].getVectorSize();
            }
            System.out.println("Total number of bits: " + totalNumBits);
            return filters;
        }
}
