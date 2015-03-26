package org.yipeng.test.index;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;
import org.yipeng.test.analysis.WhitespaceAnalyzer;

public class TestIndexCreate {
	@Test
	public void createIndexOnDisk() throws IOException{
		String path = "D:/yipeng/my_lucene/index_dir/my_index";
		Directory dir = FSDirectory.open(Paths.get(path));
		IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
		config.setUseCompoundFile(false).setCommitOnClose(true);
		config.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, config);
		
		Document doc = new Document();
		doc.add(new SortedDocValuesField("value", new BytesRef("foo")));
		doc.add(new StringField("value", "foo", Field.Store.YES));
		writer.addDocument(doc);
		doc = new Document();
		doc.add(new SortedDocValuesField("value", new BytesRef("bar")));
		doc.add(new StringField("value", "bar", Field.Store.YES));
		writer.addDocument(doc);
		writer.close();
	}
	
	private static Document createDocument(String city,String country,FieldType type){
		Document doc =  new Document();
		Field f1 = new Field("city", city, type);
		Field f2 = new Field("country", country, type);
		doc.add(f1);
		doc.add(f2);
		return doc;
	}
}
