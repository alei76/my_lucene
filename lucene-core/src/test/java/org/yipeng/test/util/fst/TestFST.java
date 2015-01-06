package org.yipeng.test.util.fst;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.ByteSequenceOutputs;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.Outputs;
import org.apache.lucene.util.fst.Util;


public class TestFST {
	public static void main(String[] args) throws IOException {
		String inputValues[] = {"abd","abe","acf"};
		byte[] outputValues[] ={{1,2},{3,4},{5,6}};
		
		Outputs<BytesRef> outputs = ByteSequenceOutputs.getSingleton();
		Builder<BytesRef> builder = new Builder<BytesRef>(FST.INPUT_TYPE.BYTE1, outputs);
		BytesRefBuilder scratchBytes = new BytesRefBuilder();
		IntsRefBuilder scratchInts = new IntsRefBuilder();
		for(int i = 0 ; i<inputValues.length ; i++){
			scratchBytes.copyChars(inputValues[i]);
			builder.add(Util.toIntsRef(scratchBytes.get(), scratchInts), new BytesRef(outputValues[i]));
		}
		FST<BytesRef> fst = builder.finish();
		BytesRef value = Util.get(fst, new BytesRef("acf"));
		System.out.println(value);
	}
}
