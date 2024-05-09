package com.kgm.common.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.kgm.common.CustomTCTable;
import com.kgm.common.FunctionField;
import com.kgm.common.OnlyDateButton;
import com.kgm.common.SYMCClass;
import com.kgm.common.WaitProgressBar;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.SplitPane;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.iButton;

/**
 * 검색 결과를 보여주는 다이얼로그의 추상 클래스
 * 
 * @Copyright : S-PALM
 * @author : 이정건
 * @since : 2012. 6. 21. Package ID : sns.teamcenter.common.dialog.AbstractSearchResultDialog.java
 */
@SuppressWarnings({"rawtypes"})
public abstract class AbstractSearchResultDialog extends SYMCAWTAbstractDialog {

	private static final long serialVersionUID = 1L;

	protected Registry reg = Registry.getRegistry(this);

	/** 검색 결과 table */
	protected CustomTCTable table;

	/** Table Header */
	private String[] headers = new String[] { "object_string", "object_name", "object_desc", "owning_user" };

	/** table에서 선택한 component */
	protected TCComponent selectedComponents;

	/** 다이얼로그 타이틀 */
	private String title;

	/** 다이얼로그 헤더 메세지 */
	private String headerMessage;

	/** 검색조건 panel */
	protected JPanel conditionPanel;

	/** 검색 결과 리스트들 */
	protected TCComponent[] components = null;

	/** 검색 실행 버튼 */
	protected iButton searchButton;

	/** 검색조건 초기화 버튼 */
	protected iButton clearButton;

	/** 테이블 헤더 */
	private String[] header = null;

	/** 검색 조건의 entry 값들을 담는 배열 */
	protected String[] entry;

	/** 검색 조건의 Value 값들을 담는 배열 */
	protected String[] value;

	/** 검색 조건에 필요한 사용자가 입력한 값들만을 확인 하여 담는 ArrayList */
	protected ArrayList<String[]> entryValueArr;
	
	/** 최초 호출되는 것인지 체크 */
	protected Boolean isFirst = true;

	private JDialog dialog;
	private WaitProgressBar waitProgress;

	public AbstractSearchResultDialog(JDialog dialog, TCComponent[] searchResults, String title,
			String headerMessage, String[] header) {
		super(dialog, "", true);
		this.title = title;
		this.header = header;
		this.headerMessage = headerMessage;
		this.dialog = dialog;
		initilizeUI(searchResults);
	}

	public AbstractSearchResultDialog(JDialog dialog, String title, String headerMessage, String[] header) {
		super(dialog, "", true);
		this.title = title;
		this.header = header;
		this.headerMessage = headerMessage;
		this.dialog = dialog;
		initilizeUI(null);
	}

	public AbstractSearchResultDialog(TCComponent[] searchResults, String title, String headerMessage,
			String[] header) {
		super(AIFUtility.getActiveDesktop(), "", true);
		this.title = title;
		this.header = header;
		this.headerMessage = headerMessage;
		initilizeUI(searchResults);
	}

	public AbstractSearchResultDialog(String title, String headerMessage, String[] header) {
		super(AIFUtility.getActiveDesktop(), "", true);
		this.title = title;
		this.header = header;
		this.headerMessage = headerMessage;
		initilizeUI(null);
	}

	public AbstractSearchResultDialog(JDialog dialog, TCComponent[] searchResults, String title,
			String headerMessage) {
		super(dialog, "", true);
		this.title = title;
		this.headerMessage = headerMessage;
		this.dialog = dialog;
		initilizeUI(searchResults);
	}

	public AbstractSearchResultDialog(JDialog dialog, String title, String headerMessage) {
		super(dialog, "", true);
		this.title = title;
		this.header = headers;
		this.headerMessage = headerMessage;
		this.dialog = dialog;
		initilizeUI(null);
	}

	public AbstractSearchResultDialog(TCComponent[] searchResults, String title, String headerMessage) {
		super(AIFUtility.getActiveDesktop(), "", true);
		this.title = title;
		this.header = headers;
		this.headerMessage = headerMessage;
		initilizeUI(searchResults);
	}

	public AbstractSearchResultDialog(String title, String headerMessage) {
		super(AIFUtility.getActiveDesktop(), "", true);
		this.title = title;
		this.header = headers;
		this.headerMessage = headerMessage;
		initilizeUI(null);
	}

	/**
	 * UI 초기화
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2011. 4. 2.
	 */
	private void initilizeUI(TCComponent[] searchResults) {

		setTitle(title);
		createDialogUI(headerMessage, reg.getImageIcon("List_32.ICON"));

		searchButton = new iButton(reg.getImageIcon("Search.ICON"));
		searchButton.setToolTipText("검색을 실행 합니다.");
		searchButton.addActionListener(this);
		searchButton.setActionCommand("SEARCH");

		clearButton = new iButton(reg.getImageIcon("Clear.ICON"));
		clearButton.setToolTipText("검색 조건을 초기화 합니다.");
		clearButton.addActionListener(this);
		clearButton.setActionCommand("CLEAR");

		JPanel panel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		panel.setBackground(Color.white);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);

