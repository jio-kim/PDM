package com.kgm.commands.conditionmapper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;

/**
 * 필터를 추가하기 위한 dialog 생성.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"serial", "rawtypes", "unused", "unchecked"})
public class FilterConditionDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private DefaultListModel optionModel = null;
	private boolean isPositive = true;
	private JRadioButton equalRadioBtn = null;
	private JRadioButton notEqualRadioBtn = null;
	private JList reqList = null, resList = null;
	private JButton okButton = null;
	private JButton applyBtn = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			FilterConditionDialog dialog = new FilterConditionDialog(null, null, true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public FilterConditionDialog(final AutoConditionMapperDialog parent, ListModel optionModel, final boolean isPositive) {
		super(parent, true);
		setTitle("Filter Dialog");
		setBounds(100, 100, 395, 274);
		this.optionModel = (DefaultListModel)optionModel;
		this.isPositive = isPositive;
		
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    Dimension screenSize = tk.getScreenSize();
	    int screenHeight = screenSize.height;
	    int screenWidth = screenSize.width;
	    int width = getSize().width;
	    int height = getSize().height;
//	    setSize(screenWidth / 2, screenHeight / 2);
	    setLocation((screenWidth - width)/2, (screenHeight - height)/ 2);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		{
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(80, 10));
			contentPanel.add(panel);
			panel.setLayout(new BorderLayout(0, 0));
			{
				DefaultListModel model = new DefaultListModel();
				if( this.optionModel != null && this.optionModel.size() > 0){
					for(int i = 0; i < this.optionModel.size(); i++){
						VariantOption option = (VariantOption)optionModel.getElementAt(i);
						if( option.hasValues()){
							java.util.List<VariantValue> valueList = AutoConditionMapperOperation.getEnableValues( option );
							for( int j = 0; j < valueList.size(); j++){
								VariantValue value = (VariantValue)valueList.get(j);
								model.addElement(value);
							}
						}
					}
				}
				
				reqList = new JList();
				reqList.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent listselectionevent) {
						if( reqList.getSelectedIndex() > -1 && resList.getSelectedIndex() > -1){
							VariantValue reqValue = (VariantValue)reqList.getSelectedValue();
							VariantValue resValue = (VariantValue)resList.getSelectedValue();
							if( !reqValue.getOption().equals(resValue.getOption())){
								okButton.setEnabled(true);
							}else{
								okButton.setEnabled(false);
							}
						}
					}
				});
				reqList.setCellRenderer(new DefaultListCellRenderer(){

					@Override
					public Component getListCellRendererComponent(
							JList jlist, Object obj, int i,
							boolean flag, boolean flag1) {
						
						if( obj instanceof VariantValue){
							Component com = super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
							JLabel label = (JLabel)com;
							VariantValue value = (VariantValue)obj;
							label.setText(value.getValueName() + " | " + value.getValueDesc());
							return label;
						}else{
							return super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
						}
					}

					
				});
				reqList.setModel(model);
				JScrollPane pane = new JScrollPane();
//				pane.setPreferredSize(new Dimension(80, 2));
				pane.setViewportView(reqList);
				pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				pane.getViewport().setBackground(Color.WHITE);
				panel.add(pane);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(50, 300));
			contentPanel.add(panel);
			ButtonGroup group = new ButtonGroup();
			panel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			{
				equalRadioBtn = new JRadioButton("=");
				equalRadioBtn.setEnabled(false);
				group.add(equalRadioBtn);
				equalRadioBtn.setSelected(isPositive);
				panel.add(equalRadioBtn);
			}
			{
				notEqualRadioBtn = new JRadioButton("!=");
				notEqualRadioBtn.setEnabled(false);
				group.add(notEqualRadioBtn);
				notEqualRadioBtn.setSelected(!isPositive);
				panel.add(notEqualRadioBtn);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(80, 10));
			contentPanel.add(panel);
			panel.setLayout(new BorderLayout(0, 0));
			{
				DefaultListModel model = new DefaultListModel();
				if( this.optionModel != null && this.optionModel.size() > 0){
					for(int i = 0; i < this.optionModel.size(); i++){
						VariantOption option = (VariantOption)optionModel.getElementAt(i);
						if( option.hasValues()){
							java.util.List<VariantValue> valueList = AutoConditionMapperOperation.getEnableValues(option);
							for( int j = 0; j < valueList.size(); j++){
								VariantValue value = (VariantValue)valueList.get(j);
								model.addElement(value);
							}
						}
					}
				}
				
				resList = new JList();
				resList.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent listselectionevent) {
						if( reqList.getSelectedIndex() > -1 && resList.getSelectedIndex() > -1){
							VariantValue reqValue = (VariantValue)reqList.getSelectedValue();
							VariantValue resValue = (VariantValue)resList.getSelectedValue();
							if( !reqValue.getOption().equals(resValue.getOption())){
								applyBtn.setEnabled(true);
								okButton.setEnabled(true);
							}else{
								applyBtn.setEnabled(false);
								okButton.setEnabled(false);
							}
						}
					}
				});
				resList.setCellRenderer(new DefaultListCellRenderer(){

					@Override
					public Component getListCellRendererComponent(
							JList jlist, Object obj, int i,
							boolean flag, boolean flag1) {
						
						if( obj instanceof VariantValue){
							Component com = super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
							JLabel label = (JLabel)com;
							VariantValue value = (VariantValue)obj;
							label.setText(value.getValueName() + " | " + value.getValueDesc());
							return label;
						}else{
							return super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
						}
					}

					
				});
				resList.setModel(model);
				
				JScrollPane pane = new JScrollPane();
//				pane.setPreferredSize(new Dimension(80, 2));
				pane.setViewportView(resList);
				pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				pane.getViewport().setBackground(Color.WHITE);
				panel.add(pane);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setEnabled(false);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						if( reqList.getSelectedIndex() > -1 && resList.getSelectedIndex() > -1){
							ArrayList list = new ArrayList();
							list.add(reqList.getSelectedValue());
							list.add(new Boolean(equalRadioBtn.isSelected()));
							list.add(resList.getSelectedValue());
							
							if( equalRadioBtn.isSelected()){
								DefaultListModel model = (DefaultListModel)parent.getPositiveFilterList().getModel();
								for( int i = 0; i < model.size(); i++){
									if( list.equals( model.getElementAt(i))){
										return;
									}
								}
								
							}else{
								DefaultListModel model = (DefaultListModel)parent.getNegativeFilterList().getModel();
								for( int i = 0; i < model.size(); i++){
									if( list.equals( model.getElementAt(i))){
										return;
									}
								}
							}
							
							parent.addFilter(isPositive, list);
							FilterConditionDialog.this.dispose();
						}
						
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				
				{
					applyBtn = new JButton("Apply");
					applyBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							if( reqList.getSelectedIndex() > -1 && resList.getSelectedIndex() > -1){
								VariantValueVector list = new VariantValueVector();
								list.add(reqList.getSelectedValue());
								list.add(new Boolean(equalRadioBtn.isSelected()));
								list.add(resList.getSelectedValue());
								if( equalRadioBtn.isSelected()){
									DefaultListModel model = (DefaultListModel)parent.getPositiveFilterList().getModel();
									for( int i = 0; i < model.size(); i++){
										if( list.equals( model.getElementAt(i))){
											return;
										}
									}
									
								}else{
									DefaultListModel model = (DefaultListModel)parent.getNegativeFilterList().getModel();
									for( int i = 0; i < model.size(); i++){
										if( list.equals( model.getElementAt(i))){
											return;
										}
									}
								}
								
								parent.addFilter(isPositive, list);
							}
						}
					});
					applyBtn.setEnabled(false);
					buttonPane.add(applyBtn);
				}
				
				JButton cancelButton = new JButton("Close");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						FilterConditionDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
