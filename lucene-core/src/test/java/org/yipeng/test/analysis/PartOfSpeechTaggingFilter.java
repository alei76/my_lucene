package org.yipeng.test.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.yipeng.test.analysis.PartOfSpeechAttribute.PartOfSpeech;

/*
 * 
 * This is a simple Attribute implementation has only a single variable that stores 
 * the part-of-speech of a token. It extends the AttributeImpl class and therefore 
 * implements its abstract methods clear() and copyTo(). Now we need a TokenFilter 
 * that can set this new PartOfSpeechAttribute for each token. 
 * In this example we show a very naive filter that tags
 *  every word with a leading upper-case letter as a 'Noun' and all other words as 'Unknown'.
 */


public class PartOfSpeechTaggingFilter extends TokenFilter{
	PartOfSpeechAttribute posAtt = addAttribute(PartOfSpeechAttribute.class);
	CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	
	public  PartOfSpeechTaggingFilter(TokenStream input) {
		super(input);
	}
	
	
	@Override
	public boolean incrementToken() throws IOException {
		if(!input.incrementToken()){
			return false;
		}
		posAtt.setPartOfSpeech(determinePOS(termAtt.buffer(), 0, termAtt.length()));
		return true;
	}
	
	protected PartOfSpeech determinePOS(char[] term , int offset , int length){
		if(length > 0 && Character.isUpperCase(term[0])){
			return PartOfSpeech.Noun;
		}
		return PartOfSpeech.Unknown;
	}
	
}
