package org.yipeng.test.util.packed;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.packed.BlockPackedWriter;
import org.apache.lucene.util.packed.PackedInts;
import org.junit.Test;

public class TestPackedInts {

	private static Random random = new Random();

	@Test
	public void testByteCount() {
		for (int i = 0; i < 5; ++i) {
			final int valueCount = random.nextInt(Integer.MAX_VALUE);
			for (PackedInts.Format format : PackedInts.Format.values()) {
				for (int bpv = 1; bpv <= 64; ++bpv) {
					final long byteCount = format.byteCount(PackedInts.VERSION_CURRENT, valueCount, bpv);
					String msg = "format=" + format + ", byteCount="+ byteCount + ", valueCount=" + valueCount+ ", bpv=" + bpv;
					assertTrue(msg, byteCount * 8 >= (long) valueCount * bpv);
					if (format == PackedInts.Format.PACKED) {
						assertTrue(msg, (byteCount - 1) * 8 < (long) valueCount* bpv);
					}
				}
			}
		}
	}

	public void testBitsRequired() {
		assertEquals(61, PackedInts.bitsRequired((long) Math.pow(2, 61) - 1));
		assertEquals(61, PackedInts.bitsRequired(0x1FFFFFFFFFFFFFFFL));
		assertEquals(62, PackedInts.bitsRequired(0x3FFFFFFFFFFFFFFFL));
		assertEquals(63, PackedInts.bitsRequired(0x7FFFFFFFFFFFFFFFL));
		assertEquals(64, PackedInts.unsignedBitsRequired(-1));
		assertEquals(64, PackedInts.unsignedBitsRequired(Long.MIN_VALUE));
		assertEquals(1, PackedInts.bitsRequired(0));
	}

	public void testMaxValues() {
		assertEquals("1 bit -> max == 1", 1, PackedInts.maxValue(1));
		assertEquals("2 bit -> max == 3", 3, PackedInts.maxValue(2));
		assertEquals("8 bit -> max == 255", 255, PackedInts.maxValue(8));
		assertEquals("63 bit -> max == Long.MAX_VALUE", Long.MAX_VALUE,PackedInts.maxValue(63));
		assertEquals("64 bit -> max == Long.MAX_VALUE (same as for 63 bit)",Long.MAX_VALUE, PackedInts.maxValue(64));
	}
	
	@Test
	public void testBlockReader() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/packedints";
		Directory dir = FSDirectory.open(Paths.get(path));
		final IndexOutput out = dir.createOutput("out.bin", IOContext.DEFAULT);
		final BlockPackedWriter writer = new BlockPackedWriter(out, 128);
		writer.add(2);
		writer.add(4);
		writer.add(2<<3);
		writer.add(2<<4);
		writer.add(2<<5);
		writer.add(2<<6);
		writer.add(2<<7);
		writer.add(2<<8);
		writer.add(2<<9);
		writer.finish();
		out.close();
	}
	
	
}