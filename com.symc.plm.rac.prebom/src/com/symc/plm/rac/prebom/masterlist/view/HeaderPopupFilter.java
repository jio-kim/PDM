package com.symc.plm.rac.prebom.masterlist.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel.HeaderPopup;

public class HeaderPopupFilter extends JPanel {
	private JTextField tfFilter;
	private HeaderPopup popup;
	private MasterListTablePanel masterListPanel;
	private JTree tree = null;
	private JScrollPane scroll = null;
	private CheckBoxNode columnCellValues[] = null;
//	private ArrayList<String> selectedList = new ArrayList();
	/**
	 * Create the panel.
	 */
	public HeaderPopupFilter(HeaderPopup popup, MasterListTablePanel masterListPanel) {
		this.popup = popup;
		this.masterListPanel = masterListPanel;
		init();
	}
	
	private void init(){
		setLayout(new BorderLayout(0, 1));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.WEST);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(HeaderPopupFilter.class.getResource("/icons/filter.png")));
		panel_2.add(lblNewLabel);

		tfFilter = new JTextField();
		tfFilter.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				for( int i = 0; i < columnCellValues.length; i++){
					String str = tfFilter.getText().trim();
					if( columnCellValues[i].text.indexOf(str) > -1 ){
						columnCellValues[i].selected = true;
						System.out.println("Filtered....");
					}else{
						columnCellValues[i].selected = false;
					}
				}
				tree.repaint();
				super.keyReleased(e);
			}
			
		});
		panel.add(tfFilter);
		tfFilter.setColumns(17);

		
		scroll = new JScrollPane();
		reload();
		add(scroll);

		JPanel btnPanel = new JPanel();
		add(btnPanel, BorderLayout.SOUTH);

		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				popup.execFilter(true);
				popup.setVisible(false);
			}
		});
		btnPanel.add(btnOk);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popup.setVisible(false);
			}
		});
		btnPanel.add(btnCancel);

		setPreferredSize(new Dimension(200, 200));
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.WEST);
		
		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setIcon(new ImageIcon(HeaderPopupFilter.class.getResource("/icons/filter_add.png")));
		panel_1.add(lblNewLabel_1);
	}

	public void reload(){
//		int modelIdx = masterListPanel.table.convertColumnIndexToModel(popup.getColumnIdx());
		DefaultTableModel model = (DefaultTableModel)masterListPanel.table.getModel();
		ArrayList<String> list = new ArrayList();
		for( int i = 0; i < model.getRowCount(); i++){
			Object obj = model.getValueAt(i, popup.getColumnIdx());
			if( obj instanceof CellValue){
				CellValue cellValue = (CellValue)obj;
				if( !list.contains(cellValue.getValue())){
					list.add(cellValue.getValue());
				}
			}else{
				if( !list.contains(obj.toString())){
					list.add(obj.toString());
				}
			}
		}
		
		String tfText = tfFilter.getText();
		columnCellValues = new CheckBoxNode[list.size() + 1];
		
		boolean bFlag = false;
		HashMap<Integer, ArrayList<String>> filterMap = popup.getFilterMap();
		ArrayList<String> filterList = filterMap.get(popup.getColumnIdx());
		if( filterList == null){
			columnCellValues[0] = new CheckBoxNode("(All)", true);
			for( int i = 0; i < list.size(); i++){
				columnCellValues[i + 1] = new CheckBoxNode(list.get(i), true);
			}
		}else{
			columnCellValues[0] = new CheckBoxNode("(All)", false);
			for( int i = 0; i < list.size(); i++){
				if( filterList.contains(list.get(i))){
					columnCellValues[i + 1] = new CheckBoxNode(list.get(i), true);
				}else{
					columnCellValues[i + 1] = new CheckBoxNode(list.get(i), false);
				}
			}
		}
		
		
		Vector rootVector = new NamedVector("Root", columnCellValues);
		tree = new JTree(rootVector);

		CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
		tree.setCellRenderer(renderer);

		CheckBoxNodeEditor editor = new CheckBoxNodeEditor(tree);
		tree.setCellEditor(editor);
		tree.setEditable(true);
		scroll.setViewportView(tree);
	}
	
	public JTextField getTfFilter() {
		return tfFilter;
	}
	
	public ArrayList<CheckBoxNode> getCheckedNode(){
		
		ArrayList<CheckBoxNode> list = new ArrayList();
		for( int i = 1; i < columnCellValues.length; i++){
			if( columnCellValues[i].isSelected()){
				list.add(columnCellValues[i]);
			}
		}
		return list;
	}

	class CheckBoxNodeRenderer implements TreeCellRenderer {
		private JCheckBox leafRenderer = new JCheckBox();

		private DefaultTreeCellRenderer nonLeafRenderer = new DefaultTreeCellRenderer();

		Color selectionBorderColor, selectionForeground, selectionBackground,
				textForeground, textBackground;

		protected JCheckBox getLeafRenderer() {
			return leafRenderer;
		}

		public CheckBoxNodeRenderer() {
			Font fontValue;
			fontValue = UIManager.getFont("Tree.font");
			if (fontValue != null) {
				leafRenderer.setFont(fontValue);
			}
			Boolean booleanValue = (Boolean) UIManager
					.get("Tree.drawsFocusBorderAroundIcon");
			leafRenderer.setFocusPainted((booleanValue != null)
					&& (booleanValue.booleanValue()));

			selectionBorderColor = UIManager
					.getColor("Tree.selectionBorderColor");
			selectionForeground = UIManager
					.getColor("Tree.selectionForeground");
			selectionBackground = UIManager
					.getColor("Tree.selectionBackground");
			textForeground = UIManager.getColor("Tree.textForeground");
			textBackground = UIManager.getColor("Tree.textBackground");
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			Component returnValue;
			if (leaf) {

				String stringValue = tree.convertValueToText(value, selected,
						expanded, leaf, row, false);
				leafRenderer.setText(stringValue);
				leafRenderer.setSelected(false);

				leafRenderer.setEnabled(tree.isEnabled());

				if (selected) {
					leafRenderer.setForeground(selectionForeground);
					leafRenderer.setBackground(selectionBackground);
				} else {
					leafRenderer.setForeground(textForeground);
					leafRenderer.setBackground(textBackground);
				}

				if ((value != null)
						&& (value instanceof DefaultMutableTreeNode)) {
					Object userObject = ((DefaultMutableTreeNode) value)
							.getUserObject();
					if (userObject instanceof CheckBoxNode) {
						CheckBoxNode node = (CheckBoxNode) userObject;
						leafRenderer.setText(node.getText());
						leafRenderer.setSelected(node.isSelected());
					}
				}
				returnValue = leafRenderer;
			} else {
				returnValue = nonLeafRenderer.getTreeCellRendererComponent(
						tree, value, selected, expanded, leaf, row, hasFocus);
			}
			return returnValue;
		}
	}

	class CheckBoxNodeEditor extends AbstractCellEditor implements
			TreeCellEditor {

		CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();

		ChangeEvent changeEvent = null;

		JTree tree;

		public CheckBoxNodeEditor(JTree tree) {
			this.tree = tree;
		}

		public Object getCellEditorValue() {
			JCheckBox checkbox = renderer.getLeafRenderer();
			CheckBoxNode checkBoxNode = new CheckBoxNode(checkbox.getText(),
					checkbox.isSelected());
			return checkBoxNode;
		}

		public boolean isCellEditable(EventObject event) {
			boolean returnValue = false;
			if (event instanceof MouseEvent) {
				MouseEvent mouseEvent = (MouseEvent) event;
				TreePath path = tree.getPathForLocation(mouseEvent.getX(),
						mouseEvent.getY());
				if (path != null) {
					Object node = path.getLastPathComponent();
					if ((node != null)
							&& (node instanceof DefaultMutableTreeNode)) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
						Object userObject = treeNode.getUserObject();
						returnValue = ((treeNode.isLeaf()) && (userObject instanceof CheckBoxNode));
					}
				}
			}
			return returnValue;
		}

		public Component getTreeCellEditorComponent(final JTree tree, final Object value,
				boolean selected, boolean expanded, boolean leaf, final int row) {

			final Component editor = renderer.getTreeCellRendererComponent(tree,
					value, true, expanded, leaf, row, true);

			// editor always selected / focused
			ItemListener itemListener = new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if( itemEvent.getStateChange() == ItemEvent.SELECTED){
						columnCellValues[row].setSelected(true);
					}else if( itemEvent.getStateChange() == ItemEvent.DESELECTED){
						columnCellValues[row].setSelected(false);
					}
					
					if (stopCellEditing()) {
						fireEditingStopped();
					}
					
					DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
					DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
					JCheckBox check = (JCheckBox)editor;
					
//					columnCellValues[row].setSelected(check.isSelected());
					
					System.out.println(columnCellValues[row].text + " : " + columnCellValues[row].isSelected());
					
					if( check.getText().equals("(All)")){
						
						Enumeration enums = root.children();
						
						if( check.isSelected()){
							while(enums.hasMoreElements()){
								DefaultMutableTreeNode node = (DefaultMutableTreeNode)enums.nextElement();
								CheckBoxNode chkNode = (CheckBoxNode)node.getUserObject();
								chkNode.setSelected(true);
							}
							
						}else{
							while(enums.hasMoreElements()){
								DefaultMutableTreeNode node = (DefaultMutableTreeNode)enums.nextElement();
								CheckBoxNode chkNode = (CheckBoxNode)node.getUserObject();
								chkNode.setSelected(false);
							}
						}
					}
					
					tree.repaint();
					ItemListener[] listener = check.getItemListeners();
					for( int i = 0; listener != null && i < listener.length; i++){
						check.removeItemListener(listener[i]);
					}
				}
			};
			if (editor instanceof JCheckBox) {
				((JCheckBox) editor).addItemListener(itemListener);
			}

			return editor;
		}
	}

	class CheckBoxNode {
		String text;

		boolean selected;

		public CheckBoxNode(String text, boolean selected) {
			this.text = text;
			this.selected = selected;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean newValue) {
			selected = newValue;
		}

		public String getText() {
			return text;
		}

		public void setText(String newValue) {
			text = newValue;
		}

		public String toString() {
			return getClass().getName() + "[" + text + "/" + selected + "]";
		}
	}

	class NamedVector extends Vector {
		String name;

		public NamedVector(String name) {
			this.name = name;
		}

		public NamedVector(String name, Object elements[]) {
			this.name = name;
			for (int i = 0, n = elements.length; i < n; i++) {
				add(elements[i]);
			}
		}

		public String toString() {
			return "[" + name + "]";
		}
	}

}
