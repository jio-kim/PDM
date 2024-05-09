package com.kgm.commands.ec.history;

import java.util.ArrayList;

import com.kgm.rac.kernel.SYMCBOMEditData;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ECORollbackOperation {

    private SYMCBOMEditData bomEditData;
    
    private boolean successFlag = false;
    
    public ECORollbackOperation(SYMCBOMEditData bomEditData) {
        this.bomEditData = bomEditData;
        executeOneStep();
    }
    
    public boolean getResult() {
        return successFlag;
    }
    
    private void executeOneStep() {
        String changeType = bomEditData.getChangeType();
        // cut -> add
        if(changeType.equals(SYMCBOMEditData.BOM_CUT)) {
            
        }
        // add or replace -> replace or delete
        else if(changeType.startsWith("N") || changeType.startsWith("F")) {
            String oldPartNo = bomEditData.getPartNoOld();
            // replace
            if(oldPartNo != null && !oldPartNo.equals("")) {
               
            }
            // delete
            else {
                
            }
        }
        // change -> change
        else {
            // revise cancel
            if(!bomEditData.getPartRevOld().equals(bomEditData.getPartRevNew())) {
                
                
            }
            // bom instace(property) rollback
            else {
                try {
                    propertyRollback(bomEditData);
                } catch (TCException e) {
                    e.printStackTrace();
                }
            }
        }
        successFlag = true;
    }
    
    private boolean propertyRollback(SYMCBOMEditData bomEditData) throws TCException {
        TCComponentItemRevision parentRev = getParentRevision(bomEditData);
        TCComponentBOMWindow bomWindow = getBOMWindow(parentRev);
        if(bomWindow == null) {
            return false;
        }
        ArrayList<String> occUids = getOccIds(bomEditData.getOccUid());
        TCComponentBOMLine parentLine = bomWindow.getTopBOMLine();
        parentLine.unpack();
        TCComponent[] childLines = parentLine.getReferenceListProperty("bl_child_lines");
        for(int i = 0 ; i < childLines.length ; i++) {
            SYMCBOMLine childLine = (SYMCBOMLine)childLines[i];
            String childOccUid = childLine.getProperty("bl_occurrence_uid");
            if(occUids.contains(childOccUid)) {
                propertyRollback(childLine, bomEditData);
            }
        }
        
        
        bomWindow.close();
        bomWindow.clearCache();

        return true;
    }
    
    private boolean propertyRollback(SYMCBOMLine bomLine, SYMCBOMEditData bomEditData) throws TCException {
        try {
            if(bomEditData.getVcOld() != null && !bomEditData.getVcOld().equals("")) {
                bomLine.setMVLCondition(bomEditData.getVcOld());
            }
//            if(bomEditData.getQtyOld())
//            bomLine.setProperties(new String[]{"bl_sequence_no", "S7_SUPPLY_MODE", "S7_ALTER_PART", "S7_MODULE_CODE"}
//                    , new String[]{bomEditData.getSeqOld(), bomEditData.getSupplyModeOld(), bomEditData.getAltOld(), bomEditData.getModuleCodeOld()});
        } catch(Exception e) {
            
        } finally {
        }
        return false;
    }
    
    private ArrayList<String> getOccIds(String occUids) {
        ArrayList<String> occIds = new ArrayList<String>();
        int occCnt = occUids.length() / 14;
        if(occCnt == 1) {
            occIds.add(occUids);
        } else {
            for(int i = 0 ; i < occCnt ; i++) {
                occIds.add(occUids.substring(0, 14));
                occUids = occUids.substring(14);
            }
        }
        return occIds;
    }
    
    private TCComponentItemRevision getParentRevision(SYMCBOMEditData bomEditData) throws TCException {
        String parentId = bomEditData.getParentNo();
        String parentRevId = bomEditData.getParentRev();
        TCComponentItemRevisionType itemType = (TCComponentItemRevisionType)((TCSession)AIFUtility.getCurrentApplication().getSession()).getTypeComponent("ItemRevision");
//        TCComponentItemRevision parentRev = itemType.findRevision(parentId, parentRevId);
        TCComponentItemRevision parentRev = null;
        TCComponentItemRevision[] revisions = itemType.findRevisions(parentId, parentRevId);
        if(revisions != null && revisions.length > 0) {
        	parentRev = revisions[0];
        }       
        
        return parentRev;
    }
    
    @SuppressWarnings("unused")
    private TCComponentBOMWindow getBOMWindow(TCComponentItemRevision parentRev) throws TCException {
        TCComponentBOMWindow bomWindow = null;
        TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType)session.getTypeComponent("BOMWindow");
        TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
        bomWindow = windowType.create(ruleType.getDefaultRule());
        bomWindow.setWindowTopLine(parentRev);
        
        TCComponentBOMLine parentLine = bomWindow.getTopBOMLine();
        if(!bomWindow.okToModify()) {
            bomWindow.close();
            bomWindow.clearCache();
            return null; 
        }
        
        return bomWindow;
    }
    
}
