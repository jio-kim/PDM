package com.symc.plm.me.sdv.view.meco;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.kgm.dto.ApprovalLineData;
import com.kgm.rac.kernel.SYMCBOPEditData;
import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.assign.AskMyAssignDialog;
import com.kgm.commands.ec.assign.SearchMyAssignDialog;
import com.kgm.commands.ec.search.SearchUserDialog;
import com.kgm.common.SYMCDateTimeButton;
import com.kgm.common.utils.progressbar.WaitProgressor;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.common.viewer.AbstractSDVViewer;
import com.symc.plm.me.sdv.command.meco.MECOProcessCommand;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.sdv.operation.meco.CompResultDiffInfo;
import com.symc.plm.me.sdv.operation.meco.MECOCreationUtil;
import com.symc.plm.me.sdv.operation.meco.validate.ValidateManager.BOPTYPE;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * [SR150715-017][20150717] shcho, 차체 MECO 상신시 검증시간 과다 소요 문제로 Checking MECO EPL을 제거. 
 *                                       그 대신에, 상신 Process 진행 전 수행하도록 변경. (정윤재 수석과 윤순식 차장님 협의 결과임.)
 *
 */
public class MECOSWTRenderingView extends AbstractSDVViewer {
    public TCSession session;
    public Text txtMecoNo, txtChangeDescription;
    public SWTComboBox cbMecoType, cbOrganization, cbProject, cbEffectEvent, cbChangeReason, cbSelectTask, cbSelectTemplate;
    public Button btnAddTask, btnDelTask, btnOpen, btnSave, btnCreateWorkflow;
    public SYMCDateTimeButton txtEffectDate;
    private Table approvalLineTable;
    private Registry registry;
    private TCComponentChangeItemRevision mecoRevision;
    public HashMap<String, Control> mecoInfoNControlMap = new HashMap<String, Control>();
    private HashMap<String, String> mecoPropertyMap = new HashMap<String, String>();
    private ArrayList<Control> madatoryControls;
    public TCComponentFolder targetFolder;
    private boolean isApprovalLineModified = false;
    private CustomMECODao dao;
    public Composite layoutComposite;
    private Composite csBasicInfo;
    private Composite csApproval;
    private String[] mecoInfoProperties;
    private String[] taskList;
    public String[] taskTemplates;
    private boolean isReleased;
    private SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private Shell shell = null;

    public MECOSWTRenderingView(Composite parent) {
        super(parent);
        initData();
        InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
        if (comps.length > 0) {
            TCComponent comp = (TCComponent) comps[0];
            if (comp instanceof TCComponentChangeItemRevision) {
                targetComp = comp;
            }
        }
    }

    /**
     * Create the composite.
     *
     * @param parent
     * @param style
     * @wbp.parser.constructor
     */
    public MECOSWTRenderingView(Composite parent, boolean isCreate) {
        super(parent);
        initData();
        InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
        if (comps.length > 0) {
            TCComponent comp = (TCComponent) comps[0];
            if (comp instanceof TCComponentFolder) {
                TCComponentFolder folder = (TCComponentFolder) comp;
                if (!folder.getType().equals("S7_CorpOptionF")) {
                    targetFolder = folder;
                }
            }
        }
    }

