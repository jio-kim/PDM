package com.ssangyong.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DatasetService;
import com.ssangyong.common.utils.ExcelService;
import com.ssangyong.common.utils.Sorter;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.IRelationName;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;

/**
 * [SR170214-022][ljg] O/Code Check 컬럼 추가
 * [SR180110-016][ljg] Last Modify Date 컬럼 추가
 */
public class PngSpecFindDlg extends AbstractAIFDialog {
	private static final long serialVersionUID = 1L;
	private TCSession session;
	private final JPanel contentPanel = new JPanel();
	private PngDlg parentDlg;
	private JTable specTable = null;
	public static final String USER_SPEC = "User Spec";
	public static final String BUILD_SPEC = "Build Spec";
	public static final String PLAN_SPEC = "Production Plan 15 Spec";
	public static final String RESULT_SPEC = "Production Result 30 Spec";
	public static final String RESULT_SPEC_60 = "Production Result 60 Spec"; //[SR170810][LJG] 60일 연동 계획 Spec List 추가
	public static final String OSPEC_REVISION_QRY = "SYMC_Search_OspecSet_Revision";

	//[CF-2016] seho Column 위치가 잘못되어 있음. 6에서 7로 변경함.
	public static final int IDX_P_DATE = 7;

	//[SR170214-022][ljg] 사용중이지 않은 옵션코드를 보여주는 컬럼
	public static final int IDX_OPTION_CODE_CHECK = 8;

	private ArrayList<String> all_osepc_option_list; //[SR170214-022][ljg] ospec 엑셀 파일의 4번째 컬럼 의 모든 option 값을 담는 리스트
	private JTextField tfSpecNo;
	private JComboBox cbSpecType = null;
	private JComboBox cbProjectNo = null;
	private JComboBox ospec_revisions_combo = null; //[SR170214-022][ljg] OSpec의 리비전들을 보여주는 combobox, 디폴트는 최신 리비전이 선택됨
	private JTextField tfProjectNo = null;
	private String productID = null;

