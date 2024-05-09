package com.kgm.commands.masterdata;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import com.kgm.common.CustomTCTable;
import com.kgm.common.FunctionField;
import com.kgm.common.SYMCAWTLabel;
import com.kgm.common.SYMCAWTTitledBorder;
import com.kgm.common.dialog.SYMCAWTAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

@SuppressWarnings("serial")
public class MasterDataDialog extends SYMCAWTAbstractDialog {
	
	private Registry registry;
	private CustomTCTable table;
	private FunctionField stdNameFT;
	private FunctionField valuesFT;
	private JTextArea descFT;

	public MasterDataDialog(Frame frame) {
		super(frame);
		this.registry = Registry.getRegistry(this);
		
		initUI();
		
		}

	private void initUI() {
		setTitle(registry.getString("MasterDataDialog.TITLE"));
		
		createDialogUI(registry.getString("MasterDataDialog.MESSAGE_TITLE"), registry.getImageIcon("MasterDataDialogHeader.ICON"));
		
		add("unbound.bind", createPanel());
	}

	private JPanel createPanel() {
		JPanel panel = new JPanel(new HorizontalLayout());
		panel.setOpaque(false);
		
		panel.add("unbound.bind", createTablePanel());
		panel.add("unbound.bind", createEditorPanel());
		return panel;
	}

	private JPanel createTablePanel() {
		JPanel panel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(boxLayout);
		panel.setOpaque(false);
		panel.setBorder(new SYMCAWTTitledBorder("기준 정보"));
		
		table = new CustomTCTable(CustomUtil.getTCSession(), new String[]{"Std Name", "Values", "Desc"});
		table.setRowHeight(20);
		table.setAutoResizeMode(TCTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setEditable(false);
		table.addMouseListener(this);
		
		JScrollPane tableScrollPane = new JScrollPane(table);
		tableScrollPane.setPreferredSize(new Dimension(300, 400));
		tableScrollPane.getViewport().setBackground(Color.WHITE);
		
		panel.add(tableScrollPane);
		
		return panel;
	}

	private JPanel createEditorPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setOpaque(false);
		panel.setBorder(new SYMCAWTTitledBorder("기준 정보 변경"));
		
		panel.add("top.bind", createEditorInfoPanel());
		panel.add("top.bind", createEditorButtonPanel());
		
		return panel;
	}
	
	private JPanel createEditorInfoPanel() {
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setOpaque(false);
		stdNameFT = new FunctionField(20);
		valuesFT = new FunctionField(20);
		descFT = new JTextArea(6, 20);
		descFT.setLineWrap(true);
		JScrollPane descScrollPane = new JScrollPane(descFT);
		descScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		panel.add("1.1.center.center", new SYMCAWTLabel("Std.Name"));
		panel.add("1.2.center.center.resizable.prefereed", stdNameFT);
		panel.add("2.1.center.center", new SYMCAWTLabel("Values"));
		panel.add("2.2.center.center.resizable.prefereed", valuesFT);
		panel.add("3.1.center.center", new SYMCAWTLabel("Desc"));
		panel.add("3.2.center.center.resizable.prefereed", descScrollPane);
		
		return panel;
	}

	private JPanel createEditorButtonPanel() {
		JPanel panel = new JPanel(new HorizontalLayout());
		panel.setOpaque(false);
		
		JButton addButton = new JButton(registry.getImageIcon("Add_24.ICON"));
		addButton.setToolTipText(registry.getString("MasterDataDialog.LABEL.Add"));
		addButton.setActionCommand("EditorAdd");
		addButton.addActionListener(this);
		addButton.setPreferredSize(new Dimension(32, 32));
		
		JButton editButton = new JButton(registry.getImageIcon("Edit_24.ICON"));
		editButton.setToolTipText(registry.getString("MasterDataDialog.LABEL.Edit"));
		editButton.setActionCommand("EditorEdit");
		editButton.addActionListener(this);
		editButton.setPreferredSize(new Dimension(32, 32));
		
		JButton deleteButton = new JButton(registry.getImageIcon("Delete_24.ICON"));
		deleteButton.setToolTipText(registry.getString("MasterDataDialog.LABEL.Delete"));
		deleteButton.setActionCommand("EditorDelete");
		deleteButton.addActionListener(this);
		deleteButton.setPreferredSize(new Dimension(32, 32));
		
		JButton clearButton = new JButton(registry.getImageIcon("Clear_24.ICON"));
		clearButton.setToolTipText(registry.getString("MasterDataDialog.LABEL.Clear"));
		clearButton.setActionCommand("EditorClear");
		clearButton.addActionListener(this);
		clearButton.setPreferredSize(new Dimension(32, 32));
		
		panel.add("right.bind", clearButton);
		panel.add("right.bind", editButton);
		panel.add("right.bind", deleteButton);
		panel.add("right.bind", addButton);
		
		return panel;
	}

	@Override
	protected JPanel getUIPanel() {
		return null;
	}

	@Override
	public boolean validCheck() {
		return false;
	}

	@Override
	public void invokeOperation(ActionEvent e) {

	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals("EditorAdd")) {
			
		} else if (actionCommand.equals("EditorDelete")) {
			
		} else if (actionCommand.equals("EditorEdit")) {
			
		} else if (actionCommand.equals("EditorClear")) {
			stdNameFT.setText("");
			valuesFT.setText("");
			descFT.setText("");
		}
	}

}
