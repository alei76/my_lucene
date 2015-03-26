package org.yipeng.test.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public  abstract class FilteringTokenFilter extends TokenFilter{
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	protected FilteringTokenFilter(TokenStream input) {
		super(input);
	}
	
	//Override this method and return if the current input token should be returned by incrementToken
	//复写这个方法，并且返回如果当前input token应该被incrementToken 返回
	protected abstract boolean accept() throws IOException;
	
	@Override
	public boolean incrementToken() throws IOException {
		int skippedPositions = 0;
		while(input.incrementToken()){
			if(accept()){
				if(skippedPositions != 0){
					posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement()+skippedPositions);
				}
				return true;
			}
			skippedPositions += posIncrAtt.getPositionIncrement();
		}
		return false;
	}
	
	@Override
	public void reset() throws IOException {
		super.reset();
	}
}
