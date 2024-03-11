package com.ssangyong.commands.ec.eco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.assign.AskMyAssignDialog;
import com.ssangyong.commands.ec.assign.SearchMyAssignDialog;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.eco.admincheck.common.ECOAdminCheckConstants;
import com.ssangyong.commands.ec.eco.admincheck.dialog.ECOAdminCheckDialog;
import com.ssangyong.commands.ec.search.ECOSearchDialog;
import com.ssangyong.commands.ec.search.FileAttachmentComposite;
import com.ssangyong.commands.ec.search.SearchECIDialog;
import com.ssangyong.commands.ec.search.SearchECRDialog;
import com.ssangyong.commands.ec.search.SearchUserDialog;
import com.ssangyong.commands.ec.workflow.ECOProcessCommand;
import com.ssangyong.commands.workflow.SYMCDecisionDialog;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.SYMCDateTimeButton;
import com.ssangyong.common.SYMCYesNoRadio;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.dto.ApprovalLineData;
import com.ssangyong.rac.kernel.SYMCECODwgData;
import com.ssangyong.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.services.NavigatorOpenService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;
import com.teamcenter.soa.client.model.Property;

/**
 * ECO Revision 선택시, View에 보여지는 화면 렌더링
 * @author slobbie
 * [SR140722-022][20140522] swyoon Module BOM Check 버튼 추가 및 검증 후, 결재 버튼 활성화.
 * [SR140701-022][20140902] jclee ECI 정보 저장 방식 수정. Relation -> String Array
 * [SR141027-030][20141111] jclee 결재완료된 ECO 클릭한 상태에서 ECO 생성 시 결재선 편집이 되지 않던 문제 해결
 * [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선 
 * [20161221] CM ECO일 경우 Reference Department Task 및 원가기획팀 결재선 추가 불가하도록 함
 * [20170105] CM ECO 일 경우 Module Validate를 하지 않음
 * [20170206] 결재선 추가 SearchUserDialog 호출시 Task 명을 넘겨주도록 변경
 * [CF-2627][20211208] 설계 담당자 3D Saving Dataset 기능 추가
 * [20240227][UPGRADE] 업그레이드 이후 생성기능 오류 수정
 * [20240228][UPGRADE] 아무것도 선택하지 않았을 경우, 선택한 항목이 없어서 오류나서 실행되지 않음. 수정함
 * [20240306][UPGRADE] Create Workflow 버튼 사이즈 수정
 */
public class ECOSWTRendering extends AbstractSYMCViewer{
	
	private Composite layoutComposite;
	private Registry registry;
	private TCSession session;
	
	private String[] ecoInfoProperties;
	private Text item_id, project, relatedECR, object_desc, affected_project;
	private String eco_kind;
	private SWTComboBox eco_type, change_reason;
	private SWTComboBox plant_code;
	private Button ecoNoGenBtn, effect_point;
	private Text effect_point_date;
	
	private Table approvalLineTable, conCurrentECOTable, relatedECITable;
	private Button loadApprovalLineButton, saveApprovalLineButton, addApprovalLineButton, deleteApprovalLineButton, addConCurrentECOButton, deleteConCurrentECOButton, addRelatedECIButton, deleteRelatedECIButton;
	private String[] approvalLine, relatedECI, conCurrentECO;
	private int[] approvalLineSize, relatedECISize, conCurrentECOSize;
	private String[] designProperties;
	/** 결재 타스크 */
	private SWTComboBox taskCombo;
	
	private Text design_concept_no, design_related_doc, description, env_law_desc, se_related_doc, veh_dvp_result_desc;
	private SWTComboBox design_reg_catg, design_verify;
	private Button adr_yn, canada_yn, china_yn, japan_yn, dom_yn, ecc_yn, ece_yn, gcc_yn, fmvss_yn, others_yn, dr_yn;
	
	private Button requestApproval, requestApprovalTop;
	private Button moduleBOMvalidBtn;
	
	private SYMCYesNoRadio env_law_yn, weight_chg_yn, cost_chg_yn, material_chg_yn, recycling_yn, se_yn, veh_dvp_yn, veh_dvp_result_yn;
	private HashMap<String, Control> ecoInfoNControlMap = new HashMap<String, Control>();
	private HashMap<String, String> ecoPropertyMap = new HashMap<String, String>();
	private ArrayList<Control> madatoryControls;
	private TCComponentChangeItemRevision ecoRevision;
	private TCComponentChangeItemRevision eciRevision;
	
	private String[] taskList;
	
	private boolean isApprovalLineModified = false;
	private boolean isConCurrentECOModified = false;
	private boolean isRelatedECIModified = false;
	
	private boolean isReleased;;
	private FileAttachmentComposite fileComposite;
	private CustomECODao dao;
	private TCComponentFolder targetFolder;
	
	private ArrayList<Button> moduleCheckBtnList = null;
	private boolean isEngineeringManager = false;
	private boolean isCreate = false;

	private Button btnAdminCheck;
	private Button btnCheckSavingDataSet;
	private int savingCheckCount = 0;
	private int ecoBCount;
	
	public ECOSWTRendering(Composite parent) {
		super(parent);
		initData();
	}

	public ECOSWTRendering(Composite parent, boolean isCreate) {
		super(parent);
		initData();
		// [20240308][UPGRADE] createPanel 에서 ecoRevision 을 할당하기 때문에 주석처리
//		ecoRevision = null;
		this.isCreate = isCreate;
		
		if (isCreate) {
			moduleBOMvalidBtn.setVisible(false);
			requestApprovalTop.setVisible(false);
			btnCheckSavingDataSet.setVisible(false);
			/**
			 * [SR141027-030][2014.11.11][jclee] 결재완료된 ECO를 클릭한 상태에서 다시 ECO Create를 했을 경우 ECO 결재선 지정 버튼이 비활성화 되는 문제 해결
			 */
			addApprovalLineButton.setEnabled(true);
			deleteApprovalLineButton.setEnabled(true);
			loadApprovalLineButton.setEnabled(true);
			saveApprovalLineButton.setEnabled(true);
		}
		
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();

		if (comps.length > 0){
			TCComponent comp = (TCComponent) comps[0];
			if (comp instanceof TCComponentFolder) {
				
				TCComponentFolder folder = (TCComponentFolder) comp;
				if(!folder.getType().equals("S7_CorpOptionF")){
					targetFolder = folder;
				}
			}
		}
	}
	
	/** 결재선 추가 */
	private void addApprovalLine() {
		if(this.taskCombo.getTextField().getText().equals("")){ // TASK 선택 여부 체크
			MessageBox.post(getShell(), "Select task first!", "Information", MessageBox.INFORMATION);
			return;
		}
		//[20161221] CM ECO일 경우 Reference Department Task 추가 못하게함
		String ecoId = item_id.getText();
		if(ecoId.startsWith("CM"))
		{
			String selectedTask = taskCombo.getTextField().getText();
			if("Reference Department".startsWith(selectedTask))
			{
				MessageBox.post(getShell(), "Reference Department Task can't be included in approval line.", "Information", MessageBox.INFORMATION);
				return;
			}
		}
		
		TCComponentGroupMember addMember = null;
		//SearchUserDialog searchDialog = new SearchUserDialog(getShell());
		SearchUserDialog searchDialog = new SearchUserDialog(getShell(),"",taskCombo.getTextField().getText());
		int returnInt = searchDialog.open();
		if(returnInt == 0){
			addMember = searchDialog.getSelectedMember();
		}
		
		if (addMember != null) {
			TableItem[] approvalLineTableItems = approvalLineTable.getItems();
			String approvalLineTableItemKey = "";
			String selectedItemKey = "";
			String[] selectedItem = null;
			try {
				selectedItem = addMember.getProperties(new String[]{"group", "the_user", "fnd0objectId"});
			} catch (TCException e) {
				e.printStackTrace();
			}
			//[20161221] CM ECO일 경우 원가기획팀은 결재선에 추가할 수 없게함
			if(ecoId.startsWith("CM") && selectedItem[0].startsWith("ENGINEERING COST"))
			{
				MessageBox.post(getShell(), "ENGINEERING COST Team can't be included in approval line.", "Information", MessageBox.INFORMATION);
				return;
			}
			// 중복 입력 방지
			for(TableItem approvalLineTableItem : approvalLineTableItems){
				approvalLineTableItemKey = approvalLineTableItem.getText(0)+approvalLineTableItem.getData("puid");
				selectedItemKey = taskCombo.getTextField().getText()+selectedItem[2];
				if(approvalLineTableItemKey.equals(selectedItemKey))				
					return;
			}
			
			// 순서 정렬
			int selectPosition = 0;
			boolean isNotAdded = true;
			for(String task : taskList){
				if(taskCombo.getTextField().getText().equals(task)){
					break;
				}
				selectPosition++;
			}
			
			String[] selectInfo = new String[4];
			selectInfo[0] = taskCombo.getTextField().getText();
			selectInfo[1] = selectedItem[0];
			selectInfo[2] = selectedItem[1];
			selectInfo[3] = selectedItem[2];
			
			ArrayList<String[]> approvalLineInfo = new ArrayList<String[]>();
			
			for(TableItem approvalLineTableItem : approvalLineTableItems){
				String[] lineInfo = new String[4];
				lineInfo[0] = approvalLineTableItem.getText(0);
				lineInfo[1] = approvalLineTableItem.getText(1);
				lineInfo[2] = approvalLineTableItem.getText(2);
				lineInfo[3] = (String) approvalLineTableItem.getData("puid");
				if(isNotAdded){
					for(int i = 0 ; i < taskList.length ; i++){
						if(lineInfo[0].equals(taskList[i])){
							if(selectPosition < i){
								approvalLineInfo.add(selectInfo);
								isNotAdded  = false;
							}
						}
					}
				}
				approvalLineInfo.add(lineInfo);
			}
			
			if(isNotAdded){
				approvalLineInfo.add(selectInfo);
				isNotAdded  = false;
			}
			
			if(approvalLineInfo.size() < 1){ // 초기 등록
				TableItem item = new TableItem(approvalLineTable, SWT.NONE);
				item.setText(0, selectInfo[0]);
				item.setText(1, selectInfo[1]);
				item.setText(2, selectInfo[2]);
				item.setData("puid", selectInfo[3]);
			}else{
				approvalLineTable.removeAll();
				for(String[] lineInfo : approvalLineInfo){
					TableItem item = new TableItem(approvalLineTable, SWT.NONE);
					item.setText(0, lineInfo[0]);
					item.setText(1, lineInfo[1]);
					item.setText(2, lineInfo[2]);
					item.setData("puid", lineInfo[3]);
				}
			}
			isApprovalLineModified = true;
		}
	}
	
	/** 동시 설변 추가 */
	private void addConCurrentECO(){

	    try {
	    	/**
	    	 * [SR141010-011][jclee][2014.10.24] Complete된 ECO 검색 가능하도록 수정.
	    	 */
//            ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(getShell(), SWT.SINGLE);
            ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(getShell(), SWT.SINGLE, false, true);
            ecoSearchDialog.open();
            TCComponentItemRevision[] ecos = ecoSearchDialog.getSelectctedECO();
            if(ecos != null) {
            	/**
        		 * [CF-3607][20230217]ECO 동시 적용 ECO입력 내용 25자이하로 제한
        		 * ECO가 결재 완료 되면 후속시스템(PDQR, QMS)으로 인터페이스 하는데 후속시스템 테이블의 동시 적용 ECO 컬럼 사이즈가 25로 제한되어 팀센터에서 동시 적용 ECO를 추가 할때 25자까지만 입력 하도록 제한 
        		 * Table Name : IF_USER.IF_LEC_ECO
        		 * Table Column Name : SYNC_APP 
        		 */
                if(!checkConCurrentECO(ecos[0].getProperty("item_id"))){
                	return;
                }
            	TableItem[] conCurrentECOs = conCurrentECOTable.getItems();
                String conCurrentECOKey = "";
                String selectedItemKey = "";
                for(TableItem conCurrentECO : conCurrentECOs){ // 중복 입력 방지 체크
                    conCurrentECOKey = conCurrentECO.getText(0);
                    selectedItemKey = ecos[0].getProperty("item_id");
                    if(conCurrentECOKey.equals(selectedItemKey)) {
                        return;
                    }
                }
                String[] wantProperties = new String[]{"item_id", "object_desc", "owning_user", "owning_group", "creation_date"};
                String[] properties = ecos[0].getProperties(wantProperties);
                TableItem item = new TableItem(conCurrentECOTable, SWT.NONE);
                item.setText(properties);
                item.setData("tcComponent", ecos[0]);
                isConCurrentECOModified = true;                
            }            
        } catch(TCException te) {
           te.printStackTrace();
        }
	}
	
