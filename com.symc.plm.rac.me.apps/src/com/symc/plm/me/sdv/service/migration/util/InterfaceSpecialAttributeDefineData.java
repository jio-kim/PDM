package com.symc.plm.me.sdv.service.migration.util;

public class InterfaceSpecialAttributeDefineData {

	private String attributeName;
	private int[] stringConstructionColumns;

	public InterfaceSpecialAttributeDefineData(String elementName, int[] stringConstructionColumns){
		this.attributeName = elementName;
		this.stringConstructionColumns = stringConstructionColumns;
	}
	
	public String getAttributeName() {
		return attributeName;
	}

	public int[] getStringConstructionColumns() {
		return stringConstructionColumns;
	}
}
