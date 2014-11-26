package org.yipeng.test.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class TestIndexSearch {
	@Test
	public void testIndexSearch() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/index_structure";
		Directory dir = FSDirectory.open(Paths.get(path));
		IndexReader indexReader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		BooleanQuery bq = new BooleanQuery();
		bq.add(new TermQuery(new Term("country","china")),Occur.MUST);
		bq.add(new TermQuery(new Term("city","wuhan")),Occur.MUST);
		TopDocs sd = searcher.search(bq, Integer.MAX_VALUE);
		System.out.println(sd.totalHits);
	}
}
