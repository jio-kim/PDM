package com.symc.plm.rac.prebom.masterlist.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.PlainDocument;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpTrim;
import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.common.util.TextFieldFilter;
import com.symc.plm.rac.prebom.common.util.lov.LovCombo;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListConditionDlg;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListDlg;
import com.symc.plm.rac.prebom.masterlist.dialog.PartNameCreationDlg;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.util.WebUtil;
import com.symc.plm.rac.prebom.masterlist.view.HeaderPopupFilter.CheckBoxNode;
import com.symc.plm.rac.prebom.masterlist.view.celleditor.ComboCellEditor;
import com.symc.plm.rac.prebom.masterlist.view.celleditor.DateCellEditor;
import com.symc.plm.rac.prebom.masterlist.view.celleditor.PartConditionCellEditor;
import com.symc.plm.rac.prebom.masterlist.view.celleditor.PartDisplayIdCellEditor;
import com.symc.plm.rac.prebom.masterlist.view.celleditor.PartNameCellEditor;
import com.symc.plm.rac.prebom.masterlist.view.celleditor.RepQtyCellEditor;
import com.symc.plm.rac.prebom.masterlist.view.celleditor.UsageCellEditor;
import com.symc.plm.rac.prebom.masterlist.view.clipboard.TextTransfer;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20160602][ymjang] Part No 에 "-" 추가시 제거 후, Part Search 하도록 개선함.
 * [20160602][ymjang] Carry Over Part No 입력시 Display No 자동 입력
 * [20161031][ymjang] 옵션입력 Dialog 창에서 Cancel 버튼을 클릭할 경우, Usage 수량 초기화하지 않도록 수정
 * [20161111][ymjang] 하위가 이미 삭제된 뒤 상위 Assy 를 제거할 경우, 오류 Nullpointer Exception 오류 발생 개선
 * [20161111][ymajng] Paste시 index out of bound 오류 수정
 * [20161111][ymjang] Assy Remove 취소시 Validation 불필요 --> 주석 처리함.
 * [20170312][ymjang] SPEC Desc. 길이 제한 체크
 * [20170312][ymjang] DPV NEED QTY Number Formaat 이 아닐 경우, Validation 체크
 * [SR170703-020][LJG]Proto Tooling 컬럼 추가
 * [SR171227-049][LJG] N,M,C,D에서 M을 -> M1,M2,M3로 세분화
 * [20180109][LJG]NMCD가 C일경우에는 수정 하지 못하도록 - 송대영 차장 요청
 * [20180202][LJG]비정규 파트일 경우는 NMCD가 C일경우라도 수정 가능 하도록 - 송대영 차장 요청
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 * [SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
 * [CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)
 */
@SuppressWarnings({ "unchecked", "rawtypes", "serial", "unused" })
public class MasterListTablePanel extends JPanel {

	Vector dataHeader;
	Vector<Vector> data;
	DefaultTableModel model;
	DefaultTableModel fixedModel;
	TableRowSorter<TableModel> sorter = null;
	
	public static final int MASTER_LIST_PART_ID_IDX = 0;
	public static final int MASTER_LIST_CONTENTS_IDX = 1;
	public static final int MASTER_LIST_SYSTEM_IDX = 2;
	public static final int MASTER_LIST_SYSTEM_NAME_IDX = 3;
	public static final int MASTER_LIST_FUNCTION_IDX = 4;
	public static final int MASTER_LIST_LEV_MAN_IDX = 5;
	public static final int MASTER_LIST_LEV_A_IDX = 6;
	public static final int MASTER_LIST_SEQUENCE_IDX = 7;
	public static final int MASTER_LIST_PARENT_ID_IDX = 8;
	public static final int MASTER_LIST_OLD_PART_ID_IDX = 9;
	public static final int MASTER_LIST_PART_DISPLAY_ID_IDX = 10;
	public static final int MASTER_LIST_PART_NAME_IDX = 11;
	public static final int MASTER_LIST_REQ_OPT_IDX = 12;
	public static final int MASTER_LIST_SPEC_DESC_IDX = 13;
	public static final int MASTER_LIST_SPEC_IDX = 14;
	public static final int MASTER_LIST_NMCD_IDX = 15;
	public static final int MASTER_LIST_PROJECT_IDX = 16;
	public static final int MASTER_LIST_PROTO_TOOLING_IDX = 17; // [SR170703-020][LJG]Proto Tooling 컬럼 추가
	public static final int MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX = 18;
	
	
	public int MASTER_LIST_SUPPLY_MODE_IDX = -1;
	public int MASTER_LIST_EST_WEIGHT_IDX = -1;
	public int MASTER_LIST_TARGET_WEIGHT_IDX = -1; 
	// [CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)
	public int MASTER_LIST_WEIGHT_MANAGEMENT_IDX = -1; 		
	public int MASTER_LIST_MODULE_IDX = -1;
	public int MASTER_LIST_ALTER_PART_IDX = -1;
	public int MASTER_LIST_DR_IDX = -1;
	//20200914 seho EJS Column 추가.
	public int MASTER_LIST_EJS_IDX = -1;
	public int MASTER_LIST_RESPONSIBILITY_IDX = -1;
	public int MASTER_LIST_CHANGE_DESCRIPTION_IDX = -1;
	public int MASTER_LIST_EST_COST_MATERIAL_IDX = -1;
	public int MASTER_LIST_TARGET_COST_MATERIAL_IDX = -1;
	public int MASTER_LIST_DVP_NEEDED_QTY_IDX = -1;
	public int MASTER_LIST_DVP_USE_IDX = -1;
	public int MASTER_LIST_DVP_REQ_DEPT_IDX = -1;
	public int MASTER_LIST_CON_DWG_PERFORMANCE_IDX = -1;
	public int MASTER_LIST_CON_DWG_PLAN_IDX = -1;
	public int MASTER_LIST_CON_DWG_TYPE_IDX = -1;
	public int MASTER_LIST_DWG_DEPLOYABLE_DATE_IDX = -1; 
	public int MASTER_LIST_PRD_DWG_PERFORMANCE_IDX = -1;
	public int MASTER_LIST_PRD_DWG_PLAN_IDX = -1;
	public int MASTER_LIST_ECO_NO_IDX = -1;
	public int MASTER_LIST_OSPEC_NO_IDX = -1; //[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
	public int MASTER_LIST_DESIGN_DOC_NO_IDX = -1;
	public int MASTER_LIST_DESIGN_REL_DATE_IDX = -1;
	public int MASTER_LIST_ENG_DEPT_NM_IDX = -1;
	public int MASTER_LIST_ENG_RESPONSIBILITY_IDX = -1;	
	public int MASTER_LIST_SELECTED_COMPANY_IDX = -1;
	public int MASTER_LIST_PRT_TOOLG_INVESTMENT_IDX = -1;
	public int MASTER_LIST_PRD_TOOL_COST_IDX = -1;
	public int MASTER_LIST_PRD_SERVICE_COST_IDX = -1;
	public int MASTER_LIST_PRD_SAMPLE_COST_IDX = -1;
	public int MASTER_LIST_PRD_SUM_IDX = -1;
	public int MASTER_LIST_PUR_TEAM_IDX = -1;
	public int MASTER_LIST_PUR_RESPONSIBILITY_IDX = -1;
	
	static final String MASTER_LIST_STD = "STD";
	static final String MASTER_LIST_OPT = "OPT";
	public static final String MASTER_LIST_REPRESENTATIVE_QUANTITY = "REPRESENTATIVE_QUANTITY";
	public static final String MASTER_LIST_DCS_NO = "DCS_NO";
	public static final String MASTER_LIST_DCS_DATE = "DCS_DATE";
	
	int columnCount = 0;
	
	Object[] fixedColumnPre = null;
	Object[] fixedColumnPost = null;
	
	int[] fixedColumnPreWidth = null;
	int[] fixedColumnPostWidth = null;
	
	int[] copiedRows = null;
	int[] copiedColumns = null;
	JTable fixedTable, table;
	GroupableTableHeader header = null;
	Vector headerVector = new Vector();
	HeaderPopup headerPopup = null;
	HashMap<Integer, ArrayList<String>> filterMap = new HashMap();
	JScrollPane scroll = null;
	int fixColumn = 1;

	private int selectedColumn = -1;
	private OSpec ospec = null;
	private ArrayList currentPreColumnList = new ArrayList();
	private ArrayList currentPostColumnList = new ArrayList();
	private MasterListReq parentDlg = null;
	private PartNameCreationDlg partNameCreationDlg = null;
	private MasterListConditionDlg masterListConditionDlg = null;
	
	private boolean isCompareWithRelease = false;
	private HashMap<Integer, String> columnMap = new HashMap();
	
	private boolean isShowDeletedLine = false;
	
	private ArrayList<Integer> notEditableColumn = new ArrayList();
	
	private ArrayList<Integer> designerReqColumn = new ArrayList();
	/**
	 * Create the panel.
	 * 
	 * @throws Exception
	 */
	public MasterListTablePanel(MasterListReq parentDlg, OSpec ospec, Vector<Vector> data) throws Exception {
		this.parentDlg = parentDlg;
		setLayout(new BorderLayout(0, 0));

		this.ospec = ospec;
		this.data = data;
		
		int tmpColumIdx = 0;
		fixedColumnPre = new Object[] { new FilterColumn("", "", -1),
				new FilterColumn("UNIQ-No.", "", tmpColumIdx++), new FilterColumn("CONTENTS", "", tmpColumIdx++),
				new FilterColumn("SYSTEM", "", tmpColumIdx++), new FilterColumn("SYSTEM NAME", "", tmpColumIdx++), 
				new FilterColumn("FUNC", "", tmpColumIdx++), 
				new FilterColumn("LEV(MAN)", "", tmpColumIdx++), new FilterColumn("LEV(A)", "", tmpColumIdx++),
				new FilterColumn("SEQ", "", tmpColumIdx++),
				new FilterColumn("Parent NO", "", tmpColumIdx++), new FilterColumn("OLD P/NO", "", tmpColumIdx++),
				new FilterColumn("P/NO", "", tmpColumIdx++), new FilterColumn("P/NAME", "", tmpColumIdx++),
				new FilterColumn("REQ. OPT.", "", tmpColumIdx++), new FilterColumn("SPEC Desc.", "", tmpColumIdx++), 
				new FilterColumn("OPT. Condition", "", tmpColumIdx++),new FilterColumn("N,M,C,D", "", tmpColumIdx++), 
				new FilterColumn("Project", "", tmpColumIdx++), 
				new FilterColumn("Proto Tooling", "", tmpColumIdx++), // [SR170703-020][LJG] Proto Tooling Column 추가
				new FilterColumn("Rep. Qty.", "", tmpColumIdx++)
				};
		fixedColumnPreWidth = new int[]{100, 60, 60, 130, 50, 50, 50, 70, 100, 100, 100, 130, 60, 150, 130, 60, 60, 60, 100};
		tmpColumIdx = ospec.getTrimList().size() + tmpColumIdx;
		fixedColumnPost = new Object[] { 
				new FilterColumn("S/MODE", "", tmpColumIdx++), 
				//WEIGHT
				new FilterColumn("ESTIMATE", "", tmpColumIdx++),  new FilterColumn("TARGET", "", tmpColumIdx++),
				
				//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)	
				new FilterColumn("Weight 관리(STD)", "", tmpColumIdx++),				
				
				new FilterColumn("Module", "", tmpColumIdx++),new FilterColumn("ALTER PART", "", tmpColumIdx++),
				new FilterColumn("DR", "", tmpColumIdx++), 
				//20200914 seho EJS Column 추가.
				new FilterColumn("EJS", "", tmpColumIdx++),
				new FilterColumn("Responsibility", "", tmpColumIdx++), new FilterColumn("Change Description", "", tmpColumIdx++),
				
				//MATERIAL COST
				new FilterColumn("ESTIMATE", "", tmpColumIdx++),  new FilterColumn("TARGET", "", tmpColumIdx++), 
				//CIC
				//DVP SAMPLE
				new FilterColumn("NECESSARY QTY", "", tmpColumIdx++), new FilterColumn("USE", "", tmpColumIdx++),
				new FilterColumn("REQ. TEAM", "", tmpColumIdx++), 
				//CONCEPT DWG
				new FilterColumn("PERFORM", "", tmpColumIdx++), new FilterColumn("PLAN", "", tmpColumIdx++),
				new FilterColumn("2D/3D", "", tmpColumIdx++), new FilterColumn("REL. DATE", "", tmpColumIdx++), 
				//PRD. DWG
				new FilterColumn("PERFORM", "", tmpColumIdx++), 
				new FilterColumn("PLAN", "", tmpColumIdx++), new FilterColumn("ECO/NO", "", tmpColumIdx++),
				//Design Concept Doc.
				new FilterColumn("OSPEC NO", "", tmpColumIdx++), //[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청 
				new FilterColumn("Doc. No.", "", tmpColumIdx++), new FilterColumn("Rel. Date", "", tmpColumIdx++),
				//DESIGN CHARGE
				new FilterColumn("TEAM", "", tmpColumIdx++), new FilterColumn("CHARGER", "", tmpColumIdx++),	
				
				new FilterColumn("SELECTED COMPANY", "", tmpColumIdx++), 
				//EST. INVESTMENT COST
				new FilterColumn("PROTO TOOL'G", "", tmpColumIdx++), 
				//PRD INVENSTMENT COST
				new FilterColumn("TOOL'G", "", tmpColumIdx++), new FilterColumn("SVC. COST", "", tmpColumIdx++), 
				new FilterColumn("SAMPLE", "", tmpColumIdx++), new FilterColumn("SUM", "", tmpColumIdx++), 
				//PROCUMENT
				new FilterColumn("TEAM", "", tmpColumIdx++), new FilterColumn("CHARGER", "", tmpColumIdx++)
			};
		fixedColumnPostWidth = new int[]{60, 60, 60, /*[CF-1706] WEIGHT MANAGEMENT 칼럼 width 추가(4번째 60) by 전성용(20201223)*/80, 50, 
										 70, 50, 50, 150, 60, 60, 80, 60, 60, 80, 60, 60, 50, 120, 80, 80, 100, 100, 100, 120, 80, 80, 
										 /** 20200914 seho EJS Column 폭 추가*/50, 120, 60, 60, 60, 60, 60, 80, 80};
		dataHeader = createTableHeader(headerVector);
		columnCount = dataHeader.size();
		setPostColumnIdx();
		
		int[] notEditColumns = new int[]{ 
				MASTER_LIST_PART_ID_IDX,
				MASTER_LIST_SYSTEM_NAME_IDX,
				MASTER_LIST_LEV_A_IDX, 
				MASTER_LIST_EST_COST_MATERIAL_IDX, 
				MASTER_LIST_TARGET_COST_MATERIAL_IDX, 
				MASTER_LIST_DESIGN_DOC_NO_IDX,
				MASTER_LIST_OSPEC_NO_IDX, //[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
				MASTER_LIST_DESIGN_REL_DATE_IDX,
				MASTER_LIST_SELECTED_COMPANY_IDX, 
//				MASTER_LIST_ENG_DEPT_NM_IDX, MASTER_LIST_ENG_RESPONSIBILITY_IDX,
				MASTER_LIST_PRT_TOOLG_INVESTMENT_IDX, MASTER_LIST_PRD_TOOL_COST_IDX,
				MASTER_LIST_PRD_SERVICE_COST_IDX, MASTER_LIST_PRD_SAMPLE_COST_IDX,
				MASTER_LIST_PRD_SUM_IDX, MASTER_LIST_PUR_TEAM_IDX, MASTER_LIST_PUR_RESPONSIBILITY_IDX};
		for( int i = 0; i < notEditColumns.length; i++){
			notEditableColumn.add(notEditColumns[i]);
		}
		
		int[] designEditColumns = new int[]{ 
				MASTER_LIST_CONTENTS_IDX, MASTER_LIST_SYSTEM_IDX, 
				MASTER_LIST_SEQUENCE_IDX, MASTER_LIST_PART_NAME_IDX, 
				MASTER_LIST_REQ_OPT_IDX, MASTER_LIST_NMCD_IDX,
				MASTER_LIST_PROJECT_IDX, MASTER_LIST_SUPPLY_MODE_IDX, 
				MASTER_LIST_EST_WEIGHT_IDX, MASTER_LIST_DR_IDX, 
				
				//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)
				MASTER_LIST_WEIGHT_MANAGEMENT_IDX,
				
				// 20200914 seho EJS Column 추가
				MASTER_LIST_EJS_IDX,
				MASTER_LIST_RESPONSIBILITY_IDX, MASTER_LIST_EST_COST_MATERIAL_IDX, 
				MASTER_LIST_ENG_DEPT_NM_IDX, MASTER_LIST_ENG_RESPONSIBILITY_IDX,
				MASTER_LIST_DVP_NEEDED_QTY_IDX, MASTER_LIST_DVP_USE_IDX,
			    MASTER_LIST_DVP_REQ_DEPT_IDX, MASTER_LIST_PROTO_TOOLING_IDX		
		};
		for( int i = 0; i < designEditColumns.length; i++){
			designerReqColumn.add(designEditColumns[i]);
		}
		
		if( parentDlg instanceof MasterListDlg){
			if( parentDlg.isEditable()){
				partNameCreationDlg = new PartNameCreationDlg(parentDlg);
				masterListConditionDlg = new MasterListConditionDlg(parentDlg, null, parentDlg.getOptionManager());
			}
		}
		
		createFixedTable();
		reloadTable(null);
		headerPopup = new HeaderPopup();

		JViewport viewport = new JViewport();
		viewport.setView(fixedTable);
		viewport.setPreferredSize(fixedTable.getPreferredSize());
		scroll.setRowHeaderView(viewport);
		scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER,
				fixedTable.getTableHeader());
		add(scroll, BorderLayout.CENTER);
		setShowDeletedLine(true);
	}
	
	public void setData(Vector<Vector> data) {
		this.data = data;
	}

	public String getColumnName(int modelColumnIdx){
		return columnMap.get(modelColumnIdx);
	}
	
	public Object[] getFixedColumnPre() {
		return fixedColumnPre;
	}

	public Object[] getFixedColumnPost() {
		return fixedColumnPost;
	}
	
	public boolean isCompareWithRelease() {
		return isCompareWithRelease;
	}

	public void setCompareWithRelease(boolean isCompareWithRelease) {
		this.isCompareWithRelease = isCompareWithRelease;
		if( table != null){
			table.repaint();
		}
	}
	
	private void setPostColumnIdx(){
		MASTER_LIST_SUPPLY_MODE_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size();
		
		MASTER_LIST_EST_WEIGHT_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 1;
		MASTER_LIST_TARGET_WEIGHT_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 2;
		
		//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)	
		MASTER_LIST_WEIGHT_MANAGEMENT_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 3;		
		
		MASTER_LIST_MODULE_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 4;
		MASTER_LIST_ALTER_PART_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 5;
		
		MASTER_LIST_DR_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 6;
		//20200914 seho EJS Column 추가.
		MASTER_LIST_EJS_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 7;
		MASTER_LIST_RESPONSIBILITY_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 8;
		
		MASTER_LIST_CHANGE_DESCRIPTION_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 9;
		
		MASTER_LIST_EST_COST_MATERIAL_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 10;
		MASTER_LIST_TARGET_COST_MATERIAL_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 11;
		
		MASTER_LIST_DVP_NEEDED_QTY_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 12;
		MASTER_LIST_DVP_USE_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 13;
		MASTER_LIST_DVP_REQ_DEPT_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 14;
		
		MASTER_LIST_CON_DWG_PERFORMANCE_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 15;
		MASTER_LIST_CON_DWG_PLAN_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 16;
		MASTER_LIST_CON_DWG_TYPE_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 17;
		MASTER_LIST_DWG_DEPLOYABLE_DATE_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 18;
		
		MASTER_LIST_PRD_DWG_PERFORMANCE_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 19;
		MASTER_LIST_PRD_DWG_PLAN_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 20;
		MASTER_LIST_ECO_NO_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 21;
		
		MASTER_LIST_OSPEC_NO_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 22; //[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
		MASTER_LIST_DESIGN_DOC_NO_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 23;
		MASTER_LIST_DESIGN_REL_DATE_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 24;
		
		MASTER_LIST_ENG_DEPT_NM_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 25;
		MASTER_LIST_ENG_RESPONSIBILITY_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 26;
		
		MASTER_LIST_SELECTED_COMPANY_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 27;
		
		MASTER_LIST_PRT_TOOLG_INVESTMENT_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 28;
		MASTER_LIST_PRD_TOOL_COST_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 29;
		MASTER_LIST_PRD_SERVICE_COST_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 30;
		MASTER_LIST_PRD_SAMPLE_COST_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 31;
		MASTER_LIST_PRD_SUM_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 32;
		
		MASTER_LIST_PUR_TEAM_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 33;
		MASTER_LIST_PUR_RESPONSIBILITY_IDX = fixedColumnPre.length - 1 + ospec.getTrimList().size() + 34;
	}

	private void createFixedTable(){
		fixedModel = new DefaultTableModel((Vector) data.clone(), getHeader()) {
			public int getColumnCount() {
				return fixColumn;
			}

		};

		fixedTable = new JTable(fixedModel) {
			public void valueChanged(ListSelectionEvent e) {
				super.valueChanged(e);

				int minRowIdx = -1;
				int[] fixedSelectedIndexes = fixedTable.getSelectedRows();
				if (fixedSelectedIndexes != null
						&& fixedSelectedIndexes.length > 0) {
					Arrays.sort(fixedSelectedIndexes);
					
					table.setRowSelectionInterval(
							fixedSelectedIndexes[0],
							fixedSelectedIndexes[0]);
					table.setColumnSelectionInterval(0,
							table.getColumnCount() - 1);
					
					for( int i = 1; i < fixedSelectedIndexes.length; i++){
						table.addRowSelectionInterval(
								fixedSelectedIndexes[i],
								fixedSelectedIndexes[i]);
						table.addColumnSelectionInterval(0,
								table.getColumnCount() - 1);
					}					
				}

			}

			public void columnMarginChanged(ChangeEvent changeevent) {
				// Auto-generated method stub
				if (scroll != null) {
					scroll.getRowHeader().setPreferredSize(
							fixedTable.getPreferredSize());
				}
				super.columnMarginChanged(changeevent);
			}

			protected JTableHeader createDefaultTableHeader() {
				return new GroupableTableHeader(columnModel);
			}

			@Override
			public Component prepareRenderer(TableCellRenderer renderer,
					int row, int column) {
				Component com = super.prepareRenderer(renderer, row, column);
				com.setBackground(Color.LIGHT_GRAY);
				return com;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				// Auto-generated method stub
				return false;
			}

		};
		fixedTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {

				if (SwingUtilities.isRightMouseButton(mouseEvent)
						&& mouseEvent.getClickCount() == 1) {
					if( parentDlg.isEditable()){
						FixedTablePopup tablePopup = new FixedTablePopup(mouseEvent);
						tablePopup.show(fixedTable, mouseEvent.getX(),
								mouseEvent.getY());
					}
				}
			}

		});

		fixedTable.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_C,
						java.awt.event.InputEvent.CTRL_DOWN_MASK),
				"actionTableRowCopy");
		fixedTable.getActionMap().put("actionTableRowCopy", new CopyAction());

		fixedTable.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_V,
						java.awt.event.InputEvent.CTRL_DOWN_MASK),
				"actionTableRowPaste");
		fixedTable.getActionMap().put("actionTableRowPaste", new PasteAction());
		
		fixedTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		fixedTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		GroupableTableHeader fixedHeader = (GroupableTableHeader) fixedTable.getTableHeader();
		fixedHeader.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent event) {
				if( parentDlg.isEditable()){
					try {
						addRow(event);
					} catch (Exception e) {
						// Auto-generated catch block
						e.printStackTrace();
						MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
					}finally{
						table.repaint();
					}
				}
				super.mouseReleased(event);
			}
		});		
		
		for (int i = 0; i < fixedTable.getColumnModel().getColumnCount(); i++) {
			fixedTable.getColumnModel().getColumn(i)
					.setHeaderRenderer(new FilterRenderer((FilterColumn)dataHeader.get(i)));
		}
	}
	
	public int getColumnCount(){
		return columnCount;
	}
	
	public ArrayList getCurrentPreColumns(){
		return currentPreColumnList;
	}
	
	public ArrayList getCurrentPostColumns(){
		return currentPostColumnList;
	}
	
	public void setShowDeletedLine(boolean bFlag){
		isShowDeletedLine = bFlag;
		headerPopup.execFilter(false);
	}
	
	public void reloadTable(Vector visibleColumns) throws TCException{
		
		int tmpColumIdx = fixedColumnPre.length;
		ArrayList<OpTrim> trimList = ospec.getTrimList();
		
		currentPreColumnList.clear();
		currentPostColumnList.clear();
		if( visibleColumns != null){
			for (OpTrim trim : trimList) {
				visibleColumns.add(new FilterColumn(trim.getTrim(), "", tmpColumIdx++) );
			}
			ArrayList list = new ArrayList(visibleColumns);
			Collections.sort(list);
			visibleColumns = new Vector(list); 
			
			for( int i = 0; i < visibleColumns.size();i++){
				FilterColumn fc = (FilterColumn)visibleColumns.get(i);
				if( fc.getOrder() < fixedColumnPre.length ){
					currentPreColumnList.add(fc);
				}else if(fc.getOrder() > fixedColumnPre.length + trimList.size() - 1){
					currentPostColumnList.add(fc);
				}
			}
			createTable(visibleColumns);
		}else{
			for( int i = 0; i < fixedColumnPre.length; i++){
				currentPreColumnList.add(fixedColumnPre[i]);
			}
			
			for( int i = 0; i < fixedColumnPost.length; i++){
				currentPostColumnList.add(fixedColumnPost[i]);
			}
			
			createTable(null);
			scroll = new JScrollPane(table);
		}
		
		TableRowResizer rowResizer = new TableRowResizer(fixedTable, table);
		fixedTable.addMouseListener(rowResizer);
		
		scroll.setViewportView(table);
		scroll.revalidate();
		
	}
	
	class MasterListTableModel extends DefaultTableModel{

			public int getColumnCount() {
				return columnCount - fixColumn;
			}

			public int getRowCount() {
				if (data == null || data.isEmpty())
					return 0;
				return dataVector.size();
			}

			public String getColumnName(int i) {
				Object obj = null;
				if (i < columnIdentifiers.size() && i >= 0)
					obj = columnIdentifiers.elementAt(i + fixColumn);
				return obj != null ? obj.toString() : super.getColumnName(i);
			}

			public Object getValueAt(int row, int col) {
				return data.get(row).get(col + fixColumn);
			}

			public void setValueAt(Object obj, int row, int col) {
				Vector rowVec = data.get(row);
				rowVec.set(col + fixColumn, obj);
			}

			public boolean CellEditable(int row, int col) {
				return true;
			}

			@Override
			public boolean isCellEditable(int row, int col) {
				// Auto-generated method stub
				if( col == (fixedColumnPre.length + ospec.getTrimList().size()) - 1){
					return false;
				}
				
				return true;
			}
	}
	
	private DefaultTableModel createDefaultTableModel(){
		DefaultTableModel model = new DefaultTableModel((Vector) data.clone(), getHeader()) {
			public int getColumnCount() {
				return columnCount - fixColumn;
			}

			public int getRowCount() {
				if (data == null || data.isEmpty())
					return 0;
				return dataVector.size();
			}

			public String getColumnName(int i) {
				Object obj = null;
				if (i < columnIdentifiers.size() && i >= 0)
					obj = columnIdentifiers.elementAt(i + fixColumn);
				return obj != null ? obj.toString() : super.getColumnName(i);
			}

			public Object getValueAt(int row, int col) {
				return data.get(row).get(col + fixColumn);
			}

			public void setValueAt(Object obj, int row, int col) {
				Vector rowVec = data.get(row);
				rowVec.set(col + fixColumn, obj);
				fireTableCellUpdated(row, col);
			}

			public boolean CellEditable(int row, int col) {
				return parentDlg.isEditable();
			}

			@Override
			public boolean isCellEditable(int row, int col) {
				// Auto-generated method stub
				if( parentDlg.isEditable()){
					// T/Usage 및 타시스템과의 I/F항목은 수정 불가.
					if( notEditableColumn.contains(col)){
						return false;
					}
					
					if( col > fixedColumnPre.length - 2 && col < (columnCount - fixedColumnPost.length - 1)){
						Object obj = getValueAt(row, MASTER_LIST_SPEC_IDX);
						if( obj == null || obj.toString().equals("")){
							return parentDlg.isEditable();
						}else{
							return false;
						}
					}
					
					Object obj = getValueAt(row, MASTER_LIST_NMCD_IDX);
					//20201105 seho EJS Column은 필수항목 조건이 아니면 편집 못하도록 막음.
					if (col == MASTER_LIST_EJS_IDX)
					{
						Object supplyMode = getValueAt(row, MASTER_LIST_SUPPLY_MODE_IDX);
						Object drString = getValueAt(row, MASTER_LIST_DR_IDX);
						if (obj.toString().equalsIgnoreCase("C")
							&& (supplyMode.equals("C0") || supplyMode.equals("C1") || supplyMode.equals("C7") || supplyMode.equals("CD") || supplyMode.equals("C7UC8") || supplyMode.equals("C7YC8") || supplyMode.equals("P0") || supplyMode.equals("P1") || supplyMode.equals("P7") || supplyMode.equals("PD") || supplyMode.equals("P7UP8") || supplyMode.equals("P7YP8") || supplyMode.equals("P7MP8"))
							&& (drString.equals("DR1") || drString.equals("DR2") || drString.equals("DR3")))
						{
							return true;
						}else
						{
							return false;
						}
					}
					if(col == MASTER_LIST_WEIGHT_MANAGEMENT_IDX)
					{
						return true;
					}
					if( obj.toString().equalsIgnoreCase("C")){
						if( col == MASTER_LIST_SEQUENCE_IDX 
								|| col == MASTER_LIST_PARENT_ID_IDX || col == MASTER_LIST_SUPPLY_MODE_IDX
								|| col == MASTER_LIST_MODULE_IDX || col == MASTER_LIST_ALTER_PART_IDX
								|| col == MASTER_LIST_REQ_OPT_IDX || col == MASTER_LIST_LEV_MAN_IDX
								|| col == MASTER_LIST_SPEC_IDX	|| col == MASTER_LIST_NMCD_IDX
								|| col == MASTER_LIST_SPEC_DESC_IDX || col == MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX
								|| col == MASTER_LIST_DVP_NEEDED_QTY_IDX || col == MASTER_LIST_DVP_REQ_DEPT_IDX
								|| col == MASTER_LIST_DVP_USE_IDX
								|| col == MASTER_LIST_ENG_DEPT_NM_IDX || col == MASTER_LIST_ENG_RESPONSIBILITY_IDX
								|| col == MASTER_LIST_PROTO_TOOLING_IDX //[SR170703-020][LJG]Proto Tooling 컬럼 추가
								|| col == MASTER_LIST_SYSTEM_IDX){
							return true;
						}else{
							return false;
						}
					}
					return true;
				}else{
					return false;
				}
			}

			@Override
			public void fireTableCellUpdated(int row, int column) {
				// Auto-generated method stub
				DefaultTableModel model = (DefaultTableModel) table
						.getModel();
				
				if( column == MASTER_LIST_PART_NAME_IDX){
					String partName = getValueAt(row, column).toString();
					ArrayList<String> essentialNames = partNameCreationDlg.getEssentialNames();
					if( essentialNames != null && essentialNames.contains(partName)){
						setValueAt("Y", row, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX);
					}else{
						setValueAt("N", row, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX);
					}
				} else if (column == MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX) {
					// 대표수량을 변경할 경우, Usage에 값이 있는 셀은 일괄적으로 변경된다.
					Object repQtyObj = model.getValueAt(row, column);
					int trimCount = getOspec().getTrimList().size();
					int fixedCount = getCurrentPreColumns().size();
					String str2 = null;
					CellValue cellValue = null;
					
					for (int i = fixedCount - 1; i < trimCount + fixedCount - 1; i++) {
						
						int col = getTable().convertColumnIndexToModel(i);
						Object obj = model.getValueAt(row, col);
						if (obj instanceof CellValue) {
							cellValue = (CellValue) obj;
							cellValue = new CellValue(cellValue.getValue(),
									cellValue.getSortValue(),
									cellValue.getOrder());
						} else {
							cellValue = new CellValue(obj.toString());
						}
						model.setValueAt(cellValue, row, col);

						str2 = cellValue.getValue();
						boolean isOpt = false;
						if (str2.indexOf("(") > -1 || str2.indexOf(")") > -1) {
							str2 = str2.replaceAll("\\(", "");
							str2 = str2.replaceAll("\\)", "");
							isOpt = true;
						}

						if (str2.length() > 0) {
							double result = Double.parseDouble(str2);
							if (result > 0) {
								try{
									int num = Integer.parseInt(repQtyObj.toString());
									cellValue.setValue(isOpt ? "(" + num + ")" : "" + num);
								}catch(NumberFormatException nfe){
									double num = Double.parseDouble(repQtyObj.toString());
									cellValue.setValue(isOpt ? "(" + num + ")" : "" + num);
								}
							}
						}

					}
				}else if(column == MASTER_LIST_SPEC_IDX){
					
					int trimCount = getOspec().getTrimList().size();
					int fixedCount = getCurrentPreColumns().size();
					CellValue cellValue = null;
					
					// [20161031][ymjang] 옵션입력 Dialog 창에서 Cancel 버튼을 클릭할 경우, Usage 수량 초기화하지 않도록 수정
					boolean isCancel = false;
					Object obj = model.getValueAt(row, column);
					if (obj instanceof CellValue) {
						cellValue = (CellValue) obj;
						isCancel = cellValue.isCancel();
					}
					
					if (!isCancel) { 
						for (int i = fixedCount - 1; i < trimCount + fixedCount - 1; i++) {
							int col = getTable().convertColumnIndexToModel(i);
							cellValue = new CellValue("");
							model.setValueAt(cellValue, row, col);
						}
					}
//					for (int i = fixedCount - 1; i < trimCount + fixedCount - 1; i++) {
//						int col = getTable().convertColumnIndexToModel(i);
//						cellValue = new CellValue("");
//						model.setValueAt(cellValue, row, col);
//					}
				}

				super.fireTableCellUpdated(row, column);
			}

		};

		return model;
	}
	
	private JTable createDefaultTable(){
		JTable table = new JTable(model) {
			
			public void valueChanged(ListSelectionEvent e) {
				super.valueChanged(e);
				// checkSelection(false);
			}

			protected JTableHeader createDefaultTableHeader() {
				return new GroupableTableHeader(columnModel);
			}
			
			public boolean isCellEditable(int row, int column) {
				int modelRow = convertRowIndexToModel(row);
				int modelColumn = convertColumnIndexToModel(column);
				if( modelRow > -1 && modelColumn > -1){
					return model.isCellEditable(modelRow, modelColumn);
				}else{
					return false;
				}
		    }
			
			@Override
			public int getRowHeight() {
				// Auto-generated method stub
				return 20;
			}

			public Component prepareRenderer(
	    			TableCellRenderer renderer, int row, int column)
	    		{
	    			Component c = super.prepareRenderer(renderer, row, column);
	    			JComponent jc = (JComponent)c;

	    			//  Color row based on a cell value
	    			//  Alternate row color
	    			if( copiedRows != null && copiedRows.length > 0 
	    					&& copiedColumns != null && copiedColumns.length > 0){
	    				
//	    				Color copyColor = UIManager.getColor("Table.highlight");
	    				Color copyColor = Color.MAGENTA;
	    				int thick = 2;
	    				if( column == copiedColumns[0]){
	    					if( row == copiedRows[0]){
	    						jc.setBorder(new MatteBorder(thick, thick, copiedRows.length == 1 ? thick:0, copiedColumns.length == 1 ? thick:0, copyColor) );
	    					}else if( row == copiedRows[copiedRows.length - 1]){
	    						jc.setBorder(new MatteBorder(0, thick, thick, copiedColumns.length == 1 ? thick:0, copyColor) );
	    					}else if( row > copiedRows[0] && row < copiedRows[copiedRows.length - 1]){
	    						jc.setBorder(new MatteBorder(0, thick, 0, copiedColumns.length == 1 ? thick:0, copyColor) );
	    					}
	    				}else if(column == copiedColumns[copiedColumns.length - 1]){
	    					if( row == copiedRows[0]){
	    						jc.setBorder(new MatteBorder(thick, 0, copiedRows.length == 1 ? thick:0, thick, copyColor) );
	    					}else if( row == copiedRows[copiedRows.length - 1]){
	    						jc.setBorder(new MatteBorder(0, 0, thick, thick, copyColor) );
	    					}else if( row > copiedRows[0] && row < copiedRows[copiedRows.length - 1]){
	    						jc.setBorder(new MatteBorder(0, 0, copiedRows.length == 1 ? thick:0, thick, copyColor) );
	    					}
	    				}else if(column > copiedColumns[0] && column < copiedColumns[copiedColumns.length - 1]){
	    					if( row == copiedRows[0]){
	    						jc.setBorder(new MatteBorder(thick, 0, copiedRows.length == 1 ? thick:0, 0, copyColor) );
	    					}else if( row == copiedRows[copiedRows.length - 1]){
	    						jc.setBorder(new MatteBorder(0, 0, thick, 0, copyColor) );
	    					}
	    				}
	    			}
	    			
	    			int modelColumn = convertColumnIndexToModel(column);
	    			if( modelColumn == MASTER_LIST_REQ_OPT_IDX){
	    				int modelRow = convertRowIndexToModel(row);
	    				String itemName = model.getValueAt(modelRow, MASTER_LIST_PART_NAME_IDX).toString();
	    				if( parentDlg.getEssentialNames().contains(itemName)){
	    					jc.setForeground(Color.RED);
	    					jc.setFont(new Font("굴림", Font.BOLD, 12));
	    				}
	    			}
	    			
	    			return c;
	    		}
			
			
		};
		
		return table;
	}
	
	private TableRowSorter createRowSorter(JTable table){
		TableRowSorter sorter = new TableRowSorter<TableModel>(table.getModel());
		sorter.addRowSorterListener(new RowSorterListener() {
			
			@Override
			public void sorterChanged(RowSorterEvent rowsorterevent) {
				// Auto-generated method stub
				refreshRowNum();
			}
		});
		sorter.setSortsOnUpdates(false);
		sorter.setMaxSortKeys(1);
		for (int i = 0; i < table.getColumnCount(); i++) {
			sorter.setComparator(i, new Comparator<Object>() {

				@Override
				public int compare(Object o1, Object o2) {

					CellValue c1, c2;
					if (o1 instanceof CellValue) {
						c1 = (CellValue) o1;
					} else {
						if( o1 == null){
							o1 = "";
						}
						c1 = new CellValue(o1 + "", o1 + "", 0);
					}

					if (o2 instanceof CellValue) {
						c2 = (CellValue) o2;
					} else {
						if( o2 == null){
							o2 = "";
						}
						c2 = new CellValue(o2 + "", o2 + "", 0);
					}

					
					try{
						double d1 = Double.parseDouble(c1.getSortValue());
						double d2 = Double.parseDouble(c2.getSortValue());
						return new Double(d1).compareTo(new Double(d2));
					}catch(Exception e){
						int result = c1.getSortValue().compareTo(c2.getSortValue());
						if (result != 0) {
							return result;
						} else {
							if (c1.getOrder() > c2.getOrder()) {
								return 1;
							} else if (c1.getOrder() < c2.getOrder()) {
								return -1;
							} else {
								return 0;
							}
						}
					}
					
				}
			});
		}
		
		return sorter;
	}
	
	private void createTable(final Vector visibleColumns) throws TCException{
		
		model = createDefaultTableModel();
		table = createDefaultTable();
		sorter = createRowSorter(table);
		table.setRowSorter(sorter);
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {

				if( parentDlg.isEditable()){
					if (SwingUtilities.isRightMouseButton(event)
							&& event.getClickCount() == 1) {
						TablePopup tablePopup = new TablePopup();
						tablePopup.show(table, event.getX(), event.getY());
					}
				}
			}
		});
		table.setShowVerticalLines(false);
		header = (GroupableTableHeader) table.getTableHeader();
		header.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent event) {
				if (SwingUtilities.isRightMouseButton(event)
						&& event.getClickCount() == 1) {
					editColumnAt(event.getPoint());
					table.repaint();
				} else if (SwingUtilities.isLeftMouseButton(event)
						&& event.getClickCount() == 1) {
					int columnIndex = header.columnAtPoint(event.getPoint());
					if (columnIndex != -1) {
						selectedColumn = table.convertColumnIndexToModel(columnIndex);
						headerPopup.setColumnIdx(selectedColumn);
						HeaderPopupFilter filter = headerPopup.getFilter();
						filter.reload();
					}
					//Sort 할 경우.
					clearSortValue();

				}
			}

		});

		table.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_C,
						java.awt.event.InputEvent.CTRL_DOWN_MASK),
				"actionTableCellCopy");
		table.getActionMap().put("actionTableCellCopy", new CopyAction());

		table.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_V,
						java.awt.event.InputEvent.CTRL_DOWN_MASK),
				"actionTableCellPaste");
		table.getActionMap().put("actionTableCellPaste", new PasteAction());
		
		table.getInputMap().put(KeyStroke.getKeyStroke("DELETE"),
				"actionTableCellDelete");
		table.getActionMap().put("actionTableCellDelete", new DeleteAction());
		
		table.getInputMap().put(
				KeyStroke.getKeyStroke("ESCAPE"), "actionTableCellCopyCancel");
		table.getActionMap().put("actionTableCellCopyCancel", new AbstractAction(){

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				// Auto-generated method stub
				copiedColumns = null;
				copiedRows = null;
				table.repaint();
			}
			
		});

		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(true);
		
		table.setPreferredScrollableViewportSize(table.getPreferredSize());

		
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);		
			
		setRenderer();
		
		for (int i = table.getColumnModel().getColumnCount() - 1; visibleColumns != null && i >= 0 ; i--) {
			if( visibleColumns != null && !visibleColumns.contains( dataHeader.get(i + fixColumn))){
				table.getColumnModel().removeColumn(table.getColumnModel().getColumn(i));
				continue;
			}
		}
		
		table.getColumnModel().setColumnMargin(0);
		setGroupColumn(table, ospec.getTrimList(), fixColumn, header, currentPreColumnList);
	}
	
	private void setColumnWidth(int column){
		if( column < fixedColumnPreWidth.length){
			table.getColumnModel().getColumn(column).setPreferredWidth(fixedColumnPreWidth[column]);
		}else if( column >= fixedColumnPreWidth.length + ospec.getTrimList().size()){
			table.getColumnModel().getColumn(column).setPreferredWidth(fixedColumnPostWidth[column - (fixedColumnPreWidth.length + ospec.getTrimList().size())]);
		}else{
			table.getColumnModel().getColumn(column).setPreferredWidth(60);
		}
	}
	
	private void setRenderer() throws TCException{
		TCSession session = CustomUtil.getTCSession();
		JComboBox combo = null;
		
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			
			table.getColumnModel().getColumn(i).setHeaderRenderer(new FilterRenderer((FilterColumn)dataHeader.get(i + fixColumn)));
			table.getColumnModel().getColumn(i).setCellRenderer(new ValueCellRenderer());
			
			setColumnWidth(i);
			
			switch(i){
			case MASTER_LIST_PROJECT_IDX:
				combo = new LovCombo(session, "S7_PROJECT_CODE");
				DefaultCellEditor editor = new ComboCellEditor(combo){

					JTextField tf = null;
					String nmcdStr = null;
					@Override
					public Component getTableCellEditorComponent(JTable table,
							Object value, boolean isSelected, int row,
							int column) {
						// Auto-generated method stub
						int modelRow = table.convertRowIndexToModel(row);
						nmcdStr = model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_NMCD_IDX).toString();
						if( nmcdStr.contains("M")){
							tf = new JTextField(value.toString());
							return tf;
						}else{
							return super.getTableCellEditorComponent(table, value, isSelected, row, column);
						}
						
					}

					@Override
					public Object getCellEditorValue() {
						// Auto-generated method stub
						if( nmcdStr.contains("M")){
							return tf.getText();
						}else{
							return super.getCellEditorValue();
						}
					}
					
				};
				editor.setClickCountToStart(2);
				table.getColumnModel().getColumn(i).setCellEditor(editor);
				break;
				
			case MASTER_LIST_CONTENTS_IDX:
				if( parentDlg instanceof MasterListDlg){
					MasterListDlg dlg = (MasterListDlg)parentDlg;
					combo = new JComboBox(dlg.getContents());
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}
				break;
				
			case MASTER_LIST_SYSTEM_IDX:
				LovCombo lovCombo = new LovCombo(session, "S7_SYSTEM_CODE");
				editor = new DefaultCellEditor(lovCombo){
					
					LovCombo combo = null;
					int row = -1, column = -1;
					@Override
					public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
						// Auto-generated method stub
						combo = (LovCombo)super.getTableCellEditorComponent(table, value, isSelected, row, column);
						this.row = row;
						this.column = column;
						
						for( int i = 0; i < combo.getItemCount();i++){
							Object obj = combo.getItemAt(i);
							if( obj.toString().equals(value.toString())){
								combo.setSelectedIndex(i);
								break;
							}
						}
						
						return combo;
					}

					@Override
					public Object getCellEditorValue() {
						table.setValueAt(new CellValue(combo.getSelectedDescription()), row, column + 1);
						table.repaint();
						return super.getCellEditorValue();
					}
					
				};
				editor.setClickCountToStart(2);
				table.getColumnModel().getColumn(i).setCellEditor(editor);
				break;
			case MASTER_LIST_LEV_MAN_IDX:
				JTextField tf = new JTextField();
				PlainDocument doc = (PlainDocument) tf.getDocument();
				doc.setDocumentFilter(new TextFieldFilter(true));
				editor = new DefaultCellEditor(tf){

					@Override
					public Component getTableCellEditorComponent(
							JTable table, Object value, boolean isSelected,
							int row, int column) {
						JTextField tf = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
						int tfLength = tf.getText().length();
						if( tfLength > 0){
							tf.setSelectionStart( 0 );
							tf.setSelectionEnd( tfLength );
						}
						return tf;
					}
					
				};
				editor.setClickCountToStart(2);
				table.getColumnModel().getColumn(i).setCellEditor(editor);
				break;
			case MASTER_LIST_PART_DISPLAY_ID_IDX:
				editor = new PartDisplayIdCellEditor(parentDlg, this);
				editor.setClickCountToStart(2);
				table.getColumnModel().getColumn(i).setCellEditor(editor);
				break;
			case MASTER_LIST_PART_NAME_IDX:
				final JTextField partNameTextField = new JTextField();
				editor = new PartNameCellEditor(partNameTextField, partNameCreationDlg);
				editor.setClickCountToStart(2);
				table.getColumnModel().getColumn(i).setCellEditor(editor);
				partNameTextField.addKeyListener(new KeyAdapter(){
					public void keyTyped(KeyEvent ke){
						if(partNameTextField.getText().length()> 50){
							ke.consume();
							MessageBox.post((Window)parentDlg, "Part Name이 50자를 초과합니다. 사양관리팀에게 문의하세요.", "경고", MessageBox.WARNING);
						}
					}
				});
				break;
			case MASTER_LIST_SPEC_IDX:
				tf = new JTextField();
				if( parentDlg instanceof MasterListDlg){
					editor = new PartConditionCellEditor((MasterListDlg)parentDlg, tf, masterListConditionDlg);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}
				break;
			case MASTER_LIST_REQ_OPT_IDX:
				String[] tmpArray = new String[]{"Y","N"};
				combo = new JComboBox(tmpArray);
				editor = new ComboCellEditor(combo);
				editor.setClickCountToStart(2);
				table.getColumnModel().getColumn(i).setCellEditor(editor);
				break;
