package com.symc.work.model;

import java.util.ArrayList;

public class NameGroupConditionVO implements Comparable<NameGroupConditionVO> {
	private int iGroupNumber = -1;
	private String sProduct = null;
	private String sCondition = null;
	private String sOperator = null;
	private int iQuantity = -1;
	private ArrayList<String> alPartNameList = null;
	
	public NameGroupConditionVO(int iGroupNumber, String sProduct, String sCondition, String sOperator, int iQuantity){
		this.iGroupNumber = iGroupNumber;
		this.sProduct = sProduct;
		this.sCondition = sCondition;
		this.sOperator = sOperator;
		this.iQuantity = iQuantity;
	}
	
	public String getProduct() {
		return sProduct;
	}
	
	public void setProduct(String sProduct) {
		this.sProduct = sProduct;
	}
	
	public String getCondition() {
		return sCondition;
	}
	
	public void setCondition(String sCondition) {
		this.sCondition = sCondition;
	}
	
	public String getOperator() {
		return sOperator;
	}
	
	public void setOperator(String sOperator) {
		this.sOperator = sOperator;
	}
	
	public int getQuantity() {
		return iQuantity;
	}
	
	public void setQuantity(int iQuantity) {
		this.iQuantity = iQuantity;
	}

	public int getGroupNumber() {
		return iGroupNumber;
	}

	public void setGroupNumber(int iGroupNumber) {
		this.iGroupNumber = iGroupNumber;
	}

	public ArrayList<String> getPartNameList() {
		return alPartNameList;
	}

	public void setPartNameList(ArrayList<String> alPartNameList) {
		this.alPartNameList = alPartNameList;
	}

	@Override
	public int compareTo(NameGroupConditionVO voNameGroupCondition) {
		if( iGroupNumber > voNameGroupCondition.getGroupNumber()){
			return 1;
		}else if( iGroupNumber < voNameGroupCondition.getGroupNumber()){
			return -1;
		}else{
			return 0;
		}
	}
}
