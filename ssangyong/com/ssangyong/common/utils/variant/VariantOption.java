package com.ssangyong.common.utils.variant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VariantOption implements Comparable{
	
	private String itemId;
	private String optionName;
	private String optionDesc;
//	private List<VariantValue> values;
	private HashMap<String, VariantValue> valueMap;
	
	private OVEOption oveOption;
	private int oveOptionId; //OVEOption의 id이며, Corporate Option Item일 경우에만 값이 입력된다. 옵션 변경시 사용됨.
	
	public VariantOption(OVEOption oveOption,String itemId, String optionName, String optionDesc){
		this(oveOption,itemId, optionName, optionDesc, null, -1);
	}
	
	public VariantOption(OVEOption oveOption, String itemId, String optionName, String optionDesc, HashMap<String, VariantValue> valueMap, int oveOptionId){
		this.oveOption = oveOption;
		this.itemId = itemId;
		this.optionName = optionName;
		this.optionDesc = optionDesc;
		this.valueMap = valueMap;
		this.oveOptionId = oveOptionId;
	}
	
	public int getOveOptionId() {
		return oveOptionId;
	}

	public OVEOption getOveOption() {
		return oveOption;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getOptionName() {
		return optionName;
	}
	
	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}
	
	public String getOptionDesc() {
		return optionDesc;
	}
	
	public void setOptionDesc(String optionDesc) {
		this.optionDesc = optionDesc;
	}
	
	public List<VariantValue> getValues() {
		if( valueMap == null) return null;
		
		Collection collection = valueMap.values();
		ArrayList list = new ArrayList();
		list.addAll(collection);
		return list;
	}
	
	public VariantValue getValue(String valueName){
		if( valueMap == null) return null;
		
		return valueMap.get(valueName);
	}
	
	public void setValues(HashMap<String, VariantValue> valueMap) {
		this.valueMap = valueMap;
	}
	
	public void addValue(VariantValue value){

		if(value == null) return;
		
		if( valueMap == null){
			valueMap = new HashMap();
		}
		
		if( !valueMap.containsValue(value))
			valueMap.put(value.getValueName(), value);
	}
	
	public void addValues(HashMap<String, VariantValue> newValues){

		if(newValues == null || newValues.isEmpty()) return;
		
		if( valueMap == null){
			valueMap = new HashMap();
		}
		
		Collection collection = newValues.values();
		Iterator<VariantValue> its = collection.iterator();
		while(its.hasNext()){
			VariantValue value = its.next();
			if( !valueMap.containsValue(value))
				valueMap.put(value.getValueName(), value);
		}
		
	}
	
	public HashMap<String, VariantValue> getValueMap() {
		return valueMap;
	}

	public boolean hasValues(){
		if( valueMap == null ){
			return false;
		}
		
		if( valueMap.isEmpty()){
			return false;
		}
		
		return true;
	}

	/**
	 * 옵션의 이름만 같으면 같다고 판단
	 */
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof VariantOption){
			VariantOption option = (VariantOption)obj;
			if(option.getOptionName().equals(getOptionName())){
				return true;
			}
		}
		return super.equals(obj);
	}

	/**
	 * 객체 복제.
	 * @return
	 */
	public VariantOption duplicate() {
		VariantOption option = new VariantOption(oveOption, itemId, optionName, optionDesc);
		List<VariantValue> values =  getValues();
		for( int i = 0; values != null && i < values.size(); i++){
			VariantValue value = new VariantValue(option, values.get(i).getValueName(), values.get(i).getValueDesc(), values.get(i).getValueStatus(), values.get(i).isNew());
			option.addValue(value);
		}
		
		return option;
	}

	/**
	 * [SR140722-022][20140708] swyoon 옵션 Sorting.
	 */		
	@Override
	public int compareTo(Object obj) {
		if( obj instanceof VariantOption){
			VariantOption option = (VariantOption)obj;
			return toString().compareTo(option.toString());
		}
		return 0;
	}

	/**
	 * [SR140722-022][20140708] swyoon 옵션 Sorting.
	 */		
	@Override
	public String toString() {
		return this.getOptionName() + " " + this.getOptionDesc();
	}

}
