package com.ssangyong.commands.ec.eco.admincheck.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.eco.admincheck.common.ECOAdminCheckConstants;
import com.ssangyong.commands.ec.eco.admincheck.dialog.ECOAdminCheckCommonMemoDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.controls.SWTComboBox;

public class ECOAdminCheckBasicInfoPanel {
	private Composite cpsParent;
	private TCComponentItemRevision ecoRevision;
	private HashMap<String, String> hmDataPropertiesOld = new HashMap<String, String>();
	
//	// 변경사유1
//	private SWTComboBox cmbChangeCause1;
//	// 변경사유1 End Item 1
//	private Text txtEndItemCount1A;
//	// 변경사유1 End Item 2
//	private Text txtEndItemCount1M;
//	// 변경사유2
//	private SWTComboBox cmbChangeCause2;
//	// 변경사유2 End Item 1
//	private Text txtEndItemCount2A;
//	// 변경사유2 End Item 2
//	private Text txtEndItemCount2M;
	
	// Common Memo Button
	private Button btnCommonMemo;
	// 양산 Project
	private Text txtRegularProjectCode;
	// 신규 Project
	private Text txtNewProjectCode;
	// Admin Check
	private Text txtAdminCheck;
	// 비고
	private Text txtNote;
	// 메모
	private Text txtMemo;
	
	private ArrayList<Control> alControls = new ArrayList<Control>();
	
	private String[] saProperties = new String[] {
//			ECOAdminCheckConstants.PROP_CHANGE_CAUSE_1,
//			ECOAdminCheckConstants.PROP_END_ITEM_COUNT1_A,
//			ECOAdminCheckConstants.PROP_END_ITEM_COUNT1_M,
//			ECOAdminCheckConstants.PROP_CHANGE_CAUSE_2,
//			ECOAdminCheckConstants.PROP_END_ITEM_COUNT2_A,
//			ECOAdminCheckConstants.PROP_END_ITEM_COUNT2_M,
			ECOAdminCheckConstants.PROP_REGULAR_PROJECT_CODE,
			ECOAdminCheckConstants.PROP_NEW_PROJECT_CODE,
			ECOAdminCheckConstants.PROP_ADMIN_CHECK,
			ECOAdminCheckConstants.PROP_NOTE,
			ECOAdminCheckConstants.PROP_MEMO
			};
	
//	private int iTotalCount = -1;
	
	public ECOAdminCheckBasicInfoPanel(Composite cpsParent, TCComponentItemRevision ecoRevision) {
		this.cpsParent = cpsParent;
		this.ecoRevision = ecoRevision;
		
		initialize();
	}

