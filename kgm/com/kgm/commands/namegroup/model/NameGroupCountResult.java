package com.kgm.commands.namegroup.model;

import java.util.ArrayList;
import java.util.HashMap;

public class NameGroupCountResult {
//	private String CountStr = null;
	private boolean isValid = true;
	private String reason = null;
	private HashMap<String, Integer> nameCountMap = null;
	private int totCount = 0;
	
	public NameGroupCountResult(){
		this(0, true);
	}
	public NameGroupCountResult(int totCount, boolean isValid){
		this.totCount = totCount;
		this.isValid = isValid;
	}
	public String getCountStr() {
		return totCount + "";
	}
	public void setCountStr(int totCount) {
		this.totCount = totCount;
	}
	public boolean isValid() {
		return isValid;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getTotCount() + "";
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public HashMap<String, Integer> getNameCountMap() {
		return nameCountMap;
	}
	public void setNameCountMap(HashMap<String, Integer> nameCountMap) {
		this.nameCountMap = nameCountMap;
	}
	public void addNameCount(String partName, int count){
		if( nameCountMap == null){
			nameCountMap = new HashMap();
		}
		
		Integer nameCount = nameCountMap.get(partName);
		if( nameCount == null){
			nameCountMap.put(partName, new Integer(count));
		}else{
			nameCountMap.put(partName, new Integer(nameCount.intValue() + count));
		}
	}
	
	public int getNameCount(String partName){
		if( nameCountMap == null){
			return 0;
		}
		
		Integer nameCount = nameCountMap.get(partName);
		if(nameCount == null){
			return 0;
		}
		
		return nameCount.intValue();
	}
	
	public int getTotCount(){
		if( nameCountMap == null){
			return totCount;
		}else{
			int tmpCount = 0;
			String[] partNames = nameCountMap.keySet().toArray(new String[nameCountMap.size()]);
			for( String partName : partNames){
				Integer intObj = nameCountMap.get(partName);
				tmpCount += intObj;
			}
			
			return tmpCount;
		}
	}
}
