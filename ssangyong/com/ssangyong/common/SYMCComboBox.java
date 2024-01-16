package com.ssangyong.common;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Painter;

@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class SYMCComboBox extends JComboBox implements SYMCInterfaceComponent {

	private static final long serialVersionUID = 1L;

	private TCSession session = CustomUtil.getTCSession();

	private boolean mandatory = false;

	private TCPreferenceService preferenceService;

	private DefaultComboBoxModel comboBoxModel;

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 7.
	 */
	public SYMCComboBox() {
		super();
	}

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 7.
	 */
	public SYMCComboBox(String lovName, boolean mandatory) {
		super();
		this.mandatory = mandatory;
		this.preferenceService = session.getPreferenceService();
		setListOfValues(lovName);
		setRenderer(new PopupCellRenderer());
	}

	/**
	 * LOV이름으로 콤보박스에 값들을 셋팅 한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 27.
	 * @param lovName
	 * @return
	 */
	public void setListOfValues(String lovName) {
		try {
			if (lovName != null) {
				TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
				TCComponentListOfValues[] listofvalues = listofvaluestype.find(lovName);
				if(listofvalues == null || listofvalues.length == 0) {
					return;
				}

				TCComponentListOfValues listofvalue = listofvalues[0];
				
				String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
				String[] lovDesces = listofvalue.getListOfValues().getDescriptions();
				String[] returnValue = new String[lovValues.length+1];
				
				if( lovValues != null ) {
					returnValue[0] = "";
					if(lovDesces == null) {
						for(int i = 0 ; i < lovValues.length ; i++) {
							returnValue[i+1] = lovValues[i];
							
						}
					} else {
						for(int i = 0 ; i < lovValues.length ; i++) {
							returnValue[i+1] = lovValues[i] + " (" + lovDesces[i] + ")";
						}
					}
				}
				
				
				comboBoxModel = new DefaultComboBoxModel(returnValue);
				setModel(comboBoxModel);
				    
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ComboBox Values 셋팅.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2012. 12. 18.
	 * @param valueArr
	 */
	public void setListOfValues(String[] valueArr){
		comboBoxModel = new DefaultComboBoxModel(valueArr);
		setModel(comboBoxModel);
	}

	/**
	 * 필수 입력 항목 지정
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 27.
	 * @param flag
	 */
	public void setMandatory(boolean flag) {
		mandatory = flag;
	}

	/**
	 * 필수 입력 항목 표시
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 27.
	 * @override
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 * @param g
	 */
	public void paint(Graphics g) {
		super.paint(g);
		if (mandatory) {
			Painter.paintIsRequired(this, g);
		}
	}

	@Override
	public Object getValue() {
		return getSelectedItem();
	}

	/**
	 * LOV Values에 지정된 토큰 문자를 Popup으로 보여준다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 27. Package ID : com.pungkang.common.lov.PreferencesLOVComboBox.java
	 */
	private class PopupCellRenderer extends BasicComboBoxRenderer {

		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				if (-1 < index) {
					list.setToolTipText(value.toString());
				}
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

}
