package org.yipeng.test.codec;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.lucene50.Lucene50SegmentInfoFormat;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.MergeInfo;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class TestLucene50SegmentInfoFormat {
	
	
	
	public void testLucene50SegmentInfoWrite() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/index_structure/segmentInfoFormat";
		Directory dir = FSDirectory.open(Paths.get(path));
	      final IOContext context = new IOContext(new MergeInfo(10, -1, true, -1));
	      SegmentInfo info = new SegmentInfo(dir, Version.LATEST, "_5", 10,
	                                         false, Codec.getDefault(), null, StringHelper.randomId());
	      info.setFiles(new HashSet<String>());
	      Lucene50SegmentInfoFormat segmentInfoFormat = new Lucene50SegmentInfoFormat();
	      segmentInfoFormat.write(dir, info, context);
		
	}
	
	
	
	@Test
	public void testLucene50SegmentInfoRead() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/index_structure/";
		Directory dir = FSDirectory.open(Paths.get(path));
	    String[] files = dir.listAll();
	    String lastSegmentsFile = SegmentInfos.getLastCommitSegmentsFileName(files);   //获取最后segment,文件名如:segments_N
	    SegmentInfos sis = SegmentInfos.readCommit(dir, lastSegmentsFile);
	    System.out.println(sis);
	    System.out.println(sis.getGeneration());        //  generation of the "segments_N" for the next commit
	    System.out.println(sis.version);                 /** Counts how often the index has been changed.  */
	    System.out.println(sis.counter); 				/** Used to name new segments. */
	}
}