//			case MASTER_LIST_SUPPLY_MODE_IDX:
//				tmpArray = CustomUtil.getLOVDisplayValues(session, "S7_SUPPLY_MODE");
//				combo = new JComboBox(tmpArray);
//				editor = new ComboCellEditor(combo);
//				editor.setClickCountToStart(2);
//				table.getColumnModel().getColumn(i).setCellEditor(editor);
//				break;
//			case MASTER_LIST_STD_OPT_IDX:
//				combo = new JComboBox();
//				combo.addItem(new CellValue(MASTER_LIST_STD));
//				combo.addItem(new CellValue(MASTER_LIST_OPT));
//				combo.addItemListener(new ItemListener() {
//					@Override
//					public void itemStateChanged(ItemEvent itemevent) {
//						if( itemevent.getStateChange() == ItemEvent.SELECTED){
//							// Auto-generated method stub
//							changeUsageType(itemevent);
//						}
//					}
//				});
//				
//				editor = new ComboCellEditor(combo);
//				editor.setClickCountToStart(2);
//				table.getColumnModel().getColumn(i).setCellEditor(editor);
//				break;
			case MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX:
				lovCombo = new LovCombo(session, "Unit of Measures");
				editor = new RepQtyCellEditor(lovCombo, parentDlg);
				editor.setClickCountToStart(2);
				table.getColumnModel().getColumn(i).setCellEditor(editor);
				break;
			default:
				
