package com.kgm.soa.ospec;

public class OPItem {
	private String itemID = null;
	private String itemName = null;
	
	public OPItem(String itemID, String itemName){
		this.itemID = itemID;
		this.itemName = itemName;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
}
