package com.ssangyong.commands.ec.ecostatus.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.ecostatus.model.EcoChangeData.StdInformData;
import com.ssangyong.commands.ec.ecostatus.model.EcoStatusData;
import com.ssangyong.commands.ec.ecostatus.model.OspecSelectData;
import com.ssangyong.commands.ec.ecostatus.operation.EcoStdInformCreateOperation;
import com.ssangyong.commands.ec.ecostatus.operation.EcoStdInformModifyOperation;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.DateControl;

/**
 * 기준정보 등록
 * 
 * @author baek
 * 
 */
public class EcoStdInformCreateDialog extends Dialog {

	private CCombo comboGModel = null;
	private CCombo comboProject = null;

	private Text textOspecId;
	private CCombo comboStageType = null;
	private Text textEstChangePeriod;
	private Text textCreateDate;
	private Text textChangeDesc;
	private Text textStatus;

	private DateControl dateCtrlEstApplyDate;
	private DateControl dateCtrlReceiptDate;
	private DateControl dateCtrlEcoCompleteReqDate;

	private Registry registry = null;
	private ArrayList<String> initGModelList = null; // G Model 리스트
	private ArrayList<String> initProjectList = null; // Project 리스트
	private EcoStatusData selectedRowData = null; // 선택된 Row Data
	private TCSession tcSession = null;
	private EventList<EcoStatusData> tableDataList = null;
	private ACTION_TYPE currentAction = null;
	private String currentGmodel = ""; // 현재 Gmodel
	private String currentProject = ""; // 현재 Project
	private Button btnSave = null;

	private static enum ACTION_TYPE {
		CREATE, MODIFY
	};

