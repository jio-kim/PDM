package com.symc.plm.me.sdv.dialog.meco;

import java.text.SimpleDateFormat;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.view.meco.UpdateEffectivityView;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

@SuppressWarnings("unused")
public class UpdateEffectivityDialog extends SYMCAbstractDialog {
    private Text text;
	private Registry registry;
	private UpdateEffectivityView UpdateEffectivityView;
	private TCComponentChangeItemRevision mecoRevision;
	private SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public UpdateEffectivityDialog(Shell paramShell) {
        super(paramShell);
		this.registry = Registry.getRegistry(this);
		this.createButtonBar(paramShell);
		

    }

	@Override
	protected Composite createDialogPanel(
			ScrolledComposite parentScrolledComposite) {
		UpdateEffectivityView = new UpdateEffectivityView(parentScrolledComposite);
		return UpdateEffectivityView.getComposite();
	}

	@Override
	protected boolean validationCheck() {
		
		boolean isOk =false;
    	Shell shell = AIFUtility.getActiveDesktop().getShell();
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

	@Override
	protected boolean apply() {
		if(validateBeforeApply()) {
			
			try {
				
				if(updateEffectivity()) {
					return true;
				}
				
			}catch(Exception ex) {
				
			}
		}
		return false;
	}
	
	private boolean updateEffectivity() throws TCException {
		boolean isOk = false;
		TCComponent refComp = mecoRevision.getReferenceProperty(SDVPropertyConstant.MECO_TYPED_REFERENCE);
		if (refComp != null)
		{
			String eff_date = "";
					eff_date = DATE_FORMATTER.format(UpdateEffectivityView.txtEffectDate.getDate(CustomUtil.getTCSession()));
			String eff_event= "";
			Object[] selects = UpdateEffectivityView.cbEffectEvent.getSelectedItems();
			if(selects != null){
				for(Object select : selects){
					if(eff_event.equals("")){
						eff_event = select.toString();
					}
				}
			}
			refComp.lock();
			if(eff_event.startsWith(".") ||"".equals(eff_event)){
				refComp.setProperty("m7_EFFECT_DATE", eff_date);	
			}else{
				refComp.setProperty("m7_EFFECT_EVENT", eff_event);
				
			}
			
//			String[] effectivityPropertiesKey = {"m7_EFFECT_DATE", "m7_EFFECT_EVENT"};
//			effectivityProperties = refComp.getProperties(effectivityPropertiesKey);
//			mecoPropertyMap.put("m7_EFFECT_DATE", effectivityProperties[0]);
//			mecoPropertyMap.put("m7_EFFECT_EVENT", effectivityProperties[1]);
			refComp.save();
			refComp.refresh();
			mecoRevision.refresh();
			
			isOk = true;
		}else{
			
			isOk = false;
		}
		
		return isOk;
	}
	
	private boolean validateBeforeApply() {
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
	
	
	protected void okPressed()
	{
		if (!validationCheck())
		{
			return;
		}
		if (apply())
		{
			this.close();
		}
	}

}
