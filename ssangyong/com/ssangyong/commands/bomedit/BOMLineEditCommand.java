package com.ssangyong.commands.bomedit;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.commands.ec.history.BOMECOSelectDialog;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class BOMLineEditCommand extends AbstractAIFCommand {

	public BOMLineEditCommand() {
	}

	protected void executeCommand() throws Exception {
		InterfaceAIFComponent[] targetComps = AIFUtility.getCurrentApplication().getTargetComponents();
		if(targetComps == null || targetComps.length == 0) {
			return;
		}
		
		// Single BOMLine Edit start
		if(targetComps.length == 1 && targetComps[0] instanceof TCComponentBOMLine) {
		    TCComponentBOMLine bomLine = (TCComponentBOMLine)targetComps[0];
		    if(bomLine.parent() == null) {
		        MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Top line is not editable", "Edit BOMLine", MessageBox.INFORMATION);
		        return;
		    }
		    String parentType = bomLine.parent().getProperty("bl_item_object_type");
		    if(bomLine.parent().getItemRevision().getProperty("s7_ECO_NO").equals("") && 
		       (parentType.equals("Function Master") || parentType.equals("Vehicle Part")) &&
		       bomLine.parent().getBOMViewRevision().okToModify()) {
                if(bomLine.parent().getItemRevision().okToModify()) {
                    TCComponentItemRevision ecoRev = selectECO();
                    if(ecoRev == null) {
                        return;
                    }
                    bomLine.parent().getItemRevision().setReferenceProperty("s7_ECO_NO", ecoRev);
                }
                if(bomLine.getProperty("s7_ECO_NO").equals("") && 
                   bomLine.getItemRevision().getProperty("s7_STAGE").equals("P") && 
                   bomLine.getItemRevision().okToModify()) {
                    bomLine.getItemRevision().setReferenceProperty("s7_ECO_NO", bomLine.parent().getItemRevision().getReferenceProperty("s7_ECO_NO"));
                }
		    }
		    AIFDesktop.getActiveDesktop().getShell().setCursor(SWTResourceManager.getCursor(SWT.CURSOR_WAIT));
            SingleBOMLineEditDialog dialog = new SingleBOMLineEditDialog(AIFDesktop.getActiveDesktop().getShell(), bomLine);
            AIFDesktop.getActiveDesktop().getShell().setCursor(SWTResourceManager.getCursor(SWT.CURSOR_ARROW));
            dialog.open();
            return;
		}
		// single end
		
		// Multi BOMLine Edit start
        if(targetComps[0] instanceof TCComponentBOMLine &&
            !((TCComponentBOMLine)targetComps[0]).window().getTopBOMLine().getProperty("bl_item_object_type").equals("Function")) {
            MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                    , "In order to edit BOM Structure, You should place Function on Top."
                    , "BOM Edit", MessageBox.INFORMATION);
            return;
        }
        // 편집 가능한 
		ArrayList<TCComponentBOMLine> targetLine = new ArrayList<TCComponentBOMLine>();
		boolean requireECO = false;
		for(int i = 0 ; i < targetComps.length ; i++) {
			if(!(targetComps[i] instanceof TCComponentBOMLine)) {
				return;
			}
			SYMCBOMLine bomLine = (SYMCBOMLine)targetComps[i];
			if(bomLine.window().getTopBOMLine().equals(bomLine)) {
			    MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Top Line is not editable.", "Edit BOMLine", MessageBox.INFORMATION);
			    continue;
			} else if(bomLine.isHistoryTarget(bomLine.parent())) {
			    if(!bomLine.isTopFunction()) {
			        return;
			    }
			}
            if(!isModifiableLine(bomLine) || !bomLine.parent().getBOMViewRevision().okToModify()) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), bomLine.toString() + " Line is not editable.", "Edit BOMLine", MessageBox.INFORMATION);
                continue;
            } else {
                if(bomLine.parent().getItemRevision().getProperty("s7_ECO_NO").equals("")) {
                    requireECO = true;
                }
                targetLine.add(bomLine);
            }
		}
		
		if(targetLine.size() == 0) {
		    return;
		} else if(targetLine.size() == 1) {
			AIFDesktop.getActiveDesktop().getShell().setCursor(SWTResourceManager.getCursor(SWT.CURSOR_WAIT));
			SingleBOMLineEditDialog dialog = new SingleBOMLineEditDialog(AIFDesktop.getActiveDesktop().getShell(), targetLine.get(0));
			AIFDesktop.getActiveDesktop().getShell().setCursor(SWTResourceManager.getCursor(SWT.CURSOR_ARROW));
			dialog.open();
		} else {
		    if(requireECO) {
		        TCComponentItemRevision ecoRev = selectECO();
	            if(ecoRev == null) {
	                return;
	            }
	            for(TCComponentBOMLine bomLine : targetLine) {
	                if(bomLine.parent().getItemRevision().getProperty("s7_ECO_NO").equals("")) {
	                    bomLine.parent().getItemRevision().setReferenceProperty("s7_ECO_NO", ecoRev);
	                }
	            }
		    }
            for(TCComponentBOMLine bomLine : targetLine) {
                if(bomLine.getProperty("s7_ECO_NO").equals("") && bomLine.getItemRevision().okToModify()) {
                    bomLine.getItemRevision().setReferenceProperty("s7_ECO_NO", bomLine.parent().getItemRevision().getReferenceProperty("s7_ECO_NO"));
                }
            }
			AIFDesktop.getActiveDesktop().getShell().setCursor(SWTResourceManager.getCursor(SWT.CURSOR_WAIT));
			MultiBOMLineEditDialog dialog = new MultiBOMLineEditDialog(AIFDesktop.getActiveDesktop().getShell(), (TCComponentBOMLine[])targetLine.toArray(new TCComponentBOMLine[targetLine.size()]));
			AIFDesktop.getActiveDesktop().getShell().setCursor(SWTResourceManager.getCursor(SWT.CURSOR_ARROW));
			dialog.open();
		}
	}
	
	private TCComponentItemRevision selectECO() {
	    BOMECOSelectDialog ecoSelect = new BOMECOSelectDialog(AIFUtility.getActiveDesktop().getShell());
        ecoSelect.getShell().setText("ECO Select");
        return ecoSelect.getECO();
	}
	
	private boolean isModifiableLine(TCComponentBOMLine bomLine) {
	    try {
            String itemType = bomLine.getProperty("bl_item_object_type");
            if(!itemType.equals("Vehicle Part") && !itemType.equals("Standard Part")) {
                return false;
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
	    return true;
	}
	
}