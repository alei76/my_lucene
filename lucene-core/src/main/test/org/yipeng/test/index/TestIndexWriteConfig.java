package org.yipeng.test.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriterConfig;
public class TestIndexWriteConfig {
	public static void main(String[] args) {
		IndexWriterConfig config = new IndexWriterConfig(new Analyzer() {
			
			@Override
			protected TokenStreamComponents createComponents(String fieldName) {
				return null;
			}
		});
	}
}
