package com.symc.plm.rac.prebom.ccn.view;

import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.kgm.common.utils.PreferenceService;
import com.symc.plm.rac.prebom.ccn.commands.dao.CustomCCNDao;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.symc.plm.rac.prebom.common.viewer.AbstractPreSYMCViewer;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Product Part Information Panel
 * [SR170703-020][LJG]Proto Tooling 컬럼 추가
 * [SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
 */
public class PreCCNEPLInfoPanel extends AbstractPreSYMCViewer {

    protected Color evenColor, modifiedColor, modifiableColor, modifiableEvenColor;
    private Composite composite;
    private Button btnReload;
    //임시
//    private Button btnReloadOld;
    private TCComponentChangeItemRevision ccnRevision;
    public TCSession session;
    
    protected Table table_1;
    protected Table table_2;
    protected Table table_3;
    protected Table table_4;
    protected Table table_5;
    
    private ArrayList<HashMap<String, Object>> ospecList;
    private ArrayList<HashMap<String, Object>> masterList;
    private ArrayList<HashMap<String, Object>> oldMasterUsageList;
    private ArrayList<HashMap<String, Object>> newMasterUsageList;
    private HashMap<String, Integer> usageCP; 
    
    public ArrayList<HashMap<String, Object>> arrSYMCBOMEditData = null;
    
    private Registry registry;
    public static final int CHANGE_DESC = 17;
    
    private int columnSeq;
    private int rowSeq = 1;
    private int totalWidth;
//    private int optionTypeSeq = 0;
//    private String optionType[] = {"STD", "OPT"};
    
    /**
     * Create CCN Menu를 통해 호출됨
     * 
     * @param parent
     * @param paramMap
     *            : PartManage Dialog에서 넘어온 Param Map
     * @param style
     *            : Dialog SWT Style
     * @wbp.parser.constructor
     */
    public PreCCNEPLInfoPanel(Composite parent) {
        super(parent);
    }
    
    
    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void load() {
//    	System.out.println("Load...");
    	//createPanel에서 이미 로드하였음.  중복 로딩으로 시간만 오래걸림.
//        try {
//            ccnRevision = getTargetComp();
//            ospecList = selectOSpecHeaderInfoList(ccnRevision.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO));
//        } catch (TCException e) {
//            e.printStackTrace();
//        }
//        executeLoadProcess(false);
    }
    private ArrayList<HashMap< String, Object>> selectOSpecHeaderInfoList(String ospecNo) {
        CustomCCNDao dao = null;
        ArrayList<HashMap< String, Object>> resultList = new ArrayList<HashMap< String, Object>>();
        try {
            dao = new CustomCCNDao();
            resultList = dao.selectOSpecHeaderInfoList(ospecNo);
//            resultList = dao.selectOSpecHeaderInfoList("OSI-A149-000");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    /**
     * DB에서 테이블 데이터를 가져와 랜더링한다.
     * 
     * @method setTableData
     * @date 2015. 4. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setTableData() throws Exception {
        getDisplay().syncExec(new Runnable() {
            public void run() {
                try {
                    boolean isMultiOption = false;
                    if (null == masterList) {
                        return;
                    }
                    for (int i = 0; i < masterList.size(); i++) {
//                        optionTypeSeq = 0;
                        oldMasterUsageList = new ArrayList<HashMap<String, Object>>();
                        newMasterUsageList = new ArrayList<HashMap<String, Object>>();
//                        if (null != masterList.get(i).get("OLD_LIST_ID")) {
//                            String oldListId = masterList.get(i).get("OLD_LIST_ID").toString();
//                            if (null != oldListId) {
//                            }
//                        }
                        if (null != masterList.get(i).get("LIST_ID")) {
                            String listId = masterList.get(i).get("LIST_ID").toString();
                            if (null != listId) {
                                oldMasterUsageList = selectMasterUsageInfoList(listId, "OLD");
                                newMasterUsageList = selectMasterUsageInfoList(listId, "NEW");
                            }
                        }
//                        int count = Integer.parseInt(masterList.get(i).get("OPTION_TYPE_COUNT").toString());
//                        if (count > 1) {
//                            isMultiOption = true;
//                        }
//                        
                        new EPLTableItem(masterList.get(i), true, isMultiOption, oldMasterUsageList);
                        new EPLTableItem(masterList.get(i), false, isMultiOption, newMasterUsageList);
//                        if (isMultiOption) {
////                            optionTypeSeq = 1;
//                            new EPLTableItem(masterList.get(i), true, isMultiOption, oldMasterUsageList);
//                            new EPLTableItem(masterList.get(i), false, isMultiOption, newMasterUsageList);
//                        }
//                        isMultiOption = false;
                        rowSeq++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    // 테이블을 5개에 Height 를 값에 따라서 바꾸게 한다 
                    if (null != masterList && masterList.size() != 0) {
                        int cellSize = 38;
                        btnReload.setBounds(0, 0, 60, 30);
//                        btnReloadOld.setBounds(100, 0, 80, 30);
                        table_1.setBounds(1, 31, totalWidth + 20, 160 + (cellSize * rowSeq));
                        table_2.setBounds(0, 25, totalWidth + 20, 140 + (cellSize * rowSeq));
                        table_3.setBounds(0, 25, totalWidth + 20, 120 + (cellSize * rowSeq));
                        table_4.setBounds(0, 25, totalWidth + 20, 100 + (cellSize * rowSeq));
                        table_5.setBounds(0, 25, totalWidth + 20, 80 + (cellSize * rowSeq));

                        setSize(totalWidth + 10 + 1, 160 + (cellSize * rowSeq));
                    }
                }
            }
        });
    }
    
    @Override
    public void save() {
    }

    @Override
    public boolean isSavable() {
        return false;
    }
    
    public void createPanel(Composite parent) {
        try {
            ccnRevision = getTargetComp();
            ospecList = selectOSpecHeaderInfoList(ccnRevision.getStringProperty(PropertyConstant.ATTR_NAME_OSPECNO));
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
            rowSeq = 1;
            masterList = selectMasterInfoList(ccnRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));
            setTableData();
        } catch (TCException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            MessageBox.post(getShell(), registry.getString("PreCCNEPLInfoPanel.CCN.ErrorReloading"), registry.getString("PreCCNEPLInfoPanel.CCN.Error"), MessageBox.ERROR);
            e1.printStackTrace();
        }
    }
    protected void updateUI() {
    }

    protected void initTable(Composite parent) throws TCException {
        
        session = SDVPreBOMUtilities.getTCSession(); 
        registry = Registry.getRegistry(this);
//        totalWidth = 4215;
        //[CSH][20180427]CCN EPL에 Usage 정보 안보이는 문제 수정
        totalWidth = 4425;
        Composite composite = new Composite(parent, SWT.NONE);

        btnReload = new Button(composite, SWT.NONE);
        btnReload.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Reload"));
        btnReload.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                	showProgress(true, getShell(), true);
                	//[CSH][20180503]preference에서 unPack 설정시 pack 설정 후 진행
					String pseAutoPackPref = PreferenceService.getValue(TCPreferenceService.TC_preference_all, "PSEAutoPackPref");
					if(pseAutoPackPref != null && pseAutoPackPref.equals("0")){
						PreferenceService.setStringValue("PSEAutoPackPref", "1");
//						MessageBox.post("Please select an option below and try again. \nEdit > Optons > Product Structure : 'Pack Structure Manager display by default'", "Information", MessageBox.INFORMATION);
					}
					table_5.removeAll();
					rowSeq = 1;
                    executeLoadProcess_(true);
//                    rowSeq = 1;
//                    setTableData();
//                    MessageBox.post(getShell(), registry.getString("PreCCNEPLInfoPanel.CCN.ComplateReloading"), registry.getString("PreCCNEPLInfoPanel.CCN.Information"), MessageBox.INFORMATION);
                } catch (Exception e1) {
                    MessageBox.post(getShell(), e1.getMessage(), registry.getString("PreCCNEPLInfoPanel.CCN.Error"), MessageBox.ERROR);
                    e1.printStackTrace();
                }  finally {
                	showProgress(false, getShell(),true);
                }
            }
        });
        //임시 start
//        btnReloadOld = new Button(composite, SWT.NONE);
//        btnReloadOld.setText("Reload Old");
//        btnReloadOld.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                try {
//                	
//                	//[CSH][20180503]preference에서 unPack 설정시 pack 설정 후 진행
//					String pseAutoPackPref = PreferenceService.getValue(TCPreferenceService.TC_preference_all, "PSEAutoPackPref");
//					if(pseAutoPackPref != null && pseAutoPackPref.equals("0")){
//						PreferenceService.setStringValue("PSEAutoPackPref", "1");
////						MessageBox.post("Please select an option below and try again. \nEdit > Optons > Product Structure : 'Pack Structure Manager display by default'", "Information", MessageBox.INFORMATION);
//					}
//					table_5.removeAll();
//					rowSeq = 1;
//                    executeLoadProcess(true, "old");
////                    rowSeq = 1;
////                    setTableData();
////                    MessageBox.post(getShell(), registry.getString("PreCCNEPLInfoPanel.CCN.ComplateReloading"), registry.getString("PreCCNEPLInfoPanel.CCN.Information"), MessageBox.INFORMATION);
//                } catch (Exception e1) {
//                    MessageBox.post(getShell(), registry.getString("PreCCNEPLInfoPanel.CCN.ErrorReloading"), registry.getString("PreCCNEPLInfoPanel.CCN.Error"), MessageBox.ERROR);
//                    e1.printStackTrace();
//                }
//            }
//        });
        //임시 end
        if (SDVPreBOMUtilities.isReleased(ccnRevision)) {
            btnReload.setEnabled(false);
//            btnReloadOld.setEnabled(false);
        }
        
        // 첫번째 테이블을 만든다
        table_1 = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        
        GridData gd_table_1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        table_1.setLayoutData(gd_table_1);
        table_1.setHeaderVisible(true);
        table_1.setLinesVisible(false);
        
        TableColumn tblclmn_1 = new TableColumn(table_1, SWT.NONE | SWT.FULL_SELECTION);
        tblclmn_1.setWidth(totalWidth);
        tblclmn_1.setText("");
        tblclmn_1.setResizable(false);
        table_1 = createDynamicColumn(table_1, "1");
        
        table_2 = new Table(table_1, SWT.NONE);
        table_2.setHeaderVisible(true);
        table_2.setLinesVisible(false);
        
        TableColumn tblclmn_2 = new TableColumn(table_2, SWT.NONE | SWT.FULL_SELECTION);
        tblclmn_2.setWidth(totalWidth);
        tblclmn_2.setText("");
        tblclmn_2.setResizable(false);
        table_2 = createDynamicColumn(table_2, "2");
        
        table_3 = new Table(table_2, SWT.NONE);
        table_3.setHeaderVisible(true);
        table_3.setLinesVisible(false);
        
        TableColumn tblclmn_3 = new TableColumn(table_3, SWT.NONE | SWT.FULL_SELECTION);
        tblclmn_3.setWidth(totalWidth);
        tblclmn_3.setText("");
        tblclmn_3.setResizable(false);
        table_3 = createDynamicColumn(table_3, "3");
        
        table_4 = new Table(table_3, SWT.NONE);
        table_4.setHeaderVisible(true);
        table_4.setLinesVisible(false);
        
        TableColumn tblclmn_4 = new TableColumn(table_4, SWT.NONE | SWT.FULL_SELECTION);
        // tblclmnNewColumn_5-16 까지에 Width 값
        tblclmn_4.setWidth(1415);
        tblclmn_4.setText("");
        tblclmn_4.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_1 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 17,18 Width 값
        tblclmnNewColumn_4_1.setWidth(120);
        tblclmnNewColumn_4_1.setText(registry.getString("PreCCNEPLInfoPanel.CCN.EngConcept"));
        tblclmnNewColumn_4_1.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_2 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 50 Width 값
        tblclmnNewColumn_4_2.setWidth(110);
        tblclmnNewColumn_4_2.setText("");
        tblclmnNewColumn_4_2.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_2_1 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 19 Width 값
        tblclmnNewColumn_4_2_1.setWidth(60);
        tblclmnNewColumn_4_2_1.setText("");
        tblclmnNewColumn_4_2_1.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_3 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 20,21 Width 값
        tblclmnNewColumn_4_3.setWidth(160);
        tblclmnNewColumn_4_3.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Weight"));
        tblclmnNewColumn_4_3.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_4 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 22-25,42 Width 값
        tblclmnNewColumn_4_4.setWidth(360);
        tblclmnNewColumn_4_4.setText("");
        tblclmnNewColumn_4_4.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_5 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 26-27 Width 값
        tblclmnNewColumn_4_5.setWidth(200);
        tblclmnNewColumn_4_5.setText(registry.getString("PreCCNEPLInfoPanel.CCN.MaterialCost"));
        tblclmnNewColumn_4_5.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_6 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 28-30 Width 값
        tblclmnNewColumn_4_6.setWidth(300);
        tblclmnNewColumn_4_6.setText(registry.getString("PreCCNEPLInfoPanel.CCN.PrtTest"));
        tblclmnNewColumn_4_6.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_7 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 31-34 Width 값
        tblclmnNewColumn_4_7.setWidth(400);
        tblclmnNewColumn_4_7.setText(registry.getString("PreCCNEPLInfoPanel.CCN.ConceptDwg"));
        tblclmnNewColumn_4_7.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_8 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 35-37 Width 값
        tblclmnNewColumn_4_8.setWidth(300);
        tblclmnNewColumn_4_8.setText(registry.getString("PreCCNEPLInfoPanel.CCN.PrdDwg"));
        tblclmnNewColumn_4_8.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_9 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 51,38-39 Width 값
        tblclmnNewColumn_4_9.setWidth(300);
        tblclmnNewColumn_4_9.setText(registry.getString("PreCCNEPLInfoPanel.CCN.DesignConceptDoc"));
        tblclmnNewColumn_4_9.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_10 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 40-41 Width 값
        tblclmnNewColumn_4_10.setWidth(200);
        tblclmnNewColumn_4_10.setText(registry.getString("PreCCNEPLInfoPanel.CCN.DesignCharge"));
        tblclmnNewColumn_4_10.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_11 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 43 Width 값
        tblclmnNewColumn_4_11.setWidth(100);
        tblclmnNewColumn_4_11.setText("");
        tblclmnNewColumn_4_11.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_12 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 44 Width 값
        tblclmnNewColumn_4_12.setWidth(100);
        tblclmnNewColumn_4_12.setText(registry.getString("PreCCNEPLInfoPanel.CCN.EstInvestmentCost"));
        tblclmnNewColumn_4_12.setResizable(false);
        
        TableColumn tblclmnNewColumn_4_13 = new TableColumn(table_4, SWT.CENTER);
        // tblclmnNewColumn_5 45-47 Width 값
        tblclmnNewColumn_4_13.setWidth(300);
        tblclmnNewColumn_4_13.setText(registry.getString("PreCCNEPLInfoPanel.CCN.PrdInvestmentCost"));
        tblclmnNewColumn_4_13.setResizable(false);
        
        table_4 = createDynamicColumn(table_4, "4");
        
        table_5 = new Table(table_4, SWT.NONE);
        table_5.setHeaderVisible(true);
        table_5.setLinesVisible(true);
        
        TableColumn tblclmnNewColumn_5_1 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_1.setWidth(35);
        tblclmnNewColumn_5_1.setText(registry.getString("PreCCNEPLInfoPanel.CCN.No"));
        tblclmnNewColumn_5_1.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_2 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_2.setWidth(100);
        tblclmnNewColumn_5_2.setText(registry.getString("PreCCNEPLInfoPanel.CCN.UniqueNO"));
        tblclmnNewColumn_5_2.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_3 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_3.setWidth(100);
        tblclmnNewColumn_5_3.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Contents"));
        tblclmnNewColumn_5_3.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_4 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_4.setWidth(50);
        tblclmnNewColumn_5_4.setText(registry.getString("PreCCNEPLInfoPanel.CCN.SysCode"));
        tblclmnNewColumn_5_4.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_5 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_5.setWidth(200);
        tblclmnNewColumn_5_5.setText(registry.getString("PreCCNEPLInfoPanel.CCN.SysCodeNmae"));
        tblclmnNewColumn_5_5.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_6 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_6.setWidth(70);
        tblclmnNewColumn_5_6.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Func"));
        tblclmnNewColumn_5_6.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_7 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_7.setWidth(50);
        tblclmnNewColumn_5_7.setText(registry.getString("PreCCNEPLInfoPanel.CCN.LEV_M"));
        tblclmnNewColumn_5_7.setResizable(false);
        
//        TableColumn tblclmnNewColumn_5_8 = new TableColumn(table_5, SWT.CENTER);
//        tblclmnNewColumn_5_8.setWidth(50);
//        tblclmnNewColumn_5_8.setText(registry.getString("PreCCNEPLInfoPanel.CCN.LEV_A"));
//        tblclmnNewColumn_5_8.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_9 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_9.setWidth(60);
        tblclmnNewColumn_5_9.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Seq"));
        tblclmnNewColumn_5_9.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_10 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_10.setWidth(120);
        tblclmnNewColumn_5_10.setText(registry.getString("PreCCNEPLInfoPanel.CCN.ParentNo"));
        tblclmnNewColumn_5_10.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_11 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_11.setWidth(80);
        tblclmnNewColumn_5_11.setText(registry.getString("PreCCNEPLInfoPanel.CCN.OldPartNo"));
        tblclmnNewColumn_5_11.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_12 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_12.setWidth(100);
        tblclmnNewColumn_5_12.setText(registry.getString("PreCCNEPLInfoPanel.CCN.PartNo"));
        tblclmnNewColumn_5_12.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_13 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_13.setWidth(100);
        tblclmnNewColumn_5_13.setText(registry.getString("PreCCNEPLInfoPanel.CCN.PartName"));
        tblclmnNewColumn_5_13.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_14 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_14.setWidth(100);
        tblclmnNewColumn_5_14.setText(registry.getString("PreCCNEPLInfoPanel.CCN.ReqOPT"));
        tblclmnNewColumn_5_14.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_15 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_15.setWidth(150);
        tblclmnNewColumn_5_15.setText(registry.getString("PreCCNEPLInfoPanel.CCN.SpecDesc"));
        tblclmnNewColumn_5_15.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_16 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_16.setWidth(100);
        tblclmnNewColumn_5_16.setText(registry.getString("PreCCNEPLInfoPanel.CCN.OptCondition"));
        tblclmnNewColumn_5_16.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_17 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_17.setWidth(60);
        tblclmnNewColumn_5_17.setText(registry.getString("PreCCNEPLInfoPanel.CCN.ChgTypeECC"));
        tblclmnNewColumn_5_17.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_18 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_18.setWidth(60);
        tblclmnNewColumn_5_18.setText(registry.getString("PreCCNEPLInfoPanel.CCN.EngProj"));
        tblclmnNewColumn_5_18.setResizable(false);
        
        //[SR170703-020][LJG]Proto Tooling 컬럼 추가
        TableColumn tblclmnNewColumn_5_50 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_50.setWidth(110);
        tblclmnNewColumn_5_50.setText("Proto Tooling");
        tblclmnNewColumn_5_50.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_19 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_19.setWidth(60);
        tblclmnNewColumn_5_19.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Smode"));
        tblclmnNewColumn_5_19.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_20 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_20.setWidth(80);
        tblclmnNewColumn_5_20.setText(registry.getString("PreCCNEPLInfoPanel.CCN.EstWeight"));
        tblclmnNewColumn_5_20.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_21 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_21.setWidth(80);
        tblclmnNewColumn_5_21.setText(registry.getString("PreCCNEPLInfoPanel.CCN.TgtWeight"));
        tblclmnNewColumn_5_21.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_22 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_22.setWidth(60);
        tblclmnNewColumn_5_22.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Module"));
        tblclmnNewColumn_5_22.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_23 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_23.setWidth(50);
        tblclmnNewColumn_5_23.setText(registry.getString("PreCCNEPLInfoPanel.CCN.AlterPart"));
        tblclmnNewColumn_5_23.setResizable(false);
       
        TableColumn tblclmnNewColumn_5_24 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_24.setWidth(50);
        tblclmnNewColumn_5_24.setText(registry.getString("PreCCNEPLInfoPanel.CCN.DR"));
        tblclmnNewColumn_5_24.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_25 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_25.setWidth(100);
        tblclmnNewColumn_5_25.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Responsibilit"));
        tblclmnNewColumn_5_25.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_42 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_42.setWidth(100);
        tblclmnNewColumn_5_42.setText(registry.getString("PreCCNEPLInfoPanel.CCN.ChangeDesc"));
        tblclmnNewColumn_5_42.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_26 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_26.setWidth(100);
        tblclmnNewColumn_5_26.setText(registry.getString("PreCCNEPLInfoPanel.CCN.EstMaterial"));
        tblclmnNewColumn_5_26.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_27 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_27.setWidth(100);
        tblclmnNewColumn_5_27.setText(registry.getString("PreCCNEPLInfoPanel.CCN.TgtMaterial"));
        tblclmnNewColumn_5_27.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_28 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_28.setWidth(100);
        tblclmnNewColumn_5_28.setText(registry.getString("PreCCNEPLInfoPanel.CCN.NecessaryQty"));
        tblclmnNewColumn_5_28.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_29 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_29.setWidth(100);
        tblclmnNewColumn_5_29.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Use"));
        tblclmnNewColumn_5_29.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_30 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_30.setWidth(100);
        tblclmnNewColumn_5_30.setText(registry.getString("PreCCNEPLInfoPanel.CCN.ReqTeam"));
        tblclmnNewColumn_5_30.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_31 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_31.setWidth(100);
        tblclmnNewColumn_5_31.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Perform"));
        tblclmnNewColumn_5_31.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_32 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_32.setWidth(100);
        tblclmnNewColumn_5_32.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Plan"));
        tblclmnNewColumn_5_32.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_33 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_33.setWidth(100);
        tblclmnNewColumn_5_33.setText(registry.getString("PreCCNEPLInfoPanel.CCN.2D/3D"));
        tblclmnNewColumn_5_33.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_34 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_34.setWidth(100);
        tblclmnNewColumn_5_34.setText(registry.getString("PreCCNEPLInfoPanel.CCN.RelDate"));
        tblclmnNewColumn_5_34.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_35 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_35.setWidth(100);
        tblclmnNewColumn_5_35.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Perform"));
        tblclmnNewColumn_5_35.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_36 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_36.setWidth(100);
        tblclmnNewColumn_5_36.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Plan"));
        tblclmnNewColumn_5_36.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_37 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_37.setWidth(100);
        tblclmnNewColumn_5_37.setText(registry.getString("PreCCNEPLInfoPanel.CCN.EcoNo"));
        tblclmnNewColumn_5_37.setResizable(false);
        
        //[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
        TableColumn tblclmnNewColumn_5_51 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_51.setWidth(100);
        tblclmnNewColumn_5_51.setText("OSPEC NO");
        tblclmnNewColumn_5_51.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_38 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_38.setWidth(100);
        tblclmnNewColumn_5_38.setText(registry.getString("PreCCNEPLInfoPanel.CCN.DocNo"));
        tblclmnNewColumn_5_38.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_39 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_39.setWidth(100);
        tblclmnNewColumn_5_39.setText(registry.getString("PreCCNEPLInfoPanel.CCN.RelDate"));
        tblclmnNewColumn_5_39.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_40 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_40.setWidth(100);
        tblclmnNewColumn_5_40.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Team"));
        tblclmnNewColumn_5_40.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_41 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_41.setWidth(100);
        tblclmnNewColumn_5_41.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Charger"));
        tblclmnNewColumn_5_41.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_43 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_43.setWidth(100);
        tblclmnNewColumn_5_43.setText(registry.getString("PreCCNEPLInfoPanel.CCN.SelectedCompany"));
        tblclmnNewColumn_5_43.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_44 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_44.setWidth(100);
        tblclmnNewColumn_5_44.setText(registry.getString("PreCCNEPLInfoPanel.CCN.ProtoToolg"));
        tblclmnNewColumn_5_44.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_45 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_45.setWidth(100);
        tblclmnNewColumn_5_45.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Toolg"));
        tblclmnNewColumn_5_45.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_46 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_46.setWidth(100);
        tblclmnNewColumn_5_46.setText(registry.getString("PreCCNEPLInfoPanel.CCN.SvcCost"));
        tblclmnNewColumn_5_46.setResizable(false);
        
        TableColumn tblclmnNewColumn_5_47 = new TableColumn(table_5, SWT.CENTER);
        tblclmnNewColumn_5_47.setWidth(100);
        tblclmnNewColumn_5_47.setText(registry.getString("PreCCNEPLInfoPanel.CCN.Sample"));
        tblclmnNewColumn_5_47.setResizable(false);
        
        table_5 = createDynamicColumn(table_5, "5");
        
        // 테이블 높이는 setTableData() 에 finaly 에서도 컨트롤 한다 (값에 갯수에 따라 변화)
        btnReload.setBounds(0, 0, 60, 30);
//        btnReloadOld.setBounds(100, 0, 80, 30);
        table_1.setBounds(1, 31, totalWidth + 20, 160);
        table_2.setBounds(0, 25, totalWidth + 20, 140);
        table_3.setBounds(0, 25, totalWidth + 20, 120);
        table_4.setBounds(0, 25, totalWidth + 20, 100);
        table_5.setBounds(0, 25, totalWidth + 20, 80);

        setSize(totalWidth + 10 + 1, 160);
    }
    
    private Table createDynamicColumn(Table targetTable, String depth) {
        if (null == ospecList || ospecList.size() == 0) {
            return targetTable;
        }
        ArrayList<String> compareList = new ArrayList<String>();
        String mapKey = "LV" + depth + "_KEY";
        String mapCountKey = "LV" + depth + "_COUNT";
        String mapAttrKey = "USAGE_LV" + depth;
        TableColumn dmColumn;
        int columnSize = 57;
        columnSeq = 0;
        usageCP = new HashMap<String, Integer>();
        for (HashMap<String, Object> mapData : ospecList) {
            if (!depth.equals("5")) {
                if (!compareList.contains(mapData.get(mapKey))) {
                    int count = Integer.parseInt(mapData.get(mapCountKey).toString());
                    dmColumn = new TableColumn(targetTable, SWT.CENTER);
                    dmColumn.setText(mapData.get(mapAttrKey).toString());
                    dmColumn.setWidth(columnSize * count);
                    dmColumn.setResizable(false);
                    compareList.add(mapData.get(mapKey).toString());
                }
            } else {
                dmColumn = new TableColumn(targetTable, SWT.CENTER);
                dmColumn.setText(mapData.get(mapAttrKey).toString());
                dmColumn.setWidth(columnSize);
                dmColumn.setResizable(false);
                usageCP.put(mapData.get(mapKey).toString(), columnSeq);
                columnSeq++;
                totalWidth += columnSize;
            }
        }
        return targetTable;
    }

	/*	
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *	데이터 생성 로직 변경으로 사용하지 않아 주석 처리 executeLoadProcess_ 사용함 
	*/
