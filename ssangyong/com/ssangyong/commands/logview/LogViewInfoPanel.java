package com.ssangyong.commands.logview;

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

import com.ssangyong.common.OnlyDateButton;
import com.ssangyong.common.SYMCAWTLabel;
import com.ssangyong.common.SYMCAWTTableModel;
import com.ssangyong.common.SYMCAWTTitledBorder;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.SYMCInterfaceInfoPanel;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.randerer.IconColorCellRenderer2;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.ComponentService;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.ExcelService;
import com.ssangyong.common.utils.SimpleFilter;
import com.ssangyong.dto.DownDataSetData;
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

		/** 선택 데이터셋 다운 정보 테이블 셋팅 */
		downLogTableAdd();

		/** btn Event */
		btnEventAdd();
	}

	/**
	 * UI Panel.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 8.
	 */
	private void initUI() {
		setOpaque(false);

		JPanel mainPanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		mainPanel.setOpaque(false);

		JPanel topPanel = new JPanel(new PropertyLayout(5, 5));
		topPanel.setOpaque(false);
		topPanel.setBorder(new SYMCAWTTitledBorder("검색 정보 입력"));

		JPanel btnPanel = new JPanel(new FlowLayout());
		btnPanel.setOpaque(false);
		btnPanel.setBorder(new SYMCAWTTitledBorder("실행 버튼"));

		JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		centerPanel.setOpaque(false);

		JPanel tablePanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		tablePanel.setOpaque(false);
		tablePanel.setBorder(new SYMCAWTTitledBorder("LOG 정보 확인"));

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

		topPanel.add("1.1.right.center.resizable.preferred", new SYMCAWTLabel("다운 날짜(이후)"));
		topPanel.add("1.2.right.center.resizable.preferred", from_date);
		topPanel.add("2.1.right.center.resizable.preferred", new SYMCAWTLabel("다운 날짜(이전)"));
		topPanel.add("2.2.right.center.resizable.preferred", to_date);

		search_btn = new JButton(registry.getImageIcon("Search.ICON"));
		btnPanel.add(search_btn);
		download_btn = new JButton(registry.getImageIcon("Excel.ICON"));
		btnPanel.add(download_btn);
		search_btn.setToolTipText("LOG 검색 실행.");
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
	 * Down 받은 DataSet Log Table Add.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 8.
	 */
	private void downLogTableAdd() {
		new Thread(new Runnable() {
			public void run() {
				waitProgress = new WaitProgressBar(dialog);
				waitProgress.start();
				waitProgress.setStatus("Log 검색 중...", true);

				TCComponentDataset dataset = (TCComponentDataset) comp.getComponent();

				String uid = dataset.getUid();
				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("uid", uid);
				
				try {
					ArrayList list = (ArrayList)remote.execute("com.ssangyong.service.DownDataSetService", "downDataSetlogSelect", ds);

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
					
					waitProgress.close("검색 완료", false);
					waitProgress.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (waitProgress != null) {
						waitProgress.close("검색 완료", false);
						waitProgress.dispose();
					}
				}
			}
		}).start();
	}

	/**
	 * Button Event 처리.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
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
			/** Excel 파일로 Log Download */
			excelFileLogDownload();
		} else if (act.equals("search_btn")) {
			// 이전, 이후 다운 날짜에 따른 로그 검색.
			downDataSetLogDateSelect();
		}
	}

	/**
	 * Excel File Log Download.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 9.
	 */
	private void excelFileLogDownload() {

		new Thread(new Runnable() {
			public void run() {
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
							int decision = JOptionPane.showConfirmDialog(dialog, "파일이 존재 합니다 덮어 쓰시겠습니까?",
									"알림", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (decision == JOptionPane.YES_OPTION) {
								file.delete();
							} else {
								return;
							}
						}
						waitProgress = new WaitProgressBar(dialog);
						waitProgress.start();
						waitProgress.setStatus("Log File 생성 중...", true);
						
						// 새로 선택된 파일의 경로를 쿠기에 재 지정.
						String chooserDir = fileChooser.getSelectedFile().getAbsolutePath().substring(0,
								fileChooser.getSelectedFile().getAbsolutePath().lastIndexOf("\\"));
						Utilities.setCookie("filechooser", true, "Chooser.DIR", chooserDir);

						ExcelService.createService();
						ExcelService.downloadTable(file, table, registry
								.getStringArray("LogViewTable.HEADER"), registry
								.getStringArray("LogViewTable.WIDTH"));

						waitProgress.close("생성 완료", false);
						waitProgress.dispose();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					if (waitProgress != null) {
						waitProgress.close("생성 완료", false);
						waitProgress.dispose();
					}
				}
			}
		}).start();
	}

	/**
	 * 이전, 이후 다운 받은 날짜 단위 로그 검색.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 9.
	 */
	private void downDataSetLogDateSelect() {
		new Thread(new Runnable() {
			public void run() {
				waitProgress = new WaitProgressBar(dialog);
				waitProgress.start();
				waitProgress.setStatus("LOG 검색 중...", true);

				TCComponentDataset dataset = (TCComponentDataset) comp.getComponent();

				String uid = dataset.getUid();

				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("dataset_uid", uid);
				ds.put("creation_date", from_date.getDate());
				ds.put("down_date", to_date.getDate());
				
				try {
					
					ArrayList list = (ArrayList)remote.execute("com.ssangyong.service.DownDataSetService", "downDataSetLogDateSelect", ds);

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

					waitProgress.close("검색 완료", false);
					waitProgress.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (waitProgress != null) {
						waitProgress.close("검색 완료", false);
						waitProgress.dispose();
					}
				}
			}
		}).start();
	}
}