	private static final int START_ROW = 8; //[SR170214-022][ljg] OSpec excel 파일의 row수만큼 for문의 반복 하는데, 이때 시작 row 값
	private static final int OPTION_CODE_COLUMN_INDEX = 3; //[SR170214-022][ljg] OSpec excel 파일의 option code 를 표시하는 컬럼
	private WaitProgressBar bar;
	private JTextArea specNoTextArea;
	/**
	 * Create the dialog.
	 */
	public PngSpecFindDlg(PngDlg parentDlg, String productID) {
		super(parentDlg, true);
		setTitle("Spec Finder");
		this.parentDlg = parentDlg;
		this.productID = productID;
		this.session = CustomUtil.getTCSession();
		init();
		setPreferredSize(new Dimension(950,600));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void init(){

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new HorizontalLayout(5));

			JPanel panel_1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
			JPanel panel_2 = new JPanel(new FlowLayout(FlowLayout.LEADING));

			panel.add("left.bind.left.center", panel_1);
			panel.add("unbound.bind.right.center", panel_2);

			panel_1.setBorder(BorderFactory.createTitledBorder(""));
			panel_2.setBorder(BorderFactory.createTitledBorder(""));
			{
				cbSpecType = new JComboBox();
				cbSpecType.setModel(new DefaultComboBoxModel(new String[] {"Select a Type", USER_SPEC, BUILD_SPEC, PLAN_SPEC, RESULT_SPEC, RESULT_SPEC_60}));
				panel_1.add(cbSpecType);

				//[SR150416-025][2015.05.27][jclee] Build Spec 검색 시 Project No 검색조건 추가
				cbSpecType.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent paramItemEvent) {
						String sSelectedSpecType = (String)cbSpecType.getSelectedItem();
						if (sSelectedSpecType == null || sSelectedSpecType.equals("") || sSelectedSpecType.length() == 0) {
							return;
						}
						
						if (sSelectedSpecType.equals(BUILD_SPEC)) {
						
							cbProjectNo.setEnabled(true);
						
						} else {
							
							cbProjectNo.setEnabled(false);
						
						}
						
						// [SR180209-050][lahobeen] Version 컬럼  추가
						// USER SPEC에서는 Version이 없음
						if (sSelectedSpecType.equals(USER_SPEC)){ 
							
							if( specTable != null && specTable.getColumnModel() != null){
								TableColumn column = specTable.getColumnModel().getColumn(2);
							    column.setMinWidth(0);
							    column.setMaxWidth(0);
							    column.setWidth(0);
							    column.setPreferredWidth(0);
							    doLayout();
							}
							
							
							
						}else{
							
							if( specTable != null && specTable.getColumnModel() != null){
								final int width = 50;
								TableColumn column = specTable.getColumnModel().getColumn(2);
							    column.setMinWidth(50);
							    column.setMaxWidth(width);
							    column.setWidth(width);
							    column.setPreferredWidth(width);
							    doLayout();
							}
						}
					}
				});
			}
			{
				tfSpecNo = new JTextField();
				panel_1.add(tfSpecNo);
				tfSpecNo.setColumns(10);
			}
			{
				try {
					cbProjectNo = new JComboBox();
					tfProjectNo = (JTextField)cbProjectNo.getEditor().getEditorComponent();

					tfProjectNo.addKeyListener(new KeyAdapter() {
						@Override
						public void keyTyped(KeyEvent paramKeyEvent) {

							if (paramKeyEvent.getKeyChar() == '\b') {
								return;
							}

							int iCount = cbProjectNo.getItemCount();
							String sTyped = tfProjectNo.getText();
							if (sTyped == null || sTyped.equals("")) {
								return;
							}

							sTyped = sTyped.toUpperCase();

							for (int inx = 0; inx < iCount; inx++) {
								Object oItem = cbProjectNo.getItemAt(inx);

								if (oItem == null) {
									continue;
								}

								String sItem = oItem.toString();
								if (!oItem.toString().equals("")) {
									if (sItem.startsWith(sTyped)) {
										int iSelectionStart = tfProjectNo.getSelectionStart();

										cbProjectNo.setSelectedIndex(inx);
										tfProjectNo.setText(cbProjectNo.getSelectedItem().toString());
										tfProjectNo.setSelectionStart(iSelectionStart);
										tfProjectNo.setSelectionEnd(tfProjectNo.getText().length());

										break;
									}
								}
							}
						}
					});

					String[] saProjectNo = CustomUtil.getLOVDisplayValues(session, "S7_PROJECT_CODE");
					ArrayList<String> alProjectNo = new ArrayList<String>();
					alProjectNo.add("");
					for (int inx = 0; inx < saProjectNo.length; inx++) {
						alProjectNo.add(saProjectNo[inx]);
					}
					for (int inx = 0; inx < alProjectNo.size(); inx++) {
						cbProjectNo.addItem(alProjectNo.get(inx));
					}

					cbProjectNo.setEditable(true);
					cbProjectNo.setEnabled(false);

					panel_1.add(cbProjectNo);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			JButton btnSearch = new JButton("Search", new ImageIcon(PngSpecFindDlg.class.getResource("/icons/search_16.png")));
			panel_1.add(btnSearch);
			btnSearch.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent actionevent)
				{
					new Thread(new Runnable()
					{
						public void run()
						{
							bar = new WaitProgressBar(PngSpecFindDlg.this);
							bar.start();
							bar.setStatus("Spec Search...", true);
							bar.setAlwaysOnTop(true);
							try
							{
//								PngSpecFindDlg.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								getSpec();
//								PngSpecFindDlg.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								bar.setStatus("Complete", true);
							} catch (Exception e)
							{
								e.printStackTrace();
//								PngSpecFindDlg.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
							}finally
							{
								bar.close();
							}
						}
					}).start();
				}
			});

			final DefaultComboBoxModel ospec_revisions_combo_model = new DefaultComboBoxModel();
			ospec_revisions_combo = new JComboBox(ospec_revisions_combo_model);
			ospec_revisions_combo.setEditable(false);

			final JButton btnVerify = new JButton("Verify", new ImageIcon(PngSpecFindDlg.class.getResource("/icons/ok_16.png")));

			cbProjectNo.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent event) {
					if (event.getStateChange() == ItemEvent.SELECTED) {
						if(cbProjectNo.getSelectedIndex() > 0){
							if(cbProjectNo.getSelectedItem().toString().length() >= 4){
								try {
									TCComponent[] results = CustomUtil.queryComponent(OSPEC_REVISION_QRY, new String[]{"Project"}, new String[]{cbProjectNo.getSelectedItem().toString()});
									if(results == null || results.length <= 0){
										ospec_revisions_combo.setEnabled(false);
										btnVerify.setEnabled(false);
//										MessageBox.post(PngSpecFindDlg.this, "OSpec Item is Not Exist", "ERROR", MessageBox.ERROR);
									}
									else{
										ospec_revisions_combo.setEnabled(true);
										btnVerify.setEnabled(true);
										Sorter.getInstance().sortByString(results, "item_revision_id", true);
										for(int i=0; i<results.length; i++){
											ospec_revisions_combo_model.addElement((TCComponentItemRevision)results[i]);
										}
										ospec_revisions_combo.setSelectedIndex(ospec_revisions_combo_model.getSize() - 1);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								PngSpecFindDlg.this.pack();
							}else{
								ospec_revisions_combo.setEnabled(false);
								btnVerify.setEnabled(false);
								ospec_revisions_combo.removeAllItems();
							}
						}
					}
				}
			});
			ospec_revisions_combo.setEnabled(false);
			btnVerify.setEnabled(false);
			panel_2.add(ospec_revisions_combo);
			panel_2.add(btnVerify);
			btnVerify.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionevent) {
					try {
						new Thread(new Runnable(){
							public void run(){
								bar = new WaitProgressBar(PngSpecFindDlg.this);
								bar.start();
								bar.setStatus("Out of Code Verifying...", true);
								bar.setAlwaysOnTop(true);
								try {
									verify();
								} catch (Exception e) {
									e.printStackTrace();
								}
								bar.setStatus("Complete");
								bar.close("Verify Complete", false);
							}
						}).start();
					} catch (Exception e) {
						bar.close();
						e.printStackTrace();
						MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
					}
				}
			});

		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				Vector headerVec = new Vector();

				headerVec.add("No."); // [SR160503-014][20160503][jclee] Row No 추가
				headerVec.add("Spec");
				headerVec.add("Version"); //[SR180209-050][20180509][lahobeen] Version 추가
				headerVec.add("Type");
				headerVec.add("Create Date");
				headerVec.add("Last Modify Date"); //[SR180110-016][ljg] Last Modify Date 컬럼 추가
				headerVec.add("PUID");
				headerVec.add("PDate"); // [SR160324-027][20160329][jclee] Build Spec, 15 Plan Spec 에 대해 각각 최근 생산일자, 생산 예정일자 컬럼 추가
				headerVec.add("O/Code Check"); // [SR170214-022][ljg] 사용중이지 않은 옵션코드를 보여주는 컬럼

				DefaultTableModel model = new DefaultTableModel(null, headerVec){
					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};

				/**
				 * [SR150416-025][2015.05.27][jclee] Row No를 Tooltip으로 볼 수 있도록 수정
				 */
				specTable = new JTable(model) {
					public String getToolTipText(MouseEvent e) {
						String tip = null;
						java.awt.Point p = e.getPoint();
						int rowIndex = rowAtPoint(p);
						try {
							tip = String.valueOf(rowIndex + 1);
						} catch (RuntimeException e1) {
							tip = String.valueOf(-1);
						}

						return tip;
					}	
				};
				specTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

				/**
				 * [SR170214-022][ljg] 마우스 리스너 추가
				 */
				specTable.addMouseListener(new MouseAdapter() {

					@Override
					public void mousePressed(MouseEvent e) {
						if(e.getClickCount() == 2 && specTable.getSelectedColumn() == IDX_OPTION_CODE_CHECK){
							String value = specTable.getValueAt(specTable.getSelectedRow(), IDX_OPTION_CODE_CHECK).toString();
							StringViewerDialog dialog = new StringViewerDialog(new String[]{value});
							dialog.setVisible(true);

						}
					}
				});

				/**
				 * [SR150416-025][2015.05.27][jclee] 정렬 기능 추가
				 */
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
				specTable.setRowSorter(sorter);
				TableColumnModel tcm = specTable.getColumnModel();

				// [SR160503-014][20160503][jclee] Row No 추가
				// [SR170214-022][ljg] O/Code Check 컬럼 추가
				// [SR180110-016][ljg] Last Modify Date 컬럼 추가
				// [SR180209-050][lahobeen] Version 컬럼 추가
				int width[] = {50, 150, 50, 80, 150, 150, 0, 0, 130};

				for( int i = 0; i < tcm.getColumnCount(); i++){
					tcm.getColumn(i).setMinWidth(0);
					tcm.getColumn(i).setPreferredWidth(width[i]);
				}

				JScrollPane scrollPane = new JScrollPane(specTable);
				scrollPane.setBorder(BorderFactory.createTitledBorder(""));
				panel.add(scrollPane, BorderLayout.CENTER);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnSpecAdd = new JButton("Add");
				btnSpecAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						int[] rowIdxes = specTable.getSelectedRows();
						DefaultTableModel model = (DefaultTableModel)specTable.getModel();

						for( int i = 0; rowIdxes != null && i < rowIdxes.length; i++ ){
							Vector rowData = new Vector(){
								private static final long serialVersionUID = 1L;

								@Override
								public synchronized boolean equals(Object obj) {
									if( obj instanceof Vector){
										Vector target = (Vector)obj;
										if( this.elementCount != target.size()){
											return false;
										}

										for( int i = 0; i < this.elementCount; i++){
											if( !this.elementData[i].equals(target.get(i))){
												return false;
											}
										}

										return true;
									}
									return super.equals(obj);
								}

							};
							int modelIdx = specTable.convertRowIndexToModel(rowIdxes[i]);

							// [SR160503-014][20160503][jclee] Row No 추가로 인해 Column을 하나씩 밀어서 반영
							//	rowData.add(model.getValueAt(modelIdx, 0));
							//	rowData.add(model.getValueAt(modelIdx, 1));
							//	rowData.add(model.getValueAt(modelIdx, 2));
							//	rowData.add(model.getValueAt(modelIdx, 3));

							//[SR180110-016][ljg] Last Modify Date 컬럼 추가로 인한 컬럼 인덱스 변경
							//rowData.add(model.getValueAt(modelIdx, 1));
							//rowData.add(model.getValueAt(modelIdx, 2));
							//rowData.add(model.getValueAt(modelIdx, 3));
							//rowData.add(model.getValueAt(modelIdx, 4));
//							rowData.add(model.getValueAt(modelIdx, 1));
//							rowData.add(model.getValueAt(modelIdx, 2));
//							rowData.add(model.getValueAt(modelIdx, 3));
//							rowData.add(model.getValueAt(modelIdx, 5));
							// [SR180209-050][lahobeen] Version 컬럼 추가로 인한 컬럼 인덱스 변경
							rowData.add(model.getValueAt(modelIdx, 1));
							rowData.add(model.getValueAt(modelIdx, 3));
							rowData.add(model.getValueAt(modelIdx, 4));
							rowData.add(model.getValueAt(modelIdx, 6));
							parentDlg.addSpec(rowData);
						}

					}
				});
				btnSpecAdd.setActionCommand("");
				buttonPane.add(btnSpecAdd);
				getRootPane().setDefaultButton(btnSpecAdd);
			}
			{
				JButton btnClose = new JButton("Close");
				btnClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						dispose();
					}
				});
				btnClose.setActionCommand("");
				buttonPane.add(btnClose);
			}
		}
		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.setBorder(BorderFactory.createTitledBorder("검색조건 및 검색후 선택"));
		getContentPane().add(searchPanel, BorderLayout.EAST);
		JScrollPane jsp = new JScrollPane();
		specNoTextArea = new JTextArea();
		jsp.getViewport().add(specNoTextArea);
		jsp.setPreferredSize(new Dimension(150, 300));
