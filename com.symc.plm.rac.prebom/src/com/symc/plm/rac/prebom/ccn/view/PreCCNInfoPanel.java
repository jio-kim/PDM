package com.symc.plm.rac.prebom.ccn.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.common.remote.DataSet;
import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.swtsearch.SearchItemDialog;
import com.kgm.common.utils.PreferenceService;
import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.ccn.commands.CCNProcessCommand;
import com.symc.plm.rac.prebom.ccn.validator.PreCCNValidator;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.SDVLOVUtils;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.symc.plm.rac.prebom.common.viewer.AbstractPreSYMCViewer;
import com.symc.plm.rac.prebom.dcs.common.DCSCommonUtil;
import com.symc.plm.rac.prebom.dcs.dialog.SearchProjectCodeDialog;
import com.symc.plm.rac.prebom.dcs.dialog.SearchUserDialog;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * Product Part Information Panel
 * [20170116] Project 코드 검색 변경
 * [20170321] Project Type Null 값 오류 수정
 */
public class PreCCNInfoPanel extends AbstractPreSYMCViewer {
    public TCSession session;
    private Registry registry;
    /** View 속성 */
    private SYMCLabel lblProjcode;
    private SYMCLabel lblProjtype;
    private SYMCLabel lblSysLabel;
    private SYMCLabel lblDecLabel;
    private SYMCLabel lblCheckpoint;
    private SYMCLabel lblOspecLabel;
    private SYMCLabel lblGateLabel;
    private SYMCLabel lblSysLabel2;
    private SYMCLabel lblDTargetLabel;
    
    private SWTComboBox sysCombo;
    private SWTComboBox projTypeCombo;
    private SYMCText projTxt;
    private SYMCText descTxt;
    private SYMCText ospecText;
    private SYMCText gateText;
    public SYMCText sysCodesTxt;
    public SYMCText deployTargetTxt;
    
    private Button btnProjSearch;
    private Button approveButton;
//    private Button approveButton_old;
    private Button btnRegulation;
    private Button btnCostDown;
    private Button btnOrderingSpec;
    private Button btnQualityImprovement;
    private Button btnCorrectionOfEPL;
    private Button btnStylingUpDate;
    private Button btnWeightChange;
    private Button btnMaterialCostChange;
    private Button btnTheOthers;
    
    private Button btnDTargetSearch;
    
    private boolean isReleased;
    public boolean isCreate = false;
    
    private Composite layoutComposite;
    
    private String[] ccnInfoProperties;
    
    private String[] userIDList;
    
    public boolean viewFlag = false;
    
    public TCComponentChangeItemRevision ccnRevision;
    
    public HashMap<String, Control> ccnInfoNControlMap = new HashMap<String, Control>();
    
    private HashMap<String, String> ccnPropertyMap = new HashMap<String, String>();

    public static String SKIP_ENABLE = "skip";
    private ArrayList<Control> madatoryControls;

    /** 조회시 Target Revision */
    TCComponentItemRevision targetRevision;
    /** 화면에 입력된 속성 Map */
    public HashMap<String, Object> attrMap;
    /** 최초 Loading시 속성 Map(수정항목 Check를 위해 사용, attrMap과 oldAttrMap을 비교) */
    public HashMap<String, Object> oldAttrMap;
    /** PartManage Dialog에서 넘어온 Param Map */
    HashMap<String, Object> paramMap;
    /** SaveAs시 Target Revison */
    TCComponentItemRevision baseItemRev;
    
    /**
     * Create CCN Menu를 통해 호출됨
     * 
     * @param parent
     * @param paramMap
     *            : PartManage Dialog에서 넘어온 Param Map
     * @param style
     *            : Dialog SWT Style
     * @throws TCException 
     * @wbp.parser.constructor
     */
    public PreCCNInfoPanel(Composite parent){
        super(parent);
        initData();
        ccnRevision = getTargetComp();
    }
    
    public PreCCNInfoPanel(Composite parent, boolean isDialog){
        super(parent);
        this.isCreate = isDialog;
        initData();
        ccnRevision = getTargetComp();
    }
    