	private void initialize() {
		VerifyListener verifyAdapterForUppercase = createVerifyAdapterForUppercase();
//		VerifyListener verifyAdapterForNumber = createVerifyAdapterForNumber();
		
		/*
		 * Basic Info
		 */
		Group groupBasicInfo = new Group(cpsParent, SWT.NONE);
		groupBasicInfo.setLayout(new GridLayout(6, false));
		groupBasicInfo.setText("Basic Info");
		groupBasicInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// [SR없음][2015.12.28][jclee] 변경사유 Project 별 분리
		// E/Item Total Count
//		GridData gdTotalCountLbl = new GridData();
//		gdTotalCountLbl.horizontalAlignment = SWT.RIGHT;
//		gdTotalCountLbl.horizontalSpan = 2;
//		Label lblTotalCount = new Label(groupCause, SWT.NONE);
//		
//		try {
//			CustomECODao dao = new CustomECODao();
//			String sECONo = ecoRevision.getProperty("item_id");
//			ArrayList<HashMap<String, String>> result = dao.selectECOEplEndItemList(sECONo);
//			
//			iTotalCount = result.size();
//			
//			lblTotalCount.setText("E/Item Total Name Group Count : " + iTotalCount);
//		} catch (Exception e) {
//			e.printStackTrace();
//			MessageBox.post(e);
//		}
//		
//		lblTotalCount.setLayoutData(gdTotalCountLbl);
//
//		// Change Cause Description
//		GridData gdChangeCauseDescLbl = new GridData();
//		gdChangeCauseDescLbl.horizontalAlignment = SWT.RIGHT;
//		gdChangeCauseDescLbl.horizontalSpan = 4;
//		Label lblChangeCauseDesc = new Label(groupCause, SWT.NONE);
//		lblChangeCauseDesc.setText("01: 설계변경(품질개선,원가절감,법규,도면수정,공용화등)\n" +
//								   "02: 신규 추가(O/SPEC,신규 추가,승인도 배포)\n" +
//								   "03: EPL 정리(사양삭제,Option 변경, S/MODE,DR,SEQ,DMU등)");
//		lblChangeCauseDesc.setLayoutData(gdChangeCauseDescLbl);
//
//		// 변경사유1
//		GridData gdChangeCause1Lbl = new GridData();
//		gdChangeCause1Lbl.horizontalAlignment = SWT.RIGHT;
//		Label lblChangeCause1 = new Label(groupCause, SWT.NONE);
//		lblChangeCause1.setText("변경사유1");
//		lblChangeCause1.setLayoutData(gdChangeCause1Lbl);
//
//		GridData gdChangeCause1 = new GridData();
//		gdChangeCause1.widthHint = 80;
//		cmbChangeCause1 = new SWTComboBox(groupCause, SWT.BORDER);
//		cmbChangeCause1.setLayoutData(gdChangeCause1);
//		cmbChangeCause1.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_CHANGE_CAUSE_1);
//		comboLOVSetting(cmbChangeCause1, ECOAdminCheckConstants.LOV_CHANGE_CAUSE);
//		cmbChangeCause1.addPropertyChangeListener(new IPropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent arg0) {
//				setCountControlsEnabled();
//			}
//		});
//		alControls.add(cmbChangeCause1);
//		
//		GridData gdEndItemCount1A = new GridData();
//		gdEndItemCount1A.widthHint = 100;
//		txtEndItemCount1A = new Text(groupCause, SWT.BORDER);
//		txtEndItemCount1A.setLayoutData(gdEndItemCount1A);
//		txtEndItemCount1A.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_END_ITEM_COUNT1_A);
//		txtEndItemCount1A.setEnabled(false);
//		txtEndItemCount1A.addKeyListener(new KeyAdapter() {
//			@Override
//			public void keyPressed(KeyEvent paramKeyEvent) {
//				if (paramKeyEvent.keyCode == SWT.CR || paramKeyEvent.keyCode == SWT.KEYPAD_CR) {
//					setTxtEndItemCount2ACalculated();
//				}
//			}
//		});
//		txtEndItemCount1A.addVerifyListener(verifyAdapterForNumber);
//		alControls.add(txtEndItemCount1A);
//		
//		GridData gdEndItemCount1ALbl = new GridData();
//		gdEndItemCount1ALbl.horizontalAlignment = SWT.LEFT;
//		Label lblEndItemCount1A = new Label(groupCause, SWT.NONE);
//		lblEndItemCount1A.setText("EA");
//		lblEndItemCount1A.setLayoutData(gdEndItemCount1ALbl);
//
//		GridData gdEndItemCount1M = new GridData();
//		gdEndItemCount1M.widthHint = 100;
//		txtEndItemCount1M = new Text(groupCause, SWT.BORDER);
//		txtEndItemCount1M.setLayoutData(gdEndItemCount1M);
//		txtEndItemCount1M.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_END_ITEM_COUNT1_M);
//		txtEndItemCount1M.setEnabled(false);
//		txtEndItemCount1M.addVerifyListener(verifyAdapterForNumber);
//		alControls.add(txtEndItemCount1M);
//
//		GridData gdEndItemCount1MLbl = new GridData();
//		gdEndItemCount1MLbl.horizontalAlignment = SWT.LEFT;
//		Label lblEndItemCount1M = new Label(groupCause, SWT.NONE);
//		lblEndItemCount1M.setText("EA");
//		lblEndItemCount1M.setLayoutData(gdEndItemCount1MLbl);
//
//		// 변경사유2
//		GridData gdChangeCause2Lbl = new GridData();
//		gdChangeCause2Lbl.horizontalAlignment = SWT.RIGHT;
//		Label lblChangeCause2 = new Label(groupCause, SWT.NONE);
//		lblChangeCause2.setText("변경사유2");
//		lblChangeCause2.setLayoutData(gdChangeCause2Lbl);
//
//		GridData gdChangeCause2 = new GridData();
//		gdChangeCause2.widthHint = 80;
//		cmbChangeCause2 = new SWTComboBox(groupCause, SWT.BORDER);
//		cmbChangeCause2.setLayoutData(gdChangeCause2);
//		cmbChangeCause2.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_CHANGE_CAUSE_2);
//		comboLOVSetting(cmbChangeCause2, ECOAdminCheckConstants.LOV_CHANGE_CAUSE);
//		cmbChangeCause2.addPropertyChangeListener(new IPropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent arg0) {
//				setCountControlsEnabled();
//			}
//		});
//		alControls.add(cmbChangeCause2);
//
//		GridData gdEndItemCount2A = new GridData();
//		gdEndItemCount2A.widthHint = 100;
//		txtEndItemCount2A = new Text(groupCause, SWT.BORDER);
//		txtEndItemCount2A.setLayoutData(gdEndItemCount2A);
//		txtEndItemCount2A.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_END_ITEM_COUNT2_A);
//		txtEndItemCount2A.setEnabled(false);
//		txtEndItemCount2A.addVerifyListener(verifyAdapterForNumber);
//		alControls.add(txtEndItemCount2A);
//
//		GridData gdEndItemCount2ALbl = new GridData();
//		gdEndItemCount2ALbl.horizontalAlignment = SWT.LEFT;
//		Label lblEndItemCount2A = new Label(groupCause, SWT.NONE);
//		lblEndItemCount2A.setText("EA");
//		lblEndItemCount2A.setLayoutData(gdEndItemCount2ALbl);
//
//		GridData gdEndItemCount2M = new GridData();
//		gdEndItemCount2M.widthHint = 100;
//		txtEndItemCount2M = new Text(groupCause, SWT.BORDER);
//		txtEndItemCount2M.setLayoutData(gdEndItemCount2M);
//		txtEndItemCount2M.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_END_ITEM_COUNT2_M);
//		txtEndItemCount2M.setEnabled(false);
//		txtEndItemCount2M.addVerifyListener(verifyAdapterForNumber);
//		alControls.add(txtEndItemCount2M);
//
//		GridData gdEndItemCount2MLbl = new GridData();
//		gdEndItemCount2MLbl.horizontalAlignment = SWT.LEFT;
//		Label lblEndItemCount2M = new Label(groupCause, SWT.NONE);
//		lblEndItemCount2M.setText("EA");
//		lblEndItemCount2M.setLayoutData(gdEndItemCount2MLbl);
		
		// 양산 Project
		GridData gdRegularProjectLbl = new GridData();
		gdRegularProjectLbl.horizontalAlignment = SWT.RIGHT;
		Label lblRegularProject = new Label(groupBasicInfo, SWT.NONE);
		lblRegularProject.setText("양산 Project");
		lblRegularProject.setLayoutData(gdRegularProjectLbl);

		GridData gdRegularProjectCode = new GridData(GridData.FILL_HORIZONTAL);
		gdRegularProjectCode.horizontalSpan = 5;
		txtRegularProjectCode = new Text(groupBasicInfo, SWT.BORDER);
		txtRegularProjectCode.setLayoutData(gdRegularProjectCode);
		txtRegularProjectCode.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_REGULAR_PROJECT_CODE);
		txtRegularProjectCode.addVerifyListener(verifyAdapterForUppercase);
		alControls.add(txtRegularProjectCode);