//				String[] tmpArray = null;
				
				if( MASTER_LIST_NMCD_IDX == i ){
					// [NoSR][20160309][jclee] T 추가
					//[SR171227-049][LJG] M -> M1,M2,M3로 세분화
					//[CSH][20180426]NMCD Combobox 값 변경후 즉시 apply 버튼 누르면 변경된 값이 적용 안되는 문제 fix
					final String[] values = new String[]{"N", "M1", "M2", "M3", "C", "D", "T"};
					combo = new JComboBox(values);
					editor = new ComboCellEditor(combo){
//						JComboBox combo = null;
						int row = -1, column = -1;
						JTable table = null;
						
						ItemListener itemListener = new ItemListener() {
							
							@Override
							public void itemStateChanged(ItemEvent itemevent) {
								// Auto-generated method stub
								if( itemevent.getStateChange() == ItemEvent.SELECTED){
									JComboBox combo = (JComboBox)editorComponent;
									String selectedValue = combo.getSelectedItem().toString();
									// [NoSR][20160309][jclee] T 추가
//									if( selectedValue.equalsIgnoreCase("N")){
									if( selectedValue.equalsIgnoreCase("N") || selectedValue.equalsIgnoreCase("T") ){
										int modelRow = table.convertRowIndexToModel(row);
										CellValue partIdCellValue = (CellValue)model.getValueAt(modelRow, MASTER_LIST_PART_ID_IDX);
										/**
										 * N, T 값일 때 공백 처리로직 제거 
										 * 값이 변경 될 때 이벤트가 아니라, 값이 선택될 때 이벤트 이므로 주석 처리 함
										 */
//										partIdCellValue.setValue("");
//										model.setValueAt("", modelRow, MASTER_LIST_PART_NAME_IDX);
//										model.setValueAt("", modelRow, MASTER_LIST_SEQUENCE_IDX);
//										model.setValueAt(parentDlg.getProject(), modelRow, MASTER_LIST_PROJECT_IDX);
									}else if(selectedValue.contains("M") ){
										int modelRow = table.convertRowIndexToModel(row);
										CellValue partIdCellValue = (CellValue)model.getValueAt(modelRow, MASTER_LIST_PART_ID_IDX);
										/**
										 * N, T 값일 때 공백 처리로직 제거 
										 * 값이 변경 될 때 이벤트가 아니라, 값이 선택될 때 이벤트 이므로 주석 처리 함
										 */
//										partIdCellValue.setValue("");
									}
								}
							}
						};
							
						
						@Override
						public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column) {
							this.table = table;
							this.row = row;
							this.column = column;
							
							//[20180109][LJG]NMCD가 C일경우에는 수정 하지 못하도록 - 송대영 차장 요청
							//[20180202][LJG]비정규 파트일 경우는 NMCD가 C일경우라도 수정 가능 하도록 - 송대영 차장 요청
							if("C".equalsIgnoreCase(value.toString())){
								if(!table.getValueAt(row, 0).toString().startsWith(ospec.getProject())){
									MessageBox.post("You can not modify a Carry Over Part.\nDelete it and add it before you work.", "", MessageBox.INFORMATION);
									return null;
								}
							}
							
							JComboBox combo = (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
							for( int i = 0; i < combo.getItemCount();i++){
								Object obj = combo.getItemAt(i);
								if( obj.toString().equals(value.toString())){
									combo.setSelectedIndex(i);
									break;
								}
							}
							combo.addItemListener(itemListener);
							return combo;
						}

						@Override
						public Object getCellEditorValue() {
							JComboBox combo = (JComboBox)editorComponent;
							Object selectedObj = combo.getSelectedItem();
							if( selectedObj == null){
								selectedObj = "";
							}
							
							CellValue cellValue = null;
							Object obj = table.getValueAt(row, column);
							if( obj instanceof CellValue){
								cellValue = (CellValue)obj;
								cellValue = new CellValue(selectedObj.toString()) ;
							}else{
								cellValue = new CellValue(selectedObj.toString()) ;
							}
							
							return cellValue;
						}
					};
					
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}else if( MASTER_LIST_EST_WEIGHT_IDX == i || MASTER_LIST_TARGET_WEIGHT_IDX == i){
					JTextField numberTf = new JTextField();
					doc = (PlainDocument) numberTf.getDocument();
					doc.setDocumentFilter(new TextFieldFilter());
					editor = new DefaultCellEditor(numberTf){

						@Override
						public Component getTableCellEditorComponent(
								JTable table, Object value, boolean isSelected,
								int row, int column) {
							// Auto-generated method stub
							JTextField numberTf = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
							Object obj = table.getValueAt(row, column);
							if( obj instanceof CellValue){
								CellValue cellValue = (CellValue)obj;
								numberTf.setText(cellValue.getValue());
							}else{
								numberTf.setText(obj.toString());
							}
							
							int tfLength = numberTf.getText().length();
							if( tfLength > 0){
								numberTf.setSelectionStart( 0 );
								numberTf.setSelectionEnd( tfLength );
							}
							return numberTf;
						}

					};
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}				
				//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가 by 전성용(20201223)	
				else if( MASTER_LIST_WEIGHT_MANAGEMENT_IDX == i){
					tmpArray = CustomUtil.getLOVDisplayValues(session, "S7_O");
					combo = new JComboBox(tmpArray);
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
					
				}else if( MASTER_LIST_MODULE_IDX == i){
					tmpArray = CustomUtil.getLOVDisplayValues(session, "S7_MODULE_CODE");
					combo = new JComboBox(tmpArray);
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}else if( MASTER_LIST_SUPPLY_MODE_IDX == i){
					tmpArray = CustomUtil.getLOVDisplayValues(session, "S7_SUPPLY_MODE");
					combo = new JComboBox(tmpArray);
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}else if( MASTER_LIST_ALTER_PART_IDX == i){
					tf = new JTextField();
					doc = (PlainDocument) tf.getDocument();
					doc.setDocumentFilter(new TextFieldFilter(3));
					editor = new DefaultCellEditor(tf){

						@Override
						public Component getTableCellEditorComponent(
								JTable table, Object value, boolean isSelected,
								int row, int column) {
							JTextField tf = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
							int tfLength = tf.getText().length();
							if( tfLength > 0){
								tf.setSelectionStart( 0 );
								tf.setSelectionEnd( tfLength );
							}
							return tf;
						}
						
					};
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}else if( MASTER_LIST_DR_IDX == i){
					tmpArray = CustomUtil.getLOVDisplayValues(session, "S7_CATEGORY");
					combo = new JComboBox(tmpArray);
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				//20200914 seho EJS Column 추가.
				}else if( MASTER_LIST_EJS_IDX == i){
					tmpArray = CustomUtil.getLOVDisplayValues(session, "S7_OX");
					combo = new JComboBox(tmpArray);
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}else if( MASTER_LIST_RESPONSIBILITY_IDX == i){
					ArrayList<String> lovs = new ArrayList<String>(); 
					tmpArray = CustomUtil.getLOVDisplayValues(session, "s7_RESPONSIBILITY");
					for(int b=0; b<tmpArray.length; b++){
						if(!tmpArray[b].startsWith("White Box") && !tmpArray[b].startsWith("Black Box") && !tmpArray[b].startsWith("Gray Box") && !tmpArray[b].startsWith("SYMC")){
							lovs.add(tmpArray[b]);
						}
					}
					combo = new JComboBox(lovs.toArray(new String[lovs.size()]));
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}else if( MASTER_LIST_CON_DWG_TYPE_IDX == i){
					tmpArray = new String[]{"2D","3D", "BOTH"};
					combo = new JComboBox(tmpArray);
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}else if( MASTER_LIST_DWG_DEPLOYABLE_DATE_IDX == i){
					DateCellEditor dateCellEditor = new DateCellEditor(new JTextField());
					dateCellEditor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(dateCellEditor);
				}
				//[SR170703-020][LJG]Proto Tooling 컬럼 추가
				else if( MASTER_LIST_PROTO_TOOLING_IDX == i){
					combo = new JComboBox(new String[]{"", "Y"});
					editor = new ComboCellEditor(combo);
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}
				else{
					editor = new DefaultCellEditor(new JTextField()){

						@Override
						public Component getTableCellEditorComponent(
								JTable table, Object value, boolean isSelected,
								int row, int column) {
							JTextField tf = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
							int tfLength = tf.getText().length();
							if( tfLength > 0){
								tf.setSelectionStart( 0 );
								tf.setSelectionEnd( tfLength );
							}
							return tf;
						}
						
					};
					editor.setClickCountToStart(2);
					table.getColumnModel().getColumn(i).setCellEditor(editor);
				}
					
				int startIdx = fixedColumnPre.length - 1;
				int endIdx = fixedColumnPre.length + ospec.getTrimList().size() - 1;
				if( i >= startIdx && i < endIdx){
					table.getColumnModel().getColumn(i).setCellEditor(new UsageCellEditor(new JTextField()));
				}
			}
			
		}
	}
	
