package com.ssangyong.commands.ospec.op;

public class OpComboValue extends OpValueName {

	public OpComboValue(String category, String categoryName, String option,
			String optionName) {
		super(category, categoryName, option, optionName);
		// TODO Auto-generated constructor stub
	}
	
	public OpComboValue(OpValueName opValueName){
		super();
		this.category = opValueName.getCategory();
		this.categoryName = opValueName.getCategoryName();
		this.option = opValueName.getOption();
		this.optionName = opValueName.getOptionName();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return option + " : " + optionName;
	}

	
}
