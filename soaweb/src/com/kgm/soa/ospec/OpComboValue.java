package com.kgm.soa.ospec;

public class OpComboValue extends OpValueName {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public OpComboValue(String category, String categoryName, String option,
			String optionName) {
		super(category, categoryName, option, optionName);
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
		return option + " : " + optionName;
	}

	
}