//	private void changeUsageType(ItemEvent itemevent){
//		JComboBox combo = (JComboBox)itemevent.getSource();
//		int startIdx = currentPreColumnList.size() - 1;
//		int endIdx = currentPreColumnList.size() + ospec.getTrimList().size() - 1;
//		int selectedColumn = table.getSelectedColumn();
//		int selectedRow = table.getSelectedRow();
//		
//		String selectedStr = null;
//		Object selectedObj = combo.getSelectedItem();
//		if( selectedObj instanceof CellValue){
//			selectedStr = ((CellValue)selectedObj).getValue();
//		}else{
//			selectedStr = selectedObj.toString();
//		}
//		
//		if( selectedStr.equals(MASTER_LIST_STD)){
//			for( int i = startIdx; i < endIdx; i++){
//				Object obj = table.getValueAt(selectedRow, i);
//				if( obj instanceof CellValue){
//					CellValue cellValue = (CellValue)obj;
//					String valueStr = cellValue.getValue();
//					try{
//						Double.parseDouble(valueStr);
//					}catch(Exception e){
//						valueStr = valueStr.replaceAll("\\(", "");
//						valueStr = valueStr.replaceAll("\\)", "");
//						cellValue.setValue(valueStr);
//						cellValue.setSortValue(valueStr);
//					}
//				}else{
//					String valueStr = obj.toString();
//					try{
//						Double.parseDouble(valueStr);
//					}catch(Exception e){
//						valueStr = valueStr.replaceAll("\\(", "");
//						valueStr = valueStr.replaceAll("\\)", "");
//						CellValue value = new CellValue(valueStr);
//						table.setValueAt(value, selectedRow, i);
//					}
//				}
//			}
//		}else if(selectedStr.equals(MASTER_LIST_OPT)){
//			for( int i = startIdx; i < endIdx; i++){
//				Object obj = table.getValueAt(selectedRow, i);
//				if( obj instanceof CellValue){
//					CellValue cellValue = (CellValue)obj;
//					String valueStr = cellValue.getValue();
//					try{
//						Double.parseDouble(valueStr);
//						valueStr = "(" + valueStr + ")";
//						cellValue.setValue(valueStr);
//						cellValue.setSortValue(valueStr);
//					}catch(Exception e){
//						e.printStackTrace();
//					}
//				}else{
//					String valueStr = obj.toString();
//					try{
//						Double.parseDouble(valueStr);
//						valueStr = "(" + valueStr + ")";
//						CellValue value = new CellValue(valueStr);
//						table.setValueAt(value, selectedRow, i);
//					}catch(Exception e){
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//		
//		table.repaint();
//	}
	
	public OSpec getOspec() {
		return ospec;
	}

	public JTable getFixedTable() {
		return fixedTable;
	}

	public JTable getTable() {
		return table;
	}
	
	public Vector createTableHeader(Vector headerVector){
		headerVector.clear();

		int columnIdx = 0;
		for (int i = 0; i < fixedColumnPre.length; i++) {
			headerVector.add(fixedColumnPre[i]);
		}

		int tmpColumIdx = fixedColumnPre.length;
		ArrayList<OpTrim> trimList = ospec.getTrimList();
		for (OpTrim trim : trimList) {
			headerVector.add(new FilterColumn(trim.getTrim(), "", tmpColumIdx++) );
		}

		for (int i = 0; i < fixedColumnPost.length; i++) {
			headerVector.add(fixedColumnPost[i]);
		}
		
		for( int i = 0; i < headerVector.size(); i++){
			columnMap.put(i, headerVector.get(i).toString());
		}
		
		return headerVector;
	}

	public Vector getHeader() {
		return headerVector;
	}
	
	public TableColumn getTableColumn(JTable table, int modelColumn, int fixedColumnCount){
		TableColumnModel cm = table.getColumnModel();
		for( int i = 0; i < cm.getColumnCount(); i++){
			int order = -1;
			if( cm.getColumn(i).getHeaderRenderer() == null){
				continue;
			}
			
			FilterColumn column = ((FilterRenderer)(cm.getColumn(i).getHeaderRenderer())).getFilterColumn();
			order = column.getOrder();
			if( order == (modelColumn + fixedColumnCount)){
				return cm.getColumn(i);
			}
		}
		return null;
	}

	public void setGroupColumn(final JTable table,
			ArrayList<OpTrim> trimList, int fixedColumnCount, GroupableTableHeader header, ArrayList currentPreColumnList) {

		ArrayList<ColumnGroup> columnGroupList = new ArrayList();
		ColumnGroup usageGroup = new ColumnGroup("Usage");
		
		ColumnGroup columnGroup = new ColumnGroup("ENG. CONCEPT");
		
		TableColumn tableColumn = getTableColumn(table, 14, fixedColumnCount);
		if( tableColumn != null){
			columnGroup.add(tableColumn);
		}
		
		tableColumn = getTableColumn(table, 15, fixedColumnCount);
		if( tableColumn != null){
			columnGroup.add(tableColumn);
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
//		columnGroup = new ColumnGroup("통합U/S");
//		tableColumn = getTableColumn(table, 19, fixedColumnCount);
//		if( tableColumn != null){
//			columnGroup.add(tableColumn);
//		}
//		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
//			columnGroupList.add(columnGroup);
//		}
		
		ColumnGroup preAreaGroup = null;
		ColumnGroup prePassGroup = null;
		ColumnGroup preEngineGroup = null;
		ColumnGroup preGradeGroup = null;

		ColumnGroup gradeColumnGroup = null;
		ColumnGroup engineColumnGroup = null;
		ColumnGroup passColumnGroup = null;
		ColumnGroup areaColumnGroup = null;

		
		boolean isEqual = false;
		for (OpTrim trim : trimList) {

			isEqual = false;

			if (preAreaGroup == null) {
				areaColumnGroup = new ColumnGroup(trim.getArea());
				preAreaGroup = areaColumnGroup;
				isEqual = false;
			} else {
				if (trim.getArea().equals(
						preAreaGroup.getHeaderValue().toString())) {
					areaColumnGroup = preAreaGroup;
					isEqual = true;
				} else {
					areaColumnGroup = new ColumnGroup(trim.getArea());
					preAreaGroup = areaColumnGroup;
					isEqual = false;
				}
			}

			if (prePassGroup == null) {
				passColumnGroup = new ColumnGroup(trim.getPassenger());
				prePassGroup = passColumnGroup;
				isEqual = false;
			} else {
				if (isEqual
						&& trim.getPassenger().equals(
								prePassGroup.getHeaderValue().toString())) {
					passColumnGroup = prePassGroup;
					isEqual = true;
				} else {
					passColumnGroup = new ColumnGroup(trim.getPassenger());
					prePassGroup = passColumnGroup;
					isEqual = false;
				}
			}
			if (!areaColumnGroup.getColumnGroups().contains(passColumnGroup)){
				areaColumnGroup.add(passColumnGroup);
			}
			
			if (preEngineGroup == null) {
				engineColumnGroup = new ColumnGroup(trim.getEngine());
				preEngineGroup = engineColumnGroup;
				isEqual = false;
			} else {
				if (isEqual
						&& trim.getEngine().equals(
								preEngineGroup.getHeaderValue().toString())) {
					engineColumnGroup = preEngineGroup;
					isEqual = true;
				} else {
					engineColumnGroup = new ColumnGroup(trim.getEngine());
					preEngineGroup = engineColumnGroup;
					isEqual = false;
				}
			}
			if (!passColumnGroup.getColumnGroups().contains(
					engineColumnGroup))
				passColumnGroup.add(engineColumnGroup);

			// Grade Group 생성.
			if (preGradeGroup == null) {
				gradeColumnGroup = new ColumnGroup(trim.getGrade());
				preGradeGroup = gradeColumnGroup;
				isEqual = false;
			} else {
				if (isEqual
						&& trim.getGrade().equals(
								preGradeGroup.getHeaderValue().toString())) {
					gradeColumnGroup = preGradeGroup;
					isEqual = true;
				} else {
					gradeColumnGroup = new ColumnGroup(trim.getGrade());
					preGradeGroup = gradeColumnGroup;
					isEqual = false;
				}
			}
			if (!engineColumnGroup.getColumnGroups().contains(
					gradeColumnGroup))
				engineColumnGroup.add(gradeColumnGroup);

			tableColumn = getTableColumn(table, table.convertColumnIndexToModel( trim.getColOrder() + currentPreColumnList.size() - fixedColumnCount), fixedColumnCount);
			if (tableColumn != null && !gradeColumnGroup.getColumnGroups().contains(tableColumn)) {
				gradeColumnGroup.add(tableColumn);
			}

			Vector v = usageGroup.getColumnGroups();
			if( !v.contains(areaColumnGroup)){
				usageGroup.add(areaColumnGroup);
			}
		}
		columnGroupList.add(usageGroup);
		
		columnGroup = new ColumnGroup("WEIGHT(KG)");
		for( int k = 0; k < 2; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
		columnGroup = new ColumnGroup("MATERIAL COST");
		for( int k = 9; k < 11; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
//		columnGroup = new ColumnGroup("CIC");
//		for( int k = 9; k < 10; k++){
//			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
//			if( tableColumn != null){
//				columnGroup.add(tableColumn);	
//			}	
//		}
//		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
//			columnGroupList.add(columnGroup);
//		}
		
		columnGroup = new ColumnGroup("DVP SAMPLE");
		for( int k = 11; k < 14; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}	
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
            columnGroupList.add(columnGroup);
        }
//		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
//			ColumnGroup columnGroup2 = new ColumnGroup("PRT-TEST");
//			columnGroup2.add(columnGroup);
//			columnGroupList.add(columnGroup2);
//		}
		
		columnGroup = new ColumnGroup("CONCEPT DWG");
		for( int k = 14; k < 18; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
		columnGroup = new ColumnGroup("PRD. DWG");
		for( int k = 18; k < 21; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}	
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
		columnGroup = new ColumnGroup("Design Concept Doc.");
		for( int k = 21; k < 24; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}	
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
		columnGroup = new ColumnGroup("DESIGN CHARGE");
		for( int k = 24; k < 26; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}	
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
		columnGroup = new ColumnGroup("EST. INVESTMENT COST");
		for( int k = 27; k < 28; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}	
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
		columnGroup = new ColumnGroup("PRD INVENSTMENT COST");
		for( int k = 28; k < 32; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}	
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
		columnGroup = new ColumnGroup("PROCUMENT");
		for( int k = 32; k < 34; k++){
			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
			if( tableColumn != null){
				columnGroup.add(tableColumn);	
			}	
		}
		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
			columnGroupList.add(columnGroup);
		}
		
//		columnGroup = new ColumnGroup("구매담당");
//		for( int k = 26; k < 29; k++){
//			tableColumn = getTableColumn(table, fixedColumnPre.length - fixedColumnCount + trimList.size() + k, fixedColumnCount);
//			if( tableColumn != null){
//				columnGroup.add(tableColumn);	
//			}	
//		}
//		if( columnGroup.getColumnGroups() != null && !columnGroup.getColumnGroups().isEmpty()){
//			columnGroupList.add(columnGroup);
//		}
		
		for (ColumnGroup cg : columnGroupList) {
			header.addColumnGroup(cg);
		}
	}

	private void editColumnAt(Point p) {
		int columnIndex = header.columnAtPoint(p);

		if (columnIndex != -1) {

			Rectangle columnRectangle = header.getHeaderRect(columnIndex);

			selectedColumn = table.convertColumnIndexToModel( columnIndex );
			HeaderPopupFilter filter = headerPopup.getFilter();
			
			ArrayList<String> filterList = filterMap.get(selectedColumn);
			if( filterList == null || filterList.isEmpty()){
				filter.getTfFilter().setText("");
			}else{
				if( filterList.size() == 1){
					filter.getTfFilter().setText(filterList.get(0).toString());
				}else{
					filter.getTfFilter().setText("**");
				}
			}
			
			headerPopup.setColumnIdx(selectedColumn);
			filter.reload();
			headerPopup.show(header, columnRectangle.x + 20, 81 + 30);
			table.repaint();
		}
	}

	private void paste() {
		
		if( !parentDlg.isEditable()){
			return;
		}
		
		int selectedColumn = table.getSelectedColumn();
		int selectedRow = table.getSelectedRow();
		
//		if (copiedRows == null || copiedRows.length == 0) {
//			return;
//		}

		for (int row = 0; copiedRows != null && row < copiedRows.length; row++) {
			for (int column = 0; copiedColumns != null && column < copiedColumns.length; column++) {
				if (selectedRow + row >= table.getRowCount()) {
					System.out.println("Out of Row Index!!!!!!");
					return;
				}

				if (selectedColumn + column >= table.getColumnCount()) {
					System.out.println("Out of Column Index!!!!!!");
					return;
				}
			}
		}

		ArrayList<Integer> rowList = new ArrayList();
		ArrayList<Integer> columnList = new ArrayList();
		
		if( copiedRows != null && copiedColumns != null){
			if( copiedRows.length == 1 && copiedColumns.length == 1){
				int[] selectedRows = table.getSelectedRows();
				int[] selectedColumns = table.getSelectedColumns();
				if( selectedRows.length > 0 && selectedColumns.length > 0){
					
					Object obj = table.getValueAt(copiedRows[0],
							copiedColumns[0]);
					for( int i = 0; i < selectedRows.length; i++){
						for( int j = 0; j < selectedColumns.length; j++){
							
							int modelColumn = table.convertColumnIndexToModel(selectedColumns[j]);
							if( notEditableColumn.contains(modelColumn)){
								continue;
							}
							
							if (obj instanceof CellValue) {
								CellValue cellValue = (CellValue) obj;
								obj = new CellValue(cellValue.getValue());
								// cellValue.setSortValue(cellValue.getValue());
							} else if (obj instanceof String) {
								obj = new CellValue(obj.toString(), obj.toString(), 0);
							}
							table.setValueAt(obj, selectedRows[i], selectedColumns[j]);
			
							rowList.add(selectedRows[i]);
							columnList.add(selectedColumns[j]);
						}
					}
				}
			}else{
				for (int row = 0; row < copiedRows.length; row++) {
					for (int column = 0; column < copiedColumns.length; column++) {
						
						int modelColumn = table.convertColumnIndexToModel(selectedColumn + column);
						if( notEditableColumn.contains(modelColumn)){
							continue;
						}
						
						Object obj = table.getValueAt(copiedRows[row],
								copiedColumns[column]);
						if (obj instanceof CellValue) {
							CellValue cellValue = (CellValue) obj;
							obj = new CellValue(cellValue.getValue());
							// cellValue.setSortValue(cellValue.getValue());
						} else if (obj instanceof String) {
							obj = new CellValue(obj.toString(), obj.toString(), 0);
						}
						table.setValueAt(obj, selectedRow + row, selectedColumn
								+ column);
		
						rowList.add(selectedRow + row);
						columnList.add(selectedColumn + column);
		
					}
				}	
				// [20161111][ymajng] Paste시 index out of bound 오류 수정
				if (rowList.size() > 0) {
					table.setRowSelectionInterval(rowList.get(0),
							rowList.get(rowList.size() - 1));
				}
				if (columnList.size() > 0) {
					table.setColumnSelectionInterval(columnList.get(0),
							columnList.get(columnList.size() - 1));
				}
			}
		}
		
		
		TextTransfer textTransfer = new TextTransfer();
		String contents = textTransfer.getClipboardContents();
		if( contents != null && !contents.equals("")){
			String[] contentsArray = contents.split("\n");
			for( int i = 0; i < contentsArray.length; i++){
				String[] rowContents = contentsArray[i].split("\t");
				for( int j = 0; j < rowContents.length; j++){
					if( selectedRow + i >= table.getRowCount()){
						continue;
					}
					
					int modelColumn = table.convertColumnIndexToModel(selectedColumn + j);
					if( notEditableColumn.contains(modelColumn)){
						continue;
					}
					
					if( (selectedColumn + j >= table.getColumnCount())){
						continue;
					}
					
					table.setValueAt(rowContents[j], selectedRow + i, selectedColumn + j);
				}
			}
			textTransfer.setClipboardContents(null);
			table.repaint();
			return;
		}
		
		table.repaint();
	}

	private void copy() {
		TextTransfer textTransfer = new TextTransfer();
		
		
		copiedColumns = table.getSelectedColumns();
		Arrays.sort(copiedColumns);
		copiedRows = table.getSelectedRows();
		Arrays.sort(copiedRows);
		
		//Excel로 Paste 할때
		int preColumn = -1;
		String contents = null;
		for( int i = 0; i < copiedRows.length; i++){
			String rowContents = "";
			for( int j = 0; j < copiedColumns.length; j++){
				String str = table.getValueAt(copiedRows[i], copiedColumns[j]).toString();
				if( j > 0){
					while( preColumn < copiedColumns[j]){
						rowContents += "\t";
						preColumn ++;
					}
					
					rowContents += str;
				}else{
					rowContents = str;
					preColumn = copiedColumns[j];
				}
			}
			if( contents == null){
				contents = rowContents;
			}else{
				contents += "\n" + rowContents;
			}
		}
		textTransfer.setClipboardContents(contents);
		
		table.repaint();
	}
	
	private void delete(){
		int[] selectedColumns = table.getSelectedColumns();
		Arrays.sort(selectedColumns);
		int[] selectedRows = table.getSelectedRows();
		Arrays.sort(selectedRows);
		
		for( int i = 0; i < selectedRows.length; i++){
			for( int j = 0; j < selectedColumns.length; j++){
				
				int modelRow = table.convertRowIndexToModel(selectedRows[i]);
				int modelColumn = table.convertColumnIndexToModel(selectedColumns[j]);
				if( notEditableColumn.contains(modelColumn)){
					continue;
				}
				
				//Part ID Column에는 System Row Key가 저장되어 있으므로 Value만 수정한다.
				if( modelColumn == MASTER_LIST_PART_ID_IDX){
					Object obj = model.getValueAt(modelRow, modelColumn);
					if( obj instanceof CellValue){
						CellValue cellValue = (CellValue)obj;
						cellValue.setValue("");
					}else{
						model.setValueAt(new CellValue(""), modelRow, modelColumn);
					}
				}else{
					model.setValueAt(new CellValue(""), modelRow, modelColumn);
				}
			}
		}
		table.repaint();
	}
	
	public String getKeyInModel(int modelRow){
		Object obj = model.getValueAt(modelRow, MASTER_LIST_PART_ID_IDX);
		if( obj instanceof CellValue){
			CellValue cellValue = (CellValue)obj;
			HashMap<String, Object> cellData = cellValue.getData();
			if( cellData == null){
				return null;
			}else{
				return (String)cellData.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
			}
		}else{
			return null;
		}
		
	}
	
	class ValueCellRenderer extends DefaultTableCellRenderer{

		@Override
		public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
			Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if( isSelected ){
				com.setBackground((Color)UIManager.get("Table.selectionBackground"));
//				com.setBackground(new Color(51, 153, 255));
				com.setForeground(Color.WHITE);
			}else{
				if( row % 2 == 0){
					com.setBackground(Color.WHITE);
				}else{
					com.setBackground(new Color(236,245, 255));
				}
				com.setForeground(Color.BLACK);
			}
			
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			int modelColumn = table.convertColumnIndexToModel(column);
			JLabel label = (JLabel)com;
			String uom = "";
			if( modelColumn == MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX){
				int modelRow = table.convertRowIndexToModel(row);
				CellValue partIdCellValue = (CellValue)model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
				HashMap data = partIdCellValue.getData();
				if( data != null){
					uom = (String)data.get(PropertyConstant.ATTR_NAME_UOMTAG);
					if( uom == null){
						uom = "";
					}
				}
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setText(label.getText() + " " + uom);
			}else if( modelColumn == MASTER_LIST_EST_COST_MATERIAL_IDX || modelColumn == MASTER_LIST_TARGET_COST_MATERIAL_IDX 
					|| modelColumn == MASTER_LIST_PRT_TOOLG_INVESTMENT_IDX || modelColumn == MASTER_LIST_PRD_TOOL_COST_IDX
					|| modelColumn == MASTER_LIST_PRD_SERVICE_COST_IDX || modelColumn == MASTER_LIST_PRD_SAMPLE_COST_IDX
					|| modelColumn == MASTER_LIST_PRD_SUM_IDX){
				
//				int modelRow = table.convertRowIndexToModel(row);
//				CellValue partIdCellValue = (CellValue)model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
//				HashMap<String, Object> cellData = partIdCellValue.getData();
//				if( !parentDlg.getCurrentUserId().equals(cellData.get(PropertyConstant.ATTR_NAME_OWNINGUSER))){
//					label.setText("******");
//				}else{
//					if( !parentDlg.getCurrentUserGroup().equals("") && parentDlg.getCurrentUserGroup().equalsIgnoreCase(cellData.get(PropertyConstant.ATTR_NAME_OWNINGGROUP).toString()) 
//							&& parentDlg.isCordinator()){
//					}else{
//						label.setText("******");
//					}
//				}
				
//				CellValue deptCellValue = null, respCellValue = null;
//				Object obj = model.getValueAt(modelRow, MASTER_LIST_ENG_DEPT_NM_IDX);
//				if( !(obj instanceof CellValue)){
//					deptCellValue = new CellValue(obj.toString());
//				}else{
//					deptCellValue = (CellValue)obj;
//				}
//				if( deptCellValue == null){
//					deptCellValue = new CellValue("");
//				}
//				
//				obj = model.getValueAt(modelRow, MASTER_LIST_ENG_RESPONSIBILITY_IDX);
//				if( !(obj instanceof CellValue)){
//					respCellValue = new CellValue(obj.toString());
//				}else{
//					respCellValue = (CellValue)obj;
//				}
//				
//				if( respCellValue == null){
//					respCellValue = new CellValue("");
//				}
//				
//				if( parentDlg.getCurrentUserGroup().equalsIgnoreCase(deptCellValue.getValue())
//						&& parentDlg.getCurrentUserName().equalsIgnoreCase(respCellValue.getValue())){
//				}else{
//					if( !parentDlg.getCurrentUserGroup().equals("") && parentDlg.getCurrentUserGroup().equalsIgnoreCase(deptCellValue.getValue()) && parentDlg.isCordinator()){
//					}else{
//						label.setText("******");
//					}
//				}
				
			}
			
			if( !isSelected){
				try {
					setDifferentCellColor(label, table, row, column);
				} catch (TCException e) {
					e.printStackTrace();
				}				
			}
			
			return com;
		}
		
		private void setDifferentCellColor(JComponent jc, JTable table, int row, int column) throws TCException{
			
			//현재 Structure의 BOM 정보.
			HashMap<String, Vector> keyRowMapper = parentDlg.getKeyRowMapper();
			
			//Latest Release기준의 BOM 정보.
			HashMap<String, Vector> releaseKeyRowMapper = parentDlg.getReleaseKeyRowMapper();
			
			if( !keyRowMapper.isEmpty() || !releaseKeyRowMapper.isEmpty()){
				int modelRow = table.convertRowIndexToModel(row);
				int modelColumn = table.convertColumnIndexToModel(column);
				String key = getKeyInModel(modelRow);
				Vector rowVector = keyRowMapper.get(key);
				Vector releaseRowVector = null;
				if( releaseKeyRowMapper != null){
					releaseRowVector = releaseKeyRowMapper.get(key);
				}
				if( (!isCompareWithRelease() && rowVector == null) || (isCompareWithRelease() && releaseRowVector == null)){
					jc.setToolTipText("Line added.");
					jc.setBackground(Color.orange);
					table.repaint();
					return;
				}
				
				Vector compareVector = null;
				if( isCompareWithRelease()){
					compareVector = releaseRowVector;
				}else{
					compareVector = rowVector;
				}
				
				if( !table.getValueAt(row, column).toString().equals(compareVector.get(modelColumn + fixColumn).toString())){
					jc.setToolTipText(compareVector.get(modelColumn + fixColumn).toString() + " ==> " + table.getValueAt(row, column));
					jc.setBackground(Color.orange);
				}else{
					jc.setToolTipText(null);
				}
			}
		}
		
	}

	public class FilterRenderer extends DefaultTableCellRenderer {
		JTextField tf = new JTextField();
		FilterColumn filterColumn = null;
		public FilterRenderer(FilterColumn filterColumn) {
			this.filterColumn = filterColumn;
		}

		@Override
		public Component getTableCellRendererComponent(JTable jtable,
				Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
			// Auto-generated method stub
			JPanel panel = new JPanel(new BorderLayout());
			JLabel header = new JLabel();

			header.setForeground(table.getTableHeader().getForeground());
			header.setBackground(table.getTableHeader().getBackground());
			header.setFont(table.getTableHeader().getFont());

			header.setHorizontalAlignment(JLabel.CENTER);
			header.setText(obj.toString());
			header.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			
			int modelColumn = table.convertColumnIndexToModel(column);
			if( designerReqColumn.contains(modelColumn)){
				header.setForeground(Color.RED);
			}else{
				header.setForeground(Color.BLACK);
			}
			
			if( jtable.equals(table)){
				List<RowSorter.SortKey> list = (List<RowSorter.SortKey>)sorter.getSortKeys();
				for( RowSorter.SortKey sortKey : list){
					if( sortKey.getColumn() == modelColumn){
						if (sortKey.getSortOrder().equals(SortOrder.ASCENDING)) {
							header.setIcon(new ImageIcon(MasterListTablePanel.class.getResource("/icons/sort-ascend.png")));	
						}else{
							header.setIcon(new ImageIcon(MasterListTablePanel.class.getResource("/icons/sort-descend.png")));							
						}
					}
				}
			}else if(jtable.equals(fixedTable)){
				header.setIcon(new ImageIcon(MasterListTablePanel.class.getResource("/icons/add_16.png")));
			}
			
			panel.add(header, BorderLayout.CENTER);
			tf.setBackground(Color.LIGHT_GRAY);
			panel.add(tf, BorderLayout.SOUTH);
			return panel;
		}

		public JTextField getFilterTf() {
			return tf;
		}
		
		public FilterColumn getFilterColumn(){
			return filterColumn;
		}

	}

	class CopyAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Auto-generated method stub
			copy();
		}
	}

	class PasteAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Auto-generated method stub
			paste();
		}
	}
	
	class DeleteAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Auto-generated method stub
			delete();
		}
	}
	public class FilterColumn implements Comparable{

		String columnName = null;
		String columnDesc = null;
		String filter = null;
		int order = -1;

		public FilterColumn(String columnName, String columnDesc, String filter, int order) {
			this.columnName = columnName;
			this.columnDesc = columnDesc;
			this.filter = filter;
			this.order = order;
		}
		
		public FilterColumn(String columnName, String filter, int order) {
			this(columnName, columnName, filter, order);
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public String getColumnDesc() {
			return columnDesc;
		}

		public void setColumnDesc(String columnDesc) {
			this.columnDesc = columnDesc;
		}

		public String getFilter() {
			return filter;
		}

		public void setFilter(String filter) {
			this.filter = filter;
		}

		@Override
		public String toString() {
			// Auto-generated method stub
			return columnName;
		}

		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		@Override
		public int compareTo(Object o) {
			// Auto-generated method stub
			if( o instanceof FilterColumn){
				FilterColumn fc = (FilterColumn)o;
				if( order > fc.getOrder()){
					return 1;
				}else if( order < fc.getOrder()){
					return -1;
				}else{
					return 0;
				}
			}else{
				return toString().compareTo(o.toString());
			}
//			return columnName.compareTo(o.getColumnName());
		}

		@Override
		public boolean equals(Object obj) {
			// Auto-generated method stub
			if( obj instanceof FilterColumn){
				FilterColumn filterColumn = (FilterColumn)obj;
				return columnName.equals(filterColumn.getColumnName()) && order == filterColumn.getOrder(); 
			}else{
				return super.equals(obj);
			}
		}

	}

	public void setFilterValue(JTextField src) {
		int viewIdx = table.convertColumnIndexToView(selectedColumn);
		FilterRenderer renderer = (FilterRenderer) header.getColumnModel()
				.getColumn(viewIdx).getHeaderRenderer();
		JTextField tf = renderer.getFilterTf();
		JTextField text = src;
		tf.setText(text.getText());

		ArrayList<String> filterList = filterMap.get(selectedColumn);
		if(	filterList == null){
			filterList = new ArrayList();
			filterList.add(text.getText());
			filterMap.put(selectedColumn, filterList);
		}else{
			if( !filterList.contains(text.getText()) ){
				filterList.add(text.getText());
			}
		}
		table.getTableHeader().repaint();
	}

	private void clearSortValue() {
		int columnIdx = headerPopup.getColumnIdx();
		if (columnIdx > -1) {
			for (int i = 0; i < data.size(); i++) {
				Vector row = data.get(i);
				Object obj = row.get(columnIdx + fixColumn);
				if( obj == null){
					obj = "";
				}
				if (obj instanceof CellValue) {
					CellValue cellValue = (CellValue) obj;
					cellValue.clearSortValue();
					cellValue.clearOrder();
				}else{
					row.set(columnIdx + fixColumn, new CellValue(obj.toString(), obj.toString(), 0));
				}

				obj = model.getValueAt(i, columnIdx);
				if( obj == null){
					obj = "";
				}
				if (obj instanceof CellValue) {
					CellValue cellValue = (CellValue) obj;
					cellValue.clearSortValue();
					cellValue.clearOrder();
				}else{
					model.setValueAt(new CellValue(obj.toString(), obj.toString(), 0), i, columnIdx);
				}
			}
		}
	}
	
	private void addRow(MouseEvent mouseEvent) throws Exception{
		//마지막 Row Idx
		int rowIdx = fixedTable.getRowCount() - 1;
		int modelRowIdx = -1;
		if( rowIdx > -1){
			modelRowIdx = fixedTable.convertRowIndexToModel(rowIdx);
		}
		int columnCount = model.getColumnCount();
		Vector<CellValue> newRow = new Vector();
		newRow.add(new CellValue("" + model.getRowCount(), "" + model.getRowCount(), 0));

		List<RowSorter.SortKey> list = (List<RowSorter.SortKey>)sorter.getSortKeys();

		for (int i = 0; i < columnCount; i++) {

			CellValue newCellValue = null;
			Object obj = null;
			//Row가 존재하면.
			if( rowIdx > -1){
				obj = table.getValueAt(rowIdx,i);
			}
			
			if( obj instanceof CellValue){
				CellValue cellValue = (CellValue) obj;
				
				int newOrder = -1;
				String sortValue = "";
				boolean bFound = false;
				for( RowSorter.SortKey sortKey : list ){
					
					if( sortKey.getColumn() == i){
						if (sortKey.getSortOrder().equals(SortOrder.ASCENDING)) {
							newOrder = cellValue.getOrder() + 1;
							sortValue = cellValue.getSortValue();
						} else {
							newOrder = cellValue.getOrder() - 1;
							sortValue = "";
						}
						newCellValue = new CellValue("", sortValue, newOrder);
						bFound = true;
						break;
					}
				}
				
				if(!bFound){
					modelRowIdx = sorter.convertRowIndexToModel(rowIdx);
					newCellValue = new CellValue("", "", 0);
				}
				
				newRow.add(newCellValue);
			}else{
				if( obj == null){
					obj = "";
				}
				CellValue cellValue = new CellValue("", "" + obj, 0);
				int newOrder = -1;
				String sortValue = "";
				boolean bFound = false;
				for( RowSorter.SortKey sortKey : list ){
					
					if( sortKey.getColumn() == i){
						if (sortKey.getSortOrder().equals(SortOrder.ASCENDING)) {
							newOrder = cellValue.getOrder() + 1;
							sortValue = cellValue.getSortValue();
						} else {
							newOrder = cellValue.getOrder() - 1;
							sortValue = "";
						}
						newCellValue = new CellValue("", sortValue, newOrder);
						bFound = true;
						break;
					}
				}
				
				if( newCellValue == null){
					newCellValue = cellValue; 
				}
				
				newRow.add(newCellValue);
			}
			
			if( i == MASTER_LIST_PART_ID_IDX){
				HashMap cellData = new HashMap();
				cellData.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, BomUtil.getNewSystemRowKey());
				cellData.put(PropertyConstant.ATTR_NAME_UOMTAG, "EA");
				newCellValue.setData(cellData);
			}else if( i == MASTER_LIST_NMCD_IDX){
				newCellValue.setValue("N");
			}else if( i == MASTER_LIST_PROJECT_IDX){
				newCellValue.setValue(parentDlg.getProject());
			}else if( i == MASTER_LIST_ENG_DEPT_NM_IDX){
				newCellValue.setValue(parentDlg.getCurrentUserPa6Group());
			}else if( i == MASTER_LIST_ENG_RESPONSIBILITY_IDX){
				newCellValue.setValue(parentDlg.getCurrentUserName());
			}

			ArrayList filterList = filterMap.get( i );
			if( filterList != null && !filterList.isEmpty()){
				if( rowIdx > -1){
					Object obj2 = table.getValueAt(rowIdx, i);
					CellValue baseValue = null;
					if( obj2 instanceof CellValue){
						baseValue = (CellValue)obj2;
					}else{
						baseValue = new CellValue(obj2.toString(), obj2.toString(), 0);
					}
					newCellValue.setSortValue(baseValue.getSortValue());
//					newCellValue.setOrder(baseValue.getOrder() - 1);
				}else{
					newCellValue.setSortValue(filterList.get(0).toString());
					newCellValue.setOrder(0);
				}
			}
		}

		if( model.getRowCount() == 0){
			data.add((Vector) newRow.clone());
			model.addRow((Vector) newRow.clone());
		}else{
			if( list == null || list.isEmpty()){
				data.add((Vector) newRow.clone());
				model.addRow((Vector) newRow.clone());
			}else{
				if( modelRowIdx < 0){
					data.add((Vector) newRow.clone());
					model.addRow((Vector) newRow.clone());
				}else{
					data.add((Vector) newRow.clone());
					model.addRow((Vector) newRow.clone());
//					data.insertElementAt((Vector) newRow.clone(), modelRowIdx);
//					model.insertRow(modelRowIdx, (Vector) newRow.clone());
				}
			}
		}

		fixedTable.clearSelection();
		table.clearSelection();
		
		refreshRowNum();
	}

	private void insertEmptyRow(MouseEvent mouseEvent, int rowCount) throws Exception {

		// Auto-generated method stub
		int rowIdx = fixedTable.rowAtPoint(mouseEvent.getPoint());
		int columnCount = model.getColumnCount();
		int modelRowIdx = fixedTable.convertRowIndexToModel(rowIdx);
		Vector<CellValue> newRow = new Vector();
		newRow.add(new CellValue("" + rowIdx, "" + rowIdx, 0));

		List<RowSorter.SortKey> list = (List<RowSorter.SortKey>)sorter.getSortKeys();
//		sorter.setSortsOnUpdates(true);
		for (int i = 0; i < columnCount; i++) {

			CellValue newCellValue = null;
			Object obj = table.getValueAt(rowIdx,i);
			if( obj instanceof CellValue){
				CellValue cellValue = (CellValue) obj;
				
				int newOrder = -1;
				boolean bFound = false;
				for( RowSorter.SortKey sortKey : list ){
					
					if( sortKey.getColumn() == i){
						if (sortKey.getSortOrder().equals(SortOrder.ASCENDING)) {
							newOrder = cellValue.getOrder() - 1;
						} else {
							newOrder = cellValue.getOrder() + 1;
						}
						newCellValue = new CellValue("", cellValue.getSortValue(), newOrder);
						bFound = true;
						break;
					}
				}
				
				if(!bFound){
					modelRowIdx = sorter.convertRowIndexToModel(rowIdx);
					newCellValue = new CellValue("", "", 0);
					
				}
				newRow.add(newCellValue);
			}else{
				newCellValue = new CellValue("", "" + (obj == null ? "":obj), 0);
				newRow.add(newCellValue);
			}

			ArrayList filterList = filterMap.get( i );
			if( filterList != null && !filterList.isEmpty()){
				Object obj2 = model.getValueAt(rowIdx, i);
				if( obj2 == null){
					obj2 = "";
				}
				CellValue baseValue = null;
				if( obj2 instanceof CellValue){
					baseValue = (CellValue)obj2;
				}else{
					baseValue = new CellValue(obj2.toString(), obj2.toString(), 0);
				}
				newCellValue.setSortValue(baseValue.getSortValue());
//				newCellValue.setOrder(baseValue.getOrder() - 1);
			}
			
			if( i == MASTER_LIST_PART_ID_IDX){
				HashMap cellData = new HashMap();
				cellData.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, BomUtil.getNewSystemRowKey());
				cellData.put(PropertyConstant.ATTR_NAME_UOMTAG, "EA");
				newCellValue.setData(cellData);
			}else if( i == MASTER_LIST_NMCD_IDX){
				newCellValue.setValue("N");
			}else if( i == MASTER_LIST_PROJECT_IDX){
				newCellValue.setValue(parentDlg.getProject());
			}else if( i == MASTER_LIST_ENG_DEPT_NM_IDX){
				newCellValue.setValue(parentDlg.getCurrentUserPa6Group());
			}else if( i == MASTER_LIST_ENG_RESPONSIBILITY_IDX){
				newCellValue.setValue(parentDlg.getCurrentUserName());
			}
		}

		for (int j = 0; j < rowCount; j++) {
			data.insertElementAt((Vector) newRow.clone(), modelRowIdx);
			model.insertRow(modelRowIdx, (Vector) newRow.clone());
		}