	/**
	 * [SR140701-022][20140902] jclee
	 * 관련 ECI 추가 */
	private void addConRelatedECI(){
		SearchECIDialog popupDialog = new SearchECIDialog(getShell());
		int returnInt = popupDialog.open();
		
//		TCComponentItemRevision selectedEciRevision = null;
		String sSelectedECI = "";
		
		if(returnInt == 0){
//			selectedEciRevision = popupDialog.getSelectedEciRevision();
			sSelectedECI = popupDialog.getSelectedECINo();
		}
//		if(selectedEciRevision != null){
		if(!sSelectedECI.isEmpty()){
			TableItem[] eciList = relatedECITable.getItems();
			String relatedECIKey = "";
			String selectedItemKey = "";
			String[] selectedItem = null;
			HashMap<String, String> hmSelectedItem = null;
			
			try {
				// [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선 
				hmSelectedItem = dao.searchECIEAI(sSelectedECI);
				//hmSelectedItem = dao.searchECI(sSelectedECI);
				selectedItem = new String[5];
				selectedItem[0] = hmSelectedItem.get("ECINO");
				selectedItem[1] = hmSelectedItem.get("TITLE");
				selectedItem[2] = hmSelectedItem.get("CUSER");
				selectedItem[3] = hmSelectedItem.get("CTEAM");
				selectedItem[4] = hmSelectedItem.get("CDATE");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			for(TableItem eci : eciList){ // 중복 입력 방지 체크
				relatedECIKey = eci.getText(0);
				selectedItemKey = selectedItem[0];
				if(relatedECIKey.equals(selectedItemKey))
					return;
			}
			TableItem item = new TableItem(relatedECITable, SWT.NONE);
			item.setText(selectedItem);
			item.setData("String[5]", selectedItem);
			
			isRelatedECIModified = true;
		}
	}
	
	/** ECO 생성 */
	public void create(){
		session.setStatus("Create ECO...");
		String message = validationForSave();
		if(message.equals("") || message == null){
			TCComponentItemType itemType;
			Markpoint mp = null;
			try {
				mp = new Markpoint(session);
				String ecoID = item_id.getText();
				 TCComponentItem searchItem = CustomUtil.findItem(SYMCECConstant.ECOTYPE, ecoID); //작성중 다른 사용자가 동일 아이디로 등록 했을 때를 방지
				 if(searchItem != null){
					 String oldID = ecoID;
					 String ecoPrefix = ecoID.substring(0, ecoID.length()-3);
					 ecoID = dao.getNextECOSerial(ecoPrefix);
					 setItem_id(ecoID);
					 MessageBox.post(getShell(), oldID+ " is already created.\n"+ecoID+" is a new number.", "Information", MessageBox.INFORMATION);
				 }
				 
				//[20240227][UPGRADE] 업그레이드 이후 생성기능 오류 수정				 
				//itemType = (TCComponentItemType)session.getTypeComponent("EngChange");
				//TCComponentItem item = itemType.create(ecoID, SYMCClass.ITEM_REV_ID, SYMCECConstant.ECOTYPE, ecoID, object_desc.getText(), null);
				 
				String ecoDescription = object_desc.getText();
				ecoDescription = ecoDescription == null || ecoDescription.isEmpty() ?ecoID:ecoDescription;
				
				//Item Property 속성 입력
				Map<String, String> itemPropMap = new HashMap<>();
				Map<String, String> itemRevsionPropMap = new HashMap<>();
				itemPropMap.put(IPropertyName.ITEM_ID, ecoID);
				itemPropMap.put(IPropertyName.OBJECT_NAME, ecoID);
				itemPropMap.put(IPropertyName.OBJECT_DESC, ecoDescription);
				
				//Item Revision 속성 입력
				itemRevsionPropMap.put(IPropertyName.ITEM_REVISION_ID, SYMCClass.ITEM_REV_ID);
				
				//ECO 생성
				TCComponentItem item = (TCComponentItem)SYMTcUtil.createItemObject(session, SYMCECConstant.ECOTYPE, itemPropMap, itemRevsionPropMap);

				ecoRevision = (TCComponentChangeItemRevision) item.getLatestItemRevision();
				
				ecoNoGenBtn.setVisible(false);
				if(targetFolder == null){
					session.getUser().getNewStuffFolder().add("contents", item);
				}else{
					targetFolder.add("contents", item);
					targetFolder.refresh();
				}
				
			    NavigatorOpenService openService = new NavigatorOpenService();
			    openService.open(ecoRevision);
				mp.forget();
			} catch (TCException e) {
				e.printStackTrace();
				try { 
					mp.rollBack();
				} catch (TCException e1) {	
					e1.printStackTrace();
				}
				MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
			} finally {
				session.setReadyStatus();
			}
		}else{
			MessageBox.post(getShell(), message, "ERROR", MessageBox.ERROR);
		}
	}
	
	/** 화면 생성 : super에서 타는거라 생성자 처리 로직 포함 */
	@Override
	public void createPanel(Composite parent) {
		dao = new CustomECODao();
		session = CustomUtil.getTCSession();
		registry = Registry.getRegistry(this);
		madatoryControls = new ArrayList<Control>();
		ecoInfoProperties = new String[]{"item_id","s7_PLANT_CODE","s7_ECO_TYPE","s7_CHANGE_REASON"
				,"s7_REPRESENTED_PROJECT","s7_AFFECTED_PROJECT","s7_EFFECT_POINT","s7_EFFECT_POINT_DATE","s7_ECR_NO","object_desc"};
		designProperties = new String[]{"s7_DESIGN_CONCEPT_NO","design_related_doc","s7_DESIGN_CATG_DESC"
				,"s7_DESIGN_REG_CATG","s7_ADR_YN","s7_CANADA_YN","s7_CHINA_YN","s7_JAPAN_YN","s7_DOM_YN"
				,"s7_ECC_YN","s7_ECE_YN","s7_GCC_YN","s7_FMVSS_YN","s7_OTHERS_YN","s7_DR_ITEM_YN","s7_ENV_LAW_YN"
				,"s7_ENV_LAW_DESC","s7_WEIGHT_CHG_YN","s7_COST_CHG_YN","s7_MATERIAL_CHG_YN","s7_RECYCLING_YN","s7_SE_YN"
				,"s7_DESIGN_VERIFY","s7_SE_RELATED_DOC","s7_VEH_DVP_YN","s7_VEH_DVP_RESULT_YN","s7_VEH_DVP_RESULT_DESC"};
		
		/** 선택 오브젝트 확인 **/
		InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
		if(comp != null && comp instanceof TCComponentChangeItemRevision){
			TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) comp;
			if(changeRevision.getType().startsWith(SYMCECConstant.ECOTYPE)){
				ecoRevision = changeRevision;
			}
			if(changeRevision.getType().startsWith(SYMCECConstant.ECITYPE)){
				eciRevision = changeRevision;
			}
		}
		
		/** 결재 여부 **/
		if(ecoRevision != null){
			String[] processProps = null;
			try {
				processProps = ecoRevision.getProperties(new String[]{"date_released", "process_stage_list"});
			} catch (TCException e) {
				e.printStackTrace();
			}
			if(processProps[0].equals("") && processProps[1].equals("")){
			}else{
				isReleased = true;
			}
		}
		moduleCheckBtnList = new ArrayList<Button>();

		GridLayout gridLayout = new GridLayout();
		parent.setLayout(gridLayout);
		
		layoutComposite = new Composite (parent, SWT.BORDER);
		gridLayout = new GridLayout();
		layoutComposite.setLayout(gridLayout);
		layoutComposite.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true));
		
		// [SR150417-011][20150909][jclee] Admin Check 기능 추가
		try {
			addAdminCheckBtn();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		createECOInfoPanel();
		createWorkflowPanel();
		createRelatedECIPanel();
		createConcurrentECOPanel();
		createDesignPanel();
		createFilePanel();
		if(!isReleased && ecoRevision != null)
			createRequestButtonPanel();
		
		//BIP, BIW, FCM, RCM관련 ECO일때만.
		if( requestApproval != null && requestApproval.isVisible()){
			if( moduleBOMvalidBtn.getVisible()){
				requestApproval.setEnabled(false);
				requestApprovalTop.setEnabled(false);
			}else{
				if (!getIsEngineeringManager()) {
					requestApproval.setEnabled(true);
					requestApprovalTop.setEnabled(true);
				}
			}			
		}	
		
		moduleBOMvalidBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				
				moduleCheckBtnList = new ArrayList<Button>();
				if( requestApprovalTop != null && requestApprovalTop.isVisible()){
					requestApprovalTop.setEnabled(false);
					if( !moduleCheckBtnList.contains(requestApprovalTop)){
						moduleCheckBtnList.add(requestApprovalTop);
					}
				}
				
				if( requestApproval != null && requestApproval.isVisible()){
					requestApproval.setEnabled(false);
					if( !moduleCheckBtnList.contains(requestApproval)){
						moduleCheckBtnList.add(requestApproval);
					}
				}
				
				ModuleBomValidationDlg dlg = null;
				try {
					dlg = new ModuleBomValidationDlg(item_id.getText(), moduleBOMvalidBtn, moduleCheckBtnList);
				} catch (Exception e1) {
					MessageBox.post(getShell(), "Module BOM Validation Fail!\n다시 실행 하거나 관리자에게 문의하여 주십시오.\n"+e1.getMessage(), "ERROR", MessageBox.ERROR);
					
				    if(dlg != null){
				    	dlg.dispose();
				    }
				}
			}
		});			
	}

    /**
     * [SR150417-011][20150904][jclee]
     * Admin Check
     */
    private void addAdminCheckBtn() throws Exception {
    	if (isCreate) {
			return;
		}
    	
    	// 접속자가 BOM Admin이어야만 Button이 보이도록 설정.
    	boolean isBOMAdmin = false;
    	TCComponentUser usrCurrent = session.getUser();
    	TCComponentGroup[] groups = usrCurrent.getGroups();
    	for (int inx = 0; inx < groups.length; inx++) {
    		TCComponentRole[] roles = usrCurrent.getRoles(groups[inx]);
    		for (int jnx = 0; jnx < roles.length; jnx++) {
				String sRole = roles[jnx].toDisplayString();
				
				if ("BOMADMIN".equals(sRole)) {
					isBOMAdmin = true;
					break;
				}
			}
		}
    	
    	if(usrCurrent.toDisplayString().contains("infodba")){
    		isBOMAdmin = true;
    	}
	    	GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
	    	gd.horizontalAlignment = SWT.RIGHT;

    	if (!isBOMAdmin) {
			return;
		}
    	
    	Composite cpsAdminCheck = new Composite(layoutComposite, SWT.NONE);
    	cpsAdminCheck.setLayout(new GridLayout(1, true));
    	if(isBOMAdmin){
    		cpsAdminCheck.setLayout(new GridLayout(1, true)); 	
    	}
    	cpsAdminCheck.setBackground(new Color(null, 255, 255, 255));
    	cpsAdminCheck.setLayoutData(gd);
    	
		btnAdminCheck = new Button(cpsAdminCheck, SWT.PUSH);
		btnAdminCheck.setText("Admin Check");
		btnAdminCheck.setLayoutData(new GridData(120, 30));
		btnAdminCheck.setEnabled(true);
		btnAdminCheck.setVisible(true);
		setControlSkipEnable(btnAdminCheck, true);
		btnAdminCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				adminCheck();
			}
		});
    }

    public void checkSavingDataSet(){
		final String checkECONo = item_id.getText();
		final String pname = "ECO ["+ checkECONo +"]" + "3D Saving Check.";
		
    	final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
		waitProgress.setWindowSize(700, 500);
		waitProgress.setShowButton(true);
		
		waitProgress.start();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try{				
					ArrayList<String> doubleCheck = null;
			    	StringBuffer unSavedList = new StringBuffer();
					StringBuffer catDoubleCheck = new StringBuffer();
					StringBuffer changeRelation = new StringBuffer();

					int tCount = 0;

					TCComponent[] partRevisionList = CustomUtil.queryComponent("SYMC_Search_ItemRevision_InECO", new String[]{"item_id"}, new String[]{checkECONo });		
					ArrayList<String> compIDs = new ArrayList<String>();
					ArrayList<TCComponent> sortList = new ArrayList<TCComponent>();
					for(TCComponent comp : partRevisionList){
						compIDs.add(comp.getProperty("item_id"));
					}
					Collections.sort(compIDs);
					for(String compID : compIDs){
						for(TCComponent solutionItem : partRevisionList){
							if(solutionItem.getProperty("item_id").equals(compID)){
								sortList.add(solutionItem);
								continue;
							}
						}
					}
					
					if(sortList != null && sortList.size() > 0){
						waitProgress.setStatus( pname + " Start...");
						for(TCComponent partRevision :  sortList){
							tCount++;
							String partID = partRevision.getProperty("item_id") + "/" + partRevision.getProperty("item_revision_id");
							AIFComponentContext[] contexts =  partRevision.getChildren();
							if(contexts != null && contexts.length > 0 ){
								doubleCheck = new ArrayList<String>();
								for(AIFComponentContext context : contexts){
									String dataSetType = context.getComponent().getType();
									if(dataSetType.equals("CATDrawing") ||dataSetType.equals("CATProduct") ||dataSetType.equals("CATPart")){
										TCComponentDataset dataSet = (TCComponentDataset)context.getComponent();
										String releaseStatus = dataSet.getProperty("date_released");
										String relation = context.getContextDisplayName().toString();							
										String dataSetName = dataSet.getProperty("object_name");
										String orginName = null;
										String orginFileSize = null;
										String tcFormName = null;
										TCComponent[] namedRefs = null;
										TCComponent namedRef = null;
										TCComponent catiaDocAttributes = null;
										TCComponentForm tcForm = null;
										try{
											namedRefs = dataSet.getNamedReferences();
											for(TCComponent namedRefd : namedRefs){
												if(namedRefd.getType().equals("ImanFile")){
													namedRef = namedRefd;
													TCComponentTcFile namedRefTcFile = (TCComponentTcFile) namedRefd;
													orginName = namedRefTcFile.getProperty("original_file_name");
													orginFileSize = namedRefTcFile.getProperty("file_size");
												}
											}
											catiaDocAttributes = dataSet.getNamedRefComponent("catia_doc_attributes");
											tcForm = (TCComponentForm) catiaDocAttributes;
											if(tcForm != null){
												tcFormName = tcForm.getProperty("object_name");
											}
										} catch (Exception e){
											orginFileSize = "0";
											orginName = "The referenced file does not exist.";
										}
										if(!releaseStatus.equals("")){
											continue;
										}
										int orginNameGapSize = 0;
										int datasetNameGapSize = 0;		
										String datasetNameLength = null;
										String orginNameLength = null;
										String datasetNm = null;
										String orginNm = null;
										try{
											orginNameGapSize = 54 - orginName.length();
											datasetNameGapSize = 35-dataSetName.length();
											datasetNameLength = String.valueOf(datasetNameGapSize);
											orginNameLength = String.valueOf(orginNameGapSize);
											datasetNm = String.format("%-"+datasetNameLength+"s", dataSetName);
											orginNm = String.format("%-"+orginNameLength+"s", orginName);
										}catch(Exception e){
											System.out.println(e);
											datasetNm = dataSetName;
											orginNm = orginName;
										}
											String resultCheck = "Saved";
											if(namedRef == null){
												resultCheck = "Unsaved";
												unSavedList.append("    " +datasetNm + "  |  " + orginNm + " | " + resultCheck + "\r\n");
												waitProgress.setStatus("    " + datasetNm + "  |  " +   orginNm + "  |  " + resultCheck + ", (" + orginFileSize + ")" + "  |  Relation : "+relation);
												continue;
											}
											String tdatasetname = dataSetName.replace("/", "_");
											if(tdatasetname.indexOf("-") > 0){
												tdatasetname = tdatasetname.substring(0, tdatasetname.indexOf("-"));
											}
											
											if(orginName == null || orginName.equals("") || orginFileSize.equals("0")){
												resultCheck = "Unsaved";
												unSavedList.append("    " +datasetNm + "  |  " + orginNm  + " | "  + resultCheck + "\r\n");
											} 
											
											if(!orginName.startsWith(tdatasetname)){
												resultCheck = "Unsaved";
												unSavedList.append("    " +datasetNm + "  |  " + orginNm  + " | " + "Dataset 네임과 실제 파일 네임이 일치하지 않습니다."  + " | " + resultCheck + "\r\n");												
											}
											/* 
											 * 20221018 기존에 카티아 네임과 파일 사이즈로 카티아 저장여부를 판단 하였는데 catia_doc_attributes이 없는 데이터셋은 저장이 안되었어도 저장여부를 판단 할 수 없어서 추가 하였음
											 * 추가 로직
											 * 1. 데이터셋에 catia_doc_attributes이 없는경우 저장 안한것으로 판단
											 * 2. 데이터셋에 catia_doc_attributes이 있는데 데이터셋과 Name이 다른경우 저장 안한것으로 판단
											 */
											if(tcFormName != null){
												if(!dataSetName.equals(tcFormName)){
													resultCheck = "Unsaved";
													unSavedList.append("    " +datasetNm + "  |  " + tcFormName  + "                      | " + "Dataset 네임과 CATIA 속성 Form 네임이 일치하지 않습니다."  + " | " + resultCheck + "\r\n");												
												}
											}

											if(tcForm == null){
												resultCheck = "Unsaved";
												unSavedList.append("    " +datasetNm + "  |  " + orginNm  + " | "  + resultCheck + "\r\n");
											} 
											
											if(!relation.equals("Specifications")){
				//								String dataSetRev = dataSet.getProperty("object_name").substring(dataSet.getProperty("object_name").indexOf("/")+1);
												String partRevID = partRevision.getProperty("item_revision_id");
												if(releaseStatus.equals("") && relation.equals("References")){
													TCComponent[] dataSetList = {dataSet}; 
													partRevision.changeRelation("IMAN_reference", "IMAN_specification", dataSetList);
													changeRelation.append("    " +datasetNm + " Change Relation : References -> Specifications \r\n");
												}
											}
											
											
											waitProgress.setStatus("    " + datasetNm + "  |  " +  orginNm + "  |  " + resultCheck + ", (" + orginFileSize + ")" + "  |  Relation : "+relation);
			//							}
									}
								}
							}else{
								waitProgress.setStatus( partID + " Dataset null...");
								continue;
							}
							if(doubleCheck.contains("CATProduct") && doubleCheck.contains("CATPart")){
								catDoubleCheck.append("    " +partID  + " CATProduct, CATPart exist together... \r\n");
								doubleCheck.clear();
							}
						}
					}else {
						waitProgress.setStatus(pname + " Start...");
						waitProgress.setStatus("    Target does not exist.");
					}
					
					waitProgress.setStatus(pname + " End...");
					
					
					if(tCount == 0){
						waitProgress.close("Target does not exist.",false,false);
					} 
		//			이상없는 것들 OK처리 요청 사항으로 주석			
		//			else if((unSavedList == null || unSavedList.toString().equals("")) && (zeroSizeList == null || zeroSizeList.toString().equals(""))){
		//				waitProgress.setStatus("");
		//				if(changeRelation != null && !changeRelation.toString().equals("")){
		//					waitProgress.setStatus("Relation Change References -> Specifications List Start...");
		//					waitProgress.setStatus(changeRelation.toString(), false);
		//					waitProgress.setStatus("Relation Change References -> Specifications List End...\r\n");
		//				}
		//				if(catDoubleCheck != null && !catDoubleCheck.toString().equals("")){
		//					waitProgress.setStatus("Dataset Double Check List Start...");
		//					waitProgress.setStatus(catDoubleCheck.toString(), false);
		//					waitProgress.setStatus("Dataset Double Check List End...\r\n");
		//				}
		//				waitProgress.close("All datasets have been saved.",false,false);
		//			}
					else {
						waitProgress.setStatus("");
						waitProgress.setStatus("====================================================================================");
						waitProgress.setStatus("[Summary]");
						
						if(changeRelation != null && !changeRelation.toString().equals("")){
							waitProgress.setStatus("Relation Change List Start...");
							waitProgress.setStatus(changeRelation.toString(), false);
							waitProgress.setStatus("Relation Change List End...\r\n");
						}else if(changeRelation.length() == 0){
							waitProgress.setStatus("Relation Check : OK \r\n");
						}
						if(catDoubleCheck != null && !catDoubleCheck.toString().equals("")){
							waitProgress.setStatus("Dataset Double Check List Start...");
							waitProgress.setStatus(catDoubleCheck.toString(), false);
							waitProgress.setStatus("Dataset Double Check List End...\r\n");
						}else if(catDoubleCheck.length() == 0){
							waitProgress.setStatus("Dataset Double Check : OK \r\n");
						}
						if(unSavedList != null && !unSavedList.toString().equals("")){
							waitProgress.setStatus("Dataset Unsaved List Start...■[2D/3D 데이터 저장 후 결재 진행해 주시기 바랍니다.]■");
							waitProgress.setStatus(unSavedList.toString(), false);
							waitProgress.setStatus("Dataset Unsaved List End... \r\n");
						}else if(unSavedList.length() == 0){
							waitProgress.setStatus("Dataset Unsaved Check : OK \r\n");
						}
		
						
						if(unSavedList.length() != 0){
							waitProgress.close("Dataset Unsaved or Exist Zero Size",true,false);
						}else{
							waitProgress.close("All datasets have been saved.",false,false);
						}
						savingCheckCount = 1;
					}
				} catch (Exception e){
					e.printStackTrace();
					waitProgress.setStatus(e.getMessage() + "Error 관리자에게 문의하여 주십시오.");
			//			waitProgress.setShowButton(true);
					waitProgress.close("Error",true,false);
				}
			}
		}).start();
    }
    

    /**
	 * [SR150417-011][20150904][jclee]
	 * Admin Check
	 */
	public void adminCheck() {
		try {
			// Admin Check Dialog Open
			String sECONo = "";
			// [20240311][UPGRADE] ECO Revision 이 존재하는 경우 체크
			if (ecoRevision != null)
				sECONo = ecoRevision.getProperty("item_id");
			
			if (sECONo == null || sECONo.equals("") || sECONo.length() == 0) {
				MessageBox.post(getShell(), "Invalid ECO.", "Error", MessageBox.ERROR);
				return;
			}
			
			ECOAdminCheckDialog dialog = new ECOAdminCheckDialog(getShell(), SWT.DIALOG_TRIM | SWT.MIN, ecoRevision);
			dialog.open();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/** ECO 정보 그룹 */
	@SuppressWarnings("rawtypes")
    public void createECOInfoPanel() {
		GridLayout gridLayout = new GridLayout (6, false);//컬럼 지정
		Group group = new Group (layoutComposite, SWT.NONE);
		group.setLayout (gridLayout);
		group.setText (registry.getString("ECORendering.GROUP.ECO_INFO"));
		
		GridData data = new GridData (SWT.FILL, SWT.FILL, true, false);	// TODO
		group.setLayoutData (data);

		// # 1
		GridData lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);//Label GRID 셋팅
		lblGridData.widthHint = 100;
		Label item_id_lbl = new Label (group, SWT.RIGHT);
		item_id_lbl.setText (registry.getString("ECORendering.LABEL.item_id"));
		item_id_lbl.setLayoutData(lblGridData);
		
		GridData textGridData = new GridData (120, SWT.DEFAULT);//Text GRID 셋팅
		item_id = new Text(group, SWT.BORDER);
		item_id.setLayoutData(textGridData);
		item_id.setEnabled(false);
		setControlSkipEnable(item_id, true);
		setMadatory(item_id);
		
		textGridData = new GridData (90, SWT.DEFAULT);//Text GRID 셋팅
		textGridData.horizontalSpan = 1;
		ecoNoGenBtn = new Button(group, SWT.PUSH);
		ecoNoGenBtn.setText("Assign");
		ecoNoGenBtn.setLayoutData(textGridData);
		ecoNoGenBtn.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				createECONo();
			}
		});
		
		// FIXED 20130422 : 상/하 2곳에 상신 버튼 추가 요청
		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		lblGridData.widthHint = 150;
		Label requestApprovalTop_lbl = new Label(group, SWT.RIGHT);
		requestApprovalTop_lbl.setText("");
		requestApprovalTop_lbl.setLayoutData(lblGridData);

		Composite btnGroup = new Composite (group, SWT.NONE);
		GridData btnGD = new GridData (SWT.END, SWT.CENTER, false, false);
		btnGD.horizontalSpan = 2;
		btnGroup.setLayoutData(btnGD);
		GridLayout btnLayout = new GridLayout (3, false);
		btnLayout.marginTop = 0;
		btnLayout.marginBottom = 0;
		btnLayout.marginHeight = 0;
		btnLayout.marginLeft = -6;
		btnGroup.setLayout (btnLayout);
		
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        DataSet dss = new DataSet();
        
		try
		{
			//UPGRADE 시 개선 사항: 선
//			dss.put("ecoNo", AIFUtility.getCurrentApplication().getTargetComponent().getProperty("item_id"));
//			final ArrayList<SYMCECODwgData> ecoBdata = (ArrayList<SYMCECODwgData>) remote.execute("com.ssangyong.service.ECOHistoryService", "selectECODwgList", dss);
//			ecoBCount = ecoBdata.size();
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			ArrayList<SYMCECODwgData> ecoBdata = null;
			if(targetComponent !=null)
			{
				dss.put("ecoNo", targetComponent.getProperty("item_id"));
			    ecoBdata = (ArrayList<SYMCECODwgData>) remote.execute("com.ssangyong.service.ECOHistoryService", "selectECODwgList", dss);
			}
			ecoBCount = ecoBdata !=null? ecoBdata.size():0;
			
			textGridData = new GridData (120, SWT.DEFAULT);//Text GRID 셋팅
	    	btnCheckSavingDataSet = new Button(btnGroup, SWT.PUSH);
			btnCheckSavingDataSet.setText("3D Saving Dataset");
			btnCheckSavingDataSet.setLayoutData(textGridData);
			btnCheckSavingDataSet.setEnabled(true);
			if(isReleased && !isReApproval()){
				btnCheckSavingDataSet.setVisible(false);
			}else{
				if (ecoBdata == null || ecoBdata.size() == 0) {
					btnCheckSavingDataSet.setVisible(false);    
		        }else{
		        	btnCheckSavingDataSet.setVisible(true);
		        }
			}
			setControlSkipEnable(btnCheckSavingDataSet, true);
			btnCheckSavingDataSet.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					checkSavingDataSet();
				}
			});
		} catch (Exception e2)
		{
			e2.printStackTrace();
		}
		
		textGridData = new GridData (106, SWT.DEFAULT);//Text GRID 셋팅
		moduleBOMvalidBtn = new Button(btnGroup, SWT.PUSH);
		moduleBOMvalidBtn.setText("Module BOM Val.");
		moduleBOMvalidBtn.setLayoutData(textGridData);
		setControlSkipEnable(moduleBOMvalidBtn, true);
		
		if( ecoRevision != null){
			try {
				Property prop = ecoRevision.getPropertyObject("release_status_list");
				if( prop.getDisplayableValue() != null){
					moduleBOMvalidBtn.setVisible(false);
				}else{
					
					//ECO_EPL에서 BIP, BIW, FCM, RCM 관련 Part가 존재하는 경우에만 버튼 활성화.
					//위 경우가 아니면 상신 버튼 바로 활성화
//					String WAS_URL = "http://127.0.0.1:8080/ssangyongweb/HomeServlet";
//					SYMCRemoteUtil remote = new SYMCRemoteUtil();
					
					try{
						//[20170105] CM ECO 일 경우 Module Validate 를 하지 않음
						boolean isCMECO = ecoRevision.getProperty("item_id").startsWith("CM")?true:false;
						DataSet ds = new DataSet();
						ds.put("eco_no", ecoRevision.getProperty("item_id"));
						ArrayList result = (ArrayList)remote.execute("com.ssangyong.service.ModuleBomValidationService", "getModulePart", ds);
						if( result != null && !result.isEmpty() && isCMECO){
							moduleBOMvalidBtn.setVisible(true);
						}else{
							moduleBOMvalidBtn.setVisible(false);
						}
					}catch( Exception e){
						throw e;
					}						
					
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				moduleBOMvalidBtn.setVisible(false);
			}
		}else{
			moduleBOMvalidBtn.setVisible(false);
		}
		
		
		String sButtonLabel = !isReApproval() ? "Create Workflow" : "Re Approval Req.";
		
//		textGridData.horizontalSpan = 2;
		//[20240306][UPGRADE] 버튼 사이즈 수정
		//textGridData = new GridData (100, SWT.DEFAULT);//Text GRID 셋팅
		textGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		requestApprovalTop = new Button(btnGroup, SWT.PUSH);
		requestApprovalTop.setText(sButtonLabel);
		requestApprovalTop.setLayoutData(textGridData);
		setControlSkipEnable(requestApprovalTop, true);
		requestApprovalTop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(savingCheckCount != 1 && btnCheckSavingDataSet.getVisible()){
					MessageBox.post(AIFDesktop.getActiveDesktop(), "3D Saving DataSet 실행 후 Create Workflow 실행해 주시기 바랍니다.", "WARNING", MessageBox.WARNING);
					return;
				}
				/**
				 * [CF-3607][20230217]ECO 동시 적용 ECO입력 내용 25자이하로 제한
				 * ECO가 결재 완료 되면 후속시스템(PDQR, QMS)으로 인터페이스 하는데 후속시스템 테이블의 동시 적용 ECO 컬럼 사이즈가 25로 제한되어 팀센터에서 동시 적용 ECO를 추가 할때 3개까지만 입력 하도록 제한 
				 * Table Name : IF_USER.IF_LEC_ECO
        		 * Table Column Name : SYNC_APP
				 */
				if(!checkConCurrentECO("")){
					return;
				}
				try{
					if (!isReApproval()) {
						new ECOProcessCommand(ecoRevision);
					} else {
						reApproval();
					}
				} catch(Exception e1){
					e1.printStackTrace();
				}
			}
		});
		if((isReleased && !isReApproval()) || ecoRevision == null){
			requestApprovalTop.setVisible(false);
		}
		
		if( requestApprovalTop != null && requestApprovalTop.isVisible()){
			if( moduleBOMvalidBtn.getVisible()){
				requestApprovalTop.setEnabled(false);
			}else{
				requestApprovalTop.setEnabled(true);
			}
		}
		// # 2
		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		lblGridData.widthHint = 100;
		Label plant_code_lbl = new Label(group, SWT.RIGHT);
		plant_code_lbl.setText(registry.getString("ECOInfoPanel.LABEL.PlantCode"));
		plant_code_lbl.setLayoutData(lblGridData);
		
		GridData comboGridData = new GridData (220, SWT.DEFAULT);
		comboGridData.horizontalSpan = 2;
		//[20240228][UPGRADE] Plant Code Multi 선택 안되도록함 
		//plant_code = new SWTComboBox(group, SWT.BORDER | SWT.MULTI);
		plant_code = new SWTComboBox(group, SWT.BORDER | SWT.MULTI);
		
		comboValueSetting(plant_code, "S7_PLANT_CODE");
		plant_code.setLayoutData(comboGridData);
		setMadatory(plant_code);

		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		lblGridData.widthHint = 80;
		Label eco_type_lbl = new Label(group, SWT.RIGHT);
		eco_type_lbl.setText(registry.getString("ECORendering.LABEL.eco_type"));
		eco_type_lbl.setLayoutData(lblGridData);
		
		comboGridData = new GridData (205, SWT.DEFAULT);
