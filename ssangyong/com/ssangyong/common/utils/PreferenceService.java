package com.ssangyong.common.utils;

import java.util.Vector;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;

public class PreferenceService {
	private static PreferenceService service;
	private static TCPreferenceService preferenceService = ((TCSession) AIFUtility.getDefaultSession()).getPreferenceService();

	public static void createService(TCSession session) {
		if (service == null) {
			service = new PreferenceService(session);
		}
	}

	private PreferenceService(TCSession session) {
		preferenceService = session.getPreferenceService();
	}

	public static String getValue(String name) {
		return getValue(TCPreferenceService.TC_preference_site, getPreferenceName(name));
	}

	public static String getValue(int scope, String name) {
		// String value = preferenceService.getString(scope, name);
		String value = preferenceService.getStringValueAtLocation(name, TCPreferenceLocation.convertLocationFromLegacy(scope));
		if (value == null) {
			return null;
		}

		return parseValue(value);
	}
	
	public static void setStringValue(String name, String value) throws TCException {
		preferenceService.setStringValue(name, value);

	}

	public static String[] getValues(String name) {
		return getValues(TCPreferenceService.TC_preference_site, getPreferenceName(name));
	}

	public static String[] getValues(int scope, String name) {
		// String[] strings = preferenceService.getStringArray(scope, name);
		String[] strings = preferenceService.getStringValuesAtLocation(name, TCPreferenceLocation.convertLocationFromLegacy(scope));
		if (strings == null) {
			return null;
		}

		String[] values = new String[strings.length];
		for (int i = 0; i < strings.length; i++) {
			values[i] = parseValue(strings[i]);
		}

		return values;
	}

	public static String getValue(String name, int index) {
		return getValue(TCPreferenceService.TC_preference_site, getPreferenceName(name), index);
	}

	public static String getValue(int scope, String name, int index) {
		// String[] strings = preferenceService.getStringArray(scope, name);
		String[] strings = preferenceService.getStringValuesAtLocation(name, TCPreferenceLocation.convertLocationFromLegacy(scope));
		if (strings == null) {
			return null;
		}

		return parseValue(strings[index]);
	}

	public static String getDisplayValue(String name) {
		return getDisplayValue(TCPreferenceService.TC_preference_site, getPreferenceName(name));
	}

	public static String getDisplayValue(int scope, String name) {
		// String value = preferenceService.getString(scope, name);
		String value = preferenceService.getStringValueAtLocation(name, TCPreferenceLocation.convertLocationFromLegacy(scope));
		if (value == null) {
			return null;
		}

		return parseDisplayValue(value);
	}

	public static String[] getDisplayValues(String name) {
		return getDisplayValues(TCPreferenceService.TC_preference_site, getPreferenceName(name));
	}

	public static String[] getDisplayValues(int scope, String name) {
		// String[] strings = preferenceService.getStringArray(scope, name);
		String[] strings = preferenceService.getStringValuesAtLocation(name, TCPreferenceLocation.convertLocationFromLegacy(scope));
		if (strings == null) {
			return null;
		}

		String[] values = new String[strings.length];
		for (int i = 0; i < strings.length; i++) {
			values[i] = parseDisplayValue(strings[i]);
		}

		return values;
	}

	public static String getDisplayValues(String name, int index) {
		return getDisplayValues(TCPreferenceService.TC_preference_site, getPreferenceName(name), index);
	}

	public static String getDisplayValues(int scope, String name, int index) {
		// String[] strings = preferenceService.getStringArray(scope, name);
		String[] strings = preferenceService.getStringValuesAtLocation(name, TCPreferenceLocation.convertLocationFromLegacy(scope));
		if (strings == null) {
			return null;
		}

		return parseDisplayValue(strings[index]);
	}

	public static String[][] getValueAndDisplayValues(String name) {
		return getValueAndDisplayValues(TCPreferenceService.TC_preference_site, getPreferenceName(name));
	}

	public static String[][] getValueAndDisplayValues(int scope, String name) {
		// String[] strings = preferenceService.getStringArray(scope, name);
		String[] strings = preferenceService.getStringValuesAtLocation(name, TCPreferenceLocation.convertLocationFromLegacy(scope));
		if (strings == null) {
			return null;
		}

		// String[] values = new String[strings.length];
		// String[] displayValues = new String[strings.length];
		String[][] valueAndDisplayValues = new String[strings.length][2];
		for (int i = 0; i < strings.length; i++) {
			valueAndDisplayValues[i][0] = parseValue(strings[i]);
			valueAndDisplayValues[i][1] = parseDisplayValue(strings[i]);
		}

		return valueAndDisplayValues;
	}

