package com.kgm.common.utils;

import java.util.*;
import javax.swing.table.*;
import com.teamcenter.rac.kernel.*;
import com.teamcenter.rac.util.*;

/**
 * com.teamcenter.rac.util.ArraySorter에 기본적인 Sort 기능은 충실히 구현되어 있으며 Array와 Vector의 전환기능도 제공된다.
 */
@SuppressWarnings({"rawtypes"})
public class Sorter {
	private static Sorter sorter = new Sorter();
	public static Sorter getInstance() {
		return sorter;
	}

	public Sorter() {
	}

	/**
	 * Vector 내의 Object를 정렬한다.
	 */
	public void sort(Vector vector, boolean ascend) {
		try {
			if (vector.elementAt(0) instanceof String) {
				ArraySorter.sort(vector, ascend);
//				sortByString(vector, ascend);
			} else if (vector.elementAt(0) instanceof Date) {
				sortByDate(vector, ascend);
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Array 내의 Object를 정렬한다.
	 */
	public void sort(Object[] object, String type, boolean ascend) {
		try {
			if (object[0] instanceof String) {
				ArraySorter.sort(object, ascend);
//				sortByDate(object, ascend);
			} else if (object[0] instanceof Date) {
				sortByDate(object, ascend);
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Vector 내의 TCComponent를 특정 property의 값에 따라 정렬한다.
	 */
	public void sort(Vector vector, String type, String property, boolean ascend) {
		try {
			if (type.equals("String")) {
				sortByString(vector, property, ascend);
			} else if (type.equals("Date")) {
				sortByDate(vector, property, ascend);
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Array 내의 TCComponent를 특정 property의 값에 따라 정렬한다.
	 */
	public void sort(TCComponent[] component, String type, String property, boolean ascend) {
		try {
			if (type.equals("String")) {
				sortByString(component, property, ascend);
			} else if (type.equals("Date")) {
				sortByDate(component, property, ascend);
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * DefaultTableModel을 특정 Column의 값에 따라 정렬한다.
	 */
	public void sort(DefaultTableModel model, int column, boolean ascend) {
		try {
			Object object = model.getValueAt(0, column);
			if (object instanceof String) {
				sortByString(model, column, ascend);
			} else if (object instanceof Date) {
				sortByDate(model, column, ascend);
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void sortByString(Vector vector, boolean ascend) throws TCException {
		String[] strings = new String[vector.size()];
		for (int i = 0; i < vector.size(); i++) {
			strings[i] = (String)vector.elementAt(i);
		}
		for (int i = 0; i < vector.size(); i++) {
			int index = i;
			for (int j = i + 1; j < vector.size(); j++) {
				if ((ascend && strings[j].compareTo(strings[index]) < 0) || (!ascend && strings[j].compareTo(strings[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			String temp = strings[i];
			strings[i] = strings[index];
			strings[index] = temp;
			String data = (String)vector.elementAt(i);
			vector.setElementAt(vector.elementAt(index), i);
			vector.setElementAt(data, index);
		}
	}

	@SuppressWarnings("unchecked")
	public void sortByDate(Vector vector, boolean ascend) throws TCException {
		Date[] dates = new Date[vector.size()];
		for (int i = 0; i < vector.size(); i++) {
			dates[i] = (Date)vector.elementAt(i);
		}
		for (int i = 0; i < vector.size(); i++) {
			int index = i;
			for (int j = i + 1; j < vector.size(); j++) {
				if ((ascend && dates[j].compareTo(dates[index]) < 0) || (!ascend && dates[j].compareTo(dates[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			Date temp = dates[i];
			dates[i] = dates[index];
			dates[index] = temp;
			Date data = (Date)vector.elementAt(i);
			vector.setElementAt(vector.elementAt(index), i);
			vector.setElementAt(data, index);
		}
	}

	public void sortByString(Object[] object, boolean ascend) throws TCException {
		String[] strings = new String[object.length];
		for (int i = 0; i < object.length; i++) {
			strings[i] = (String)object[i];
		}
		for (int i = 0; i < object.length; i++) {
			int index = i;
			for (int j = i + 1; j < object.length; j++) {
				if ((ascend && strings[j].compareTo(strings[index]) < 0) || (!ascend && strings[j].compareTo(strings[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			String temp = strings[i];
			strings[i] = strings[index];
			strings[index] = temp;
			String data = (String)object[i];
			object[i] = object[index];
			object[index] = data;
		}
	}

	public void sortByDate(Object[] object, boolean ascend) throws TCException {
		Date[] dates = new Date[object.length];
		for (int i = 0; i < object.length; i++) {
			dates[i] = (Date)object[i];
		}
		for (int i = 0; i < object.length; i++) {
			int index = i;
			for (int j = i + 1; j < object.length; j++) {
				if ((ascend && dates[j].compareTo(dates[index]) < 0) || (!ascend && dates[j].compareTo(dates[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			Date temp = dates[i];
			dates[i] = dates[index];
			dates[index] = temp;
			Date data = (Date)object[i];
			object[i] = object[index];
			object[index] = data;
		}
	}

	@SuppressWarnings("unchecked")
	public void sortByString(Vector vector, String property, boolean ascend) throws TCException {
		String[] strings = new String[vector.size()];
		for (int i = 0; i < vector.size(); i++) {
			strings[i] = ((TCComponent)vector.elementAt(i)).getStringProperty(property);
		}
		for (int i = 0; i < vector.size(); i++) {
			int index = i;
			for (int j = i + 1; j < vector.size(); j++) {
				if ((ascend && strings[j].compareTo(strings[index]) < 0) || (!ascend && strings[j].compareTo(strings[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			String temp = strings[i];
			strings[i] = strings[index];
			strings[index] = temp;
			TCComponent data = (TCComponent)vector.elementAt(i);
			vector.setElementAt(vector.elementAt(index), i);
			vector.setElementAt(data, index);
		}
	}

	@SuppressWarnings("unchecked")
	public void sortByDate(Vector vector, String property, boolean ascend) throws TCException {
		Date[] dates = new Date[vector.size()];
		for (int i = 0; i < vector.size(); i++) {
			dates[i] = ((TCComponent)vector.elementAt(i)).getDateProperty(property);
		}
		for (int i = 0; i < vector.size(); i++) {
			int index = i;
			for (int j = i + 1; j < vector.size(); j++) {
				if ((ascend && dates[j].compareTo(dates[index]) < 0) || (!ascend && dates[j].compareTo(dates[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			Date temp = dates[i];
			dates[i] = dates[index];
			dates[index] = temp;
			TCComponent data = (TCComponent)vector.elementAt(i);
			vector.setElementAt(vector.elementAt(index), i);
			vector.setElementAt(data, index);
		}
	}

	public void sortByString(TCComponent[] component, String property, boolean ascend) throws TCException {
		String[] strings = new String[component.length];
		for (int i = 0; i < component.length; i++) {
			strings[i] = component[i].getStringProperty(property);
		}
		for (int i = 0; i < component.length; i++) {
			int index = i;
			for (int j = i + 1; j < component.length; j++) {
				if ((ascend && strings[j].compareTo(strings[index]) < 0) || (!ascend && strings[j].compareTo(strings[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			String temp = strings[i];
			strings[i] = strings[index];
			strings[index] = temp;
			TCComponent data = component[i];
			component[i] = component[index];
			component[index] = data;
		}
	}

	public void sortByDate(TCComponent[] component, String property, boolean ascend) throws TCException {
		Date[] dates = new Date[component.length];
		for (int i = 0; i < component.length; i++) {
			dates[i] = component[i].getDateProperty(property);
		}
		for (int i = 0; i < component.length; i++) {
			int index = i;
			for (int j = i + 1; j < component.length; j++) {
				if ((ascend && dates[j].compareTo(dates[index]) < 0) || (!ascend && dates[j].compareTo(dates[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			Date temp = dates[i];
			dates[i] = dates[index];
			dates[index] = temp;
			TCComponent data = component[i];
			component[i] = component[index];
			component[index] = data;
		}
	}

	public void sortByString(DefaultTableModel model, int column, boolean ascend) throws TCException {
		String[] strings = new String[model.getRowCount()];
		for (int i = 0; i < model.getRowCount(); i++) {
			strings[i] = (String)model.getValueAt(i, column);
		}
		for (int i = 0; i < model.getRowCount(); i++) {
			int index = i;
			for (int j = i + 1; j < model.getRowCount(); j++) {
				if ((ascend && strings[j].compareTo(strings[index]) < 0) || (!ascend && strings[j].compareTo(strings[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			String temp = strings[i];
			strings[i] = strings[index];
			strings[index] = temp;
			model.moveRow(index, index, i);
		}
	}

	public void sortByDate(DefaultTableModel model, int column, boolean ascend) throws TCException {
		Date[] dates = new Date[model.getRowCount()];
		for (int i = 0; i < model.getRowCount(); i++) {
			dates[i] = (Date)model.getValueAt(i, column);
		}
		for (int i = 0; i < model.getRowCount(); i++) {
			int index = i;
			for (int j = i + 1; j < model.getRowCount(); j++) {
				if ((ascend && dates[j].compareTo(dates[index]) < 0) || (!ascend && dates[j].compareTo(dates[index]) > 0)) {
					index = j;
				}
			}
			if (index == i)continue;
			Date temp = dates[i];
			dates[i] = dates[index];
			dates[index] = temp;
			model.moveRow(index, index, i);
		}
	}
}