//    private void executeLoadProcess(final boolean isReload, final String type) {
//        new Job("EPL Load...") {
//            @Override
//            protected IStatus run(IProgressMonitor arg0) {
//                try {
//                    getDisplay().syncExec(new Runnable() {
//                        public void run() {
//                            setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
//                        }
//                    });
//                    if(isReload){
//                    	buildCCNEPL(type);
//                        masterList = selectMasterInfoList(ccnRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));                          
//                        setTableData();
//                    }else{
//                        masterList = selectMasterInfoList(ccnRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));
////                        setTableData();
//                    }
//                } catch(final Exception e) {
//                    getDisplay().syncExec(new Runnable() {
//                        public void run() {
//                            MessageBox.post(getShell(), e.getMessage(), "Notification", 2);
//                        }
//                    });
//                    return Status.CANCEL_STATUS;
//                } finally {
//                    getDisplay().syncExec(new Runnable() {
//                        public void run() {
//                            setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
//                        }
//                    });
//                }
//                return Status.OK_STATUS;
//            }
//        }.schedule();    
//    }
    
	/*	
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *	데이터 생성 로직 변경으로 변수 type을 사용 안해 삭제 
	 *  private void executeLoadProcess_(final boolean isReload, final String type) throws Exception{
	*/
    private void executeLoadProcess_(final boolean isReload) throws Exception{
        if(isReload){
        	buildCCNEPL();
            masterList = selectMasterInfoList(ccnRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));                          
            setTableData();
        }else{
            masterList = selectMasterInfoList(ccnRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));
