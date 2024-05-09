package com.kgm.commands.logview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;

import com.kgm.common.OnlyDateButton;
import com.kgm.common.SYMCAWTLabel;
import com.kgm.common.SYMCAWTTableModel;
import com.kgm.common.SYMCAWTTitledBorder;
import com.kgm.common.SYMCClass;
import com.kgm.common.SYMCInterfaceInfoPanel;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.randerer.IconColorCellRenderer2;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.ComponentService;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.ExcelService;
import com.kgm.common.utils.SimpleFilter;
import com.kgm.dto.DownDataSetData;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.rac.util.VerticalLayout;

@SuppressWarnings({"unused", "rawtypes"})
public class LogViewInfoPanel extends JPanel implements SYMCInterfaceInfoPanel, ActionListener {

	private static final long serialVersionUID = 1L;
	private JDialog dialog;
	private AIFComponentContext comp;
	private Registry registry = Registry.getRegistry(this);
    private TCSession session = CustomUtil.getTCSession();

	/** from_date onlyDateButton */
	private OnlyDateButton from_date = new OnlyDateButton(false);
	/** to_date onlyDateButton */
	private OnlyDateButton to_date = new OnlyDateButton(false);
	private JButton search_btn;
	private JButton download_btn;
	private JTable table;
	private SYMCAWTTableModel model;
	private String[] columnsWidth;

	private WaitProgressBar waitProgress;

	public LogViewInfoPanel(JDialog dialog, AIFComponentContext comp) {
		super(new VerticalLayout(5, 5, 5, 5, 5));

		this.dialog = dialog;
		this.comp = comp;

		initUI();

		/** ���� �����ͼ� �ٿ� ���� ���̺� ���� */
		downLogTableAdd();

		/** btn Event */
		btnEventAdd();
	}

	/**
	 * UI Panel.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2013. 1. 8.
	 */
	private void initUI() {
		setOpaque(false);

		JPanel mainPanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		mainPanel.setOpaque(false);

		JPanel topPanel = new JPanel(new PropertyLayout(5, 5));
		topPanel.setOpaque(false);
		topPanel.setBorder(new SYMCAWTTitledBorder("�˻� ���� �Է�"));

		JPanel btnPanel = new JPanel(new FlowLayout());
		btnPanel.setOpaque(false);
		btnPanel.setBorder(new SYMCAWTTitledBorder("���� ��ư"));

		JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		centerPanel.setOpaque(false);

		JPanel tablePanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		tablePanel.setOpaque(false);
		tablePanel.setBorder(new SYMCAWTTitledBorder("LOG ���� Ȯ��"));

		model = new SYMCAWTTableModel(registry.getStringArray("LogViewTable.HEADER"), 0);
		table = new JTable(model);

		columnsWidth = registry.getStringArray("LogViewTable.WIDTH");
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setCellRenderer(new IconColorCellRenderer2(null, new Color(230, 230, 230)));
			column.setPreferredWidth(Integer.parseInt(columnsWidth[i]));
		}

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setPreferredSize(new Dimension(350, 96));

		tablePanel.add("unbound.bind", scrollPane);

		topPanel.add("1.1.right.center.resizable.preferred", new SYMCAWTLabel("�ٿ� ��¥(����)"));
		topPanel.add("1.2.right.center.resizable.preferred", from_date);
		topPanel.add("2.1.right.center.resizable.preferred", new SYMCAWTLabel("�ٿ� ��¥(����)"));
		topPanel.add("2.2.right.center.resizable.preferred", to_date);

		search_btn = new JButton(registry.getImageIcon("Search.ICON"));
		btnPanel.add(search_btn);
		download_btn = new JButton(registry.getImageIcon("Excel.ICON"));
		btnPanel.add(download_btn);
		search_btn.setToolTipText("LOG �˻� ����.");
		download_btn.setToolTipText("Excle File Export.");
		
		centerPanel.add(topPanel);
		centerPanel.add(btnPanel);

		mainPanel.add("bound.bind", centerPanel);
		mainPanel.add("unbound.bind", tablePanel);

		add("unbound.bind", mainPanel);

