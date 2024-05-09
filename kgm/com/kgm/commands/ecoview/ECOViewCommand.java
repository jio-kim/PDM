package com.kgm.commands.ecoview;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.Activator;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class ECOViewCommand extends AbstractAIFCommand {

	public ECOViewCommand() {
	}

	protected void executeCommand() throws Exception {
	    AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
	    InterfaceAIFComponent target = application.getTargetComponent();
	    
	    if(target instanceof TCComponentBOMLine) {
	        TCComponentBOMLine targetBOMLine = (TCComponentBOMLine)target;
	        String targetType = targetBOMLine.getItem().getType();
	        if(!targetType.equals("S7_Vehpart") && !targetType.equals("S7_FunctionMast")) {
	            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Function Master 또는 Vehicle Part 를 선택해 주십시요!", "ECO View", MessageBox.INFORMATION);
                return;
	        }
	        
	        TCComponentItemRevision targetRev = ((TCComponentBOMLine)target).getItemRevision();
	        TCComponentItemRevision targetECORev = (TCComponentItemRevision)targetRev.getReferenceProperty("s7_ECO_NO");
	        if(targetECORev != null) {
	            Activator.getDefault().openPerspective("com.teamcenter.rac.ui.perspectives.navigatorPerspective");
	            Activator.getDefault().openComponents("com.teamcenter.rac.ui.perspectives.navigatorPerspective", new InterfaceAIFComponent[]{targetECORev});
	        } else {
	            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Selected Part has not ECO.", "ECO View", MessageBox.INFORMATION);
                return;
	        }
	    }
	    
	}
	
}