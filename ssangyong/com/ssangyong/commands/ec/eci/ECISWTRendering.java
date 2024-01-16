package com.ssangyong.commands.ec.eci;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.assign.AskMyECIAssignDialog;
import com.ssangyong.commands.ec.assign.SearchMyAssignDialog;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.search.LawCheckListDialog;
import com.ssangyong.commands.ec.search.SYMCTeamListDialog;
import com.ssangyong.commands.ec.search.SearchECRDialog;
import com.ssangyong.commands.ec.workflow.ECOProcessCommand;
import com.ssangyong.common.SYMCDateTimeButton;
import com.ssangyong.common.SYMCYesNoRadio;
import com.ssangyong.common.swtsearch.SearchPartDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.FTPConnection;
import com.ssangyong.common.utils.ProcessUtil;
import com.ssangyong.dto.ApprovalLineData;
import com.ssangyong.dto.VnetTeamInfoData;
import com.ssangyong.dto.VnetTeamReviewData;
import com.ssangyong.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.services.NavigatorOpenService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

public class ECISWTRendering extends AbstractSYMCViewer {
	
	private Composite mainComposite;
	private Registry registry;
	private TCSession session;
	
	private TCComponentItemRevision selectedItemRevision;
	
	private TCComponentChangeItemRevision eciRevision;
	
	private GridData gridFillData = new GridData (SWT.FILL, SWT.FILL, false, false);
	
	private String[] eciInfoProperties;
	private Text s7_TITLE, creation_date, s7_ST_DATE,owning_group, owning_user, s7_ECO_NO;
	private SWTComboBox s7_DOC_DIVISION, s7_REPRESENT_VEHICLE, s7_ECI_MATURITY;
	private SWTComboBox s7_APPLIED_VEHICLE;
	
	private Text last_appproval, s7_APPROVAL_NO, s7_BP_DATE, s7_BP_SCHEDULE;
	
	private Text s7_BEFORE_PART_NO, s7_BEFORE_PART_NAME, s7_BEFORE_DESC;
	private Button searchPartButton, addBeforePartButton, deleteBeforePartButton;
	private Table beforePartTable;
	private String[] beforePartTableHeader;
	private int[] beforePartTableHeaderSize;
	private Text s7_AFTER_PART_NO, s7_AFTER_PART_NAME, s7_AFTER_DESC;
	
	private Text s7_EXP_INVEST, s7_CHANGE_COST;
	private Button up, down, same;
	private Text s7_BUDGET_CODE, s7_US, s7_SUPPLIER;
	
	private SWTComboBox s7_AS_COMPATIBLE, s7_SUPPLY_REQ_YN, s7_SUBSTITUDE_PART;
	private Text s7_REF_FILE;//대체품 넘버임
	private Button searchSubstitudePartButton;
	
	private Text s7_REVIEW_DEPT, s7_LAW_CHECK;
	private Button searchReviewteamButton, searchLawCheckButton, searchFileButton, downloadFileButton, searchReferenceTeamButton;
	
	private Text s7_ECR_NO, s7_RELATION_ID;
	
	private Text s7_BASE_ON, s7_REASON_OFFER_DESC, s7_REF_FILE_PATH, s7_REF_DEPT;
	private SWTComboBox s7_CHANGE_REASON;
	private SWTComboBox s7_EC_MANAGE_CODE;
	private SWTComboBox s7_REASON_OFFER_CODE;
	
	private String s7_REVIEW_DEPT_CODE, s7_REF_DEPT_CODE, s7_WORKFLOW;
	
	private HashMap<String, Control> eciInfoNControlMap = new HashMap<String, Control>();
	private HashMap<String, String> eciPropertyMap = new HashMap<String, String>();
	
	private boolean isReleased = false;
	
	private boolean isBeforePartsTableModified = false;
	
	// [SR140701-022] jclee
	private boolean isCreateMode = false;
	
	private File file;
	
	private ArrayList<Control> madatoryControls;
	private HashMap<String, String> users;
	private SWTComboBox combo1, combo2, combo3, combo4;
	private static String STEP = "step";
	private static String SS = ":";
	
	private CustomECODao dao;
	
	public ECISWTRendering(Composite parent) {
		super(parent);
		
		initData();
	}

	public ECISWTRendering(Composite parent, TCComponentItemRevision selectedItemRevision) {
		super(parent);
		this.selectedItemRevision = selectedItemRevision;
		
		eciRevision = null;
		initData();
	}
	
	@Override
	public void createPanel(Composite parent) {
		this.registry = Registry.getRegistry(this);
		this.session = CustomUtil.getTCSession();
		this.madatoryControls = new ArrayList<Control>();
		this.dao = new CustomECODao();
		
		this.eciInfoProperties = new String[]{"s7_DOC_DIVISION","s7_TITLE","s7_WORKFLOW"
				,"s7_REPRESENT_VEHICLE","s7_APPLIED_VEHICLE","s7_BEFORE_PART_NO","s7_BEFORE_PART_NAME"
				,"s7_BEFORE_DESC","s7_AFTER_PART_NO","s7_AFTER_PART_NAME","s7_AFTER_DESC","s7_EXP_INVEST","s7_CHANGE_COST"
				,"s7_COST_SIGN","s7_BUDGET_CODE","s7_US","s7_SUPPLIER","s7_AS_COMPATIBLE","s7_SUPPLY_REQ_YN"
				,"s7_SUBSTITUDE_PART","s7_REVIEW_DEPT","s7_LAW_CHECK","s7_ECR_NO","s7_RELATION_ID","s7_BASE_ON","s7_REASON_OFFER_DESC"
				,"s7_REF_FILE","s7_REF_FILE_PATH","s7_REF_DEPT","s7_CHANGE_REASON","s7_EC_MANAGE_CODE","s7_REASON_OFFER_CODE","s7_ECI_MATURITY"};
		
		/** ECI 확인 **/
		InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
		if(comp != null && comp instanceof TCComponentChangeItemRevision){
			TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) comp;
			if(changeRevision.getType().startsWith(SYMCECConstant.ECITYPE)){
				eciRevision = changeRevision;
			}
		}
		
		/** 결재 여부 **/
		if(eciRevision != null){
			String processProps = null;
			try {
				eciRevision.getItem().refresh();
				processProps = eciRevision.getProperty("date_released");
			} catch (TCException e) {
				e.printStackTrace();
			}
			if(!processProps.equals("")){
				isReleased = true;
			}
		}
		
		parent.setLayout(new GridLayout());
		
		mainComposite = new Composite (this, SWT.BORDER);
		mainComposite.setLayout(new GridLayout());
		