//		sorter.sort();
		fixedTable.clearSelection();
		table.clearSelection();

		refreshRowNum();
//		sorter.setSortsOnUpdates(false);
	}
	
	public void refreshRowNum(){
		for( int i = fixedModel.getRowCount() - 1; i >= 0; i--){
			fixedModel.removeRow(i);
		}
		
		for (int i = 0; i < sorter.getViewRowCount(); i++) {
			Vector row = new Vector();
			row.add(i+1);
			fixedModel.addRow(row);
		}
		
		for (int i = 0; i < sorter.getViewRowCount(); i++) {
			int tableRowHeight = table.getRowHeight(i);
			if( tableRowHeight < 1){
				tableRowHeight = 25;
			}
			fixedTable.setRowHeight(i, tableRowHeight );
		}
		
		scroll.getRowHeader().getView().setPreferredSize(fixedTable.getPreferredSize());
		scroll.getRowHeader().getView().revalidate();
	}
	
	private void insertCopiedRow(MouseEvent mouseEvent) throws Exception{
		if (copiedColumns != null
				&& copiedColumns.length == table.getColumnCount()) {
			int rowIdx = fixedTable.rowAtPoint(mouseEvent
					.getPoint());

			List<RowSorter.SortKey> list = (List<RowSorter.SortKey>)sorter.getSortKeys();

			// Cell Copy.
//			ArrayList<Integer> tmpList = new ArrayList();
//			for( int i = 0; i < copiedColumns.length; i++){
//				tmpList.add(copiedColumns[i]);
//			}
			Vector<Vector<CellValue>> copiedData = new Vector();
			for (int row = 0; row < copiedRows.length; row++) {
				Vector<CellValue> copiedRow = new Vector();
				copiedRow.add(new CellValue("", "", 0));

				CellValue cellValue = null;
				for (int column = 0; column < model.getColumnCount(); column++) {
					
					Object obj = model.getValueAt( table.convertRowIndexToModel( copiedRows[row] ), column);
					if( notEditableColumn.contains(column)){
						cellValue = new CellValue("");
						
						if( column == MASTER_LIST_PART_ID_IDX){
							HashMap cellData = new HashMap();
							cellData.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, BomUtil.getNewSystemRowKey());
							
							CellValue partIdCellValue = (CellValue)obj;
							HashMap map = partIdCellValue.getData();
							cellData.put(PropertyConstant.ATTR_NAME_UOMTAG, map.get(PropertyConstant.ATTR_NAME_UOMTAG));
							cellValue.setData(cellData);
						}
						
						copiedRow.add(cellValue);
						continue;
					}
					
					cellValue = new CellValue(obj
							.toString(), obj.toString(), 0);
					
					copiedRow.add(cellValue);
				}
				copiedData.add(copiedRow);
			}

			int newOrder = 0;
			CellValue baseOrderCellValue = null;

			for (int j = copiedData.size() - 1; j >= 0; j--) {
				Vector<CellValue> newRow = copiedData.get(j);
				

				if (list != null && !list.isEmpty()) {
					//Column별 Sort에 대응하기 위해 .
					int tmpCount = 0;
					for( RowSorter.SortKey sortKey : list){
						CellValue cellValue = newRow.get(sortKey.getColumn() + 1);
						baseOrderCellValue = (CellValue) table.getValueAt(rowIdx, sortKey.getColumn());
						cellValue.setSortValue(baseOrderCellValue.getSortValue());
						
						newOrder = baseOrderCellValue.getOrder();
						cellValue.setOrder(newOrder);
					}
				}
				data.insertElementAt((Vector) newRow.clone(),
						sorter.convertRowIndexToModel(rowIdx));
				model.insertRow(sorter.convertRowIndexToModel(rowIdx),
						(Vector) newRow.clone());
			}
			
			fixedTable.clearSelection();
			table.clearSelection();

			refreshRowNum();

			fixedTable.setRowSelectionInterval(rowIdx,
					rowIdx + copiedData.size() - 1);

			copiedColumns = null;
			copiedRows = null;
		}
	}
	
	private String getPartId(String key){
		HashMap<String, Vector> keyRowMapper = parentDlg.getKeyRowMapper();
		Vector row = keyRowMapper.get(key);
		if( row == null){
			return null;
		}
		
		return row.get(MASTER_LIST_PART_ID_IDX + fixColumn).toString();
	}
	
	private ArrayList<Integer> getChildModelRows(String key, HashMap<String, Integer> keyIndexer, HashMap<String
			, ArrayList<String>> childRowKeyMap, HashMap<String, ArrayList<Integer>> partIdxMap){
		
		ArrayList<Integer> modelRows = new ArrayList();
		ArrayList<String> childRowKeys = childRowKeyMap.get(key);
		if( childRowKeys != null){
			for( String childRowKey : childRowKeys){
				Integer modelRow = keyIndexer.get(childRowKey);
				if( modelRow != null && !modelRows.contains(modelRow)){
					modelRows.add(modelRow);
				}
				String partId = getPartId(childRowKey);
				
				
				if( partId != null){
					ArrayList<Integer> rows = partIdxMap.get(partId);
					//현재 FMP내부에서 공용으로 사용되는 어셈블리의 하위를 테이블에서 제거할때
					//해당 어셈블리가 다른 곳에서 사용되면 하위를 제거하지 않음.
					// [20161111][ymjang] 하위가 이미 삭제된 뒤 상위 Assy 를 제거할 경우, 오류 Nullpointer Exception 오류 발생 개선
					if (rows != null) {
						rows.remove(0);
					}
					
					if( rows != null && rows.isEmpty()){
						ArrayList<Integer> childRows = getChildModelRows(childRowKey, keyIndexer, childRowKeyMap, partIdxMap);
						if( !childRows.isEmpty()){
							
							//modelRows에 존재하지 않는 Row Idx만 추가.
							childRows.removeAll(modelRows);
							modelRows.addAll(childRows);
						}
					}
				}
			}
		}
		
		return modelRows;
	}

	class FixedTablePopup extends JPopupMenu {

		MouseEvent mouseEvent;
		JMenuItem menuInsertCopiedRow = null;
		JMenuItem menuPasteRow = null;
		
		public FixedTablePopup(MouseEvent mouseEvent) {

			this.mouseEvent = mouseEvent;
			popupInit();
		}

		@Override
		protected void firePopupMenuWillBecomeVisible() {
			System.out.println("firePopupMenuWillBecomeVisible");
			if( menuInsertCopiedRow != null){
				if (copiedColumns != null
						&& copiedColumns.length == table.getColumnCount()) {
					menuPasteRow.setEnabled(true);
					menuInsertCopiedRow.setEnabled(true);
				}else{
					menuInsertCopiedRow.setEnabled(false);
					menuPasteRow.setEnabled(false);
				}
			}
			super.firePopupMenuWillBecomeVisible();
		}

		private void popupInit() {
			JMenuItem menuInsertEmptyRow = new JMenuItem("Insert Empty Row",
					new ImageIcon(MasterListTablePanel.class
							.getResource("/icons/table-insert-row.png")));
			menuInsertEmptyRow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					try {
						insertEmptyRow(mouseEvent, 1);
					} catch (Exception e) {
						// Auto-generated catch block
						e.printStackTrace();
						MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
					}
				}
			});
			add(menuInsertEmptyRow);

			menuInsertCopiedRow = new JMenuItem("Insert Copied Row");
			menuInsertCopiedRow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					try {
						insertCopiedRow(mouseEvent);
					} catch (Exception e) {
						// Auto-generated catch block
						e.printStackTrace();
						MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
					}
				}
			});
			add(menuInsertCopiedRow);
			
			JMenuItem menuInsertDerivationpart = new JMenuItem("Insert Derivation Part");
			menuInsertDerivationpart.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					int rowIdx = fixedTable.rowAtPoint(mouseEvent.getPoint());
					int modelRowIdx = table.convertRowIndexToModel(rowIdx);
					
					Vector<CellValue> newRow = new Vector();
					newRow.add(new CellValue("", "", 0));

					CellValue cellValue = null;
					for (int column = 0; column < model.getColumnCount(); column++) {
						Object obj = model.getValueAt( modelRowIdx, column);
						
						if( notEditableColumn.contains(column)){
							cellValue = new CellValue("");
							newRow.add(cellValue);
							if( column == MASTER_LIST_PART_ID_IDX){
								HashMap cellData = new HashMap();
								try {
									cellData.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, BomUtil.getNewSystemRowKey());
								} catch (Exception e) {
									// Auto-generated catch block
									e.printStackTrace();
									MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
									return;
								}
								
								CellValue partIdCellValue = (CellValue)obj;
								HashMap map = partIdCellValue.getData();
								cellData.put(PropertyConstant.ATTR_NAME_UOMTAG, map.get(PropertyConstant.ATTR_NAME_UOMTAG));
								cellValue.setData(cellData);
							}
							
							continue;
						}
						
						if( column == MASTER_LIST_PART_ID_IDX || column == MASTER_LIST_SPEC_IDX || column == MASTER_LIST_SPEC_DESC_IDX ||
								column == MASTER_LIST_SEQUENCE_IDX ){
							cellValue = new CellValue("");
						}else if(column == MASTER_LIST_NMCD_IDX){
							cellValue = new CellValue("N");
						}else{
							cellValue = new CellValue(obj.toString(), obj.toString(), 0);
						}
						newRow.add(cellValue);
					}
					
					data.insertElementAt((Vector) newRow.clone(),
							sorter.convertRowIndexToModel(rowIdx));
					model.insertRow(sorter.convertRowIndexToModel(rowIdx),
							(Vector) newRow);
					
					refreshRowNum();
				}
			});
			add(menuInsertDerivationpart);

			JMenuItem menuRemoveRow = new JMenuItem("Remove Row",
					new ImageIcon(MasterListTablePanel.class
							.getResource("/icons/table-delete-row.png")));
			menuRemoveRow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					int rowIdx = fixedTable.rowAtPoint(mouseEvent.getPoint());
					int[] selectedRows = fixedTable.getSelectedRows();
					if( selectedRows == null || selectedRows.length == 0){
						selectedRows = new int[]{rowIdx};
					}
					int[] selectedModelRows = new int[selectedRows.length];
					for (int i = selectedRows.length - 1; i >= 0; i--) {
						selectedModelRows[i] = table.convertRowIndexToModel(selectedRows[i]);
					}
					Arrays.sort(selectedModelRows);

					//Parent별로 하위 Child를 가진 객체가 필요.(Working BOM)
					//하위가 존재 할 경우, 메시지 표시(Yes or No)
					//Yes 선택시 해당하는 Rows를 모델에서 제거.
					//No 선택시 ValidationDlg를 통해 해당 셀을 Disp..
					ArrayList<Integer> idxesToRemove = getRemoveTarget(selectedModelRows);
					
					if( selectedModelRows.length != idxesToRemove.size()){
						int result = JOptionPane.showConfirmDialog(
								(Window)parentDlg,
							    "Including the Assembly. Do you want to continue?",
							    "Check Remove",
							    JOptionPane.YES_NO_OPTION);
						if( result == JOptionPane.YES_OPTION){
							for( int i = idxesToRemove.size() - 1; i >= 0; i--){
								model.removeRow(idxesToRemove.get(i));
								data.remove(idxesToRemove.get(i).intValue());
							}
							
							//선택한 Row 삭제
//							for (int i = selectedModelRows.length - 1; i >= 0; i--) {
//								model.removeRow(selectedModelRows[i]);
//								data.remove(selectedModelRows[i]);
//							}
							
							fixedTable.clearSelection();
							table.clearSelection();
							
							refreshRowNum();
						} else {
							// [20161111][ymjang] Assy Remove 취소시 Validation 불필요 --> 주석 처리함.
							/*
							Vector<Vector> validateResult = new Vector();
							for( int row = 0; row < idxesToRemove.size(); row++){
								Vector rowVec = new Vector();
								rowVec.add(idxesToRemove.get(row));
								rowVec.add(MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX);
								rowVec.add("To delete Target");
								validateResult.add(rowVec);
							}
							
							ValidationDlg validationDlg = new ValidationDlg( (MasterListDlg)parentDlg, validateResult);
							validationDlg.setPreferredSize(new Dimension(100, 100));
							validationDlg.setVisible(true);
							*/
							return;
						}
					}else{
						for( int i = idxesToRemove.size() - 1; i >= 0; i--){
							model.removeRow(idxesToRemove.get(i));
							data.remove(idxesToRemove.get(i).intValue());
						}
						
						fixedTable.clearSelection();
						table.clearSelection();
						
						refreshRowNum();
					}
					
					table.repaint();
				}
			});
			add(menuRemoveRow);

			JMenuItem menuCopyRow = new JMenuItem("Copy Row", new ImageIcon(
					MasterListTablePanel.class.getResource("/icons/Copy.png")));
			menuCopyRow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					copy();
				}
			});
			add(menuCopyRow);

			menuPasteRow = new JMenuItem("Paste Row", new ImageIcon(
					MasterListTablePanel.class.getResource("/icons/Paste.png")));
			menuPasteRow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					int rowIdx = fixedTable.rowAtPoint(mouseEvent.getPoint());
					table.clearSelection();
					table.setRowSelectionInterval(rowIdx, rowIdx);
					table.setColumnSelectionInterval(0, 0);
					paste();
				}
			});
			add(menuPasteRow);
			
			JMenuItem menuAddRow = new JMenuItem("Add Row", new ImageIcon(
					MasterListTablePanel.class.getResource("/icons/add_16.png")));
			menuAddRow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					int rowIdx = fixedTable.rowAtPoint(mouseEvent.getPoint());
					table.clearSelection();
