package com.symc.plm.rac.prebom.masterlist.model;

import java.util.HashMap;

/**
 * [20161031][ymjang] 옵션입력 Dialog 창에서 Cancel 버튼을 클릭할 경우, Usage 수량 초기화하지 않도록 수정
 */
public class CellValue implements Comparable<CellValue>{
	private String value;
	private String sortValue;
	private int order;
	// [20161031][ymjang] 옵션입력 Dialog 창에서 Cancel 버튼을 클릭할 경우, Usage 수량 초기화하지 않도록 수정
	private boolean isCancel = false;
	private HashMap<String, Object> data = null;
	
	public static final String BOM_LINE = "BOM_LINE";
	public static final String PROP_MAP = "PROP_MAP";
	
	public CellValue(String value){
		this(value, value, 0);
	}
	
	public CellValue(String value, boolean isCancel){
		this(value, value, 0, isCancel);
	}
	
	public CellValue(String value, String sortValue, int order){
		this.value = value;
		this.sortValue = sortValue;
		this.order = order;
		this.isCancel = false;
	}
	
	public CellValue(String value, String sortValue, int order, boolean isCancel){
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
	public int compareTo(CellValue o) {
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
		if( obj instanceof CellValue){
			CellValue target = (CellValue)obj;
			return value.equals(target.getValue());
		}else{
			return value.equals(obj.toString());
		}
	}
	
}
