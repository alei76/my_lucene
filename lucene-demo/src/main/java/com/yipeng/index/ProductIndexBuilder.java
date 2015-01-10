package com.yipeng.index;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.yipeng.analysis.WhitespaceAnalyzer;

import com.yipeng.model.Product;
import com.yipeng.util.ProductModel;

public class ProductIndexBuilder {
	private Directory dir;
	IndexWriterConfig config;
	public ProductIndexBuilder(Directory dir) {
		this.dir = dir;
		config = new IndexWriterConfig(new WhitespaceAnalyzer());
		config.setUseCompoundFile(false).setCommitOnClose(true);
		config.setOpenMode(OpenMode.CREATE);
	}
	
	
	public void buildIndex(List<Product> products) throws IOException{
		IndexWriter writer = new IndexWriter(dir, config);
		for(Product prod : products){
			Document doc = new Document();
			doc.add(new LongField("id", prod.getProductId(), Store.YES));
			doc.add(new TextField("name", prod.getProductName(), Store.YES));
			doc.add(new LongField("brand_id", prod.getBrandId(), Store.YES));
			doc.add(new StringField("brand_name", prod.getBrandName(), Store.YES));
			doc.add(new LongField("category_id",prod.getCategoryId(),Store.YES));
			doc.add(new StringField("category_name", prod.getCategoryName(), Store.YES));
			doc.add(new DoubleField("score", prod.getScore(), Store.YES));
			writer.addDocument(doc);
		}
		writer.commit();
		writer.close();
	}
	
	public static void main(String[] args) throws IOException {
		String dataPath  = "src/main/resources/data/product.data";
		String path = "E:/yipeng/my_lucene/index_dir/my_index";
		Directory dir = FSDirectory.open(Paths.get(path));
		ProductIndexBuilder builder = new ProductIndexBuilder(dir);
		List<Product> products = ProductModel.getAllProducts(dataPath);
		builder.buildIndex(products);
	}
}