//                        setTableData();
        }
    }
    
    private ArrayList<HashMap< String, Object>> selectMasterInfoList(String ccnId) {                                                
        CustomCCNDao dao = null;
        ArrayList<HashMap< String, Object>> resultList = new ArrayList<HashMap< String, Object>>();
        try {
            dao = new CustomCCNDao();
            resultList = dao.selectMasterInfoList(ccnId);
//            resultList = dao.selectMasterInfoList("CNX100C101504");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    private ArrayList<HashMap< String, Object>> selectMasterUsageInfoList(String listId, String historyType) {                                                
        CustomCCNDao dao = null;
        ArrayList<HashMap< String, Object>> resultList = new ArrayList<HashMap< String, Object>>();
        try {
            dao = new CustomCCNDao();
            resultList = dao.selectMasterUsageInfoList(listId, historyType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    
    protected class EPLTableItem extends TableItem {

        protected HashMap<String, Object> bomEditData;
        protected ArrayList<HashMap<String, Object>> usageList;
        protected ArrayList<Integer> usageNoList;
        protected ArrayList<String> usageNoCountList;
        private boolean isOld;
        private boolean hasPart;
        private boolean isMultiOption;
        //[CSH][20180502]CCN epl column 정합성 조정
//        private int columnSize = 47;
        private int columnSize = 48;

        public EPLTableItem(HashMap<String, Object> bomEditData, boolean isOld, boolean isMultiOption, ArrayList<HashMap<String, Object>> usageList) throws TCException {
            super(table_5, SWT.None);
            this.bomEditData = bomEditData;
            this.usageList = usageList;
            this.isOld = isOld;
            this.isMultiOption = isMultiOption;
            if(isOld) {
                setOldData();
            } else {
                setNewData();
            }
            setRowProperty();
        }

        protected boolean isOld() {
            return isOld;
        }

        protected boolean hasPart() {
            return hasPart;
        }

        protected void setOldData() throws TCException {
            hasPart = true;

            String[] rowOldItemData = new String[columnSize + columnSeq];
            // No.
            rowOldItemData[0] = Integer.toString(rowSeq);
    
            //------------------------------------------------------------------------
            // UNIQ-No
            rowOldItemData[1] = (String) bomEditData.get("OLD_CHILD_UNIQUE_NO");
            
            // Contents
            rowOldItemData[2] = (String) bomEditData.get("OLD_CONTENTS");
            
            // System Code
            rowOldItemData[3] = (String) bomEditData.get("OLD_SYSTEM_CODE");
            
            // System Code Name
            rowOldItemData[4] = (String) bomEditData.get("OLD_SYSTEM_NAME");
            
            // FUNC
            rowOldItemData[5] = (String) bomEditData.get("OLD_FUNCTION");
            
            // LEV(MAN)
            rowOldItemData[6] = getStringValue(bomEditData.get("OLD_LEV"));
            
            // Seq
            rowOldItemData[7] = (String) bomEditData.get("OLD_SEQ");
            
            // Parent NO.
            if (null != bomEditData.get("OLD_CHILD_NO") && !bomEditData.get("OLD_CHILD_NO").equals("")) {
                rowOldItemData[8] = (String) bomEditData.get("PARENT_UNIQUE_NO");
            }else{
                rowOldItemData[8] = "";
            }
            
            // Old P/No.
            rowOldItemData[9] = (String) bomEditData.get("OLD_PRD_PART_NO");
            
            // P/No.
            rowOldItemData[10] = (String) bomEditData.get("OLD_CHILD_NO");
            
            // P/Name
            rowOldItemData[11] = (String) bomEditData.get("OLD_CHILD_NAME");
            
            // REQ. OPT
            rowOldItemData[12] = (String) bomEditData.get("OLD_REQ_OPT");
            
            // SPEC Desc
//            rowOldItemData[13] = (String) bomEditData.get("OLD_SPEC_DESC");
            rowOldItemData[13] = (String) bomEditData.get("OLD_SPECIFICATION");
            
            // OPT. Condition
            rowOldItemData[14] = (String) bomEditData.get("OLD_VC");
            
            // ENG. CONCEPT N,M,C,D
            rowOldItemData[15] = (String) bomEditData.get("OLD_CHG_TYPE_ENGCONCEPT");
            
            // ENG. CONCEPT Project
            rowOldItemData[16] = (String) bomEditData.get("OLD_PROJECT");
            
            // Proto Tooling
            rowOldItemData[17] = (String) bomEditData.get("OLD_PROTO_TOOLING");
            
            // S/MODE
            rowOldItemData[18] = (String) bomEditData.get("OLD_SMODE");
            
            // WEIGHT(KG) ESTIMATE
            rowOldItemData[19] = getStringValue(bomEditData.get("OLD_EST_WEIGHT"));
            
            // WEIGHT(KG) TARGET
            rowOldItemData[20] = getStringValue(bomEditData.get("OLD_TGT_WEIGHT"));
            
            // Module
            rowOldItemData[21] = (String) bomEditData.get("OLD_MODULE");
            
            // AlterPart
            rowOldItemData[22] = (String) bomEditData.get("OLD_ALTER_PART");

            // DR
            rowOldItemData[23] = (String) bomEditData.get("OLD_REGULATION");
            
            // Responsibility
            rowOldItemData[24] = (String) bomEditData.get("OLD_BOX");
            
            // Change Description
            rowOldItemData[25] = "";
            
            // Material Cost ESTIMATE
            rowOldItemData[26] = "";
            
            // Material Cost TARGET
            rowOldItemData[27] = "";
            
            // DVP SAMPLE NECESSARY QTY
            rowOldItemData[28] = getStringValue(bomEditData.get("OLD_DVP_NEEDED_QTY"));
            
            // DVP SAMPLE USE
            rowOldItemData[29] = (String) bomEditData.get("OLD_DVP_USE");
            
            // DVP SAMPLE REQ TEAM
            rowOldItemData[30] = (String) bomEditData.get("OLD_DVP_REQ_DEPT");
            
            // CON DWG PERFORM
            rowOldItemData[31] = (String) bomEditData.get("OLD_CON_DWG_PERFORMANCE");
            
            // CON DWG PLAN
            rowOldItemData[32] = (String) bomEditData.get("OLD_CON_DWG_PLAN");
            
            // CON DWG 2D/3D
            rowOldItemData[33] = (String) bomEditData.get("OLD_CON_DWG_TYPE");
            
            // CON DWG REL. DATE
            rowOldItemData[34] = SDVPreBOMUtilities.getChangeCCNDBDate((String)bomEditData.get("OLD_DWG_DEPLOYABLE_DATE"));
            
            // PRD DWG PERFORM
            rowOldItemData[35] = (String) bomEditData.get("OLD_PRD_DWG_PERFORMANCE");
            
            // PRD DWG PLAN
            rowOldItemData[36] = (String) bomEditData.get("OLD_PRD_DWG_PLAN");
            
            // PRD DWG ECO/NO
            rowOldItemData[37] = (String) bomEditData.get("OLD_ECO");
            
            //[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
            // OSPEC NO
            rowOldItemData[38] = (String) bomEditData.get("OLD_OSPEC_NO");
            
            // Doc. No.
            rowOldItemData[39] = (String) bomEditData.get("OLD_DC_ID");
            
            // Rel. Date
            rowOldItemData[40] = SDVPreBOMUtilities.getChangeCCNDBDate((String) bomEditData.get("OLD_RELEASED_DATE"));
            
            // Design Charge TEAM
            rowOldItemData[41] = (String) bomEditData.get("OLD_ENG_DEPT_NM");
            
            // Design Charge CHARGER
            rowOldItemData[42] = (String) bomEditData.get("OLD_ENG_RESPONSIBLITY");
            
            // SELECTED COMPANY
            rowOldItemData[43] = "";
            
            // EST. INVESTMENT COST PROTO TOOL'G
            rowOldItemData[44] = "";
            
            // PRD. INVESTMENT COST TOOL'G
            rowOldItemData[45] = "";
            
            // PRD. INVESTMENT COST SVC. COST
            rowOldItemData[46] = "";
            
            // PRD. INVESTMENT COST SAMPLE
            rowOldItemData[47] = "";
            
//            // OPTION_TYPE
//            String optionTypeStr = "";
//            if (isMultiOption) {
//                rowOldItemData[41] = optionType[optionTypeSeq];
//                optionTypeStr = optionType[optionTypeSeq];
//            } else {
//                rowOldItemData[41] = (String) bomEditData.get("OPTION_TYPE_NAME");
//                optionTypeStr = (String) bomEditData.get("OPTION_TYPE_NAME");
//            }
            
            // UsageList 
            if (null != usageList && usageList.size() > 0) {
//                getUsagePosition(usageList, optionTypeStr);
                getUsagePosition(usageList);
                for (int i = 0; i < usageNoList.size(); i++) {
                    rowOldItemData[columnSize + usageNoList.get(i)] = usageNoCountList.get(i);
                }
            }
            
            setText(rowOldItemData);
        }

        private void setNewData() throws TCException {
            hasPart = true;

            String[] rowNewItemData = new String[columnSize + columnSeq];
            // No.
            rowNewItemData[0] = Integer.toString(rowSeq);
            
            //------------------------------------------------------------------------
            // UNIQ-No
            rowNewItemData[1] = (String) bomEditData.get("NEW_CHILD_UNIQUE_NO");
            
            // Contents
            rowNewItemData[2] = (String) bomEditData.get("NEW_CONTENTS");
            
            // System Code
            rowNewItemData[3] = (String) bomEditData.get("NEW_SYSTEM_CODE");
            
            // System Code Name
            rowNewItemData[4] = (String) bomEditData.get("NEW_SYSTEM_NAME");
            
            // FUNC
            rowNewItemData[5] = (String) bomEditData.get("NEW_FUNCTION");
            
            // LEV(MAN)
            rowNewItemData[6] = getStringValue(bomEditData.get("NEW_LEV"));
            
            // Seq
            rowNewItemData[7] = (String) bomEditData.get("NEW_SEQ");
            
            // Parent NO.
            if (null != bomEditData.get("NEW_CHILD_NO") && !bomEditData.get("NEW_CHILD_NO").equals("")) {
                rowNewItemData[8] = (String) bomEditData.get("PARENT_UNIQUE_NO");
            }else{
                rowNewItemData[8] = "";
            }
            
            // Old P/No.
            rowNewItemData[9] = (String) bomEditData.get("NEW_PRD_PART_NO");
            
            // P/No.
            rowNewItemData[10] = (String) bomEditData.get("NEW_CHILD_NO");
            
            // P/Name
            rowNewItemData[11] = (String) bomEditData.get("NEW_CHILD_NAME");
            
            // REQ. OPT
            rowNewItemData[12] = (String) bomEditData.get("NEW_REQ_OPT");
            
            // SPEC Desc
//            rowNewItemData[13] = (String) bomEditData.get("NEW_SPEC_DESC");
            rowNewItemData[13] = (String) bomEditData.get("NEW_SPECIFICATION");
            
            // OPT. Condition
            rowNewItemData[14] = (String) bomEditData.get("NEW_VC");
            
            // ENG. CONCEPT N,M,C,D
            rowNewItemData[15] = (String) bomEditData.get("NEW_CHG_TYPE_ENGCONCEPT");
            
            // ENG. CONCEPT Project
            rowNewItemData[16] = (String) bomEditData.get("NEW_PROJECT");
            
            // Proto Tooling
            rowNewItemData[17] = (String) bomEditData.get("NEW_PROTO_TOOLING");
            
            // S/MODE
            rowNewItemData[18] = (String) bomEditData.get("NEW_SMODE");
            
            // WEIGHT(KG) ESTIMATE
            rowNewItemData[19] = getStringValue(bomEditData.get("NEW_EST_WEIGHT"));
            
            // WEIGHT(KG) TARGET
            rowNewItemData[20] = getStringValue(bomEditData.get("NEW_TGT_WEIGHT"));
            
            // Module
            rowNewItemData[21] = (String) bomEditData.get("NEW_MODULE");
            
            // AlterPart
            rowNewItemData[22] = (String) bomEditData.get("NEW_ALTER_PART");

            // DR
            rowNewItemData[23] = (String) bomEditData.get("NEW_REGULATION");
            
            // Responsibility
            rowNewItemData[24] = (String) bomEditData.get("NEW_BOX");
            
            // Change Description
            rowNewItemData[25] = (String) bomEditData.get("CHANGE_DESC");
            
            // Material Cost ESTIMATE
            rowNewItemData[26] = (String) bomEditData.get("EST_COST_MATERIAL");
            
            // Material Cost TARGET
            rowNewItemData[27] = (String) bomEditData.get("TGT_COST_MATERIAL");
            
            // DVP SAMPLE NECESSARY QTY
            rowNewItemData[28] = getStringValue(bomEditData.get("NEW_DVP_NEEDED_QTY"));
            
            // DVP SAMPLE USE
            rowNewItemData[29] = (String) bomEditData.get("NEW_DVP_USE");
            
            // DVP SAMPLE REQ TEAM
            rowNewItemData[30] = (String) bomEditData.get("NEW_DVP_REQ_DEPT");
            
            // CON DWG PERFORM
            rowNewItemData[31] = (String) bomEditData.get("NEW_CON_DWG_PERFORMANCE");
            
            // CON DWG PLAN
            rowNewItemData[32] = (String) bomEditData.get("NEW_CON_DWG_PLAN");
            
            // CON DWG 2D/3D
            rowNewItemData[33] = (String) bomEditData.get("NEW_CON_DWG_TYPE");
            
            // CON DWG REL. DATE
            rowNewItemData[34] = SDVPreBOMUtilities.getChangeCCNDBDate((String)bomEditData.get("NEW_DWG_DEPLOYABLE_DATE"));
            
            // PRD DWG PERFORM
            rowNewItemData[35] = (String) bomEditData.get("NEW_PRD_DWG_PERFORMANCE");
            
            // PRD DWG PLAN
            rowNewItemData[36] = (String) bomEditData.get("NEW_PRD_DWG_PLAN");
            
            // PRD DWG ECO/NO
            rowNewItemData[37] = (String) bomEditData.get("NEW_ECO");
            
            //[SR180315-044][ljg] 설계구상서 및 o-spec no 등록요청
            // OSPEC NO
            rowNewItemData[38] = (String) bomEditData.get("NEW_OSPEC_NO");
            
            // Doc. No.
            rowNewItemData[39] = (String) bomEditData.get("NEW_DC_ID");
            
            // Rel. Date
            rowNewItemData[40] = SDVPreBOMUtilities.getChangeCCNDBDate((String)bomEditData.get("NEW_RELEASED_DATE"));
            
            // Design Charge TEAM
            rowNewItemData[41] = (String) bomEditData.get("NEW_ENG_DEPT_NM");
            
            // Design Charge CHARGER
            rowNewItemData[42] = (String) bomEditData.get("NEW_ENG_RESPONSIBLITY");
            
            // SELECTED COMPANY
            rowNewItemData[43] = (String) bomEditData.get("SELECTED_COMPANY");
            
            // EST. INVESTMENT COST PROTO TOOL'G
            rowNewItemData[44] = "";
            
            // PRD. INVESTMENT COST TOOL'G
            rowNewItemData[45] = (String) bomEditData.get("PRD_TOOL_COST");
            
            // PRD. INVESTMENT COST SVC. COST
            rowNewItemData[46] = (String) bomEditData.get("PRD_SERVICE_COST");
            
            // PRD. INVESTMENT COST SAMPLE
            rowNewItemData[47] = (String) bomEditData.get("PRD_SAMPLE_COST");
            
            // OPTION_TYPE
//            String optionTypeStr = "";
//            if (isMultiOption) {
//                rowNewItemData[41] = optionType[optionTypeSeq];
//                optionTypeStr = optionType[optionTypeSeq];
//            } else {
//                rowNewItemData[41] = (String) bomEditData.get("OPTION_TYPE_NAME");
//                optionTypeStr = (String) bomEditData.get("OPTION_TYPE_NAME");
//            }

            // UsageList (0번이 41)
            if (null != usageList && usageList.size() > 0) {
//                getUsagePosition(usageList, optionTypeStr);
                getUsagePosition(usageList);
                for (int i = 0; i < usageNoList.size(); i++) {
                    rowNewItemData[columnSize + usageNoList.get(i)] = usageNoCountList.get(i);
                }
            }
            
            //------------------------------------------------------------------------

            setText(rowNewItemData);
        }
        
        @SuppressWarnings("static-access")
        protected String getStringValue(Object value){
            if (!toString().valueOf(value).equals("null")) {
                return toString().valueOf(value);
            }else{
                return "";
            }
        }

        protected int getRowNo(boolean isMultiOption) {
            int row = table_5.indexOf(this);
            if (isMultiOption) {
                return row / 4 + 1;
            } else {
                return row / 2 + 1;
            }
        }

        public void setRowProperty() {
//            int rowNum = getRowNo(isMultiOption);
            int rowNum = rowSeq;
            if(hasPart) {
                setText(0, rowNum + "");
            }
            if(rowNum % 2 == 0) {
                if (isMultiOption) {
                    setBackground(modifiableEvenColor);
                } else {
                    setBackground(evenColor);
                }
            } else {
                setBackground(table_5.getBackground());
            }
        }

        protected void checkSubclass() {

        }
        
        @SuppressWarnings("unused")
        private void getUsagePosition(ArrayList<HashMap<String, Object>> usageList, String optionTypeStr) {
            usageNoList = new ArrayList<Integer>();
            usageNoCountList = new ArrayList<String>();
            for (HashMap<String, Object> resultMap : usageList) {
                if (null != resultMap.get("OPTION_TYPE") && resultMap.get("OPTION_TYPE").equals(optionTypeStr)) {
                    if (null != usageCP.get(resultMap.get("LV5_KEY"))) {
                        int seq = usageCP.get(resultMap.get("LV5_KEY"));
                        usageNoList.add(seq);
                        if (resultMap.get("OPTION_TYPE").toString().equals("OPT")) {
                            usageNoCountList.add("(" + resultMap.get("USAGE_QTY").toString() + ")");
                        }else{
                            usageNoCountList.add(resultMap.get("USAGE_QTY").toString());
                        }
                    }
                }
            }
        }
        private void getUsagePosition(ArrayList<HashMap<String, Object>> usageList) {
            usageNoList = new ArrayList<Integer>();
            usageNoCountList = new ArrayList<String>();
            for (HashMap<String, Object> resultMap : usageList) {
                if (null != usageCP.get(resultMap.get("LV5_KEY"))) {
                    int seq = usageCP.get(resultMap.get("LV5_KEY"));
                    usageNoList.add(seq);
                    if (resultMap.get("OPTION_TYPE").toString().equals("OPT")) {
                        usageNoCountList.add("(" + resultMap.get("USAGE_QTY").toString() + ")");
                    }else{
                        usageNoCountList.add(resultMap.get("USAGE_QTY").toString());
                    }
                }
            }
        }
    }
    
    public TCComponentChangeItemRevision getTargetComp(){
        InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
        if (comps.length > 0) {
            TCComponent comp = (TCComponent) comps[0];
            if (comp instanceof TCComponentChangeItemRevision) {
                ccnRevision = (TCComponentChangeItemRevision)comp;
            }
        }
        return ccnRevision;
    }
	/*	
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *	데이터 생성 로직 변경으로 변수 type을 사용 안해 삭제 
	 *  public ArrayList<HashMap<String, Object>> buildCCNEPL(String type) throws Exception { 
	*/
	@SuppressWarnings("static-access")
    public ArrayList<HashMap<String, Object>> buildCCNEPL() throws Exception {

		BomUtil bomUtil = new BomUtil();
		CustomCCNDao dao = new CustomCCNDao();
		String ccn_id = ccnRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
        
		ArrayList<HashMap<String, Object>> arrResultEPL = null;
		/*	
		 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
		 *	if문으로 old체크 하는 부분은 데이터 생성 로직 변경으로 사용하지 않아 주석 처리 buildCCNEPL__을 사용함 
		*/
//		//old
//		if(type.equals("old")){
//			arrResultEPL = bomUtil.buildCCNEPL(ccnRevision, true, null);
//		}
		//new
//		else {
			arrResultEPL = bomUtil.buildCCNEPL__(ccnRevision, true, null);
//		}
//		System.out.println("insertCCNEplList start : "+ new Date());
		// 가져온 값을 DB 테이블에 넣는다
		if (null != arrResultEPL && arrResultEPL.size() > 0) {
//			if(type.equals("old")){
//				dao.insertCCNEplList(ccn_id, arrResultEPL);
//			} else {
//				//new
				dao.insertCCNEplList_(ccn_id, arrResultEPL);
//			}
        }
//		System.out.println("insertCCNMaster start : "+ new Date());
		// CCN Master 테이블에 값을 넣는다
        dao.deleteCCNMaster(ccn_id);
        dao.insertCCNMaster(ccnRevision, false);
//        System.out.println("insertCCNMaster end : "+ new Date());
		this.arrSYMCBOMEditData = arrResultEPL;
		return arrSYMCBOMEditData;
	}
	
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
    }
    
}
