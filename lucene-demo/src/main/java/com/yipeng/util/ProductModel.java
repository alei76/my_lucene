package com.yipeng.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.yipeng.model.Product;

public class ProductModel {
	public static List<Product> getAllProducts(String path) throws IOException{
		File f = new File(path);
		List<Product> products = new ArrayList<Product>();
		if(f.exists()){
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf8"));
			String line = reader.readLine();
			
			while(line != null){
				products.add(Product.toProduct(line));
				line = reader.readLine();
			}
			reader.close();
		}
		return products;
	}
	
	public static void main(String[] args) throws IOException {
		String path  = "src/main/resources/data/product.data";
		List<Product> products = ProductModel.getAllProducts(path);
		System.out.println(products);
	}
}