		// 신규 Project
		GridData gdNewProjectLbl = new GridData();
		gdNewProjectLbl.horizontalAlignment = SWT.RIGHT;
		Label lblNewProject = new Label(groupBasicInfo, SWT.NONE);
		lblNewProject.setText("신규 Project");
		lblNewProject.setLayoutData(gdNewProjectLbl);

		GridData gdNewProjectCode = new GridData(GridData.FILL_HORIZONTAL);
		gdNewProjectCode.horizontalSpan = 5;
		txtNewProjectCode = new Text(groupBasicInfo, SWT.BORDER);
		txtNewProjectCode.setLayoutData(gdNewProjectCode);
		txtNewProjectCode.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_NEW_PROJECT_CODE);
		txtNewProjectCode.addVerifyListener(verifyAdapterForUppercase);
		alControls.add(txtNewProjectCode);

		// Admin Check
		GridData gdAdminCheckLbl = new GridData();
		gdAdminCheckLbl.horizontalAlignment = SWT.RIGHT;
		Label lblAdminCheck = new Label(groupBasicInfo, SWT.NONE);
		lblAdminCheck.setText("Admin Check");
		lblAdminCheck.setLayoutData(gdAdminCheckLbl);

		GridData gdAdminCheck = new GridData(GridData.FILL_HORIZONTAL);
		gdAdminCheck.horizontalSpan = 5;
		txtAdminCheck = new Text(groupBasicInfo, SWT.BORDER);
		txtAdminCheck.setLayoutData(gdAdminCheck);
		txtAdminCheck.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_ADMIN_CHECK);
		alControls.add(txtAdminCheck);

		// 비고
		GridData gdNoteLbl = new GridData();
		gdNoteLbl.horizontalAlignment = SWT.RIGHT;
		Label lblNote = new Label(groupBasicInfo, SWT.NONE);
		lblNote.setText("비고");
		lblNote.setLayoutData(gdNoteLbl);

		GridData gdNote = new GridData(GridData.FILL_HORIZONTAL);
		gdNote.horizontalSpan = 5;
		txtNote = new Text(groupBasicInfo, SWT.BORDER);
		txtNote.setLayoutData(gdNote);
		txtNote.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_NOTE);
		alControls.add(txtNote);
		
		// Memo
		GridData gdMemoLbl = new GridData();
		gdMemoLbl.horizontalAlignment = SWT.RIGHT;
		Label lblMemo = new Label(groupBasicInfo, SWT.NONE);
		lblMemo.setText("메모");
		lblMemo.setLayoutData(gdMemoLbl);
		
		GridData gdMemo = new GridData(GridData.FILL_HORIZONTAL);
		gdMemo.horizontalSpan = 4;
		gdMemo.heightHint = 120;
		txtMemo = new Text(groupBasicInfo, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		txtMemo.setLayoutData(gdMemo);
		txtMemo.setData(ECOAdminCheckConstants.PROP, ECOAdminCheckConstants.PROP_MEMO);
		alControls.add(txtMemo);

		// Common Memo
		GridData gdCommonMemoBtn = new GridData(120, 30);
		gdCommonMemoBtn.horizontalAlignment = SWT.RIGHT;
		gdCommonMemoBtn.verticalAlignment = SWT.TOP;
		
		btnCommonMemo = new Button(groupBasicInfo, SWT.PUSH);
		btnCommonMemo.setLayoutData(gdCommonMemoBtn);
		btnCommonMemo.setText("Common Memo");
		btnCommonMemo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				openAdminCheckCommonMemo();
			}
		});
	}
	
	/**
	 * Open Common Memo Dialog
	 */
	private void openAdminCheckCommonMemo() {
		ECOAdminCheckCommonMemoDialog dialog = new ECOAdminCheckCommonMemoDialog(cpsParent.getShell(), SWT.DIALOG_TRIM | SWT.MIN, this);
		dialog.open();
	}

	/**
	 * 자동 대문자 변경 Listener
	 * @return
	 */
	private VerifyListener createVerifyAdapterForUppercase() {
		return new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent event) {
				event.text = event.text.toUpperCase();
			}
		};
	}

