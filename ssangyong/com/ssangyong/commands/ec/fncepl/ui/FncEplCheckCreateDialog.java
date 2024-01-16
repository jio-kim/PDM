package com.ssangyong.commands.ec.fncepl.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.ssangyong.commands.ec.fncepl.model.FncEplCheckData;
import com.ssangyong.commands.ec.fncepl.operation.FncEplCheckCreateOperation;
import com.ssangyong.commands.ec.fncepl.operation.FncEplCheckModifyOperation;
import com.ssangyong.commands.ec.search.ECOSearchDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.DateControl;

/**
 * Function EPL 등록/수정 Dilaog
 * 
 * @author baek
 * 
 */
public class FncEplCheckCreateDialog extends Dialog {
	private Text textFunctionNo;
	private Text textDescription;
	private CCombo comboProdNo;
	private CCombo comboAddEcoPublish;
	private Text textApplyEco;
	private Text textFileUpload;
	private Registry registry = null;
	private EventList<FncEplCheckData> tableDataList = null;
	private DateControl dateCtlBaseDate = null;
	private Button btnVehicleType;
	private Button btnPowerTrain;
	private File uploadFile = null;
	private TCSession tcSession = null;
	private FncEplCheckData selectedRowData = null;
	private ACTION_TYPE currentAction; // 등록/수정여부
	private boolean isFileDelete = false;// delete 버튼 클릭여부

	/**
	 * 생성/ 수정 타입
	 * 
	 * @author baek
	 * 
	 */
	private static enum ACTION_TYPE {
		CREATE, MODIFY
	};

