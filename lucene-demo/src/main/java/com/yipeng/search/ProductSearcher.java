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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ProductSearcher {
	public static void main(String[] args) throws IOException {
		String path = "E:/yipeng/my_lucene/index_dir/my_index" ;
		Directory dir = FSDirectory.open(Paths.get(path));
		IndexReader indexReader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TermQuery query = new TermQuery(new Term("name", "三星"));
		TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
		for(ScoreDoc sc : topDocs.scoreDocs){
			StoredDocument doc = indexReader.document(sc.doc);
			System.out.println("id : "+doc.get("id") + " name : "+doc.get("name") +" score : "+doc.get("score"));
		}
	}
}
