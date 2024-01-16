package com.ssangyong.commands.weight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;

/**
 * [SR170707-024] E-BOM Weight Report 개발 요청
 * @Copyright : Plmsoft
 * @author   : 이정건
 * @since    : 2017. 7. 10.
 * Package ID : com.ssangyong.commands.weight.StoredOptionSet.java
 */
public class StoredOptionSet {
	private String name = null;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private HashMap<String, ArrayList<String>> optionSet = new HashMap();
	private String stringValue = null;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ArrayList<String> valueList = new ArrayList();

	public StoredOptionSet(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public HashMap<String, ArrayList<String>> getOptionSet() {
		return optionSet;
	}

	@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
	public void add(String option, String value) {
		ArrayList<String> opValues = optionSet.get(option);
		if (opValues == null) {
			opValues = new ArrayList() {
				@Override
				public String toString() {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < this.size(); i++) {
						if (sb.length() > 0) {
							sb.append(" AND ");
						}
						sb.append(get(i));
					}
					return super.toString();
				}
			};
			opValues.add(value);
			optionSet.put(option, opValues);
		} else {
			if (!opValues.contains(value)) {
				opValues.add(value);
			}
		}

		stringValue = null;
		if (!valueList.contains(value)) {
			valueList.add(value);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addAll(HashMap<String, ArrayList<String>> map) {
		String[] options = map.keySet().toArray(new String[map.size()]);
		List<String> list = Arrays.asList(options);
		Collections.sort(list);

		for (String option : list) {
			ArrayList<String> value = map.get(option);

			ArrayList<String> opValues = optionSet.get(option);
			if (opValues == null) {
				opValues = new ArrayList();
				opValues.addAll(value);
				optionSet.put(option, opValues);
			} else {
				if (!opValues.contains(value)) {
					opValues.addAll(value);
				}
			}

			if (!valueList.contains(value)) {
				valueList.addAll(value);
			}
		}
		stringValue = null;
	}

	/**
	 * Category 삭제
	 * 
	 * @param sOptionCategory
	 * @author jclee
	 */
	public void removeOptionCategory(String sOptionCategory) {
		ArrayList<String> opValues = optionSet.get(sOptionCategory);
		if (opValues == null || opValues.isEmpty()) {
			return;
		} else {
			for (int inx = 0; inx < opValues.size(); inx++) {
				String value = opValues.get(inx);

				if (valueList.contains(value)) {
					stringValue = null;
					valueList.remove(value);
				}
			}

			optionSet.remove(sOptionCategory);
		}
	}

	/**
	 * Value 삭제
	 * 
	 * @param sOptionValue
	 * @author jclee
	 */
	public void removeOptionValue(String sOptionValue) {
		Set<String> keySet = optionSet.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String sOptionCategory = iterator.next();
			ArrayList<String> alOptionValues = optionSet.get(sOptionCategory);

			if (alOptionValues.contains(sOptionValue)) {
				alOptionValues.remove(sOptionValue);
			}

			if (valueList.contains(sOptionValue)) {
				stringValue = null;
				valueList.remove(sOptionValue);
			}
		}
	}

	/**
	 * Option Value 삭제
	 * @param sOptionCategory
	 * @param sOptionValue
	 * @author jclee
	 */
	public void removeOptionValue(String sOptionCategory, String sOptionValue) {
		ArrayList<String> opValues = optionSet.get(sOptionCategory);
		if (opValues.contains(sOptionValue)) {
			opValues.remove(sOptionValue);
		}
		
		if (valueList.contains(sOptionValue)) {
			stringValue = null;
			valueList.remove(sOptionValue);
		}
	}
	
	/**
	 * <pre>
	 * Option Value 교체
	 * 1. Source Option Value 제거
	 * 2. Target Option Value 추가
	 *  - Source가 없을 경우 Target 신규 생성
	 * </pre>
	 * @param sOptionCategory
	 * @param sOptionValue
	 * @author jclee
	 */
	public void replaceOptionValue(String sOptionCategory, String sSourceOptionValue, String sTargetOptionValue) {
		removeOptionValue(sOptionCategory, sSourceOptionValue);
		add(sOptionCategory, sTargetOptionValue);
	}
	
	/**
	 * <pre>
	 * Option Value 교체
	 * 1. 대상 Option Value만 남기고 모든 Value 제거
	 * </pre>
	 * @param sOptionCategory
	 * @param sOptionValue
	 * @author jclee
	 */
	public void replaceOptionValue(String sOptionCategory, String sOptionValue) {
		removeOptionCategory(sOptionCategory);
		add(sOptionCategory, sOptionValue);
	}

	@Override
	public String toString() {
		String[] options = optionSet.keySet().toArray(new String[optionSet.size()]);
		List<String> list = Arrays.asList(options);
		Collections.sort(list);

		StringBuffer sb = new StringBuffer();
		for (String option : list) {
			if (sb.length() > 0) {
				sb.append(" AND ");
			}

			String tmpStr = "";
			ArrayList<String> opList = optionSet.get(option);
			for (String op : opList) {
				if (tmpStr.equals("")) {
					tmpStr = op;
				} else {
					tmpStr += " OR " + op;
				}
			}
			sb.append("(" + tmpStr + ")");
		}
		return sb.toString();
	}

	public ArrayList<String> getValueList() {
		return valueList;
	}

	private String getStringValue() {
		if (stringValue == null) {
			stringValue = toString();
		}

		return stringValue;
	}

	public boolean isInclude(String condition) {

		if (condition == null || condition.equals("")) {
			return true;
		}

		String tmpStr = condition.toUpperCase();
		String[] orList = tmpStr.split("OR");
		for (int i = 0; i < orList.length; i++) {
			String[] andList = orList[i].trim().split("AND");
			boolean result = true;
			for (int j = 0; j < andList.length; j++) {
				String value = andList[j].trim();
				String category = BomUtil.getCategory(value);
				if (category == null || category.equals("") || !optionSet.containsKey(category)) {
					continue;
				}
				result &= valueList.contains(value);
				if (!result) {
					break;
				}
			}

			if (result) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param engine
	 * @param condition
	 *            컨디션 값 EX) A01E AND B00L AND ...
	 * @param optionSetStr
	 *            옵션 셋 EX) A01E AND B00L AND .....
	 * @return
	 */
	public boolean isInclude(ScriptEngine engine, String condition) {
		String optionSetStr = getStringValue();
		if (condition == null || condition.equals("")) {
			return true;
		}

		condition = condition.toUpperCase().trim();
		String tmpStr = condition + " ";
		// Pattern이 맞는 옵셥값을 찾고, 앞에 #을 붙인다.
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayList<String> foundOpValueList = new ArrayList();
		Pattern p = Pattern.compile("[a-zA-Z0-9]{5}_STD|[a-zA-Z0-9]{5}_OPT|[a-zA-Z0-9]{4}");
		Matcher m = p.matcher(tmpStr);
		while (m.find()) {

			if (!foundOpValueList.contains(m.group().trim())) {
				String str = m.group().trim();
				str = str.replaceAll("\"", "");
				foundOpValueList.add(str);
			}
		}

		@SuppressWarnings("unchecked")
		ArrayList<String> foundOpValueListClone = (ArrayList<String>) foundOpValueList.clone();
		for (int i = foundOpValueListClone.size() - 1; i >= 0; i--) {
			String opValue = foundOpValueListClone.get(i);
			String category = BomUtil.getCategory(opValue);
			if (category == null || category.equals("")) {
				tmpStr = tmpStr.replaceAll(opValue, "true");
				continue;
			}

			if (!optionSet.containsKey(category)) {
				tmpStr = tmpStr.replaceAll(opValue, "true");
				continue;
			}

		}

		for (String opValue : foundOpValueList) {
			tmpStr = tmpStr.replaceAll(opValue, "#" + opValue);
		}

		tmpStr = tmpStr.replaceAll("AND", "&&");
		tmpStr = tmpStr.replaceAll("OR", "||");
		for (String opValue : foundOpValueList) {
			tmpStr = tmpStr.replaceAll("#" + opValue, "('##CONDITION##'.indexOf('" + opValue + "') > -1)");
		}

		boolean isEnable = false;
		String defaultStr = tmpStr;

		defaultStr = tmpStr;
		defaultStr = defaultStr.replaceAll("##CONDITION##", optionSetStr);

		Object obj;
		try {
			obj = engine.eval(defaultStr);
			if (obj instanceof Boolean) {
				Boolean b = (Boolean) obj;
				isEnable = b.booleanValue();
			} else {
				throw new Exception("Not available Option : " + condition);
			}
		} catch (Exception e) {
			return false;
		}

		// String[] subConditions = optionSetStr.split("OR");
		// for( String subCondition : subConditions){
		// defaultStr = tmpStr;
		// defaultStr = defaultStr.replaceAll("##CONDITION##", subCondition);
		//
		// Object obj;
		// try {
		// obj = engine.eval(defaultStr);
		// if( obj instanceof Boolean){
		// Boolean b = (Boolean)obj;
		// isEnable = b.booleanValue();
		// break;
		// }else{
		// throw new Exception("Not available Option : " + condition);
		// }
		// } catch (Exception e) {
		// return false;
		// }
		//
		// }

		return isEnable;
	}
}
