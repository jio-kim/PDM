package com.kgm.common.utils;

import java.util.StringTokenizer;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

import com.teamcenter.rac.kernel.TCDateEncoder;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.DateButton;

@SuppressWarnings({"rawtypes"})
public class PropertyService {
	private static PropertyService service;

	public static void createService(TCSession session) {
		if (service == null) {
			service = new PropertyService(session);
		}
	}

	private PropertyService(TCSession session) {
	}

	public static void setStringToPropertyValue(TCProperty property, Object object) throws TCException {
		String string = new String();
		if (object instanceof JTextComponent) {
			string = ((JTextComponent) object).getText().trim();
		} else if (object instanceof JComboBox) {
			string = ((JComboBox) object).getSelectedItem().toString().trim();
		} else if (object instanceof JList) {
			StringBuilder stringBuilder = new StringBuilder();
			int count = ((JList) object).getModel().getSize();
			for (int i = 0; i < count; i++) {
				String value = (String) ((JList) object).getModel().getElementAt(i);
				stringBuilder.append(value);
				if (i != count - 1) {
					stringBuilder.append("|");
				}
			}
			string = stringBuilder.toString();
		} else if (object instanceof DateButton) {
			java.util.Date date = ((DateButton) object).getDate();
			if (date == null)
				property.setNullVerdict(true);
			else
				property.setDateValueData(date);
			return;
		} else if (object instanceof JCheckBox) {
			string = ((JCheckBox) object).isSelected() ? "Y" : "N";
		}
		// if (string.equals("")) {
		// return;
		// }
		setStringToPropertyValue(property, string);
	}

	public static void setStringToPropertyValue(TCProperty property, String string) throws TCException {
		int i = property.getPropertyType();
		switch (i) {
		case TCProperty.PROP_char:
			if (string.length() <= 0)
				break;
			property.setCharValueData(string.charAt(0));
			break;
		case TCProperty.PROP_date:
			property.setDateValueData(TCDateEncoder.getDate(string));
			break;
		case TCProperty.PROP_double:
			if (string.length() <= 0)
				break;
			property.setDoubleValueData(Double.parseDouble(string));
			break;
		case TCProperty.PROP_float:
			if (string.length() <= 0)
				break;
			//property.setFloatValueData(Float.parseFloat(string));
			property.setFloatValueData(Double.parseDouble(string));
			break;
		case TCProperty.PROP_int:
			if (string.length() <= 0)
				break;
			property.setIntValueData(Integer.parseInt(string));
			break;
		case TCProperty.PROP_logical:
			Boolean boolean1 = new Boolean(string);
			property.setLogicalValueData(boolean1.booleanValue());
			break;
		case TCProperty.PROP_short:
			Short short1 = new Short(string);
			property.setShortValueData(short1.shortValue());
			break;
		case TCProperty.PROP_string:
			if (property.getPropertyDescriptor().isArray()) {
				StringTokenizer tokenizer = new StringTokenizer(string, "|");
				String[] strings = new String[tokenizer.countTokens()];
				int j = 0;
				while (tokenizer.hasMoreElements()) {
					strings[j++] = (String) tokenizer.nextElement();
				}
				property.setStringValueArrayData(strings);
			} else {
				property.setStringValueData(string);
			}
			break;
		case TCProperty.PROP_note:
			property.setNoteValueData(string);
			break;
		default:
			break;
		}
	}

	public static String getStringValue(TCProperty property) throws TCException {
		int i = property.getPropertyType();
		switch (i) {
		case TCProperty.PROP_char:
			return String.valueOf(property.getCharValue());
		case TCProperty.PROP_date:
			return String.valueOf(property.getDateValue());
		case TCProperty.PROP_double:
			return String.valueOf(property.getDoubleValue());
		case TCProperty.PROP_float:
			//return String.valueOf(property.getFloatValue());
			return String.valueOf((float)property.getFloatValueAsDouble());
		case TCProperty.PROP_int:
			return String.valueOf(property.getIntValue());
		case TCProperty.PROP_logical:
			return String.valueOf(property.getLogicalValue());
		case TCProperty.PROP_short:
			return String.valueOf(property.getShortValue());
		case TCProperty.PROP_string:
			if (property.getPropertyDescriptor().isArray()) {
				String[] strings = property.getStringValueArray();
				StringBuilder stringBuilder = new StringBuilder();
				for (int j = 0; j < strings.length; j++) {
					stringBuilder.append(strings[j]);
					if (j != strings.length - 1) {
						stringBuilder.append("|");
					}
				}
				return stringBuilder.toString();
			} else {
				return String.valueOf(property.getStringValue());
			}
		case TCProperty.PROP_note:
			return String.valueOf(property.getNoteValue());
		default:
			return new String();
		}
	}
}
