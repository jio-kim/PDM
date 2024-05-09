package com.kgm.commands.ospec.op;

import java.util.ArrayList;

public class OpCategory {
	private String category = null;
	private String categoryName = null;
	private ArrayList<Option> opValueList = null;
	
	public OpCategory(String category, String categoryName){
		this.category = category;
		this.categoryName = categoryName;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public ArrayList<Option> getOpValueList() {
		return opValueList;
	}
	public void setOpValueList(ArrayList<Option> opValueList) {
		this.opValueList = opValueList;
	}

	@Override
	public boolean equals(Object obj) {

		if( obj instanceof OpCategory){
			OpCategory opCategory = (OpCategory)obj;
			return this.category.equals(opCategory.category);
		}
		return super.equals(obj);
	}
	
	
}
