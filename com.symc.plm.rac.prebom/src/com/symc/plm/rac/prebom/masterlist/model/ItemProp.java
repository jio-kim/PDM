package com.symc.plm.rac.prebom.masterlist.model;

import com.symc.plm.rac.prebom.common.PropertyConstant;

public enum ItemProp {
	ITEMID(PropertyConstant.ATTR_NAME_ITEMID), UOM(PropertyConstant.ATTR_NAME_UOMTAG);
	
	private String propName;
	private ItemProp(String propName) {
        this.propName = propName;
	}
	
	public static String[] getPropNames(){
		ItemProp[] itemProps = ItemProp.values();
		String[] propNames = new String[itemProps.length];
		for( int i = 0; i < itemProps.length; i++){
			ItemProp itemProp = itemProps[i];
			propNames[i] = itemProp.propName;
		}
		
		return propNames;
	}

	public String getPropName() {
		return propName;
	}
	
};
