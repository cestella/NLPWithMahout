package com.caseystella.talks.util.localitysenitivehashing;

import com.google.common.base.Function;
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.bloom.Key;

/**
 * Created with IntelliJ IDEA.
 * User: cstella
 * Date: 5/31/13
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class LSHBloomFilter {


        private BloomFilter[] filters;
        private Function<byte[], Integer> hash;
        public LSHBloomFilter(BloomFilter[] filters, Function<byte[], Integer> hash)
        {
            this.filters = filters;
            this.hash = hash;
        }

        public int getTotalBits()
        {
            int total = 0;
            for(BloomFilter filter : filters)
            {
                total += filter.getVectorSize();
            }
            return total;
        }




        public int getIndex(Key key)
        {
            return hash.apply(key.getBytes());
        }

        public boolean membershipTest(Key key)
        {
            int filterIndex = getIndex(key);
            return filters[filterIndex].membershipTest(key);
        }

        public void add(Key key)
        {
            int filterIndex = getIndex(key);
            filters[filterIndex].add(key);
        }
}
