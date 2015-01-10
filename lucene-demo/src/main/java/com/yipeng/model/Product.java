package com.yipeng.model;

public class Product {
	private long productId;
	private String productName;
	private long brandId;
	private String brandName;
	private long categoryId;
	private String categoryName;
	private double score;
	
	/*
	 * 
	 */
	public static Product toProduct(String line){
		if(line == null){
			throw new NullPointerException("input can't be null");
		}
		String[] arrays = line.split(",");
		Product p = new Product();
		p.setProductId(Long.parseLong(arrays[0]));
		p.setProductName(arrays[1]);
		p.setBrandId(Long.parseLong(arrays[2]));
		p.setBrandName(arrays[3]);
		p.setCategoryId(Long.parseLong(arrays[4]));
		p.setCategoryName(arrays[5]);
		p.setScore(Double.parseDouble(arrays[6]));
		return p;
	}
	
	
	public long getProductId() {
		return productId;
	}
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}


	public long getBrandId() {
		return brandId;
	}


	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}
}
