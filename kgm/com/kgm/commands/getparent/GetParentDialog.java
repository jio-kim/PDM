package com.kgm.commands.getparent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.ec.report.ExcelReportWithPoi;
import com.kgm.common.SortListenerFactory;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20160816][ymjang][SR160719-029] Get Parent 오류 개선
 * [20160919][ymjang][SR160906-034] Get Parent 오류 개선 - Parent Part 가 삭제된 경우 : 회색 음영 표시(Part application과 동일)
 * [20160919][ymjang][SR160906-034] Get Parent 오류 개선 - Dialog ScrollBar 중복 오류 현상 개선
 * [20160919][ymjang][SR160906-034] Get Parent 오류 개선 - Parent 정렬 순서 수정
 * [20161026][ymjang] Parent 정렬 순서 조정 (P1 > P2 > P3 ...와 같이 바로 상위의 Parent로 우선 정렬 (ex.4072021000)) 
 * [20161026][ymjang] Search 버튼 클릭시 최신 리비전 찾기 오류 수정
 * [20161111][ymjang] Parent 정렬 순서 조정 Project 별로 P1 > P2 > P3 순 정렬
 * [20161207][ymjang] Latest Released 조회시 FMP 표시 안됨.
 * [20161207][ymjang] FMP 정렬순서 오류 수정
 */
public class GetParentDialog extends Dialog {

    private TCSession session;
	
	private Button btnClose;
	private Button btnSearch;
	private Button btnExcel;
	
	private Text txtPartNo;
	private Text txtPartRev;
	
	private String sSelectedPartNo; 
	private String sSelectedPartRev; 
	
	private String sPartNo;
	private String sPartRev;
	private String sPartName;
	private String sPartReleased;
	private String sPartDeaded;
	
	private Table tblAll;
	private Table tblLatestReleased;
	private Table tblLatestWorking;
	
	private TabFolder tabFolder;
	private TabItem itemAll;
	private TabItem itemLatestReleased;
	private TabItem itemLatestWorking;
	
	private ArrayList<String> alDeadedParts = new ArrayList<String>();
	
	/**
	 * [20160919][ymjang][SR160906-034] Get Parent 오류 개선 - Dialog ScrollBar 중복 오류 현상 개선
	 * @param parent
	 * @param _selection
	 */
	public GetParentDialog(Shell parent, int _selection) {

		super(parent);
		
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);				
		