//		comboGridData = new GridData (GridData.FILL_HORIZONTAL);
		comboGridData.horizontalSpan = 2;
		eco_type = new SWTComboBox(group, SWT.BORDER);
		comboValueSetting(eco_type, "S7_ECO_TYPE");
		eco_type.setLayoutData(comboGridData);
		setMadatory(eco_type);
		
		// # 3
		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		lblGridData.widthHint = 100;
		Label change_reason_lbl = new Label(group, SWT.RIGHT);
		change_reason_lbl.setText(registry.getString("ECORendering.LABEL.change_reason"));
		change_reason_lbl.setLayoutData(lblGridData);
		
		comboGridData = new GridData (220, SWT.DEFAULT);
		comboGridData.horizontalSpan = 2;
		change_reason = new SWTComboBox(group, SWT.BORDER);
		comboValueSetting(change_reason, "S7_ECO_REASON");
		change_reason.setLayoutData(comboGridData);
		setMadatory(change_reason);

		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		lblGridData.widthHint = 150;
		Label project_lbl = new Label(group, SWT.RIGHT);
		project_lbl.setText(registry.getString("ECOInfoPanel.LABEL.Representation"));
		project_lbl.setLayoutData(lblGridData);
		
		textGridData = new GridData (200, SWT.DEFAULT);//Text GRID 셋팅
