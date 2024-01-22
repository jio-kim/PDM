package com.symc.plm.rac.prebom.common.util.lov;

public class ListOfValue {
	private String stringValue;
	private String displayValue;
	private String description;
	private String displayDescription;

	public ListOfValue(String stringValue, String description, String displayValue,
			String displayDescription) {
		this.stringValue = stringValue;
		this.description = description;
		this.displayValue = displayValue;
		this.displayDescription = displayDescription;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayDescription() {
		return displayDescription;
	}

	public void setDisplayDescription(String displayDescription) {
		this.displayDescription = displayDescription;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return displayValue;
	}

	@Override
	public boolean equals(Object obj) {
		if( obj instanceof ListOfValue){
			return super.equals(obj);
		}else if(obj instanceof String){
			return displayValue.equals(obj.toString());
		}else{
			return super.equals(obj);
		}
	}

}