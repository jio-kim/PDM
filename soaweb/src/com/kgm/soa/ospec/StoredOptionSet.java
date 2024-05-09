package com.kgm.soa.ospec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;

public class StoredOptionSet {
	private String name = null;
	private HashMap<String, ArrayList<String>> optionSet = new HashMap<String, ArrayList<String>>();
	private String stringValue = null;
	private ArrayList<String> valueList = new ArrayList<String>();
	
	public StoredOptionSet(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public HashMap<String, ArrayList<String>> getOptionSet() {
		return optionSet;
	}

	public void add(String option, String value){
		ArrayList<String> opValues = optionSet.get(option);
		if( opValues == null){
			opValues = new ArrayList<String>(){
                private static final long serialVersionUID = 1L;

                @Override
				public String toString() {
					StringBuffer sb = new StringBuffer();
					for( int i = 0; i < this.size(); i++){
						
						if( sb.length() > 0){
							sb.append(" AND ");
						}
						sb.append(get(i));
					}
					return super.toString();
				}
				
			};
			opValues.add(value);
			optionSet.put(option, opValues);
		}else{
			if( !opValues.contains(value)){
				opValues.add(value);
			}
		}
		
		stringValue = null;
		if( !valueList.contains(value)){
			valueList.add(value);
		}
	}
	
	public void addAll(HashMap<String, ArrayList<String>> map){
		String[] options = map.keySet().toArray(new String[map.size()]);
		List<String> list = Arrays.asList(options);
		Collections.sort(list);
		
		for( String option:list){
			ArrayList<String> value = map.get(option);
			
			ArrayList<String> opValues = optionSet.get(option);
			if( opValues == null){
				opValues = new ArrayList<String>();
				opValues.addAll(value);
				optionSet.put(option, opValues);
			}else{
				if( !opValues.contains(value)){
					opValues.addAll(value);
				}
			}
			
			if( !valueList.contains(value)){
				valueList.addAll(value);
			}
		}
		stringValue = null;
	}
	
	@Override
	public String toString() {
		String[] options = optionSet.keySet().toArray(new String[optionSet.size()]);
		List<String> list = Arrays.asList(options);
		Collections.sort(list);
		
		StringBuffer sb = new StringBuffer();
		for( String option:list){
			if( sb.length() > 0){
				sb.append(" AND ");
			}
			
			String tmpStr = "";
			ArrayList<String> opList = optionSet.get(option);
			for( String op : opList){
				if( tmpStr.equals("")){
					tmpStr = op;
				}else{
					tmpStr += " OR " + op;
				}
			}
			sb.append( "(" + tmpStr + ")");
		}
		return sb.toString();
	}
	
	public ArrayList<String> getValueList() {
		return valueList;
	}

	private String getStringValue(){
		if( stringValue == null){
			stringValue = toString();
		}
		
		return stringValue;
	}

	public boolean isInclude(String condition){
		
		if( condition == null || condition.equals("")){
			return true;
		}
		
		String tmpStr = condition.toUpperCase();
		String[] orList = tmpStr.split("OR");
		for( int i = 0; i < orList.length; i++){
			String[] andList = orList[i].trim().split("AND");
			boolean result = true;
			for( int j = 0; j < andList.length; j++){
				String value = andList[j].trim();
				String category = OpUtil.getCategory(value);
				if( category == null || category.equals("") || !optionSet.containsKey(category)){
					continue;
				}
				result &= valueList.contains(value);
				if( !result){
					break;
				}
			}
			
			if( result ){
				return true;
			}
		}
		return false;
	}
	/**
	 * 
	 * @param engine
	 * @param condition	EX) A01E AND B00L AND ...
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public boolean isInclude(ScriptEngine engine, String condition){
		String optionSetStr = getStringValue();
		if( condition == null || condition.equals("")){
			return true;
		}
		
		condition = condition.toUpperCase().trim();
		String tmpStr = condition + " ";
		ArrayList<String> foundOpValueList = new ArrayList<String>();
		Pattern p = Pattern.compile("[a-zA-Z0-9]{5}_STD|[a-zA-Z0-9]{5}_OPT|[a-zA-Z0-9]{4}");
		Matcher m = p.matcher(tmpStr);
		while (m.find()) {
			
			if( !foundOpValueList.contains(m.group().trim())){
			    String str = m.group().trim();
			    str = str.replaceAll("\"","");
				foundOpValueList.add(str);
			}
		}
		
		ArrayList<String> foundOpValueListClone = (ArrayList<String>)foundOpValueList.clone();
		for( int i = foundOpValueListClone.size() - 1; i >= 0 ; i--){
			 String opValue = foundOpValueListClone.get(i);
			 String category = OpUtil.getCategory(opValue);
			 if( category == null || category.equals("")){
				 tmpStr = tmpStr.replaceAll(opValue, "true");
				 continue;
			 }
			 
			 if( !optionSet.containsKey(category)){
				 tmpStr = tmpStr.replaceAll(opValue, "true");
				 continue;
			 }
			 
		}
		
		for( String opValue : foundOpValueList){
			tmpStr = tmpStr.replaceAll(opValue, "#" + opValue);
		}
		
		tmpStr = tmpStr.replaceAll("AND", "&&");
		tmpStr = tmpStr.replaceAll("OR", "||");		
		for( String opValue : foundOpValueList){
			tmpStr = tmpStr.replaceAll("#" + opValue, "('##CONDITION##'.indexOf('" + opValue + "') > -1)");
		}
		
		boolean isEnable = false;
		String defaultStr = tmpStr;
		
		defaultStr = tmpStr;
		defaultStr = defaultStr.replaceAll("##CONDITION##", optionSetStr);
		
		Object obj;
		try {
			obj = engine.eval(defaultStr);
			if( obj instanceof Boolean){
				Boolean b = (Boolean)obj;
				isEnable = b.booleanValue();
			}else{
				throw new Exception("Not available Option : " + condition);
			}
		} catch (Exception e) {
			return false;
		}
		
		return isEnable;
	}
}
