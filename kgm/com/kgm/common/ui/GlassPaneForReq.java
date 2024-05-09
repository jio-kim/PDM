package com.kgm.common.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * SWT�� �ʼ� �Է� ���� üũ�� �����ϰ�
 * Swing���� �ʼ� �Է� �������� ǥ���ϱ� ���� �����.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked", "unused"})
public class GlassPaneForReq extends JComponent {
	private ArrayList<Component> starComponents = null;
	private JDialog dialog = null;
	public GlassPaneForReq(ArrayList starComponents, JDialog dialog){
		this.starComponents = starComponents;
		this.dialog = dialog;
	}
	
	protected void paintComponent(Graphics g) {
		if( starComponents != null && !starComponents.isEmpty()){
			int absX = -10;
			int absY = -1;
			for( Component com : starComponents){
				 g.setColor(Color.red);
				 Component parent = com.getParent();
				 Point p = com.getLocation();
				 p = SwingUtilities.convertPoint(parent, p, dialog.getContentPane());
				 
				 int x1 = p.x + com.getWidth() + 4 + absX;
				 int y1 = p.y + 1;
				 int x2 = p.x + com.getWidth() + 4 + absX;
				 int y2 = p.y + 7;
				 //������
				 g.drawLine(x1, y1, x2, y2);
				 
				 x1 = p.x + com.getWidth() + 1 + absX;
				 y1 = p.y + 4;
				 x2 = p.x + com.getWidth() + 7 + absX;
				 y2 = p.y + 4;
				 //����
				 g.drawLine(x1, y1, x2, y2);
				 
				 x1 = p.x + com.getWidth() + 2 + absX;
				 y1 = p.y + 2;
				 x2 = p.x + com.getWidth() + 6 + absX;
				 y2 = p.y + 6;
				 //�»� --> ����
				 g.drawLine(x1, y1, x2, y2);
				 
				 x1 = p.x + com.getWidth() + 2 + absX;
				 y1 = p.y + 6;
				 x2 = p.x + com.getWidth() + 6 + absX;
				 y2 = p.y + 2;
				 //���� --> ���
				 g.drawLine(x1, y1, x2, y2);
			}
		}
    }

	/**
	 * �ʼ� �Է� �ʵ�� �߰��� ������Ʈ�� �˻��Ͽ�
	 * �Է»����� ������ return false;
	 */
	public boolean isValid(){
		
		if( starComponents == null ){
			return false;
		}
		
		if( starComponents != null && !starComponents.isEmpty()){
			boolean isAllValid = true;
			for( Component com : starComponents){
				if( com instanceof JTextField){
					JTextField tf = (JTextField)com;
					if( tf.getText().trim().equals("")){
						return false;
					}
				}else if( com instanceof JComboBox){
					JComboBox combo = (JComboBox)com;
					Object obj = combo.getSelectedItem();
					if( obj == null || combo.getSelectedIndex() < 0){
						return false;
					}
				}else if( com instanceof JTable){
					JTable table = (JTable)com;
					if( table.getRowCount() < 1){
						return false;
					}
				}else{
					isAllValid &= true;
				}
			}
		}
		return true;
	}
	
	/**
	 * �ʼ��Է� �ʵ������� ���� �Է����� ���� Component�� ����.
	 * @return
	 */
	public ArrayList getInvalidComponents(){
		
		if( starComponents == null ){
			return null;
		}
		
		ArrayList invalidComs = new ArrayList();
		if( starComponents != null && !starComponents.isEmpty()){
			boolean isAllValid = true;
			for( Component com : starComponents){
				if( com instanceof JTextField){
					JTextField tf = (JTextField)com;
					if( tf.getText().trim().equals("")){
						invalidComs.add(com);
					}
				}else if( com instanceof JComboBox){
					JComboBox combo = (JComboBox)com;
					Object obj = combo.getSelectedItem();
					if( obj == null || combo.getSelectedIndex() < 0){
						invalidComs.add(com);
					}
				}else if( com instanceof JTable){
					JTable table = (JTable)com;
					if( table.getRowCount() < 1){
						invalidComs.add(com);
					}
				}
			}
		}
		return invalidComs;
	}
}
