package org.yipeng.test.codec;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.codecs.lucene50.Lucene50FieldInfosFormat;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.yipeng.test.analysis.WhitespaceAnalyzer;

/**
 * 
 * @author yipeng
 *测试索引的结构文件。
 *
 *		文件名						扩展名							描述
 *Segments File				segments.gen,segments_N			段文件，存储提交点的信息
 *Lock File					write.lock						写文件锁，阻止多个IndexWriter写入同一个文件
 *Segment Info 				.si								段信息文件，存储段的元数据
 *Compound File    			.cfs , .cfe						复合文件，它是一个虚拟文件，为系统中使用较为频繁的索引文件
 *Fields					.fnm							域文件，存储field信息		{@link Lucene50FieldInfosFormat}
 *Fields Index				.fdx							域指针文件，包含域数据数据的指针
 *Fields Data				.fdt							域数据文件，包含文档中存储的域数据
 *Term Dictionary			.tim							项词典，存储项信息
 *Term Index				.tip							项索引，存储到项词典的索引
 *Frequencies				.doc							频率,包含每个项以及频率信息
 *Positions					.pos							位置，存储一个项在索引中的位置信息
 *Payloads					.pay							载荷，存储额外的预先设置好的元信息，如：字符偏移或者用户载荷
 *Norms						.nvd , .nvm						调整因子，编码文档和域的长度及加权
 *Per-Document Values		.dvd , .dvm						编码额外的打分因子以及预置的文档信息
 *Term Vector Index			.tvx							文档向量索引，存储在文档数据文件的偏移
 *Term Vector Documents 	.tvd							包含每个文档的项向量信息
 *Term Vector Fields		.tvf							包含域级别的向量信息
 *Deleted Documents			.del							关于文档被删除的信息
 *
 *
 *
 *
 *
 */
public class TestIndexStructure {
	
	@Test
	public void createIndexOnDisk() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/index_structure";
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
		
		Document doc1 = createDocument("wuhan", "china", type);
		Document doc2 = createDocument("LA", "USA", type);
		Document doc3 = createDocument("New York", "USA", type);
		writer.addDocument(doc1);
		writer.addDocument(doc2);
		writer.addDocument(doc3);
		writer.commit();                     //通过多次commit,产生多个Segment段.
		
		
		Document doc4 = createDocument("ShangHai", "China", type);
		Document doc5 = createDocument("London", "UK", type);
		writer.addDocument(doc4);
		writer.addDocument(doc5);
		writer.commit();
		
		Document doc6 = createDocument("Sydney", "Australia", type);
		Document doc7 = createDocument("Paris", "France", type);
		writer.addDocument(doc6);
		writer.addDocument(doc7);
		writer.commit();
		
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
