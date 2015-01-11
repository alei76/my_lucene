package com.yipeng.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredDocument;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class TermRangeFilterDemo {
	
	/*
	 * {@TermRangeTermsEnum}里进行比较,首先setInitialSeekTerm(lowerBytesRef);然后与upperBytesRef比较获取范围内的。
	 */
	public void testTermRangeFilter() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/my_index" ;
		Directory dir = FSDirectory.open(Paths.get(path));
		IndexReader indexReader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TermQuery query = new TermQuery(new Term("name", "手机"));
		System.out.println("命中3个,lower包含a,upper包含c");
		TopDocs topDocs = searcher.search(query, TermRangeFilter.newStringRange("code", "a", "c", true, true), Integer.MAX_VALUE);
		showTopDocs(topDocs, indexReader);
		System.out.println("命中2个,lower不包含a,upper包含c");
		topDocs = searcher.search(query, TermRangeFilter.newStringRange("code", "a", "c", false, true), Integer.MAX_VALUE);
		showTopDocs(topDocs, indexReader);

	}
	
	public static void main(String[] args) throws IOException {
		TermRangeFilterDemo demo = new TermRangeFilterDemo();
		demo.testTermRangeFilter();
	}
	
	public void showTopDocs(TopDocs topDocs,IndexReader indexReader) throws IOException{
		for(ScoreDoc sc : topDocs.scoreDocs){
			StoredDocument doc = indexReader.document(sc.doc);
			System.out.println("id : "+doc.get("id") + " name : "+doc.get("name") +" score : "+doc.get("score") + " code : " + doc.get("code"));
		}
	}
}