		this.session = CustomUtil.getTCSession();
		try {
			InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
			if (comps.length == 1) {
				InterfaceAIFComponent comp = comps[0];
				String sType = comp.getType();
				
				if (comp instanceof TCComponentItem && (sType.equals("S7_Vehpart") || sType.equals("S7_Stdpart"))) {
					TCComponentItem selectedItem = (TCComponentItem) comp;
					TCComponentItemRevision selectedItemLatestRevision = selectedItem.getLatestItemRevision();
					
					sSelectedPartNo = selectedItem.getProperty("item_id");
					sSelectedPartRev = selectedItemLatestRevision.getProperty("item_revision_id");
				} else if (comp instanceof TCComponentItemRevision && (sType.equals("S7_VehpartRevision") || sType.equals("S7_StdpartRevision"))) {
					TCComponentItemRevision selectedItemRevision = (TCComponentItemRevision) comp;
					
					sSelectedPartNo = selectedItemRevision.getProperty("item_id");
					sSelectedPartRev = selectedItemRevision.getProperty("item_revision_id");
				} else {
					sSelectedPartNo = "";
					sSelectedPartRev = "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 버튼 변경 */
	protected void createButtonsForButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		// Close Button Custom
		btnClose = new Button(composite, SWT.PUSH);
		btnClose.setText("Close");
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getShell().close();
			}
		});
	}

	/**
	 * [20160919][ymjang][SR160906-034] Get Parent 오류 개선 - Dialog ScrollBar 중복 오류 현상 개선
	 * Creating Dialog Panel
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Get Parent");
		
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Composite container = (Composite) super.createDialogArea(parent);

		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		container.setBackgroundMode(SWT.INHERIT_FORCE);

		GridLayout gl_container = new GridLayout(1, false);
		gl_container.marginWidth = 0;
		gl_container.marginHeight = 0;
		gl_container.verticalSpacing = 0;
		container.setLayout(gl_container);
		
		createSearchComposite(container);	// Search Composite
		createSearchResultTable(container);	// Search Result Composite
		setInit();
		
		return container;
	}

	/**
	 * 검색 조건 입력 부
	 * @param composite
	 */
	private void createSearchComposite(Composite parent) {
		GridLayout glSearch = new GridLayout(6, false);
		Composite cpsSearch = new Composite(parent, SWT.None);
		cpsSearch.setLayout(glSearch);
		cpsSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Part No
		CLabel lblPartNo = new CLabel(cpsSearch, SWT.RIGHT);
		lblPartNo.setText("Part No.");
		lblPartNo.setLayoutData(new GridData(80, SWT.DEFAULT));
		
		txtPartNo = new Text(cpsSearch, SWT.BORDER);
		txtPartNo.setLayoutData(new GridData(110, SWT.DEFAULT));
		txtPartNo.setTextLimit(12);
		txtPartNo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				if (keyEvent.keyCode == SWT.CR || keyEvent.keyCode == SWT.KEYPAD_CR) {
					try {
						String sPartNo = txtPartNo.getText();
						
						TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
						TCComponentQuery query = (TCComponentQuery) queryType.find("Item...");
						TCComponent[] cItems = query.execute(new String[] {"Item ID"}, new String[] {sPartNo});
						
						if (cItems.length == 1) {
							TCComponent cItem = cItems[0];
							if (cItem instanceof TCComponentItem) {
								TCComponentItem item = (TCComponentItem) cItem;
								String sPartRev = item.getLatestItemRevision().getProperty("item_revision_id");
								txtPartRev.setText(sPartRev);
								
								search();
							}
						} else {
							MessageBox.post("This part is not exist.\nPlease check part no.", "Error", MessageBox.ERROR);
							return;
						}
						
					} catch (Exception e) {
						e.printStackTrace();
						MessageBox.post(e);
					}
				}
			}
		});
		
		// Part Rev No
		CLabel lblPartRev = new CLabel(cpsSearch, SWT.RIGHT);
		lblPartRev.setText("Part Rev.");
		lblPartRev.setLayoutData(new GridData(80, SWT.DEFAULT));
		
		txtPartRev = new Text(cpsSearch, SWT.BORDER);
		txtPartRev.setLayoutData(new GridData(50, SWT.DEFAULT));
		txtPartRev.setTextLimit(3);
		txtPartRev.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				if (keyEvent.keyCode == SWT.CR || keyEvent.keyCode == SWT.KEYPAD_CR) {
					String sPartRev = txtPartRev.getText();
					if (sPartRev == null || sPartRev.equals("") || sPartRev.length() == 0) {
						MessageBox.post("This part revision is not exist.\nPlease check part no or revision no.", "Error", MessageBox.ERROR);
						return;
					}
					
					search();
				}
			}
		});
		
		// Search Button
		GridData gdSearch = new GridData(50, SWT.DEFAULT);
		gdSearch.horizontalAlignment = SWT.RIGHT;
		gdSearch.grabExcessHorizontalSpace = true;
		btnSearch = new Button(cpsSearch, SWT.PUSH);
		btnSearch.setText("Search");
		btnSearch.setLayoutData(gdSearch);
		btnSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				search();
			}
		});
		
		// Excel Export Button
		GridData gdExcel = new GridData(50, SWT.DEFAULT);
		gdExcel.horizontalAlignment = SWT.RIGHT;
		gdExcel.grabExcessHorizontalSpace = false;
		btnExcel = new Button(cpsSearch, SWT.PUSH);
		btnExcel.setText("Excel");
		btnExcel.setLayoutData(gdExcel);
		btnExcel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				exportExcel();
			}
		});

		Label lblSep1 = new Label(cpsSearch, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gdSep1 = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdSep1.horizontalSpan = 6;
		lblSep1.setLayoutData(gdSep1);

		Label lblSep2 = new Label(cpsSearch, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gdSep2 = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdSep2.horizontalSpan = 6;
		lblSep2.setLayoutData(gdSep2);
	}
	
	/**
	 * 결과 테이블
	 * @param composite
	 */
	private void createSearchResultTable(Composite parent) {
		tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// All Tab
		itemAll = new TabItem(tabFolder, SWT.NONE, 0);
		itemAll.setText("All");
		
		// Latest Released Tab
		itemLatestReleased = new TabItem(tabFolder, SWT.NONE, 1);
		itemLatestReleased.setText("Latest Released");
		
		// Latest Working Tab
		itemLatestWorking = new TabItem(tabFolder, SWT.NONE, 2);
		itemLatestWorking.setText("Latest Working");
		
		// [SR150415-024][2015.05.15][jclee] Default로 Latest Released Tab 선택
		tabFolder.setSelection(itemLatestReleased);
	}
	
	/**
	 * 선택한 Part의 Part No, Part Rev No 셋팅
	 */
	private void setInit() {
		if (!(sSelectedPartNo == null || sSelectedPartNo.isEmpty() || sSelectedPartNo.length() == 0)) {
			txtPartNo.setText(sSelectedPartNo);
		}
		
		if (!(sSelectedPartRev == null || sSelectedPartRev.isEmpty() || sSelectedPartRev.length() == 0)) {
			txtPartRev.setText(sSelectedPartRev);
		}
	}
	
	/**
	 * 검색
	 */
	private void search() {
		try {
			GetParentDao dao = new GetParentDao();
			
			sPartNo = txtPartNo.getText();
			sPartRev = txtPartRev.getText();
			
			TCComponentItem item = CustomUtil.findItem("Item", sPartNo);
			TCComponentItemRevision itemRevision = item.getLatestItemRevision();
			
			// [20161026][ymjang] Search 버튼 클릭시 최신 리비전 찾기 오류 수정
			if (sPartRev == null || sPartRev.equals("")) {
				sPartRev = itemRevision.getProperty("item_revision_id");
				txtPartRev.setText(sPartRev);
			}
			
			sPartName = item.getProperty("object_name");
			sPartReleased = itemRevision.getProperty("S7_Maturity").equals("Released") ? "Y" : "N";
			
			ArrayList<String> alTargetPartNo = new ArrayList<String>();
			alTargetPartNo.add(sPartNo);
			ArrayList<HashMap<String, String>> resultTargetPartDeaded = dao.isConnectedFunction(alTargetPartNo);
			ArrayList<String> alTargetPartDeaded = getDeadPartsList(resultTargetPartDeaded);
			sPartDeaded = alTargetPartDeaded != null && alTargetPartDeaded.size() > 0 ? "N" : "Y";
			
			// Query Result
			//[20160816][ymjang][SR160719-029] Get Parent 오류 개선
			ArrayList<HashMap<String, String>> queryResultAll = dao.searchUpperBOM(sPartNo, sPartRev, "ALL");
			ArrayList<HashMap<String, String>> queryResultLatestReleased = dao.searchUpperBOM(sPartNo, sPartRev, "1");
			ArrayList<HashMap<String, String>> queryResultLatestWorking = dao.searchUpperBOM(sPartNo, sPartRev, "0");			
//			ArrayList<HashMap<String, String>> queryResultAll = dao.searchAll(sPartNo, sPartRev);
//			ArrayList<HashMap<String, String>> queryResultLatestReleased = dao.searchLatestReleased(sPartNo, sPartRev);
//			ArrayList<HashMap<String, String>> queryResultLatestWorking = dao.searchLatestWorking(sPartNo, sPartRev);
			
			alDeadedParts = getDeadPartsList(queryResultAll);	// Deaded Parts
			
			// Result For Table
			ArrayList<HashMap<String, String>> resultAll = getResultForTable(queryResultAll);
			ArrayList<HashMap<String, String>> resultLatestReleased = getResultForTable(queryResultLatestReleased);
			ArrayList<HashMap<String, String>> resultLatestWorking = getResultForTable(queryResultLatestWorking);
			
			// Column Count
			int iColumnCntAll = getColumnCnt(resultAll);
			int iColumnCntLatestReleased = getColumnCnt(resultLatestReleased);
			int iColumnCntLatestWorking = getColumnCnt(resultLatestWorking);
			
			// Initialize Tables
			initTables();
			
			// Setting Column Header
			setTableColumnHeader(iColumnCntAll, tblAll);
			setTableColumnHeader(iColumnCntLatestReleased, tblLatestReleased);
			setTableColumnHeader(iColumnCntLatestWorking, tblLatestWorking);
			
			// Setting Table Result
			setResult(tblAll, resultAll, iColumnCntAll);
			setResult(tblLatestReleased, resultLatestReleased, iColumnCntLatestReleased);
			setResult(tblLatestWorking, resultLatestWorking, iColumnCntLatestWorking);
			
			// Setting Table Cell Color
			setCellColor(tblAll, iColumnCntAll);
			setCellColor(tblLatestReleased, iColumnCntLatestReleased);
			setCellColor(tblLatestWorking, iColumnCntLatestWorking);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 쿼리 결과 중 BOM구성이 되지 않은 Part의 목록 반환.
	 * @param queryResultAll
	 * @return
	 */
	private ArrayList<String> getDeadPartsList(ArrayList<HashMap<String, String>> queryResultAll) {
		ArrayList<String> resultDeadParts = new ArrayList<String>();
		ArrayList<String> searchTargetParts = new ArrayList<String>();

		if (queryResultAll == null || queryResultAll.size() == 0) {
			return null;
		}
		
		// DeadBOM Part 여부 확인 Query 발송 전 중복 Part 제거
		for (int inx = 0; inx < queryResultAll.size(); inx++) {
			HashMap<String, String> result = queryResultAll.get(inx);
			String sPartNo = result.get("PARENT_NO");
			boolean isContains = false;
			
			if (searchTargetParts.isEmpty() && inx == 0) {
				searchTargetParts.add(sPartNo);
			} else {
				for (int jnx = 0; jnx < searchTargetParts.size(); jnx++) {
					isContains = searchTargetParts.get(jnx).equals(sPartNo);
					
					if (isContains) {
						break;
					}
				}
				
				if (!isContains) {
					searchTargetParts.add(sPartNo);
				}
			}
		}
		
		if (searchTargetParts == null || searchTargetParts.size() == 0) {
			return null;
		}
		
		try {
			GetParentDao dao = new GetParentDao();
			ArrayList<HashMap<String, String>> results = dao.isConnectedFunction(searchTargetParts);
			
			if (results == null || results.size() == 0) {
				return null;
			}
			
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("PART_NO");
				String sPartDeaded = result.get("PART_DEADED");
				
				if (sPartDeaded != null && sPartDeaded.length() > 0 && sPartDeaded.equals("N")) {
					resultDeadParts.add(sPartNo);
				}
			}
			
			return resultDeadParts;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * [20160919][ymjang][SR160906-034] Get Parent 오류 개선 - Parent Part 가 삭제된 경우 : 회색 음영 표시(Part application과 동일)
	 * [20160919][ymjang][SR160906-034] Get Parent 오류 개선 - Parent 정렬 순서 수정
	 * Table에 넣을 수 있는 포맷으로 변경.
	 * 세로로 된 트리 구조의 결과를 가로로 펼쳐준다.
	 * @param queryResults
	 * @return
	 */
	@SuppressWarnings("unchecked")
    private ArrayList<HashMap<String, String>> getResultForTable(ArrayList<HashMap<String, String>> queryResults) {
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> result = new HashMap<String, String>();		
		
		String sTempLv = "";
		String sSortKey = "";
		String sProjectNoForSorting = "";
		for (int inx = 0; inx < queryResults.size(); inx++) {
			HashMap<String, String> queryResult = queryResults.get(inx);
			
			String sLevel = String.valueOf(queryResult.get("LV"));
			String sParentNo = queryResult.get("PARENT_NO");
			String sParentRev = queryResult.get("PARENT_REV");
			String sParentReleased = queryResult.get("PARENT_RELEASED");
			String sPlife = queryResult.get("P_LIFE");
			String sProjectNo = queryResult.get("PROJECT_NO");
			String sParentRetType = queryResult.get("PARENT_REV_TYPE");
			
			if (sParentRetType.equals("S7_FunctionMastRevision")) {
				sProjectNoForSorting = sProjectNo;
			}
			
			boolean isContains = false;
			
			String sParentDeaded = "N";
			
			String sPartNoColumnName = "LV" + sLevel + "_PART_NO";
			String sPartRevColumnName = "LV" + sLevel + "_PART_REV";
			String sPartReleasedColumnName = "LV" + sLevel + "_PART_RELEASED";
			String sProjectColumnName = "LV" + sLevel + "_PROJECT_NO";
			String sPartDeadedColumnName = "LV" + sLevel + "_PART_DEADED";
			
			sSortKey = sParentNo + "_" + sParentRev +  ":";
			
			if (!(!sTempLv.equals("") && (Integer.valueOf(sTempLv) + 1 == Integer.valueOf(sLevel))) && inx != 0) {
				result.put("TARGET_PART_NO", this.sPartNo);
				result.put("TARGET_PART_REV", this.sPartRev);
				result.put("TARGET_PART_RELEASED", sPartReleased);
				result.put("TARGET_PART_DEADED", sPartDeaded);
				
				results.add(result);
				
				HashMap<String, String> tmpResult = (HashMap<String, String>)result.clone();
				
				result = new HashMap<String, String>();
				
				int iLv = Integer.valueOf(sLevel);
				if (iLv > 1) {
					for (int jnx = 1; jnx < iLv; jnx++) {
						result.put("LV" + jnx + "_PART_NO", tmpResult.get("LV" + jnx + "_PART_NO"));
						result.put("LV" + jnx + "_PART_REV", tmpResult.get("LV" + jnx + "_PART_REV"));
						result.put("LV" + jnx + "_PART_RELEASED", tmpResult.get("LV" + jnx + "_PART_RELEASED"));
						result.put("LV" + jnx + "_PROJECT_NO", sProjectNoForSorting);
						result.put("LV" + jnx + "_PART_DEADED", tmpResult.get("LV" + jnx + "_PART_DEADED"));
					}
				}
			} else {
				result.put("LV" + sTempLv + "_PROJECT_NO", sProjectNoForSorting);
			}
			
			result.put(sPartNoColumnName, sParentNo);
			result.put(sPartRevColumnName, sParentRev);
			result.put(sPartReleasedColumnName, sParentReleased);
			result.put(sProjectColumnName, sProjectNoForSorting);
			
			for (int jnx = 1; jnx < Integer.valueOf(sLevel); jnx++) {
				//[20161111][ymjang] Parent 정렬 순서 조정 Project 별로 P1 > P2 > P3 순 정렬
				sSortKey = result.get("LV" + jnx + "_PROJECT_NO") + "_" + 
				           result.get("LV" + jnx + "_PART_NO") + "_" + 
						   result.get("LV" + jnx + "_PART_REV") +  ":" + 
				           //[20161207][ymjang] FMP 정렬순서 오류 수정
						   sSortKey.replaceAll( result.get("LV" + jnx + "_PROJECT_NO"), "") ;
				           //sSortKey ;
				//[20161026][ymjang] Parent 정렬 순서 조정 (P1 > P2 > P3 ...와 같이 바로 상위의 Parent로 우선 정렬 (ex.4072021000))
				//sSortKey = result.get("LV" + jnx + "_PART_NO") + "_" + result.get("LV" + jnx + "_PART_REV") +  ":" + sSortKey ;
				//sSortKey += result.get("LV" + jnx + "_PART_NO") + "_" + result.get("LV" + jnx + "_PART_REV") +  ":";
			}
			
			sParentDeaded = getPartDeaded(sParentNo);
			if (sParentDeaded != null && !sParentDeaded.equals("N")) {
				sParentDeaded = (sPlife != null && sPlife.equals("D")) ? "N" : "Y";
			}
			
			result.put(sPartDeadedColumnName, sParentDeaded);
			result.put("SORT_KEY", sSortKey);
			
			if (inx == queryResults.size() - 1) {
				result.put("TARGET_PART_NO", this.sPartNo);
				result.put("TARGET_PART_REV", this.sPartRev);
				result.put("TARGET_PART_RELEASED", sPartReleased);
				result.put("TARGET_PART_DEADED", sPartDeaded);
				
				results.add(result);
				
			} else {
				sTempLv = sLevel;	// 직전 Lv
			}
			
			sProjectNoForSorting = "";
		}
		
		// [20160919][ymjang][SR160906-034] Get Parent 오류 개선 - Parent 정렬 순서 수정
		// 정렬을 위하여 Sort Key 에 의한 Map 을 생성함.
		HashMap<String, HashMap<String, String>> notSortedMap = new HashMap<String, HashMap<String, String>>();		
		for(HashMap<String, String> resultMap : results) {
			String sortKey = resultMap.get("SORT_KEY");
			if (!notSortedMap.containsKey(sortKey)) {
				notSortedMap.put(sortKey, resultMap);
			}
		}
		
		// 위에서 생성된 Map을 Sort Key를 이용하여 정렬을 수행한 후, 다시 결과 집합을 생성한다. 
		results.clear();
		TreeMap<String, HashMap<String, String>> sortedMap = new TreeMap<String, HashMap<String, String>>( notSortedMap );
		for(Entry<String, HashMap<String, String>> sortKeys : sortedMap.entrySet()) {
			results.add(sortKeys.getValue());
		}
		
		return results;
	}

	/**
	 * Part가 BOM에 구성되어있는지 여부 확인
	 * @param sPartNo
	 * @return 구성되어 있을 경우 "Y" 안되어있을 경우 "N"
	 */
	private String getPartDeaded(String sPartNo) {
		if (alDeadedParts != null) {
			for (int inx = 0; inx < alDeadedParts.size(); inx++) {
				String sDeadedPartNo = alDeadedParts.get(inx);
				if (sDeadedPartNo.equals(sPartNo)) {
					return "N";
				}
			}
		}
		
		return "Y";
	}
	
	/**
	 * Initialize Tables
	 */
	private void initTables() {
		// Column이 가변적이므로 기존 Table을 Dispose 후 새로 생성
		if (tblAll != null) {
			tblAll.dispose();
		}
		if (tblLatestReleased != null) {
			tblLatestReleased.dispose();
		}
		if (tblLatestWorking != null) {
			tblLatestWorking.dispose();
		}
		
		// Contruct Table
		tblAll = new Table(tabFolder, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tblLatestReleased = new Table(tabFolder, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tblLatestWorking = new Table(tabFolder, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		
		tblAll.setHeaderVisible(true);
		tblAll.setLinesVisible(true);
		tblLatestReleased.setHeaderVisible(true);
		tblLatestReleased.setLinesVisible(true);
		tblLatestWorking.setHeaderVisible(true);
		tblLatestWorking.setLinesVisible(true);
		
		// Setting Tables to Tab
		itemAll.setControl(tblAll);
		itemLatestReleased.setControl(tblLatestReleased);
		itemLatestWorking.setControl(tblLatestWorking);
	}
	
	/**
	 * Setting Column Header
	 * @param iColumnCount
	 * @param tbl
	 */
	private void setTableColumnHeader(int iColumnCount, Table tbl) {
		for (int inx = 0; inx < iColumnCount; inx++) {
			TableColumn column = new TableColumn(tbl, SWT.NONE);
			// 0, 1번 Column은 Target Part No 이후 Column은 [Level No]Lv Part No, [Level No]Lv Part Rev로 Setting
			if (inx == 0) {
				column.setText("Target Part No");
			} else if (inx == 1) {
				column.setText("T/Rev");
			} else if (inx % 2 == 0) {
				column.setText("Parent Part" + (inx / 2));
			} else if (inx % 2 == 1) {
				column.setText("P" + (inx / 2) + "/Rev");
			}
			column.setWidth(inx % 2 == 0 ? 100 : 50);
			column.setAlignment(SWT.CENTER);
			column.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
		}
	}
	
	/**
	 * Setting Table Result
	 * @param tbl
	 * @param result
	 * @param iColumnCnt
	 */
	private void setResult(Table tbl, ArrayList<HashMap<String, String>> result, int iColumnCnt) {
		for (int inx = 0; inx < result.size(); inx++) {
			HashMap<String, String> hmResult = result.get(inx);
			TableItem item = new TableItem(tbl, SWT.NONE, inx);
			
			for (int jnx = 0; jnx < iColumnCnt; jnx++) {
				if (jnx == 0) {
					item.setText(jnx, hmResult.get("TARGET_PART_NO"));
				} else if (jnx == 1) {
					item.setText(jnx, hmResult.get("TARGET_PART_REV"));
				} else if (jnx % 2 == 0) {
					String sValue = hmResult.get("LV" + (jnx / 2) + "_PART_NO");
					sValue = getNullToString(sValue);
					item.setText(jnx, sValue);
				} else if (jnx % 2 == 1) {
					String sValue = hmResult.get("LV" + (jnx / 2) + "_PART_REV");
					sValue = getNullToString(sValue);
					item.setText(jnx, sValue);
				}
				
				item.setData(hmResult);
			}
		}
		
		// [SR150415-024][2015.05.15][jclee] 조회 후 Latest Released Tab 선택
		tabFolder.setSelection(itemLatestReleased);
	}
	
	/**
	 * null -> "" 로 변경
	 * @param sValue
	 * @return
	 */
	private String getNullToString(String sValue) {
		if (sValue == null || sValue.length() == 0 || sValue.isEmpty()) {
			return "";
		} else {
			return sValue;
		}
	}
	
	/**
	 * Query Result Column Count(Be Validated Column Count)
	 *  * 유효한 컬럼 수 중 가장 큰 수를 최종 Column Count로 설정
	 * @param result
	 * @return
	 */
	private int getColumnCnt(ArrayList<HashMap<String, String>> result) {
		ArrayList<Integer> alColumnCnt = new ArrayList<Integer>();
		int iReturn = 0;
		for (int inx = 0; inx < result.size(); inx++) {
			HashMap<String, String> mapResult = result.get(inx);
			int iColumnCnt = 0;
			Iterator<String> it = mapResult.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String sResult = mapResult.get(key);
				//[20161207][ymjang] Latest Released 조회시 FMP 표시 안됨.
				if (key.contains("PART_NO") || key.contains("PART_REV")) {
					if (sResult.isEmpty() || sResult.equals("") || sResult.length() == 0) {
						break;
					}
					iColumnCnt++;
				}				
//				if (sResult.isEmpty() || sResult.equals("") || sResult.length() == 0) {
//					break;
//				}
//				iColumnCnt++;
			}
			
			//[20161207][ymjang] Latest Released 조회시 FMP 표시 안됨.
//			if (iColumnCnt > 0) {
//				// Part No, Part Rev, Part Released, Part Deaded 항이 항상 셋트로 나옴. 그러나 Table에는 Part No, Part Rev만 필요하므로 4개를 2개로 만들어주기 위한 계산식임.
//				iColumnCnt = iColumnCnt / 2;
//			}
			alColumnCnt.add(iColumnCnt);
		}
		
		if (alColumnCnt.size() > 0) {
			iReturn = Collections.max(alColumnCnt);	// 조회된 Row중 가장 큰 Column Count를 찾아 Return.
		} else {
			iReturn = 2;	// 조회 결과가 없어도 Target Part, Target Rev 자리는 기본으로 만들어주기 위해 2를 Return.
		}
		
		return iReturn;
	}
	
	/**
	 * Table Cell Color 설정
	 * @param tbl
	 * @param result
	 * @param iColumnCnt
	 */
	@SuppressWarnings("unchecked")
    private void setCellColor(Table tbl, int iColumnCnt) {
		Color cInWork = getShell().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
		Color cDeaded = getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY);
		TableItem[] tblItems = tbl.getItems();
		
		for (int inx = 0; inx < tblItems.length; inx++) {
			TableItem tblItem = tblItems[inx];
			boolean isLineAllDead = false;
			for (int jnx = 0; jnx < tbl.getColumnCount(); jnx++) {
				String sStyle = "NONE";
				HashMap<String, String> tblData = (HashMap<String, String>)tblItem.getData();
				String sIsReleased = "";
				String sIsDeaded = "";
				
				if (jnx == 0 || jnx == 1) {
					sIsReleased = tblData.get("TARGET_PART_RELEASED");
					sIsDeaded = tblData.get("TARGET_PART_DEADED");
				} else {
					sIsReleased = tblData.get("LV" + (jnx / 2) + "_PART_RELEASED");
					sIsDeaded = tblData.get("LV" + (jnx / 2) + "_PART_DEADED");
				}
				
				boolean isReleasedIsNull = sIsReleased == null || sIsReleased.isEmpty() || sIsReleased.length() == 0;
				boolean isDeadedIsNull = sIsDeaded == null || sIsDeaded.isEmpty() || sIsDeaded.length() == 0;
				
				if (isReleasedIsNull) {
					sStyle = "NONE";
				} else {
					if (sIsReleased.equals("Y") ? true : false) {
						sStyle = "NONE";
					} else {
						if (!isDeadedIsNull && sIsDeaded.equals("N") ? true : false) {
							// Dead BOM인 경우. Grey로 Cell 표시 
							sStyle = "GREY";
						} else {
							// Released가 되지 않은 경우. 즉, Working 상태이면 Yellow로 Cell 표시 
							sStyle = "YELLOW";
						}
					}
				}
				
				if (!isDeadedIsNull && sIsDeaded.equals("N") ? true : false) {
					// Working 상태인 경우라도 Dead BOM인 경우. Grey로 Cell 표시 
					sStyle = "GREY";
				}
				
				Color color = null;
				//isLineAllDead = false;
				if (sStyle.equals("NONE")) {
					continue;
				} else {
					if (sStyle.equals("YELLOW")) {
						color = cInWork;
					} else if (sStyle.equals("GREY")) {
						color = cDeaded;
						// [20160926][ymjang] 최상위가 Dead 이면 하위는 모두 Dead 처리함.
						isLineAllDead = true; 
					}
					tblItem.setBackground(jnx, color);
				}
			}
			
			// [20160926][ymjang] 최상위가 Dead 이면 하위는 모두 Dead 처리함.
			if (isLineAllDead) {
				for (int jnx = 0; jnx < tbl.getColumnCount(); jnx++) {
					tblItem.setBackground(jnx, cDeaded);
				}
			}
			
		}
	}

	/**
	 * Table 내용 Excel 출력
	 */
	private void exportExcel() {
		if (sPartNo.isEmpty() || sPartNo == null || sPartNo.length() == 0 ||
			sPartRev.isEmpty() || sPartRev == null || sPartRev.length() == 0) {
			return;
		}
		
		try {
			String sFileName = "C:\\Temp\\" + sPartNo + "_" + sPartRev + "_Get_Parent_Report.xls";
			String[] saSheetNames = new String[] {sPartNo + "_" + sPartRev + "_All",
												  sPartNo + "_" + sPartRev + "_Latest_Released",
												  sPartNo + "_" + sPartRev + "_Latest_Working"}; 
			ExcelReportWithPoi ctrl = new ExcelReportWithPoi(saSheetNames);
			
			// Create Custom Cell Style
			createCellStyle(ctrl);
			
			// Setting Excel Header From Tables
	        setExcelPartInfo(ctrl, tblAll, 0);
	        setExcelPartInfo(ctrl, tblLatestReleased, 1);
	        setExcelPartInfo(ctrl, tblLatestWorking, 2);
	        
	        // Setting Excel Header From Tables
	        setExcelHeader(ctrl, tblAll, 0);
	        setExcelHeader(ctrl, tblLatestReleased, 1);
	        setExcelHeader(ctrl, tblLatestWorking, 2);
	        
	        // Setting Excel Rows From Tables
	        setExcelResult(ctrl, tblAll, 0);
	        setExcelResult(ctrl, tblLatestReleased, 1);
	        setExcelResult(ctrl, tblLatestWorking, 2);
	        
			// Create Excel File
	        final File reportFile = new File(sFileName);
	        if (reportFile.exists()) {
	        	reportFile.delete();
			}
	        
			ctrl.createExcelFile(sFileName);
				
			ctrl.distroy();
            ctrl = null;
            Runtime.getRuntime().gc();
            
            // File Open
			Thread thread = new Thread(new Runnable(){
				public void run(){
					try{
						String[] commandString = {"CMD", "/C", reportFile.getPath()};
						com.teamcenter.rac.util.Shell ishell = new com.teamcenter.rac.util.Shell(commandString);
						ishell.run();
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			});
			thread.start();
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create Custom Cell Style
	 * @param ctrl
	 */
	private void createCellStyle(ExcelReportWithPoi ctrl) {
        ctrl.createFont("11B_Black");
        ctrl.setBoldWeight("11B_Black", true);
        ctrl.setFontColor("11B_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("11B_Black", (short) 11);

        ctrl.createFont("9B_Black");
        ctrl.setBoldWeight("9B_Black", true);
        ctrl.setFontColor("9B_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("9B_Black", (short) 9);
        
        ctrl.createFont("9_Black");
        ctrl.setBoldWeight("9_Black", false);
        ctrl.setFontColor("9_Black", HSSFColor.BLACK.index);
        ctrl.setFontSize("9_Black", (short) 9);
        
		ctrl.createStyleWithFont("12B_Black_Left", "11B_Black");
		ctrl.setAlignment("12B_Black_Left", HSSFCellStyle.ALIGN_LEFT);
		
		ctrl.createStyleWithFont("Blue_T1B1L1R1_9B_Black_Center", "9B_Black");
		ctrl.setBorderStyle("Blue_T1B1L1R1_9B_Black_Center", 1, 1, 1, 1);
		ctrl.setCellColor("Blue_T1B1L1R1_9B_Black_Center", HSSFColor.SKY_BLUE.index);
		ctrl.setAlignment("Blue_T1B1L1R1_9B_Black_Center", HSSFCellStyle.ALIGN_CENTER);
		
		ctrl.createStyleWithFont("Grey_T1B1L1R1_9B_Black_Center", "9B_Black");
        ctrl.setBorderStyle("Grey_T1B1L1R1_9B_Black_Center", 1, 1, 1, 1);
        ctrl.setCellColor("Grey_T1B1L1R1_9B_Black_Center", HSSFColor.GREY_25_PERCENT.index);
        ctrl.setAlignment("Grey_T1B1L1R1_9B_Black_Center", HSSFCellStyle.ALIGN_CENTER);
        
        ctrl.createStyleWithFont("Grey_T1B1L1R1_9_Black_Center", "9_Black");
        ctrl.setBorderStyle("Grey_T1B1L1R1_9_Black_Center", 1, 1, 1, 1);
        ctrl.setCellColor("Grey_T1B1L1R1_9_Black_Center", HSSFColor.GREY_25_PERCENT.index);
        ctrl.setAlignment("Grey_T1B1L1R1_9_Black_Center", HSSFCellStyle.ALIGN_CENTER);

        ctrl.createStyleWithFont("T1B1L1R1_9_Black_Center", "9_Black");
        ctrl.setBorderStyle("T1B1L1R1_9_Black_Center", 1, 1, 1, 1);
        ctrl.setAlignment("T1B1L1R1_9_Black_Center", HSSFCellStyle.ALIGN_CENTER);
        
        ctrl.createStyleWithFont("YELLOW_T1B1L1R1_9_Black_Center", "9_Black");
        ctrl.setBorderStyle("YELLOW_T1B1L1R1_9_Black_Center", 1, 1, 1, 1);
        ctrl.setCellColor("YELLOW_T1B1L1R1_9_Black_Center", HSSFColor.YELLOW.index);
        ctrl.setAlignment("YELLOW_T1B1L1R1_9_Black_Center", HSSFCellStyle.ALIGN_CENTER);
	}
	
	/**
	 * Setting Excel Header From Tables
	 * @param ctrl
	 * @param tbl
	 * @param iSheetNum
	 */
	private void setExcelPartInfo(ExcelReportWithPoi ctrl, Table tbl, int iSheetNum) {
		int iColumnCnt = tbl.getColumnCount();
		ctrl.fillDataWithStyleAndMerge("12B_Black_Left", iSheetNum, 0, 0, 0, iColumnCnt - 1, sPartNo + "/" + sPartRev + "-" + sPartName);
		ctrl.setRowSizeByPixel(iSheetNum, 0, 40);
	}
	
	/**
	 * Setting Excel Header From Tables
	 * @param ctrl
	 * @param tbl
	 * @param iSheetNum
	 */
	private void setExcelHeader(ExcelReportWithPoi ctrl, Table tbl, int iSheetNum) {
		int iColumnCnt = tbl.getColumnCount();
		for (int jnx = 0; jnx < iColumnCnt; jnx++) {
			ctrl.fillDataWithStyle("Blue_T1B1L1R1_9B_Black_Center", iSheetNum, 1, jnx, tbl.getColumn(jnx).getText());
		}
	}
	
	/**
	 * Setting Excel Result From Tables
	 * @param ctrl
	 * @param tbl
	 * @param iSheetNum
	 */
	@SuppressWarnings("unchecked")
	private void setExcelResult(ExcelReportWithPoi ctrl, Table tbl, int iSheetNum) {
		TableItem[] tblItems = tbl.getItems();
		for (int inx = 0; inx < tblItems.length; inx++) {
			TableItem tblItem = tblItems[inx];
			for (int jnx = 0; jnx < tbl.getColumnCount(); jnx++) {
				String sValue = tblItem.getText(jnx);
				HashMap<String, String> tblData = (HashMap<String, String>)tblItem.getData();
				String sStyle = "T1B1L1R1_9_Black_Center";
				String sIsReleased = "";
				String sIsDeaded = "";
				
				if (jnx == 0 || jnx == 1) {
					sIsReleased = tblData.get("TARGET_PART_RELEASED");
					sIsDeaded = tblData.get("TARGET_PART_DEADED");
				} else {
					sIsReleased = tblData.get("LV" + (jnx / 2) + "_PART_RELEASED");
					sIsDeaded = tblData.get("LV" + (jnx / 2) + "_PART_DEADED");
				}
				
				boolean isReleasedIsNull = sIsReleased == null || sIsReleased.isEmpty() || sIsReleased.length() == 0;
				boolean isDeadedIsNull = sIsDeaded == null || sIsDeaded.isEmpty() || sIsDeaded.length() == 0;
				
				if (isReleasedIsNull) {
					sStyle = "T1B1L1R1_9_Black_Center";
				} else {
					if (sIsReleased.equals("Y") ? true : false) {
						sStyle = "T1B1L1R1_9_Black_Center";
					} else {
						if (!isDeadedIsNull && sIsDeaded.equals("N") ? true : false) {
							// Dead BOM인 경우. Grey로 Cell 표시 
							sStyle = "Grey_T1B1L1R1_9_Black_Center";
						} else {
							// Released가 되지 않은 경우. 즉, Working 상태이면 Yellow로 Cell 표시 
							sStyle = "YELLOW_T1B1L1R1_9_Black_Center";
						}
					}
				}
				
				if (!isDeadedIsNull && sIsDeaded.equals("N") ? true : false) {
					// Working 상태인 경우라도 Dead BOM인 경우. Grey로 Cell 표시 
					sStyle = "Grey_T1B1L1R1_9_Black_Center";
				}
				
				ctrl.fillDataWithStyle(sStyle, iSheetNum, inx + 2, jnx, sValue);
				ctrl.SetColWidthSettingByPixel(iSheetNum, jnx, jnx % 2 == 0 ? 100 : 50);	// Column Size
			}
		}
	}

    @Override
    protected Point getInitialSize() {
        return new Point(700, 600);
    }

}