		createTopLayout();
		createPartInfoLayout();
		createCenterLayout();
		createReviewLayout();
		createRequestButtonPanel();
		
	}
	
	/**
	 * ECI 검토 결과 보기 및 비전 넷 ECI 바로 가기 화면 생성
	 */
	private void createReviewLayout() {
		try{
			String itemPuid = eciRevision.getItem().getUid();

			ArrayList<VnetTeamReviewData> resultList = dao.getECIReviewInfo(itemPuid);
			for(VnetTeamReviewData reviewData : resultList){
				@SuppressWarnings("unused")
				ECIReviewRendering reviewComposite = new ECIReviewRendering(mainComposite, reviewData);
			}
			
			Link link = new Link(mainComposite, SWT.NONE);
			setControlSkipEnable(link, true);
			link.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			link.setText("  ▒▒▒ Click here to link Vision-NET  <A>Vision-NET</A> ▒▒▒");
			link.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Runtime rt = Runtime.getRuntime();
					try {
						String linkUrl = eciRevision.getProperty("s7_VISIONNET_URL");
						if(!linkUrl.equals("")){
							String URL = "Explorer \"" + linkUrl + "\"";
							@SuppressWarnings("unused")
							Process proc = rt.exec(URL);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (TCException e1) {
						e1.printStackTrace();
					}
				}
			});
			
		}catch(Exception e){

		}
	}
	
	/**
	 * 결재선 관리 화면 및 결재 상신 버튼 화면 생성
	 */
	private void createRequestButtonPanel() {
//		Composite composite = new Composite(mainComposite, SWT.NONE);
//		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
//		GridLayout layout = new GridLayout();
//		composite.setLayout(layout);
		
		Group group = new Group (mainComposite, SWT.NONE);
		group.setLayout (new GridLayout());
		group.setText (registry.getString("ECISWTRendering.lbl_WORKFLOW"));
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
//		gridData.minimumHeight = 180;
//		if(this.eciRevision != null && !eciRevision.isCheckedOut()){
//			gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
//		}
		group.setLayoutData (gridData);
		
		GridLayout flayout = new GridLayout (8, false);
		flayout.marginWidth = 0;
		flayout.marginHeight = 0;
		Composite composite = new Composite(group, SWT.NONE);
		composite.setLayout(flayout);
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Button loadApprovalLineButton = new Button(composite, SWT.NONE);
		loadApprovalLineButton.setText("Open...");
		loadApprovalLineButton.setLayoutData(new GridData (60, SWT.DEFAULT));
		loadApprovalLineButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				loadUserApprovalLine();
			}
		});
		
		Button saveApprovalLineButton = new Button(composite, SWT.NONE);
		saveApprovalLineButton.setText("Save...");
		gridData = new GridData (60, SWT.DEFAULT);
		gridData.horizontalSpan = 7; 
		saveApprovalLineButton.setLayoutData(gridData);
		saveApprovalLineButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				saveUserApprovalLine();
			}
		});
		
		Label lv1 = new Label(composite, SWT.RIGHT);
		lv1.setText("1차결재");

		combo1 = new SWTComboBox(composite, SWT.BORDER);
		combo1.setData(STEP, 1);
		makeUserList(combo1);
		
		Label lv2 = new Label(composite, SWT.RIGHT);
		lv2.setText("2차결재");

		combo2 = new SWTComboBox(composite, SWT.BORDER);
		combo2.setData(STEP, 2);
		makeUserList(combo2);
		
		Label lv3 = new Label(composite, SWT.RIGHT);
		lv3.setText("3차결재");

		combo3 = new SWTComboBox(composite, SWT.BORDER);
		combo3.setData(STEP, 3);
		makeUserList(combo3);
		
		Label lv4 = new Label(composite, SWT.RIGHT);
		lv4.setText("4차결재");

		combo4 = new SWTComboBox(composite, SWT.BORDER);
		combo4.setData(STEP, 4);
		makeUserList(combo4);
		
		// [SR140701-022] jclee, 결재가 완료되었거나 Create Mode일 경우 Create Workflow 버튼을 보여주지 않음.
		if(!isReleased && !isCreateMode){
			Label lSeparator = new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
			gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
			gridData.horizontalSpan = 8;
			lSeparator.setLayoutData(gridData);

			Composite compositeWF = new Composite(group, SWT.NONE);
			compositeWF.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			compositeWF.setLayout(layout);

			Button requestApproval = new Button(compositeWF, SWT.PUSH);
			requestApproval.setText("Create Workflow");
			setControlSkipEnable(requestApproval, true);
			requestApproval.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					@SuppressWarnings("unused")
					ECOProcessCommand command = null;
					try{
						command = new ECOProcessCommand(eciRevision);
					} catch(Exception e1){
						e1.printStackTrace();
					}
				}
			});
		}
	}
	
	/**
	 * Create Mode Setting
	 */
	public void setCreateMode(boolean isCreateMode) {
		this.isCreateMode = true;
	}
	
	private void loadUserApprovalLine() {
		try{
			ApprovalLineData map = new ApprovalLineData();
			map.setSaved_user(session.getUser().getUserId());
			map.setEco_no(SYMCECConstant.ECI_PROCESS_TEMPLATE);
			
			ArrayList<ApprovalLineData> resultSavedApprovalLines = dao.loadSavedUserApprovalLine(map);

			if(resultSavedApprovalLines == null || resultSavedApprovalLines.size() < 1){
				MessageBox.post("You have no saved approval lines.", "Information", MessageBox.INFORMATION);
				return;
			}
			String[] savedApprovalLines = new String[resultSavedApprovalLines.size()];
			int i = 0;
			for(ApprovalLineData resultSavedApprovalLine : resultSavedApprovalLines){
				savedApprovalLines[i] = resultSavedApprovalLine.getSaved_name();
				i++;
			}
			
			SearchMyAssignDialog dialog = new SearchMyAssignDialog(getShell(), session, savedApprovalLines, true);
			ArrayList<ApprovalLineData> selectedItemInfos = dialog.open();
			if(selectedItemInfos != null){
				combo1.setText("");
				combo2.setText("");
				combo3.setText("");
				combo4.setText("");
				for(ApprovalLineData selectedItemInfo : selectedItemInfos){
					if(selectedItemInfo.getTask().equals("Review 1"))
						combo1.setText(selectedItemInfo.getUser_name());
					if(selectedItemInfo.getTask().equals("Review 2"))
						combo2.setText(selectedItemInfo.getUser_name());
					if(selectedItemInfo.getTask().equals("Review 3"))
						combo3.setText(selectedItemInfo.getUser_name());
					if(selectedItemInfo.getTask().equals("Review 4"))
						combo4.setText(selectedItemInfo.getUser_name());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
		}
	}
	
	
	private void saveUserApprovalLine() {
		s7_WORKFLOW = "";
		if(setForSavingWorkflow(combo1) != null)
			if(setForSavingWorkflow(combo2) != null)
				if(setForSavingWorkflow(combo3) != null)
					setForSavingWorkflow(combo4);
		
		if(s7_WORKFLOW.length() < 1){
			MessageBox.post(getShell(), "You have no saving informations.", "Information", MessageBox.INFORMATION);
			return;
		}
		AskMyECIAssignDialog dialog = new AskMyECIAssignDialog(getShell(), s7_WORKFLOW, session);
		dialog.open();
		
	}
	
	private void makeUserList(SWTComboBox combo) {
		if(users == null){
			users = new HashMap<String,String>();
			try {
				String groupName = owning_group.getText();
				if(groupName.equals(""))
					groupName = "*"+session.getGroup().getProperty("name")+"*";
				TCComponent[] members = CustomUtil.queryComponent("__SYMC_group_members", new String[] {"Group"}, new String[] {groupName});
				if(members != null){
					for (TCComponent member : members) {
						TCComponentGroupMember memberComp = (TCComponentGroupMember) member;
						String user_name = memberComp.getProperty("user");
						users.put(user_name, memberComp.getUid());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		combo.setLayoutData(new GridData (120, SWT.DEFAULT));

		combo.addItem("", null);
		for(Object user : users.keySet().toArray()){
			combo.addItem((String)user, (String)user);
		}
		
		combo.setAutoCompleteSuggestive(false);	
	}
	
	/** ECI 정보 그룹 */
	public void createTopLayout() {
		Composite group = new Composite(mainComposite, SWT.NONE);
		group.setLayout (new GridLayout (8, false));
		
		int default_label_width = 80;
		int default_text_width = 120;
		int default_combo_width = 100;
		
		GridData layoutData;
		
		//#1
		Label lbl_DOC_DIVISION = new Label (group, SWT.RIGHT);
		lbl_DOC_DIVISION.setText (registry.getString("ECISWTRendering.lbl_DOC_DIVISION"));
		layoutData = new GridData (default_label_width, SWT.DEFAULT);
		lbl_DOC_DIVISION.setLayoutData(layoutData);
		
		s7_DOC_DIVISION = new SWTComboBox(group, SWT.BORDER);
		layoutData = new GridData (default_combo_width+25, SWT.DEFAULT);
		s7_DOC_DIVISION.setLayoutData(layoutData);
		comboValueSetting(s7_DOC_DIVISION, "S7_DOC_DIVISION");
		setMadatory(s7_DOC_DIVISION);
		
		Label lbl_TITLE = new Label (group, SWT.RIGHT);
		lbl_TITLE.setText (registry.getString("ECISWTRendering.lbl_TITLE"));
		layoutData = new GridData (default_label_width, SWT.DEFAULT);
		lbl_TITLE.setLayoutData(layoutData);
		
		s7_TITLE = new Text(group, SWT.BORDER);
		layoutData = new GridData (600, SWT.DEFAULT);
		layoutData.horizontalSpan = 5;
		s7_TITLE.setLayoutData(layoutData);
		setMadatory(s7_TITLE);
		
		//#2
		Label lbl_ECO_NO = new Label(group, SWT.RIGHT);
		lbl_ECO_NO.setText(registry.getString("ECISWTRendering.lbl_ECO_NO"));
		layoutData = new GridData (default_label_width, SWT.DEFAULT);
		lbl_ECO_NO.setLayoutData(layoutData);
		
		s7_ECO_NO = new Text(group, SWT.BORDER);
		s7_ECO_NO.setEnabled(false);
		layoutData = new GridData (default_text_width, SWT.DEFAULT);
		s7_ECO_NO.setLayoutData(layoutData);
		setControlSkipEnable(s7_ECO_NO, true);
		
		Label lbl_create_date = new Label(group, SWT.RIGHT);
		lbl_create_date.setText(registry.getString("ECISWTRendering.lbl_create_date"));
		layoutData = new GridData (default_label_width, SWT.DEFAULT);
		lbl_create_date.setLayoutData(layoutData);
		
		creation_date = new Text(group, SWT.BORDER);
		creation_date.setEnabled(false);
		layoutData = new GridData (default_text_width, SWT.DEFAULT);
		creation_date.setLayoutData(layoutData);
		setControlSkipEnable(creation_date, true);
		
		Label lbl_STATUS = new Label(group, SWT.RIGHT);
		lbl_STATUS.setText(registry.getString("ECISWTRendering.lbl_STATUS"));
		layoutData = new GridData (default_combo_width, SWT.DEFAULT);
		lbl_STATUS.setLayoutData(layoutData);
		
		s7_ECI_MATURITY = new SWTComboBox(group, SWT.BORDER);
		s7_ECI_MATURITY.setEnabled(false);
		layoutData = new GridData (default_combo_width+15, SWT.DEFAULT);
		s7_ECI_MATURITY.setLayoutData(layoutData);
		comboValueSetting(s7_ECI_MATURITY, "S7_ECI_MATURITY");
		setControlSkipEnable(s7_ECI_MATURITY, true);
		
		Label lbl_SD_DATE = new Label(group, SWT.RIGHT);
		lbl_SD_DATE.setText(registry.getString("ECISWTRendering.lbl_SD_STATUS"));
		layoutData = new GridData (default_combo_width+20, SWT.DEFAULT);
		lbl_SD_DATE.setLayoutData(layoutData);
		
		s7_ST_DATE = new Text(group, SWT.BORDER);
		s7_ST_DATE.setEnabled(false);
		layoutData = new GridData (default_text_width-10, SWT.DEFAULT);
		s7_ST_DATE.setLayoutData(layoutData);
		setControlSkipEnable(s7_ST_DATE, true);
		
		//#3
		Label lbl_owing_group = new Label(group, SWT.RIGHT);
		lbl_owing_group.setText(registry.getString("ECISWTRendering.lbl_owing_group"));
		layoutData = new GridData (default_label_width, SWT.DEFAULT);
		lbl_owing_group.setLayoutData(layoutData);
		
		owning_group = new Text(group, SWT.BORDER);
		owning_group.setEnabled(false);
		layoutData = new GridData (default_text_width, SWT.DEFAULT);
		owning_group.setLayoutData(layoutData);
		setControlSkipEnable(owning_group, true);

		Label lbl_owing_user = new Label(group, SWT.RIGHT);
		lbl_owing_user.setText(registry.getString("ECISWTRendering.lbl_owing_user"));
		layoutData = new GridData (default_label_width, SWT.DEFAULT);
		lbl_owing_user.setLayoutData(layoutData);
		
		owning_user = new Text(group, SWT.BORDER);
		owning_user.setEnabled(false);
		layoutData = new GridData (default_text_width, SWT.DEFAULT);
		owning_user.setLayoutData(layoutData);
		setControlSkipEnable(owning_user, true);
		
		Label lbl_REPRESENT_VEHICLE = new Label(group, SWT.RIGHT);
		lbl_REPRESENT_VEHICLE.setText(registry.getString("ECISWTRendering.lbl_REPRESENT_VEHICLE"));
		layoutData = new GridData (default_combo_width, SWT.DEFAULT);
		lbl_REPRESENT_VEHICLE.setLayoutData(layoutData);
		
		s7_REPRESENT_VEHICLE = new SWTComboBox(group, SWT.BORDER);
		layoutData = new GridData (default_combo_width+15, SWT.DEFAULT);
		s7_REPRESENT_VEHICLE.setLayoutData(layoutData);
		comboValueSetting(s7_REPRESENT_VEHICLE, "S7_VEHICLE_CODE");
		setMadatory(s7_REPRESENT_VEHICLE);
		s7_REPRESENT_VEHICLE.addPropertyChangeListener(new IPropertyChangeListener() {
	        public void propertyChange(PropertyChangeEvent event) {
				s7_APPLIED_VEHICLE.setSelectedItems(s7_REPRESENT_VEHICLE.getTextField().getText());
			}
		});
		
		Label lbl_APPLIED_VEHICLE = new Label(group, SWT.RIGHT);
		lbl_APPLIED_VEHICLE.setText(registry.getString("ECISWTRendering.lbl_APPLIED_VEHICLE"));
		layoutData = new GridData (default_combo_width+20, SWT.DEFAULT);
		lbl_APPLIED_VEHICLE.setLayoutData(layoutData);
		
		s7_APPLIED_VEHICLE = new SWTComboBox(group, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
		comboValueSetting(s7_APPLIED_VEHICLE, "S7_VEHICLE_CODE");
		layoutData = new GridData (default_combo_width+15, SWT.DEFAULT);
		s7_APPLIED_VEHICLE.setLayoutData(layoutData);
		setMadatory(s7_APPLIED_VEHICLE);
		
		if(isReleased){
			//#4
			Label lbl_LAST_APPROVAL = new Label(group, SWT.RIGHT);
			lbl_LAST_APPROVAL.setText(registry.getString("ECISWTRendering.lbl_LAST_APPROVAL"));
			layoutData = new GridData (default_label_width, SWT.DEFAULT);
			lbl_LAST_APPROVAL.setLayoutData(layoutData);
			
			last_appproval = new Text(group, SWT.BORDER);
			last_appproval.setEnabled(false);
			layoutData = new GridData (default_text_width, SWT.DEFAULT);
			last_appproval.setLayoutData(layoutData);
			setControlSkipEnable(last_appproval, true);

			Label lbl_PS7_APPROVAL_NO = new Label(group, SWT.RIGHT);
			lbl_PS7_APPROVAL_NO.setText(registry.getString("ECISWTRendering.lbl_PS7_APPROVAL_NO"));
			layoutData = new GridData (default_label_width, SWT.DEFAULT);
			lbl_PS7_APPROVAL_NO.setLayoutData(layoutData);
			
			s7_APPROVAL_NO = new Text(group, SWT.BORDER);
			s7_APPROVAL_NO.setEnabled(false);
			layoutData = new GridData (default_text_width, SWT.DEFAULT);
			s7_APPROVAL_NO.setLayoutData(layoutData);
			setControlSkipEnable(s7_APPROVAL_NO, true);
			
			Label lbl_PS7_BP_DATE = new Label(group, SWT.RIGHT);
			lbl_PS7_BP_DATE.setText(registry.getString("ECISWTRendering.lbl_PS7_BP_DATE_"));
			layoutData = new GridData (default_combo_width, SWT.DEFAULT);
			lbl_PS7_BP_DATE.setLayoutData(layoutData);
			
			s7_BP_DATE = new Text(group, SWT.BORDER);
			s7_BP_DATE.setEnabled(false);
			layoutData = new GridData (default_combo_width+10, SWT.DEFAULT);
			s7_BP_DATE.setLayoutData(layoutData);
			setControlSkipEnable(s7_BP_DATE, true);
			
			Label lbl_PS7_BP_SCHEDULE = new Label(group, SWT.RIGHT);
			lbl_PS7_BP_SCHEDULE.setText(registry.getString("ECISWTRendering.lbl_PS7_BP_SCHEDULE"));
			layoutData = new GridData (default_combo_width+20, SWT.DEFAULT);
			lbl_PS7_BP_SCHEDULE.setLayoutData(layoutData);
			
			s7_BP_SCHEDULE = new Text(group, SWT.BORDER);
			s7_BP_SCHEDULE.setEnabled(false);
			layoutData = new GridData (default_combo_width+10, SWT.DEFAULT);
			s7_BP_SCHEDULE.setLayoutData(layoutData);
			setControlSkipEnable(s7_BP_SCHEDULE, true);
		}
		
		Label lSeparator = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		layoutData = new GridData (SWT.FILL, SWT.FILL, true, false);
		layoutData.horizontalSpan = 8;
        lSeparator.setLayoutData(layoutData);
	}
	
	/** ECI Change Parts Info */
	public void createPartInfoLayout() {
		int small_width = 80;
		int mid_width = 120;
		int long_width = 320;
		GridData layoutData;
		
		Composite composite = new Composite(mainComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData (gridFillData);
		
		Group beforeGroup = new Group (composite, SWT.NONE);
		beforeGroup.setLayout (new GridLayout (7, false));
		beforeGroup.setText (registry.getString("ECISWTRendering.GROUP.BeforePart"));
		
		//#1
		Label lbl_BEFORE_PART_NO = new Label (beforeGroup, SWT.RIGHT);
		lbl_BEFORE_PART_NO.setText (registry.getString("ECISWTRendering.lbl_BEFORE_PART_NO"));
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		lbl_BEFORE_PART_NO.setLayoutData(layoutData);
		
		s7_BEFORE_PART_NO = new Text(beforeGroup, SWT.BORDER);
		layoutData = new GridData (small_width, SWT.DEFAULT);
		s7_BEFORE_PART_NO.setLayoutData(layoutData);
		s7_BEFORE_PART_NO.setEnabled(false);
		setMadatory(s7_BEFORE_PART_NO);
		setControlSkipEnable(s7_BEFORE_PART_NO, true);
		
		searchPartButton = new Button(beforeGroup, SWT.NONE);
		searchPartButton.setImage(registry.getImage("Search.ICON"));
		searchPartButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				
				SearchPartDialog popupDialog = new SearchPartDialog(mainComposite.getShell());
				HashMap<String, Object> selectedInfo = popupDialog.open();
				
				if(selectedInfo != null) {
					TableItem[] items = beforePartTable.getItems();
					String compareKey = "";
					String selectedItemKey = "";
					String[] selectedItem = (String[]) selectedInfo.get("rowData");
					for(TableItem item : items){ // 중복 입력 방지 체크
						compareKey = item.getText(0);
						selectedItemKey = selectedItem[0];
						if(compareKey.equals(selectedItemKey))
							return;
					}
					
					s7_BEFORE_PART_NO.setText(selectedItem[0]);
					s7_BEFORE_PART_NAME.setText(selectedItem[1]);
					TCComponentItemRevision itemRevision = (TCComponentItemRevision) selectedInfo.get("tcComponent");
					
					try {
						String[] itemProperties = itemRevision.getProperties(new String[]{"item_id", "object_name", "s7_BUDGET_CODE"});

						TableItem item = new TableItem(beforePartTable, SWT.NONE);
						item.setText(0, itemProperties[0]);
						item.setText(1, itemProperties[1]);
						item.setData("tcComponent", itemRevision);

						isBeforePartsTableModified = true;

						s7_BUDGET_CODE.setText(itemProperties[2]);
					} catch (TCException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		Label lbl_BEFORE_PART_NAME = new Label (beforeGroup, SWT.RIGHT);
		lbl_BEFORE_PART_NAME.setText (registry.getString("ECISWTRendering.lbl_BEFORE_PART_NAME"));
		layoutData = new GridData (small_width, SWT.DEFAULT);
		lbl_BEFORE_PART_NAME.setLayoutData(layoutData);
		
		s7_BEFORE_PART_NAME = new Text(beforeGroup, SWT.BORDER);
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		s7_BEFORE_PART_NAME.setLayoutData(layoutData);
		s7_BEFORE_PART_NAME.setEnabled(false);
		setMadatory(s7_BEFORE_PART_NAME);
		setControlSkipEnable(s7_BEFORE_PART_NAME, true);

		Label lbl_BEFORE_DESC = new Label (beforeGroup, SWT.RIGHT);
		lbl_BEFORE_DESC.setText (registry.getString("ECISWTRendering.lbl_BEFORE_DESC"));
		layoutData = new GridData (small_width, SWT.DEFAULT);
		lbl_BEFORE_DESC.setLayoutData(layoutData);

		s7_BEFORE_DESC = new Text(beforeGroup, SWT.BORDER);
		layoutData = new GridData (long_width+10, SWT.DEFAULT);
		s7_BEFORE_DESC.setLayoutData(layoutData);
		
		GridLayout flayout = new GridLayout (3, false);
		flayout.marginWidth = 0;
		flayout.marginHeight = 0;
		Composite btComposite = new Composite(beforeGroup, SWT.NONE);
		btComposite.setLayout(flayout);
		layoutData = new GridData (SWT.FILL, SWT.CENTER, true, true);
		layoutData.horizontalSpan = 7;
		btComposite.setLayoutData(layoutData);
		
		Label lbl_blank = new Label(btComposite, SWT.RIGHT);
		layoutData = new GridData (SWT.FILL, SWT.CENTER, true, true);
		lbl_blank.setLayoutData(layoutData);
		
		addBeforePartButton = new Button(btComposite, SWT.PUSH);
		addBeforePartButton.setText("Add");
		addBeforePartButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addBeforePart();
			}
		});
		
		deleteBeforePartButton = new Button(btComposite, SWT.PUSH);
		deleteBeforePartButton.setText("Delete");
		deleteBeforePartButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteBeforePart();
			}
		});
		
		beforePartTable = new Table(beforeGroup, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		beforePartTable.setHeaderVisible(true);
		beforePartTable.setLinesVisible(true);
		layoutData = new GridData (SWT.FILL, SWT.CENTER, true, true);
		layoutData.minimumHeight = 100;
		layoutData.horizontalSpan = 7;
		beforePartTable.setLayoutData(layoutData);
		
		int i = 0;
		beforePartTableHeader = new String[]{"Part NO.", "Part Name", "Description"};
		beforePartTableHeaderSize = new int[]{80, 220, 300};
		for(String value : beforePartTableHeader){
			TableColumn column = new TableColumn(beforePartTable, SWT.NONE);
			column.setText(value);
			column.setWidth(beforePartTableHeaderSize[i]);
			i++;
		}

		Group afterGroup = new Group (composite, SWT.NONE);
		afterGroup.setLayout (new GridLayout (6, false));
		afterGroup.setText (registry.getString("ECISWTRendering.GROUP.AfterPart"));

		//#1
		Label lbl_AFTER_PART_NO = new Label (afterGroup, SWT.RIGHT);
		lbl_AFTER_PART_NO.setText (registry.getString("ECISWTRendering.lbl_AFTER_PART_NO"));
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		lbl_AFTER_PART_NO.setLayoutData(layoutData);

		s7_AFTER_PART_NO = new Text(afterGroup, SWT.BORDER);
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		s7_AFTER_PART_NO.setLayoutData(layoutData);

		Label lbl_AFTER_PART_NAME = new Label (afterGroup, SWT.RIGHT);
		lbl_AFTER_PART_NAME.setText (registry.getString("ECISWTRendering.lbl_AFTER_PART_NAME"));
		layoutData = new GridData (small_width, SWT.DEFAULT);
		lbl_AFTER_PART_NAME.setLayoutData(layoutData);

		s7_AFTER_PART_NAME = new Text(afterGroup, SWT.BORDER);
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		s7_AFTER_PART_NAME.setLayoutData(layoutData);

		Label lbl_AFTER_DESC = new Label (afterGroup, SWT.RIGHT);
		lbl_AFTER_DESC.setText (registry.getString("ECISWTRendering.lbl_AFTER_DESC"));
		layoutData = new GridData (small_width, SWT.DEFAULT);
		lbl_AFTER_DESC.setLayoutData(layoutData);

		s7_AFTER_DESC = new Text(afterGroup, SWT.BORDER);
		layoutData = new GridData (long_width+2, SWT.DEFAULT);
		s7_AFTER_DESC.setLayoutData(layoutData);
	}
	
	/**
	 * ECI 상세 정보 화면 생성
	 */
	public void createCenterLayout() {
		
		int short_combo_width = 60;
		int short_width = 95;
		int default_width = 120;
		int mid_width = 180;
		
		int bt_width = 248;
		int long_width = 280;
		
		Label lSeparator = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData layoutData = new GridData (SWT.FILL, SWT.FILL, true, false);
        lSeparator.setLayoutData(layoutData);
		
		Composite upperComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout layout = new GridLayout (9, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		upperComposite.setLayout (layout);
		
		Label lbl_EXP_INVEST = new Label (upperComposite, SWT.RIGHT);
		lbl_EXP_INVEST.setText (registry.getString("ECISWTRendering.lbl_EXP_INVEST"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_EXP_INVEST.setLayoutData(layoutData);
		
		s7_EXP_INVEST = new Text(upperComposite, SWT.BORDER | SWT.RIGHT);
		layoutData = new GridData (short_width, SWT.DEFAULT);
		s7_EXP_INVEST.setLayoutData(layoutData);
		s7_EXP_INVEST.addVerifyListener(new SYMCSYMCVerifyListener());
		setMadatory(s7_EXP_INVEST);
		
		Label lbl_thousnand_won = new Label (upperComposite, SWT.LEFT);
		lbl_thousnand_won.setText (registry.getString("ECISWTRendering.lbl_thousnand_won"));
		layoutData = new GridData (short_width, SWT.DEFAULT);
		lbl_thousnand_won.setLayoutData(layoutData);
		
		Label lbl_CHANGE_CODE = new Label (upperComposite, SWT.RIGHT);
		lbl_CHANGE_CODE.setText (registry.getString("ECISWTRendering.lbl_CHANGE_CODE"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_CHANGE_CODE.setLayoutData(layoutData);
		
		s7_CHANGE_COST = new Text(upperComposite, SWT.BORDER | SWT.RIGHT);
		layoutData = new GridData (short_width, SWT.DEFAULT);
		s7_CHANGE_COST.setLayoutData(layoutData);
		s7_CHANGE_COST.addVerifyListener(new SYMCSYMCVerifyListener());
		setMadatory(s7_CHANGE_COST);
		
		Label lbl_won = new Label (upperComposite, SWT.LEFT);
		lbl_won.setText (registry.getString("ECISWTRendering.lbl_won"));
		layoutData = new GridData (short_width, SWT.DEFAULT);
		lbl_won.setLayoutData(layoutData);
		
		up = new Button (upperComposite, SWT.RADIO);
		up.setText(registry.getString("ECISWTRendering.up"));
		up.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String cCost = s7_CHANGE_COST.getText();
				if(cCost.startsWith("-")){
					cCost = cCost.replace("-", "");
					s7_CHANGE_COST.setText(cCost);
				}
			}
		});
		
		down = new Button (upperComposite, SWT.RADIO);
		down.setText(registry.getString("ECISWTRendering.down"));
		down.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String cCost = s7_CHANGE_COST.getText();
				if(!cCost.startsWith("-")){
					cCost = "-" + cCost;
					s7_CHANGE_COST.setText(cCost);
				}
			}
		});
		
		same = new Button (upperComposite, SWT.RADIO);
		same.setText(registry.getString("ECISWTRendering.same"));
		same.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				s7_CHANGE_COST.setText("0");
			}
		});
		
		Label lbl_SYSTEM_CODE = new Label (upperComposite, SWT.RIGHT);
		lbl_SYSTEM_CODE.setText (registry.getString("ECISWTRendering.lbl_SYSTEM_CODE"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_SYSTEM_CODE.setLayoutData(layoutData);
		
		s7_BUDGET_CODE = new Text(upperComposite, SWT.BORDER);
		layoutData = new GridData (short_width, SWT.DEFAULT);
		layoutData.horizontalSpan = 2;
		s7_BUDGET_CODE.setLayoutData(layoutData);
		s7_BUDGET_CODE.setEnabled(false);
		s7_BUDGET_CODE.setToolTipText("This field is setted autimatic by selecting before part property.");
		setMadatory(s7_BUDGET_CODE);
		setControlSkipEnable(s7_BUDGET_CODE, true);
		
		Label lbl_US = new Label (upperComposite, SWT.RIGHT);
		lbl_US.setText (registry.getString("ECISWTRendering.lbl_US"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_US.setLayoutData(layoutData);
		
		s7_US = new Text(upperComposite, SWT.BORDER | SWT.RIGHT);
		layoutData = new GridData (short_width, SWT.DEFAULT);
		s7_US.setTextLimit(2);
		s7_US.setLayoutData(layoutData);
		s7_US.addVerifyListener(new SYMCSYMCVerifyListener());
		setMadatory(s7_US);
		
		Label lbl_ea = new Label (upperComposite, SWT.LEFT);
		lbl_ea.setText (registry.getString("(EA)"));
		layoutData = new GridData (short_width, SWT.DEFAULT);
		lbl_ea.setLayoutData(layoutData);
		
		Label lbl_SUPPLIER = new Label (upperComposite, SWT.RIGHT);
		lbl_SUPPLIER.setText (registry.getString("ECISWTRendering.lbl_SUPPLIER"));
		layoutData = new GridData (short_width, SWT.DEFAULT);
		lbl_SUPPLIER.setLayoutData(layoutData);
		
		s7_SUPPLIER = new Text(upperComposite, SWT.BORDER);
		layoutData = new GridData (115, SWT.DEFAULT);
		layoutData.horizontalSpan = 2;
		s7_SUPPLIER.setLayoutData(layoutData);
		setMadatory(s7_SUPPLIER);
		
		//##
		Composite midComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout (10, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		midComposite.setLayout (layout);
		
		Label lbl_AS_COMPATIBLE = new Label (midComposite, SWT.RIGHT);
		lbl_AS_COMPATIBLE.setText (registry.getString("ECISWTRendering.lbl_AS_COMPATIBLE"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_AS_COMPATIBLE.setLayoutData(layoutData);
		
		s7_AS_COMPATIBLE = new SWTComboBox(midComposite, SWT.BORDER);
		layoutData = new GridData (short_combo_width+40, SWT.DEFAULT);
		s7_AS_COMPATIBLE.setLayoutData(layoutData);
		comboValueSetting(s7_AS_COMPATIBLE, "S7_AS_COMPATIBLE");
		setMadatory(s7_AS_COMPATIBLE);
		
		Label lbl_SUPPLY_REQ_YN = new Label (midComposite, SWT.RIGHT);
		lbl_SUPPLY_REQ_YN.setText (registry.getString("ECISWTRendering.lbl_SUPPLY_REQ_YN"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_SUPPLY_REQ_YN.setLayoutData(layoutData);
		
		s7_SUPPLY_REQ_YN = new SWTComboBox(midComposite, SWT.BORDER);
		layoutData = new GridData (short_combo_width+20, SWT.DEFAULT);
		comboValueSetting(s7_SUPPLY_REQ_YN, "S7_YN");
		s7_SUPPLY_REQ_YN.setLayoutData(layoutData);
		
		Label lbl_SUBSTITUDE = new Label (midComposite, SWT.RIGHT);
		lbl_SUBSTITUDE.setText (registry.getString("ECISWTRendering.lbl_SUBSTITUDE"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_SUBSTITUDE.setLayoutData(layoutData);
		
		s7_SUBSTITUDE_PART = new SWTComboBox(midComposite, SWT.BORDER);
		layoutData = new GridData (short_combo_width+20, SWT.DEFAULT);
		comboValueSetting(s7_SUBSTITUDE_PART, "S7_YN");
		s7_SUBSTITUDE_PART.setLayoutData(layoutData);
		
		Label lbl_SUBSTITUDE_PART = new Label (midComposite, SWT.RIGHT);
		lbl_SUBSTITUDE_PART.setText (registry.getString("ECISWTRendering.lbl_SUBSTITUDE_PART"));
		layoutData = new GridData (45, SWT.DEFAULT);
		lbl_SUBSTITUDE_PART.setLayoutData(layoutData);
		
		s7_REF_FILE = new Text(midComposite, SWT.BORDER); // 조심하세요[대체 파트 번호임]
		layoutData = new GridData (140, SWT.DEFAULT);
		s7_REF_FILE.setLayoutData(layoutData);
		s7_REF_FILE.setEnabled(false);
		setControlSkipEnable(s7_REF_FILE, true);
		
		searchSubstitudePartButton = new Button(midComposite, SWT.NONE);
		searchSubstitudePartButton.setImage(registry.getImage("Search.ICON"));
		searchSubstitudePartButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				SearchPartDialog popupDialog = new SearchPartDialog(mainComposite.getShell());
				HashMap<String, Object> selectedInfo = popupDialog.open();
				
				if(selectedInfo != null) {
					String[] selectedItem = (String[]) selectedInfo.get("rowData");
					s7_REF_FILE.setText(selectedItem[0]);
				}
			}
		});
		
		//##
		Composite underComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout (6, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		underComposite.setLayout (layout);
		
		Label lbl_REVIEW_DEPT = new Label (underComposite, SWT.RIGHT);
		lbl_REVIEW_DEPT.setText (registry.getString("ECISWTRendering.lbl_REVIEW_DEPT"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_REVIEW_DEPT.setLayoutData(layoutData);
		
		s7_REVIEW_DEPT = new Text(underComposite, SWT.BORDER);
		layoutData = new GridData (bt_width, SWT.DEFAULT);
		s7_REVIEW_DEPT.setLayoutData(layoutData);
		s7_REVIEW_DEPT.setEnabled(false);
		setMadatory(s7_REVIEW_DEPT);
		setControlSkipEnable(s7_REVIEW_DEPT, true);
		
		searchReviewteamButton = new Button(underComposite, SWT.NONE);
		searchReviewteamButton.setImage(registry.getImage("Search.ICON"));
		searchReviewteamButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				SYMCTeamListDialog popupDialog = new SYMCTeamListDialog(mainComposite.getShell(), s7_REVIEW_DEPT_CODE);
				popupDialog.open();
				if(popupDialog != null){
					if(popupDialog.getSelectedTeams() != null && !popupDialog.getSelectedTeams().equals("")){
						s7_REVIEW_DEPT.setText(popupDialog.getSelectedTeams());
						s7_REVIEW_DEPT_CODE = popupDialog.getSelectedTeamCodes();
					}
				}
			}
		});
		
		Label lbl_LAW_CHECK = new Label (underComposite, SWT.RIGHT);
		lbl_LAW_CHECK.setText (registry.getString("ECISWTRendering.lbl_LAW_CHECK"));
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		lbl_LAW_CHECK.setLayoutData(layoutData);
		
		s7_LAW_CHECK = new Text(underComposite, SWT.BORDER);
		layoutData = new GridData (bt_width, SWT.DEFAULT);
		s7_LAW_CHECK.setLayoutData(layoutData);
		s7_LAW_CHECK.setEnabled(false);
		setMadatory(s7_LAW_CHECK);
		setControlSkipEnable(s7_LAW_CHECK, true);
		
		searchLawCheckButton = new Button(underComposite, SWT.NONE);
		searchLawCheckButton.setImage(registry.getImage("Search.ICON"));
		searchLawCheckButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				LawCheckListDialog popupDialog = new LawCheckListDialog(mainComposite.getShell(), s7_LAW_CHECK.getText());
				popupDialog.open();
				if(popupDialog != null){
					s7_LAW_CHECK.setText(popupDialog.getLawChecks());
				}
			}
		});
		
		Label lbl_ECR_NO = new Label (underComposite, SWT.RIGHT);
		lbl_ECR_NO.setText (registry.getString("ECISWTRendering.lbl_ECR_NO"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_ECR_NO.setLayoutData(layoutData);
		
		s7_ECR_NO = new Text(underComposite, SWT.BORDER);
		layoutData = new GridData (bt_width, SWT.DEFAULT);
		s7_ECR_NO.setLayoutData(layoutData);
		s7_ECR_NO.setEditable(false);
		setControlSkipEnable(s7_ECR_NO, true);
		
		Button searchReviewteamButton = new Button(underComposite, SWT.NONE);
		searchReviewteamButton.setImage(registry.getImage("Search.ICON"));
		searchReviewteamButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				SearchECRDialog popupDialog = new SearchECRDialog(mainComposite.getShell(), SWT.NONE);
				if(popupDialog.open() != SWT.CANCEL){
					// [SR140701-022] jclee, Related ECR을 가져올 때 System 등록 코드가 아니라 ECR 번호를 가져오도록 변경. 
					String seqNo = popupDialog.getECRRegNo();
					if(seqNo != null)
						s7_ECR_NO.setText(popupDialog.getECRRegNo());
				}
			}
		});
		
		Label lbl_RELATION_ID = new Label (underComposite, SWT.RIGHT);
		lbl_RELATION_ID.setText (registry.getString("ECISWTRendering.lbl_RELATION_ID"));
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		lbl_RELATION_ID.setLayoutData(layoutData);
		
		s7_RELATION_ID = new Text(underComposite, SWT.BORDER);
		layoutData = new GridData (long_width, SWT.DEFAULT);
		layoutData.horizontalSpan = 2;
		s7_RELATION_ID.setLayoutData(layoutData);
		
		Label lbl_BASE_ON = new Label (underComposite, SWT.RIGHT);
		lbl_BASE_ON.setText (registry.getString("ECISWTRendering.lbl_BASE_ON"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_BASE_ON.setLayoutData(layoutData);
		
		s7_BASE_ON = new Text(underComposite, SWT.BORDER);
		layoutData = new GridData (long_width, SWT.DEFAULT);
		layoutData.horizontalSpan = 2;
		s7_BASE_ON.setLayoutData(layoutData);
		setMadatory(s7_BASE_ON);
		
		Label lbl_REASON = new Label (underComposite, SWT.RIGHT);
		lbl_REASON.setText (registry.getString("ECISWTRendering.lbl_REASON"));
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		lbl_REASON.setLayoutData(layoutData);
		
		s7_CHANGE_REASON = new SWTComboBox(underComposite, SWT.BORDER);
		layoutData = new GridData (long_width+5, SWT.DEFAULT);
		layoutData.horizontalSpan = 2;
		s7_CHANGE_REASON.setLayoutData(layoutData);
		comboValueSetting(s7_CHANGE_REASON, "S7_ECO_REASON");
		setMadatory(s7_CHANGE_REASON);
		
		Label lbl_EC_MANAGE_CODE = new Label (underComposite, SWT.RIGHT);
		lbl_EC_MANAGE_CODE.setText (registry.getString("ECISWTRendering.lbl_EC_MANAGE_CODE"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_EC_MANAGE_CODE.setLayoutData(layoutData);
		
		s7_EC_MANAGE_CODE = new SWTComboBox(underComposite, SWT.BORDER);
		layoutData = new GridData (long_width+5, SWT.DEFAULT);
		layoutData.horizontalSpan = 2;
		s7_EC_MANAGE_CODE.setLayoutData(layoutData);
		comboValueSetting(s7_EC_MANAGE_CODE, "S7_EC_MANAGE_CODE");
		setMadatory(s7_EC_MANAGE_CODE);
		
		Label lbl_REASON_OFFER_CODE = new Label (underComposite, SWT.RIGHT);
		lbl_REASON_OFFER_CODE.setText (registry.getString("ECISWTRendering.lbl_REASON_OFFER_CODE"));
		layoutData = new GridData (mid_width, SWT.DEFAULT);
		lbl_REASON_OFFER_CODE.setLayoutData(layoutData);
		
		s7_REASON_OFFER_CODE = new SWTComboBox(underComposite, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
		comboValueSetting(s7_REASON_OFFER_CODE, "S7_REASON_OFFER_CODE");
		layoutData = new GridData (long_width+5, SWT.DEFAULT);
		layoutData.horizontalSpan = 2;
		s7_REASON_OFFER_CODE.setLayoutData(layoutData);
		setMadatory(s7_REASON_OFFER_CODE);
		
		Label lbl_REASON_OFFER_DESC = new Label (underComposite, SWT.RIGHT);
		lbl_REASON_OFFER_DESC.setText (registry.getString("ECISWTRendering.lbl_REASON_OFFER_DESC"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_REASON_OFFER_DESC.setLayoutData(layoutData);
		
		s7_REASON_OFFER_DESC = new Text(underComposite, SWT.BORDER);
		layoutData = new GridData (760, SWT.DEFAULT);
		layoutData.horizontalSpan = 5;
		s7_REASON_OFFER_DESC.setLayoutData(layoutData);
		setMadatory(s7_REASON_OFFER_DESC);

		Composite fileComp = new Composite(underComposite, SWT.NONE);
		layout = new GridLayout (4, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		fileComp.setLayout(layout);
		layoutData = new GridData (SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 6;
		fileComp.setLayoutData(layoutData);
		Label lbl_REF_FILE = new Label (fileComp, SWT.RIGHT);
		lbl_REF_FILE.setText (registry.getString("ECISWTRendering.lbl_REF_FILE"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_REF_FILE.setLayoutData(layoutData);
		
		s7_REF_FILE_PATH = new Text(fileComp, SWT.BORDER);
		layoutData = new GridData (655, SWT.DEFAULT);
		s7_REF_FILE_PATH.setLayoutData(layoutData);
		s7_REF_FILE_PATH.setEnabled(false);
		
		downloadFileButton = new Button(fileComp, SWT.NONE);
		downloadFileButton.setText("Download");
		setControlSkipEnable(downloadFileButton, true);
		downloadFileButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				download();
			}
		});
		
		searchFileButton = new Button(fileComp, SWT.NONE);
		searchFileButton.setImage(registry.getImage("Search.ICON"));
		searchFileButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				openFile();
			}
		});
		
		Label lbl_REF_DEPT = new Label (underComposite, SWT.RIGHT);
		lbl_REF_DEPT.setText (registry.getString("ECISWTRendering.lbl_REF_DEPT"));
		layoutData = new GridData (default_width, SWT.DEFAULT);
		lbl_REF_DEPT.setLayoutData(layoutData);
		
		s7_REF_DEPT = new Text(underComposite, SWT.BORDER);
		layoutData = new GridData (728, SWT.DEFAULT);
		layoutData.horizontalSpan = 4;
		s7_REF_DEPT.setEnabled(false);
		s7_REF_DEPT.setLayoutData(layoutData);
		
		searchReferenceTeamButton = new Button(underComposite, SWT.NONE);
		searchReferenceTeamButton.setImage(registry.getImage("Search.ICON"));
		searchReferenceTeamButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				SYMCTeamListDialog popupDialog = new SYMCTeamListDialog(mainComposite.getShell(), s7_REF_DEPT_CODE);
				popupDialog.open();
				if(popupDialog != null){
					if(popupDialog.getSelectedTeamCodes() != null && !popupDialog.getSelectedTeamCodes().equals("")){
						s7_REF_DEPT.setText(popupDialog.getSelectedTeams());
						s7_REF_DEPT_CODE = popupDialog.getSelectedTeamCodes();
					}
				}
			}
		});
		
//		Label lbl_WORKFLOW = new Label (underComposite, SWT.RIGHT);
//		lbl_WORKFLOW.setText (registry.getString("ECISWTRendering.lbl_WORKFLOW"));
//		layoutData = new GridData (default_width, SWT.DEFAULT);
//		lbl_WORKFLOW.setLayoutData(layoutData);
//		
//		s7_WORKFLOW = new Text(underComposite, SWT.BORDER);
//		layoutData = new GridData (728, SWT.DEFAULT);
//		layoutData.horizontalSpan = 4;
//		s7_WORKFLOW.setEnabled(false);
//		s7_WORKFLOW.setLayoutData(layoutData);
//		
//		searchUserButton = new Button(underComposite, SWT.NONE);
//		searchUserButton.setImage(registry.getImage("Search.ICON"));
//		searchUserButton.addSelectionListener(new SelectionAdapter () {
//			public void widgetSelected(SelectionEvent e) {
//				SearchMemberDialog popupDialog = new SearchMemberDialog(getShell());
//				HashMap<String, Object> selectedInfo = popupDialog.open();
//				String[] selectedItem = (String[]) selectedInfo.get("rowData");
//				s7_WORKFLOW.setText(selectedItem[1]+"("+(String) selectedInfo.get("puid")+")");
//			}
//		});
		
	}

	/** 
	 * 화면 콘트롤과 속성 값 맵 생성 및 
	 * 선택한 파트의 속성을 파트 변경 전후 화면의 변경전으로 자동으로 입력
	 */
	private void initData() {
		if(selectedItemRevision != null && selectedItemRevision.getType().startsWith("S7_Veh")){
			try {
				String[] itemProperties = selectedItemRevision.getProperties(new String[]{"item_id", "object_name", "s7_BUDGET_CODE"});
				s7_BEFORE_PART_NO.setText(itemProperties[0]);
				s7_BEFORE_PART_NAME.setText(itemProperties[1]);
				s7_BUDGET_CODE.setText(itemProperties[2]);
				
				TableItem item = new TableItem(beforePartTable, SWT.NONE);
				item.setText(0, itemProperties[0]);
				item.setText(1, itemProperties[1]);
				item.setData("tcComponent", selectedItemRevision);
				
				isBeforePartsTableModified = true;
				
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
//		if(this.eciRevision != null)
//			s7_MATURITY.setText("A");
		
		eciInfoNControlMap.put("s7_DOC_DIVISION", s7_DOC_DIVISION);
		eciInfoNControlMap.put("s7_TITLE", s7_TITLE);
		eciInfoNControlMap.put("s7_ECI_MATURITY", s7_ECI_MATURITY);
		eciInfoNControlMap.put("s7_REPRESENT_VEHICLE", s7_REPRESENT_VEHICLE);
		eciInfoNControlMap.put("s7_APPLIED_VEHICLE", s7_APPLIED_VEHICLE);
		eciInfoNControlMap.put("s7_BEFORE_PART_NO", s7_BEFORE_PART_NO);
		eciInfoNControlMap.put("s7_BEFORE_PART_NAME", s7_BEFORE_PART_NAME);
		eciInfoNControlMap.put("s7_BEFORE_DESC", s7_BEFORE_DESC);
		eciInfoNControlMap.put("s7_AFTER_PART_NO", s7_AFTER_PART_NO);
		eciInfoNControlMap.put("s7_AFTER_PART_NAME", s7_AFTER_PART_NAME);
		eciInfoNControlMap.put("s7_AFTER_DESC", s7_AFTER_DESC);
		eciInfoNControlMap.put("s7_EXP_INVEST", s7_EXP_INVEST);
		eciInfoNControlMap.put("s7_CHANGE_COST", s7_CHANGE_COST);
		eciInfoNControlMap.put("s7_COST_SIGN", up);
		eciInfoNControlMap.put("s7_BUDGET_CODE", s7_BUDGET_CODE);
		eciInfoNControlMap.put("s7_US", s7_US);
		eciInfoNControlMap.put("s7_SUPPLIER", s7_SUPPLIER);
		eciInfoNControlMap.put("s7_AS_COMPATIBLE", s7_AS_COMPATIBLE);
		eciInfoNControlMap.put("s7_SUPPLY_REQ_YN", s7_SUPPLY_REQ_YN);
		eciInfoNControlMap.put("s7_SUBSTITUDE_PART", s7_SUBSTITUDE_PART);
		eciInfoNControlMap.put("s7_REVIEW_DEPT", s7_REVIEW_DEPT);
		eciInfoNControlMap.put("s7_LAW_CHECK", s7_LAW_CHECK);
		eciInfoNControlMap.put("s7_ECR_NO", s7_ECR_NO);
		eciInfoNControlMap.put("s7_RELATION_ID", s7_RELATION_ID);
		eciInfoNControlMap.put("s7_BASE_ON", s7_BASE_ON);
		eciInfoNControlMap.put("s7_REASON_OFFER_DESC", s7_REASON_OFFER_DESC);
		eciInfoNControlMap.put("s7_REF_FILE", s7_REF_FILE);
		eciInfoNControlMap.put("s7_REF_FILE_PATH", s7_REF_FILE_PATH);
		eciInfoNControlMap.put("s7_REF_DEPT", s7_REF_DEPT);
		eciInfoNControlMap.put("s7_CHANGE_REASON", s7_CHANGE_REASON);
		eciInfoNControlMap.put("s7_EC_MANAGE_CODE", s7_EC_MANAGE_CODE);
		eciInfoNControlMap.put("s7_REASON_OFFER_CODE", s7_REASON_OFFER_CODE);
	}
	
	private void addBeforePart(){
		SearchPartDialog popupDialog = new SearchPartDialog(mainComposite.getShell());
		HashMap<String, Object> selectedInfo = popupDialog.open();
		
		if (selectedInfo != null) {
			TableItem[] items = beforePartTable.getItems();
			String compareKey = "";
			String selectedItemKey = "";
			String[] selectedItem = (String[]) selectedInfo.get("rowData");
			for(TableItem item : items){ // 중복 입력 방지 체크
				compareKey = item.getText(0);
				selectedItemKey = selectedItem[0];
				if(compareKey.equals(selectedItemKey))
					return;
			}
			TableItem item = new TableItem(beforePartTable, SWT.NONE);
			item.setText(0, selectedItem[0]);
			item.setText(1, selectedItem[1]);
			item.setData("tcComponent", (TCComponentItemRevision) selectedInfo.get("tcComponent"));
			
			isBeforePartsTableModified = true;
		}
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void deleteBeforePart(){
		TableItem[] items = beforePartTable.getSelection();
		if (items.length == 0) return;
		if(items[0].getText(0).equals(s7_BEFORE_PART_NO.getText())){
			s7_BEFORE_PART_NO.setText("");
			s7_BEFORE_PART_NAME.setText("");
			s7_BUDGET_CODE.setText("");
		}

		items[0].dispose();

		isBeforePartsTableModified = true;
	}

	/** main Composite 반환 */
	public Composite getComposite() {
		return this;
	}

	/** 화면 속성 맵 생성 */
	public HashMap<String, String> getParamMap(){
		HashMap<String, String> paramMap = new HashMap<String, String>();
		String value = "";
		for(Object property : eciInfoNControlMap.keySet().toArray()){
			value = "";
			if(eciInfoNControlMap.get(property) instanceof Text){
				Text con = (Text) eciInfoNControlMap.get(property);
				if(property.equals("s7_REVIEW_DEPT")){
					value = s7_REVIEW_DEPT_CODE;
				}else if(property.equals("s7_REF_DEPT")){
					value = s7_REF_DEPT_CODE;
				}else{
					value = con.getText();
				}
			}else if(eciInfoNControlMap.get(property) instanceof SWTComboBox){
				SWTComboBox con = (SWTComboBox) eciInfoNControlMap.get(property);
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
			}else if(eciInfoNControlMap.get(property) instanceof Button){
				Button con = (Button) eciInfoNControlMap.get(property);
				if(con.getSelection())
					value = "Y";
				else
					value = "N";
			}else if(eciInfoNControlMap.get(property) instanceof SYMCDateTimeButton){
				SYMCDateTimeButton con = (SYMCDateTimeButton) eciInfoNControlMap.get(property);
				value = con.getTCDate(session);
			}
			paramMap.put((String)property, value);
		}
		return paramMap;
	}
	
	private String getTeamNames(String codes) throws Exception{
		StringBuffer addInfo = new StringBuffer();
		VnetTeamInfoData data = new VnetTeamInfoData();
		data.setCodeList(codes.split(SYMCECConstant.SEPERATOR));

		ArrayList<VnetTeamInfoData> teamInfoDataList = dao.getVnetTeamNames(data);
		if(teamInfoDataList != null)
			for(VnetTeamInfoData teamInfoData : teamInfoDataList){
				if(addInfo.length() > 0) {
					addInfo.append(SYMCECConstant.SEPERATOR);
				}
				addInfo.append(teamInfoData.getTeam_name());
			}
		return addInfo.toString();
	}
	
	/** ECI 생성 */
	public void create(){
		session.setStatus("Create ECI...");
		String message = validationForSave();
		if(message.equals("") || message == null){
			TCComponentItemType itemType;
			Markpoint mp = null;
			try {
				mp = new Markpoint(session);
				itemType = (TCComponentItemType)session.getTypeComponent("EngChange");
				String eciID =  "ECI-" + itemType.getNewID();
				TCComponentItem item = itemType.create(eciID, SYMCECConstant.EC_REV_ID, SYMCECConstant.ECITYPE, eciID, "", null);
				eciRevision = (TCComponentChangeItemRevision) item.getLatestItemRevision();

				session.getUser().getNewStuffFolder().add("contents", item);
				
			    NavigatorOpenService openService = new NavigatorOpenService();
			    openService.open(eciRevision);
				mp.forget();
			} catch (TCException e) {
				e.printStackTrace();
				try { 
					mp.rollBack();
				} catch (TCException e1) {	
					e1.printStackTrace();
				}
				MessageBox.post(mainComposite.getShell(), e.toString(), "ERROR", MessageBox.ERROR);
			} finally {
				session.setReadyStatus();
			}
		}else{
			MessageBox.post(mainComposite.getShell(), message, "ERROR", MessageBox.ERROR);
		}
	}

	/**
	 * FTP에서 파일 다운로드
	 * 저장 위치 지정 및 열기 여부 확인
	 */
	private void download() {
		String theFileName = s7_REF_FILE_PATH.getText();
		if(theFileName.equals("")){
			MessageBox.post("File not Exist", "Fine download", MessageBox.INFORMATION);
			return;
		}
		
		FileDialog saveDialog = new FileDialog(getShell(), SWT.SAVE);
		saveDialog.setFileName(theFileName);
		
		if(saveDialog.open() != null){
			String name = saveDialog.getFileName();
			if(name.equals("")) return;

			File downFile = new File(saveDialog.getFilterPath(), name);
			if(downFile.exists()) {
				org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				box.setText("Already Exist");
				box.setMessage(downFile.getName() + " is already exist!\nDo you overwrite the file?");
				if(box.open() != SWT.YES) {
					return;
				}
			}

			try{
				FTPConnection ftp = new FTPConnection();
				String fileName = "";
				String eciItemPuid = eciRevision.getItem().getUid();
				if(ProcessUtil.isReleased(eciRevision)){
					HashMap<String, String> map = dao.getECIfileInfo(eciItemPuid);
					ftp.download(downFile, map.get("FILEPATH"), map.get("FILENAME"));
				}else{
					fileName = eciItemPuid;
					ftp.download(downFile, null, fileName);
				}
				ftp.disconnect();
				
				org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.YES | SWT.NO);
				box.setText("Open");
				box.setMessage("Do you want to open the file?");
				if(box.open() == SWT.YES) {
					openSavedFile(downFile);
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public TCComponentChangeItemRevision getECIRevision() {
		return eciRevision;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isModified() {
		if(!eciRevision.isCheckedOut()) return false;
		HashMap<String, String> paramMap = getParamMap();
		String property = "";
		String param = "";
		for(Object key : eciPropertyMap.keySet().toArray()){
			if(key.equals("s7_WORKFLOW")){
				s7_WORKFLOW = "";
				if(setForSavingWorkflow(combo1) != null)
					if(setForSavingWorkflow(combo2) != null)
						if(setForSavingWorkflow(combo3) != null)
							setForSavingWorkflow(combo4);
				param = s7_WORKFLOW;
			}else{
				param = paramMap.get(key);
			}
			if(param == null) param = "";
			property = eciPropertyMap.get(key);
			if(property == null) property = "";
			if(!param.equals(property)){
				return true;
			}
		}
		
		if(isBeforePartsTableModified) return true;
		
		return false;
	}

	@Override
	public void load() {
		
	}
	
	private void openFile() {	
		FileDialog fileDialog = new FileDialog(mainComposite.getShell(), SWT.OPEN);

		fileDialog.setFilterExtensions(new String[] {"*.xls;", "*.*"});
		fileDialog.setFilterNames(new String[] {"Excel (*.xls)", "All Files (*.*)"});
		String name = fileDialog.open();

		if(name == null) return;
		file = new File(name);
		if (!file.exists()) {
			MessageBox.post(mainComposite.getShell(), "File "+file.getName()+" "+" Does_not_exist", "ERROR", MessageBox.ERROR);
			return;
		}
		
		s7_REF_FILE_PATH.setText(file.getName());
	}
	
	private void openSavedFile(File file){
		final File downFile = file;
		Thread thread = new Thread(new Runnable(){
			public void run(){
				try{
					String[] commandString = {"CMD", "/C", downFile.getPath()};
					com.teamcenter.rac.util.Shell ishell = new com.teamcenter.rac.util.Shell(commandString);
					ishell.run();
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}
	
	private String readSignoffTaskProfile(TCComponentTask approveTask) throws TCException {
		String signoffUsers = "";
        TCComponentTask performSignoffTask = approveTask.getSubtask("perform-signoffs");
        TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();
        if(signoffs.length > 0) {
            for(TCComponentSignoff signoff : signoffs) {
            	signoff.refresh();
            	TCComponentGroupMember groupMember = signoff.getGroupMember();
            	signoffUsers = signoffUsers + groupMember.getUserId();
            }
        }
        return signoffUsers;
    }
	
	/** 저장 */
	public void save(){
		String message = validationForSave();
		if(message.equals("") || message == null){
			try {
				// 속성 저장
				eciRevision.setProperties(getParamMap());
				
				eciRevision.setProperty("s7_ECI_MATURITY", "A");
				
				// 문제 아이템 로드
				if(isBeforePartsTableModified){
					saveProblemItems();
				}
				
				// 파일 관리
				String fileTextName = s7_REF_FILE_PATH.getText().trim();
				if(fileTextName != null && !fileTextName.equals("")){
					if (file != null && file.exists()){
						String fileName = eciRevision.getItem().getUid();

						FTPConnection ftp = new FTPConnection();
						ftp.upload(fileName, file);
						ftp.disconnect();
					}
				}else{
					String original = eciPropertyMap.get("s7_REF_FILE_PATH")+"";
					if(!(original.equals("") || original.equals("null"))){
						String fileName = eciRevision.getItem().getUid();

						FTPConnection ftp = new FTPConnection();
						ftp.delete(null, fileName);
						ftp.disconnect();
					}
				}
				
				// 결재선 관리
				s7_WORKFLOW = "";
				if(setForSavingWorkflow(combo1) != null)
					if(setForSavingWorkflow(combo2) != null)
						if(setForSavingWorkflow(combo3) != null)
							setForSavingWorkflow(combo4);
				if(s7_WORKFLOW.length() > 0)
					eciRevision.setProperty("s7_WORKFLOW", s7_WORKFLOW);

			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(mainComposite.getShell(), e.toString(), "ERROR", MessageBox.ERROR);
			}
		}else{
			MessageBox.post(mainComposite.getShell(), message, "ERROR", MessageBox.ERROR);
		}
	}
	
	private String setForSavingWorkflow(SWTComboBox combo){
		String stepInfo = combo.getTextField().getText();
		if(!stepInfo.equals("")){
			stepInfo = "Review " + combo.getData(STEP)+ SS + stepInfo + SS + users.get(stepInfo); // [0=TASK명:1=사용자이름:2=그룹멤버PUID]
			if(s7_WORKFLOW.length() > 0){
				s7_WORKFLOW = s7_WORKFLOW + SYMCECConstant.SEPERATOR + stepInfo;
			}else{
				s7_WORKFLOW = stepInfo;
			}
		}else{
			return null;
		}
		return stepInfo;
	}
	
	/** ECO 릴레이션 */
	private void saveProblemItems(){
		try {
			//릴레이션 삭제
			TCComponent[] oldRevisions = eciRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL);
			eciRevision.remove(SYMCECConstant.PROBLEM_REL, oldRevisions);
			
			//릴레이션 추가
			if(beforePartTable.getItemCount() > 0){
				TableItem[] problems = beforePartTable.getItems();
				TCComponent[] problemItems = new TCComponent[problems.length];
				int i = 0;
				for(TableItem tableItem : problems){
					problemItems[i] = (TCComponent) tableItem.getData("tcComponent");
					i++;
				}
				eciRevision.add(SYMCECConstant.PROBLEM_REL, problemItems);
			}
			
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
		}
		isBeforePartsTableModified = false;
	}
	
	/** 필수 항목 셋팅 **/
	private void setMadatory(Control con){
		ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
		dec.setImage(registry.getImage("CONTROL_MANDATORY"));
		dec.setDescriptionText("This value will be required.");
		madatoryControls.add(con);
	}
	
	
	/** 속성 셋팅 */
	public void setProperties() {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {

				setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
//				/** ECI 확인 **/
//				if(eciRevision == null){
//					InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
//					if(comp != null && comp instanceof TCComponentChangeItemRevision){
//						TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) comp;
//						if(changeRevision.getType().startsWith(SYMCECConstant.ECITYPE)){
//							eciRevision = changeRevision;
//						}
//					}
//				}
				
				if(eciRevision == null) return;
				
				/* 속성 정보 셋 */
				String[] properties = null;
				try {

					// SYMC 보안관리 정책 상 ECI는 비밀등급 1급[작성 및 수신 결재자만 열람 가능]
					String loginUser = session.getUser().getUserId();
					String allowedUsers = eciRevision.getProperty("owning_user");

					if(eciRevision.getCurrentJob() != null){
						TCComponentTask rootTask = eciRevision.getCurrentJob().getRootTask();
						TCComponentTask[] subTasks = rootTask.getSubtasks();
						for(TCComponentTask subTask : subTasks) {
							if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE)){
								allowedUsers = allowedUsers + readSignoffTaskProfile(subTask);
							}
						}
					}

					if(allowedUsers.indexOf(loginUser) > -1){

						//저장항목
						properties = eciRevision.getProperties(eciInfoProperties);
						for(int i = 0 ; i < eciInfoProperties.length ; i++){
							eciPropertyMap.put(eciInfoProperties[i], properties[i]);
						}

						for(Object key : eciPropertyMap.keySet().toArray()){
							if(key.equals("s7_WORKFLOW")){
								String workflowInfo = eciPropertyMap.get(key);
								String[] workflows = workflowInfo.split(SYMCECConstant.SEPERATOR);
								for(int i = 0 ; i < workflows.length ; i++){
									if(workflows[i].length() > 0){
										String[] nameNpuid = workflows[i].split(SS);
										if(i == 0)
											combo1.setText(nameNpuid[1]);
										if(i == 1)
											combo2.setText(nameNpuid[1]);
										if(i == 2)
											combo3.setText(nameNpuid[1]);
										if(i == 3)
											combo4.setText(nameNpuid[1]);
									}
								}
							}
							if(eciInfoNControlMap.get(key) instanceof Text){
								Text con = (Text) eciInfoNControlMap.get(key);
								if(key.equals("s7_REVIEW_DEPT")){
									s7_REVIEW_DEPT_CODE = eciPropertyMap.get(key);
									if(s7_REVIEW_DEPT_CODE.length() >0){
										con.setText(getTeamNames(s7_REVIEW_DEPT_CODE));
									}
								}else if(key.equals("s7_REF_DEPT")){
									s7_REF_DEPT_CODE = eciPropertyMap.get(key);
									if(s7_REF_DEPT_CODE.length() >0){
										con.setText(getTeamNames(s7_REF_DEPT_CODE));
									}
								}else{
									con.setText(eciPropertyMap.get(key));
								}
							}else if(eciInfoNControlMap.get(key) instanceof SWTComboBox){
								SWTComboBox con = (SWTComboBox) eciInfoNControlMap.get(key);
								String strings = eciPropertyMap.get(key);
								con.setSelectedItems(strings.split(con.getMultipleDelimeter()));
							}else if(eciInfoNControlMap.get(key) instanceof Button){
								Button con = (Button) eciInfoNControlMap.get(key);
								if(eciPropertyMap.get(key).equals("Y"))
									con.setSelection(true);
							}else if(eciInfoNControlMap.get(key) instanceof SYMCDateTimeButton){
								SYMCDateTimeButton con = (SYMCDateTimeButton) eciInfoNControlMap.get(key);
								con.setTCDate(eciPropertyMap.get(key), session);
							}
						}

						// Problem Items
						try {
							if(beforePartTable.getItemCount() > 0) beforePartTable.removeAll();

							TCComponent[] problemItems = eciRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL);
							if(problemItems != null){
								for(TCComponent problemItem : problemItems){
									TCComponentItemRevision revision = (TCComponentItemRevision) problemItem;
									TableItem item = new TableItem(beforePartTable, SWT.NONE);
									item.setText(revision.getProperties(new String[]{"item_id", "object_name"}));
									item.setData("tcComponent", revision);
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
							MessageBox.post(getShell(), e.toString(), "ERROR in setProperties()", MessageBox.ERROR);
						}

						//입력 불가 항목
						properties = eciRevision.getProperties(new String[]{"creation_date","owning_group","owning_user","s7_ECI_MATURITY"});
						creation_date.setText(properties[0]);
						owning_group.setText(properties[1]);
						owning_user.setText(properties[2]);
						s7_ECI_MATURITY.setText(properties[3]);

						if(s7_APPROVAL_NO != null){
							properties = eciRevision.getProperties(new String[]{"s7_APPROVAL_NO","s7_ST_DATE","s7_ECO_NO","PS7_BP_SCHEDULE","s7_BP_DATE"});
							s7_APPROVAL_NO.setText(properties[0]);
							if(!s7_APPROVAL_NO.getText().equals(""))
								last_appproval.setText("Approval");
							s7_ST_DATE.setText(properties[1]);
							s7_ECO_NO.setText(properties[2]);
							s7_BP_SCHEDULE.setText(properties[3]);
							s7_BP_DATE.setText(properties[4]);
						}
					}
					/**
					 * [SR140701-022] jclee, ECR 보기 권한이 없을 경우 메시지창 오픈.
					 */
					else {
						MessageBox.post(mainComposite.getShell(), "You don't have privilage to load.", "", MessageBox.INFORMATION);
					}

				} catch (TCException e) {
					e.printStackTrace();
					MessageBox.post(mainComposite.getShell(), e.toString(), "ERROR in setProperties()", MessageBox.ERROR);
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(mainComposite.getShell(), e.toString(), "ERROR in setProperties()", MessageBox.ERROR);
				} finally{
					setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
				}
			}
		});
	}
	
	
	/**
	 * 저장 검증
	 * @return 에러
	 */
	public String validationForSave(){
		StringBuffer message = new StringBuffer();
		String value = "";
		for(Object property : eciInfoNControlMap.keySet().toArray()){
			if(madatoryControls.contains(eciInfoNControlMap.get(property))){
				value = "";
				if(eciInfoNControlMap.get(property) instanceof Text){
					Text con = (Text) eciInfoNControlMap.get(property);
					value = con.getText();
				}else if(eciInfoNControlMap.get(property) instanceof SYMCYesNoRadio){
					SYMCYesNoRadio con = (SYMCYesNoRadio) eciInfoNControlMap.get(property);
					value = con.getText();
				}else if(eciInfoNControlMap.get(property) instanceof SWTComboBox){
					SWTComboBox con = (SWTComboBox) eciInfoNControlMap.get(property);
					value = con.getTextField().getText();
				}else if(eciInfoNControlMap.get(property) instanceof Button){
					Button con = (Button) eciInfoNControlMap.get(property);
					if(con.getSelection())
						value = "Y";
					else
						value = "N";
				}
				if(value == null || value.equals(""))
					message.append(property +" will be required.\n");
			}
		}
		return message.toString();
	}
	
	private class SYMCSYMCVerifyListener implements VerifyListener {
		@Override
		public void verifyText(VerifyEvent e) {
			switch (e.keyCode) {
			case SWT.BS: // Backspace
			case SWT.DEL: // Delete
			case SWT.HOME: // Home
			case SWT.END: // End
			case SWT.ARROW_LEFT: // Left arrow
			case SWT.ARROW_RIGHT: // Right arrow
				return;
			}
            // Control + V 금지
            if (e.keyCode == 118 && (e.stateMask & SWT.CTRL)!=0) {
                e.doit = false;
                return;
            }
			if (!Character.isDigit(e.character)) { // NUMERIC
				if(e.keyCode == 0 || e.keyCode == 45 || e.keyCode == 16777261) return; // 0 붙여넣기 
				e.doit = false; // disallow the action
			}
			return;
		}
	}

	public boolean isSavable() {
		String message = validationForSave();
		if(message.equals("") || message == null)
			return true;
		MessageBox.post(getShell(), message, "ERROR", MessageBox.ERROR);
		return false;
	}
}