//					table.setRowSelectionInterval(rowIdx, rowIdx);
//					table.setColumnSelectionInterval(0, 0);
					try {
						addRow(mouseEvent);
					} catch (Exception e) {
						// Auto-generated catch block
						e.printStackTrace();
						MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
					}
				}
			});
			add(menuAddRow);			
		}
	}
	
	private ArrayList<Integer> getRemoveTarget(int[] selectedModelRows){
		HashMap<String, Integer> keyIndexer = new HashMap();
		HashMap<String, ArrayList<Integer>> partIdxMap = new HashMap(){

			@Override
			public Object clone() {
				HashMap resultMap = new HashMap();
				Iterator<String> its = keySet().iterator();
				while(its.hasNext()){
					String partId = its.next();
					ArrayList<Integer> rows = (ArrayList<Integer>)get(partId);
					resultMap.put(partId, rows.clone());
				}
				return resultMap;
			}
			
		};
		for( int i = 0; i < model.getRowCount(); i++){
			String key = getKeyInModel(i);
			keyIndexer.put(key, i);
			
			String partId = getPartId(key);
			ArrayList<Integer> rows = null;
			if( partId != null){
				rows = partIdxMap.get(partId);
				if( rows == null){
					rows = new ArrayList();
					partIdxMap.put(partId, rows);
				}
				
				if( !rows.contains(i)){
					rows.add(i);
				}
			}
			
		}
		HashMap<String, ArrayList<String>> childRowKeyMap = parentDlg.getWorkingChildRowKeys();
		ArrayList<Integer> idxesToRemove = new ArrayList();
		for (int i = selectedModelRows.length - 1; i >= 0; i--) {
			
			int modelRow = selectedModelRows[i];
			if( !idxesToRemove.contains(modelRow)){
				idxesToRemove.add( modelRow);
			}
			
			if( !childRowKeyMap.isEmpty() ){
				String key = getKeyInModel(modelRow);
				
				ArrayList<Integer> childRows = getChildModelRows(key, keyIndexer, childRowKeyMap, (HashMap<String, ArrayList<Integer>>)partIdxMap.clone());
				if( !childRows.isEmpty()){
					
					childRows.removeAll(idxesToRemove);
					idxesToRemove.addAll(childRows);
					
					for( Integer childRow:childRows){
						
						String parentId = model.getValueAt(childRow, MASTER_LIST_PARENT_ID_IDX).toString();
						ArrayList<Integer> rowIdxes = partIdxMap.get(parentId);
						if( !idxesToRemove.containsAll(rowIdxes)){
							idxesToRemove.remove(childRow);
						}
					}
				}
			}
		}
		
		Collections.sort(idxesToRemove);
		return idxesToRemove;
	}
	
	private String getParentId(int modelRow){
		
		String parentId = model.getValueAt(modelRow, MASTER_LIST_PARENT_ID_IDX).toString();
		if( modelRow < 1){
			return parentId;
		}
		
		int level = -1;
		String levelM = model.getValueAt(modelRow, MASTER_LIST_LEV_MAN_IDX).toString();
		try{
			level = Integer.parseInt(levelM);
		}catch( NumberFormatException nfe){
			return parentId;
		}
		
		if( level <= 1){
			return parentId;
		}
		
		int upperLevel = -1;
		for( int row = modelRow - 1; row >= 0; row--){
			String partId = model.getValueAt(row, MASTER_LIST_PART_ID_IDX).toString();
			String tmpLevelM = model.getValueAt(row, MASTER_LIST_LEV_MAN_IDX).toString();
			if( tmpLevelM.equals("")){
				return "";
			}
			try{
				upperLevel = Integer.parseInt(tmpLevelM);
			}catch( NumberFormatException nfe){
				return "";
			}
			
			if( level == upperLevel + 1){
				return partId;
			}
		}
		
		return "";
	}

	class TablePopup extends JPopupMenu {
		JMenuItem menuPaste = null;
		JMenuItem menuParentInput = null;
		JMenuItem menuGenerateSeq = null;
		JMenuItem menuCarryOver = null;
		
		public TablePopup() {

			JMenuItem menuCopyRow = new JMenuItem("Copy Cell", new ImageIcon(
					MasterListTablePanel.class.getResource("/icons/Copy.png")));
			menuCopyRow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					new CopyAction().actionPerformed(null);
				}
			});
			add(menuCopyRow);

			menuPaste = new JMenuItem("Paste Cell", new ImageIcon(
					MasterListTablePanel.class.getResource("/icons/Paste.png")));
			menuPaste.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					new PasteAction().actionPerformed(null);
				}
			});
			add(menuPaste);
			
