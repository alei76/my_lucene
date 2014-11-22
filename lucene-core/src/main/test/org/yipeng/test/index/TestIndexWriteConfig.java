package org.yipeng.test.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.lucene50.Lucene50Codec;
import org.apache.lucene.index.IndexWriterConfig;
public class TestIndexWriteConfig {
	public static void main(String[] args) throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(new Analyzer() {
			@Override
			protected TokenStreamComponents createComponents(String fieldName) {
				return null;
			}
		});
		
		config.setCodec(new Lucene50Codec()).setCommitOnClose(true);       //可以使用链设置参数。
	}
}