	/**
	 * 생성자
	 * 
	 * @param parentShell
	 * @param selectedRowData
	 * @param tableDataList
	 */
	public FncEplCheckCreateDialog(Shell parentShell, FncEplCheckData selectedRowData, EventList<FncEplCheckData> tableDataList) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL);
		registry = Registry.getRegistry("com.ssangyong.commands.ec.ecostatus.ui.ui");
		this.selectedRowData = selectedRowData;
		this.tableDataList = tableDataList;
		tcSession = CustomUtil.getTCSession();
		if (selectedRowData == null)
			currentAction = ACTION_TYPE.CREATE;
		else
			currentAction = ACTION_TYPE.MODIFY;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (selectedRowData == null)
			newShell.setText("Function Epl Check 등록");
		else
			newShell.setText("Function Epl Check 수정");

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

		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		GridLayout gl_compositeBasic = new GridLayout(4, false);
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);

		btnVehicleType = new Button(compositeBasic, SWT.RADIO);
		btnVehicleType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comboValueSetting(comboProdNo, "S7_PLANT_CODE");
			}
		});
		toolkit.adapt(btnVehicleType, true, true);
		btnVehicleType.setText("Vehicle");

		btnPowerTrain = new Button(compositeBasic, SWT.RADIO);
		btnPowerTrain.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comboValueSetting(comboProdNo, "S7_PRODUCT_CODE");
			}
		});
		toolkit.adapt(btnPowerTrain, true, true);
		btnPowerTrain.setText("Power Train");
		new Label(compositeBasic, SWT.NONE);
		new Label(compositeBasic, SWT.NONE);

		Label lblProdNo = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblProdNo, true, true);
		lblProdNo.setText("Prod. No.");

		comboProdNo = new CCombo(compositeBasic, SWT.BORDER | SWT.READ_ONLY);
		GridData gd_comboProdNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProdNo.widthHint = 180;
		comboProdNo.setLayoutData(gd_comboProdNo);
		new Label(compositeBasic, SWT.NONE);
		comboProdNo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				textFunctionNo.setFocus();
			}
		});
		makeMadatoryImage(comboProdNo);
		new Label(compositeBasic, SWT.NONE);

		Label lblBaseDate = new Label(compositeBasic, SWT.NONE);
		lblBaseDate.setText("\uAE30\uC900 Date");
		toolkit.adapt(lblBaseDate, true, true);

		dateCtlBaseDate = new DateControl(compositeBasic, new Date(), "yyyy-MM-dd");
		toolkit.adapt(dateCtlBaseDate);
		toolkit.paintBordersFor(dateCtlBaseDate);
		new Label(compositeBasic, SWT.NONE);
		new Label(compositeBasic, SWT.NONE);
		makeMadatoryImage(dateCtlBaseDate);

		Label lblFunctionId = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblFunctionId, true, true);
		lblFunctionId.setText("Function");

		textFunctionNo = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textFunctionNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textFunctionNo.widthHint = 180;
		textFunctionNo.setLayoutData(gd_textFunctionNo);
		toolkit.adapt(textFunctionNo, true, true);
		new Label(compositeBasic, SWT.NONE);
		new Label(compositeBasic, SWT.NONE);
		makeMadatoryImage(textFunctionNo);
		textFunctionNo.setTextLimit(4);

		Label lblUploadFile = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblUploadFile, true, true);
		lblUploadFile.setText("File Upload");

		textFileUpload = new Text(compositeBasic, SWT.BORDER | SWT.READ_ONLY);
		textFileUpload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(textFileUpload, true, true);

		Button btnUpload = new Button(compositeBasic, SWT.NONE);
		btnUpload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uploadFile();
			}
		});
		toolkit.adapt(btnUpload, true, true);
		btnUpload.setText("Upload");
		btnUpload.setImage(SWTResourceManager.getImage(FncEplCheckCreateDialog.class, "/icons/upload_16.png"));

		Button btnDelete = new Button(compositeBasic, SWT.NONE);
		toolkit.adapt(btnDelete, true, true);
		btnDelete.setText("Delete");
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteFile();
			}
		});

		Label lblApplyEco = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblApplyEco, true, true);
		lblApplyEco.setText("\uBC18\uC601 ECO");

		textApplyEco = new Text(compositeBasic, SWT.BORDER | SWT.READ_ONLY);
		textApplyEco.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(textApplyEco, true, true);

		Button btnApplyEcoSearch = new Button(compositeBasic, SWT.NONE);
		toolkit.adapt(btnApplyEcoSearch, true, true);
		btnApplyEcoSearch.setImage(SWTResourceManager.getImage(FncEplCheckCreateDialog.class, "/icons/search_16.png"));
		new Label(compositeBasic, SWT.NONE);
		btnApplyEcoSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectEco();
			}
		});

		Label lblAddEcoPublish = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblAddEcoPublish, true, true);
		lblAddEcoPublish.setText("\uCD94\uAC00 ECO \uBC1C\uD589");

		comboAddEcoPublish = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboAddEcoPublish = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboAddEcoPublish.widthHint = 150;
		comboAddEcoPublish.setLayoutData(gd_comboAddEcoPublish);
		toolkit.adapt(comboAddEcoPublish);
		toolkit.paintBordersFor(comboAddEcoPublish);
		new Label(compositeBasic, SWT.NONE);
		new Label(compositeBasic, SWT.NONE);

		Label lblDescription = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblDescription, true, true);
		lblDescription.setText("\uBE44\uACE0");

		textDescription = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
		gd_textDescription.heightHint = 40;
		textDescription.setLayoutData(gd_textDescription);
		toolkit.adapt(textDescription, true, true);

		initDataLoad();

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Button btnSave = createButton(parent, IDialogConstants.OK_ID, "Save", true);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnSave.setImage(SWTResourceManager.getImage(FncEplCheckCreateDialog.class, "/icons/save_16.png"));
		Button btnClose = createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
		btnClose.setImage(SWTResourceManager.getImage(FncEplCheckCreateDialog.class, "/icons/cancel_16.png"));
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(507, 380);
	}

	/**
	 * 초기 Data Load
	 */
	private void initDataLoad() {
		try {
			comboAddEcoPublish.setItems(FncEplCheckData.ECO_PUBLISH_LIST);

			if (currentAction == ACTION_TYPE.MODIFY) {
				String ecoType = selectedRowData.getEcoType();
				String baseDate = selectedRowData.getBaseDate();
				String functionNo = selectedRowData.getFunctionNo();
				String attachFilePuid = selectedRowData.getAttachFilePuid();
				String applyEcoNo = selectedRowData.getApplyEcoNo();
				String addEcoPublish = selectedRowData.getAddEcoPublish();
				String description = selectedRowData.getDescription();
				String prodDspName = selectedRowData.getProdDspName();

				// Vehicle / Power Train
				if ("V".equals(ecoType)) {
					btnVehicleType.setSelection(true);
					comboValueSetting(comboProdNo, "S7_PLANT_CODE");
				} else {
					btnPowerTrain.setSelection(true);
					comboValueSetting(comboProdNo, "S7_PRODUCT_CODE");
				}
				// Prod. No.
				comboProdNo.setText(prodDspName);

				// 기준 Date
				dateCtlBaseDate.setDate(baseDate);
				// Function
				textFunctionNo.setText(functionNo);
				// 첨부파일
				if (attachFilePuid != null && !attachFilePuid.isEmpty()) {
					TCComponent comp = tcSession.stringToComponent(attachFilePuid);
					if (comp != null) {
						TCComponentDataset dataSetComp = (TCComponentDataset) comp;
						TCComponentTcFile[] tcFiles = dataSetComp.getTcFiles();
						String fileName = tcFiles[0].getProperty("original_file_name");
						textFileUpload.setText(fileName);
					}
				}
				textApplyEco.setText(applyEcoNo);
				comboAddEcoPublish.setText(addEcoPublish);
				textDescription.setText(description);
			}

		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * Combo Box 값 설정
	 * 
	 * @param combo
	 * @param lovName
	 */
	private void comboValueSetting(CCombo combo, String lovName) {
		try {
			combo.removeAll();
			TCSession session = CustomUtil.getTCSession();
			if (lovName != null) {
				TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
				TCComponentListOfValues[] listofvalues = listofvaluestype.find(lovName);
				if (listofvalues == null || listofvalues.length == 0) {
					return;
				}
				TCComponentListOfValues listofvalue = listofvalues[0];
				String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
				String[] lovDesces = listofvalue.getListOfValues().getDescriptions();
				int i = 0;
				for (String lovValue : lovValues) {
					combo.add(lovValue + " (" + lovDesces[i] + ")");
					combo.setData(lovValue);
					i++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void makeMadatoryImage(Control con) {
		ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
		dec.setImage(registry.getImage("CONTROL_MANDATORY"));
		dec.setDescriptionText("This value will be required.");
	}

	@Override
	protected void okPressed() {
		FncEplCheckData inputData = new FncEplCheckData();
		try {
			if (!isValidData(inputData))
				return;

			if (currentAction == ACTION_TYPE.CREATE) {
				FncEplCheckCreateOperation operation = new FncEplCheckCreateOperation(inputData, tableDataList);
				tcSession.queueOperation(operation);
			} else {
				FncEplCheckModifyOperation operation = new FncEplCheckModifyOperation(inputData, selectedRowData);
				tcSession.queueOperation(operation);
			}
			super.okPressed();
		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * Validation 체크
	 * 
	 * @param inputData
	 * @return
	 * @throws Exception
	 */
	private boolean isValidData(FncEplCheckData inputData) throws Exception {
		StringBuilder errorMsg = new StringBuilder();

		String prodNo = comboProdNo.getText();
		String functionNo = textFunctionNo.getText();
		Date baseDate = dateCtlBaseDate.getDate();
		String applyEcoNo = textApplyEco.getText();
		String addEcoPublish = comboAddEcoPublish.getText();
		String description = textDescription.getText();

		if (prodNo.isEmpty())
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "Prod. No.").concat("\n"));

		if (functionNo.isEmpty())
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "Function").concat("\n"));

		if (baseDate == null)
			errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "기준 Date").concat("\n"));

		if (errorMsg.length() > 0) {
			MessageBox.post(this.getShell(), errorMsg.toString(), "Warning", MessageBox.WARNING);
			return false;
		}

		if (uploadFile != null && !uploadFile.exists()) {
			MessageBox.post(this.getShell(), "Uploaded File not exist. Upload file again.", "WARNING", MessageBox.WARNING);
			return false;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String baseDateStr = baseDate != null ? dateFormat.format(baseDate) : null;

		String attachFilePath = uploadFile != null ? uploadFile.getAbsolutePath() : null;

		inputData.setProdNo(prodNo);
		inputData.setFunctionNo(functionNo);
		inputData.setEcoType(btnVehicleType.getSelection() ? "V" : "P");
		inputData.setApplyEcoNo(applyEcoNo);
		inputData.setAddEcoPublish(addEcoPublish);
		inputData.setDescription(description);
		inputData.setAttachFilePath(attachFilePath);
		inputData.setRegisterId(tcSession.getUser().getUserId());
		inputData.setBaseDate(baseDateStr);
		inputData.setAttachFileDelete(isFileDelete);

		return true;
	}

	/**
	 * 파일 선택
	 * 
	 * @return
	 */
	private String selectUploadFile() {
		FileDialog fDialog = new FileDialog(this.getShell(), SWT.SINGLE);
		fDialog.setFilterNames(new String[] { "Excel File" });
		fDialog.setFilterExtensions(new String[] { "*.xlsx;*.xls", "*.xlsx", "*.xls", "*.*" });
		String selectFile = fDialog.open();
		if (selectFile == null)
			return null;
		String selectFileName = fDialog.getFileName();
		if ((selectFileName == null) || (selectFileName.equals("")))
			return null;

		String filterPath = fDialog.getFilterPath();
		return filterPath + File.separatorChar + selectFileName;
	}

	private void uploadFile() {
		String templateFilePath = selectUploadFile();
		if (templateFilePath == null || templateFilePath.isEmpty()) {
			MessageBox.post(this.getShell(), " The file to upload is not selected.", "WARNING", MessageBox.WARNING);
			return;
		}

		File file = new File(templateFilePath);
		if (!file.exists()) {
			MessageBox.post(this.getShell(), "File not exist", "WARNING", MessageBox.WARNING);
			return;
		}
		textFileUpload.setText(file.getName());
		uploadFile = file;
	}

	/**
	 * 업로드 파일 제거
	 */
	private void deleteFile() {
		uploadFile = null;
		textFileUpload.setText("");
		isFileDelete = true;
	}

	/**
	 * ECO 선택
	 */
	private void selectEco() {
		ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(getShell(), SWT.SINGLE, false, true);
		ecoSearchDialog.setAllMaturityButtonsEnabled(false);
		ecoSearchDialog.setBInProcessSelect(false);
		ecoSearchDialog.setBWorkingSelect(false);
		ecoSearchDialog.clearOwningUser();
		ecoSearchDialog.open();

		TCComponentItemRevision[] selectedEcoRevList = ecoSearchDialog.getSelectctedECO();
		if (selectedEcoRevList == null || selectedEcoRevList.length == 0)
			return;

		TCComponentItemRevision selectedEcoRevision = selectedEcoRevList[0];

		try {
			textApplyEco.setText(selectedEcoRevision.getProperty(IPropertyName.ITEM_ID));
		} catch (TCException e) {
			e.printStackTrace();
		}

	}

}
