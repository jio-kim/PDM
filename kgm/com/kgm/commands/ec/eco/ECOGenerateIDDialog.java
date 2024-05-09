/**
 * ECO ID�� �����ϴ� ȭ�� ��.
 * [SR140807-039][jclee][2014.08.06] ������ ��û. ������ ECO ä��
 */
package com.kgm.commands.ec.eco;

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

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.SYMCLOVCombo;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.PreferenceService;
import com.kgm.common.utils.SWTUtilities;
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
		SWTUtilities.skipKeyEvent(getShell()); //ESC Ű ����

		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout ());
		
		createDialogWindow(composite);
		
		return composite;
	}

	/** ȭ�� ���� */
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
	 * ����
	 */
	@Override
	protected boolean validationCheck() {
		 message = new StringBuffer();
		/** Vehicle No., Major Component, Year No �ʼ� ���� **/
		if(vehicleECO.getSelection()){
			if(vehicleNo.getText().equals(""))
				message.append("Vehicle No. must be seleted.\n");
			if(majorComp.getText().equals(""))
				message.append("Major Component must be seleted.\n");
			if(year.getText().equals(""))
				message.append("Year No must be seleted.\n");
//			if(!vehicleNo.getText().equals("") & (vehicleNo.getTextDesc() == null || vehicleNo.getTextDesc().length() != 4))
			/**
			 * [SR140807-039][jclee][2014.08.06] ������ ��û. ������ ECO ä��
			 * ���� : VehicleNo LOV�� Description���� Project code�� ������ ����.
			 * TO-DO : Vehicle No�� �̿��Ͽ� ������ Project Item�� �ش� Vehicle No�� �Ӽ����� ������ is Vehicle No �� 'Y'�� �׸��� Project Code ����.
			 */
			if(!vehicleNo.getText().equals("") & (vehicleNo.getTextDesc() == null))
				message.append("Represent the project code does not conform to the format.\nPlease contact the administrator.");
			
			// [SR140807-039] jclee, 20140807, ProjectCode Validation.
			//  : �ݵ�� 1���� Project Code�� ����Ǿ�� ��.
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
	 * ID ����
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
				initialValueKey = PreferenceService.getValue("SYMC_ECO_Init_NO_"+vehicleNo.getText());//FIXME : Preference�� ��� �Ͽ��� ��. ���ڸ�������.

				/**
				 * [SR140807-039][jclee][2014.08.06]
				 * ���� : VehicleNo LOV�� Description���� Project code�� ������ ����.
				 * TO-BE : Vehicle No�� �̿��Ͽ� ������ Project Item�� �ش� Vehicle No�� �Ӽ����� ������ is Vehicle No �� 'Y'�� �׸��� Project Code ����.
				 */
//				repProject = vehicleNo.getTextDesc();
				// 1. Vehicle No ����
				String sVehicleNo = vehicleNo.getText();
				repProject = getProjectCodes(sVehicleNo).get(0);
			}else{
				ecoPrefix = prodNo.getText()+year.getText();
				initialValueKey = PreferenceService.getValue("SYMC_ECO_Init_NO_"+prodNo.getText());
				// 20131112 repProject ������ TextDesc �� �ٽ� ����. getText �� ������ ������ �� �� ����. 
				//  getText�� �ϸ� EPL generation���� Project item�� ã�� ���� F1�� F2�� ������ ������ �߻� ��. 
				//  �� �κ� getTextDesc �� ���� �� ���� ����͸� �ʿ�. (by �۴뿵C)
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
			
			// FIXED : 2013.04.30, BY DJKIM, ���� ECO���� ���� ECO���� ���� �߰�[V/E]
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
	 * [SR140807-039][jclee][2014.08.06] Vehicle No�� �´� Project Code ��ȯ
	 * Project �˻� Quiry Builder�� �̿��Ͽ� �ش� Vehicle No�� ������ is Vehicle No �Ӽ��� 'Y'�� Project �˻�.
	 * @param sVehicleNo
	 * @return
	 */
	private ArrayList<String> getProjectCodes(String sVehicleNo) {
		ArrayList<String> alProjectNos = new ArrayList<String>();	// Project Code ���� ��� ArrayList
		try {
			// 1. saved query �� �̿��Ͽ� is vehicle project�� "Y" �� Project �˻�
			TCComponent[] comps = CustomUtil.queryComponent("SYMC_Search_Project_Revision", new String[] { "IS VEHICLE PROJECT" }, new String[] { "Y" });
			
			// 2. Project ��� �� Vehicle No�� ��ġ�ϴ� �׸� ����.
			for (int inx = 0; inx < comps.length; inx++) {
				// 2.1. Type�� Project Revision�� �͸� �˻�
				if (comps[inx].getTypeComponent().toString().equals("S7_PROJECTRevision")) {
					TCComponentItemRevision comp = (TCComponentItemRevision) comps[inx];
					String sTargetVehicleNo = comp.getProperty("s7_VEHICLE_NO").trim();
					String sProjectNo = comp.getProperty("item_id").trim();
					
					// 2.1.1. Vehicle No�� ��ġ�ϰ� Project Code�� null�� �ƴ� Project�� ���� ����.
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