    /** 결재선 추가 */
    @SuppressWarnings("rawtypes")
    private void addApprovalLine() {
        if (this.cbSelectTask.getTextField().getText().equals("")) { // TASK 선택 여부 체크
            MessageBox.post(getShell(), "Select task first!", "Information", MessageBox.INFORMATION);
            return;
        }

        TCComponentGroupMember addMember = null;
        HashMap addReferencesMember = null;
        SearchUserDialog searchDialog = new SearchUserDialog(getShell(), "MFG", this.cbSelectTask.getTextField().getText());
        int returnInt = searchDialog.open();
        if (returnInt == 0) {
            if ("References".equals(cbSelectTask.getTextField().getText())) {
                addReferencesMember = searchDialog.getSelectedReferencesMember();
            } else {
                addMember = searchDialog.getSelectedMember();
            }
        }

        if (addMember != null || addReferencesMember != null) {
            TableItem[] approvalLineTableItems = approvalLineTable.getItems();
            String approvalLineTableItemKey = "";
            String selectedItemKey = "";
            String[] selectedItem = null;
            try {
                if ("References".equals(cbSelectTask.getTextField().getText())) {
                    selectedItem = new String[] { (String) addReferencesMember.get("TEAM"), (String) addReferencesMember.get("USER_NAME"), (String) addReferencesMember.get("THE_USER") };
                } else {
                    selectedItem = addMember.getProperties(new String[] { "group", "the_user", "fnd0objectId" });// [ME PLANNING.SYMC_MFG, 이종화, wIaJdXTAo1W$GD]
                }
            } catch (TCException e) {
                e.printStackTrace();
            }

            // 중복 입력 방지
            for (TableItem approvalLineTableItem : approvalLineTableItems) {
                approvalLineTableItemKey = approvalLineTableItem.getText(0) + approvalLineTableItem.getData("puid");
                selectedItemKey = cbSelectTask.getTextField().getText() + selectedItem[2];
                if (approvalLineTableItemKey.equals(selectedItemKey))
                    return;
            }

            // 순서 정렬
            int selectPosition = 0;
            boolean isNotAdded = true;
            for (String task : taskList) {
                if (cbSelectTask.getTextField().getText().equals(task)) {
                    break;
                }
                selectPosition++;
            }

            String[] selectInfo = new String[4];
            selectInfo[0] = cbSelectTask.getTextField().getText();
            selectInfo[1] = selectedItem[0];
            selectInfo[2] = selectedItem[1];
            selectInfo[3] = selectedItem[2];

            ArrayList<String[]> approvalLineInfo = new ArrayList<String[]>();

            for (TableItem approvalLineTableItem : approvalLineTableItems) {
                String[] lineInfo = new String[4];
                lineInfo[0] = approvalLineTableItem.getText(0);
                lineInfo[1] = approvalLineTableItem.getText(1);
                lineInfo[2] = approvalLineTableItem.getText(2);
                lineInfo[3] = (String) approvalLineTableItem.getData("puid");
                if (isNotAdded) {
                    for (int i = 0; i < taskList.length; i++) {
                        if (lineInfo[0].equals(taskList[i])) {
                            if (selectPosition < i) {
                                approvalLineInfo.add(selectInfo);
                                isNotAdded = false;
                            }
                        }
                    }
                }
                approvalLineInfo.add(lineInfo);
            }

            if (isNotAdded) {
                approvalLineInfo.add(selectInfo);
                isNotAdded = false;
            }

            if (approvalLineInfo.size() < 1) { // 초기 등록
                TableItem item = new TableItem(approvalLineTable, SWT.NONE);
                item.setText(0, selectInfo[0]);
                item.setText(1, selectInfo[1]);
                item.setText(2, selectInfo[2]);
                item.setData("puid", selectInfo[3]);
            } else {
                approvalLineTable.removeAll();
                for (String[] lineInfo : approvalLineInfo) {
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

    private void changeSelectTask() {
        try {
            cbSelectTask.removeAllItems();
            if (!"".equals(BundleUtil.nullToString((cbSelectTemplate.getTextField().getText())))) {
                ArrayList<String> selectTasks = CustomUtil.getWorkflowTask(cbSelectTemplate.getTextField().getText(), session);
                int cnt = 0;
                taskList = new String[selectTasks.size()];
                for (String selectTask : selectTasks) {
                    cbSelectTask.addItem(selectTask);
                    taskList[cnt] = selectTask;
                    cnt++;
                }

                if (!(BundleUtil.nullToString(cbSelectTemplate.getTextField().getText())).equals(mecoPropertyMap.get(SDVPropertyConstant.MECO_WORKFLOW_TYPE))) {
                    approvalLineTable.removeAll();
                }
            }
        } catch (TCException e2) {
            e2.printStackTrace();
        }
    }

    public void create() throws Exception {
        getParamMap();
    }

    @Override
    public void createPanel(Composite parent) {
        dao = new CustomMECODao();
        session = CustomUtil.getTCSession();
        this.registry = Registry.getRegistry(this);
        madatoryControls = new ArrayList<Control>();

        /** 선택 오브젝트 확인 **/
        InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
        if (comp != null && comp instanceof TCComponentChangeItemRevision) {
            TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) comp;
            if (changeRevision.getType().startsWith(SDVTypeConstant.MECO_ITEM_REV)) {
                this.mecoRevision = changeRevision;
            }

            String[] processProps = null;
            try {
        	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체
                processProps = mecoRevision.getProperties(new String[] { "date_released", "fnd0StartedWorkflowTasks" });
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (processProps[0].equals("") || processProps[1].contains("Creator")) {
                isReleased = false;
            } else {
                isReleased = true;
            }
        }

        initUI();
        setOrg();
    }

    private void initUI() {
        setLayout(new FillLayout(SWT.HORIZONTAL));
        mecoInfoProperties = new String[] { SDVPropertyConstant.ITEM_ITEM_ID, SDVPropertyConstant.MECO_TYPE, SDVPropertyConstant.MECO_ORG_CODE, SDVPropertyConstant.MECO_PROJECT, SDVPropertyConstant.MECO_EFFECT_DATE, SDVPropertyConstant.MECO_EFFECT_EVENT, SDVPropertyConstant.MECO_CHANGE_REASON, SDVPropertyConstant.ITEM_OBJECT_DESC, SDVPropertyConstant.MECO_WORKFLOW_TYPE };

        layoutComposite = new Composite(this, SWT.NONE);
        layoutComposite.setLayout(new GridLayout(1, false));

        csBasicInfo = new Composite(layoutComposite, SWT.NONE);
        GridData gd_csBasicInfo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_csBasicInfo.widthHint = 666;
        gd_csBasicInfo.heightHint = 168;
        csBasicInfo.setLayoutData(gd_csBasicInfo);

        // MECO NO
        Label lbMecoNo = new Label(csBasicInfo, SWT.NONE);
        lbMecoNo.setText("MECO No");
        lbMecoNo.setBounds(39, 15, 54, 15);

        txtMecoNo = new Text(csBasicInfo, SWT.BORDER | SWT.READ_ONLY);
        txtMecoNo.setEnabled(false);
        txtMecoNo.setBounds(98, 12, 158, 21);

        // MECO TYPE
        Label lbMecoType = new Label(csBasicInfo, SWT.NONE);
        lbMecoType.setText("MECO Type");
        lbMecoType.setBounds(423, 15, 63, 15);

        cbMecoType = new SWTComboBox(csBasicInfo, SWT.BORDER);
        SDVLOVUtils.comboValueSetting(cbMecoType, "M7_MECO_TYPE");
        cbMecoType.setBounds(495, 12, 158, 23);
        setMandatory(cbMecoType);
        cbMecoType.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                if (cbMecoType.getSelectedItemCount() > 0 && !"".equals(BundleUtil.nullToString(cbMecoType.getTextField().getText()))) {
                    cbSelectTemplate.setEnabled(true);
                    csApproval.setEnabled(true);
                    try {
                        taskTemplates = CustomUtil.loadWorkflowTemplate(cbMecoType.getSelectedItem().toString());
                        cbSelectTemplate.setSelectedObject(null);
                        cbSelectTemplate.removeAllItems();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (String tasktemplate : taskTemplates) {
                        cbSelectTemplate.addItem(tasktemplate);
                    }
                    if (taskTemplates.length > 0) {

                        cbSelectTemplate.setSelectedIndex(0);
                    }
                    if (!"".equals(BundleUtil.nullToString(cbSelectTemplate.getTextField().getText()))) {
                        changeSelectTask();
                    }
                }
            }
        });

        // ORGANIZATION
        Label lbOrganization = new Label(csBasicInfo, SWT.NONE);
        lbOrganization.setText("Organization");
        lbOrganization.setBounds(423, 44, 69, 15);

        cbOrganization = new SWTComboBox(csBasicInfo, SWT.BORDER);
        SDVLOVUtils.comboValueSetting(cbOrganization, "M7_MECO_ORG");
        cbOrganization.setBounds(495, 41, 158, 23);
        setMandatory(cbOrganization);

        // PROJECT
        cbProject = new SWTComboBox(csBasicInfo, SWT.BORDER);
        SDVLOVUtils.comboValueSetting(cbProject, "S7_PROJECT_CODE");
        cbProject.setBounds(98, 40, 158, 23);
        cbProject.setEnabled(true);
        setControlSkipEnable(cbProject, false);
        setMandatory(cbProject);

        Label lbProject = new Label(csBasicInfo, SWT.NONE);
        lbProject.setText("Project");
        lbProject.setBounds(56, 44, 37, 15);

        // EFFECT DATE
        Label lbEffectDate = new Label(csBasicInfo, SWT.NONE);
        lbEffectDate.setText("Eff. Date");
        lbEffectDate.setBounds(47, 73, 46, 15);

        txtEffectDate = new SYMCDateTimeButton(csBasicInfo, SWT.BORDER | SWT.DROP_DOWN);
        txtEffectDate.setBounds(98, 69, 158, 24);
        txtEffectDate.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
                if (e.keyCode > 1) {
                    if (txtEffectDate.getData() != null) {
                        cbEffectEvent.setEnabled(true);
                    } else {
                        cbEffectEvent.setSelectedIndex(-1);
                    }
                }
            }

            public void keyPressed(KeyEvent e) {

            }
        });

