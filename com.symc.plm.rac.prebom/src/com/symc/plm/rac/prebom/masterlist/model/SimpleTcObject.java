package com.symc.plm.rac.prebom.masterlist.model;

public class SimpleTcObject {
	private String itemId = null;
	private String itemRevId = null;
	private String puid = null;
	
	public SimpleTcObject(String itemId, String puid){
		this(itemId, "000", puid);
	}
	public SimpleTcObject(String itemId, String itemRevId, String puid){
		this.itemId = itemId;
		this.itemRevId = itemRevId;
		this.puid = puid;
	}
	public String getPuid() {
		return puid;
	}
	public void setPuid(String puid) {
		this.puid = puid;
	}
	
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return itemId + "/" + itemRevId;
	}
	public String getItemRevId() {
		return itemRevId;
	}
	public void setItemRevId(String itemRevId) {
		this.itemRevId = itemRevId;
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if( obj instanceof SimpleTcObject){
			SimpleTcObject tcObj = (SimpleTcObject)obj;
			return this.puid.equals(tcObj.getPuid());
		}
		return super.equals(obj);
	}
	
	
}