	/**
	 * 
	 * @param parentShell
	 * @param initGModelList
	 * @param initProjectList
	 * @param selectedRowData
	 * @param tableDataList
	 */
	public EcoStdInformCreateDialog(Shell parentShell, ArrayList<String> initGModelList, ArrayList<String> initProjectList, EcoStatusData selectedRowData,
			EventList<EcoStatusData> tableDataList) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL);
		this.initGModelList = initGModelList;
		this.initProjectList = initProjectList;
		this.selectedRowData = selectedRowData;
		this.tableDataList = tableDataList;
		this.registry = Registry.getRegistry(this);
		this.tcSession = CustomUtil.getTCSession();
		if (selectedRowData == null)
			currentAction = ACTION_TYPE.CREATE;
		else
			currentAction = ACTION_TYPE.MODIFY;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (selectedRowData == null)
			newShell.setText("기준정보 등록");
		else
			newShell.setText("기준정보 수정");
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Composite container = (Composite) super.createDialogArea(parent);

		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		container.setBackgroundMode(SWT.INHERIT_FORCE);

		GridLayout gl_container = new GridLayout(1, false);
		gl_container.marginWidth = 0;
		gl_container.marginHeight = 0;
		gl_container.verticalSpacing = 0;
		container.setLayout(gl_container);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section sectionbBasic = toolkit.createSection(container, Section.TITLE_BAR);
		GridData gd_sectionbBasic = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);
		sectionbBasic.setText("기준정보 ");

		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		GridLayout gl_compositeBasic = new GridLayout(4, false);
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);

		Label lblStageType = new Label(compositeBasic, SWT.NONE);
		lblStageType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblStageType, true, true);
		lblStageType.setText("분류");

		comboStageType = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboStageType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboStageType.widthHint = 160;
		comboStageType.setLayoutData(gd_comboStageType);
		toolkit.adapt(comboStageType);
		toolkit.paintBordersFor(comboStageType);
		makeMadatoryImage(comboStageType);

		Label lblGModel = new Label(compositeBasic, SWT.NONE);
		lblGModel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblGModel, true, true);
		lblGModel.setText("G-Model");

		comboGModel = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboGmodel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboGmodel.widthHint = 160;
		comboGModel.setLayoutData(gd_comboGmodel);
		toolkit.adapt(comboGModel);
		toolkit.paintBordersFor(comboGModel);
		makeMadatoryImage(comboGModel);
		comboGModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// Project 리스트 설정
					setProjectList(false);
					if (currentGmodel.equals(comboGModel.getText()))
						return;
					currentGmodel = comboGModel.getText();
					textOspecId.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		Label lblProject = new Label(compositeBasic, SWT.NONE);
		lblProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblProject, true, true);
		lblProject.setText("Project");

		comboProject = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProject.widthHint = 160;
		comboProject.setLayoutData(gd_comboProject);
		toolkit.adapt(comboProject);
		toolkit.paintBordersFor(comboProject);
		makeMadatoryImage(comboProject);
		comboProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {

					if (currentProject.equals(comboProject.getText()))
						return;
					currentProject = comboProject.getText();
					textOspecId.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		Label lblStatus = new Label(compositeBasic, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblStatus, true, true);
		lblStatus.setText("상태");

		textStatus = new Text(compositeBasic, SWT.BORDER);
		GridData gd_comboStatus = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboStatus.widthHint = 155;
		textStatus.setLayoutData(gd_comboStatus);
		textStatus.setEnabled(false);

		Composite compositeOspec = new Composite(compositeBasic, SWT.NONE);
		GridLayout gl_compositeOspec = new GridLayout(3, false);
		gl_compositeOspec.marginLeft = 18;
		gl_compositeOspec.marginHeight = 0;
		gl_compositeOspec.marginWidth = 0;
		compositeOspec.setLayout(gl_compositeOspec);
		compositeOspec.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		toolkit.adapt(compositeOspec);
		toolkit.paintBordersFor(compositeOspec);

		Label lblOspecId = new Label(compositeOspec, SWT.NONE);
		lblOspecId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblOspecId, true, true);
		lblOspecId.setText("구분(O/SPEC)");

		textOspecId = new Text(compositeOspec, SWT.BORDER);
		GridData gd_textOspecId = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textOspecId.widthHint = 407;
		textOspecId.setLayoutData(gd_textOspecId);
		toolkit.adapt(textOspecId, true, true);
		makeMadatoryImage(textOspecId);
		textOspecId.setEnabled(false);

		Button btnSearchOspec = new Button(compositeOspec, SWT.NONE);
		btnSearchOspec.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(btnSearchOspec, true, true);
		btnSearchOspec.setImage(SWTResourceManager.getImage(EcoStdInformCreateDialog.class, "/icons/search_16.png"));
		btnSearchOspec.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearchOspec();
			}
		});

		Label lblEstApplyDate = new Label(compositeBasic, SWT.NONE);
		lblEstApplyDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblEstApplyDate, true, true);
		lblEstApplyDate.setText("예상 적용시점");

		dateCtrlEstApplyDate = new DateControl(compositeBasic, null, "yyyy-MM-dd");
		GridData gd_dateCtrlEstApplyDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_dateCtrlEstApplyDate.widthHint = 164;
		dateCtrlEstApplyDate.setLayoutData(gd_dateCtrlEstApplyDate);
		dateCtrlEstApplyDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		dateCtrlEstApplyDate.setBackgroundMode(SWT.INHERIT_FORCE);
		makeMadatoryImage(dateCtrlEstApplyDate);

		Label lblReceiptDate = new Label(compositeBasic, SWT.NONE);
		lblReceiptDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblReceiptDate, true, true);
		lblReceiptDate.setText("O/SPEC 접수일");

		dateCtrlReceiptDate = new DateControl(compositeBasic, null, "yyyy-MM-dd");
		GridData gd_dateCtrlReceiptDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_dateCtrlReceiptDate.widthHint = 164;
		dateCtrlReceiptDate.setLayoutData(gd_dateCtrlReceiptDate);
		dateCtrlReceiptDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		dateCtrlReceiptDate.setBackgroundMode(SWT.INHERIT_FORCE);
		makeMadatoryImage(dateCtrlReceiptDate);

		Label lblEcoCompleteReqDate = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblEcoCompleteReqDate, true, true);
		lblEcoCompleteReqDate.setText("ECO 완료요청일");
		dateCtrlEcoCompleteReqDate = new DateControl(compositeBasic, null, "yyyy-MM-dd");
		GridData gd_dateCtrlEcoCompleteReqDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_dateCtrlEcoCompleteReqDate.widthHint = 164;
		dateCtrlEcoCompleteReqDate.setLayoutData(gd_dateCtrlEcoCompleteReqDate);
		dateCtrlEcoCompleteReqDate.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		dateCtrlEcoCompleteReqDate.setBackgroundMode(SWT.INHERIT_FORCE);

		Label lblEstChangePeriod = new Label(compositeBasic, SWT.NONE);
		lblEstChangePeriod.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblEstChangePeriod, true, true);
		lblEstChangePeriod.setText("예상 설계변경기간");

		textEstChangePeriod = new Text(compositeBasic, SWT.BORDER);
		textEstChangePeriod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(textEstChangePeriod, true, true);
		textEstChangePeriod.setEnabled(false);

		Label lblChangeDesc = new Label(compositeBasic, SWT.NONE);
		lblChangeDesc.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 2));
		toolkit.adapt(lblChangeDesc, true, true);
		lblChangeDesc.setText("변경내용");

		textChangeDesc = new Text(compositeBasic, SWT.BORDER);
		textChangeDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 2));
		toolkit.adapt(textChangeDesc, true, true);
		makeMadatoryImage(textChangeDesc);

		Label lblCreateDate = new Label(compositeBasic, SWT.NONE);
		lblCreateDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblCreateDate, true, true);
		lblCreateDate.setText("등록일");

		textCreateDate = new Text(compositeBasic, SWT.BORDER);
		textCreateDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(textCreateDate, true, true);
		textCreateDate.setEnabled(false);

		initDataLoad();
		return container;
	}

	/**
	 * OSPEC 검색
	 */
	protected void doSearchOspec() {

		String gModel = comboGModel.getText();
		String project = comboProject.getText();

		StringBuffer sb = new StringBuffer();

		if (gModel.isEmpty())
			sb.append("Select GModel\n");
		if (project.isEmpty())
			sb.append("Select Project");

		if (sb.length() > 0) {
			MessageBox.post(this.getShell(), sb.toString(), "Warning", MessageBox.WARNING);
			return;
		}

		OspecSelectData initData = new OspecSelectData();
		initData.setgModel(gModel);
		initData.setProjectNo(project);

		EcoStatusSelectOspecDialog dialog = new EcoStatusSelectOspecDialog(this.getShell(), initData);
		int returnValue = dialog.open();
		if (returnValue == Window.OK) {
			String ospecId = dialog.getSelectedOspecId();
			textOspecId.setText(ospecId);
		}

	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnSave = createButton(parent, IDialogConstants.OK_ID, "Save", true);
		Button btnClose = createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
		btnSave.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/save_16.png"));
		btnClose.setImage(SWTResourceManager.getImage(EcoChangeListMgrComposite.class, "/icons/cancel_16.png"));

		if (currentAction == ACTION_TYPE.CREATE)
			return;
		try {
			String registerUserId = selectedRowData.getRegisterUserId(); // 등록된UserId
			String loginUserId = tcSession.getUser().getUserId();
			String roleName = tcSession.getCurrentRole().getProperty("role_name");
			// 등록자와 현재 사용자 ID가 다르면 Save 버튼 비활성화
			if (!loginUserId.equals(registerUserId) && !roleName.equalsIgnoreCase("DBA"))
				btnSave.setEnabled(false);
		} catch (TCException e) {
			e.printStackTrace();
		}

	}

	private void makeMadatoryImage(Control con) {
		ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
		dec.setImage(registry.getImage("CONTROL_MANDATORY"));
		dec.setDescriptionText("This value will be required.");
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(600, 320);
	}

	private void initDataLoad() {

		try {

			/**
			 * G MODEL 리스트
			 */
			setGModelList();

			/**
			 * Project List
			 */
			setProjectList(true);

			/**
			 * 분류
			 */
			setStageList();
			if (currentAction == ACTION_TYPE.CREATE) {
				SimpleDateFormat createDateSd = new SimpleDateFormat("yyyy-MM-dd");
				Date toDay = new Date();

				String createDate = createDateSd.format(toDay);
				textCreateDate.setText(createDate);
				comboGModel.setFocus();
			} else {
				comboStageType.setText(selectedRowData.getStageType());
				comboProject.setText(selectedRowData.getProjectId());

				textStatus.setText(selectedRowData.getStatus());
				String gModel = getGmodelWithProject(selectedRowData.getProjectId());
				if (gModel != null) {
					comboGModel.setText(gModel);

				}
				textOspecId.setText(selectedRowData.getOspecId());

				dateCtrlEstApplyDate.setDate(selectedRowData.getEstApplyDate());
				dateCtrlReceiptDate.setDate(selectedRowData.getReceiptDate());
				dateCtrlEcoCompleteReqDate.setDate(selectedRowData.getEcoCompleteReqDate());

				textChangeDesc.setText(selectedRowData.getChangeDesc());
				textCreateDate.setText(selectedRowData.getRegisterDate());
				String estChangePeriod = selectedRowData.getEstChangePeriod();
				if (estChangePeriod != null)
					textEstChangePeriod.setText(estChangePeriod);

				currentProject = selectedRowData.getProjectId();
				currentGmodel = gModel;

				textChangeDesc.setFocus();
			}

		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * MODEL 리스트
	 * 
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String> setGModelList() throws Exception {
		comboGModel.removeAll();
		comboGModel.add("");
		comboGModel.select(0);

		for (String gmodel : initGModelList) {
			comboGModel.add(gmodel);
			comboGModel.setData(gmodel);
		}
		return initGModelList;
	}

	/**
	 * Project List 설정
	 * 
	 * @param isAllList
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<String> setProjectList(boolean isAllList) throws Exception {
		comboProject.removeAll();
		comboProject.add("");
		comboProject.select(0);

		ArrayList<String> projectList = null;

		if (isAllList)
			projectList = initProjectList;
		else {
			if (comboGModel.getSelectionIndex() > 0) {
				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				String gModel = (String) comboGModel.getText();
				ds.put("G_MODEL", gModel);
				projectList = (ArrayList<String>) remote.execute("com.ssangyong.service.OSpecService", "getProject", ds);
			} else
				projectList = initProjectList;
		}

		for (String project : projectList) {
			comboProject.add(project);
			comboProject.setData(project);
		}
		return projectList;
	}

	/**
	 * 분류 리스트
	 */
	private void setStageList() {
		comboStageType.removeAll();
		String[] statusList = registry.getStringArray("STAGE_TYPE.LIST");
		comboStageType.setItems(statusList);
		comboStageType.remove(0);
		comboStageType.select(0);
	}

	@Override
	protected void okPressed() {
		try {
			StdInformData inputData = new StdInformData();
			if (!isValidData(inputData))
				return;
			// 생성일 경우
			if (currentAction == ACTION_TYPE.CREATE) {
				EcoStdInformCreateOperation operation = new EcoStdInformCreateOperation(inputData, tableDataList);
				tcSession.queueOperation(operation);
			} else {
				EcoStdInformModifyOperation operation = new EcoStdInformModifyOperation(inputData, selectedRowData);
				tcSession.queueOperation(operation);
			}
			super.okPressed();
		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * 입력값 검증
	 * 
	 * @return
	 */
	private boolean isValidData(StdInformData inputData) throws Exception {
		StringBuilder errorMsg = new StringBuilder();
		String stageType = comboStageType.getText();
		String projectId = comboProject.getText();
		String ospecId = textOspecId.getText();
		Date estApplyDate = dateCtrlEstApplyDate.getDate();
		Date receiptDate = dateCtrlReceiptDate.getDate();
		Date ecoCompleteReqDate = dateCtrlEcoCompleteReqDate.getDate();
		String changeDesc = textChangeDesc.getText();

		if (stageType.isEmpty())
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "분류").concat("\n"));

		if (projectId.isEmpty())
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "Project").concat("\n"));

		if (ospecId.isEmpty())
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "구분(O/SPEC)").concat("\n"));

		if (estApplyDate == null)
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "예상 적용시점").concat("\n"));

		if (receiptDate == null)
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "O/SPEC 접수일").concat("\n"));

		if (changeDesc.isEmpty())
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "변경내용").concat("\n"));

		if (errorMsg.length() > 0) {
			MessageBox.post(this.getShell(), errorMsg.toString(), "Warning", MessageBox.WARNING);
			return false;
		}

		// 수정일 경우 Project, O/SPEC, 분류 가 변경되었는지 유무를 확인함. 자신일 경우 데이터 체크 안함
		boolean isNotChangedBaseContent = currentAction == ACTION_TYPE.MODIFY && projectId.equals(selectedRowData.getProjectId())
				&& ospecId.equals(selectedRowData.getOspecId()) && stageType.equals(selectedRowData.getStageType());

		/**
		 * 이미 데이터가 존재하는 지 체크함
		 */
		if (currentAction == ACTION_TYPE.CREATE || !isNotChangedBaseContent) {
			CustomECODao dao = new CustomECODao();
			DataSet ds = new DataSet();
			ds.put("PROJECT_NO", projectId);
			ds.put("OSPEC_ID", ospecId);
			ds.put("STAGE_TYPE", stageType);
			ArrayList<String> dupMasterPuidList = dao.getDupRptInfoList(ds);
			if (dupMasterPuidList.size() > 0) {
				StringBuffer msgSb = new StringBuffer();
				msgSb.append("이미 데이터가 존재합니다.\n[분류, Project, 구분(O/Spec)]\n");
				msgSb.append(stageType + ", " + projectId + ", " + ospecId);
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), msgSb.toString(), "Warning", MessageBox.WARNING);
				return false;
			}
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String applyDate = estApplyDate != null ? dateFormat.format(estApplyDate) : null;
		String receiptDateStr = receiptDate != null ? dateFormat.format(receiptDate) : null;
		String ecoCompleteReqDateStr = ecoCompleteReqDate != null ? dateFormat.format(ecoCompleteReqDate) : null;

		inputData.setProjectId(projectId);
		inputData.setOspecId(ospecId);
		inputData.setStageType(stageType);
		inputData.setChangeDesc(changeDesc);
		inputData.setRegisterType("시스템");
		inputData.setApplyDate(applyDate);
		inputData.setReceiptDate(receiptDateStr);
		inputData.setEcoCompleteReqDate(ecoCompleteReqDateStr);
		return true;
	}

	/**
	 * Project 의 GModel 을 가져옴
	 * 
	 * @param projectNo
	 * @return
	 * @throws Exception
	 */
	private String getGmodelWithProject(String projectNo) throws Exception {
		CustomECODao dao = new CustomECODao();
		String sysGuid = dao.getGmodelWithProject(projectNo);
		return sysGuid;
	}
}