		ComponentService.setComboboxSize(topPanel, 150, 22);
		ComponentService.setLabelSize(topPanel, 120, 22);
	}

	/**
	 * Down ���� DataSet Log Table Add.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2013. 1. 8.
	 */
	private void downLogTableAdd() {
		new Thread(new Runnable() {
			public void run() {
				waitProgress = new WaitProgressBar(dialog);
				waitProgress.start();
				waitProgress.setStatus("Log �˻� ��...", true);

				TCComponentDataset dataset = (TCComponentDataset) comp.getComponent();

				String uid = dataset.getUid();
				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("uid", uid);
				
				try {
					ArrayList list = (ArrayList)remote.execute("com.kgm.service.DownDataSetService", "downDataSetlogSelect", ds);

					if (list == null) {
						return;
					}

					int listSize = list.size();

					DownDataSetData data = null;
					String[] strs = new String[7];
					for (int i = 0; i < listSize; i++) {
						data = (DownDataSetData) list.get(i);

						// File TC Name,Down User,Down Date,Down DataSet Name,Create Date,Down
						// Path,UID
						strs[0] = data.getItem_id();
						strs[1] = data.getLogin_user();
						strs[2] = SYMCClass.DATE_FORMAT_MM.format(data.getDown_date());
						strs[3] = data.getDataset_name();
						strs[4] = SYMCClass.DATE_FORMAT_MM.format(data.getCreation_date());
						strs[5] = data.getDown_path();
						strs[6] = data.getDataset_uid();

						model.addRow(strs);
					}

					table.updateUI();
					
					waitProgress.close("�˻� �Ϸ�", false);
					waitProgress.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (waitProgress != null) {
						waitProgress.close("�˻� �Ϸ�", false);
						waitProgress.dispose();
					}
				}
			}
		}).start();
	}

	/**
	 * Button Event ó��.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2013. 1. 8.
	 */
	private void btnEventAdd() {
		search_btn.addActionListener(this);
		search_btn.setActionCommand("search_btn");

		download_btn.addActionListener(this);
		download_btn.setActionCommand("download_btn");
	}

	@Override
	public boolean validCheck() {
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String act = e.getActionCommand();
		if (act.equals("download_btn")) {
			/** Excel ���Ϸ� Log Download */
			excelFileLogDownload();
		} else if (act.equals("search_btn")) {
			// ����, ���� �ٿ� ��¥�� ���� �α� �˻�.
			downDataSetLogDateSelect();
		}
	}

	/**
	 * Excel File Log Download.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2013. 1. 9.
	 */
	private void excelFileLogDownload() {

		new Thread(new Runnable() {
			public void run() {
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
							int decision = JOptionPane.showConfirmDialog(dialog, "������ ���� �մϴ� ���� ���ðڽ��ϱ�?",
									"�˸�", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (decision == JOptionPane.YES_OPTION) {
								file.delete();
							} else {
								return;
							}
						}
						waitProgress = new WaitProgressBar(dialog);
						waitProgress.start();
						waitProgress.setStatus("Log File ���� ��...", true);
						
						// ���� ���õ� ������ ��θ� ��⿡ �� ����.
						String chooserDir = fileChooser.getSelectedFile().getAbsolutePath().substring(0,
								fileChooser.getSelectedFile().getAbsolutePath().lastIndexOf("\\"));
						Utilities.setCookie("filechooser", true, "Chooser.DIR", chooserDir);

						ExcelService.createService();
						ExcelService.downloadTable(file, table, registry
								.getStringArray("LogViewTable.HEADER"), registry
								.getStringArray("LogViewTable.WIDTH"));

						waitProgress.close("���� �Ϸ�", false);
						waitProgress.dispose();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					if (waitProgress != null) {
						waitProgress.close("���� �Ϸ�", false);
						waitProgress.dispose();
					}
				}
			}
		}).start();
	}

	/**
	 * ����, ���� �ٿ� ���� ��¥ ���� �α� �˻�.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2013. 1. 9.
	 */
	private void downDataSetLogDateSelect() {
		new Thread(new Runnable() {
			public void run() {
				waitProgress = new WaitProgressBar(dialog);
				waitProgress.start();
				waitProgress.setStatus("LOG �˻� ��...", true);

				TCComponentDataset dataset = (TCComponentDataset) comp.getComponent();

				String uid = dataset.getUid();

				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("dataset_uid", uid);
				ds.put("creation_date", from_date.getDate());
				ds.put("down_date", to_date.getDate());
				
				try {
					
					ArrayList list = (ArrayList)remote.execute("com.kgm.service.DownDataSetService", "downDataSetLogDateSelect", ds);

					if (list == null) {
						return;
					}

					table.removeAll();

					int rowCnt = model.getRowCount();
					for (int j = rowCnt - 1; j > -1; j--) {
						model.removeRow(j);
					}

					int listSize = list.size();

					DownDataSetData data = null;
					String[] strs = new String[7];
					for (int i = 0; i < listSize; i++) {
						data = (DownDataSetData) list.get(i);

						// File TC Name,Down User,Down Date,Down DataSet Name,Create Date,Down
						// Path,UID
						strs[0] = data.getItem_id();
						strs[1] = data.getLogin_user();
						strs[2] = SYMCClass.DATE_FORMAT_MM.format(data.getDown_date());
						strs[3] = data.getDataset_name();
						strs[4] = SYMCClass.DATE_FORMAT_MM.format(data.getCreation_date());
						strs[5] = data.getDown_path();
						strs[6] = data.getDataset_uid();

						model.addRow(strs);
					}

					table.updateUI();

					waitProgress.close("�˻� �Ϸ�", false);
					waitProgress.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (waitProgress != null) {
						waitProgress.close("�˻� �Ϸ�", false);
						waitProgress.dispose();
					}
				}
			}
		}).start();
	}
}
