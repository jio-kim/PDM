package com.ssangyong.commands.ospec;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpComboValue;
import com.ssangyong.commands.ospec.op.OpValueName;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.util.MessageBox;

public class OSpecConditionSetDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable combTable = null;
	private OSpecMainDlg parentDlg = null;
	private JTextField tfCondition = null;
	private Vector<Object> conditionVec = null;
	private OSpec ospec = null;
	private JComboBox cbOption = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			OSpecConditionSetDlg dialog = new OSpecConditionSetDlg(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public OSpecConditionSetDlg(OSpecMainDlg parentDlg, OSpec ospec) {
		super(parentDlg, true);
		setTitle("Condition Setting");
		setResizable(true);
		setBounds(100, 100, 477, 225);
		this.parentDlg = parentDlg;
		this.ospec = ospec;
		init();
	}
	
	private void init(){
		
		conditionVec = new Vector<Object>(){

			@Override
			public synchronized String toString() {
				// TODO Auto-generated method stub
				String conditionStr = "";
				for(int i = 0; i < elementCount; i++){
					if( elementData[i] instanceof OpComboValue){
						conditionStr += " " + ((OpComboValue)elementData[i]).getOption();
					}else{
						conditionStr += " " + elementData[i].toString();
					}
				}
				
				return conditionStr.trim();
			}
			
		};
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Logical operation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new GridLayout(2, 1, 0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1);
				FlowLayout fl_panel_1 = (FlowLayout) panel_1.getLayout();
				fl_panel_1.setAlignment(FlowLayout.LEADING);
				
				cbOption = new JComboBox();
				cbOption.addItem("Select Option");
				for( OpValueName opValueName : ospec.getOpNameList()){
					cbOption.addItem(new OpComboValue(opValueName));
				};
				
				cbOption.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent event) {
						if( event.getStateChange() == ItemEvent.SELECTED){
							Object obj = cbOption.getSelectedItem();
							if( obj instanceof OpValueName){
								conditionVec.add(obj);
								tfCondition.setText(conditionVec.toString());
							}
						}
					}
				});
				
				panel_1.add(cbOption);
			}
			{
				JPanel panel_1 = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
				flowLayout.setAlignment(FlowLayout.LEADING);
				panel.add(panel_1);
				JRadioButton rdbtnAnd = new JRadioButton("AND");
				panel_1.add(rdbtnAnd);
				rdbtnAnd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						conditionVec.add("AND");
						tfCondition.setText(conditionVec.toString());
						JRadioButton btn = (JRadioButton)e.getSource();
						btn.setSelected(false);
					}
				});
				
				JRadioButton rdbtnOr = new JRadioButton("OR");
				panel_1.add(rdbtnOr);
				rdbtnOr.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						conditionVec.add("OR");
						tfCondition.setText(conditionVec.toString());
						JRadioButton btn = (JRadioButton)e.getSource();
						btn.setSelected(false);
					}
				});
				
				JRadioButton rdBtnLeft = new JRadioButton("(");
				panel_1.add(rdBtnLeft);
				rdBtnLeft.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						conditionVec.add("(");
						tfCondition.setText(conditionVec.toString());
						JRadioButton btn = (JRadioButton)e.getSource();
						btn.setSelected(false);
					}
				});
				
				JRadioButton rdBtnRight = new JRadioButton(")");
				panel_1.add(rdBtnRight);
				rdBtnRight.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						conditionVec.add(")");
						tfCondition.setText(conditionVec.toString());
						JRadioButton btn = (JRadioButton)e.getSource();
						btn.setSelected(false);
					}
				});
				
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Condition", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			contentPanel.add(panel, BorderLayout.SOUTH);
			{
				tfCondition = new JTextField();
				Font font = tfCondition.getFont();
				// same font but bold
				Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
				tfCondition.setFont(boldFont);
				tfCondition.setEditable(false);
				panel.add(tfCondition);
				tfCondition.setColumns(25);
			}
			{
				JButton btnCe = new JButton("CE");
				btnCe.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						conditionVec.remove(conditionVec.size() - 1);
						tfCondition.setText(conditionVec.toString());
						cbOption.setSelectedIndex(0);
					}
				});
				panel.add(btnCe);
				
				JButton btnReset = new JButton("C");
				btnReset.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						conditionVec.clear();
						tfCondition.setText(conditionVec.toString());
						cbOption.setSelectedIndex(0);
					}
				});
				panel.add(btnReset);
			}
		}
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						String result = null;
						String condition = conditionVec.toString();
						if( condition != null && !condition.equals("")){
							
							condition = condition.replaceAll(" AND ", " && ");
							condition = condition.replaceAll(" OR ", " || ");
							Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
							Matcher m = p.matcher(condition);
							result = m.replaceAll("true");
							Object obj;
							try {
								obj = parentDlg.getEngine().eval(result);
								if( !(obj instanceof Boolean)){
									MessageBox.post(OSpecConditionSetDlg.this, "Invalid Condition", "ERROR", MessageBox.ERROR);
									return;
								}
								
								parentDlg.addCondition(conditionVec);
								dispose();
							} catch (ScriptException e1) {
								// TODO Auto-generated catch block
								MessageBox.post(OSpecConditionSetDlg.this, "Invalid Condition", "ERROR", MessageBox.ERROR);
								return;
							}
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}		
	}

}
