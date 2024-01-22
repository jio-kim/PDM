/**
 * 
 */
package com.symc.plm.me.sdv.view.meco;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.progressbar.WaitProgressor;
import com.ssangyong.rac.kernel.SYMCBOPEditData;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.viewer.AbstractSDVViewer;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.ProcessUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
//import com.ssangyong.common.remote.DataSet;
//import com.ssangyong.common.remote.SYMCRemoteUtil;
//import com.teamcenter.rac.kernel.TCComponentBOMWindow;
/**
 * Class Name : EPL
 * Class Description : 
 * @date 2013. 10. 10.
 *
 */
@SuppressWarnings("unused")
public class MECOEPLSWTRenderingView extends AbstractSDVViewer {
	protected Table table;
//	protected ArrayList<EPLTableItem> modifiedTableData;
	public ArrayList<SYMCBOPEditData> arrSYMCBOPEditData = null;
	protected Color evenColor, modifiedColor, modifiableColor, modifiableEvenColor;
	private Composite composite;
	private Button btnReload;
	private TCComponentChangeItemRevision mecoRevision;
    private TCComponent[] solutionList; //, problemList;
	private SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private TCSession session;
	private CustomUtil customUtil = null;
	private DataSet ds = null;
	private boolean isLoad;
	private Shell shell;
	
	

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MECOEPLSWTRenderingView(Composite parent) {
		super(parent);
		shell = getShell();
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
		session = CustomUtil.getTCSession();
		if (comps.length > 0){
			TCComponent comp = (TCComponent) comps[0];
			if (comp instanceof TCComponentChangeItemRevision) {
				targetComp = comp;
				mecoRevision = (TCComponentChangeItemRevision)targetComp;
				updateUI();
			}
		}

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void load() {
		if(!isLoad)
		{
			isLoad = true;
		}else
		{
			return;
		}
		ArrayList<HashMap<String,Object>> changedMeplList = null;
//				checkModifiedMEPL();
		
//		if(eplIds.size() > 0) {
		if(false) {
			
			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			box.setText("Reload EPL");
			box.setMessage("Exist modified EPL. Do you want to reload MEPL?");
		
			int choice = box.open();
			if(choice == SWT.NO) {
				executeLoadProcess(false);
			}else{
				
				executeLoadProcess(true);
			}
		}else{
			executeLoadProcess(false);
		}
	}
	
	private ArrayList<SYMCBOPEditData> selectMECOEplList(String mecoNo) {
		CustomMECODao dao = null;
		ArrayList<SYMCBOPEditData> arrlist = null;
		try {
			dao = new CustomMECODao();
			arrlist = (ArrayList<SYMCBOPEditData>)dao.selectMECOEplList(mecoNo);
//					this.arrSYMCBOPEditData =arrlist;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arrlist;
	}

	/**
	 * DB에서 테이블 데이터를 가져와 랜더링한다.
	 * 
	 * @method setTableData
	 * @date 2013. 11. 28.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
//	@SuppressWarnings("unchecked")
	public void setTableData() throws Exception {
		// Modify List Clear
//		if (modifiedTableData == null) {
//			modifiedTableData = new ArrayList<EPLTableItem>();
//		} else {
//			modifiedTableData.clear();
//		}
//
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		ds = new DataSet();
		ds.put("mecoNo", AIFUtility.getCurrentApplication().getTargetComponent().getProperty("item_id"));
		this.arrSYMCBOPEditData = (ArrayList<SYMCBOPEditData>)selectMECOEplList(mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
		final ArrayList<SYMCBOPEditData> rows = this.arrSYMCBOPEditData;
		getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					table.removeAll(); // 테이블 데이터 삭제
					if (rows == null || rows.size() == 0) {
						return;
					}
					ArrayList<String> addedEPLs = new ArrayList<String>();
					for (int i = 0; i < arrSYMCBOPEditData.size(); i++) {
						String eplId = rows.get(i).getEplId();
//						String eplId = Integer.toString(i);
						if(!addedEPLs.contains(eplId)) {
							new EPLTableItem(arrSYMCBOPEditData.get(i), true);
							new EPLTableItem(arrSYMCBOPEditData.get(i), false);
							addedEPLs.add(eplId);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	@Override
	public void save() {
		// nothing
	}

	@Override
	public boolean isSavable() {
		return false;
	}

	@Override
	public void createPanel(Composite parent) {

		Display display = parent.getDisplay();
		evenColor = new Color(display, 192, 214, 248);
		modifiedColor = new Color(display, 255, 225, 225);
		modifiableColor = new Color(display, 218, 237, 190);
		modifiableEvenColor = new Color(display, 255, 255, 132);

		// Button btn = new Button(parent, SWT.PUSH);
		composite = new Composite(parent, SWT.None);
		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginWidth = 1;
		mainLayout.marginHeight = 1;
		composite.setLayout(mainLayout);
		initTable(composite);


	}

	protected void updateUI() {
		
		boolean hasBypass = mecoRevision.getSession().hasBypass();
		// [NON-SR][20160817] taeku.jeong Bypass가 켜져 있으면 Reload 버튼이 나오도록 수정
		try {
			if(ProcessUtil.isWorkingStatus(mecoRevision)==false && hasBypass==false) {
				// Working 중이 아니면 EPL Reload 버튼을 비활성화 한다.
				btnReload.setEnabled(false);
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		}
	}
	
	protected void initTable(Composite parent) {
		
		btnReload = new Button(composite, SWT.NONE);
		btnReload.setText("Reload");
		btnReload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				executeLoadProcess(true);
			
			}
		});

		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		//        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn.setWidth(30);
		tblclmnNewColumn.setText("No");

		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText("P_TYPE");

		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_2.setWidth(150);
		tblclmnNewColumn_2.setText("Parent Part No.");

		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_3.setWidth(50);
		tblclmnNewColumn_3.setText("Rev");

		TableColumn tblclmnParentPartName = new TableColumn(table, SWT.CENTER);
		tblclmnParentPartName.setWidth(150);
		tblclmnParentPartName.setText("Parent Part Name");

		TableColumn tblclmnSeq = new TableColumn(table, SWT.CENTER);
		tblclmnSeq.setWidth(50);
		tblclmnSeq.setText("SEQ");

		TableColumn tblclmnCtype = new TableColumn(table, SWT.CENTER);
		tblclmnCtype.setWidth(100);
		tblclmnCtype.setText("C_TYPE");

		TableColumn tblclmnChildPartNo = new TableColumn(table, SWT.CENTER);
		tblclmnChildPartNo.setWidth(150);
		tblclmnChildPartNo.setText("Child Part No");

		TableColumn tblclmnCver = new TableColumn(table, SWT.CENTER);
		tblclmnCver.setWidth(50);
		tblclmnCver.setText("Rev");

		TableColumn tblclmnChildPartName = new TableColumn(table, SWT.CENTER);
		tblclmnChildPartName.setWidth(150);
		tblclmnChildPartName.setText("Child Part Name");

		TableColumn tblclmnQty = new TableColumn(table, SWT.CENTER);
		tblclmnQty.setWidth(100);
		tblclmnQty.setText("QTY");

		TableColumn tblclmnShownon = new TableColumn(table, SWT.CENTER);
		tblclmnShownon.setWidth(100);
		tblclmnShownon.setText("Shown_On");

		TableColumn tblclmnEcoNo = new TableColumn(table, SWT.CENTER);
		tblclmnEcoNo.setWidth(100);
		tblclmnEcoNo.setText("ECO No");

		TableColumn tblclmnOption = new TableColumn(table, SWT.CENTER);
		tblclmnOption.setWidth(500);
		tblclmnOption.setText("Option");
	}

	private ArrayList<HashMap<String,String>> checkModifiedMEPL() {

		CustomMECODao dao = null;
		ArrayList<HashMap<String,String>> changedEplList = null;
		ds = new DataSet();
		try {
			dao = new CustomMECODao();
			ds.put("mecoNo", mecoRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
			changedEplList = dao.checkModifiedMEPL(ds);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return changedEplList;
	}

	private void executeLoadProcess(final boolean isReload)
	{
		new Job("EPL Load...")
		{
			@Override
			protected IStatus run(IProgressMonitor arg0)
			{
				System.out.println("EPL 로딩 시작...");
				WaitProgressor waitProgressor = new WaitProgressor(shell);
				try
				{
					System.out.println("status 창 띄우기...");
					waitProgressor.start();
					System.out.println("로딩 메시지...");
					waitProgressor.setMessage("MECO EPL 로딩중...");
					if (isReload)
					{
						waitProgressor.setMessage("MEPL 생성중. 잠시만 기다려주세요.");
						System.out.println("EPL 생성중...");
						buildMEPL();
					}
					waitProgressor.setMessage("MEPL 데이터 테이블 로딩중...");
					System.out.println("테이블에 데이터 로딩중...");
					setTableData();
					System.out.println("완료...");
					waitProgressor.end();
				} catch (final Exception e)
				{
					e.printStackTrace();
					waitProgressor.end();
					getDisplay().syncExec(new Runnable()
					{
						public void run()
						{
							MessageBox.post(getShell(), e.getMessage(), "Notification", 2);
						}
					});
					return Status.CANCEL_STATUS;
				} finally
				{
					isLoad = false;
					waitProgressor.end();
				}
				return Status.OK_STATUS;
			}

		}.schedule();
	}

	protected class EPLTableItem extends TableItem {

		protected SYMCBOPEditData bopEditData;
		private boolean isOld;
		private boolean hasPart;

		public EPLTableItem(SYMCBOPEditData bopEditData, boolean isOld) {
			super(table, SWT.None);
			this.bopEditData = bopEditData;
			this.isOld = isOld;
			if(isOld) {
				setOldData();
			} else {
				setNewData();
			}
			setRowProperty();
		}

		//        private SYMCbopEditData getbopEditData() {
			//            return bopEditData;
			//        }

		protected boolean isOld() {
			return isOld;
		}

		protected boolean hasPart() {
			return hasPart;
		}

		protected void setOldData() {
			if("".equals(BundleUtil.nullToString(bopEditData.getOld_child_no()))) {
				return; 
			}
			hasPart = true;

			String[] rowOldItemData = new String[14];
			// No.
			rowOldItemData[0] = getRowNo() + "";
	
			//------------------------------------------------------------------------
			//Parent Type
			rowOldItemData[1] = bopEditData.getParentType();
			
			//Parent PartNo
			rowOldItemData[2] = bopEditData.getParentNo();
			
			//Parent Ver
			rowOldItemData[3] = bopEditData.getParentRev();
			
			//Paernt Part Name
			rowOldItemData[4] = bopEditData.getParentName();
			
			//Seq
			rowOldItemData[5] = bopEditData.getSeq();
			
			//Child Type
			rowOldItemData[6] = bopEditData.getOld_child_type();
			
			//Child Part No
			rowOldItemData[7] = bopEditData.getOld_child_no();
			
			//Child Ver
			rowOldItemData[8] = bopEditData.getOld_child_rev();
			
			//Child Part Name
			rowOldItemData[9] = bopEditData.getOld_child_name();
			
			//QTY
			rowOldItemData[10] = bopEditData.getOld_qty();
			
			//Shown On
			rowOldItemData[11] = bopEditData.getOld_shown_no_no();
			
			//ECO No
//			rowOldItemData[12] = bopEditData.getEcoNo();
			
			//Option
			rowOldItemData[13] = bopEditData.getOld_vc() !=null ? bopEditData.getOld_vc().toString() : "";
			
			
			
			//------------------------------------------------------------------------
/**			
			// Proj.
			//            rowOldItemData[1] = bopEditData.getProject();
			// SEQ
			rowOldItemData[1] = bopEditData.getSeqOld();
			// C/T
			rowOldItemData[2] = bopEditData.getChangeType().equals("D") ? bopEditData.getChangeType() : "";
			// Parent No, Parent Rev
			rowOldItemData[3] = bopEditData.getParentNo();
			rowOldItemData[4] = bopEditData.getParentRev();
			// Part Origin
			//            rowOldItemData[6] = bopEditData.getPartOriginOld();
			// Part No
			rowOldItemData[5] = bopEditData.getPartNoOld();
			// Part Rev
			rowOldItemData[6] = bopEditData.getPartRevOld();
			// Part Name
			rowOldItemData[7] = bopEditData.getPartNameOld();
			// IC
			//            rowOldItemData[10] = bopEditData.getIcOld();
			// Supply Mode
			//            rowOldItemData[11] = bopEditData.getSupplyModeOld();
			// QTY
			rowOldItemData[8] = bopEditData.getQtyOld();
			// ALT
			//            rowOldItemData[13] = bopEditData.getAltOld();
			// SEL
			//            rowOldItemData[14] = bopEditData.getSelOld();
			// CAT
			//            rowOldItemData[15] = bopEditData.getCatOld();
			// Color
			//            rowOldItemData[16] = bopEditData.getColorIdOld();
			// Color Section
			//            rowOldItemData[17] = bopEditData.getColorSectionOld();
			// Module Code
			//            rowOldItemData[18] = bopEditData.getModuleCodeOld();
			// PLT Stk
			//            rowOldItemData[19] = bopEditData.getPltStkOld();
			// A/S Stk
			//            rowOldItemData[20] = bopEditData.getAsStkOld();
			// Cost
			//            rowOldItemData[21] = "";
			// Tool
			//            rowOldItemData[22] = "";
			// Shown-On
			rowOldItemData[9] = bopEditData.getShownOnOld();
			// Options
			rowOldItemData[10] = bopEditData.getVcOld() != null ? bopEditData.getVcOld().toString() : "";                       
			// Change Desc
			rowOldItemData[11] = bopEditData.getChangeType().equals("D") ? bopEditData.getChgDesc() : "";
*/
			setText(rowOldItemData);
		}

		private void setNewData() {
			if("".equals(BundleUtil.nullToString(bopEditData.getNew_child_no()))) {
				return;
			}
			hasPart = true;

			String[] rowNewItemData = new String[14];
			// No.
			rowNewItemData[0] = getRowNo() + "";
			
			//------------------------------------------------------------------------
			//Parent Type
			rowNewItemData[1] = bopEditData.getParentType();
			
			//Parent PartNo
			rowNewItemData[2] = bopEditData.getParentNo();
			
			//Parent Ver
			rowNewItemData[3] = bopEditData.getParentRev();
			
			//Paernt Part Name
			rowNewItemData[4] = bopEditData.getParentName();
			
			//Seq
			rowNewItemData[5] = bopEditData.getSeq();
			
			//Child Type
			rowNewItemData[6] = bopEditData.getNew_child_type();
			
			//Child Part No
			rowNewItemData[7] = bopEditData.getNew_child_no();
			
			//Child Ver
			rowNewItemData[8] = bopEditData.getNew_child_rev();
			
			//Child Part Name
			rowNewItemData[9] = bopEditData.getNew_child_name();
			
			//QTY
			rowNewItemData[10] = bopEditData.getNew_qty();
			
			//Shown On
			rowNewItemData[11] = bopEditData.getNew_shown_no_no();
			
			//ECO No
			rowNewItemData[12] = bopEditData.getEcoNo();
			
			//Option
			rowNewItemData[13] = bopEditData.getNew_vc() !=null ? bopEditData.getNew_vc().toString() : "";
			
			
			
			//------------------------------------------------------------------------
			

			setText(rowNewItemData);
		}

		protected int getRowNo() {
			int row = table.indexOf(this);
			return row / 2 + 1;
		}

		public void setRowProperty() {
			int rowNum = getRowNo();
			if(hasPart) {
				setText(0, rowNum + "");
			}
//			String ct = bopEditData.getChangeType();
			if(rowNum % 2 == 0) {
//				                if(bopEditData.isReplace() && !bopEditData.getPartTypeNew().equals("S7_FunctionMast") && isEditable()) {
//				                    setBackground(IC, modifiableEvenColor);
//				                }
//				                if(hasPart && isEditable()) {
//				                    if(isOld) {
//				                        setBackground(PLT_STK, modifiableEvenColor);
//				                        setBackground(AS_STK, modifiableEvenColor);
//				                        if(ct.equals("D")) {
//				                            setBackground(CHANGE_DESC, modifiableEvenColor);
//				                        }
//				                    } else {
//				                        setBackground(COST, modifiableEvenColor);
//				                        setBackground(TOOL, modifiableEvenColor);
//				                        if(!ct.equals("D")) {
//				                            setBackground(CHANGE_DESC, modifiableEvenColor);
//				                        }
//				                    }
//				                }
				setBackground(evenColor);
			} else {
				//                if(bopEditData.isReplace() && !bopEditData.getPartTypeNew().equals("S7_FunctionMast") && isEditable()) {
				//                    setBackground(IC, modifiableColor);
				//                }
				//                if(hasPart && isEditable()) {
				//                    if(isOld) {
				//                        setBackground(PLT_STK, modifiableColor);
				//                        setBackground(AS_STK, modifiableColor);
				//                        if(ct.equals("D")) {
				//                            setBackground(CHANGE_DESC, modifiableColor);
				//                        }
				//                    } else {
				//                        setBackground(COST, modifiableColor);
				//                        setBackground(TOOL, modifiableColor);
				//                        if(!ct.equals("D")) {
				//                            setBackground(CHANGE_DESC, modifiableColor);
				//                        }
				//                    }
				//                }
				setBackground(table.getBackground());
			}
		}

		protected void checkSubclass() {

		}



	}
	
