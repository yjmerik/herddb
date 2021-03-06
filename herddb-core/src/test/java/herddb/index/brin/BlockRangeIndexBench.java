/*
 Licensed to Diennea S.r.l. under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. Diennea S.r.l. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package herddb.index.brin;

import herddb.core.RandomPageReplacementPolicy;
import herddb.utils.Sized;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author enrico.olivelli
 */
public class BlockRangeIndexBench {

    @Test
    public void testHuge() {
        final int testSize = 1_000_000;

        final int tenPerc = testSize / 10;

        long _start = System.currentTimeMillis();
        BlockRangeIndex<Sized<Integer>, Sized<String>> index =
                new BlockRangeIndex<>(10000, new RandomPageReplacementPolicy(10000));
        index.boot(BlockRangeIndexMetadata.empty());
        for (int i = 0; i < testSize; i++) {
            index.put(Sized.valueOf(i), Sized.valueOf("test_" + i));
            if (i % tenPerc == 0) {
                System.out.println("insert : " + (1 + i / tenPerc) * 10 + "%: " + i);
            }
        }
        long _stop = System.currentTimeMillis();
        System.out.println("time w: " + (_stop - _start));
        System.out.println("num segments: " + index.getNumBlocks());
        for (int i = 0; i < testSize; i++) {
            List<Sized<String>> s = index.search(Sized.valueOf(i));
            Assert.assertEquals(1, s.size());
            Assert.assertEquals("test_" + i, s.get(0).dummy);

            if (i % tenPerc == 0) {
                System.out.println("search : " + (1 + i / tenPerc) * 10 + "%: " + i);
            }
        }
        _start = _stop;
        _stop = System.currentTimeMillis();
        System.out.println("time r: " + (_stop - _start));
        index.clear();
    }

    /**
     * Test with multivalued keys
     *
     * @author diego.salvi
     */
    @Test
    public void testHugeMultivalued() {
        final int testSize = 1_000_000;
        final int valuesPerKey = 10;

        final int tenPerc = testSize / 10;

        long _start = System.currentTimeMillis();
        BlockRangeIndex<Sized<Integer>, Sized<String>> index =
                new BlockRangeIndex<>(10000, new RandomPageReplacementPolicy(10000));
        index.boot(BlockRangeIndexMetadata.empty());
        for (int i = 0; i < testSize; i++) {
            Sized<Integer> si = Sized.valueOf(i);
            for (int j = 0; j < valuesPerKey; j++) {
                Sized<String> sij = Sized.valueOf("test_" + i + "_" + j);
                index.put(si, sij);
            }

            if (i % tenPerc == 0) {
                System.out.println("insert : " + (1 + i / tenPerc) * 10 + "%: " + i);
            }
        }
        long _stop = System.currentTimeMillis();
        System.out.println("time w: " + (_stop - _start));
        System.out.println("num segments: " + index.getNumBlocks());
        for (int i = 0; i < testSize; i++) {
            List<Sized<String>> s = index.search(Sized.valueOf(i));
            Assert.assertEquals(valuesPerKey, s.size());

            for (int j = 0; j < valuesPerKey; ++j) {
                Assert.assertEquals("test_" + i + "_" + j, s.get(j).dummy);
            }

            if (i % tenPerc == 0) {
                System.out.println("search : " + (1 + i / tenPerc) * 10 + "%: " + i);
            }
        }
        _start = _stop;
        _stop = System.currentTimeMillis();
        System.out.println("time r: " + (_stop - _start));
        index.clear();
    }

}