    @Override
    public void createPanel(Composite parent) {
        session = SDVPreBOMUtilities.getTCSession();
        registry = Registry.getRegistry(this);
        madatoryControls = new ArrayList<Control>();
        
        /** 선택 오브젝트 확인 **/
        InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
        if (comp != null && comp instanceof TCComponentChangeItemRevision) {
            TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) comp;
            if (changeRevision.getType().startsWith(TypeConstant.S7_PRECCNREVISIONTYPE)) {
                this.ccnRevision = changeRevision;
            }

            checkReleased();
        }
        initUI();
    }
    
    /**
     * 현재 CCN이 Released되어있는지 확인
     */
    private void checkReleased() {
    	String[] processProps = null;
        try { 
//            processProps = ccnRevision.getProperties(new String[] { PropertyConstant.ATTR_NAME_DATERELEASED, PropertyConstant.ATTR_NAME_PROCESSSTAGELIST });
            processProps = ccnRevision.getProperties(new String[] { PropertyConstant.ATTR_NAME_DATERELEASED, PropertyConstant.ATTR_NAME_STARTEDWORKFLOWTASKS });
        } catch (TCException e) {
            e.printStackTrace();
        }
        if (processProps[0].equals("") || processProps[1].contains("Creator")) {
            isReleased = false;
        } else {
            isReleased = true;
        }
    }

    /**

    /**
     * 화면 초기화
     */
    // [20240305] Layout 내 component 크기 / 위치 수정 
    private void initUI() {
        setLayout(new FillLayout(SWT.HORIZONTAL));
        ccnInfoProperties = new String[] { PropertyConstant.ATTR_NAME_ITEMID, PropertyConstant.ATTR_NAME_PROJCODE, 
                PropertyConstant.ATTR_NAME_PROJCODE, PropertyConstant.ATTR_NAME_SYSTEMCODE, PropertyConstant.ATTR_NAME_PROJECTTYPE, 
                PropertyConstant.ATTR_NAME_OSPECNO, PropertyConstant.ATTR_NAME_GATENO, PropertyConstant.ATTR_NAME_ITEMDESC, 
                PropertyConstant.ATTR_NAME_REGULATION, PropertyConstant.ATTR_NAME_COSTDOWN, PropertyConstant.ATTR_NAME_ORDERINGSPEC,
                PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT, PropertyConstant.ATTR_NAME_CORRECTIONOFEPL, PropertyConstant.ATTR_NAME_STYLINGUPDATE,
                PropertyConstant.ATTR_NAME_WEIGHTCHANGE, PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE, PropertyConstant.ATTR_NAME_THEOTHERS,
                PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET};
        
        layoutComposite = new Composite(this, SWT.NONE);
        layoutComposite.setBounds(0, 0, 697, 384);
        layoutComposite.setBackground(new Color(null, 255, 255, 255));
        
        Group ccnInfoGroup = new Group(layoutComposite, SWT.NONE);
        ccnInfoGroup.setBounds(10, 10, 676, 464);
        ccnInfoGroup.setText(registry.getString("CreateCCNView.CCN.Information"));
        
        lblProjcode = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblProjcode.setBounds(10, 37, 116, 15);
        lblProjcode.setText(registry.getString("CreateCCNView.CCN.ProjectCode"));
        
        projTxt = new SYMCText(ccnInfoGroup, SWT.BORDER);
        projTxt.setBounds(132, 34, 114, 21);
        projTxt.setEditable(false);
        projTxt.setMandatory(true);
        
        btnProjSearch = new Button(ccnInfoGroup, SWT.PUSH);
        btnProjSearch.setBounds(252, 32, 30, 25);
        btnProjSearch.setImage(registry.getImage("Search.ICON2"));
        btnProjSearch.addSelectionListener(new RevSelectionAdapter(projTxt, TypeConstant.S7_PREPROJECTREVISIONTYPE));
        
        lblGateLabel = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblGateLabel.setBounds(320, 37, 73, 15);
        lblGateLabel.setText(registry.getString("CreateCCNView.CCN.GateNo"));
        
        gateText = new SYMCText(ccnInfoGroup, SWT.BORDER);
        gateText.setBounds(399, 34, 127, 23);
        gateText.setMandatory(true);
        gateText.setEnabled(false);
        gateText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        
        lblOspecLabel = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblOspecLabel.setBounds(10, 82, 116, 15);
        lblOspecLabel.setText(registry.getString("CreateCCNView.CCN.OspecNo"));
        
        ospecText = new SYMCText(ccnInfoGroup, SWT.BORDER);
        ospecText.setBounds(132, 78, 114, 21);
        ospecText.setMandatory(true);
        ospecText.setEnabled(false);
        ospecText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        
        lblProjtype = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblProjtype.setBounds(320, 82, 73, 15);
        lblProjtype.setText(registry.getString("CreateCCNView.CCN.ProjectType"));
        
        projTypeCombo = new SWTComboBox(ccnInfoGroup, SWT.BORDER);
        projTypeCombo.setBounds(399, 78, 127, 23);
        SDVLOVUtils.comboValueSetting(projTypeCombo, PropertyConstant.ATTR_NAME_PROJECTTYPE);
        projTypeCombo.setEnabled(false);
        projTypeCombo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        setMandatory(projTypeCombo);
        
        lblSysLabel = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblSysLabel.setBounds(10, 125, 116, 15);
        lblSysLabel.setText(registry.getString("CreateCCNView.CCN.SystemCode"));
        
        sysCombo = new SWTComboBox(ccnInfoGroup, SWT.BORDER);
        sysCombo.setBounds(132, 122, 534, 21);
        sysCombo.setEnabled(true);
        SDVLOVUtils.comboValueSetting(sysCombo, PropertyConstant.ATTR_NAME_SYSTEMCODE);
        setMandatory(sysCombo);
//        sysCombo.setEditable(true);
        
        lblDecLabel = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblDecLabel.setBounds(10, 167, 116, 15);
        lblDecLabel.setText(registry.getString("CreateCCNView.CCN.Description"));
        
        descTxt = new SYMCText(ccnInfoGroup, SWT.BORDER);
        descTxt.setBounds(132, 167, 534, 80);
        descTxt.setEnabled(true);
        descTxt.setMandatory(false);
        
        lblCheckpoint = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblCheckpoint.setBounds(10, 265, 116, 15);
        lblCheckpoint.setText(registry.getString("CreateCCNView.CCN.ChangeReason"));
        
        Group checkGroup = new Group(ccnInfoGroup, SWT.NONE);
        checkGroup.setBounds(132, 250, 534, 80);
        setMandatory(checkGroup);
        
        btnRegulation = new Button(checkGroup, SWT.CHECK);
        btnRegulation.setBounds(10, 15, 153, 16);
        btnRegulation.setText(registry.getString("CreateCCNView.CCN.Regulation"));
        btnRegulation.setEnabled(true);
        
        btnCostDown = new Button(checkGroup, SWT.CHECK);
        btnCostDown.setBounds(190, 15, 180, 16);
        btnCostDown.setText(registry.getString("CreateCCNView.CCN.CostDown"));
        btnCostDown.setEnabled(true);
        
        btnOrderingSpec = new Button(checkGroup, SWT.CHECK);
        btnOrderingSpec.setBounds(370, 15, 130, 16);
        btnOrderingSpec.setText(registry.getString("CreateCCNView.CCN.OrderingSpec"));
        btnOrderingSpec.setEnabled(true);
        
        btnQualityImprovement = new Button(checkGroup, SWT.CHECK);
        btnQualityImprovement.setBounds(10, 36, 180, 16);
        btnQualityImprovement.setText(registry.getString("CreateCCNView.CCN.QualityImprovement"));
        btnQualityImprovement.setEnabled(true);
        
        btnCorrectionOfEPL = new Button(checkGroup, SWT.CHECK);
        btnCorrectionOfEPL.setBounds(190, 36, 180, 16);
        btnCorrectionOfEPL.setText(registry.getString("CreateCCNView.CCN.CorrectionOfEPL"));
        btnCorrectionOfEPL.setEnabled(true);
        
        btnStylingUpDate = new Button(checkGroup, SWT.CHECK);
        btnStylingUpDate.setBounds(370, 36, 130, 16);
        btnStylingUpDate.setText(registry.getString("CreateCCNView.CCN.StylingUpDate"));
        btnStylingUpDate.setEnabled(true);
        
        btnWeightChange = new Button(checkGroup, SWT.CHECK);
        btnWeightChange.setBounds(10, 56, 153, 16);
        btnWeightChange.setText(registry.getString("CreateCCNView.CCN.WeightChange"));
        btnWeightChange.setEnabled(true);																																																							
        
        btnMaterialCostChange = new Button(checkGroup, SWT.CHECK);
        btnMaterialCostChange.setBounds(190, 56, 180, 16);
        btnMaterialCostChange.setText(registry.getString("CreateCCNView.CCN.MaterialCostChange"));
        btnMaterialCostChange.setEnabled(true);
        
        btnTheOthers = new Button(checkGroup, SWT.CHECK);
        btnTheOthers.setBounds(370, 56, 130, 16);
        btnTheOthers.setText(registry.getString("CreateCCNView.CCN.TheOthers"));
        btnTheOthers.setEnabled(true);
                
        
        lblSysLabel2 = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblSysLabel2.setBounds(10, 342, 116, 35);
        lblSysLabel2.setText(registry.getString("CreateCCNView.CCN.AFFECTEDSYSCODE"));
        
        sysCodesTxt = new SYMCText(ccnInfoGroup, SWT.BORDER);
        sysCodesTxt.setBounds(132, 346, 534, 21);
        sysCodesTxt.setEnabled(false);
        sysCodesTxt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        
        lblDTargetLabel = new SYMCLabel(ccnInfoGroup, SWT.NONE);
        lblDTargetLabel.setBounds(10, 386, 116, 35);
        lblDTargetLabel.setText(registry.getString("CreateCCNView.CCN.DeploymentTarget"));
        
        deployTargetTxt = new SYMCText(ccnInfoGroup, SWT.BORDER);
        deployTargetTxt.setBounds(132, 390, 500, 21);
        deployTargetTxt.setEnabled(false);
        //20211119 seho CF-2613 CCN 오류
        //이런 미친, 이걸 텍스트 수정하도록 만들면 우짜노.
        //수정 불가로 만듬.
        deployTargetTxt.setEditable(false);
        
        btnDTargetSearch = new Button(ccnInfoGroup, SWT.PUSH);
        btnDTargetSearch.setBounds(635, 388, 30, 25);
        btnDTargetSearch.setImage(registry.getImage("Search.ICON2"));
        btnDTargetSearch.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e) {
                ArrayList<HashMap<String, Object>> userList = new ArrayList<HashMap<String, Object>>();
                HashMap<String, Object> userMap = new HashMap<String, Object>();
                if (null != userIDList && userIDList.length > 0) {
                    for (String userId : userIDList) {
                    	if(userId == null || userId.trim().isEmpty())
                    	{
                    		continue;
                    	}
                        HashMap<String, Object> saveUserMap = new HashMap<String, Object>();
                        try {
                        	//20211119 seho CF-2613 CCN 오류
                        	//팀센터 유저에 없는 Vnet 유저를 팀센터에서 찾으면 안되지.
                        	//vnet에서 찾도록 바꾸자요.
//                            TCComponentUser user = DCSCommonUtil.getUser(userId);
                        	DataSet dataSet = new DataSet();
                        	dataSet.put("user_id", userId);
                        	List<HashMap<String, Object>> dataList = DCSCommonUtil.selectVNetUserList(dataSet);
                        	if(dataList == null || dataList.isEmpty())
                        	{
                        		//사번에 해당하는 user가 존재하지 않음.
                        		continue;
                        	}
                        	String userName = (String) dataList.get(0).get("USER_NAME");
                            saveUserMap.put("USER_NAME", userName);
                            saveUserMap.put("USER_ID", userId);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        userList.add(saveUserMap);
                    }
                }
                userMap.put("vNetDataList", userList);
                SearchUserDialog userDialog = new SearchUserDialog(parent.getShell(), 1, userMap); 
                HashMap<String, Object> resultMap = (HashMap<String, Object>) userDialog.open();
                if (null != resultMap) {
                    userList = (ArrayList<HashMap<String, Object>>)resultMap.get("vNetDataList");
                    String targetText = "";
                    userIDList = new String[userList.size()];
                    if (null != resultMap) {
                        for (int i = 0; i < userList.size(); i++) {
                            targetText += userList.get(i).get("USER_ID");
                            if ((i + 1) != userList.size()) {
                                targetText += ","; 
                            }
                            userIDList[i] = (String) userList.get(i).get("USER_ID");
                        }
                    }
                    deployTargetTxt.setText(targetText);
                }
            }

        });
        
        approveButton = new Button(ccnInfoGroup, SWT.NONE);
        approveButton.setText(registry.getString("CreateCCNView.CCN.RequestApproval"));
        approveButton.setBounds(196, 430, 134, 25);
        
