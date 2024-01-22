package com.symc.plm.me.sdv.view.meco;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.SYMCDateTimeButton;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.viewer.AbstractSDVViewer;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.controls.SWTComboBox;

@SuppressWarnings("unused")
public class UpdateEffectivityView extends AbstractSDVViewer {

	public SYMCDateTimeButton txtEffectDate;
	public SWTComboBox cbEffectEvent;
	private Composite composite;
    private Button btnNewButton;
	private Label lbEffectDate, lbEffectEvent;
	private TCComponentChangeItemRevision mecoRevision;
	
	public UpdateEffectivityView(Composite parent) {
		super(parent);
	}

	@Override
	public void load() {

	}

	@Override
	public void save() {
		System.out.println("UpdateEffectivityView.save()");
//		if(validateBeforeAppl()) {
//			
//			try {
//				
//				if(updateEffectivity()) {
//					
//				}
//				
//			}catch(Exception ex) {
//				
//			}
//		}
	}

	@Override
	public boolean isSavable() {
		return false;
	}

	@Override
	public void createPanel(Composite parent) {
		initUI();
	}

	private void initUI(){
		
        setLayout(new FillLayout(SWT.HORIZONTAL));

        composite = new Composite(this, SWT.NONE);
        composite.setLayout(new GridLayout(6, false));

        Label lblNewLabel = new Label(composite, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblNewLabel.setText("Eff. Date");

        txtEffectDate = new SYMCDateTimeButton(composite, SWT.BORDER | SWT.DROP_DOWN);
        txtEffectDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

//        btnNewButton = new Button(composite, SWT.NONE);
//        btnNewButton.setText("...");
//        new Label(composite, SWT.NONE);
        
        lbEffectDate = new Label(composite, SWT.NONE);
        lbEffectDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lbEffectDate.setText("Eff. Event");

        cbEffectEvent = new SWTComboBox(composite, SWT.BORDER);
		ArrayList<HashMap<String,String>> arrEffectEvent = null; 
		try{
			
			arrEffectEvent = CustomUtil.getRevisionEffectivityReferencedByMeco();
			cbEffectEvent.addItem(".","(.)");
			for(HashMap<String,String> hashEffectEvent : arrEffectEvent) {
				cbEffectEvent.addItem(hashEffectEvent.get(SDVPropertyConstant.EFFECTIVITY_ID)+" ("+hashEffectEvent.get(SDVPropertyConstant.EFFECTIVITY_DATES)+")",hashEffectEvent.get(SDVPropertyConstant.EFFECTIVITY_ID ));
				//combobox.addItem(lovValue+" (" + lovDesces[i] + ")", lovValue);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
        cbEffectEvent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

	}
	
//	private boolean updateEffectivity() throws TCException {
//		
//		TCComponent refComp = mecoRevision.getReferenceProperty(SDVPropertyConstant.MECO_TYPED_REFERENCE);
//		if (refComp != null)
//		{
//			String eff_date = (String)txtEffectDate.getData();
//			String eff_event= (String)cbEffectEvent.getTextField().getText();
//			
//			refComp.lock();
//			refComp.setProperty("m7_EFFECT_DATE", eff_date);
//			refComp.setProperty("m7_EFFECT_EVENT", eff_event);
////			String[] effectivityPropertiesKey = {"m7_EFFECT_DATE", "m7_EFFECT_EVENT"};
////			effectivityProperties = refComp.getProperties(effectivityPropertiesKey);
////			mecoPropertyMap.put("m7_EFFECT_DATE", effectivityProperties[0]);
////			mecoPropertyMap.put("m7_EFFECT_EVENT", effectivityProperties[1]);
//			refComp.save();
//			refComp.unlock();
//			refComp.refresh();
//			mecoRevision.refresh();
//			
//			return true;
//		}else{
//			
//			return false;
//		}
//		
//	}
	
	private boolean validateBeforeAppl() {
		boolean isOk = false;
		
    	Shell shell = AIFUtility.getActiveDesktop().getShell();
//		UpdateEffectivityDialog updateEffectivityDialog = new UpdateEffectivityDialog(shell);
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
		
		if (comps.length == 1){
			
			TCComponent comp = (TCComponent) comps[0];
			if (!(comp instanceof TCComponentChangeItemRevision)) {
				
				MessageBox.post(shell, "Should you select meco's revision when try to update effectivity on MECO!", "Warning", MessageBox.WARNING);
				
			}else{
				mecoRevision = (TCComponentChangeItemRevision)comp;
				isOk = true;
			}
		}else{
			MessageBox.post(shell, "Should you select only single meco's revision when try to update effectivity on MECO!", "Warning", MessageBox.WARNING);
		}
 
		return isOk;
	}
	
	public Composite getComposite() {
		return this;
	}
}
