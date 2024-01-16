package com.ssangyong.commands.namegroup.model;

import java.util.ArrayList;

public class PngCondition implements Comparable<PngCondition>{
	private int groupNumber = -1;
	private String product = null;
	private String condition = null;
	private String operator = null;
	private int quantity = -1;
	private ArrayList<String> partNameList = null;
	
	public PngCondition(int groupNumber, String product, String condition, String operator, int quantity){
		this.groupNumber = groupNumber;
		this.product = product;
		this.condition = condition;
		this.operator = operator;
		this.quantity = quantity;
	}
	
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	public ArrayList<String> getPartNameList() {
		return partNameList;
	}

	public void setPartNameList(ArrayList<String> partNameList) {
		this.partNameList = partNameList;
	}

	@Override
	public int compareTo(PngCondition arg) {
		// TODO Auto-generated method stub
		if( groupNumber > arg.getGroupNumber()){
			return 1;
		}else if( groupNumber < arg.getGroupNumber()){
			return -1;
		}else{
			return 0;
		}
	}
	
}
