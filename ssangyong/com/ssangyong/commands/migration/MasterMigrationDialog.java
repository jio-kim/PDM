package com.ssangyong.commands.migration;

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

import com.ssangyong.common.OperationAbortedListener;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.ExcelService;
import com.ssangyong.common.utils.SimpleFilter;
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
		session.setStatus("마이그레이션 준비...");
		initUI();
		setBackground((JComponent)parentPanel);
		session.setReadyStatus();
	}

	public void initUI() {
		super.initUI();
		try {
			setTitle("마이그레이션");
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
		mainPanel.add("top.bind", UIUtilities.createGradientHeader("마이그레이션 다이얼로그 입니다. 데이터 타입을 선택 하시고, 검사버튼을 클릭 하시어 정합성 검사를 먼저 실행 하세요.", registry.getImageIcon("Migration_Title.ICON"), 1));
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

		// 리스너 추가
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
		JButton uploadButton = new JButton("업로드", registry.getImageIcon("Upload.ICON"));
		uploadButton.setActionCommand("UpLoad");
		uploadButton.addActionListener(this);
		JButton downloadButton = new JButton("다운로드",  registry.getImageIcon("Download.ICON"));
		downloadButton.setActionCommand("DownLoad");
		downloadButton.addActionListener(this);
		JButton checkButton = new JButton("검사",  registry.getImageIcon("OK_16.ICON"));
		checkButton.setActionCommand("Check");
		checkButton.addActionListener(this);
		JButton stopButton = new JButton("중지",  registry.getImageIcon("Cancel_16.ICON"));
		stopButton.setActionCommand("Stop");
		stopButton.addActionListener(this);
		checkBox = new JCheckBox("오류 발생시 계속");
		checkBox.setOpaque(false);
		checkBox.setSelected(true);
		JPanel buttonPanel = new JPanel(new ButtonLayout());
		buttonPanel.add(uploadButton);
		buttonPanel.add(downloadButton);
		buttonPanel.add(checkButton);
		buttonPanel.add(stopButton);
		panel.add("left.bind", new JLabel("타입"));
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
			// 이전 경로를 확인 하여 null이 아니면 이전에 열었던 경로를 열음.
			String strCookieDir = Utilities.getCookie("filechooser", "Chooser.DIR", true);
			if (strCookieDir == null) {
				strCookieDir = "";
			}
			JFileChooser fileChooser = new JFileChooser(strCookieDir);
			fileChooser.setFileFilter(new SimpleFilter("xls", "*.xls"));
			int status = fileChooser.showOpenDialog(MasterMigrationDialog.this);
			if (status == JFileChooser.APPROVE_OPTION) {
				try {
					// 새로 선택된 파일의 경로를 쿠기에 재 지정.
					String chooserDir = fileChooser.getSelectedFile().getAbsolutePath().substring(0, fileChooser.getSelectedFile().getAbsolutePath().lastIndexOf("\\"));
					Utilities.setCookie("filechooser", true, "Chooser.DIR", chooserDir);
					
					upload(fileChooser.getSelectedFile());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else if (actionCommand.equals("DownLoad")) {
			try {
				// 이전 경로를 확인 하여 null이 아니면 이전에 열었던 경로를 열음.
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
						int decision = JOptionPane.showConfirmDialog(this, "파일이 존재 합니다 덮어 쓰시겠습니까?", "알림", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (decision == JOptionPane.YES_OPTION) {
							file.delete();
						} else {
							return;
						}
					}
					// 새로 선택된 파일의 경로를 쿠기에 재 지정.
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
							waitProgress.setStatus("정합성 검사 중...", true);

							if(check()){
								waitProgress.setStatus("검사가 완료 되었습니다. 업로드를 시작 할 수 있습니다.", false);
							}
							else{
								waitProgress.setStatus("검사가 완료 되었습니다. 에러를 확인 하시고, 파일을 수정한 후 다시 업로드를 시도해 주세요.", false);
							}
							waitProgress.close("검사 완료", false, false);
						} catch(Exception ex)
						{
							waitProgress.setStatus(ex.getMessage());
							ex.printStackTrace();
							waitProgress.close("에러 발생", true, true);
						}
					}
				}).start();
			}
			else{
				MessageBox.post(this, "업로드 버튼을 클릭 하시고, 업로드 할 EXCEL 파일을 먼저 선택해 주세요.", "알림", MessageBox.INFORMATION);
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