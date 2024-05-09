package com.kgm.commands.nmcd;

import java.util.HashMap;

/**
 * [20190522][chkim] 
 */
public class JCellValue implements Comparable<JCellValue>{
	private String value;
	private String sortValue;
	private int order;
	private boolean isCancel = false;
	private HashMap<String, Object> data = null;
	
	
	public JCellValue(String value){
		this(value, value, 0);
	}
	
	public JCellValue(String value, boolean isCancel){
		this(value, value, 0, isCancel);
	}
	
	public JCellValue(String value, String sortValue, int order){
		this.value = value;
		this.sortValue = sortValue;
		this.order = order;
		this.isCancel = false;
	}
	
	public JCellValue(String value, String sortValue, int order, boolean isCancel){
		this.value = value;
		this.sortValue = sortValue;
		this.order = order;
		this.isCancel = isCancel;
	}
	
	public HashMap<String, Object> getData() {
		return data;
	}

	public void setData(HashMap<String, Object> data) {
		this.data = data;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getSortValue() {
		return sortValue;
	}
	public void setSortValue(String sortValue) {
		this.sortValue = sortValue;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public void setIsCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
	public boolean isCancel() {
		return isCancel;
	}
	
	public void clearOrder(){
		this.order = 0;
	}
	
	public void clearSortValue(){
		this.sortValue = value;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return value;
//		return value + "[" + sortValue + "][" + order + "]";
	}

	@Override
	public int compareTo(JCellValue o) {
		// TODO Auto-generated method stub
		int result = sortValue.compareTo(o.getSortValue());
		if( result != 0){
			return result;
		}else{
			if( order > o.getOrder()) {
				return 1;
			}else{
				return -1;
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if( obj instanceof JCellValue){
			JCellValue target = (JCellValue)obj;
			return value.equals(target.getValue());
		}else{
			return value.equals(obj.toString());
		}
	}
	
}
