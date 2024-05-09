package com.kgm.commands.namegroup.model;

public class PngProd {
	private String product = null;
	private int assignOrder = -1;
	
	public PngProd(String product, int assignOrder){
		this.product = product;
		this.assignOrder = assignOrder;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public int getAssignOrder() {
		return assignOrder;
	}

	public void setAssignOrder(int assignOrder) {
		this.assignOrder = assignOrder;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return product;
	}
	
}
