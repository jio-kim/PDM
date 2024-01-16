package com.ssangyong.commands.ospec.op;

import java.io.Serializable;

public class OpValueName implements Comparable<OpValueName>, Serializable{
	protected String category = null;
	protected String categoryName = null;
	protected String option= null;
	protected String optionName;
	
	protected OpValueName(){
	}
	
	public OpValueName(String category, String categoryName, String option, String optionName){
		this.category = category;
		this.categoryName = categoryName;
		this.option = option;
		this.optionName = optionName;
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
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	public String getOptionName() {
		return optionName;
	}
	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return option; 
	}

	@Override
	public int compareTo(OpValueName o) {
		OpValueName target = (OpValueName)o;
		int cResult = category.compareTo(target.getCategory());
		if( cResult == 0){
			return this.option.compareTo(target.getOption());
		}else{
			return cResult;
		}
//		return option.compareTo(target.getOption());
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new OpValueName(category, categoryName, option, optionName);
	}
	
}