//		textGridData = new GridData (GridData.FILL_HORIZONTAL);
		textGridData.horizontalSpan = 2;
		project = new Text(group, SWT.BORDER);
		project.setLayoutData(textGridData);
		project.setEnabled(false);
		setControlSkipEnable(project, true);
		setMadatory(project);
		
		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		Label effect_point_lbl = new Label(group, SWT.RIGHT);
		effect_point_lbl.setText(registry.getString("ECORendering.LABEL.effect_point"));
		effect_point_lbl.setLayoutData(lblGridData);
		
		GridLayout igridLayout = new GridLayout(2, false);
		igridLayout.marginWidth = 0;
		igridLayout.marginHeight = 0;
		textGridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		textGridData.horizontalSpan = 2;
		Composite effectComposite = new Composite(group, SWT.NONE);
		effectComposite.setLayout(igridLayout);
		effectComposite.setLayoutData(textGridData);
		
		textGridData = new GridData (75, SWT.DEFAULT);
		effect_point = new Button (effectComposite, SWT.CHECK);
		effect_point.setText("ASAP");
		effect_point.setLayoutData(textGridData);
		effect_point.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				if(effect_point.getSelection()){
					effect_point_date.setVisible(false);
					/*
					 *[CF-4217][20230719]ECO 적용 시점 35바이트 이내 작성
					 * 오류 발생 내용 : I/F데이터 생성중 35바이트를 초과하여 에러발생 (PLM최대 입력값(40), IF_LEC_ECO 테이블 컬럼 최대 입력값(35))
					 * 수정 내용 : ASAP 체크 박스 선택시 직접 입력하는 텍스트 박스가 Visible처리 되는데 
					 * 텍스트 박스에 입력 중 체크 박스 선택시 입력한 값이 그대로 남아있어서 체크박스 선택시 입력 내용 null처리 하는 부분 추가   
					 * */
					effect_point_date.setText("");
				}else{
					effect_point_date.setVisible(true);
				}
			}
		});
		
		comboGridData = new GridData (135, SWT.DEFAULT);
		effect_point_date = new Text(effectComposite, SWT.BORDER);
		effect_point_date.setLayoutData(comboGridData);
		
		/*
		 *[CF-4217][20230719]ECO 적용 시점 35바이트 이내 작성
		 * 오류 발생 내용 : I/F데이터 생성중 35바이트를 초과하여 에러발생 (PLM최대 입력값(40), IF_LEC_ECO 테이블 컬럼 최대 입력값(35))
		 * 수정 내용 : Change Eff. Point(적용 시점) 35byte까지만 입력 되도록 리밋 설정  
		 * */
		effect_point_date.setTextLimit(35);
		effect_point_date.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent paramVerifyEvent)
			{
				String keyString = paramVerifyEvent.text;
				int costLength = effect_point_date.getText().getBytes().length + keyString.getBytes().length;
				if(costLength > 35){
					paramVerifyEvent.doit = false;
				}
			}
		});
		setMadatory(effect_point_date);
		
		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		lblGridData.widthHint = 100;
		Label relatedECR_lbl = new Label(group, SWT.RIGHT);
		relatedECR_lbl.setText(registry.getString("ECORendering.LABEL.relatedECR"));
		relatedECR_lbl.setLayoutData(lblGridData);
		
