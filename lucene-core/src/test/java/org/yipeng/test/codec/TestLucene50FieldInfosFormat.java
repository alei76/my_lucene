package org.yipeng.test.codec;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.codecs.lucene50.Lucene50FieldInfosFormat;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.junit.Test;

public class TestLucene50FieldInfosFormat {
	/*
	 * 这个是在 .fnm 文件里面的信息
	 */
	@Test
	public void testLucene50FieldInfosFormatRead() throws IOException{
		String path = "E:/yipeng/my_lucene/index_dir/index_structure/";
		Directory dir = FSDirectory.open(Paths.get(path));
	    String[] files = dir.listAll();
	    String lastSegmentsFile = SegmentInfos.getLastCommitSegmentsFileName(files);
	    SegmentInfos sis = SegmentInfos.readCommit(dir, lastSegmentsFile);
	    
	    Lucene50FieldInfosFormat fieldInfosFormat = new Lucene50FieldInfosFormat();
	    SegmentCommitInfo sci = sis.asList().get(0);
	    FieldInfos fieldInfos = fieldInfosFormat.read(sci.info.dir, sci.info, "", IOContext.READONCE); //这个段中所有域的元数据在FieldInfos里面。
	    
	    FieldInfo fieldInfoByName1 = fieldInfos.fieldInfo("city");			//通过名字得到单个FieldInfo的信息
	    FieldInfo fieldInfoByNumber1 = fieldInfos.fieldInfo(0);				//通过Number获得单个FieldInfo的信息，也就是加入到doc时的顺序。
	    
	    FieldInfo fieldInfoByName2 = fieldInfos.fieldInfo("country");
	    FieldInfo fieldInfoByNumber2 = fieldInfos.fieldInfo(1);
	    
	    System.out.println(fieldInfoByName1.name);
	    System.out.println(fieldInfoByName1.getIndexOptions());
	    
	    System.out.println(fieldInfoByNumber1.name);
	    System.out.println(fieldInfoByNumber1.getIndexOptions());
	    
	    System.out.println(fieldInfoByName2.name);
	    System.out.println(fieldInfoByName2.getIndexOptions());
	    
	    System.out.println(fieldInfoByNumber2.name);
	    System.out.println(fieldInfoByNumber2.getIndexOptions());
	}
}
