/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.persistit;

import java.util.Random;

import com.persistit.Exchange.Sequence;
import com.persistit.policy.SplitPolicy;
import com.persistit.unit.PersistitUnitTestCase;

public class SplitPolicyTest extends PersistitUnitTestCase {

    public void testLeftBias() {
        Buffer nullBuffer = null;
        int mockLeftSize = 20;
        int capacity = 0;
        SplitPolicy leftBias = SplitPolicy.LEFT_BIAS;
        assertEquals("LEFT", leftBias.toString());
        int measure = leftBias.splitFit(nullBuffer, 0, 0, false, mockLeftSize, 0, 0, 0, capacity, 0, Sequence.NONE);
        /* splitFit should return 0 since leftSize is larger than capcity */
        assertEquals(0, measure);

        capacity = 21;
        measure = leftBias.splitFit(nullBuffer, 0, 0, false, mockLeftSize, 0, 0, 0, capacity, 0, Sequence.NONE);
        /* splitFit just returns the given leftSize for LEFT_BIAS policy */
        assertEquals(mockLeftSize, measure);
    }

    public void testRightBias() {
        Buffer nullBuffer = null;
        int mockRightSize = 20;
        int capacity = 0;
        SplitPolicy rightBias = SplitPolicy.RIGHT_BIAS;
        assertEquals("RIGHT", rightBias.toString());
        int measure = rightBias.splitFit(nullBuffer, 0, 0, false, 0, mockRightSize, 0, 0, capacity, 0, Sequence.NONE);
        /* splitFit should return 0 since rightSize is larger than capacity */
        assertEquals(0, measure);

        capacity = 21;
        measure = rightBias.splitFit(nullBuffer, 0, 0, false, 0, mockRightSize, 0, 0, capacity, 0, Sequence.NONE);
        /* splitFit just returns the given rightSize for RIGHT_BIAS policy */
        assertEquals(mockRightSize, measure);
    }

    public void testEvenBias() {
        Buffer nullBuffer = null;
        int mockRightSize = 20;
        int mockLeftSize = 20;
        int capacity = 0;
        SplitPolicy evenBias = SplitPolicy.EVEN_BIAS;
        assertEquals("EVEN", evenBias.toString());
        int measure = evenBias.splitFit(nullBuffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0,
                Sequence.NONE);
        /*
         * splitFit should return 0 since rightSize & leftSize are larger than
         * capacity
         */
        assertEquals(0, measure);

        capacity = 21;
        measure = evenBias.splitFit(nullBuffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0,
                Sequence.NONE);
        /*
         * splitFit returns (capacity - abs(rightSize - leftSize)) for EVEN_BIAS
         * policy
         */
        assertEquals(capacity, measure);

        capacity = 21;
        mockLeftSize = 5;
        mockRightSize = 15;
        measure = evenBias.splitFit(nullBuffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0,
                Sequence.NONE);
        /*
         * splitFit returns (capacity - abs(rightSize - leftSize)) for EVEN_BIAS
         * policy
         */
        assertEquals(11, measure);
    }

    public void testNiceBias() {
        Buffer nullBuffer = null;
        int mockRightSize = 20;
        int mockLeftSize = 20;
        int capacity = 0;
        SplitPolicy niceBias = SplitPolicy.NICE_BIAS;
        assertEquals("NICE", niceBias.toString());
        int measure = niceBias.splitFit(nullBuffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0,
                Sequence.NONE);
        /*
         * splitFit should return 0 since rightSize & leftSize are larger than
         * capacity
         */
        assertEquals(0, measure);

        capacity = 21;
        measure = niceBias.splitFit(nullBuffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0,
                Sequence.NONE);
        /*
         * splitFit returns ((capacity * 2) - abs((2 * rightSize) - leftSize))
         * for EVEN_BIAS policy
         */
        assertEquals(22, measure);

        capacity = 21;
        mockLeftSize = 5;
        mockRightSize = 15;
        measure = niceBias.splitFit(nullBuffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0,
                Sequence.NONE);
        /*
         * splitFit returns ((capacity * 2) - abs((2 * rightSize) - leftSize))
         * for EVEN_BIAS policy
         */
        assertEquals(17, measure);
    }

