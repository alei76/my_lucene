package org.yipeng.test.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final  class LengthFilter extends FilteringTokenFilter{
	private final int min;
	private final int max;
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	
	
	public LengthFilter(TokenStream input, int min, int max){
		super(input);
		this.min = min;
		this.max = max;
	}
	
	
	@Override
	protected boolean accept() throws IOException {
		final int len = termAtt.length();
		return (len >= min && len <= max);
	}
	
}
