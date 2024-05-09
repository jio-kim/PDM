package com.kgm.common;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Painter;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SYMCAWTLOVComboBox extends JComboBox implements SYMCInterfaceComponent {

	private static final long serialVersionUID = 1L;
	private TCSession session = CustomUtil.getTCSession();
	private boolean mandatory;
	
	/**
	 * Lov Name�� ���Ͽ� Teamcenter LOV ���� ComboBox�� ����.
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2012. 12. 10.
	 * @param lovName
	 */
	public SYMCAWTLOVComboBox(String lovName, boolean mandatory){
		super();
		
		this.mandatory = mandatory;
		/** ComboBox Value ���� */
		comboValueSetting(lovName);
		/** LOV Values�� ������ ��ū ���ڸ� Popup���� �����ش�. */
		setRenderer(new PopupCellRenderer());
	}
	
	/**
	 * ������.
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2012. 12. 10.
	 */
	public SYMCAWTLOVComboBox(String[] comboValue, boolean mandatory){
		super();
		
		this.mandatory = mandatory;
		/** ComboBox �Էµ� �� ���� �޼ҵ� */
		setModel(new DefaultComboBoxModel(comboValue));
		/** LOV Values�� ������ ��ū ���ڸ� Popup���� �����ش�. */
		setRenderer(new PopupCellRenderer());
	}
	
	public SYMCAWTLOVComboBox(Vector valueVector, boolean mandatory){
		super();
		
		this.mandatory = mandatory;
		
		setModel(new DefaultComboBoxModel(valueVector));
	}

	/**
	 * lovName�� ���Ͽ� ComboBox �� ����.
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2012. 12. 10.
	 */
	private void comboValueSetting(String lovName) {
		try{
			if(lovName != null){
				TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
	    		TCComponentListOfValues[] listofvalues = listofvaluestype.find(lovName);
	    		TCComponentListOfValues listofvalue = listofvalues[0];
	    		
	    		Object[] lovValues = listofvalue.getListOfValues().getListOfValues();
	    		
	    		setModel(new DefaultComboBoxModel(lovValues));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �ʼ� �Է� �׸� ����
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2012. 3. 27.
	 * @param flag
	 */
	public void setMandatory(boolean flag) {
		mandatory = flag;
	}

	/**
	 * �ʼ� �Է� �׸� ǥ��
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
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
	 * LOV Values�� ������ ��ū ���ڸ� Popup���� �����ش�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
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