//        approveButton_old = new Button(ccnInfoGroup, SWT.NONE);
//        approveButton_old.setText("Approval Old");
//        approveButton_old.setBounds(340, 430, 140, 25);
        
        if (null == ccnRevision || isReleased) {
            approveButton.setVisible(false);
//            approveButton_old.setVisible(false);
        }
        approveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    @SuppressWarnings("unused")
                    //[CSH][20180503]preference에서 unPack 설정시 pack 설정 후 진행
					String pseAutoPackPref = PreferenceService.getValue(TCPreferenceService.TC_preference_all, "PSEAutoPackPref");
					if(pseAutoPackPref != null && pseAutoPackPref.equals("0")){
						PreferenceService.setStringValue("PSEAutoPackPref", "1");
//						MessageBox.post("Please select an option below and try again. \nEdit > Optons > Product Structure : 'Pack Structure Manager display by default'", "Information", MessageBox.INFORMATION);
					}
					/*	
					 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
					 *	데이터 생성 로직 변경으로 사용하지 않는 로직 구분 할려고 "new"를 던졌는데 사용하지 않는 생성 로직 주석 처리로 "new" 제거
					 *  CCNProcessCommand command = new CCNProcessCommand(ccnRevision, PreCCNInfoPanel.this, "new"); 
					*/
					CCNProcessCommand command = new CCNProcessCommand(ccnRevision, PreCCNInfoPanel.this);
					
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        
//        approveButton_old.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                try {
//                    @SuppressWarnings("unused")
//                    //[CSH][20180503]preference에서 unPack 설정시 pack 설정 후 진행
//					String pseAutoPackPref = PreferenceService.getValue(TCPreferenceService.TC_preference_all, "PSEAutoPackPref");
//					if(pseAutoPackPref != null && pseAutoPackPref.equals("0")){
//						PreferenceService.setStringValue("PSEAutoPackPref", "1");
////						MessageBox.post("Please select an option below and try again. \nEdit > Optons > Product Structure : 'Pack Structure Manager display by default'", "Information", MessageBox.INFORMATION);
//					} 
//					CCNProcessCommand command = new CCNProcessCommand(ccnRevision, PreCCNInfoPanel.this, "old");
//					
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//            }
//        });
    }
    
    
    /**
     * Composite 반환
     */
    public Composite getComposite() {
        return this;
    }

    public TCComponentChangeItemRevision getCCNRevision() {
        return ccnRevision;
    }
    
    /** 화면 속성 맵 생성 */
    public HashMap<String, String> getParamMap() {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        String value = "";
        for (Object property : ccnInfoNControlMap.keySet().toArray()) {
            value = "";
            if (ccnInfoNControlMap.get(property) instanceof SYMCText) {
                SYMCText con = (SYMCText) ccnInfoNControlMap.get(property);
                value = con.getText();
            } else if (ccnInfoNControlMap.get(property) instanceof SWTComboBox) {
                SWTComboBox con = (SWTComboBox) ccnInfoNControlMap.get(property);
                Object[] selects = con.getSelectedItems();
                if (selects != null) {
                    for (Object select : selects) {
                        if (value.equals("")) {
                            value = select.toString();
                        } else {
                            value = value + con.getMultipleDelimeter() + select.toString();
                        }
                    }
                }
            } else if (ccnInfoNControlMap.get(property) instanceof Button) {
                Button con = (Button) ccnInfoNControlMap.get(property);
                if (con.getSelection()){
                    value = "Y";
                } else {
                    value = "N";
                }
//            } else if (ccnInfoNControlMap.get(property) instanceof DateTime) {
//                SYMCDateTimeButton con = (SYMCDateTimeButton) ccnInfoNControlMap.get(property);
//                if (property.equals("m7_EFFECT_DATE")) {
//                    if ((cbEffectEvent.getTextField().getText()).startsWith(".") || cbEffectEvent.getSelectedItemCount() == 0) {
//                        value = DATE_FORMATTER.format(con.getDate(session));
//                        System.out.println("value-->" + value);
//                    } else {
//                        System.out.println("!!!");
//                    }
//                } else {
//                    value = con.getTCDate(session);
//                }
            }
            if (!((String) property).equals(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET)) {
                paramMap.put((String) property, value);
            }
        }
        return paramMap;
    }
    
    private void initData() {
        //Text 
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_OSPECNO, ospecText);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_PROJCODE, projTxt);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_GATENO, gateText);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, descTxt);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_AFFECTEDSYSCODE, sysCodesTxt);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET, deployTargetTxt);
        
        //Combo
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_SYSTEMCODE, sysCombo);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_PROJECTTYPE, projTypeCombo);
        //Button
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_REGULATION, btnRegulation);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_COSTDOWN, btnCostDown);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_ORDERINGSPEC, btnOrderingSpec);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT, btnQualityImprovement);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL, btnCorrectionOfEPL);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_STYLINGUPDATE, btnStylingUpDate);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_WEIGHTCHANGE, btnWeightChange);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE, btnMaterialCostChange);
        ccnInfoNControlMap.put(PropertyConstant.ATTR_NAME_THEOTHERS, btnTheOthers);
        
        if (isCreate) {
            approveButton.setVisible(false);
//            approveButton_old.setVisible(false);
        }
    }
    
    /** 속성 및 관계 변경 여부 */
    public boolean isModified() {
        if (ccnRevision != null && ccnRevision.isCheckedOut() && isPropertisModified())
            return true;
        return false;
    }

    /** 속성 변경 여부 */
    public boolean isPropertisModified() {
        ccnPropertyMap.remove("item_id");
        HashMap<String, String> paramMap = getParamMap();
        String property = "";
        String param = "";
        for (Object key : ccnPropertyMap.keySet().toArray()) {
            param = paramMap.get(key);
            if (param == null){
                param = "";
            }
            property = ccnPropertyMap.get(key);
            if (property == null){
                property = "";
            }
            if (!param.equals(property)){
                return true;
            }
        }

        return false;
    }
    
    @Override
    public boolean isSavable() {
        return validationCheck();
    }
    
    public void load() {
//        System.out.println("PreCCNInfoPanel.load()");
    }
    
    @Override
    public void save() {
        final HashMap<String, String> saveAttrMap = getParamMap();
        if (validationCheck()) {
            layoutComposite.getDisplay().syncExec(new Runnable() {
                public void run() {
                    try {
                        ccnRevision.setProperties(saveAttrMap);
                        if (null != userIDList && userIDList.length > 0) {
                            TCProperty referenceDeptCodeTCProperty = ccnRevision.getTCProperty(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET);
                            referenceDeptCodeTCProperty.setStringValueArray(userIDList);
                        }
                        //20211119 seho CF-2613 CCN 배포처 제거시 제거 안되는 문제.
                        //값이 있을때만 set을 하면 다 지웠을때는 저장이 안되자나. 아놔~ 다 지웠을 때는 아래와 같이 null을 넣어서 데이터를 지워야 한다.
                        else
                        {
                            TCProperty referenceDeptCodeTCProperty = ccnRevision.getTCProperty(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET);
                            referenceDeptCodeTCProperty.setStringValueArray(null);
                        }
                        ccnRevision.lock();
                        ccnRevision.save();
                        ccnRevision.unlock();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
                    }
                }
            });
        }
    }
    
    public void updateUI() throws TCException {
        projTxt.setEnabled(false);
        projTxt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        
        descTxt.setEnabled(false);
        descTxt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        
        deployTargetTxt.setEnabled(false);
        deployTargetTxt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        
        sysCombo.setEnabled(false);
        sysCombo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        
//        projTypeCombo.setEnabled(false);
//        projTypeCombo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        
        setControlReadOnly(btnProjSearch);
        setControlReadOnly(btnRegulation);
        setControlReadOnly(btnCostDown);
        setControlReadOnly(btnOrderingSpec);
        setControlReadOnly(btnQualityImprovement);
        setControlReadOnly(btnCorrectionOfEPL);
        setControlReadOnly(btnStylingUpDate);
        setControlReadOnly(btnWeightChange);
        setControlReadOnly(btnMaterialCostChange);
        setControlReadOnly(btnTheOthers);
        setControlReadOnly(approveButton);
//        setControlReadOnly(approveButton_old);
        deployTargetTxt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        setControlReadOnly(btnDTargetSearch);
        
        String owning_user = ccnRevision.getProperty("owning_user");
        if (ccnRevision != null && ccnRevision.isCheckedOut() && owning_user.equals(session.getUser().toString())) {
            descTxt.setEnabled(true);
            descTxt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            
            deployTargetTxt.setEnabled(true);
            deployTargetTxt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            btnDTargetSearch.setEnabled(true);
            
            btnRegulation.setEnabled(true);
            btnCostDown.setEnabled(true);
            btnOrderingSpec.setEnabled(true);
            btnQualityImprovement.setEnabled(true);
            btnCorrectionOfEPL.setEnabled(true);
            btnStylingUpDate.setEnabled(true);
            btnWeightChange.setEnabled(true);
            btnMaterialCostChange.setEnabled(true);
            btnTheOthers.setEnabled(true);
            
            approveButton.setEnabled(false);
//            approveButton_old.setEnabled(false);
        } else {
            approveButton.setEnabled(true);
//            approveButton_old.setEnabled(true);
        }
    }
    
    public void setCCNRevison(TCComponentChangeItemRevision ccnRevision) {
        this.ccnRevision = ccnRevision;
    }
    
    public void setProperties() throws TCException {
        layoutComposite.getDisplay().syncExec(new Runnable() {
            public void run() {
                String[] properties = null;
                try {
                    properties = ccnRevision.getProperties(ccnInfoProperties);

                    TCProperty[] tcProperties = ccnRevision.getAllTCProperties();
                    HashMap<String, Object> propsMap = new HashMap<String, Object>();
                    for (TCProperty tcprop : tcProperties) {
                        propsMap.put(tcprop.getPropertyName(), tcprop.getPropertyValue());
                    }

                    for (int i = 0; i < ccnInfoProperties.length; i++) {
                        ccnPropertyMap.put(ccnInfoProperties[i], properties[i]);
                    }

                    for (Object key : ccnPropertyMap.keySet().toArray()) {
                        if (ccnInfoNControlMap.get(key) instanceof SYMCText) {
                            SYMCText con = (SYMCText) ccnInfoNControlMap.get(key);
                            con.setText(ccnPropertyMap.get(key));
                            if (key.equals(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET)) {
                                userIDList = ccnPropertyMap.get(key).split(",");
                            }
                        } else if (ccnInfoNControlMap.get(key) instanceof SWTComboBox) {
                            SWTComboBox con = (SWTComboBox) ccnInfoNControlMap.get(key);
                            String strings = ccnPropertyMap.get(key);
                            con.setSelectedItems(strings.split(con.getMultipleDelimeter()));
                        } else if (ccnInfoNControlMap.get(key) instanceof Button) {
                            Button con = (Button) ccnInfoNControlMap.get(key);
                            if (ccnPropertyMap.get(key).equals("True")){
                                con.setSelection(true);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.post(getShell(), e.toString(), "ERROR in setProperties()", MessageBox.ERROR);
                } finally {
                    setCursor(new Cursor(layoutComposite.getDisplay(), SWT.CURSOR_ARROW));
                }
            }
        });
        updateUI();
    }
    
    private void setMandatory(Control con) {
        ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
        Registry registry = Registry.getRegistry("com.kgm.common.common");
        dec.setImage(registry.getImage("CONTROL_MANDATORY"));
        dec.setDescriptionText("This value will be required.");
        madatoryControls.add(con);
    }
    
    
    /**
     * 화면에 입력된 속성 값을 저장
     * 
     * @param attributeMap
     *            : 속성값이 저장될 HashMap
     * @return
     * @throws Exception
     */
    public HashMap<String, Object> getPropDataMap() throws Exception {
        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        if (null != sysCombo.getSelectedItem()) {
            attributeMap.put(PropertyConstant.ATTR_NAME_SYSTEMCODE, sysCombo.getSelectedItem());
        }
        if (null != projTypeCombo.getSelectedItem()) {
            attributeMap.put(PropertyConstant.ATTR_NAME_PROJECTTYPE, projTypeCombo.getSelectedItem());
        }
        
        attributeMap.put(PropertyConstant.ATTR_NAME_OSPECNO, ospecText.getText());
        attributeMap.put(PropertyConstant.ATTR_NAME_AFFECTEDSYSCODE, sysCodesTxt.getText());
        attributeMap.put(PropertyConstant.ATTR_NAME_GATENO, gateText.getText());
        attributeMap.put(PropertyConstant.ATTR_NAME_PROJCODE, projTxt.getText());
        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, descTxt.getText());
        attributeMap.put(PropertyConstant.ATTR_NAME_REGULATION, btnRegulation.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_COSTDOWN, btnCostDown.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_ORDERINGSPEC, btnOrderingSpec.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT, btnQualityImprovement.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL, btnCorrectionOfEPL.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_STYLINGUPDATE, btnStylingUpDate.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_WEIGHTCHANGE, btnWeightChange.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE, btnMaterialCostChange.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_THEOTHERS, btnTheOthers.getSelection());
        attributeMap.put(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET, userIDList);
//        attributeMap.put(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET, deployTargetTxt.getText());

        return attributeMap;
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
    
    public boolean validationCheck(){
        try    {
            HashMap<String, Object> resultMap = getPropDataMap();
            // CCN Validator
            PreCCNValidator validator = new PreCCNValidator();
            String strMessage = validator.validate(resultMap, PreCCNValidator.TYPE_VALID_CREATE);

            // Error 발생시 메시지 출력
            if (!SDVPreBOMUtilities.isEmpty(strMessage)) {
                MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
                return false;
            }
        } catch (Exception e) {
            MessageBox.post(getShell(), e.getMessage(), "Warning", MessageBox.WARNING);
            return false;
        }
        return true;
    }
    
    /**
     * Revision 검색시 사용되는 Selection Adapter
     */
    class RevSelectionAdapter extends SelectionAdapter {
        /** 검색된 Item ID값이 Setting될 Field */
        SYMCText targetText;
        /** 검색할 Revision Type */
        String strItemRevType;

        /**
         * Selection Adapter 생성자
         * 
         * @param text
         *            : 검색된 Item ID값이 Setting될 Field
         * @param strItemRevType
         *            : 검색할 Revision Type
         */
        RevSelectionAdapter(SYMCText text, String strItemRevType) {
            this.targetText = text;
            
            this.strItemRevType = strItemRevType;
        }

        public void widgetSelected(SelectionEvent event) {
//            // 검색 Dialog
//            SearchItemRevDialog itemDialog = new SearchItemRevDialog(getShell(), SWT.SINGLE, strItemRevType);
//            // 선택된 Revision
//            TCComponentItemRevision[] selectedItems = (TCComponentItemRevision[]) itemDialog.open();
//
//            if (selectedItems != null) {
//                // Project는 ItemID만 저장
//                if (TypeConstant.S7_PREPROJECTREVISIONTYPE.equals(this.strItemRevType)) {
//                    try {
//                        targetText.setText(selectedItems[0].getProperty(PropertyConstant.ATTR_NAME_ITEMID));
//                    } catch (TCException e) {
//                        e.printStackTrace();
//                    }
//                }
//                // ItemID/Revision Object 저장
//                else {
//                    targetText.setText(selectedItems[0].toDisplayString());
//                    targetText.setData(selectedItems[0]);
//                }
//                
//                String ospecId[] = new String[3];
//                try {
//                    ospecId = getOspecId(selectedItems[0].getProperty(PropertyConstant.ATTR_NAME_ITEMID));
//                    if (null == ospecId) {
//                        throw new Exception();
//                    }
//                    ospecText.setText(ospecId[0]);
//                    gateText.setText(ospecId[1]);
//                    projTypeCombo.setSelectedItems(ospecId[2]);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("PreCCNEPLInfoPanel.Error.MESSAGE"), "Error", MessageBox.ERROR);
//                }
//            } else {
//                ospecText.setText("");
//                gateText.setText(""); 
//                projTypeCombo.removeAllItems();
//            }
            //[20170116] Project 코드 검색 변경
        	HashMap<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("parentDialogType", "search");
			SearchProjectCodeDialog dialog = new SearchProjectCodeDialog(getShell(), SWT.NONE, dataMap);
			@SuppressWarnings("unchecked")
			HashMap<String, Object> resultDataMap = (HashMap<String, Object>) dialog.open();
			if (resultDataMap != null) {
				targetText.setText("");
				if (resultDataMap.containsKey("projectCodeList")) {
					@SuppressWarnings("unchecked")
					List<HashMap<String, Object>> projectCodeList = (List<HashMap<String, Object>>) resultDataMap.get("projectCodeList");
					for (HashMap<String, Object> projectCodeDataMap : projectCodeList) {
						String projectCode = (String) projectCodeDataMap.get("projectCode");
						targetText.setText(projectCode);
						break;
					}
				} else {
					targetText.setText((String) resultDataMap.get("projectCode"));
				}
				
                String ospecId[] = new String[3];
                try {
                    ospecId = getOspecId(targetText.getText());
                    if (null == ospecId) {
                    	targetText.setText("");
                        ospecText.setText("");
                        gateText.setText(""); 
                        throw new Exception();
                    }
                    ospecText.setText(ospecId[0]);
                    gateText.setText(ospecId[1]);
                    projTypeCombo.setSelectedItems(ospecId[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("PreCCNEPLInfoPanel.Error.MESSAGE"), "Error", MessageBox.ERROR);
                }
                
			}else
			{
                ospecText.setText("");
                gateText.setText(""); 
			}
        }

        private String[] getOspecId(String projectId) throws Exception {
            TCComponent[] tcComponents = SDVPreBOMUtilities.queryComponent("__SYMC_S7_PreProductRevision", new String[]{"Project Code"}, new String[]{projectId});
            if (null != tcComponents && tcComponents.length > 0) {
                TCComponentItemRevision productRevision = null;
                for (TCComponent tcComponent : tcComponents) {
                    if (tcComponent.getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE)) {
                        productRevision = SYMTcUtil.getLatestReleasedRevision(((TCComponentItemRevision)tcComponent).getItem());
                        break;
                    }
                }
                String[] resultArray = new String[3];
                resultArray[0] = productRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
                resultArray[1] = productRevision.getProperty(PropertyConstant.ATTR_NAME_GATENO);
                resultArray[2] = productRevision.getProperty(PropertyConstant.ATTR_NAME_PROJECTTYPE);
                
                return resultArray;
            }
            return null;
        }
    }
    
    /**
     * Item 검색시 사용되는 Selection Adapter
     */
    class ItemSelectionAdapter extends SelectionAdapter {
        /** 검색된 Item ID값이 Setting될 Field */
        SYMCText targetText;
        /** 검색할 Item Type */
        String strItemType;

        /**
         * Selection Adapter 생성자
         * 
         * @param targetText
         *            : 검색된 Item ID값이 Setting될 Field
         * @param strItemRevType
         *            : 검색할 Item Type
         */
        ItemSelectionAdapter(SYMCText targetText, String strItemType) {
            this.targetText = targetText;
            this.strItemType = strItemType;
        }

        public void widgetSelected(SelectionEvent event) {

            SearchItemDialog itemDialog = new SearchItemDialog(getShell(), SWT.SINGLE, strItemType);
            TCComponentItem[] selectedItems = (TCComponentItem[]) itemDialog.open();

            if (selectedItems != null) {
                try {
                    targetText.setText(selectedItems[0].getProperty(PropertyConstant.ATTR_NAME_ITEMID));
                    targetText.setData(selectedItems[0]);
                } catch (TCException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 결재 버튼을 가져옴
     * @return
     */
    public Button getApprovalButton()
    {
    	return approveButton;
    }
    
//    public Button getApprovalButtonOld()
//    {
//    	return approveButton_old;
//    }
}
