package org.yipeng.test.codec;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.codecs.lucene50.Lucene50FieldInfosFormat;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.junit.Test;

public class TestLucene50FieldInfosFormat {
	@Test
	public void testLucene50FieldInfosFormatRead() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/index_structure/";
		Directory dir = FSDirectory.open(Paths.get(path));
	    String[] files = dir.listAll();
	    String lastSegmentsFile = SegmentInfos.getLastCommitSegmentsFileName(files);
	    SegmentInfos sis = SegmentInfos.readCommit(dir, lastSegmentsFile);
	    
	    Lucene50FieldInfosFormat fieldInfosFormat = new Lucene50FieldInfosFormat();
	    SegmentCommitInfo sci = sis.asList().get(0);
	    fieldInfosFormat.read(sci.info.dir, sci.info, "", IOContext.READONCE);
	}
}
