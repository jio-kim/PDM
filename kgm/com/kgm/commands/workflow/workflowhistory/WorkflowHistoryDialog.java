package com.kgm.commands.workflow.workflowhistory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.kgm.common.dialog.SYMCAWTAbstractDialog;
import com.kgm.common.randerer.IconColorCellRenderer2;
import com.kgm.common.utils.SimpleFilter;
import com.kgm.common.utils.TemplateParser;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.rac.util.VerticalLayout;

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class WorkflowHistoryDialog extends SYMCAWTAbstractDialog {
	
	/** */
	private static final long serialVersionUID = 1L;

	private TCSession session;
	private String className;
	private JPanel panel, processPanel;
	private JTable table;
	protected String[] columns, columnsWidth;
	private DefaultTableModel model;
	private Vector<InterfaceAIFComponent> processVector;
    private JComboBox processComboBox;
	private SimpleDateFormat simpleDateFormat;
	private Registry registry = Registry.getRegistry(this);

	public WorkflowHistoryDialog(Frame frame, TCComponent component) {
		super(frame, true);
		className = getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);
		session = (TCSession)component.getSession();
		processVector = new Vector<InterfaceAIFComponent>();
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			session.setStatus("Analyze");
			if (component instanceof TCComponentProcess) {
				processVector.addElement(component);
			} else if (component instanceof TCComponent) {
				AIFComponentContext[] context = component.whereReferenced();
				for (int i = 0; i < context.length; i++) {
					if (context[i].getComponent() instanceof TCComponentProcess)
						processVector.addElement(context[i].getComponent());
				}
			}
			session.setReadyStatus();
			initializeDialog();
			analyze(((TCComponentProcess)processVector.elementAt(0)).getRootTask());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeDialog() {
		setTitle(registry.getString("WorkflowHistoryDialog.Title"));
		super.createDialogUI("���� �̷��� ��ȸ�ϰ�, HTML�� Export�� �� �ֽ��ϴ�.", registry.getImageIcon("WorkflowHistoryDialogTitle.ICON"));
		applyButton.setVisible(false);
		okButton.setText(registry.getString("Export.TEXT"));
		okButton.setIcon(registry.getImageIcon("Export_24.ICON"));
		panel = new JPanel(new VerticalLayout(5,5,5,5,5));
		panel.setOpaque(false);
		processPanel = new JPanel(new HorizontalLayout());
		processPanel.setOpaque(false);
		processComboBox = new JComboBox(processVector);
		processComboBox.setPreferredSize(new Dimension(220,25));
		processPanel.add("right.bind.center.center", processComboBox);
		panel.add("top.bind.center.center", processPanel);
		panel.add("unbound", createTablePanel());
		add("unbound", panel);
		setPreferredSize(new Dimension(700,400));
	}

	protected JScrollPane createTablePanel() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setOpaque(false);
		columns = registry.getStringArray(className + ".TableColumn");
		model = new DefaultTableModel(columns, 0) {
			/** */
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		table = new JTable(model);
		table.setRowHeight(18);
		table.setCellSelectionEnabled(false);
//		table.setCellSelectionEnabled(true);
//		table.setSelectionBackground(new Color(135, 206, 235));// Sky Blue
		columnsWidth = registry.getStringArray(className + ".TableColumnWidth");
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setCellRenderer(new IconColorCellRenderer2(null, new Color(230,230,230)));
			column.setPreferredWidth(Integer.parseInt(columnsWidth[i]));
		}
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.getTableHeader().setReorderingAllowed(false);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(table);
		scrollPane.getViewport().setBackground(Color.white);
		return scrollPane;
	}

	public void analyze(TCComponentTask task) {
		try {
			task.refresh();
			if (task.getTaskType().equals("EPMTask") || task.getTaskType().equals("EPMReviewTask") || task.getTaskType().equals("EPMAcknowledgeTask")) {
				// TCProperty property = task.getProcess().getTCProperty("owning_user");
				// TCComponentUser user = (TCComponentUser)property.getReferenceValue();
				// model.addRow(new String[] {task.getName(), user.toString(), task.getTaskState(), task.getInstructions(), getDateInfomation(task, true)});
				TCComponentTask[] subTask = task.getSubtasks();
				if (subTask.length > 0) {
					for (int i = 0; i < subTask.length; i++)
						analyze(subTask[i]);
				}
			} else if (task.getTaskType().equals("EPMDoTask")) {
				TCComponentUser user = (TCComponentUser)task.getResponsibleParty();
				model.addRow(new String[] {task.getName(), user.toString(), getDateInfomation(task, true), task.getInstructions(), task.getTaskState()});
			} else if (task.getTaskType().equals("EPMPerformSignoffTask")) {
				TCComponentSignoff[] signoffs = task.getValidSignoffs();
				if (signoffs.length == 0) {
					model.addRow(new String[] {task.getParent().getName(), "����", "", "", task.getTaskState()});
				}
				for (int j = 0; j < signoffs.length; j++) {
					TCComponentSignoff signoff = signoffs[j];
					signoff.refresh();
					String state = task.getProperty("real_state");
					if (task.getTaskState().equals("Started")) {
						
						// 2024.01.09  ����  TCCRDecision.REJECT_DECISION ==>  signoff.getRejectDecision()
						if (signoff.getDecision().equals(signoff.getRejectDecision()))
							state = signoff.getDecision().toString();
					}
					TCComponentUser user = signoff.getGroupMember().getUser();
					model.addRow(new String[] {task.getParent().getName(), user.toString(), getDateInfomation(task, true), task.getValidSignoffs()[0].getComments(), task.getTaskState()});
				}
			} else if (task.getTaskType().equals("EPMAddStatusTask")) {
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public String[] getUserInfomation(TCComponentUser user) {
		try {
			TCComponentGroupMember[] groupMember = user.getGroupMembers();
			String[] userInformation = {user.toString(), groupMember[0].getGroup().toString(), groupMember[0].getRole().toString()};
			return userInformation;
		} catch (TCException e) {
			return null;
		}
	}

	public String getDateInfomation(TCComponentTask task, boolean isCreation) {
		String dateProperty = "";
		if (isCreation) {
			dateProperty = "creation_date";
		} else {
			dateProperty = "last_mod_date";
		}
		try {
			return simpleDateFormat.format(task.getDateProperty(dateProperty));
		} catch (TCException e) {
			return null;
		}
	}

	protected void clearTable() {
		DefaultTableModel tablemodel = (DefaultTableModel)table.getModel();
		while (tablemodel.getRowCount() > 0) {
			tablemodel.removeRow(0);
		}
	}

	public void itemStateChanged(ItemEvent itemevent) {
		if (itemevent.getStateChange() == ItemEvent.SELECTED) {
			TCComponentProcess process = (TCComponentProcess)processComboBox.getSelectedItem();
			try {
				clearTable();
				analyze(process.getRootTask());
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void invokeOperation(ActionEvent e) {
		session.queueOperation(new AbstractAIFOperation() {
			
			@Override
			public void executeOperation() throws Exception {
				TCComponentProcess process = (TCComponentProcess)processComboBox.getSelectedItem();
				try {
					// ���� ��θ� Ȯ�� �Ͽ� null�� �ƴϸ� ������ ������ ��θ� ����.
					String strCookieDir = Utilities.getCookie("filechooser", "Chooser.DIR", true);
					if (strCookieDir == null) {
						strCookieDir = "";
					}
					JFileChooser fileChooser = new JFileChooser(strCookieDir);
					FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
					fileChooser.removeChoosableFileFilter(fileFilter);
					fileChooser.addChoosableFileFilter(new SimpleFilter("html", "*.html"));
					String title = processComboBox.getSelectedItem().toString().replaceAll("/", "").replaceAll("\\\\", "").replaceAll(":", "").replaceAll("\\?", "").replaceAll("\"", "").replaceAll("<", "").replaceAll(">", "").replaceAll("|", "");
					fileChooser.setSelectedFile(new File(title +" [�����̷�]"+ ".html"));
					if (fileChooser.showSaveDialog(WorkflowHistoryDialog.this) == JFileChooser.APPROVE_OPTION) {
						File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
						if (file.exists()) {
							int decision = JOptionPane.showConfirmDialog(WorkflowHistoryDialog.this,"������ ���� �մϴ�. ����ðڽ��ϱ�?", "�˸�", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (decision == JOptionPane.YES_OPTION) {
								// ���� ���õ� ������ ��θ� ��⿡ �� ����.
								String chooserDir = fileChooser.getSelectedFile().getAbsolutePath().substring(0, fileChooser.getSelectedFile().getAbsolutePath().lastIndexOf("\\"));
								Utilities.setCookie("filechooser", true, "Chooser.DIR", chooserDir);
								file.delete();
							} else {
								return;
							}
						}
						TemplateParser parser = new TemplateParser();
						parser.parseWorkflow(process, file);
					}
				} catch (Exception ex) {
					ex.getStackTrace();
					MessageBox.post(ex);
				}
				
			}
		});
	}

	@Override
	public boolean validCheck() {
		return true;
	}

	@Override
	protected JPanel getUIPanel() {
		return panel;
	}
	
	@Override
	public boolean confirmCheck() {
		return true;
	}
}