//		specNoTextArea.setToolTipText(specNoTextArea.getText());
//		specNoTextArea.setBackground(Color.LIGHT_GRAY);
		searchPanel.add(jsp, BorderLayout.CENTER);
		JButton selectButton = new JButton("자동 선택");
		searchPanel.add(selectButton, BorderLayout.SOUTH);
		selectButton.setToolTipText("옆 TextArea에 엑셀에서 copy한 스펙 번호를 입력하고 자동 선택을 클릭하면 리스트에서 값을 자동으로 선택해준다.");
		selectButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionevent)
			{
				int count = 0;
				String specNos = specNoTextArea.getText();
				if(specNos.isEmpty() || specTable.getRowCount() == 0)
				{
					return;
				}
				specTable.clearSelection();
				String[] specNoArray = specNos.split("\n");
				for(int i=0;specNoArray != null && i<specNoArray.length;i++)
				{
					for(int row=0;row<specTable.getRowCount();row++)
					{
						if(specTable.getValueAt(row, 1).equals(specNoArray[i].trim()))
						{
							specTable.getSelectionModel().addSelectionInterval(row, row);
							specTable.scrollRectToVisible(new Rectangle(specTable.getCellRect(row, 0, true)));
							count++;
							break;
						}
					}
				}
				MessageBox.post(PngSpecFindDlg.this, count + "개의 Row가 선택되었습니다.", "선택", MessageBox.INFORMATION);
			}
		});

		// Table 정렬 시 Row No 재 입력
		specTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int iRowCount = specTable.getRowCount();

				for (int inx = 0; inx < iRowCount; inx++) {
					specTable.setValueAt(inx + 1, inx, 0);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void getSpec() throws Exception{
		String specType = (String)cbSpecType.getSelectedItem();
		String specNo = tfSpecNo.getText().trim();
		ArrayList<String> specConditionList = getSpecConditionList();
		String projectNo = tfProjectNo.getText();

		ArrayList<HashMap<String, String>> specList = null;
		if( specType.equals(USER_SPEC)){
			specList = getUserSpecList(specNo);
		}else if(specType.equals(BUILD_SPEC)){
			specList = getBuildSpecList(specNo, projectNo, specConditionList); //[SR150416-025][2015.05.27][jclee] Build Spec 검색 시 Project No 검색조건 추가
		}else if(specType.equals(PLAN_SPEC)){
			specList = getPlan15SpecList(specNo);
		}else if(specType.equals(RESULT_SPEC)){
			specList = getResult30SpecList(specNo);
		}else if(specType.equals(RESULT_SPEC_60)){
			specList = getResult60SpecList(specNo);
		}else{
			return;
		}

		if( specList == null){
			return;
		}

		parentDlg.removeAllRow(specTable);

		DefaultTableModel model = (DefaultTableModel)specTable.getModel();
		TableColumnModel columnModel = specTable.getColumnModel();

		int iRowNo = 0; // [SR160503-014][20160503][jclee] Row No 추가

		for( HashMap<String, String> map : specList){
			@SuppressWarnings("rawtypes")
			Vector rowData = new Vector();

			rowData.add(++iRowNo); // [SR160503-014][20160503][jclee] Row No 추가

			rowData.add(map.get("SPEC_NO"));
			rowData.add( ( map.get("VERSION") ==null?" ":map.get("VERSION") ) ); //[SR180209-050][lahobeen] Version 컬럼 추가
			rowData.add(map.get("TYPE"));
			rowData.add(map.get("CREATE_DATE"));
			rowData.add(map.get("LAST_MODIFY_DATE")); //[SR180110-016][ljg] Last Modify Date 컬럼 추가
			rowData.add(map.get("PUID"));

			// [SR160324-027][20160329][jclee]
			//[CF-2016] seho 화면에 날짜를 뿌려줄때 불필요한 시분초까지 나와서 제거하도록 함.
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			TableColumn cPDate = columnModel.getColumn(IDX_P_DATE);
			if (specType.equals(BUILD_SPEC)) {
				cPDate.setPreferredWidth(0);
				cPDate.setHeaderValue("Recent Production Date");
				rowData.add("");
			} else if (specType.equals(PLAN_SPEC)) {
				cPDate.setPreferredWidth(150);
				cPDate.setHeaderValue("Production Plan Date");
				Object date = map.get("PRODUCTION_PLAN_DATE");
				Date ts = (Date) date;
				rowData.add(sdf.format(ts));
			} else {
				cPDate.setPreferredWidth(0);
				cPDate.setHeaderValue("PDate");
				rowData.add("");
			}

			model.addRow(rowData);
		}
	}

	private ArrayList<HashMap<String, String>> getUserSpecList(String specNo) throws Exception{

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT", productID);
		ds.put("SPEC_NO", specNo);
		try {

			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> specList = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PartNameGroupService", "getUserSpecList", ds);
			return specList;

		} catch (Exception e) {
			throw e;
		}			
	}

	/**
	 * [SR150416-025][2015.05.27][jclee] Build Spec 검색 시 Project No 검색조건 추가
	 */
	private ArrayList<HashMap<String, String>> getBuildSpecList(String specNo, String projectNo, ArrayList<String> specConditionList) throws Exception{

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT", productID);
		ds.put("SPEC_NO", specNo.isEmpty()?null:specNo);
		ds.put("PROJECT_NO", projectNo != null ? projectNo.toUpperCase() : null);
		ds.put("specList", specConditionList);
		try {

			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> specList = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PartNameGroupService", "getBuildSpecList", ds);
			return specList;

		} catch (Exception e) {
			throw e;
		}	
	}

	private ArrayList<HashMap<String, String>> getPlan15SpecList(String specNo) throws Exception{

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT", productID);
		ds.put("SPEC_NO", specNo.isEmpty()?null:specNo);
		try {
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> specList = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PartNameGroupService", "getPlan15SpecList", ds);
			return specList;

		} catch (Exception e) {
			throw e;
		}	
	}

	private ArrayList<HashMap<String, String>> getResult30SpecList(String specNo) throws Exception{

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT", productID);
		ds.put("SPEC_NO", specNo.isEmpty()?null:specNo);
		try {

			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> specList = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PartNameGroupService", "getResult30SpecList", ds);
			return specList;

		} catch (Exception e) {
			throw e;
		}
	}

	//[SR170810][LJG] 60일 연동 계획 Spec List 추가
	private ArrayList<HashMap<String, String>> getResult60SpecList(String specNo) throws Exception{

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT", productID);
		ds.put("SPEC_NO", specNo.isEmpty()?null:specNo);
		try {

			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> specList = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PartNameGroupService", "getResult60SpecList", ds);
			return specList;

		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 주어진 Spec의 옵션 코드리스트를 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 18.
	 * @param specNo
	 * @param projectNo
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> getOptionCodeList(String specNo, String projectNo) throws Exception{

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("SPEC_NO", specNo);
		ds.put("PROJECT_NO", !CustomUtil.isNullString(projectNo) ? projectNo.toUpperCase() : "");
		try {

			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> specList = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PartNameGroupService", "getOptionCodeList", ds);
			return specList;

		} catch (Exception e) {
			throw e;
		}
	}


	/**
	 * 해당 프로젝트에서 선택한 OSPEC의 모든 Option에 대해서 각Build SPEC별 Option Set과 비교 하여, 없어진 OSPEC의 Option Code리스트를 보여 준다. 
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @throws Exception 
	 * @since  : 2017. 4. 7.
	 */
	private void verify() throws Exception{
		all_osepc_option_list = getAllOspecOptionList();
		ArrayList<String> build_spec_option_code_list = new ArrayList<String>(); // 쿼리 결과 값을 Hashmap으로 리턴되므로 String만 담기 위해서 사용하는 ArrayList

		for(int i=0; i<specTable.getRowCount(); i++){
			build_spec_option_code_list.clear();
			String query_condition = specTable.getValueAt(i, 1).toString();

			if(query_condition.trim().length() < 15){
				query_condition = "     " + query_condition;
			}
			bar.setStatus(String.valueOf(i+1) + "/" + specTable.getRowCount()+ query_condition + "...", true);
			ArrayList<HashMap<String, String>> option_code_query_list = getOptionCodeList(query_condition, cbProjectNo.getSelectedItem().toString());

			for(int j=0; j<option_code_query_list.size(); j++){
				build_spec_option_code_list.add(j, option_code_query_list.get(j).get("OPTION_NO"));
			}
			ArrayList<String> dead_code_list = getDeadCode(build_spec_option_code_list);
			specTable.setValueAt(getString(dead_code_list), i, IDX_OPTION_CODE_CHECK);
		}
	}

	/**
	 * All Ospec에는 존재하지않고 현재 스펙의 옵션리스트에는 존재하지 않는 옵션코드를 담기 위한 ArrayList
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 19.
	 */
	private ArrayList<String> getDeadCode(ArrayList<String> build_spec_option_code_list){

		ArrayList<String> dead_code_list = (ArrayList<String>) CollectionUtils.subtract(build_spec_option_code_list, all_osepc_option_list);
		return dead_code_list;
	}

	/**
	 * Excell의 D컬럼(4번째 컬럼)의 옵션 값들을 excel row 수 만큼 전부 담아서 리턴한다.
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 18.
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String> getAllOspecOptionList() throws Exception{
		TCComponentItemRevision revision = (TCComponentItemRevision)ospec_revisions_combo.getSelectedItem();
		File ospec_excel_file = getOSpecDataset(revision);
		ArrayList<String> osepc_all_option_list = new ArrayList<String>();
		ExcelService.createService();
		Workbook workbook = null;
		if (getExtension(ospec_excel_file).equalsIgnoreCase("xls")) {
			workbook = ExcelService.getHSSFWorkbook(ospec_excel_file);
		} else if (getExtension(ospec_excel_file).equalsIgnoreCase("xlsx")) {
			workbook = ExcelService.getXSSFWorkbook(ospec_excel_file);
		}
		Sheet sheet = workbook.getSheetAt(0);
		for (int i = START_ROW; i < sheet.getPhysicalNumberOfRows() + 1; i++) {
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(OPTION_CODE_COLUMN_INDEX);
			String string = getCellData(cell);
			if (!CustomUtil.isNullString(string) && !osepc_all_option_list.contains(string)) {
				osepc_all_option_list.add(string);
			}
		}

		return osepc_all_option_list;
	}

	/**
	 * 선택한 OSPEC 리비전 하위에 Dataset으로 붙어있는 OSPEC Excel 파일을 리턴한다.
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 7.
	 * @param revision
	 * @return excel file
	 * @throws Exception 
	 */
	private File getOSpecDataset(TCComponentItemRevision revision) throws Exception{
		AIFComponentContext[]	context = revision.getChildren(IRelationName.IMAN_reference);

		if(context == null || context.length <= 0){
			MessageBox.post(this, "OSpec Excel Dataset is Not Exist", "ERROR", MessageBox.ERROR);
			return null;
		}

		for(int i=0; i<context.length; i++){
			TCComponent component = (TCComponent)context[i].getComponent();
			if(component instanceof TCComponentDataset && (component.getType().equalsIgnoreCase("MSExcel") || component.getType().equalsIgnoreCase("MSExcelX"))){
				DatasetService.createService(session);
				return DatasetService.getFiles((TCComponentDataset)component)[0];
			}
		}

		MessageBox.post(this, "OSpec Excel Dataset is Not Exist", "ERROR", MessageBox.ERROR);
		return null;
	}

	/**
	 * 파일 확장자 가져오기
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 7.
	 * @param file
	 * @return
	 */
	private String getExtension(File file) {
		if (file.isDirectory())
			return null;
		String filename = file.getName();
		int i = filename.lastIndexOf(".");
		if (i > 0 && i < filename.length() - 1) {
			return filename.substring(i + 1).toLowerCase();
		}
		return null;
	}

	/**
	 * 엑셀 cell의 값을 string으로 리턴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 7.
	 * @param cell
	 * @return
	 */
	private String getCellData(Cell cell) {
		if (cell == null) {
			return new String();
		}
		if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			return cell.getCellFormula().trim();
		} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return String.valueOf(cell.getNumericCellValue()).trim();
		} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().trim();
		} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return new String();
		} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue()).trim();
		} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return String.valueOf(cell.getNumericCellValue()).trim();
		}
		return new String();
	}

	/**
	 * 인자로 받은 ArrayList를 "a,b,c,d,e....." 로 리턴한다
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 19.
	 * @param list
	 * @return
	 */
	private String getString(ArrayList<String> list){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<list.size(); i++){
			sb.append(list.get(i));
			if(i < list.size() - 1){
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private ArrayList<String> getSpecConditionList()
	{
		ArrayList<String> specConditionList = new ArrayList<String>();
		String specNos = specNoTextArea.getText();
		if(specNos.isEmpty())
		{
			return null;
		}
		String[] specNoArray = specNos.split("\n");
		for(String specNo : specNoArray)
		{
			specConditionList.add(specNo);
		}
		return specConditionList;
	}
}
