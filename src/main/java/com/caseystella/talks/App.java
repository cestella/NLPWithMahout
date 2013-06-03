package com.caseystella.talks;

import com.caseystella.talks.util.localitysenitivehashing.*;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;

import java.nio.ByteBuffer;
import java.util.*;

import static com.caseystella.talks.util.localitysenitivehashing.AbstractLocalitySensitiveHash.printDistribution;

/**
 * Hello world!
 *
 */
public class App
{


    private static final int NUM_ELEMENTS = 1500000;
    private static final double INCLUSION_PROB = 0.2;
    private static final int NUM_MINHASHFUNCTIONS = 3;
    private static final int MINHASH_DEPTH = 8;
    private static final int SEED = 0;
    //private static final int ELEMENT_SIZE = 20;

    private static final double PROBABILITY_OF_FALSE_POSITIVE = 0.01;
    private static final int SAMPLE_SIZE = (int)(0.01*NUM_ELEMENTS);

    private static class RealSampler implements Function<Object, Double>
    {
        private RealDistribution distribution;
        public RealSampler(RealDistribution distribution)
        {
            this.distribution = distribution;
        }
        @Override
        public Double apply(java.lang.Object o) {
            return distribution.sample();
        }
    }

    private static Iterable<Key> generateData(int numElements, int seed, Function<Object, Double> sampler)
    {
        Random r = new Random(seed);
        List<Key> ret = new ArrayList<Key>();
        for(int i = 0;i < numElements;++i)
        {
            byte[] sample = new byte[8];
            double value = sampler.apply(null); //new byte[ELEMENT_SIZE];
            ByteBuffer.wrap(sample).putDouble(value);
            //r.nextBytes(sample);
            ret.add(new Key(sample));
        }
        return ret;
    }





    public static void main( String[] args )
    {
        //construct reference set
        Set<Key> referenceSet = new HashSet<Key>();
        Iterable<Key> data = generateData(NUM_ELEMENTS, SEED, new RealSampler(new NormalDistribution(10, 1)));
        //BloomFilter bloomFilter = new BloomFilter(1476383, 7, Hash.MURMUR_HASH);//BloomFilterUtil.createBloomFilter(PROBABILITY_OF_FALSE_POSITIVE, (int) (NUM_ELEMENTS * INCLUSION_PROB));


        LSHBloomFilter randomFilter = null;
        {
            BloomFilter[] filters = null;
            Function<byte[], Integer> hash = new Function<byte[], Integer>()
            {

                @Override
                public Integer apply(byte[] bytes) {
                    return (Arrays.hashCode(bytes) & Integer.MAX_VALUE) % MINHASH_DEPTH;
                }
            };
            int[] distribution = new int[MINHASH_DEPTH];
            for(int i = 0;i < distribution.length;++i)
            {
               distribution[i] = 1;
            }
            //System.out.println("UNIFORM DISTRIBUTION:");
            //printDistribution(distribution);
            filters = MinHash.createBloomFilters(distribution, PROBABILITY_OF_FALSE_POSITIVE, (int) (NUM_ELEMENTS * INCLUSION_PROB));
            randomFilter = new LSHBloomFilter(filters, hash);
        }

        LSHBloomFilter lshBloomFilter = null;
        {
            BloomFilter[] filters = null;
            L2Hash hash = new L2Hash(MINHASH_DEPTH);
            int[] distribution = hash.getDistribution(SAMPLE_SIZE, Iterables.transform(data, new Function<Key, byte[]>() { public byte[] apply(Key key) { return key.getBytes();} } ));

            System.out.println("DISTRIBUTION:");
            printDistribution(distribution);
            filters = MinHash.createBloomFilters(distribution, PROBABILITY_OF_FALSE_POSITIVE, (int) (NUM_ELEMENTS * INCLUSION_PROB));
            lshBloomFilter = new LSHBloomFilter(filters, hash);
        }
        BloomFilter bloomFilter = new BloomFilter(lshBloomFilter.getTotalBits(), 7, Hash.MURMUR_HASH);
        Random r = new Random(100);
        int totalIncluded = 0;
        int t = 0;
        for(Key datum : data)
        {
           t++;
           if(r.nextDouble() < INCLUSION_PROB)
           {
               totalIncluded++;
               //include in the sets
               referenceSet.add(datum);
               bloomFilter.add(datum);
               lshBloomFilter.add(datum);
               randomFilter.add(datum);
           }
        }
        System.out.println("Total Included: " + totalIncluded + " (" + 1.0*totalIncluded/t + ")");
        //test false positive rate
        double bloomFalsePositive= 0;
        double lshHashFalsePositive = 0;
        int[] lshErrorDistribution = new int[MINHASH_DEPTH];
        double randomFalsePositive = 0;
        int[] randErrorDistribution = new int[MINHASH_DEPTH];
        int total = 0;
        for(Key datum : data)
        {
            if(!referenceSet.contains(datum))
            {
                total++;
                if(bloomFilter.membershipTest(datum)){
                    bloomFalsePositive++;
                }
                if(lshBloomFilter.membershipTest(datum))
                {
                    lshHashFalsePositive++;
                    lshErrorDistribution[lshBloomFilter.getIndex(datum)]++;
                }
                if(randomFilter.membershipTest(datum))
                {
                    randomFalsePositive++;
                    randErrorDistribution[randomFilter.getIndex(datum)]++;

                }
            }
        }
        System.out.println("========================\n");
        System.out.println("Bloom False Positive Rate: " + (bloomFalsePositive/ total) + " = "  + bloomFalsePositive + " / " + total + "; divergence of ~" + Math.abs(1.0*bloomFalsePositive/total - PROBABILITY_OF_FALSE_POSITIVE));
        System.out.println("========================\n");
        System.out.println("lshBloom False Positive Rate: " + (lshHashFalsePositive/ total) + " = " + lshHashFalsePositive+ " / " + total+ "; divergence of ~" + Math.abs(1.0*lshHashFalsePositive/total - PROBABILITY_OF_FALSE_POSITIVE));
        AbstractLocalitySensitiveHash.printDistribution(lshErrorDistribution);
        System.out.println("========================\n");
        System.out.println("RandomBloom False Positive Rate: " + (randomFalsePositive/ total) + " = " + randomFalsePositive+ " / " + total+ "; divergence of ~" + Math.abs(1.0*randomFalsePositive/total - PROBABILITY_OF_FALSE_POSITIVE));
        AbstractLocalitySensitiveHash.printDistribution(randErrorDistribution);
        System.out.println("========================\n");
        System.out.println("Expected false positive rate: " + PROBABILITY_OF_FALSE_POSITIVE);

    }
}
