package com.kgm.commands.migration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import com.kgm.common.OperationAbortedListener;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.ExcelService;
import com.kgm.common.utils.SimpleFilter;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.AbstractTCCommandDialog;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.UIUtilities;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.rac.util.VerticalLayout;

@SuppressWarnings({"rawtypes", "unchecked", "static-access"})
public abstract class MasterMigrationDialog extends AbstractTCCommandDialog implements InterfaceAIFOperationListener, ActionListener, ItemListener, MouseListener {

	private static final long serialVersionUID = 1L;

	protected TCSession session;
	protected TCTextService textService;
	protected AbstractAIFOperation operation;
	protected String className;
	protected JTable table;
	protected String[] columns, columnsWidth;
    protected JComboBox comboBox;
	protected JCheckBox checkBox;
	protected JProgressBar progressBar;
	public  Registry registry = Registry.getRegistry(this);
	public WaitProgressBar waitProgress;

	public MasterMigrationDialog(AbstractAIFCommand command, Frame frame, TCSession session) {
		super(frame, command, false);
		this.session = session;
		textService = session.getTextService();
		className = getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);
		session.setStatus("���̱׷��̼� �غ�...");
		initUI();
		setBackground((JComponent)parentPanel);
		session.setReadyStatus();
	}

	public void initUI() {
		super.initUI();
		try {
			setTitle("���̱׷��̼�");
			createUI();
			setComponents();
			okButton.setVisible(false);
			showDialog();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void createUI() {
		mainPanel.setLayout(new VerticalLayout(10,0,0,0,0));
		mainPanel.add("top.bind", UIUtilities.createGradientHeader("���̱׷��̼� ���̾�α� �Դϴ�. ������ Ÿ���� ���� �Ͻð�, �˻��ư�� Ŭ�� �Ͻþ� ���ռ� �˻縦 ���� ���� �ϼ���.", registry.getImageIcon("Migration_Title.ICON"), 1));
		mainPanel.add("top.bind", createTypePanel());
		mainPanel.add("unbound.bind", createTablePanel());
		mainPanel.add("bottom.bind", createProgressPanel());
		session.setReadyStatus();
	}

	private JScrollPane createTablePanel() {
		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		JScrollPane scrollpane = new JScrollPane(table);

		// ������ �߰�
		ItemEvent event = new ItemEvent(comboBox, ItemEvent.ITEM_STATE_CHANGED, comboBox.getSelectedItem(), ItemEvent.SELECTED);
		itemStateChanged(event);

		return scrollpane;
	}

	private JPanel createTypePanel() {
		JPanel panel = new JPanel(new HorizontalLayout(10,10,10,0,0));
		comboBox = new JComboBox(registry.getStringArray("MigrationDialog.Type"));
		comboBox.setMaximumRowCount(13);
		comboBox.setActionCommand("MigrationType");
		comboBox.addItemListener(this);
		JButton uploadButton = new JButton("���ε�", registry.getImageIcon("Upload.ICON"));
		uploadButton.setActionCommand("UpLoad");
		uploadButton.addActionListener(this);
		JButton downloadButton = new JButton("�ٿ�ε�",  registry.getImageIcon("Download.ICON"));
		downloadButton.setActionCommand("DownLoad");
		downloadButton.addActionListener(this);
		JButton checkButton = new JButton("�˻�",  registry.getImageIcon("OK_16.ICON"));
		checkButton.setActionCommand("Check");
		checkButton.addActionListener(this);
		JButton stopButton = new JButton("����",  registry.getImageIcon("Cancel_16.ICON"));
		stopButton.setActionCommand("Stop");
		stopButton.addActionListener(this);
		checkBox = new JCheckBox("���� �߻��� ���");
		checkBox.setOpaque(false);
		checkBox.setSelected(true);
		JPanel buttonPanel = new JPanel(new ButtonLayout());
		buttonPanel.add(uploadButton);
		buttonPanel.add(downloadButton);
		buttonPanel.add(checkButton);
		buttonPanel.add(stopButton);
		panel.add("left.bind", new JLabel("Ÿ��"));
		panel.add("left.bind", comboBox);
		panel.add("left.bind", checkBox);
		panel.add("right.bind", buttonPanel);
		applyButton.setVisible(false);
		return panel;
	}

	protected void upload(File file) {
	}

	protected abstract boolean check();

	protected void clearTable() {
		for (int i = table.getRowCount() - 1; i >= 0; i--) {
			((DefaultTableModel)table.getModel()).removeRow(i);
		}
	}

	private JPanel createProgressPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setOpaque(false);
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setStringPainted(true);
		panel.add("unbound.bind", progressBar);
		return panel;
	}

	protected void setComponents() throws TCException{

	}


	@Override
	public void startCommandOperation() {
		try {
			operation = createOperation();
			operation.addOperationListener(this);
			session.queueOperation(operation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected AbstractAIFOperation createOperation() {
		return (AbstractAIFOperation)null;
	}

	//	public void applyPressed() {
	//		okOrApply = 1;
	//		startCommandOperation();
	//		initializeUI();
	//	}

	@Override
	public void startOperation(String string) {
		super.startOperation(string);
		validate();
	}

	@Override
	public void endOperation() {
		if(validateTimer != null)
			validateTimer.start();
		okButton.setVisible(false);
		cancelButton.setVisible(true);
		progressBar.setVisible(false);
		operation.removeOperationListener(this);
		session.setReadyStatus();
	}

	protected void postMessage(String message, String title, int type) {
		MessageBox.post(this, message, title, type);
	}

	public boolean showCloseButton() {
		return false;
	}

	public boolean isPerformable() {
		boolean flag = true;
		return flag;
	}

	public void stopPressed() {
	}

	private void setBackground(JComponent component) {
		component.setBackground(Color.WHITE);
		Component[] components = component.getComponents();
		for (int i = 0; i < components.length; i++) {
			// components[i].setBackground(Color.WHITE);
			if (components[i] instanceof JPanel || components[i] instanceof JTabbedPane) {
				setBackground((JComponent)components[i]);
			}
		}
	}

	public void actionPerformed(ActionEvent actionevent) {
		Object object = actionevent.getSource();
		if (object instanceof JButton) {
			JButton button = (JButton)object;
			String actionCommand = button.getActionCommand();
			buttonPerformed(actionCommand, button);
		}
	}

	protected void buttonPerformed(String actionCommand, JButton button) {
		if (actionCommand.equals("UpLoad")) {
			// ���� ��θ� Ȯ�� �Ͽ� null�� �ƴϸ� ������ ������ ��θ� ����.
			String strCookieDir = Utilities.getCookie("filechooser", "Chooser.DIR", true);
			if (strCookieDir == null) {
				strCookieDir = "";
			}
			JFileChooser fileChooser = new JFileChooser(strCookieDir);
			fileChooser.setFileFilter(new SimpleFilter("xls", "*.xls"));
			int status = fileChooser.showOpenDialog(MasterMigrationDialog.this);
			if (status == JFileChooser.APPROVE_OPTION) {
				try {
					// ���� ���õ� ������ ��θ� ��⿡ �� ����.
					String chooserDir = fileChooser.getSelectedFile().getAbsolutePath().substring(0, fileChooser.getSelectedFile().getAbsolutePath().lastIndexOf("\\"));
					Utilities.setCookie("filechooser", true, "Chooser.DIR", chooserDir);
					
					upload(fileChooser.getSelectedFile());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else if (actionCommand.equals("DownLoad")) {
			try {
				// ���� ��θ� Ȯ�� �Ͽ� null�� �ƴϸ� ������ ������ ��θ� ����.
				String strCookieDir = Utilities.getCookie("filechooser", "Chooser.DIR", true);
				if (strCookieDir == null) {
					strCookieDir = "";
				}
				JFileChooser fileChooser = new JFileChooser(strCookieDir);
				FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
				fileChooser.removeChoosableFileFilter(fileFilter);
				fileChooser.addChoosableFileFilter(new SimpleFilter("xls", "*.xls"));
				if (fileChooser.showSaveDialog(AIFUtility.getActiveDesktop()) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.exists()) {
						int decision = JOptionPane.showConfirmDialog(this, "������ ���� �մϴ� ���� ���ðڽ��ϱ�?", "�˸�", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (decision == JOptionPane.YES_OPTION) {
							file.delete();
						} else {
							return;
						}
					}
					// ���� ���õ� ������ ��θ� ��⿡ �� ����.
					String chooserDir = fileChooser.getSelectedFile().getAbsolutePath().substring(0, fileChooser.getSelectedFile().getAbsolutePath().lastIndexOf("\\"));
					Utilities.setCookie("filechooser", true, "Chooser.DIR", chooserDir);
					
					ExcelService.createService();
					ExcelService.downloadTable(file, table, columns, columnsWidth);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (actionCommand.equals("Check")) {
			if(table.getRowCount() > 0){
				new Thread(new Runnable()
				{
					public void run()
					{
						waitProgress = new WaitProgressBar(MasterMigrationDialog.this);
						try
						{
							waitProgress.start();
							waitProgress.setStatus("���ռ� �˻� ��...", true);

							if(check()){
								waitProgress.setStatus("�˻簡 �Ϸ� �Ǿ����ϴ�. ���ε带 ���� �� �� �ֽ��ϴ�.", false);
							}
							else{
								waitProgress.setStatus("�˻簡 �Ϸ� �Ǿ����ϴ�. ������ Ȯ�� �Ͻð�, ������ ������ �� �ٽ� ���ε带 �õ��� �ּ���.", false);
							}
							waitProgress.close("�˻� �Ϸ�", false, false);
						} catch(Exception ex)
						{
							waitProgress.setStatus(ex.getMessage());
							ex.printStackTrace();
							waitProgress.close("���� �߻�", true, true);
						}
					}
				}).start();
			}
			else{
				MessageBox.post(this, "���ε� ��ư�� Ŭ�� �Ͻð�, ���ε� �� EXCEL ������ ���� ������ �ּ���.", "�˸�", MessageBox.INFORMATION);
			}
		} else if (actionCommand.equals("Stop")) {
			if (operation != null) {
				((OperationAbortedListener)operation).operationAborted();
			}
		}
	}

	public void itemStateChanged(ItemEvent itemevent) {
		Object object = itemevent.getSource();
		if (object instanceof JComboBox) {
			if (itemevent.getStateChange() == itemevent.SELECTED) {
				JComboBox comboBox = (JComboBox)object;
				String actionCommand = comboBox.getActionCommand();
				comboBoxStateChanged(actionCommand, comboBox);
			}
		} else if (object instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox)object;
			String actionCommand = checkBox.getActionCommand();
			checkBoxStateChanged(actionCommand, checkBox);
		} else if (object instanceof JRadioButton) {
			if (itemevent.getStateChange() == itemevent.SELECTED) {
				JRadioButton radioButton = (JRadioButton)object;
				String actionCommand = radioButton.getActionCommand();
				radioButtonStateChanged(actionCommand, radioButton);
			}
		}
	}

	protected void comboBoxStateChanged(String actionCommand, JComboBox comboBox) {
	}

	protected void checkBoxStateChanged(String actionCommand, JCheckBox checkBox) {
	}

	protected void radioButtonStateChanged(String actionCommand, JRadioButton radioButton) {
	}

	public void mouseClicked(MouseEvent mouseEvent) {
		if (mouseEvent.getClickCount() == 2) {
			mouseDoubleClicked(mouseEvent);
		} else {
			mouseOneClicked(mouseEvent);
		}
	}

	public void mouseOneClicked(MouseEvent mouseEvent) {
	}

	public void mouseDoubleClicked(MouseEvent mouseEvent) {
	}

	public void mousePressed(MouseEvent mouseEvent) {
	}

	public void mouseReleased(MouseEvent mouseEvent) {
	}

	public void mouseEntered(MouseEvent mouseEvent) {
	}

	public void mouseExited(MouseEvent mouseEvent) {
	}
}