	public static String getPreferenceName(String preference) {
		if (preference.substring(preference.length() - 1).equals("+")) {
			return preference.substring(0, preference.length() - 1);
		} else {
			return preference;
		}
	}

	public static Vector<String[]> findPreference(String name, String value, String displayValue) {
		return findPreference(TCPreferenceService.TC_preference_site, getPreferenceName(name), value, displayValue);
	}

	public static Vector<String[]> findPreference(int scope, String name, String value, String displayValue) {
		Vector<String[]> vector = new Vector<String[]>();
		String[][] valueAndDisplayValues = getValueAndDisplayValues(name);
		value = value.replace("*", "").trim();
		displayValue = displayValue.replace("*", "").trim();
		for (int i = 0; i < valueAndDisplayValues.length; i++) {
			String val = valueAndDisplayValues[i][0];
			String dval = valueAndDisplayValues[i][1];
			if (!value.equals("") && !displayValue.equals("")) {
				if (val.contains(value) && dval.contains(displayValue)) {
					vector.addElement(valueAndDisplayValues[i]);
				}
			} else if (!value.equals("")) {
				if (val.contains(value)) {
					vector.addElement(valueAndDisplayValues[i]);
				}
			} else if (!displayValue.equals("")) {
				if (dval.contains(displayValue)) {
					vector.addElement(valueAndDisplayValues[i]);
				}
			}
		}

		return vector;
	}

	public static String[] findPreferenceValue(int scope, String name, String value) {
		Vector<String> vector = new Vector<String>();
		String[] values = getValues(scope, getPreferenceName(name));
		value = value.replace("*", "");
		for (int i = 0; i < values.length; i++) {
			if (values[i].indexOf(value) >= 0) {
				vector.addElement(values[i]);
			}
		}

		return (String[]) vector.toArray(new String[vector.size()]);
	}

	public static String[] findPreferenceDisplayValue(int scope, String name, String value) {
		Vector<String> vector = new Vector<String>();
		String[] values = getDisplayValues(scope, getPreferenceName(name));
		value = value.replace("*", "");
		for (int i = 0; i < values.length; i++) {
			if (values[i].indexOf(value) >= 0) {
				vector.addElement(values[i]);
			}
		}

		return (String[]) vector.toArray(new String[vector.size()]);
	}

	public static int getIndex(String name, String value) {
		return getIndex(TCPreferenceService.TC_preference_site, getPreferenceName(name), value);
	}

	public static int getIndex(int scope, String name, String value) {
		String[] strings = getValues(scope, name);
		for (int i = 0; i < strings.length; i++) {
			if (strings[i].equals(value)) {
				return i;
			}
		}

		return -1;
	}

	private static String parseValue(String string) {
		int i = string.indexOf(":");
		if (i == -1) {
			return string;
		} else {
			return string.substring(0, i);
		}
	}

	private static String parseDisplayValue(String string) {
		int i = string.indexOf(":");
		if (i == -1) {
			return new String();
		} else {
			return string.substring(i + 1);
		}
	}

	public static String getDisplayValue(int scope, String preferenceName, String value) {
		String[][] values = PreferenceService.getValueAndDisplayValues(scope, preferenceName);
		for (int i = 0; i < values.length; i++) {
			if (values[i][0].equals(value)) {
				return values[i][1];
			}
		}

		return "";
	}

	public static String getValue(int scope, String preferenceName, String displayValue) {
		String[][] values = PreferenceService.getValueAndDisplayValues(scope, preferenceName);
		for (int i = 0; i < values.length; i++) {
			if (values[i][1].equals(displayValue)) {
				return values[i][0];
			}
		}

		return "";
	}

	public static boolean getLogicalValueAtLocation(String paramString, TCPreferenceLocation paramTCPreferenceLocation) {
		Boolean flag = preferenceService.getLogicalValueAtLocation(paramString, paramTCPreferenceLocation);
		if (flag == null) {
			return false;
		}

		return flag;
	}

}