	/**
	 *  MEPL 생성 (MECO_EPL Table에 Data 생성)
	 * @return
	 * @throws Exception
	 */
	public ArrayList<SYMCBOPEditData> buildMEPL() throws Exception {
		customUtil = new CustomUtil();
		ArrayList<SYMCBOPEditData> arrResultEPL = customUtil.buildMEPL(mecoRevision, true) ;
		this.arrSYMCBOPEditData = arrResultEPL;
		return arrResultEPL;
	}

	private boolean compareString (String oldStr, String newStr) throws Exception{
		
		if("".equals(BundleUtil.nullToString(oldStr)) && "".equals(BundleUtil.nullToString(newStr))) {
			return true;
		}else if(!"".equals(BundleUtil.nullToString(oldStr)) && "".equals(BundleUtil.nullToString(newStr))){
			return false;
		}else if("".equals(BundleUtil.nullToString(oldStr)) && !"".equals(BundleUtil.nullToString(newStr))) {
			return false;
		} else { 
			if(oldStr.equals(newStr)) {
				return true;
			}else{
				return false;
			}
		}
		
		
	}
	public TCComponentChangeItemRevision getMecoRevision() {
		return mecoRevision;
	}

	public void setMecoRevision(TCComponentChangeItemRevision mecoRevision) {
		this.mecoRevision = mecoRevision;
	}
}
