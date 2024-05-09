package com.kgm.common.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.sql.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

import com.kgm.common.FunctionField;
import com.kgm.common.OnlyDateButton;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.DateButton;
import com.teamcenter.rac.util.FilterDocument;
import com.teamcenter.rac.util.Painter;
import com.teamcenter.rac.util.iTextField;

@SuppressWarnings({"unused", "rawtypes", "unchecked", "static-access", "serial"})
public class ComponentService {
	private static ComponentService service;
	private static TCSession session;

	public static void createService(TCSession session) {
		if (service == null) {
			service =  new ComponentService(session);
		}
	}

	private ComponentService(TCSession session) {
		this.session = session;
	}

	public static void setDateButton(Vector componentVector) {
		for (int i = 0; i < componentVector.size(); i++) {
			if (componentVector.elementAt(i) instanceof DateButton) {
				((DateButton)componentVector.elementAt(i)).setDate((java.util.Date)null);
				((DateButton)componentVector.elementAt(i)).setDisplayFormat("yyyy-MM-dd a HH mm");
			}
		}
	}

	public static void setComponentSize(JPanel panel, int width, int height) {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component component = panel.getComponent(i);
			if (component instanceof JPanel) {
				setComponentSize((JPanel)component, width, height);
			} else if (component instanceof JTextField) {
				((JTextField)component).setPreferredSize(new Dimension(width, height));
			} else if (component instanceof FunctionField) {
				((FunctionField)component).setPreferredSize(new Dimension(width, height));
			} else if (component instanceof JComboBox) {
				((JComboBox)component).setPreferredSize(new Dimension(width, height));
			} else if (component instanceof JList) {
				((JList)component).setPreferredSize(new Dimension(width, height + 50));
			} else if (component instanceof DateButton) {
				((DateButton)component).setPreferredSize(new Dimension(width, height));
			} else if (component instanceof JScrollPane) {
				if (((JScrollPane)component).getViewport().getComponentCount() > 0) {
					Component scrollComponent = ((JScrollPane)component).getViewport().getComponent(0);
					if (scrollComponent instanceof JTextArea) {
						((JScrollPane)component).setPreferredSize(new Dimension(width, height + 50));
					}
				}
			}
		}
	}
	
	public static void setLabelSize(JPanel panel, int width, int height) {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component component = panel.getComponent(i);
			if (component instanceof JPanel) {
				setLabelSize((JPanel)component, width, height);
			} else if (component instanceof JLabel) {
				((JLabel)component).setPreferredSize(new Dimension(width, height));
			}
		}
		panel.updateUI();
	}
	
	public static void setTextfieldSizs(JPanel panel, int width, int height) {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component component = panel.getComponent(i);
			if (component instanceof JPanel) {
				setTextfieldSizs((JPanel)component, width, height);
			} else if (component instanceof JTextField) {
				((JTextField)component).setPreferredSize(new Dimension(width, height));
			} else if (component instanceof FunctionField) {
				((FunctionField)component).setPreferredSize(new Dimension(width, height));
			} else if (component instanceof iTextField) {
				((iTextField)component).setPreferredSize(new Dimension(width, height));
			}
			
		}
		panel.updateUI();
	}
	
	public static void setComboboxSize(JPanel panel, int width, int height) {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component component = panel.getComponent(i);
			if (component instanceof JPanel) {
				setComboboxSize((JPanel)component, width, height);
			} else if (component instanceof JComboBox) {
				((JComboBox)component).setPreferredSize(new Dimension(width, height));
			} else if (component instanceof OnlyDateButton) {
				((OnlyDateButton)component).setPreferredSize(new Dimension(width, height));
			} else if (component instanceof DateButton) {
				((DateButton)component).setPreferredSize(new Dimension(width, height));
			}
		}
		panel.updateUI();
	}

	public static void setPanelEnable(JPanel panel, boolean enable) {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component component = panel.getComponent(i);
			if (component instanceof JPanel) {
				setPanelEnable((JPanel)component, enable);
			}
			component.setEnabled(false);
		}
	}

	public static JComponent createComponent(String[] fieldInformation, TCPropertyDescriptor descriptor) {
		JComponent component;
		String className = fieldInformation[0];
		boolean mandatory = Boolean.valueOf(fieldInformation[1]).booleanValue();
		boolean enable = Boolean.valueOf(fieldInformation[2]).booleanValue();
		if (mandatory) {
			component = createMandatoryComponent(className);
		} else {
			component = createComponent(className);
		}
		component.setEnabled(enable);
		if (className.equals("JTextField") || className.equals("JTextArea")) {
			JTextComponent textComponent = (JTextComponent)component;
			if (descriptor != null) {
				if (descriptor.getType() != TCProperty.PROP_double && descriptor.getType() != TCProperty.PROP_float && descriptor.getType() != TCProperty.PROP_int) {
					textComponent.setDocument(new FilterDocument(descriptor.getMaxStringLength(), TCSession.getServerEncodingName(session)));
				} else {
					FilterDocument filterDocument = new FilterDocument(100, TCSession.getServerEncodingName(session));
					filterDocument.setAcceptedChars(FilterDocument.FLOAT);
					textComponent.setDocument(filterDocument);
				}
			}
			String defaultValue = fieldInformation[3];
			if (!defaultValue.equals("None")) {
				((JTextComponent)component).setText(defaultValue);
			}
			if (className.equals("JTextArea")) {
				((JTextArea)component).setLineWrap(true);
			}
		}
//		else if (className.equals("JComboBox")) {
//			String preference = fieldInformation[3];
//			JComboBox comboBox = (JComboBox)component;
//			if (!preference.equals("None")) {
//				if (!preference.substring(preference.length() - 1).equals("+"))
//					comboBox.setModel(new DefaultComboBoxModel(PreferenceService.getValues(preference)));
//				else
//					comboBox.setModel(new DefaultComboBoxModel(PreferenceService.getDisplayValues(PreferenceService.getPreferenceName(preference))));
//				comboBox.insertItemAt(new String(), 0);
//				comboBox.setSelectedIndex(0);
//				if (fieldInformation.length == 5) {
//					String defaultValue = fieldInformation[4];
//					int i = PreferenceService.getIndex(PreferenceService.getPreferenceName(preference), defaultValue);
//					comboBox.setSelectedIndex(i + 1);
//				}
//			}
//		} 
	else if (className.equals("JList")) {
			String preference = fieldInformation[3];
			JList list = (JList)component;
			if (!preference.substring(preference.length() - 1).equals("+"))
				list.setModel(new DefaultComboBoxModel(PreferenceService.getValues(preference)));
			else
				list.setModel(new DefaultComboBoxModel(PreferenceService.getDisplayValues(preference)));
			list.setVisibleRowCount(5);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if (className.equals("DateButton")) {
			DateButton dateButton = (DateButton)component;
			dateButton.setDate((java.util.Date)null);
			dateButton.setDisplayFormat("yyyy-MM-dd a HH mm");
			dateButton.setDate(new java.util.Date());
		}
		return component;
	}

	public static JComponent createComponent(String[] fieldInformation, TCProperty property) {
		JComponent component;
		String className = fieldInformation[0];
		boolean mandatory = Boolean.valueOf(fieldInformation[1]).booleanValue();
		boolean enable = Boolean.valueOf(fieldInformation[2]).booleanValue();
		String preference = fieldInformation[3];
		String defaultValue = null;
		if (fieldInformation.length == 5)
			defaultValue = fieldInformation[4];
		if (mandatory)
			component = createMandatoryComponent(className);
		else
			component = createComponent(className);
		component.setEnabled(enable);
		if (className.equals("JTextField") || className.equals("JTextArea")) {
			JTextComponent textComponent = (JTextComponent)component;
			if (property.getPropertyType() == TCProperty.PROP_float) {
				FilterDocument filterDocument = new FilterDocument(100, TCSession.getServerEncodingName(session));
				filterDocument.setAcceptedChars(FilterDocument.FLOAT);
				textComponent.setDocument(filterDocument);
				//textComponent.setText(String.valueOf(property.getFloatValue()));
				textComponent.setText(String.valueOf((float)property.getFloatValueAsDouble()));		
			} else if (property.getPropertyType() == TCProperty.PROP_double) {
				FilterDocument filterDocument = new FilterDocument(100, TCSession.getServerEncodingName(session));
				filterDocument.setAcceptedChars(FilterDocument.FLOAT);
				textComponent.setDocument(filterDocument);
				textComponent.setText(String.valueOf(property.getDoubleValue()));
			} else if (property.getPropertyType() == TCProperty.PROP_int) {
				FilterDocument filterDocument = new FilterDocument(100, TCSession.getServerEncodingName(session));
				filterDocument.setAcceptedChars(FilterDocument.INTEGER_NUMBER);
				textComponent.setDocument(filterDocument);
				textComponent.setText(String.valueOf(property.getIntValue()));
			} else {
				int i = property.getPropertyDescriptor().getMaxStringLength();
				if (i > 0) {
					textComponent.setDocument(new FilterDocument(property.getPropertyDescriptor().getMaxStringLength(), TCSession.getServerEncodingName(session)));
				} else {
					textComponent.setDocument(new FilterDocument(1024 * 1024, TCSession.getServerEncodingName(session)));
				}
				String string = property.getStringValue();
				if (string.equals("") && defaultValue != null) {
					textComponent.setText(defaultValue);
				} else {
					textComponent.setText(property.getStringValue());
				}
			}
		} else if (className.equals("JComboBox")) {
			JComboBox comboBox = (JComboBox)component;
			if (!preference.equals("None")) {
				if (!preference.substring(preference.length() - 1).equals("+"))
					comboBox.setModel(new DefaultComboBoxModel(PreferenceService.getValues(preference)));
				else
					comboBox.setModel(new DefaultComboBoxModel(PreferenceService.getDisplayValues(PreferenceService.getPreferenceName(preference))));
				comboBox.insertItemAt(new String(), 0);
				String string = property.getStringValue();
				if (!string.equals("")) {
					int i = PreferenceService.getIndex(PreferenceService.getPreferenceName(preference), string);
					comboBox.setSelectedIndex(i + 1);
				} else if (defaultValue != null) {
					int i = PreferenceService.getIndex(PreferenceService.getPreferenceName(preference), defaultValue);
					comboBox.setSelectedIndex(i + 1);
				} else {
					comboBox.setSelectedIndex(0);
				}
			}
		} else if (className.equals("JList")) {
			JList list = (JList)component;
			list.setVisibleRowCount(5);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			String[] strings = property.getStringValueArray();
			list.setModel(new DefaultComboBoxModel(strings));
		} else if (className.equals("DateButton")) {
			DateButton dateButton = (DateButton)component;
			dateButton.setDisplayFormat("yyyy-MM-dd a HH mm");
			dateButton.setDate(property.getDateValue());
		}
		return component;
	}

	public static JComponent createMandatoryComponent(String className) {
		if (className.equals("JTextField")) {
			JTextField textField = new JTextField() {
				public void paint(Graphics g) {
					super.paint(g);
					Painter.paintIsRequired(this, g);
				}
			};
			textField.setHorizontalAlignment(JTextField.LEFT);
			return textField;
		} else if (className.equals("JComboBox")) {
			return new JComboBox() {
				public void paint(Graphics g) {
					super.paint(g);
					Painter.paintIsRequired(this, g);
				}
			};
		} else if (className.equals("JList")) {
			return new JList() {
				public void paint(Graphics g) {
					super.paint(g);
					Painter.paintIsRequired(this, g);
				}
			};
		} else if (className.equals("JTextArea")) {
			return new JTextArea() {
				public void paint(Graphics g) {
					super.paint(g);
					Painter.paintIsRequired(this, g);
				}
			};
		} else if (className.equals("DateButton")) {
			DateButton dateButton = new DateButton() {
				public void paint(Graphics g) {
					super.paint(g);
					Painter.paintIsRequired(this, g);
				}
			};
			return dateButton;
		} else if (className.equals("JCheckBox")) {
			return new JCheckBox() {
				public void paint(Graphics g) {
					super.paint(g);
					Painter.paintIsRequired(this, g);
				}
			};
		} else
			return null;
	}

	public static JComponent createComponent(String className) {
		if (className.equals("JTextField")) {
			JTextField textField = new JTextField();
			textField.setHorizontalAlignment(JTextField.LEFT);
			return textField;
		} else if (className.equals("JComboBox")) {
			JComboBox comboBox = new JComboBox();
			return comboBox;
		} else if (className.equals("JList")) {
			return new JList();
		} else if (className.equals("JTextArea")) {
			return new JTextArea();
		} else if (className.equals("DateButton")) {
			return new DateButton();
		} else if (className.equals("JCheckBox")) {
			return new JCheckBox();
		} else {
			return null;
		}
	}

	public static void setComponentValueByType(TCProperty property, Object object, String[] fieldInformation) throws TCException {
		if (object instanceof JTextField)
			((JTextField)object).setText(getStringValueByType(property));
		else if (object instanceof JTextArea)
			((JTextArea)object).setText(getStringValueByType(property));
		else if (object instanceof JComboBox) {
			boolean mandatory = Boolean.valueOf(fieldInformation[1]).booleanValue();
			if (!mandatory && property.getStringValue() == "") {
				((JComboBox)object).setSelectedIndex(0);
			} else {
				String[] strings = PreferenceService.getValues(PreferenceService.getPreferenceName(fieldInformation[3]));
				for (int i = 0; i < strings.length; i++) {
					if (property.getUIFValue().equals(strings[i]))
						((JComboBox)object).setSelectedIndex(i + (mandatory ? 0 : 1));
				}
			}
		} else if (object instanceof JList) {
			if (property.getStringValue() == null)
				return;
			StringTokenizer tokenizer = new StringTokenizer(property.getStringValue(), ",");
			Vector vector = new Vector();
			while (tokenizer.hasMoreElements()) {
				vector.addElement(tokenizer.nextElement());
			}
			String[] strings = PreferenceService.getValues(PreferenceService.getPreferenceName((fieldInformation[3])));
			for (int i = 0; i < strings.length; i++) {
				if (vector.contains(strings[i]))
					((JList)object).addSelectionInterval(i, i);
			}
		} else if (object instanceof DateButton) {
			java.util.Date date = property.getDateValue();
			if (date != null)
				if (date.getTime() > 0)
					((DateButton)object).setDate(property.getDateValue());
				else
					((DateButton)object).setDate((java.util.Date)null);
			else
				((DateButton)object).setDate((java.util.Date)null);
			((DateButton)object).setMandatory(true);
		} else if (object instanceof JCheckBox)
			((JCheckBox)object).setSelected(property.getLogicalValue());
	}

	public static void setComponentByValue(Component component, String value, String[] fieldInformation) throws TCException {
		if (component instanceof JTextField) {
			((JTextField)component).setText(value);
		} else if (component instanceof JComboBox) {
			if (value == null || value.equals("")) {
				((JComboBox)component).setSelectedIndex(0);
			} else {
				String preference = fieldInformation[3];
				int i = PreferenceService.getIndex(PreferenceService.getPreferenceName(preference), value);
				((JComboBox)component).setSelectedIndex(i + 1);
			}
		} else if (component instanceof DateButton) {
			if (value == null || value.equals("")) {
				((DateButton)component).setDate((java.util.Date)null);
			} else {
				((DateButton)component).setDate(Date.valueOf(value));
			}
		}
	}

	private static String getStringValueByType(TCProperty property) throws TCException {
		String string = "";
		if (property.getPropertyType() == TCProperty.PROP_string)
			string = property.getStringValue();
		else if (property.getPropertyType() == TCProperty.PROP_int)
			string = Integer.toString(property.getIntValue());
		else if (property.getPropertyType() == TCProperty.PROP_double) {
			string = Double.toString(property.getDoubleValue());
			if (string.equals("0.0"))
				string = "";
		} else if (property.getPropertyType() == TCProperty.PROP_typed_reference)
			string = property.getReferenceValue().toString();
		return string;
	}

	public static String getValue(Component component) {
		if (component instanceof JTextField)
			return ((JTextField)component).getText().trim();
		else if (component instanceof JComboBox)
			return (String)((JComboBox)component).getSelectedItem();
		else if (component instanceof JList)
			return (String)((JList)component).getSelectedValue();
		else if (component instanceof JTextArea)
			return ((JTextArea)component).getText().trim();
		else if (component instanceof DateButton)
			return ((DateButton)component).getDateString();
		else
			return new String();
	}

	public static boolean checkMandatoryField(Component component) {
		String value = new String();
		if (component instanceof JTextField || component instanceof JTextArea) {
			value = ((JTextComponent)component).getText().trim();
		} else if (component instanceof JComboBox) {
			value = (String)((JComboBox)component).getSelectedItem();
		} else if (component instanceof JList) {
			value = (String)((JList)component).getSelectedValue();
		} else if (component instanceof DateButton) {
			value = ((DateButton)component).getDateString();
		}
		if (value == null || value.trim().length() == 0) {
			component.requestFocus();
			return false;
		}
		return true;
	}

	public static boolean equal(TCProperty property, Component component, String[] fieldInformation) throws TCException {
		String string = new String();
		if (component instanceof JTextComponent) {
			string = ((JTextComponent)component).getText().trim();
			int i = property.getPropertyType();
			if (i == TCProperty.PROP_string) {
				return property.getStringValue().equals(string);
			} else if (i == TCProperty.PROP_int) {
				return String.valueOf(property.getIntValue()).equals(string);
			}
		} else if (component instanceof JComboBox) {
			int i = ((JComboBox)component).getSelectedIndex();
			String preference = fieldInformation[3];
			int j = PreferenceService.getIndex(PreferenceService.getPreferenceName(preference), property.getStringValue());
			return (i - 1 == j);
		} else if (component instanceof JList) {
			String[] strings = property.getStringValueArray();
			int count = ((JList)component).getModel().getSize();
			if (strings.length != count)
				return false;
			for (int i = 0; i < count; i++) {
				String value = (String)((JList)component).getModel().getElementAt(i);
				if (strings[i].equals(value)) {
					return false;
				}
			}
			return true;
		} else if (component instanceof DateButton) {
			java.util.Date componentDate = ((DateButton)component).getDate();
			java.util.Date propertyDate = property.getDateValue();
			if (componentDate == null && propertyDate == null) {
				return true;
			} else if (componentDate == null && propertyDate != null) {
				return false;
			} else if (componentDate != null && propertyDate == null) {
				return false;
			} else {
				return propertyDate.equals(componentDate);
			}
		} else if (component instanceof JCheckBox) {
			string = ((JCheckBox)component).isSelected() ? "Y" : "N";
		}
		return true;
	}

	public static String getStringValue(Component component, String[] fieldInformation) throws TCException {
		String string = new String();
		if (component instanceof JTextComponent) {
			return ((JTextComponent)component).getText().trim();
		} else if (component instanceof JComboBox) {
			int i = ((JComboBox)component).getSelectedIndex();
			if (i == 0) {
				return new String();
			}
			String preference = fieldInformation[3];
			return PreferenceService.getValue(PreferenceService.getPreferenceName(preference), i - 1);
		} else if (component instanceof JList) {
			StringBuilder stringBuilder = new StringBuilder();
			int count = ((JList)component).getModel().getSize();
			for (int i = 0; i < count; i++) {
				String value = (String)((JList)component).getModel().getElementAt(i);
				stringBuilder.append(value);
				if (i != count - 1) {
					stringBuilder.append("|");
				}
			}
			return stringBuilder.toString();
		} else if (component instanceof DateButton) {
			java.util.Date date = ((DateButton)component).getDate();
			return String.valueOf(date);
		} else if (component instanceof JCheckBox) {
			return String.valueOf(((JCheckBox)component).isSelected());
		}
		return new String();
	}
}
