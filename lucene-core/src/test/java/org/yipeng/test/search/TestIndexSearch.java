package org.yipeng.test.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class TestIndexSearch {
	@Test
	public void testIndexSearch() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/my_index" ;
		Directory dir = FSDirectory.open(Paths.get(path));
		IndexReader indexReader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		Sort sort = new Sort(new SortField("value", SortField.Type.STRING,true));
		TopDocs td = searcher.search(new MatchAllDocsQuery(), 10, sort);
		
		System.out.println(searcher.doc(td.scoreDocs[0].doc).get("value"));
		
	}
}
