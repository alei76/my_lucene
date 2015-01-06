package org.yipeng.test.analysis;

import org.apache.lucene.analysis.Analyzer;

public class WhitespaceAnalyzer extends Analyzer{
	
	public WhitespaceAnalyzer() {
	}
	
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		return new TokenStreamComponents(new WhitespaceTokenizer());
	}

}
