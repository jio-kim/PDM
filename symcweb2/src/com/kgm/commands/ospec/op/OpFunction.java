package com.kgm.commands.ospec.op;

public class OpFunction implements Comparable<OpFunction>{
	private String itemId = null;
	private String itemRevId = null;
	private String itemName = null;
	private String productId = null;
	private String project = null;
	
	public OpFunction(String itemId, String itemRevId,String itemName, String productId,String project ){
		this.itemId = itemId;
		this.itemRevId = itemRevId;
		this.itemName = itemName;
		this.productId = productId;
		this.project = project;
	}
	
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getItemRevId() {
		return itemRevId;
	}
	public void setItemRevId(String itemRevId) {
		this.itemRevId = itemRevId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}

	@Override
	public String toString() {
		return itemId;
	}

	@Override
	public int compareTo(OpFunction function) {
		return itemId.compareTo(function.getItemId());
	}
	
}
