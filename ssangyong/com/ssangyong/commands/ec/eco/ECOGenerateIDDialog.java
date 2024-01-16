/**
 * ECO ID를 생성하는 화면 임.
 * [SR140807-039][jclee][2014.08.06] 박태훈 요청. 전차종 ECO 채변
 */
package com.ssangyong.commands.ec.eco;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.common.SYMCLOVCombo;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.PreferenceService;
import com.ssangyong.common.utils.SWTUtilities;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ECOGenerateIDDialog extends SYMCAbstractDialog {
	
	private Registry registry;
	private Button vehicleECO, licensedECO;
	private SYMCLOVCombo vehicleNo, majorComp, prodNo, year;
	private Label vehicleNo_label, majorComp_label, prodNo_label, year_label;
	private ECOSWTRendering ecoRendering;
	private TCSession session;
	private StringBuffer message = new StringBuffer();
	private GridData gridFillData = new GridData (SWT.FILL, SWT.CENTER, true, false);
	private CustomECODao dao;
	
	public ECOGenerateIDDialog(Shell parent, ECOSWTRendering ecoRendering) {
		super(parent);
		this.registry = Registry.getRegistry(this);
		this.ecoRendering = ecoRendering;
		this.session = CustomUtil.getTCSession();
		this.setApplyButtonVisible(false);
	}
	
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		this.setDialogTextAndImage(registry.getString("ECOIDGeneration.TITLE"), null);
		SWTUtilities.skipKeyEvent(getShell()); //ESC 키 막음

		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout ());
		
		createDialogWindow(composite);
		
		return composite;
	}

	/** 화면 생성 */
	protected void createDialogWindow(Composite paramComposite) {
		
		getShell().setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		getShell().setBackgroundMode(SWT.INHERIT_FORCE);
		
		GridLayout gridLayout = new GridLayout (4, false);
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		paramComposite.setLayout(gridLayout);
		
		paramComposite.setLayoutData (gridFillData);
		
		Group group = new Group (paramComposite, SWT.NONE);
		gridLayout = new GridLayout (2, false);
		group.setLayout (gridLayout);
		group.setText (registry.getString("ECOIDGeneration.GROUP.Type"));
		
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 4;
		group.setLayoutData(gridData);
		
		vehicleECO = new Button (group, SWT.RADIO);
		vehicleECO.setText(registry.getString("ECOIDGeneration.LABEL.VehicleECO"));
		vehicleECO.setLayoutData(new GridData (120, SWT.DEFAULT));
		vehicleECO.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				vehicleNo.setVisible(true);
				majorComp.setVisible(true);
				vehicleNo_label.setVisible(true);
				majorComp_label.setVisible(true);
				prodNo_label.setVisible(false);
				prodNo.setVisible(false);
			}
		});
		
		licensedECO = new Button (group, SWT.RADIO);
		licensedECO.setText(registry.getString("ECOIDGeneration.LABEL.LicensedECO"));
		licensedECO.setLayoutData(new GridData (120, SWT.DEFAULT));
		licensedECO.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				vehicleNo.setVisible(false);
				majorComp.setVisible(false);
				vehicleNo_label.setVisible(false);
				majorComp_label.setVisible(false);
				prodNo_label.setVisible(true);
				prodNo.setVisible(true);
			}
		});
		
		vehicleNo_label = new Label (paramComposite, SWT.RIGHT);
		vehicleNo_label.setText (registry.getString("ECOIDGeneration.LABEL.VehicleNo"));
		vehicleNo_label.setLayoutData(new GridData (120, SWT.DEFAULT));

        vehicleNo = new SYMCLOVCombo(paramComposite, "S7_VEHICLE_NO");
        vehicleNo.setLayoutData(new GridData (120, SWT.DEFAULT));
		vehicleNo.setFocus();
		
		majorComp_label = new Label (paramComposite, SWT.RIGHT);
		majorComp_label.setText (registry.getString("ECOIDGeneration.LABEL.MajorComp"));
		majorComp_label.setLayoutData(new GridData (120, SWT.DEFAULT));

        majorComp = new SYMCLOVCombo(paramComposite, "S7_MAJOR_COMPONENT");
        majorComp.setLayoutData(new GridData (120, SWT.DEFAULT));
		
		prodNo_label = new Label (paramComposite, SWT.RIGHT);
		prodNo_label.setText (registry.getString("ECOIDGeneration.LABEL.ProdNo"));
		prodNo_label.setLayoutData(new GridData (120, SWT.DEFAULT));

        prodNo = new SYMCLOVCombo(paramComposite, "S7_PRODUCT_CODE");
        prodNo.setLayoutData(new GridData (120, SWT.DEFAULT));
        
        year_label = new Label (paramComposite, SWT.RIGHT);
		year_label.setText (registry.getString("ECOIDGeneration.LABEL.Year"));
		year_label.setLayoutData(new GridData (120, SWT.DEFAULT));

        year = new SYMCLOVCombo(paramComposite, "S7_YEAR_NO");
        year.setLayoutData(new GridData (120, SWT.DEFAULT));
		SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
		String theyear = simpleDateformat.format((new Date()).getTime());
		String[] yearList = year.getItems();
		int i = 0;
		for(String y : yearList){
			if(y.indexOf(theyear) > -1){
				year.select(i);
				break;
			}
			i++;	
		}
		if(year.getText().length() > 0)
			year.setEnabled(false);
		else
			MessageBox.post(getShell(), "Check the Year LOV!\n Please call PLM Admin.", "ERROR", MessageBox.ERROR);
        
        vehicleECO.setSelection(true);
		prodNo_label.setVisible(false);
		prodNo.setVisible(false);
		
		getShell().setText(registry.getString("ECOIDGeneration.TITLE"));
	}

	/**
	 * 검증
	 */
	@Override
	protected boolean validationCheck() {
		 message = new StringBuffer();
		/** Vehicle No., Major Component, Year No 필수 여부 **/
		if(vehicleECO.getSelection()){
			if(vehicleNo.getText().equals(""))
				message.append("Vehicle No. must be seleted.\n");
			if(majorComp.getText().equals(""))
				message.append("Major Component must be seleted.\n");
			if(year.getText().equals(""))
				message.append("Year No must be seleted.\n");
//			if(!vehicleNo.getText().equals("") & (vehicleNo.getTextDesc() == null || vehicleNo.getTextDesc().length() != 4))
			/**
			 * [SR140807-039][jclee][2014.08.06] 박태훈 요청. 전차종 ECO 채변
			 * 기존 : VehicleNo LOV의 Description에서 Project code를 가져와 수행.
			 * TO-DO : Vehicle No를 이용하여 생성된 Project Item중 해당 Vehicle No를 속성으로 가지며 is Vehicle No 가 'Y'인 항목의 Project Code 추출.
			 */
			if(!vehicleNo.getText().equals("") & (vehicleNo.getTextDesc() == null))
				message.append("Represent the project code does not conform to the format.\nPlease contact the administrator.");
			
			// [SR140807-039] jclee, 20140807, ProjectCode Validation.
			//  : 반드시 1건의 Project Code가 추출되어야 함.
			ArrayList<String> alProjectCodes = getProjectCodes(vehicleNo.getText().trim());
			if (alProjectCodes.size() != 1)
				message.append("Represent the project code is not One.(2 more or zero)\nPlease contact the administrator.");
			
		}else{
			if(prodNo.getText().equals(""))
				message.append("Product No. must be seleted.\n");
			if(year.getText().equals(""))
				message.append("Year No must be seleted.\n");
			if(prodNo.getTextDesc().length() > 6)
				message.append("Represent the project code does not conform to the format.\nPlease contact the administrator.");
		}
		if(message.toString().length() > 0){
			MessageBox.post(getShell(), message.toString(), "ERROR", MessageBox.ERROR);
			return false;
		}
			
		return true;
	}

	/**
	 * ID 생성
	 */
	@Override
	protected boolean apply() {

		dao = new CustomECODao();
		try {

			String ecoPrefix = null;
			PreferenceService.createService(session);
			String initialValueKey = "";
			String repProject = null;
			if(vehicleECO.getSelection()){
				ecoPrefix = vehicleNo.getText()+majorComp.getText()+year.getText();
				initialValueKey = PreferenceService.getValue("SYMC_ECO_Init_NO_"+vehicleNo.getText());//FIXME : Preference에 등록 하여야 함. 세자리여야함.

				/**
				 * [SR140807-039][jclee][2014.08.06]
				 * 기존 : VehicleNo LOV의 Description에서 Project code를 가져와 수행.
				 * TO-BE : Vehicle No를 이용하여 생성된 Project Item중 해당 Vehicle No를 속성으로 가지며 is Vehicle No 가 'Y'인 항목의 Project Code 추출.
				 */
//				repProject = vehicleNo.getTextDesc();
				// 1. Vehicle No 추출
				String sVehicleNo = vehicleNo.getText();
				repProject = getProjectCodes(sVehicleNo).get(0);
			}else{
				ecoPrefix = prodNo.getText()+year.getText();
				initialValueKey = PreferenceService.getValue("SYMC_ECO_Init_NO_"+prodNo.getText());
				// 20131112 repProject 정보를 TextDesc 로 다시 변경. getText 로 변경한 사유를 알 수 없음. 
				//  getText로 하면 EPL generation에서 Project item을 찾지 못해 F1이 F2로 나오는 문제가 발생 함. 
				//  이 부분 getTextDesc 로 변경 해 놓고 모니터링 필요. (by 송대영C)
//				repProject = prodNo.getText();//prodNo.getTextDesc(); // FIXME
				repProject = prodNo.getTextDesc(); 
			}

			if(initialValueKey.length() == 3){

				TCComponentItem item = CustomUtil.findItem(SYMCECConstant.ECOTYPE, ecoPrefix+initialValueKey);
				if(item == null){
					ecoPrefix = ecoPrefix+initialValueKey;
				}else{
					ecoPrefix = dao.getNextECOSerial(ecoPrefix);
				}

			}else{
				ecoPrefix = dao.getNextECOSerial(ecoPrefix);
			}

			if(ecoPrefix != null)
				ecoRendering.setItem_id(ecoPrefix);

			if(repProject != null)
				ecoRendering.setRepProject(repProject);
			
			// FIXED : 2013.04.30, BY DJKIM, 차량 ECO인지 엔진 ECO인지 구분 추가[V/E]
			if(vehicleECO.getSelection()){
				ecoRendering.setEco_kind("V");
			}else{
				ecoRendering.setEco_kind("E");
			}

		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
			return false;
		}
		return true;
	}
	
	/**
	 * [SR140807-039][jclee][2014.08.06] Vehicle No에 맞는 Project Code 반환
	 * Project 검색 Quiry Builder를 이용하여 해당 Vehicle No를 가지며 is Vehicle No 속성이 'Y'인 Project 검색.
	 * @param sVehicleNo
	 * @return
	 */
	private ArrayList<String> getProjectCodes(String sVehicleNo) {
		ArrayList<String> alProjectNos = new ArrayList<String>();	// Project Code 추출 결과 ArrayList
		try {
			// 1. saved query 를 이용하여 is vehicle project가 "Y" 인 Project 검색
			TCComponent[] comps = CustomUtil.queryComponent("SYMC_Search_Project_Revision", new String[] { "IS VEHICLE PROJECT" }, new String[] { "Y" });
			
			// 2. Project 목록 중 Vehicle No가 일치하는 항목 추출.
			for (int inx = 0; inx < comps.length; inx++) {
				// 2.1. Type이 Project Revision인 것만 검색
				if (comps[inx].getTypeComponent().toString().equals("S7_PROJECTRevision")) {
					TCComponentItemRevision comp = (TCComponentItemRevision) comps[inx];
					String sTargetVehicleNo = comp.getProperty("s7_VEHICLE_NO").trim();
					String sProjectNo = comp.getProperty("item_id").trim();
					
					// 2.1.1. Vehicle No가 일치하고 Project Code가 null이 아닌 Project에 대해 추출.
					if (sVehicleNo.equals(sTargetVehicleNo) && !sProjectNo.isEmpty()) {
						alProjectNos.add(sProjectNo);
					}
				}
			}
			
			return alProjectNos;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(getShell(), e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
	}
}