		buttonPanel.add(searchButton);
		buttonPanel.add(clearButton);

		panel.add("top.bind", buttonPanel);
		panel.add("top.bind", new Separator());
		panel.add("unbound.bind", getSearchConditionPanel());

		SplitPane splitPane = new SplitPane();
		splitPane.setLeftComponent(panel);
		splitPane.setRightComponent(getSearchResultPanel(searchResults));

		add("unbound.bind", splitPane);

		applyButton.setVisible(false);
		setPreferredSize(new Dimension(800, 500));

		addKeyEventListener(getUIPanel());

		/** 검색 결과 테이블 컬럼 사이즈 등록 */
		tableColumnSize();
	}

	/**
	 * 검색 조건을 추가하거나 변경 할 경우 하위의 상속받은 클래스에서 오버라이드 하여 구현 한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 19.
	 * @return 검색 조건 panel
	 */
	protected JPanel getSearchConditionPanel() {
		conditionPanel = new JPanel(new PropertyLayout(5, 5, 5, 5, 5, 5));
		conditionPanel.setOpaque(false);

		return conditionPanel;
	}

	/**
	 * 검색 결과가 보여지는 table panel
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 19.
	 * @param searchResults
	 * @return
	 */
	private JPanel getSearchResultPanel(TCComponent[] searchResults) {
		JPanel resultPanel = new JPanel(new VerticalLayout());
		resultPanel.setOpaque(false);

		table = new CustomTCTable(session, header);
		table.setAutoResizeMode(TCTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.setRowHeight(20);

		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setEditable(false);
		if (searchResults != null && searchResults.length != 0) {
			table.addRows(searchResults);
		}
		table.addMouseListener(this);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.getViewport().setBackground(Color.white);
		resultPanel.add("unbound.bind", scrollpane);
		return resultPanel;
	}

	public TCComponent getSelectedComponents() {
		return selectedComponents;
	}

	/**
	 * 하위에서 검색 실행 부분을 구현 한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 19.
	 */
	public abstract TCComponent[] search() throws Exception;

	/**
	 * 확인 버튼을 눌렀을때 처리
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 6. 21.
	 * @override
	 * @see SYMCAWTAbstractDialog.teamcenter.common.dialog.SpalmAbstractDialog#invokeOperation(java.awt.event.ActionEvent)
	 * @param e
	 */
	public abstract void invokeOperation(ActionEvent e);

	/**
	 * 전체 패널에서 해당하는 Component들의 실제 입력 받은 값이 있는 속성들만 체크한다. QueryBuilder에서 쿼리를 생성할때, 쿼리 entry 이름과
	 * JLabel의 이름을 똑같이 생성 하여야 한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 4. 20.
	 */
	private boolean componentPanelCheck(JPanel pan) {
		int size = pan.getComponentCount();
		for (int i = 0; i < size; i++) {
			Component component = pan.getComponent(i);
			if (component instanceof JPanel) {
				componentPanelCheck((JPanel) component);
			}
			if (component instanceof FunctionField) {
				if (((FunctionField) component).getText() != null
						&& !((FunctionField) component).getText().equals("")) {
					String entStr = ((JLabel) ((JPanel) component.getParent()).getComponent(i - 1)).getText();
					String value = ((FunctionField) component).getText();
					// String valStr = "";
					// String[] valStrs = value.split(";");
					// int strSize = valStrs.length;
					// if(strSize > 0){
					// valStr = valStrs[0];
					// }
					addEntryValueArr(entStr, "*" + value.toUpperCase() + "*");
				}
			} else if (component instanceof OnlyDateButton) {
				if (((OnlyDateButton) component).getDate() != null
						&& !((OnlyDateButton) component).getDateString().equals("")) {
					String entStr = ((JLabel) ((JPanel) component.getParent()).getComponent(i - 1)).getText();
					String valStr = ((OnlyDateButton) component).getDateString();
					addEntryValueArr(entStr, valStr);
				}
			} else if (component instanceof JComboBox) {
				if (((JComboBox) component).getSelectedItem() != null
						&& !((JComboBox) component).getSelectedItem().toString().equals("")) {
					String entStr = ((JLabel) ((JPanel) component.getParent()).getComponent(i - 1)).getText();
					String valStr = ((JComboBox) component).getSelectedItem().toString();
					addEntryValueArr(entStr, valStr);
				}
			}
		}
		return settingEntryValue();
	}

	@Override
	public boolean validCheck() {
		return true;
	}

	/**
	 * 모든 component들에 대하여 keyevent 등록
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 26.
	 * @param panel
	 */
	private void addKeyEventListener(JPanel panel) {
		int size = panel.getComponentCount();
		for (int i = 0; i < size; i++) {
			Component component = panel.getComponent(i);
			if (component instanceof JPanel) {
				((JPanel) component).addKeyListener(this);
				addKeyEventListener((JPanel) component);
			}
			if (component instanceof FunctionField) {
				((FunctionField) component).addKeyListener(this);
			} else if (component instanceof OnlyDateButton) {
				((OnlyDateButton) component).addKeyListener(this);
			} else if (component instanceof JComboBox) {
				((JComboBox) component).addKeyListener(this);
			}
		}
	}

	/**
	 * 검색 버튼이 눌렸을 경우 액션 처리
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 19.
	 * @override
	 * @see SYMCAWTAbstractDialog.teamcenter.common.dialog.SpalmAbstractDialog#actionPerformed(java.awt.event.ActionEvent)
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getActionCommand().equals("SEARCH")) {
				new Thread(new Runnable() {
					public void run() {
						try {
							waitProgress = new WaitProgressBar(dialog);
							waitProgress.start();
							waitProgress.setStatus("검색 중...", true);
							
							setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							entryValueArr = new ArrayList<String[]>();

							preSearchAction();

							if (componentPanelCheck(conditionPanel)) {
								components = search();
							} else {
//								MessageBox.post("최소한 하나의 검색 조건은 입력 하셔야 합니다.", "알림", MessageBox.INFORMATION);
								JOptionPane.showMessageDialog(dialog, "최소한 하나의 검색 조건은 입력 하셔야 합니다.",	"알림", JOptionPane.CLOSED_OPTION);
								setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								return;
							}

							table.clear();
							if (components == null || components.length == 0) {
								if(isFirst) {
//									MessageBox.post(dialog, "검색 결과가 없습니다.", "알림", MessageBox.INFORMATION);
									JOptionPane.showMessageDialog(dialog, "검색 결과가 없습니다.",	"알림", JOptionPane.CLOSED_OPTION);
									setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
									return;
								} else {
									setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
									return;
								}
								
							} else {
								if (components.length > 50) {
									int ok = JOptionPane
											.showConfirmDialog(
													dialog,
													"검색 결과가 50개가 넘습니다. 계속 진행 하시면 결과를 로딩 하는데 많은 시간이 소요 됩니다.\n 검색 조건을 수정 하셔서 재 검색 하시겠습니까? \n진행 하시려면 예(Y) 버튼을 누르세요.",
													"알림", JOptionPane.YES_NO_OPTION);
									if (ok != 0) {
										setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
										return;
									}
								}
								table.addRows(components);
							}

							setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							
							waitProgress.close("검색 완료", false);
							waitProgress.dispose();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							isFirst = true;
							if (waitProgress != null) {
								waitProgress.close("검색 완료", false);
								waitProgress.dispose();
							}
						}
					}
				}).start();
			} else {
				clear(conditionPanel);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			invokeOperation(null);
			super.cancelButtonClicked(null);
		}
	}

	/**
	 * 선택 된 값들만 EntryValueArrayList에 담음
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 4. 20.
	 * @param key
	 * @param value
	 */
	public void addEntryValueArr(String key, String value) {
		if (key.equals(SYMCClass.CISNOFIELD)) {
			key = SYMCClass.CISNOFIELD;
		}
		if (key.equals(SYMCClass.ITEMIDFIELD)) {
			// key = CustomUtil.getTextServerString(session, "k_find_itemid_name");
			key = "ItemID";
		} else if (key.equals(SYMCClass.NAMEFIELD)) {
			key = "Name";
		}

		entryValueArr.add(new String[] { key, value });
	}

	/**
	 * 사용자가 입력한 값들만 Entry[], Value[]에 담음 최소한 하나의 검색 조건은 입력되어야함. 그렇지 않으면 false리턴;
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 4. 20.
	 */
	private boolean settingEntryValue() {
		int arrSize = entryValueArr.size();

		if (entryValueArr == null || entryValueArr.size() == 0) {
			return false;
		}

		entry = new String[arrSize];
		value = new String[arrSize];

		for (int i = 0; i < arrSize; i++) {
			String[] strArr = (String[]) entryValueArr.get(i);
			entry[i] = strArr[0].toString();
			value[i] = strArr[1].toString();
		}

		return true;
	}

	/**
	 * 검색조건 초기화
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 24.
	 * @param panel
	 */
	private void clear(JPanel panel) {
		int size = panel.getComponentCount();
		for (int i = 0; i < size; i++) {
			Component component = panel.getComponent(i);
			if (component instanceof JPanel) {
				clear((JPanel) component);
			}
			if (component instanceof FunctionField) {
				((FunctionField) component).setText("");
			} else if (component instanceof OnlyDateButton) {
				((OnlyDateButton) component).setDate(null);
			} else if (component instanceof JComboBox) {
				((JComboBox) component).setSelectedIndex(0);
			}
		}
	}

	/**
	 * 검색 실행전 사전 작업이 있을경우 구현 한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 24.
	 */
	public abstract void preSearchAction();

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			searchButton.doClick();
		}
	}

	/**
	 * 검색 결과 출력 테이블 컬럼 사이즈 지정.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 7. 11.
	 */
	private void tableColumnSize() {
		int ss = table.getColumnCount();

		String[] sizeArr = new String[ss];

		for (int i = 0; i < ss; i++) {
			if (i == 0) {
				sizeArr[i] = "100";
			} else {
				sizeArr[i] = "30";
			}
		}
		table.setColumnWidths(sizeArr);
	}
}