        // EFFECT EVENT
        Label lbEffectEvent = new Label(csBasicInfo, SWT.NONE);
        lbEffectEvent.setText("Eff. Event");
        lbEffectEvent.setBounds(423, 73, 50, 15);

        cbEffectEvent = new SWTComboBox(csBasicInfo, SWT.BORDER);
        ArrayList<HashMap<String, String>> arrEffectEvent = null;
        try {
            arrEffectEvent = CustomUtil.getRevisionEffectivityReferencedByMeco();
            cbEffectEvent.addItem(".", "(.)");
            for (HashMap<String, String> hashEffectEvent : arrEffectEvent) {
                cbEffectEvent.addItem(hashEffectEvent.get(SDVPropertyConstant.EFFECTIVITY_ID) + " (" + hashEffectEvent.get(SDVPropertyConstant.EFFECTIVITY_DATES) + ")", hashEffectEvent.get(SDVPropertyConstant.EFFECTIVITY_ID));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        cbEffectEvent.setBounds(495, 70, 158, 23);
        cbEffectEvent.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                if (cbEffectEvent.getSelectedItemCount() > 0 && !(cbEffectEvent.getTextField().getText()).startsWith(".")) {
                    txtEffectDate.setEnabled(false);
                } else {
                    if ((cbEffectEvent.getTextField().getText()).startsWith(".")) {
                        txtEffectDate.setEnabled(true);
                    }
                }
            }
        });

        // CHANGE REASON
        Label lbChangeReason = new Label(csBasicInfo, SWT.NONE);
        lbChangeReason.setText("Change Reason");
        lbChangeReason.setBounds(10, 102, 83, 15);

        cbChangeReason = new SWTComboBox(csBasicInfo, SWT.BORDER);
        SDVLOVUtils.comboValueSetting(cbChangeReason, "M7_MECO_REASON");
        cbChangeReason.setBounds(98, 98, 158, 23);
        setMandatory(cbChangeReason);

        // CHANGE DESCRIPTION
        Label lbChangeDescription = new Label(csBasicInfo, SWT.NONE);
        lbChangeDescription.setText("Change Desc");
        lbChangeDescription.setBounds(22, 133, 71, 15);

        txtChangeDescription = new Text(csBasicInfo, SWT.BORDER);
        txtChangeDescription.setBounds(98, 132, 555, 21);

        csApproval = new Composite(layoutComposite, SWT.NONE);
        GridData gd_csApproval = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 3);
        gd_csApproval.heightHint = 260;
        gd_csApproval.widthHint = 665;
        csApproval.setLayoutData(gd_csApproval);

        Group grApprovalinfo = new Group(csApproval, SWT.NONE);
        grApprovalinfo.setText("Approval Line");
        grApprovalinfo.setBounds(0, 0, 658, 259);

        approvalLineTable = new Table(grApprovalinfo, SWT.BORDER | SWT.FULL_SELECTION);
        approvalLineTable.setBounds(10, 53, 644, 153);
        approvalLineTable.setHeaderVisible(true);
        approvalLineTable.setLinesVisible(true);

        TableColumn tbclmnTask = new TableColumn(approvalLineTable, SWT.CENTER);
        tbclmnTask.setWidth(101);
        tbclmnTask.setText("Task");

        TableColumn tblclmnName = new TableColumn(approvalLineTable, SWT.CENTER);
        tblclmnName.setWidth(117);
        tblclmnName.setText("Dept");

        TableColumn tblclmnDept = new TableColumn(approvalLineTable, SWT.CENTER);
        tblclmnDept.setWidth(99);
        tblclmnDept.setText("User Name");

        TableColumn tblclmnNewColumn = new TableColumn(approvalLineTable, SWT.CENTER);
        tblclmnNewColumn.setWidth(66);
        tblclmnNewColumn.setText("Tel");

        TableColumn tblclmnDate = new TableColumn(approvalLineTable, SWT.CENTER);
        tblclmnDate.setWidth(100);
        tblclmnDate.setText("Date");

        TableColumn tblclmnNewColumn_1 = new TableColumn(approvalLineTable, SWT.NONE);
        tblclmnNewColumn_1.setWidth(158);
        tblclmnNewColumn_1.setText("Comments");

        btnAddTask = new Button(grApprovalinfo, SWT.CENTER);
        btnAddTask.setBounds(538, 22, 51, 25);
        btnAddTask.setText("Add");
        btnAddTask.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addApprovalLine();
            }
        });

        btnDelTask = new Button(grApprovalinfo, SWT.NONE);
        btnDelTask.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteApprovalLine();
            }
        });
        btnDelTask.setBounds(590, 22, 65, 25);
        btnDelTask.setText("Delete");

        cbSelectTemplate = new SWTComboBox(grApprovalinfo, SWT.BORDER);
        cbSelectTemplate.setBounds(210, 22, 120, 25);
        cbSelectTemplate.getTextField().setEditable(false);
        cbSelectTemplate.setEnabled(false);
        cbSelectTemplate.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
                changeSelectTask();
            }
        });

        cbSelectTask = new SWTComboBox(grApprovalinfo, SWT.BORDER);
        cbSelectTask.setBounds(405, 22, 120, 25);

        Label lbSelectTask = new Label(grApprovalinfo, SWT.NONE);
        lbSelectTask.setText("Task");
        lbSelectTask.setBounds(364, 27, 35, 15);

        btnOpen = new Button(grApprovalinfo, SWT.CENTER);
        btnOpen.setLocation(10, 22);
        btnOpen.setSize(51, 25);
        btnOpen.setText("Open");
        btnOpen.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (cbMecoType.getSelectedItemCount() > 0 || !"".equals(cbMecoType.getTextField().getText())) {
                    loadUserApprovalLine();
                }
            }
        });

        btnSave = new Button(grApprovalinfo, SWT.CENTER);
        btnSave.setText("Save");
        btnSave.setBounds(65, 22, 51, 25);
        btnSave.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                saveUserApprovalLine();
            }
        });

        Label lbSelectTemplate = new Label(grApprovalinfo, SWT.NONE);
        lbSelectTemplate.setText("Template");
        lbSelectTemplate.setBounds(152, 27, 57, 15);

        btnCreateWorkflow = new Button(grApprovalinfo, SWT.CENTER);
        btnCreateWorkflow.setText("Request Approval");
        btnCreateWorkflow.setBounds(290, 220, 120, 25);
        btnCreateWorkflow.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unused")
            public void widgetSelected(SelectionEvent e) {
				if (approvalLineTable.getItemCount() == 0)
				{
					MessageBox.post(shell, "Must be filled in the approval line", "WARNING", MessageBox.WARNING);
					return;
				} else
				{
					MECOProcessCommand command = new MECOProcessCommand(mecoRevision);
				}
            }
        });

        if (isReleased || mecoRevision == null) {
            btnCreateWorkflow.setVisible(false);
        }

        if (!"".equals(BundleUtil.nullToString(cbMecoType.getTextField().getText()))) {
            csApproval.setEnabled(true);
        }
    }

    /** 결재선 삭제 */
    private void deleteApprovalLine() {
        TableItem[] items = approvalLineTable.getSelection();
        if (items.length == 0)
            return;
        items[0].dispose();
        isApprovalLineModified = true;
    }

    /**
     * Composite 반환
     */
    public Composite getComposite() {
        return this;
    }

    public TCComponentChangeItemRevision getMecoRevision() {
        return mecoRevision;
    }

    /** 화면 속성 맵 생성 */
    public HashMap<String, String> getParamMap() {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        mecoInfoNControlMap.remove("item_id");
        String value = "";
        for (Object property : mecoInfoNControlMap.keySet().toArray()) {
            value = "";
            if (mecoInfoNControlMap.get(property) instanceof Text) {
                Text con = (Text) mecoInfoNControlMap.get(property);
                value = con.getText();
            } else if (mecoInfoNControlMap.get(property) instanceof SWTComboBox) {
                SWTComboBox con = (SWTComboBox) mecoInfoNControlMap.get(property);
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
            } else if (mecoInfoNControlMap.get(property) instanceof Button) {
                Button con = (Button) mecoInfoNControlMap.get(property);
                if (con.getSelection())
                    value = "Y";
                else
                    value = "N";
            } else if (mecoInfoNControlMap.get(property) instanceof DateTime) {
                SYMCDateTimeButton con = (SYMCDateTimeButton) mecoInfoNControlMap.get(property);
                if (property.equals("m7_EFFECT_DATE")) {
                    if ((cbEffectEvent.getTextField().getText()).startsWith(".") || cbEffectEvent.getSelectedItemCount() == 0) {
                        value = DATE_FORMATTER.format(con.getDate(session));
                        System.out.println("value-->" + value);
                    } else {
                        System.out.println("!!!");
                    }
                } else {
                    value = con.getTCDate(session);
                }
            }
            paramMap.put((String) property, value);
        }

        return paramMap;
    }

    private void initData() {
    	shell = getShell();
        mecoInfoNControlMap.put(SDVPropertyConstant.ITEM_ITEM_ID, txtMecoNo);
        mecoInfoNControlMap.put(SDVPropertyConstant.MECO_TYPE, cbMecoType);
        mecoInfoNControlMap.put(SDVPropertyConstant.MECO_ORG_CODE, cbOrganization);
        mecoInfoNControlMap.put(SDVPropertyConstant.MECO_PROJECT, cbProject);
        mecoInfoNControlMap.put(SDVPropertyConstant.MECO_EFFECT_DATE, txtEffectDate);
        mecoInfoNControlMap.put(SDVPropertyConstant.MECO_EFFECT_EVENT, cbEffectEvent);
        mecoInfoNControlMap.put(SDVPropertyConstant.MECO_CHANGE_REASON, cbChangeReason);
        mecoInfoNControlMap.put(SDVPropertyConstant.ITEM_OBJECT_DESC, txtChangeDescription);
        mecoInfoNControlMap.put(SDVPropertyConstant.MECO_WORKFLOW_TYPE, cbSelectTemplate);
    }

    /** 속성 및 관계 변경 여부 */
    public boolean isModified() {
        if (mecoRevision != null && mecoRevision.isCheckedOut() && isPropertisModified() || isApprovalLineModified)
            return true;
        return false;
    }

    /** 속성 변경 여부 */
    public boolean isPropertisModified() {
        mecoPropertyMap.remove("item_id");
        HashMap<String, String> paramMap = getParamMap();
        String property = "";
        String param = "";
        for (Object key : mecoPropertyMap.keySet().toArray()) {
            param = paramMap.get(key);
            if (param == null)
                param = "";
            property = mecoPropertyMap.get(key);
            if (property == null)
                property = "";
            if (!param.equals(property))
                return true;
        }

        return false;
    }

    @Override
    public boolean isSavable() {
        String message = validationForSave();
        if (message.equals("") || message == null) {
            return true;
        }
        MessageBox.post(getShell(), message, "ERROR", MessageBox.ERROR);

        return false;
    }

    @Override
    public void load() {
        System.out.println("MECOSWTRenderingView.load()");
    }

    private String checkValidateTemplateType(String templateName) {
        for (String taskTemplate : taskTemplates) {
            if (templateName.startsWith(taskTemplate)) {
                return taskTemplate;
            }
        }

        return null;
    }

    private void loadUserApprovalLine() {
        try {
            ApprovalLineData map = new ApprovalLineData();
            map.setSaved_user(session.getUser().getUserId());
            map.setEco_no(SYMCECConstant.ECO_PROCESS_TEMPLATE);

            ArrayList<ApprovalLineData> resultSavedApprovalLines = dao.loadSavedUserApprovalLine(map);

            if (resultSavedApprovalLines == null || resultSavedApprovalLines.size() < 1) {
                MessageBox.post(layoutComposite.getShell(), "You have no saved approval lines.", "Information", MessageBox.INFORMATION);
                return;
            }

            String[] savedApprovalLines = new String[resultSavedApprovalLines.size()];
            int i = 0;
            for (ApprovalLineData resultSavedApprovalLine : resultSavedApprovalLines) {
                savedApprovalLines[i] = resultSavedApprovalLine.getSaved_name();
                i++;
            }

            SearchMyAssignDialog dialog = new SearchMyAssignDialog(getShell(), session, savedApprovalLines, "MFG");
            ArrayList<ApprovalLineData> selectedItemInfos = dialog.open();
            if (selectedItemInfos != null) {
                if (approvalLineTable.getItemCount() > 0)
                    approvalLineTable.removeAll();
                boolean isValidateTemplate = false;
                String savedTempalte_name = null;
                for (ApprovalLineData selectedItemInfo : selectedItemInfos) {
                    savedTempalte_name = checkValidateTemplateType(selectedItemInfo.getSaved_name());
                    if (null != savedTempalte_name) {
                        TableItem item = new TableItem(approvalLineTable, SWT.NONE);
                        item.setText(0, selectedItemInfo.getTask());
                        item.setText(1, selectedItemInfo.getTeam_name());
                        item.setText(2, selectedItemInfo.getUser_name());
                        item.setData("puid", selectedItemInfo.getTc_member_puid()); // TCComponentGroupMember puid
                        isApprovalLineModified = true;
                        isValidateTemplate = true;

                    }
                }

                cbSelectTemplate.setSelectedItem(savedTempalte_name);
                cbSelectTemplate.update();

                if (!isValidateTemplate) {
                    MessageBox.post(getShell(), "The name of selected approval line is unsuitable to the usable template.", "WARNING", MessageBox.WARNING);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
        }
    }

    @Override
    public void save() {
        String message = validationForSave();
        final HashMap<String, String> attrMap = getParamMap();
        if (message.equals("") || message == null) {
            layoutComposite.getDisplay().syncExec(new Runnable() {
                public void run() {
                    try {
                        if (isApprovalLineModified) {
                            saveApprovalLine();
                        }

                        // Case saving properties (EFFECT_EVENT, EFFECT_DATE), using reference object.
                        TCComponent refComp = mecoRevision.getReferenceProperty(SDVPropertyConstant.MECO_TYPED_REFERENCE);
                        // 속성 Object가 없는 경우 생성
                        if (refComp != null) {
                            refComp.setProperty("m7_EFFECT_DATE", (String) attrMap.get("m7_EFFECT_DATE"));
                            refComp.setProperty("m7_EFFECT_EVENT", (String) attrMap.get("m7_EFFECT_EVENT"));
                            mecoRevision.setReferenceProperty(SDVPropertyConstant.MECO_TYPED_REFERENCE, refComp);
                            refComp.refresh();
                        }
                        // 속성 Object가 존재하는 경우 수정
                        else {
                            refComp = SYMTcUtil.createApplicationObject(mecoRevision.getSession(), SDVTypeConstant.MECO_TYPED_REFERECE, new String[] { "m7_EFFECT_DATE", "m7_EFFECT_EVENT" }, new String[] { (String) attrMap.get("m7_EFFECT_DATE"), (String) attrMap.get("m7_EFFECT_EVENT") });

                            mecoRevision.setReferenceProperty(SDVPropertyConstant.MECO_TYPED_REFERENCE, refComp);
                        }
                        attrMap.remove("m7_EFFECT_DATE");
                        attrMap.remove("m7_EFFECT_EVENT");

                        mecoRevision.setProperties(attrMap);
                        mecoRevision.lock();
                        mecoRevision.save();
                        mecoRevision.unlock();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
                    }
                }
            });
        } else {
            MessageBox.post(getShell(), message, "ERROR", MessageBox.ERROR);
        }
    }

    private void saveUserApprovalLine() {
        if (approvalLineTable.getItemCount() < 1) {
            MessageBox.post(getShell(), "You have no saving informations.", "Information", MessageBox.INFORMATION);
            return;
        }

        AskMyAssignDialog dialog = new AskMyAssignDialog(getShell(), approvalLineTable, cbSelectTemplate.getSelectedItem().toString(), session);
        dialog.open();
    }

    public void updateUI() throws TCException {
        setControlReadOnly(cbMecoType);
        setControlReadOnly(cbOrganization);
        setControlSkipEnable(btnCreateWorkflow, true);

        String owning_user = mecoRevision.getProperty(SDVPropertyConstant.ITEM_OWNING_USER);
        if (mecoRevision != null && mecoRevision.isCheckedOut() && owning_user.equals(session.getUser().toString())) {
            setControlReadWrite(cbProject);
            setControlReadWrite(cbChangeReason);
            setControlReadWrite(cbEffectEvent);
            setControlReadWrite(csApproval);
            txtChangeDescription.setEnabled(true);
            String compValue = "";
            try {
                compValue = (String) mecoRevision.getProperty("m7_EFFECT_EVENT");
                if (".".equals(compValue) || "".equals(compValue)) {
                    txtEffectDate.setEnabled(true);
                    cbEffectEvent.update();
                } else {
                    txtEffectDate.setEnabled(false);
                    txtEffectDate.update();
                }

                taskTemplates = CustomUtil.loadWorkflowTemplate(cbMecoType.getSelectedItem().toString());
                for (String tasktemplate : taskTemplates) {
                    cbSelectTemplate.addItem(tasktemplate);
                }

                if (!"".equals(BundleUtil.nullToString((cbSelectTemplate.getTextField().getText())))) {
                    changeSelectTask();
                }

                if (!"".equals(BundleUtil.nullToString(cbMecoType.getTextField().getText()))) {
                    csApproval.setEnabled(true);
                } else {
                    csApproval.setEnabled(false);
                    setControlSkipEnable(btnCreateWorkflow, true);
                }
            } catch (TCException e) {
                e.printStackTrace();
                MessageBox.post(getShell(), e.toString(), "ERROR in updateUI()", MessageBox.ERROR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setControlReadOnly(csBasicInfo);
            setControlReadOnly(csApproval);
            setControlSkipEnable(btnCreateWorkflow, true);
        }
    }

    public void setMECORevison(TCComponentChangeItemRevision mecoRevision) {
        this.mecoRevision = mecoRevision;
    }

    public void setProperties() throws TCException {
        layoutComposite.getDisplay().syncExec(new Runnable() {
            public void run() {
                String[] properties = null;
                try {
                    properties = mecoRevision.getProperties(mecoInfoProperties);

                    TCProperty[] tcProperties = mecoRevision.getAllTCProperties();
                    HashMap<String, Object> propsMap = new HashMap<String, Object>();
                    for (TCProperty tcprop : tcProperties) {
                        propsMap.put(tcprop.getPropertyName(), tcprop.getPropertyValue());
                    }

                    // 속성 Object가 없는 경우 생성
                    String[] effectivityProperties = null;

                    for (int i = 0; i < mecoInfoProperties.length; i++) {
                        mecoPropertyMap.put(mecoInfoProperties[i], properties[i]);
                    }

                    TCComponent refComp = mecoRevision.getReferenceProperty(SDVPropertyConstant.MECO_TYPED_REFERENCE);
                    if (refComp != null) {
                        String[] effectivityPropertiesKey = { "m7_EFFECT_DATE", "m7_EFFECT_EVENT" };
                        effectivityProperties = refComp.getProperties(effectivityPropertiesKey);
                        mecoPropertyMap.put("m7_EFFECT_DATE", effectivityProperties[0]);
                        mecoPropertyMap.put("m7_EFFECT_EVENT", effectivityProperties[1]);

                    }

                    for (Object key : mecoPropertyMap.keySet().toArray()) {
                        if (mecoInfoNControlMap.get(key) instanceof Text) {
                            Text con = (Text) mecoInfoNControlMap.get(key);
                            con.setText(mecoPropertyMap.get(key));
                        } else if (mecoInfoNControlMap.get(key) instanceof SWTComboBox) {
                            SWTComboBox con = (SWTComboBox) mecoInfoNControlMap.get(key);
                            String strings = mecoPropertyMap.get(key);
                            con.setSelectedItems(strings.split(con.getMultipleDelimeter()));
                        } else if (mecoInfoNControlMap.get(key) instanceof Button) {
                            Button con = (Button) mecoInfoNControlMap.get(key);
                            if (mecoPropertyMap.get(key).equals("Y"))
                                con.setSelection(true);
                        } else if (mecoInfoNControlMap.get(key) instanceof DateTime) {
                            SYMCDateTimeButton con = (SYMCDateTimeButton) mecoInfoNControlMap.get(key);
                            con.setData(mecoPropertyMap.get(key));
                            txtEffectDate.setData(mecoPropertyMap.get(key));
                            if (mecoPropertyMap.get(key).length() != 0) {
                                txtEffectDate.setDate(DATE_FORMATTER.parse(mecoPropertyMap.get(key)), session);
                            }
                        }
                    }

                    // 결재선
                    if (approvalLineTable.getItemCount() > 0)
                        approvalLineTable.removeAll();

                    ApprovalLineData theLine = new ApprovalLineData();
                    theLine.setEco_no(txtMecoNo.getText());
                    ArrayList<ApprovalLineData> paramList = dao.getApprovalLine(theLine);

                    if (mecoRevision.getCurrentJob() == null) {
                        /* 결재 정보 셋 */
                        for (ApprovalLineData map : paramList) {
                            TableItem item = new TableItem(approvalLineTable, SWT.NONE);
                            item.setText(0, map.getTask());
                            item.setText(1, map.getTeam_name());
                            item.setText(2, map.getUser_name());
                            item.setData("puid", map.getTc_member_puid());
                        }
                    } else {
                        TCComponentTask rootTask = mecoRevision.getCurrentJob().getRootTask();
                        TCComponentTask[] subTasks = rootTask.getSubtasks();
                        for (TCComponentTask subTask : subTasks) { // FIXED, 20130531, DJKIM, 작업자 정보 첫줄에 추가
                            if (subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) || subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)) {
                                if (subTask.getName().equals("Creator"))
                                    readSignoffTaskProfile(subTask);
                            }
                        }

                        for (ApprovalLineData map : paramList) {
                            for (TCComponentTask subTask : subTasks) {
                                if (subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) || subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)) {
                                    if (!subTask.getName().equals("Creator"))
                                        if (map.getTask().equals(subTask.getName()))
                                            readSignoffTaskProfile(subTask, map);
                                }
                            }
                        }

                        for (ApprovalLineData map : paramList) {
                            if (map.getTask().equals("References")) {

                                TableItem item = new TableItem(approvalLineTable, SWT.NONE);
                                item.setText(0, map.getTask());
                                item.setText(1, map.getTeam_name());
                                item.setText(2, map.getUser_name());
                                item.setData("puid", map.getTc_member_puid());
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

    /**
     * 저장 검증
     *
     * @return 에러
     */
    public String validationForSave() {
        StringBuffer message = new StringBuffer();
        String value = "";
        // 필수 값 체크
        for (Object property : mecoInfoNControlMap.keySet().toArray()) {
            if (madatoryControls.contains(mecoInfoNControlMap.get(property))) {
                value = "";
                if (mecoInfoNControlMap.get(property) instanceof Text) {
                    Text con = (Text) mecoInfoNControlMap.get(property);
                    if (property.equals("m7_EFFECT_DATE")) {

                    } else {
                        value = con.getText();
                    }
                } else if (mecoInfoNControlMap.get(property) instanceof SWTComboBox) {
                    SWTComboBox con = (SWTComboBox) mecoInfoNControlMap.get(property);

                    if (property.equals("m7_EFFECT_EVENT")) {
                        if (!(cbEffectEvent.getTextField().getText()).startsWith(".")) {
                            value = con.getTextField().getText();
                        } else {
                            value = "";
                        }
                    } else {
                        value = con.getTextField().getText();
                    }
                } else if (mecoInfoNControlMap.get(property) instanceof Button) {
                    Button con = (Button) mecoInfoNControlMap.get(property);
                    if (con.getSelection())
                        value = "Y";
                    else
                        value = "N";
                }

                if (value == null || value.equals(""))
                    message.append(registry.getString(property.toString()) + " will be required.\n");
            }
        }

        return message.toString();
    }

    /** 결재선 저장 */
    private void saveApprovalLine() {
        ArrayList<ApprovalLineData> paramList = new ArrayList<ApprovalLineData>();
        TableItem[] itemList = approvalLineTable.getItems();

        try {
            String meco_no = txtMecoNo.getText();
            ApprovalLineData delLine = new ApprovalLineData();
            delLine.setEco_no(meco_no);

            dao.removeApprovalLine(delLine);

            if (itemList != null && itemList.length > 0) {
                int i = 0;
                for (TableItem item : itemList) {
                    ApprovalLineData theLine = new ApprovalLineData();
                    theLine.setEco_no(meco_no);
                    theLine.setSort(i + "");
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

    private void readSignoffTaskProfile(TCComponentTask approveTask) throws TCException {
        TCComponentTask performSignoffTask = approveTask.getSubtask("perform-signoffs");
        TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();
        if (signoffs.length > 0) {
            for (TCComponentSignoff signoff : signoffs) {
                signoff.refresh();
                TCComponentGroupMember groupMember = signoff.getGroupMember();
                String[] groupMemberProperties = groupMember.getProperties(new String[] { "the_group", "the_user" });
                String[] signoffProperties = signoff.getProperties(new String[] { "decision_date", "comments" });
                TCComponent pserson = groupMember.getUser().getRelatedComponent("person");
                TableItem item = new TableItem(approvalLineTable, SWT.NONE);
                item.setText(0, approveTask.getName());
                item.setText(1, groupMemberProperties[0]);
                item.setText(2, groupMemberProperties[1]);
                item.setText(3, pserson.getProperty("PA10"));
                if (approveTask.getName().equals("Creator")) { // FIXED, 20130531, DJKIM, 작업자 정보 첫줄에 추가 및 상신일과 재상신 구분
                    if (signoffProperties[0] == null || signoffProperties[0].equals("")) {
                        item.setText(4, approveTask.getRoot().getProperty("creation_date"));
                        item.setText(5, "");
                    } else {
                        item.setText(4, signoffProperties[0]);
                        item.setText(5, signoffProperties[1]);
                    }
                } else {
                    item.setText(4, signoffProperties[0]);
                    item.setText(5, signoffProperties[1]);
                }
            }
        }
    }

    /**
     * 결재 정보 확인
     *
     * @param approveTask
     * @throws TCException
     */
    private void readSignoffTaskProfile(TCComponentTask approveTask, ApprovalLineData map) throws TCException {
        TCComponentTask performSignoffTask = approveTask.getSubtask("perform-signoffs");
        TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();

        TableItem item = new TableItem(approvalLineTable, SWT.NONE);

        if (!"References".equals(map.getTask())) {
            for (TCComponentSignoff signoff : signoffs) {
                signoff.refresh();
                TCComponentGroupMember groupMember = signoff.getGroupMember();
                String[] groupMemberProperties = groupMember.getProperties(new String[] { "the_group", "the_user" });
                String[] signoffProperties = signoff.getProperties(new String[] { "decision_date", "comments" });
                TCComponent pserson = groupMember.getUser().getRelatedComponent("person");

                if ("Creator".equals(approveTask.getName())) {
                    item.setText(0, approveTask.getName());
                    item.setText(1, groupMemberProperties[0]);
                    item.setText(2, groupMemberProperties[1]);
                    item.setText(3, pserson.getProperty("PA10"));
                    if (signoffProperties[0] == null || signoffProperties[0].equals("")) {
                        item.setText(4, approveTask.getRoot().getProperty("creation_date"));
                        item.setText(5, "");
                    }
                } else {
                    item.setText(0, map.getTask());
                    item.setText(1, map.getTeam_name());
                    item.setText(2, map.getUser_name());
                    item.setData("puid", map.getTc_member_puid());
                    item.setText(3, pserson.getProperty("PA10"));
                    item.setText(4, signoffProperties[0]);
                    item.setText(5, signoffProperties[1]);
                }
            }
        } else {
            item.setText(0, map.getTask());
            item.setText(1, map.getTeam_name());
            item.setText(2, map.getUser_name());
            item.setData("puid", map.getTc_member_puid());
        }
    }

    private void setMandatory(Control con) {
        ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
        Registry registry = Registry.getRegistry("com.kgm.common.common");
        dec.setImage(registry.getImage("CONTROL_MANDATORY"));
        dec.setDescriptionText("This value will be required.");
        madatoryControls.add(con);
    }

    public Table getApprovalLineTable() {
        return approvalLineTable;
    }

    private void setOrg() {
        String user_group = "";

        if (null == mecoRevision) {
            try {
                user_group = (String) session.getGroup().getGroupName();
                if (user_group.startsWith("BODY")) {
                    cbOrganization.setSelectedItem("PB");
                    cbOrganization.setEnabled(false);
                } else if (user_group.startsWith("ASSEMBLY") || user_group.startsWith("ASSY")) {        //20140417, user_group이 ASSAY로 시작 되지 않고 ASSEMBLY로 시작 되므로 키워드를  ASSEMBLY로변경. 그리고 ASSY도 사용 가능성이 있어서 추가함.
                    cbOrganization.setSelectedItem("PA");
                    cbOrganization.setEnabled(false);
                } else if (user_group.startsWith("PAINT")) {
                    cbOrganization.setSelectedItem("PP");
                    cbOrganization.setEnabled(false);
                }
            } catch (Exception ex) {
                MessageBox.post(getShell(), "Failed to get user'group.", "WARNING", MessageBox.WARNING);
                ex.printStackTrace();
            }
        }
    }

    
    private BOPTYPE getBopType(String type) {
        BOPTYPE bopType;
        if (type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) || type.startsWith("P"))
            bopType = BOPTYPE.PAINT;
        else if (type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) || type.startsWith("B"))
            bopType = BOPTYPE.BODY;
        else
            bopType = BOPTYPE.ASSEMBLY;

        return bopType;
    }
}
