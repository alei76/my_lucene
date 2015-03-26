package org.yipeng.test.analysis.demo;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.yipeng.test.analysis.LengthFilter;
import org.yipeng.test.analysis.WhitespaceTokenizer;

public class MyAnalyzer extends Analyzer{

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source = new  WhitespaceTokenizer();
		TokenStream result = new LengthFilter(source, 3, Integer.MAX_VALUE);
		return new TokenStreamComponents(source,result);
	}
	
	
	public static void main(String[] args) throws IOException {
		Analyzer analyzer = new MyAnalyzer();
		TokenStream ts = analyzer.tokenStream("myField", "wo are children you are my dream gilr");
		
		OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
		//表示位置增加的。
		PositionIncrementAttribute posAtt = ts.addAttribute(PositionIncrementAttribute.class);
		try{
			ts.reset();
			int i = 0;
			while(ts.incrementToken()){
				System.out.println("token : " + ts.reflectAsString(true));
				System.out.println("token start offset : " + offsetAtt.startOffset());
				System.out.println("token end offset : " + offsetAtt.endOffset());
				i += posAtt.getPositionIncrement();
				System.out.println("token pos : " + i);
			}
			ts.end();
		}finally{
			ts.close();
		}
		analyzer.close();
	}
}
