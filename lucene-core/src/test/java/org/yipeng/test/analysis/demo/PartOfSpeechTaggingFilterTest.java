package org.yipeng.test.analysis.demo;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.yipeng.test.analysis.LengthFilter;
import org.yipeng.test.analysis.PartOfSpeechAttribute;
import org.yipeng.test.analysis.PartOfSpeechTaggingFilter;
import org.yipeng.test.analysis.WhitespaceTokenizer;

public class PartOfSpeechTaggingFilterTest {
	public static void main(String[] args) throws IOException {
		Analyzer analyzer = new Analyzer() {
			@Override
			protected TokenStreamComponents createComponents(String fieldName) {
				final Tokenizer source = new WhitespaceTokenizer();
				TokenStream result = new LengthFilter(source, 3, Integer.MAX_VALUE);
				PartOfSpeechTaggingFilter posResult = new PartOfSpeechTaggingFilter(result);
				return new TokenStreamComponents(source,posResult);
			}
		};
		
		TokenStream ts = analyzer.tokenStream("myField", "wo Are children You are my Dream gilr");
		
		OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
		//表示位置增加的。
		PositionIncrementAttribute posAtt = ts.addAttribute(PositionIncrementAttribute.class);
		
		PartOfSpeechAttribute spAtt = ts.addAttribute(PartOfSpeechAttribute.class);
		
		try{
			ts.reset();
			int i = 0;
			while(ts.incrementToken()){
				System.out.println("token : " + ts.reflectAsString(true));
				System.out.println("token start offset : " + offsetAtt.startOffset());
				System.out.println("token end offset : " + offsetAtt.endOffset());
				i += posAtt.getPositionIncrement();
				System.out.println("token pos : " + i);
				System.out.println("token speech : " + spAtt.getPartOfSpeech());
			}
			ts.end();
		}finally{
			ts.close();
		}
		analyzer.close();
	}
}
