package org.yipeng.test.analysis.demo;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.yipeng.test.analysis.WhitespaceAnalyzer;


public class AnalyzerTest {
	public static void main(String[] args) throws IOException {
		Version matchVersion = Version.LATEST;
		Analyzer analyzer = new WhitespaceAnalyzer();
		TokenStream ts = analyzer.tokenStream("myfield", new StringReader("some text goes here"));
		
		OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
		try{
			ts.reset();
			while(ts.incrementToken()){
				System.out.println("token : " + ts.reflectAsString(true));
				System.out.println("token start offset : " + offsetAtt.startOffset());
				System.out.println("token end offset : " + offsetAtt.endOffset());
			}
			ts.end();
		}finally{
			ts.close();
		}
	}
}