    public void testPackBias() throws Exception {
        final Exchange ex = _persistit.getExchange("persistit", "SplitPolicyTest", true);
        ex.getValue().put("aaabbbcccdddeee");
        ex.to(1);
        long page = ex.fetchBufferCopy(0).getPageAddress();
        final Buffer buffer = ex.getBufferPool().get(ex.getVolume(), page, false, true);
        buffer.releaseTouched();
        for (int i = 0; buffer.getAvailableSize() > 100; i++) {
            ex.to(i).store();
        }

        int mockRightSize = 20;
        int mockLeftSize = 20;
        int capacity = 0;
        SplitPolicy packBias = SplitPolicy.PACK_BIAS;
        assertEquals("PACK", packBias.toString());
        //
        // For non-sequential inserts, works the same as NICE_BIAS.
        //
        int measure = packBias.splitFit(buffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0,
                Sequence.NONE);
        /*
         * splitFit should return 0 since rightSize & leftSize are larger than
         * capacity
         */
        assertEquals(0, measure);

        capacity = 21;
        measure = packBias.splitFit(buffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0, Sequence.NONE);
        /*
         * splitFit returns ((capacity * 2) - abs((2 * rightSize) - leftSize))
         * for EVEN_BIAS policy
         */
        assertEquals(22, measure);

        capacity = 21;
        mockLeftSize = 5;
        mockRightSize = 15;
        measure = packBias.splitFit(buffer, 0, 0, false, mockLeftSize, mockRightSize, 0, 0, capacity, 0, Sequence.NONE);
        /*
         * splitFit returns ((capacity * 2) - abs((2 * rightSize) - leftSize))
         * for EVEN_BIAS policy
         */
        assertEquals(17, measure);

        //
        // Sequential insert cases
        //
        for (int p = buffer.getKeyBlockStart(); p < buffer.getKeyBlockEnd(); p += Buffer.KEYBLOCK_LENGTH) {
            int splitBest = split(packBias, buffer, p, Sequence.FORWARD);
            if (p > buffer.getKeyBlockStart() + 256 && p < buffer.getKeyBlockEnd() - 256) {
                assertEquals(splitBest, p);
            }
        }
        for (int p = buffer.getKeyBlockStart(); p < buffer.getKeyBlockEnd(); p += Buffer.KEYBLOCK_LENGTH) {
            int splitBest = split(packBias, buffer, p, Sequence.REVERSE);
            if (p > buffer.getKeyBlockStart() + 260 && p < buffer.getKeyBlockEnd() - 260) {
                assertEquals(splitBest, p + Buffer.KEYBLOCK_LENGTH);
            }
        }
    }

    private int split(final SplitPolicy policy, final Buffer buffer, final int foundAt, final Sequence sequence) {
        int best = -1;
        int bestMeasure = -1;
        int leftSize = 0;
        int rightSize = buffer.getBufferSize() + 10;
        int perKeySize = rightSize / buffer.getKeyCount();
        for (int p = buffer.getKeyBlockStart(); p < buffer.getKeyBlockEnd(); p += Buffer.KEYBLOCK_LENGTH) {

            int measure = policy.splitFit(buffer, p, foundAt, false, leftSize, rightSize, buffer.getBufferSize() - 100,
                    leftSize + rightSize, buffer.getBufferSize(), best, sequence);
            if (measure > bestMeasure) {
                best = p;
                bestMeasure = measure;
            }
            leftSize += perKeySize;
            rightSize -= perKeySize;
        }
        return best;
    }

    public void testPackBiasPacking() throws Exception {
        final Exchange ex = _persistit.getExchange("persistit", "SplitPolicyTest", true);
        final Random random = new Random(1);
        ex.setSplitPolicy(SplitPolicy.PACK_BIAS);
        ex.getValue().put("aaabbbcccdddeee");

        ex.removeAll();
        for (int i = 0; ex.getVolume().getStorage().getNextAvailablePage() < 20; i++) {
            ex.to(i).store();
        }
        final float ratioFowardSequential = inuseRatio(ex);
        assertTrue(ratioFowardSequential > .85);

        ex.removeAll();
        for (int i = 1000000; ex.getVolume().getStorage().getNextAvailablePage() < 21; i--) {
            ex.to(i).store();
        }
        final float ratioReverseSequential = inuseRatio(ex);
        assertTrue(ratioReverseSequential > .85);

        ex.removeAll();
        for (; ex.getVolume().getStorage().getNextAvailablePage() < 22;) {
            ex.to(random.nextInt()).store();
        }
        final float ratioRandom = inuseRatio(ex);
        assertTrue(ratioRandom > .5 && ratioRandom < .75);
    }

    private float inuseRatio(final Exchange ex) throws Exception {
        float total = 0;
        float used = 0;
        //
        // forward sequential
        //
        for (long page = 2; page < 20; page++) {
            final Buffer buffer = ex.getBufferPool().get(ex.getVolume(), page, false, true);
            if (buffer.isDataPage()) {
                int available = buffer.getAvailableSize();
                System.out.println(buffer + " avail=" + available);
                total = total + buffer.getBufferSize();
                used = used + buffer.getBufferSize() - buffer.getAvailableSize();
            }
            buffer.releaseTouched();
        }
        return used / total;
    }

    @Override
    public void runAllTests() throws Exception {
        // TODO Auto-generated method stub

    }
}