//			PARENT Part Auto Input
			menuParentInput = new JMenuItem("P/Part Auto Input", null);
			menuParentInput.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					int[] selectedRows = table.getSelectedRows();
					if( selectedRows == null || selectedRows.length < 1){
						return;
					}
					int[] selectedModelRows = new int[selectedRows.length];
					for( int i = 0; i < selectedRows.length; i++){
						selectedModelRows[i] = table.convertRowIndexToModel( selectedRows[i] );
					}
					Arrays.sort(selectedModelRows);
					
					for( int i = 0; i < selectedModelRows.length; i++){
						String parentId = getParentId(selectedModelRows[i]) ;
						model.setValueAt(new CellValue(parentId), selectedModelRows[i], MASTER_LIST_PARENT_ID_IDX);
					}
					table.repaint();
				}
			});
			add(menuParentInput);
			
			menuGenerateSeq = new JMenuItem("Generate Seq.", null);
			menuGenerateSeq.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					int selectedColumn = table.getSelectedColumn();
					int[] selectedRows = table.getSelectedRows();
					Arrays.sort(selectedRows);
					int[] selectedModelRows = new int[selectedRows.length];
					for( int i = 0; i < selectedRows.length; i++){
						selectedModelRows[i] = table.convertRowIndexToModel( selectedRows[i] );
					}
					
					HashMap<String, Integer> maxSequenceMap = new HashMap();
					for( int modelRow = 0; modelRow < model.getRowCount(); modelRow++){
						String parentId = model.getValueAt(modelRow, MASTER_LIST_PARENT_ID_IDX).toString().trim();
						if( parentId == null || parentId.equals("")){
							try {
								parentId = parentDlg.getFmpRevision().getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
							} catch (TCException e) {
								// Auto-generated catch block
								e.printStackTrace();
								MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
								return;
							}
						}
						
						int sequence = 0;
						String sequenceStr = model.getValueAt(modelRow, MASTER_LIST_SEQUENCE_IDX).toString().trim();
						try{
							sequence = Integer.parseInt(sequenceStr);
						}catch(NumberFormatException nfe){
							continue;
						}
						Integer maxSeq = maxSequenceMap.get(parentId);
						if( maxSeq == null){
							maxSequenceMap.put(parentId, sequence);
						}else{
							if( !sequenceStr.equals("")){
								if( sequence > maxSeq){
									maxSequenceMap.put(parentId, sequence);
								}
							}
						}
					}
					
					for( int i = 0; selectedModelRows != null && i < selectedModelRows.length; i++){
						String parentId = model.getValueAt(selectedModelRows[i], MASTER_LIST_PARENT_ID_IDX).toString();
						if( parentId == null || parentId.trim().equals("")){
							try {
								parentId = parentDlg.getFmpRevision().getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
							} catch (TCException e) {
								// Auto-generated catch block
								e.printStackTrace();
								MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
								return;
							} 
						}
						Integer maxSeq = maxSequenceMap.get(parentId);
						if( maxSeq == null){
							maxSeq = 0;
						}
						int t = maxSeq/10;
						t++;
						t = t * 10;
						maxSequenceMap.put(parentId, t);
						model.setValueAt(String.format("%06d", t), selectedModelRows[i], MASTER_LIST_SEQUENCE_IDX);
					}
				}
			});
			add(menuGenerateSeq);
			
			menuCarryOver = new JMenuItem("Carry Over", null);
			menuCarryOver.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					int[] selectedRows = table.getSelectedRows();
					if( selectedRows == null || selectedRows.length < 1){
						return;
					}
					int[] selectedModelRows = new int[selectedRows.length];
					for( int i = 0; i < selectedRows.length; i++){
						selectedModelRows[i] = table.convertRowIndexToModel( selectedRows[i] );
					}
					Arrays.sort(selectedModelRows);
					
					try{
						for( int i = 0; i < selectedModelRows.length; i++){
							Object nmcdObj = model.getValueAt(selectedRows[i], MASTER_LIST_NMCD_IDX);
//							if( nmcdObj.toString().equalsIgnoreCase("C")){
//								continue;
//							}
							Object partIdObj = model.getValueAt(selectedRows[i], MASTER_LIST_PART_ID_IDX);
							String str = model.getValueAt(selectedRows[i], MASTER_LIST_PART_DISPLAY_ID_IDX).toString();
							str = str.trim().replaceAll(" ", "");
							// [20160602][ymjang] Part No 에 "-" 추가시 제거 후, Part Search 하도록 개선함.
							str = str.trim().replaceAll("-", "");
							HashMap<String, String> resultMap = WebUtil.getPart(str);
							if( resultMap == null){
								continue;
							}
							
							String key = getKeyInModel(selectedRows[i]);
							ArrayList<TCComponentBOMLine> lists = parentDlg.getBOMLines(key);
							
							TCSession session = CustomUtil.getTCSession();
							TCComponentItem item = (TCComponentItem)session.stringToComponent(resultMap.get("PUID"));
							TCComponentItemRevision revision = item.getLatestItemRevision();
							String systemCode = "";
							
							/**
							 * 20180511 bug [beenlaho]
							 * excel 에서 import한 경우 BOMLINE이 존재하지 않는다. list.size() 값이 0이다. 
							 */
							if( lists.size() == 0 )
								systemCode = model.getValueAt(selectedRows[i], MASTER_LIST_SYSTEM_IDX).toString();
							else	
								systemCode = lists.get(0).getProperty(PropertyConstant.ATTR_NAME_BL_BUDGETCODE);
							
							String itemName = item.getProperty("object_name");
							String itemId = item.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
							String dispPartNo = revision.getProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);
							String projectCode = revision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
							
							if( partIdObj instanceof CellValue){
								CellValue partIdCellValue = (CellValue)partIdObj;
								
								//Part ID(Unique NO)가 없는 경우에만 입력됨.
								if( partIdCellValue.getValue().trim().equals("")){
									partIdCellValue.setValue(itemId);
									// [20160602][ymjang] Carry Over Part No 입력시 Display No 자동 입력
									model.setValueAt(dispPartNo, selectedModelRows[i], MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX);
								}
							}else{
								MessageBox.post((Window)parentDlg, "Invalid Part ID Cell Type", "ERROR", MessageBox.ERROR);
								return;
							}
							
							model.setValueAt(systemCode, selectedModelRows[i], MasterListTablePanel.MASTER_LIST_SYSTEM_IDX);
							model.setValueAt(itemName, selectedModelRows[i], MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
							model.setValueAt(new CellValue("C"), selectedModelRows[i], MasterListTablePanel.MASTER_LIST_NMCD_IDX);
							model.setValueAt(projectCode, selectedModelRows[i], MasterListTablePanel.MASTER_LIST_PROJECT_IDX);							
						}
					}catch(Exception e){
						MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
						return;
					}finally{
						table.repaint();
					}
				}
			});
			add(menuCarryOver);
		}
		
		@Override
		protected void firePopupMenuWillBecomeVisible() {
			if( menuPaste != null){
				if (copiedColumns != null) {
					menuPaste.setEnabled(true);
				}else{
					menuPaste.setEnabled(false);
				}
			}
			
			if( menuGenerateSeq != null){
				int[] selectedColumns = table.getSelectedColumns();
				Arrays.sort(selectedColumns);
				int[] selectedRows = table.getSelectedRows();
				Arrays.sort(selectedRows);
				
				if( selectedColumns != null && selectedColumns.length == 1
					&& selectedColumns[0] == MasterListTablePanel.MASTER_LIST_SEQUENCE_IDX){
					menuGenerateSeq.setEnabled(true);
				}else{
					menuGenerateSeq.setEnabled(false);
				}
			}
			
			if( menuParentInput != null){
				int[] selectedColumns = table.getSelectedColumns();
				Arrays.sort(selectedColumns);
				int[] selectedRows = table.getSelectedRows();
				Arrays.sort(selectedRows);
				
				if( selectedColumns != null && selectedColumns.length == 1
					&& selectedColumns[0] == MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX){
					menuParentInput.setEnabled(true);
				}else{
					menuParentInput.setEnabled(false);
				}
			}
			
			if( menuCarryOver != null){
				int[] selectedColumns = table.getSelectedColumns();
				Arrays.sort(selectedColumns);
				int[] selectedRows = table.getSelectedRows();
				Arrays.sort(selectedRows);
				
				if( selectedColumns != null && selectedColumns.length == 1
					&& selectedColumns[0] == MasterListTablePanel.MASTER_LIST_PART_DISPLAY_ID_IDX){
					menuCarryOver.setEnabled(true);
				}else{
					menuCarryOver.setEnabled(false);
				}
			}
			super.firePopupMenuWillBecomeVisible();
		}
	}
	
	public void clearAllFilter(){
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			FilterRenderer renderer = (FilterRenderer)table.getColumnModel().getColumn(i).getHeaderRenderer();
			renderer.tf.setText("");
		}
		filterMap.clear();
		
		if( isShowDeletedLine){
			sorter.setRowFilter(null);
		}else{
			DeletedLineFilter delLineFilter = new DeletedLineFilter();
			sorter.setRowFilter(delLineFilter);
		}
		refreshRowNum();
		header.repaint();
	}

	class HeaderPopup extends JPopupMenu {
		HeaderPopupFilter filter = null;
		private int columnIdx = -1;

		public HeaderPopup() {
			
			JMenuItem menuRemoveSort = new JMenuItem("Remove Sort");
			menuRemoveSort.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					ArrayList list = new ArrayList();
//					list.add(new RowSorter.SortKey(columnIdx,
//							SortOrder.ASCENDING));
					sorter.setSortKeys(list);
					clearSortValue();
					refreshRowNum();
				}
			});
			add(menuRemoveSort);
			
			JMenuItem menuAsc = new JMenuItem("ASC", new ImageIcon(
					MasterListTablePanel.class
							.getResource("/icons/sort-ascend.png")));
			menuAsc.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					ArrayList list = new ArrayList();
					list.add(new RowSorter.SortKey(columnIdx,
							SortOrder.ASCENDING));
					sorter.setSortKeys(list);
