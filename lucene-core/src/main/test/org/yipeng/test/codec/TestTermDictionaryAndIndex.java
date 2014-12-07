package org.yipeng.test.codec;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.yipeng.test.analysis.WhitespaceAnalyzer;

public class TestTermDictionaryAndIndex {
	@Test
	public void createIndexOnDisk() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/tim_tip_structure";
		Directory dir = FSDirectory.open(Paths.get(path));
		IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
		config.setUseCompoundFile(false).setCommitOnClose(true);
		config.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, config);
		
		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		type.setStored(true);
		type.setStoreTermVectors(true);
		type.setOmitNorms(true);
		type.setStoreTermVectorPayloads(true);
		type.setStoreTermVectorOffsets(true);
		type.setStoreTermVectorPositions(true);
		
		Document doc = new Document();
		Field field = new Field("myField", "abc abdf abdg abdh abei abej abek abel abem aben", type);
		doc.add(field);
		writer.addDocument(doc);
		writer.commit();
		
		writer.close();
	}

}
