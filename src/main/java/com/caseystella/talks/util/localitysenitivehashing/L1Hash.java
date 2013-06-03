package com.caseystella.talks.util.localitysenitivehashing;

import com.caseystella.lsh.L1LSH;
import com.caseystella.lsh.interfaces.ILSH;
import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: cstella
 * Date: 5/31/13
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class L1Hash extends AbstractLocalitySensitiveHash
{
    ILSH hash;

    public L1Hash(int max)
    {
        super(max);
        L1LSH.Creator creator = new L1LSH.Creator(1, .5f);
        try {
            hash = creator.construct(0);
        } catch (MathException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    @Override
    public Integer apply(byte[] bytes) {
        RealVector vector = new ArrayRealVector(new double[] {ByteBuffer.wrap(bytes).getDouble()});
        return ((int)hash.apply(vector) & Integer.MAX_VALUE) % max;
    }
}