//					sorter.sort();
					clearSortValue();
					refreshRowNum();
				}
			});
			add(menuAsc);
			JMenuItem menuDesc = new JMenuItem("Desc", new ImageIcon(
					MasterListTablePanel.class
							.getResource("/icons/sort-descend.png")));
			menuDesc.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					ArrayList list = new ArrayList();
					list.add(new RowSorter.SortKey(columnIdx,
							SortOrder.DESCENDING));
					sorter.setSortKeys(list);
//					sorter.sort();
					clearSortValue();
					refreshRowNum();
				}
			});
			add(menuDesc);
			addSeparator();

			JMenuItem menuCancelFilter = new JMenuItem("Cancel Filter",
					new ImageIcon(
							MasterListTablePanel.class
									.getResource("/icons/filter_delete.png")));
			menuCancelFilter.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					Integer[] filteredColumns = filterMap.keySet().toArray(new Integer[filterMap.size()]);
					int col = headerPopup.getColumnIdx();
					filterMap.remove(col);
					
					Integer[] filteredColumn = filterMap.keySet().toArray(new Integer[filterMap.size()]);
					List<RowFilter<Object,Object>> filters = new ArrayList<RowFilter<Object,Object>>();
					for( int i = 0; filteredColumn != null &&  i < filteredColumn.length; i++){
						
						ArrayList<RowFilter<Object,Object>> orList = new ArrayList();
						ArrayList<String> filterList = filterMap.get(filteredColumn[i]);
						for( int j = 0; j < filterList.size(); j++){
							RowFilter filter = new CellValueFilter(filterList.get(j), filteredColumn[i]);
							orList.add(filter);
						}
						RowFilter rowFilter = RowFilter.orFilter(orList);
						filters.add(rowFilter);
					}
					
					DeletedLineFilter delLineFilter = new DeletedLineFilter();
					if( !isShowDeletedLine){
						filters.add(delLineFilter);
					}
					RowFilter rowFilter = RowFilter.andFilter(filters);
					if( filters.isEmpty()){
						sorter.setRowFilter(delLineFilter);
					}else{
						
						sorter.setRowFilter(rowFilter);
					}
					
					FilterRenderer renderer = (FilterRenderer) header.getColumnModel()
							.getColumn(col).getHeaderRenderer();
					JTextField tf = renderer.getFilterTf();
					tf.setText("");
					refreshRowNum();
					header.repaint();
				}
			});
			add(menuCancelFilter);
			
			JMenuItem menuCancelAllFilter = new JMenuItem("Cancel All Filter",
					new ImageIcon(
							MasterListTablePanel.class
									.getResource("/icons/delete_filter.gif")));
			menuCancelAllFilter.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionevent) {
					// Auto-generated method stub
					clearAllFilter();
				}
			});
			add(menuCancelAllFilter);

			filter = new HeaderPopupFilter(this, MasterListTablePanel.this);
			filter.getTfFilter().addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent keyevent) {
					if (keyevent.getKeyChar() == '\n') {
						if (selectedColumn != -1) {
							execFilter(true);
							setVisible(false);
						}
					}else{
//						filter.getCheckedNode();
					}
					super.keyTyped(keyevent);
				}
			});
			add(filter);
		}

		public HeaderPopupFilter getFilter() {
			return filter;
		}
		
		public HashMap<Integer, ArrayList<String>> getFilterMap() {
			return filterMap;
		}

		public int getColumnIdx() {
			return columnIdx;
		}

		public void setColumnIdx(int columnIdx) {
			this.columnIdx = columnIdx;
		}
		
		public void execFilter(boolean isColumnFilterShowing){
			
			clearSortValue();
			
//			if( true) return;
			
			ArrayList<CheckBoxNode> nodeList = filter.getCheckedNode();
			if( nodeList.isEmpty()){
				filterMap.remove(columnIdx);
			}else{
				filterMap.remove(columnIdx);
				for( CheckBoxNode node : nodeList){
					String filterStr = node.getText();
					ArrayList<String> filterList = filterMap.get(columnIdx);
					if( filterList == null){
						if( columnIdx > -1){
							filterList = new ArrayList();
							filterList.add(filterStr);
							filterMap.put(columnIdx, filterList);
						}
					}else{
						if( !filterList.contains(filterStr)){
							filterList.add(filterStr);
						}
					}
				}
				
			}
			Integer[] filteredColumn = filterMap.keySet().toArray(new Integer[filterMap.size()]);
			List<RowFilter<Object,Object>> filters = new ArrayList<RowFilter<Object,Object>>();
			for( int i = 0; filteredColumn != null &&  i < filteredColumn.length; i++){
//				RowFilter filter = RowFilter.regexFilter(filterMap.get(filteredColumn[i]), filteredColumn[i]);
				List<RowFilter<Object,Object>> filterList = new ArrayList<RowFilter<Object,Object>>();
				ArrayList<String> orList = filterMap.get(filteredColumn[i]);
				for( int j = 0; j < orList.size(); j++){
					RowFilter filter = new CellValueFilter(orList.get(j), filteredColumn[i]);
					filterList.add(filter);
				}
				RowFilter orFilter = RowFilter.orFilter(filterList);
				filters.add(orFilter);
			}
			
			DeletedLineFilter delLineFilter = new DeletedLineFilter();
			if( !isShowDeletedLine){
				filters.add(delLineFilter);
			}
			RowFilter rowFilter = RowFilter.andFilter(filters);
			if( filters.isEmpty()){
				if( isShowDeletedLine){
					sorter.setRowFilter(null);
				}else{
					sorter.setRowFilter(delLineFilter);
				}
			}else{
				sorter.setRowFilter(rowFilter);
			}
			
			// 컬럼에 필터 Text 표시.
			if( selectedColumn > -1 && isColumnFilterShowing){
				int viewIdx = table.convertColumnIndexToView(selectedColumn);
				FilterRenderer renderer = (FilterRenderer) header.getColumnModel()
						.getColumn(viewIdx).getHeaderRenderer();
				JTextField tf = renderer.getFilterTf();
				
				if( nodeList.isEmpty()){
					tf.setText("");
				}else{
					if( nodeList.size() == 1){
						tf.setText(nodeList.get(0).getText());
					}else{
						tf.setText("**");
					}
				}
			}
			refreshRowNum();
			header.repaint();
		}

	}
	
	private class DeletedLineFilter extends RowFilter{

		@Override
		public boolean include(Entry entry) {
			
			Object obj = entry.getValue(MASTER_LIST_SPEC_IDX);
			if( obj instanceof CellValue){
				CellValue cellValue = (CellValue)obj;
				
				return !cellValue.getSortValue().equals("NONE");
			}else{
				if( obj == null){
					return false;
				}
					
				return !obj.toString().equals("NONE");
			}
		}
		
	}
	
	private class CellValueFilter extends RowFilter{
		
		String txt;
		int column;
		public CellValueFilter(String txt, int column){
			this.txt = txt;
			this.column = column;
		}

		@Override
		public boolean include(Entry entry) {
			// Auto-generated method stub
			Object obj = entry.getValue(column);
			if( obj instanceof CellValue){
				CellValue cellValue = (CellValue)obj;
				return cellValue.getSortValue().equals(txt);
			}else{
				return obj.toString().equals(txt);
			}
		}
		
	}
	
}
