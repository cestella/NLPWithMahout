package com.caseystella.talks.util.localitysenitivehashing;

import org.apache.mahout.clustering.minhash.HashFactory;
import org.apache.mahout.clustering.minhash.HashFunction;

public class MinHash extends AbstractLocalitySensitiveHash {
        private int numHashFunctions;
        private HashFunction[] hashFunctions;

        public MinHash(HashFactory.HashType type, int numHashFunctions, int max) {
            super(max);
            this.numHashFunctions = numHashFunctions;
            this.hashFunctions = HashFactory.createHashFunctions(type, numHashFunctions);
        }

        public int getNumHashFunctions() {
            return numHashFunctions;
        }


        @Override
        public Integer apply(byte[] bytes) {
            int[] minHashes = new int[numHashFunctions];
            for (int i = 0; i < minHashes.length; ++i) {
                minHashes[i] = Integer.MAX_VALUE;
            }
            for (int j = 0; j < hashFunctions.length; ++j) {
                for (int i = 0; i < bytes.length; ++i) {
                    byte val = bytes[i];
                    int minHash = hashFunctions[j].hash(new byte[]{val});

                    //if our new hash value is less than the old one, replace the old one
                    if (minHashes[j] > minHash) {
                        minHashes[j] = minHash;
                    }
                }
            }
            return (minHashes.hashCode() & Integer.MAX_VALUE) % max;
        }
}