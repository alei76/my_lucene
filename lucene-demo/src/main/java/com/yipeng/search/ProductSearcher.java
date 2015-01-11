package com.yipeng.search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredDocument;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ProductSearcher {
	public static void main(String[] args) throws IOException {
		String path = "E:/yipeng/my_lucene/index_dir/my_index" ;
		Directory dir = FSDirectory.open(Paths.get(path));
		IndexReader indexReader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TermQuery query = new TermQuery(new Term("name", "apple"));
		TermQuery query1 = new TermQuery(new Term("name","samsung"));
		query1.setBoost(100f);
		BooleanQuery bq = new BooleanQuery();
		bq.add(query1, Occur.SHOULD);
		bq.add(query,Occur.SHOULD);
		Sort sort = new Sort(new SortedNumericSortField("score", SortField.Type.DOUBLE,true));
		TopDocs topDocs = searcher.search(bq,Integer.MAX_VALUE,sort);
		for(ScoreDoc sc : topDocs.scoreDocs){
			StoredDocument doc = indexReader.document(sc.doc);
			System.out.println("id : "+doc.get("id") + " name : "+doc.get("name") +" score : "+doc.get("score"));
		}
	}
}
