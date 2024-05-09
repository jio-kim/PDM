package com.kgm.commands.ifpe;

import java.util.HashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FunctionInfo implements Comparable{
	private String itemId;
	private HashMap data;
	
	public FunctionInfo(String itemId, HashMap data){
		this.itemId = itemId;
		this.setData(data);
	}
	
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public HashMap getData() {
		return data;
	}
	public void setData(HashMap data) {
		if( this.data == null ){
			this.data = new HashMap();
		}
		
		this.data.clear();
		this.data.putAll(data);
	}
	@Override
	public String toString() {
		if( data != null){
			return data.get("ITEM_ID") + "-" + data.get("ITEM_NAME");
		}
		return "";
	}

	@Override
	public int compareTo(Object o) {
		
		if( o instanceof FunctionInfo){
			FunctionInfo tmpInfo = (FunctionInfo)o;
			return this.itemId.compareTo(tmpInfo.getItemId());
		}
		
		return 0;
	}
	
	
}