//	/**
//	 * 숫자만 입력 가능하도록 변경 Listener
//	 * @return
//	 */
//	private VerifyListener createVerifyAdapterForNumber() {
//		return new VerifyListener() {
//			@Override
//			public void verifyText(VerifyEvent event) {
//				Text text = (Text)event.getSource();
//
//	            final String oldS = text.getText();
//	            String newS = oldS.substring(0, event.start) + event.text + oldS.substring(event.end);
//
//	            boolean isInteger = true;
//	            try {
//	                Integer.parseInt(newS);
//	            } catch(NumberFormatException ex) {
//	                isInteger = false;
//	            }
//	            
//	            boolean isDeleteOrEnter = false;
//	            if (event.keyCode == 0 || event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR || event.keyCode == SWT.DEL || event.keyCode == SWT.BS) {
//	            	isDeleteOrEnter = true;
//				}
//	            
//	            if(!isInteger && !isDeleteOrEnter)
//	            	event.doit = false;
//			}
//		};
//	}
//
//	/**
//	 * 변경사유 선택 여부에 따른 E/Item 수량 입력 가능 여부 설정
//	 */
//	private void setCountControlsEnabled() {
//		boolean isSelectedChangeCause1 = false;
//		boolean isSelectedChangeCause2 = false;
//
//		isSelectedChangeCause1 = cmbChangeCause1.getSelectedItem() != null && !cmbChangeCause1.getSelectedItem().toString().equals("");
//		isSelectedChangeCause2 = cmbChangeCause2.getSelectedItem() != null && !cmbChangeCause2.getSelectedItem().toString().equals("");
//		
//		if (!isSelectedChangeCause1) {
//			txtEndItemCount1A.setText("");
//			txtEndItemCount1M.setText("");
//		}
//		
//		if (!isSelectedChangeCause2) {
//			txtEndItemCount2A.setText("");
//			txtEndItemCount2M.setText("");
//		}
//		
//		txtEndItemCount1A.setEnabled(isSelectedChangeCause1);
//		txtEndItemCount1M.setEnabled(isSelectedChangeCause1);
//		
//		txtEndItemCount2A.setEnabled(isSelectedChangeCause2);
//		txtEndItemCount2M.setEnabled(isSelectedChangeCause2);
//	}
//	
//	/**
//	 * 변경사유2 End Item Count 자동계산
//	 */
//	private void setTxtEndItemCount2ACalculated() {
//		boolean isSelectedChangeCause2 = cmbChangeCause2.getSelectedItem() != null && !cmbChangeCause2.getSelectedItem().toString().equals("");
//		
//		// 변경사유2가 입력되어있지 않은 경우에만 계산
//		if (isSelectedChangeCause2) {
//			return;
//		}
//		
//		String sEndItemCount1A = txtEndItemCount1A.getText();
//		
//		if (sEndItemCount1A == null || sEndItemCount1A.equals("") || sEndItemCount1A.length() == 0) {
//			return;
//		}
//		
//		int iEndItemCount1A = Integer.valueOf(sEndItemCount1A.trim());
//		
//		if (iEndItemCount1A > -1) {
//			if (iTotalCount > 0) {
//				if (iEndItemCount1A > iTotalCount) {
//					iEndItemCount1A = iTotalCount;
//					txtEndItemCount1A.setText(String.valueOf(iEndItemCount1A));
//				}
//				
//				int iEndItemCount2A = iTotalCount - iEndItemCount1A;
//				
//				if (iEndItemCount2A != 0) {
//					cmbChangeCause2.setSelectedItem("02");
//					setCountControlsEnabled();
//					txtEndItemCount2A.setText(String.valueOf(iEndItemCount2A));
//				}
//			}
//		}
//	}
//	
//	/**
//	 * LOV Setting
//	 * @param combo
//	 * @param lovName
//	 */
//	private void comboLOVSetting(SWTComboBox combo, String lovName) {
//		try {
//			if (lovName != null) {
//				TCComponentListOfValues lov = SYMCLOVLoader.getLOV(lovName);
//				
//				if (lov == null) {
//					return;
//				}
//				
//				String[] saLOVValues = lov.getListOfValues().getStringListOfValues();
//				String[] saLOVDesces = lov.getListOfValues().getDescriptions();
//				int inx = 0;
//				combo.addItem("", "");
//				for(String sLOVValue : saLOVValues){
//					combo.addItem(sLOVValue+" (" + saLOVDesces[inx] + ")", sLOVValue);
//					inx++;
//				}
//			}
//			combo.setAutoCompleteSuggestive(false);
//		} catch (Exception e) {
//			e.printStackTrace();
//			MessageBox.post(e);
//		}
//	}

	/**
	 * Clear UI
	 */
	public void clear() {
//		cmbChangeCause1.setSelectedItem("");
//		txtEndItemCount1A.setText("");
//		txtEndItemCount1M.setText("");
//		cmbChangeCause2.setSelectedItem("");
//		txtEndItemCount2A.setText("");
//		txtEndItemCount2M.setText("");
		txtRegularProjectCode.setText("");
		txtNewProjectCode.setText("");
		txtAdminCheck.setText("");
		txtNote.setText("");
		txtMemo.setText("");
	}

	/**
	 * Typed Reference 속성 Load
	 */
	public void load() throws Exception {
		TCComponent trECORevision = ecoRevision.getReferenceProperty("s7_ECO_TypedReference");
		hmDataPropertiesOld = new HashMap<String, String>();
		
		if (trECORevision == null) {
			return;
		}
		
		String[] saValues = trECORevision.getProperties(saProperties);

		if (saValues == null || saValues.length == 0) {
			return;
		}

		if (saProperties.length == saValues.length) {
			for (int inx = 0; inx < saProperties.length; inx++) {
				hmDataPropertiesOld.put(saProperties[inx], setNullToString(saValues[inx]).trim());
			}
		}
		
		for (int inx = 0; inx < alControls.size(); inx++) {
			Control control = alControls.get(inx);
			String sProp = control.getData(ECOAdminCheckConstants.PROP).toString();
			
			if (control instanceof SWTComboBox) {
				SWTComboBox cmb = (SWTComboBox) control;
				cmb.setSelectedItem(hmDataPropertiesOld.get(sProp));
			} else if (control instanceof Text) {
				Text text = (Text) control;
				text.setText(hmDataPropertiesOld.get(sProp));
			}
		}
		
//		setCountControlsEnabled();
	}
	
	/**
	 * Null To String
	 * @param sProps
	 * @return
	 */
	private String setNullToString(String sProps) {
		if (sProps == null || sProps.equals("") || sProps.length() == 0) {
			return "";
		} else {
			return sProps;
		}
	}
	
	/**
	 * Controls 반환
	 * @return
	 */
	public ArrayList<Control> getControls() {
		return alControls;
	}
	
	/**
	 * Old Data Properties
	 * @return
	 */
	public HashMap<String, String> getDataPropertiesOld() {
		return hmDataPropertiesOld;
	}
	
	/**
	 * Memo 입력
	 * @param sMemo
	 */
	public void setMemo(String sMemo) {
		txtMemo.setText(sMemo);
	}
	
//	/**
//	 * ECO End Item Total Count for Validation
//	 * @return
//	 */
//	public int getEndItemTotalCount() {
//		return iTotalCount;
//	}
}