//		textGridData = new GridData (168, SWT.DEFAULT);
		textGridData = new GridData (GridData.FILL_HORIZONTAL);
		relatedECR = new Text(group, SWT.BORDER);
		relatedECR.setLayoutData(textGridData);
		relatedECR.setEnabled(false);
		
		// FIXED 20130422 : ECR 검색 입력 으로 변경
		Button searchReviewteamButton = new Button(group, SWT.NONE);
		searchReviewteamButton.setImage(registry.getImage("Search.ICON"));
		searchReviewteamButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				SearchECRDialog popupDialog = new SearchECRDialog(getShell(), SWT.NONE);
				if(popupDialog.open() != SWT.CANCEL){
					String seqNo = popupDialog.getECRSeqNo();
					if(seqNo != null) {
						relatedECR.setText(popupDialog.getECRRegNo());
					} else {
						relatedECR.setText("");
					}
				}
			}
		});

		// # 4
		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		lblGridData.widthHint = 100;
		Label object_desc_lbl = new Label(group, SWT.RIGHT);
		object_desc_lbl.setText(registry.getString("ECORendering.LABEL.object_desc"));
		object_desc_lbl.setLayoutData(lblGridData);
		
		textGridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		textGridData.horizontalSpan = 5;
		object_desc = new Text(group, SWT.BORDER);
		object_desc.setLayoutData(textGridData);
		
		// # 5
		lblGridData = new GridData (SWT.END, SWT.CENTER, false, false);
		lblGridData.widthHint = 110;
		Label affected_project_lbl = new Label(group, SWT.RIGHT);
		affected_project_lbl.setText(registry.getString("ECOInfoPanel.LABEL.Affected"));
		affected_project_lbl.setLayoutData(lblGridData);
		
		textGridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		textGridData.horizontalSpan = 5;
		affected_project = new Text(group, SWT.BORDER);
		affected_project.setLayoutData(textGridData);
		affected_project.setEnabled(false);
		
		// FIXED Affected Project를 항상 보여 줌.
		if(!isReleased){
			if(ecoRevision != null) {
				try{
					String affecedProjects = dao.getAffectedProject(ecoRevision.getProperty("item_id"));
					if(affecedProjects != null){
						String[] affProjs = affecedProjects.split(",");
						ArrayList<String> affecedProjectList = new ArrayList<String>();
						String affecedProject = "";
						for(String affProj : affProjs){
							if(!affecedProjectList.contains(affProj))
								affecedProjectList.add(affProj);
						}
						for(String affProj : affecedProjectList){
							if(affecedProject.equals("")){
								affecedProject = affProj;
							}else{
								affecedProject = affecedProject+","+affProj;
							}
						}
						affected_project.setText(affecedProject);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Workflow가 존재하면서 ECO의 상태가 In Work인 경우 true 반환
	 * @return
	 */
	private boolean isReApproval() {
		try {
			if (ecoRevision == null) {
				return false;
			}
			
			if (ecoRevision.getCurrentJob() == null) {
				return false;
			}
			
			String sMaturity = ecoRevision.getProperty("s7_ECO_MATURITY");
			if (sMaturity.equals("In Work")) {
				return true;
			}
			
		} catch (Exception e) {
			MessageBox.post(e);
		}
		return false;
	}
	
	/**
	 * Decision Dialog Open
	 */
	public void setUpDecisionDialog(AIFDesktop desktop, TCComponentTask task, TCComponentSignoff signoff) {
		SYMCDecisionDialog decisionDlg = ((SYMCDecisionDialog) this.registry.newInstanceFor("SYMCDecisionDialog", new Object[] { desktop, task, signoff }));
		decisionDlg.updateSYMCDecisionDialog();
		decisionDlg.setModal(false);
		decisionDlg.setVisible(true);
	}
	
	/**
	 * [SR151204-011][20151209][jclee] ECO 재상신
	 */
	private void reApproval() {
		try {
			TCComponentProcess currentJob = ecoRevision.getCurrentJob();
			TCComponentTask rootTask = currentJob.getRootTask();
			TCComponentTask[] subTasks = rootTask.getSubtasks();
			TCComponentTask task = null;
			TCComponentSignoff signoff = null;
			
			for(TCComponentTask subTask : subTasks) {
				if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) || subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)){
					if(subTask.getName().equals("Creator")) {
						task = subTask.getSubtask("perform-signoffs");
						TCComponentSignoff[] signoffs = task.getValidSignoffs();
						
						if (signoffs.length == 1) {
							signoff = signoffs[0];
							break;
						}
					}
				}
			}
			setUpDecisionDialog(AIFDesktop.getActiveDesktop(), task, signoff);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	/** 결재선 그룹 */
	public void createWorkflowPanel() {
		approvalLine = new String[]{"Task", "Dept", "User Name", "TEL", "Date", "Comments"};
		approvalLineSize = new int[]{220, 150, 120, 80, 150, 320};

		Group group = new Group (layoutComposite, SWT.NONE);
		group.setLayout (new GridLayout());
		group.setText (registry.getString("ECORendering.GROUP.ApprovalLine"));
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		gridData.minimumHeight = 180;

		group.setLayoutData (gridData);
		
		GridLayout flayout = new GridLayout (10, false);
		flayout.marginWidth = 0;
		flayout.marginHeight = 0;
		Composite composite = new Composite(group, SWT.NONE);
		composite.setLayout(flayout);
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		loadApprovalLineButton = new Button(composite, SWT.NONE);
		loadApprovalLineButton.setText("Open...");
		loadApprovalLineButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				loadUserApprovalLine();
			}
		});
		
		saveApprovalLineButton = new Button(composite, SWT.NONE);
		saveApprovalLineButton.setText("Save...");
		saveApprovalLineButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				saveUserApprovalLine();
			}
		});
		
		Label oppenGapLabel = new Label(composite, SWT.RIGHT);
		oppenGapLabel.setText("Select Task");
		gridData = new GridData (SWT.FILL, SWT.CENTER, true, true);
		gridData.horizontalSpan = 4; 
		oppenGapLabel.setLayoutData(gridData);
		
		taskList = SYMCECConstant.ECO_TASK_LIST;

		taskCombo = new SWTComboBox(composite, SWT.BORDER);
		taskCombo.setAutoCompleteSuggestive(false);
		taskCombo.setLayoutData(new GridData (220, SWT.DEFAULT));
		for(String task : taskList){
			taskCombo.addItem(task, task);
		}
		
		Label serchLabel = new Label(composite, SWT.RIGHT);
		gridData = new GridData (20, SWT.DEFAULT);
		serchLabel.setLayoutData(gridData);
		
		addApprovalLineButton = new Button(composite, SWT.NONE);
		addApprovalLineButton.setText("Add");
		addApprovalLineButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				addApprovalLine();
			}
		});
		
		deleteApprovalLineButton = new Button(composite, SWT.NONE);
		deleteApprovalLineButton.setText("Delete");
		deleteApprovalLineButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteApprovalLine();
			}
		});
		
		approvalLineTable = new Table(group, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		approvalLineTable.setHeaderVisible(true);
		approvalLineTable.setLinesVisible(true);
		gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 140;
		approvalLineTable.setLayoutData(gridData);
		
		int i = 0;
		for(String value : approvalLine){
			TableColumn column = new TableColumn(approvalLineTable, SWT.CENTER);
			column.setText(value);
			column.setWidth(approvalLineSize[i]);
			i++;
		}
		
		// FIXED 2013.05.21, DJKIM : Workflow 생성 되면 결재선 수정은 불가
		if(isReleased && !this.isCreate){
			addApprovalLineButton.setEnabled(false);
			deleteApprovalLineButton.setEnabled(false);
			loadApprovalLineButton.setEnabled(false);
			saveApprovalLineButton.setEnabled(false);
		}
	}

	/** 관련 설변 그룹 */
	public void createConcurrentECOPanel() {
		conCurrentECO = SYMCECConstant.ECO_CONCURRENTECO_TABLE_COLS;
		conCurrentECOSize = new int[]{150, 320, 150, 220, 200};
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		Group group = new Group (layoutComposite, SWT.NONE);
		group.setLayout (formLayout);
		group.setText (registry.getString("ECORendering.GROUP.ConcurrentImplementation"));
		
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 180;
		group.setLayoutData (gridData);
		
		//하단에 위치
		FormData formData = new FormData();
		formData.top = new FormAttachment(10, 15);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		
		conCurrentECOTable = new Table(group, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		conCurrentECOTable.setHeaderVisible(true);
		conCurrentECOTable.setLinesVisible(true);
		conCurrentECOTable.setLayoutData(formData);
		
		formData = new FormData();
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(conCurrentECOTable, -5);
		deleteConCurrentECOButton = new Button(group, SWT.NONE);
		deleteConCurrentECOButton.setText("Delete");
		deleteConCurrentECOButton.setLayoutData(formData);
		deleteConCurrentECOButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteConCurrentECO();
			}
		});
		
		formData = new FormData();
		formData.right = new FormAttachment(deleteConCurrentECOButton, -5);
		formData.bottom = new FormAttachment(conCurrentECOTable, -5);
		addConCurrentECOButton = new Button(group, SWT.NONE);
		addConCurrentECOButton.setText("Add");
		addConCurrentECOButton.setLayoutData(formData);
		addConCurrentECOButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				addConCurrentECO();
			}
		});
		
		int i = 0;
		for(String value : conCurrentECO){
			TableColumn column = new TableColumn(conCurrentECOTable, SWT.NONE);
			column.setText(value);
			column.setWidth(conCurrentECOSize[i]);
			i++;
		}
	}
	
	/** 관련 ECI 그룹 생성 */
	public void createRelatedECIPanel() {
		relatedECI = SYMCECConstant.ECI_CONCURRENTECO_TABLE_COLS;
		relatedECISize = new int[]{150, 320, 150, 220, 200};
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		Group group = new Group (layoutComposite, SWT.NONE);
		group.setLayout (formLayout);
		group.setText (registry.getString("ECORendering.GROUP.RelatedECI"));
		
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 180;
		group.setLayoutData (gridData);
		
		//하단에 위치
		FormData formData = new FormData();
		formData.top = new FormAttachment(10, 15);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		
		relatedECITable = new Table(group, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		relatedECITable.setHeaderVisible(true);
		relatedECITable.setLinesVisible(true);
		relatedECITable.setLayoutData(formData);
		
		formData = new FormData();
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(relatedECITable, -5);
		deleteRelatedECIButton = new Button(group, SWT.NONE);
		deleteRelatedECIButton.setText("Delete");
		deleteRelatedECIButton.setLayoutData(formData);
		deleteRelatedECIButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteRelatedECI();
			}
		});
		
		formData = new FormData();
		formData.right = new FormAttachment(deleteRelatedECIButton, -5);
		formData.bottom = new FormAttachment(relatedECITable, -5);
		addRelatedECIButton = new Button(group, SWT.NONE);
		addRelatedECIButton.setText("Add");
		addRelatedECIButton.setLayoutData(formData);
		addRelatedECIButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				addConRelatedECI();
			}
		});
		
		int i = 0;
		for(String value : relatedECI){
			TableColumn column = new TableColumn(relatedECITable, SWT.NONE);
			column.setText(value);
			column.setWidth(relatedECISize[i]);
			i++;
		}
	}
	
	/** 디자인 정보 입력 그룹 생성 */
	public void createDesignPanel() {
		GridLayout gridLayout = new GridLayout (1, false);
		Group group = new Group (layoutComposite, SWT.NONE);
		group.setLayout (gridLayout);
		group.setText ("Design Check");
		
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		group.setLayoutData (gridData);
		
		gridLayout = new GridLayout (4, false);
		Group designGroup = new Group (group, SWT.NONE);
		designGroup.setLayout (gridLayout);
		designGroup.setText (registry.getString("ECORendering.GROUP.DesignSource"));
		
		gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		designGroup.setLayoutData (gridData);

		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 150;
		Label design_concept_no_lbl = new Label (designGroup, SWT.RIGHT);
		design_concept_no_lbl.setText (registry.getString("ECORendering.LABEL.design_concept_no"));
		design_concept_no_lbl.setLayoutData(gridData);
		
		gridData = new GridData (200, SWT.DEFAULT);
		design_concept_no = new Text(designGroup, SWT.BORDER);
		design_concept_no.setLayoutData(gridData);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 150;
		Label design_related_doc_lbl = new Label (designGroup, SWT.RIGHT);
		design_related_doc_lbl.setText (registry.getString("ECORendering.LABEL.design_related_doc"));
		design_related_doc_lbl.setLayoutData(gridData);
		
		gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		design_related_doc = new Text(designGroup, SWT.BORDER);
		design_related_doc.setLayoutData(gridData);
		
		gridLayout = new GridLayout (8, false);
		Group regGroup = new Group (group, SWT.NONE);
		regGroup.setLayout (gridLayout);
		regGroup.setText (registry.getString("ECORendering.GROUP.RegulationCheck"));
		
		gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		regGroup.setLayoutData (gridData);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 80;
		gridData.verticalSpan = 2;
		Label design_reg_catg_lbl = new Label (regGroup, SWT.RIGHT);
		design_reg_catg_lbl.setText (registry.getString("ECORendering.LABEL.design_reg_catg"));
		design_reg_catg_lbl.setLayoutData(gridData);

		gridData = new GridData (195, SWT.DEFAULT);
		gridData.verticalSpan = 2;
		design_reg_catg = new SWTComboBox(regGroup, SWT.BORDER);
		comboValueSetting(design_reg_catg, "S7_CATEGORY");
		design_reg_catg.setLayoutData(gridData);

		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		adr_yn = new Button (regGroup, SWT.CHECK);
		adr_yn.setLayoutData(gridData);
		adr_yn.setText(registry.getString("ECORendering.LABEL.adr_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		canada_yn = new Button (regGroup, SWT.CHECK);
		canada_yn.setLayoutData(gridData);
		canada_yn.setText(registry.getString("ECORendering.LABEL.canada_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		china_yn = new Button (regGroup, SWT.CHECK);
		china_yn.setLayoutData(gridData);
		china_yn.setText(registry.getString("ECORendering.LABEL.china_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		japan_yn = new Button (regGroup, SWT.CHECK);
		japan_yn.setLayoutData(gridData);
		japan_yn.setText(registry.getString("ECORendering.LABEL.japan_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		dom_yn = new Button (regGroup, SWT.CHECK);
		dom_yn.setLayoutData(gridData);
		dom_yn.setText(registry.getString("ECORendering.LABEL.dom_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		ecc_yn = new Button (regGroup, SWT.CHECK);
		ecc_yn.setLayoutData(gridData);
		ecc_yn.setText(registry.getString("ECORendering.LABEL.ecc_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		ece_yn = new Button (regGroup, SWT.CHECK);
		ece_yn.setLayoutData(gridData);
		ece_yn.setText(registry.getString("ECORendering.LABEL.ece_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		gcc_yn = new Button (regGroup, SWT.CHECK);
		gcc_yn.setLayoutData(gridData);
		gcc_yn.setText(registry.getString("ECORendering.LABEL.gcc_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		fmvss_yn = new Button (regGroup, SWT.CHECK);
		fmvss_yn.setLayoutData(gridData);
		fmvss_yn.setText(registry.getString("ECORendering.LABEL.fmvss_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		others_yn = new Button (regGroup, SWT.CHECK);
		others_yn.setLayoutData(gridData);
		others_yn.setText(registry.getString("ECORendering.LABEL.others_yn"));
		
		gridData = new GridData (SWT.DEFAULT, SWT.DEFAULT);
		gridData.horizontalSpan = 2;
		dr_yn = new Button (regGroup, SWT.CHECK);
		dr_yn.setLayoutData(gridData);
		dr_yn.setText(registry.getString("ECORendering.LABEL.dr_yn"));
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 80;
		Label description_lbl = new Label (regGroup, SWT.RIGHT);
		description_lbl.setText (registry.getString("ECORendering.LABEL.CategoryDescription"));
		description_lbl.setLayoutData(gridData);

		gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 7;
		description = new Text(regGroup, SWT.BORDER);
		description.setLayoutData(gridData);
		
		gridLayout = new GridLayout (6, false);
		Group envGroup = new Group (group, SWT.NONE);
		envGroup.setLayout (gridLayout);
		envGroup.setText (registry.getString("ECORendering.GROUP.EnvironmentCheck"));
		gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		envGroup.setLayoutData (gridData);

		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 80;
		Label weight_chg_yn_lbl = new Label (envGroup, SWT.RIGHT);
		weight_chg_yn_lbl.setText (registry.getString("ECORendering.LABEL.weight_chg_yn"));
		weight_chg_yn_lbl.setLayoutData(gridData);
		makeMadatoryImage(weight_chg_yn_lbl);
		
		gridData = new GridData (180, SWT.DEFAULT);
		weight_chg_yn = new SYMCYesNoRadio(envGroup, SWT.NONE);
		weight_chg_yn.setLayoutData(gridData);
		setMadatory(weight_chg_yn);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 80;
		Label material_chg_yn_lbl = new Label (envGroup, SWT.RIGHT);
		material_chg_yn_lbl.setText (registry.getString("ECORendering.LABEL.material_chg_yn"));
		material_chg_yn_lbl.setLayoutData(gridData);
		makeMadatoryImage(material_chg_yn_lbl);
		
		gridData = new GridData (100, SWT.DEFAULT);
		material_chg_yn = new SYMCYesNoRadio(envGroup, SWT.NONE);
		material_chg_yn.setLayoutData(gridData);
		setMadatory(material_chg_yn);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 160;
		Label cost_chg_yn_lbl = new Label (envGroup, SWT.RIGHT);
		cost_chg_yn_lbl.setText (registry.getString("ECORendering.LABEL.cost_chg_yn"));
		cost_chg_yn_lbl.setLayoutData(gridData);
		makeMadatoryImage(cost_chg_yn_lbl);
		
		cost_chg_yn = new SYMCYesNoRadio(envGroup, SWT.NONE);
		setMadatory(cost_chg_yn);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 80;
		Label recycling_yn_lbl = new Label (envGroup, SWT.RIGHT);
		recycling_yn_lbl.setText (registry.getString("ECORendering.LABEL.recycling_yn"));
		recycling_yn_lbl.setLayoutData(gridData);
		makeMadatoryImage(recycling_yn_lbl);
		
		gridData = new GridData (180, SWT.DEFAULT);
		recycling_yn = new SYMCYesNoRadio(envGroup, SWT.NONE);
		recycling_yn.setLayoutData(gridData);
		setMadatory(recycling_yn);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 80;
		Label env_law_yn_lbl = new Label (envGroup, SWT.RIGHT);
		env_law_yn_lbl.setText (registry.getString("ECORendering.LABEL.env_law_yn"));
		env_law_yn_lbl.setLayoutData(gridData);
		makeMadatoryImage(env_law_yn_lbl);
		
		gridData = new GridData (100, SWT.DEFAULT);
		env_law_yn = new SYMCYesNoRadio(envGroup, SWT.NONE);
		env_law_yn.setLayoutData(gridData);
		setMadatory(env_law_yn);
		
		gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		env_law_desc = new Text(envGroup, SWT.BORDER);
		env_law_desc.setLayoutData(gridData);

		gridLayout = new GridLayout (6, false);
		Group seNtestGroup = new Group (group, SWT.NONE);
		seNtestGroup.setLayout (gridLayout);
		seNtestGroup.setText (registry.getString("ECORendering.GROUP.SEAndVehicleTest"));
		
		gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		seNtestGroup.setLayoutData (gridData);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 80;
		Label se_yn_lbl = new Label (seNtestGroup, SWT.RIGHT);
		se_yn_lbl.setText (registry.getString("ECORendering.LABEL.se_yn"));
		se_yn_lbl.setLayoutData(gridData);
		makeMadatoryImage(se_yn_lbl);
		
		gridData = new GridData (125, SWT.DEFAULT);
		se_yn = new SYMCYesNoRadio(seNtestGroup, SWT.NONE);
		se_yn.setLayoutData(gridData);
		setMadatory(se_yn);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		Label design_verify_lbl = new Label (seNtestGroup, SWT.RIGHT);
		design_verify_lbl.setText (registry.getString("ECORendering.LABEL.design_verify"));
		design_verify_lbl.setLayoutData(gridData);
		
		gridData = new GridData (120, SWT.DEFAULT);
		design_verify = new SWTComboBox(seNtestGroup, SWT.BORDER);
		comboValueSetting(design_verify, "S7_DESIGN_CHECK");
		design_verify.setLayoutData(gridData);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		Label se_related_doc_lbl = new Label (seNtestGroup, SWT.RIGHT);
		se_related_doc_lbl.setText (registry.getString("ECORendering.LABEL.se_related_doc"));
		se_related_doc_lbl.setLayoutData(gridData);
		
		gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		se_related_doc = new Text(seNtestGroup, SWT.BORDER);
		se_related_doc.setLayoutData(gridData);
		
		gridData = new GridData (SWT.END, SWT.CENTER, false, false);
		gridData.widthHint = 80;
		Label veh_dvp_yn_lbl = new Label (seNtestGroup, SWT.RIGHT);
		veh_dvp_yn_lbl.setText (registry.getString("ECORendering.LABEL.veh_dvp_yn"));
		veh_dvp_yn_lbl.setLayoutData(gridData);
		makeMadatoryImage(veh_dvp_yn_lbl);
		
		gridData = new GridData (125, SWT.DEFAULT);
		veh_dvp_yn = new SYMCYesNoRadio(seNtestGroup, SWT.NONE);
		veh_dvp_yn.setLayoutData(gridData);
		
		gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 4;
		veh_dvp_result_desc = new Text(seNtestGroup, SWT.BORDER);
		veh_dvp_result_desc.setLayoutData(gridData);
	}
	
	/**
	 * 관련 문서 파일 선택 그룹 생성
	 */
	private void createFilePanel(){
		fileComposite = new FileAttachmentComposite(layoutComposite);
	}
	
	@SuppressWarnings("unused")
	private void createRequestButtonPanel() {
		Composite composite = new Composite(layoutComposite, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
//		setControlSkipEnable(composite, true);
		
		requestApproval = new Button(composite, SWT.PUSH);
		requestApproval.setText("Create Workflow");
		setControlSkipEnable(requestApproval, true);
		requestApproval.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(savingCheckCount != 1 && btnCheckSavingDataSet.getVisible()){
					MessageBox.post(AIFDesktop.getActiveDesktop(), "3D Saving DataSet 실행 후 Create Workflow 실행해 주시기 바랍니다.", "WARNING", MessageBox.WARNING);
					return;
				}
				/**
				 * [CF-3607][20230217]ECO 동시 적용 ECO입력 내용 25자이하로 제한
				 * ECO가 결재 완료 되면 후속시스템(PDQR, QMS)으로 인터페이스 하는데 후속시스템 테이블의 동시 적용 ECO 컬럼 사이즈가 25로 제한되어 팀센터에서 동시 적용 ECO를 추가 할때 3개까지만 입력 하도록 제한 
				 * Table Name : IF_USER.IF_LEC_ECO
        		 * Table Column Name : SYNC_APP
				 */
		     	if(!checkConCurrentECO("")){
		     		return;
		     	}
				ECOProcessCommand command = null;
				try{
					command = new ECOProcessCommand(ecoRevision);
				} catch(Exception e1){
					e1.printStackTrace();
				}
			}
		});	
		
	}
	
	/** 채번 */
	private void createECONo() {
		ECOGenerateIDDialog dialog = new ECOGenerateIDDialog(getShell(), this);
		dialog.open();
	}
	
	/** 속성 및 관계 변경 여부 */
	public boolean isModified() {
		if(ecoRevision != null && ecoRevision.isCheckedOut() && isPropertisModified() || isApprovalLineModified || isConCurrentECOModified || isRelatedECIModified || fileComposite.isFileModified())
			return true;
		return false;
	}
	
	/** 속성 변경 여부 */
	public boolean isPropertisModified() {
		ecoPropertyMap.remove("item_id");
		HashMap<String, String> paramMap = getParamMap();
		String property = "";
		String param = "";
		for(Object key : ecoPropertyMap.keySet().toArray()){
			param = paramMap.get(key);
			if(param == null) param = "";
			property = ecoPropertyMap.get(key);
			if(property == null) property = "";
			if(!param.equals(property)){
				return true;
			}
		}
		return false;
	}
	
	/** 체크인 시 저장 여부 확인 */
	public boolean isSavable() {
		/**
		 * [CF-3607][20230217]ECO 동시 적용 ECO입력 내용 25자이하로 제한
		 * ECO가 결재 완료 되면 후속시스템(PDQR, QMS)으로 인터페이스 하는데 후속시스템 테이블의 동시 적용 ECO 컬럼 사이즈가 25로 제한되어 팀센터에서 동시 적용 ECO를 추가 할때 3개까지만 입력 하도록 제한 
		 * Table Name : IF_USER.IF_LEC_ECO
		 * Table Column Name : SYNC_APP
		 */
     	if(!checkConCurrentECO("")){
     		return false;
     	}
		String message = validationForSave();
		if(message.equals("") || message == null){
			return true;
		}
		MessageBox.post(getShell(), message, "ERROR", MessageBox.ERROR);
		return false;
	}
	
	/** 속성 값 맵핑 */
	private void initData() {
		ecoInfoNControlMap.put("item_id", item_id);
		ecoInfoNControlMap.put("object_desc", object_desc);
		ecoInfoNControlMap.put("s7_PLANT_CODE", plant_code);
		ecoInfoNControlMap.put("s7_CHANGE_REASON", change_reason);
		ecoInfoNControlMap.put("s7_EFFECT_POINT", effect_point);
		ecoInfoNControlMap.put("s7_EFFECT_POINT_DATE", effect_point_date);
		ecoInfoNControlMap.put("s7_ECO_TYPE", eco_type);
		ecoInfoNControlMap.put("s7_DESIGN_CONCEPT_NO", design_concept_no);
		ecoInfoNControlMap.put("s7_DESIGN_REG_CATG", design_reg_catg);
		ecoInfoNControlMap.put("s7_ADR_YN", adr_yn);
		ecoInfoNControlMap.put("s7_CANADA_YN", canada_yn);
		ecoInfoNControlMap.put("s7_CHINA_YN", china_yn);
		ecoInfoNControlMap.put("s7_JAPAN_YN", japan_yn);
		ecoInfoNControlMap.put("s7_DOM_YN", dom_yn);
		ecoInfoNControlMap.put("s7_ECC_YN", ecc_yn);
		ecoInfoNControlMap.put("s7_ECE_YN", ece_yn);
		ecoInfoNControlMap.put("s7_GCC_YN", gcc_yn);
		ecoInfoNControlMap.put("s7_FMVSS_YN", fmvss_yn);
		ecoInfoNControlMap.put("s7_OTHERS_YN", others_yn);
		ecoInfoNControlMap.put("s7_DR_ITEM_YN", dr_yn);
		ecoInfoNControlMap.put("s7_ENV_LAW_YN", env_law_yn);
		ecoInfoNControlMap.put("s7_ENV_LAW_DESC", env_law_desc);
		ecoInfoNControlMap.put("s7_WEIGHT_CHG_YN", weight_chg_yn);
		ecoInfoNControlMap.put("s7_COST_CHG_YN", cost_chg_yn);
		ecoInfoNControlMap.put("s7_MATERIAL_CHG_YN", material_chg_yn);
		ecoInfoNControlMap.put("s7_RECYCLING_YN", recycling_yn);
		ecoInfoNControlMap.put("s7_SE_YN", se_yn);
		ecoInfoNControlMap.put("s7_DESIGN_VERIFY", design_verify);
		ecoInfoNControlMap.put("s7_VEH_DVP_YN", veh_dvp_yn);
		ecoInfoNControlMap.put("s7_VEH_DVP_RESULT_YN", veh_dvp_result_yn);
		ecoInfoNControlMap.put("s7_VEH_DVP_RESULT_DESC", veh_dvp_result_desc);
		ecoInfoNControlMap.put("s7_REPRESENTED_PROJECT", project);
		ecoInfoNControlMap.put("s7_ECR_NO", relatedECR);
		ecoInfoNControlMap.put("s7_SE_RELATED_DOC", se_related_doc);
		ecoInfoNControlMap.put("s7_DESIGN_CATG_DESC", description);
		if(isReleased){
			ecoInfoNControlMap.put("s7_AFFECTED_PROJECT", affected_project);
		}
	}

	/** 화면 속성 맵 생성 */
	public HashMap<String, String> getParamMap(){
		HashMap<String, String> paramMap = new HashMap<String, String>();
		ecoInfoNControlMap.remove("item_id");
		String value = "";
		for(Object property : ecoInfoNControlMap.keySet().toArray()){
			value = "";
			if(ecoInfoNControlMap.get(property) instanceof Text){
				Text con = (Text) ecoInfoNControlMap.get(property);
				value = con.getText();
			}else if(ecoInfoNControlMap.get(property) instanceof SYMCYesNoRadio){
				SYMCYesNoRadio con = (SYMCYesNoRadio) ecoInfoNControlMap.get(property);
				value = con.getText();
			}else if(ecoInfoNControlMap.get(property) instanceof SWTComboBox){
				SWTComboBox con = (SWTComboBox) ecoInfoNControlMap.get(property);
				Object[] selects = con.getSelectedItems();
				if(selects != null){
					for(Object select : selects){
						if(value.equals("")){
							value = select.toString();
						}else{
							value = value+con.getMultipleDelimeter()+select.toString();
						}
					}
				}
			}else if(ecoInfoNControlMap.get(property) instanceof Button){
				Button con = (Button) ecoInfoNControlMap.get(property);
				if(con.getSelection())
					value = "Y";
				else
					value = "N";
			}else if(ecoInfoNControlMap.get(property) instanceof SYMCDateTimeButton){
				SYMCDateTimeButton con = (SYMCDateTimeButton) ecoInfoNControlMap.get(property);
				if(property.equals("s7_EFFECT_POINT_DATE")){
					if(effect_point.getSelection()) 
						value = "";
					else
						value = con.getTCDate(session);
				}else{
					value = con.getTCDate(session);
				}
			}
			paramMap.put((String)property, value);
		}
		return paramMap;
	}

	public TCComponentChangeItemRevision getEcoRevision() {
		return ecoRevision;
	}
	
	private void comboValueSetting(SWTComboBox combo, String lovName) {
		try {
			if (lovName != null) {
				TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
				TCComponentListOfValues[] listofvalues = listofvaluestype.find(lovName);
				if(listofvalues == null || listofvalues.length == 0) {
					return;
				}
				TCComponentListOfValues listofvalue = listofvalues[0];
				String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
				String[] lovDesces = listofvalue.getListOfValues().getDescriptions();
				int i = 0;
				for(String lovValue : lovValues){
					combo.addItem(lovValue+" (" + lovDesces[i] + ")", lovValue);
					i++;
				}
			}
			combo.setAutoCompleteSuggestive(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 결재선 삭제 */
	private void deleteApprovalLine(){
		TableItem[] items = approvalLineTable.getSelection();
		if (items.length == 0) return;
		items[0].dispose();
		isApprovalLineModified = true;
	}
	
	/** 동시 설변 삭제 */
	private void deleteConCurrentECO(){
		TableItem[] items = conCurrentECOTable.getSelection();
		if (items.length == 0) return;
		items[0].dispose();
		isConCurrentECOModified = true;
	}
	
	/** 관련 ECI 삭제 */
	private void deleteRelatedECI(){
		TableItem[] items = relatedECITable.getSelection();
		if (items.length == 0) return;
		items[0].dispose();
		isRelatedECIModified = true;
	}
	
	/**
	 * Composite 반환
	 */
	public Composite getComposite() {
		return this;
	}
	

	@Override
	public void load() {
		
	}
	
	private void loadUserApprovalLine() {
		
		try{
			ApprovalLineData map = new ApprovalLineData();
			map.setSaved_user(session.getUser().getUserId());
			map.setEco_no(SYMCECConstant.ECO_PROCESS_TEMPLATE);
			
			ArrayList<ApprovalLineData> resultSavedApprovalLines = dao.loadSavedUserApprovalLine(map);

			if(resultSavedApprovalLines == null || resultSavedApprovalLines.size() < 1){
				MessageBox.post(layoutComposite.getShell(), "You have no saved approval lines.", "Information", MessageBox.INFORMATION);
				return;
			}
			String[] savedApprovalLines = new String[resultSavedApprovalLines.size()];
			int i = 0;
			for(ApprovalLineData resultSavedApprovalLine : resultSavedApprovalLines){
				savedApprovalLines[i] = resultSavedApprovalLine.getSaved_name();
				i++;
			}
			
			SearchMyAssignDialog dialog = new SearchMyAssignDialog(getShell(), session, savedApprovalLines);
			ArrayList<ApprovalLineData> selectedItemInfos = dialog.open();
			if(selectedItemInfos != null){
				if(approvalLineTable.getItemCount() > 0) approvalLineTable.removeAll();
				for(ApprovalLineData selectedItemInfo : selectedItemInfos){
					TableItem item = new TableItem(approvalLineTable, SWT.NONE);
					item.setText(0, selectedItemInfo.getTask());
					item.setText(1, selectedItemInfo.getTeam_name());
					item.setText(2, selectedItemInfo.getUser_name());
					item.setData("puid", selectedItemInfo.getTc_member_puid());//TCComponentGroupMember puid
					isApprovalLineModified = true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
		}
	}
	
	/**
	 * 결재 정보 확인
	 * @param approveTask
	 * @throws TCException
	 */
	private void readSignoffTaskProfile(TCComponentTask approveTask) throws TCException {
        TCComponentTask performSignoffTask = approveTask.getSubtask("perform-signoffs");
        TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();
        
        if(signoffs.length > 0) {
        	// [SR150406-020][2015.04.07][jclee] 결재 기각 시 결재선 상 결재일 표시 여부 비교를 위해 Creator 결재선의 상신일을 가져온다.
        	// 한번도 기각되지 않아 결재일이 없을 경우 최초 상신일을 가져온다.
        	TCComponentTask[] subTasks = approveTask.getRoot().getSubtasks();
        	
        	Date dateCreatorApproval = null;
        	for (int inx = 0; inx < subTasks.length; inx++) {
				if (subTasks[inx].getName().equals("Creator")) {
					TCComponentSignoff[] tempSignoffs = subTasks[inx].getValidSignoffs();
					for (TCComponentSignoff signoff : tempSignoffs) {
						signoff.refresh();
						
						if (subTasks[inx].getName().equals("Creator")) {
							dateCreatorApproval = signoff.getDateProperty("decision_date");
							if (dateCreatorApproval == null) {
								dateCreatorApproval = subTasks[inx].getDateProperty("creation_date");
							}
//							signoff.getProperties();
							break;
						}
					}
				}
			}
        	
            for(TCComponentSignoff signoff : signoffs) {
            	signoff.refresh();
            	TCComponentGroupMember groupMember = signoff.getGroupMember();
            	String[] groupMemberProperties = groupMember.getProperties(new String[]{"the_group","the_user"});
            	String[] signoffProperties = signoff.getProperties(new String[]{"decision_date","comments"});
            	Date signoffDateProperty = signoff.getDateProperty("decision_date");
            	
            	TCComponent pserson = groupMember.getUser().getRelatedComponent("person");
				TableItem item = new TableItem(approvalLineTable, SWT.NONE);
				item.setText(0, approveTask.getName());
				item.setText(1, groupMemberProperties[0]);
				item.setText(2, groupMemberProperties[1]);
				item.setText(3, pserson.getProperty("PA10"));
				if(approveTask.getName().equals("Creator")){ // // FIXED, 20130531, DJKIM, 작업자 정보 첫줄에 추가 및 상신일과 재상신 구분
					if(signoffProperties[0] == null || signoffProperties[0].equals("")){
						item.setText(4, approveTask.getRoot().getProperty("creation_date"));
						item.setText(5, "");
					}else{
						item.setText(4, signoffProperties[0]);
						item.setText(5, signoffProperties[1]);
					}
				}else{
					// [SR150406-020][2015.04.07][jclee] 현 결재선 사용자의 결재일이 상신일 혹은 재상신일보다 이전일 경우 결재선상에 보여주지 않도록 수정
					String sMaturity = ecoRevision.getProperty("s7_ECO_MATURITY");
					if (sMaturity != null && !sMaturity.equals("") && !sMaturity.equals("In Work") && dateCreatorApproval != null && signoffDateProperty != null && !signoffDateProperty.before(dateCreatorApproval)) {
						item.setText(4, signoffProperties[0]);
					} else {
						item.setText(4, "");
					}
					item.setText(5, signoffProperties[1]);
				}
				item.setData("puid", groupMember.getUid());
            }
        }
    }
	
	/** 저장 */
	public void save(){
//		System.out.println("A Save Start....");
		String message = validationForSave();
		if(message.equals("") || message == null){
			layoutComposite.getDisplay().syncExec(new Runnable() {
				public void run() {
					try {

						if(isApprovalLineModified)
							saveApprovalLine();

						if(isConCurrentECOModified)
							saveConcurrentECO();

						if(isRelatedECIModified)
							saveRelatedECI();

						if(eciRevision != null){
							ecoRevision.add(SYMCECConstant.PROBLEM_REL, eciRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL));
						}

						if(fileComposite.isFileModified()){
							fileComposite.createDatasetAndMakerelation(ecoRevision);
						}
						
						if(eco_kind != null){
							ecoRevision.setProperty("s7_ECO_MATURITY", "In Work");
							ecoRevision.setProperty("s7_ECO_KIND", eco_kind);
						}

						ecoRevision.setProperties(getParamMap());
						
						setTypedReference();
						
//						System.out.println("  A Saved ");
						
					} catch (Exception e) {
						e.printStackTrace();
						MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
					}
				}
			});
		}else{
			MessageBox.post(getShell(), message, "ERROR", MessageBox.ERROR);
		}
//		System.out.println("A Save End....");
	}
	
	/**
	 * Typed Reference 생성
	 * @throws Exception
	 */
	protected void setTypedReference() throws Exception {
		/**
		 * [SR150417-011][20150908][jclee] Admin Check Properties. (Typed Reference)
		 */
		TCComponent trECORevision = ecoRevision.getReferenceProperty("s7_ECO_TypedReference");
		if (trECORevision == null) {
			trECORevision = SYMTcUtil.createApplicationObject(ecoRevision.getSession(), "S7_ECO_TypedReference",
					new String[] {
							ECOAdminCheckConstants.PROP_CHANGE_CAUSE_1,
							ECOAdminCheckConstants.PROP_END_ITEM_COUNT1_A,
							ECOAdminCheckConstants.PROP_END_ITEM_COUNT1_M,
							ECOAdminCheckConstants.PROP_CHANGE_CAUSE_2,
							ECOAdminCheckConstants.PROP_END_ITEM_COUNT2_A,
							ECOAdminCheckConstants.PROP_END_ITEM_COUNT2_M,
							ECOAdminCheckConstants.PROP_REGULAR_PROJECT_CODE,
							ECOAdminCheckConstants.PROP_NEW_PROJECT_CODE,
							ECOAdminCheckConstants.PROP_ADMIN_CHECK,
							ECOAdminCheckConstants.PROP_NOTE},
					new String[] {"", "", "", "", "", "", "", "", "", ""}
					);
			ecoRevision.setReferenceProperty("s7_ECO_TypedReference", trECORevision);
		}
	}

	/** 결재선 저장 */
	private void saveApprovalLine() {
		ArrayList<ApprovalLineData> paramList = new ArrayList<ApprovalLineData>();
		TableItem[] itemList = approvalLineTable.getItems();
		
		try {
			String eco_no = item_id.getText();
			ApprovalLineData delLine = new ApprovalLineData();
			delLine.setEco_no(eco_no);
			
			dao.removeApprovalLine(delLine);

			if(itemList != null && itemList.length > 0){
				int i = 0;
				for(TableItem item : itemList){
					ApprovalLineData theLine = new ApprovalLineData();
					theLine.setEco_no(eco_no);
					theLine.setSort(i+"");
					theLine.setTask(item.getText(0));
					theLine.setTeam_name(item.getText(1));
					theLine.setUser_name(item.getText(2));
					theLine.setTc_member_puid((String) item.getData("puid"));

					paramList.add(theLine);
					i++;
				}
			}
			dao.saveApprovalLine(paramList);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(getShell(), e.getMessage(), "ERROR", MessageBox.ERROR);
		}
		isApprovalLineModified = false;
	}
	
	/** ECO 릴레이션 */
	private void saveConcurrentECO(){
		try {
			//릴레이션 삭제
			TCComponent[] oldRevisions = ecoRevision.getRelatedComponents(SYMCECConstant.CONCURRENT_ECO);
			ecoRevision.remove(SYMCECConstant.CONCURRENT_ECO, oldRevisions);
			
			//릴레이션 추가
			if(conCurrentECOTable.getItemCount() > 0){
				TableItem[] conCurrentECOs = conCurrentECOTable.getItems();
				TCComponent[] presentRevisions = new TCComponent[conCurrentECOs.length];
				int i = 0;
				for(TableItem tableItem : conCurrentECOs){
					presentRevisions[i] = (TCComponent) tableItem.getData("tcComponent");
					i++;
				}
				ecoRevision.add(SYMCECConstant.CONCURRENT_ECO, presentRevisions);
			}
			
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(getShell(), "ERROR : Doing make concurrent ECO.\nCall the PLM Admin.\n"+e.toString(), "ERROR", MessageBox.ERROR);
		}
		isConCurrentECOModified = false;
	}
	
	/** ECI 릴레이션 */
	private void saveRelatedECI(){
//		try {
//			//릴레이션 삭제
//			TCComponent[] oldRevisions = ecoRevision.getRelatedComponents(SYMCECConstant.IMPLEMENTS_REL);
//			ecoRevision.remove(SYMCECConstant.IMPLEMENTS_REL, oldRevisions);
//			
//			//릴레이션 추가
//			if(relatedECITable.getItemCount() > 0){
//				TableItem[] tableItems = relatedECITable.getItems();
//				TCComponent[] presentRevisions = new TCComponent[tableItems.length];
//				int i = 0;
//				for(TableItem tableItem : tableItems){
//					presentRevisions[i] = (TCComponent) tableItem.getData("tcComponent");
//					i++;
//				}
//				ecoRevision.add(SYMCECConstant.IMPLEMENTS_REL, presentRevisions);
//			}
//			
//		} catch (TCException e) {
//			e.printStackTrace();
//			MessageBox.post(getShell(), "ERROR : Doing make concurrent ECO.\nCall the PLM Admin.\n"+e.toString(), "ERROR", MessageBox.ERROR);
//		}
//		isRelatedECIModified = false;
		
		try {
			String[] sECINos = new String[relatedECITable.getItemCount()];
			if (relatedECITable.getItemCount() > 0) {
				TableItem[] tableItems = relatedECITable.getItems();
				for (int inx = 0; inx < tableItems.length; inx++) {
					sECINos[inx] = ((String[])tableItems[inx].getData("String[5]"))[0];
				}
			}
			ecoRevision.getTCProperty("s7_ECI_NO").setStringValueArray(sECINos);
			
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(getShell(), "ERROR : Doing make related ECI.\nCall the PLM Admin.\n"+e.toString(), "ERROR", MessageBox.ERROR);
		}
		isRelatedECIModified = false;
	}
	
	/** eco_id SET */
	public void setItem_id(String item_id) {
		this.item_id.setText(item_id);
	}
	
	public void setRepProject(String repProject) {
		this.project.setText(repProject);
	}
	
	public void setEco_kind(String eco_kind) {
		this.eco_kind = eco_kind;
	}
	
	private void makeMadatoryImage(Control con){
		ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
		dec.setImage(registry.getImage("CONTROL_MANDATORY"));
		dec.setDescriptionText("This value will be required.");
	}
	
	private void setMadatory(Control con){
		if(con instanceof SYMCYesNoRadio){
		}else{
			ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
			dec.setImage(registry.getImage("CONTROL_MANDATORY"));
			dec.setDescriptionText("This value will be required.");
		}
		madatoryControls.add(con);
	}
	
	private static List sortByValue(final HashMap hMap, final String order){
		List list  = new ArrayList();
		list.addAll(hMap.keySet());
		
		Collections.sort(list, new Comparator(){
			public int compare(Object o1, Object o2){
				Object v1 = hMap.get(o1);
				Object v2 = hMap.get(o2);
				if(order.equals("ASC")){
					return ((Comparable)v1).compareTo(v2);
				} else if (order.equals("DESC")){
					return ((Comparable)v2).compareTo(v1);
				} else {
					return -1;
				}
			}
		});
		return list;
	}
	
	/** 속성 셋팅 */
	public void setProperties() {
		layoutComposite.getDisplay().syncExec(new Runnable() {
			public void run() {
				try {

					setCursor(new Cursor(layoutComposite.getDisplay(), SWT.CURSOR_WAIT));

					if(ecoRevision == null) return;
					
					// 채번 숨김
					ecoNoGenBtn.setVisible(false);

					// 속성 정보 셋
					String[] properties = null;
					properties = ecoRevision.getProperties(ecoInfoProperties);
					for(int i = 0 ; i < ecoInfoProperties.length ; i++){
						ecoPropertyMap.put(ecoInfoProperties[i], properties[i]);
					}
					properties = ecoRevision.getProperties(designProperties);
					for(int i = 0 ; i < designProperties.length ; i++){
						ecoPropertyMap.put(designProperties[i], properties[i]);
					}

					for(Object key : ecoPropertyMap.keySet().toArray()){
						if(ecoInfoNControlMap.get(key) instanceof Text){
							Text con = (Text) ecoInfoNControlMap.get(key);
							con.setText(ecoPropertyMap.get(key));
						}else if(ecoInfoNControlMap.get(key) instanceof SYMCYesNoRadio){
							SYMCYesNoRadio con = (SYMCYesNoRadio) ecoInfoNControlMap.get(key);
							con.setText(ecoPropertyMap.get(key));
						}else if(ecoInfoNControlMap.get(key) instanceof SWTComboBox){
							SWTComboBox con = (SWTComboBox) ecoInfoNControlMap.get(key);
							String strings = ecoPropertyMap.get(key);
							con.setSelectedItems(strings.split(con.getMultipleDelimeter()));
						}else if(ecoInfoNControlMap.get(key) instanceof Button){
							Button con = (Button) ecoInfoNControlMap.get(key);
							if(ecoPropertyMap.get(key).equals("Y"))
								con.setSelection(true);
						}else if(ecoInfoNControlMap.get(key) instanceof SYMCDateTimeButton){
							SYMCDateTimeButton con = (SYMCDateTimeButton) ecoInfoNControlMap.get(key);
							con.setTCDate(ecoPropertyMap.get(key), session);
						}
					}

					if(effect_point.getSelection()) 
						effect_point_date.setVisible(false);
					else
						effect_point_date.setVisible(true);

					//결재선
					if(approvalLineTable.getItemCount() > 0) approvalLineTable.removeAll();

					if(ecoRevision.getCurrentJob() == null){
						/* 결재 정보 셋 */
						ApprovalLineData theLine = new ApprovalLineData();
						theLine.setEco_no(item_id.getText());
						ArrayList<ApprovalLineData> paramList = dao.getApprovalLine(theLine);
						for(ApprovalLineData map : paramList){
							TableItem item = new TableItem(approvalLineTable, SWT.NONE);
							item.setText(0, map.getTask());
							item.setText(1, map.getTeam_name());
							item.setText(2, map.getUser_name());
							item.setData("puid", map.getTc_member_puid());
						}
					}else{
						TCComponentTask rootTask = ecoRevision.getCurrentJob().getRootTask();
						TCComponentTask[] subTasks = rootTask.getSubtasks();
						
//						for(TCComponentTask subTask : subTasks) { // FIXED, 20130531, DJKIM, 작업자 정보 첫줄에 추가
//							if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) 
//									|| subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)){
//								if(subTask.getName().equals("Creator"))
//									readSignoffTaskProfile(subTask);
//							}
//						}
//						for(TCComponentTask subTask : subTasks) {
//							if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) 
//									|| subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)){
//								if(!subTask.getName().equals("Creator"))
//									readSignoffTaskProfile(subTask);
//							}
//						}
						
						//순서정렬
						//'Related Team Review', 1, 'Sub-team Leader', 2, 'Design Team Leader', 3, 'Technical Management', 4, 'Reference Department', 5
						HashMap<TCComponentTask, String> taskHash = new HashMap<TCComponentTask, String>();
						String taskName = "";
						String sort = "";
						for(TCComponentTask subTask : subTasks) { // FIXED, 20130531, DJKIM, 작업자 정보 첫줄에 추가
							if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) 
									|| subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)){
								taskName = subTask.getName();
								if(taskName.equals("Creator")){
									readSignoffTaskProfile(subTask);
								} else {
									if(taskName.equals("Related Team Review")){
										sort = "1";
									} else if(taskName.equals("Sub-team Leader")){
										sort = "2";
									} else if(taskName.equals("Design Team Leader")){
										sort = "3";
									} else if(taskName.equals("Technical Management")){
										sort = "4";
									} else if(taskName.equals("Reference Department")){
										sort = "5";
									}
									taskHash.put(subTask,sort);
								}
							}
						}
						
						Iterator it = sortByValue(taskHash, "ASC").iterator();
						while(it.hasNext()){
							readSignoffTaskProfile((TCComponentTask)it.next());
						}
					}
					
					// CONCURRENT ECO
					if(conCurrentECOTable.getItemCount() > 0) conCurrentECOTable.removeAll();

					TCComponent[] referenceECOs = ecoRevision.getRelatedComponents(SYMCECConstant.CONCURRENT_ECO);
					if(referenceECOs != null){
						for(TCComponent referenceECO : referenceECOs){
							// Reference Items에 붙은 Item 중 Concurrent ECO Revision에 대해서만 Load
							if (!referenceECO.getType().equals("S7_ECORevision")) {
								continue;
							}
							
							TCComponentItemRevision revision = (TCComponentItemRevision) referenceECO;
							TableItem item = new TableItem(conCurrentECOTable, SWT.NONE);
							item.setText(revision.getProperties(SYMCECConstant.ECO_POPUP_PROPERTIES));
							item.setData("tcComponent", revision);
						}
					}

					// ECI
					if(relatedECITable.getItemCount() > 0) relatedECITable.removeAll();

					/**
					 *  [SR140701-022][20140902] jclee, 추가, 관련 ECI 로드.
					 */
//					TCComponent[] referenceECIs = ecoRevision.getRelatedComponents(SYMCECConstant.IMPLEMENTS_REL);
//					if(referenceECIs != null){
//						for(TCComponent referenceECI : referenceECIs){
//							TCComponentItemRevision revision = (TCComponentItemRevision) referenceECI;
//							TableItem item = new TableItem(relatedECITable, SWT.NONE);
//							item.setText(revision.getProperties(SYMCECConstant.ECO_POPUP_PROPERTIES));
//							item.setData("tcComponent", revision);
//						}
//					}
					// ECI Relation 방식을 String Array 로 변환하여 화면에 Load.
					String[] sECINos = ecoRevision.getTCProperty("s7_ECI_NO").getStringValueArray();
					if(sECINos != null){
						for(String sECINo : sECINos){
							// [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선
							// 각 ECI마다 정보를 Vision Net에서 가져온다.
							HashMap<String, String> result = dao.searchECIEAI(sECINo);
							//HashMap<String, String> result = dao.searchECI(sECINo);
							TableItem item = new TableItem(relatedECITable, SWT.NONE);
							
							String[] sECIInfo = new String[5];
							sECIInfo[0] = result.get("ECINO");
							sECIInfo[1] = result.get("TITLE");
							sECIInfo[2] = result.get("CUSER");
							sECIInfo[3] = result.get("CTEAM");
							sECIInfo[4] = result.get("CDATE");
							
							item.setText(sECIInfo);
							item.setData("String[5]", sECIInfo);
						}
					}

					// 데이터 셋
					fileComposite.roadDataSet(ecoRevision);
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(getShell(), e.toString(), "ERROR in setProperties()", MessageBox.ERROR);
				}finally{
					setCursor(new Cursor(layoutComposite.getDisplay(), SWT.CURSOR_ARROW));
				}
			}
		});
	}
	
	/** 결재선 저장 */
	private void saveUserApprovalLine() {
		if(approvalLineTable.getItemCount() < 1){
			MessageBox.post(getShell(), "You have no saving informations.", "Information", MessageBox.INFORMATION);
			return;
		}
		AskMyAssignDialog dialog = new AskMyAssignDialog(getShell(), approvalLineTable, session);
		dialog.open();
	}

	/**
	 * 저장 검증
	 * @return 에러
	 */
	public String validationForSave(){
		StringBuffer message = new StringBuffer();
		String value = "";
		// 필수 값 체크
		for(Object property : ecoInfoNControlMap.keySet().toArray()){
			if(madatoryControls.contains(ecoInfoNControlMap.get(property))){
				value = "";
				if(ecoInfoNControlMap.get(property) instanceof Text){
					Text con = (Text) ecoInfoNControlMap.get(property);
					if(property.equals("s7_EFFECT_POINT_DATE")){
						if(effect_point.getSelection()) {
							value = "Y";
						}else{
							value = con.getText();
						}
					}else{
						value = con.getText();
					}
				}else if(ecoInfoNControlMap.get(property) instanceof SYMCYesNoRadio){
					SYMCYesNoRadio con = (SYMCYesNoRadio) ecoInfoNControlMap.get(property);
					value = con.getText();
				}else if(ecoInfoNControlMap.get(property) instanceof SWTComboBox){
					SWTComboBox con = (SWTComboBox) ecoInfoNControlMap.get(property);
					value = con.getTextField().getText();
				}else if(ecoInfoNControlMap.get(property) instanceof Button){
					Button con = (Button) ecoInfoNControlMap.get(property);
					if(con.getSelection())
						value = "Y";
					else
						value = "N";
				}else if(ecoInfoNControlMap.get(property) instanceof SYMCDateTimeButton){
					SYMCDateTimeButton con = (SYMCDateTimeButton) ecoInfoNControlMap.get(property);
					if(property.equals("s7_EFFECT_POINT_DATE")){
						if(effect_point.getSelection()) 
							value = "Y";
						else
							value = con.getTCDate(session);
					}else{
						value = con.getTCDate(session);
					}
				}
				
				/*
				 *[CF-4217][20230719]ECO 적용 시점 35바이트 이내 작성
				 * 오류 발생 내용 : I/F데이터 생성중 35바이트를 초과하여 에러발생 (PLM최대 입력값(40), IF_LEC_ECO 테이블 컬럼 최대 입력값(35))
				 * 수정 내용 : ECO 생성 또는 수정 시 Change Eff. Point값이 35바이트를 넘어가는 지 체크 하고 35바이트를 넘어갈 시 오류 메시지 발생 
				 * */
				if(ecoInfoNControlMap.get(property) instanceof Text){
					Text con = (Text) ecoInfoNControlMap.get(property);
					if(property.equals("s7_EFFECT_POINT_DATE")){
						if(value.getBytes().length > 35) {
							message.append("적용 시점(Change Eff. Point) 35바이트 이내 입력 요망 \n");
						}
					}
				}
				if(value == null || value.equals(""))
					message.append(registry.getString(property.toString()) +" will be required.\n");
			}
		}
		
		// FIXED 2013.05.13, DJKIM, CATEGORY [DR1/DR2] 선택 시 지역은 하나 이상 필수로 체크 되어야 함.
		String category = design_reg_catg.getTextField().getText();
		if((category.startsWith("DR1") || category.startsWith("DR1")) & !(adr_yn.getSelection() || canada_yn.getSelection() || china_yn.getSelection() || 
				japan_yn.getSelection() || dom_yn.getSelection() || ecc_yn.getSelection() || 
				ece_yn.getSelection() || gcc_yn.getSelection() || fmvss_yn.getSelection() || 
				others_yn.getSelection() || dr_yn.getSelection())){
			message.append(registry.getString("ECORendering.LABEL.design_reg_catg") +" DR1 or DR2 is selected.\nPlease check the country.");
		}

		return message.toString();
	}
	/**
	 * [CF-3607][20230217]ECO 동시 적용 ECO입력 내용 25자이하로 제한
	 * ECO가 결재 완료 되면 후속시스템(PDQR, QMS)으로 인터페이스 하는데 후속시스템 테이블의 동시 적용 ECO 컬럼 사이즈가 25로 제한되어 팀센터에서 동시 적용 ECO를 추가 할때 3개까지만 입력 하도록 제한 
	 * Table Name : IF_USER.IF_LEC_ECO
	 * Table Column Name : SYNC_APP
	 */
	public boolean checkConCurrentECO(String ecoNo){
		boolean checkECO = true;
		TableItem[] conCurrentECOs = conCurrentECOTable.getItems();
		String conCurrentECOKey = "";
		StringBuffer conCurrentKey = new StringBuffer();
		if(ecoNo != null && !ecoNo.equals("")){
			conCurrentKey.append(ecoNo);
		}
		for(TableItem conCurrentECO : conCurrentECOs){ 
			conCurrentECOKey = conCurrentECO.getText(0);
			if(conCurrentKey.length() != 0){
				conCurrentKey.append(",");
			}
			conCurrentKey.append(conCurrentECOKey);
		}
     	if(conCurrentKey.length() > 25){
     		checkECO = false;
     		MessageBox.post(getShell(),"동시 적용 ECO번호 조합은 25자를 초과 할 수 없습니다.\n" + "ECO번호 조합 : " + conCurrentKey.toString() + " (" + conCurrentKey.length() + "자)", "Information", MessageBox.INFORMATION);
     	}
		return checkECO;
	}

	public Button getModuleBOMvalidBtn() {
		return moduleBOMvalidBtn;
	}
	
	public Button getRequestApprovalTopBtn() {
		return requestApprovalTop;
	}
	
	public Button getRequestApprovalBtn() {
		return requestApproval;
	}

	/**
	 * [SR140926-025][20140926][jclee] 기술관리부 사용자가 열었을 경우
	 * @param isEngineManager
	 */
	public void setIsEngineeringManager(boolean isEngineeringManager) {
		this.isEngineeringManager = isEngineeringManager;
	}
	
	public boolean getIsEngineeringManager() {
		return this.isEngineeringManager;
	}
}