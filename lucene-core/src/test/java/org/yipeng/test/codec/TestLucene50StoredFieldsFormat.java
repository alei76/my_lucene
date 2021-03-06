package org.yipeng.test.codec;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.lucene50.Lucene50FieldInfosFormat;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.junit.Test;

public class TestLucene50StoredFieldsFormat {
	/*
	 * 查看.fdt文件。其保存域数据文件，包含文档中存储的域数据。比如fieldName,fieldValue,域对应的fieldType
	 * 
	 */
	@Test
	public void testFDTRead() throws IOException{
		Lucene50StoredFieldsFormat storedFieldsFormat = new Lucene50StoredFieldsFormat();
		
		String path = "E:/yipeng/my_lucene/index_dir/index_structure/";
		Directory dir = FSDirectory.open(Paths.get(path));
	    String[] files = dir.listAll();
	    String lastSegmentsFile = SegmentInfos.getLastCommitSegmentsFileName(files);
	    SegmentInfos sis = SegmentInfos.readCommit(dir, lastSegmentsFile);
	    
	    Lucene50FieldInfosFormat fieldInfosFormat = new Lucene50FieldInfosFormat();
	    SegmentCommitInfo sci = sis.asList().get(0);
	    FieldInfos fn = fieldInfosFormat.read(sci.info.dir, sci.info, "", IOContext.READONCE);   //获取段中field的元数据。
	    final DocumentStoredFieldVisitor visitor = new DocumentStoredFieldVisitor();
		StoredFieldsReader sfr =  storedFieldsFormat.fieldsReader(dir,sci.info,fn,IOContext.DEFAULT);
		sfr.visitDocument(300000, visitor);
	}
}
