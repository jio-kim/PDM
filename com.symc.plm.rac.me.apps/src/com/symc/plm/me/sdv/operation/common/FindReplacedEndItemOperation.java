package com.symc.plm.me.sdv.operation.common;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.tree.ExpandVetoException;

import com.kgm.dto.EndItemData;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomBOPDao;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEProcess;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.common.BOMTreeTableModel;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.treetable.TreeTableNode;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR150122-027][20150210] shcho, Find automatically replaced end item (���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ�) (10�������� ���� �� �ҽ� 9���� �̽���)
 * [SR150122-027][20150309] shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
 * [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item �޴� ��� ���� (UnPack ��� �߰�)
 * [SR150421-019][20150518] shcho, Pack �Ǿ��ִ� E/Item�� ��� Unpack�� �Ͽ� ��� ���������� ǥ�� �ǵ��� ����
 */
public class FindReplacedEndItemOperation extends AbstractTCSDVOperation {
    
    private int displayMode = 0;
    private TCComponent mProductRevision;
    private TCComponentBOPLine target;
    private TCComponentBOPLine latestOperation = null;
    private Registry registry;
    private Color highlightColor = Color.RED;
    
    public FindReplacedEndItemOperation() {
        registry = Registry.getRegistry("com.symc.plm.me.sdv.operation.common.common");
    }

    @Override
    public void startOperation(String commandId) {
        displayMode = ConfirmationDialog.post(registry.getString("Confirm.NAME"), registry.getString("ShowInBOMView.MSG"));
    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        BOMLineNode.setPropertyLoading(false);
        
        latestOperation = null;

        InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
        target = (TCComponentBOPLine) selectedTargets[0];

        try {
            //  BOP Top�� ����� MProduct�� ã�´�.
            TCComponentBOPLine topBopLine = (TCComponentBOPLine) target.window().getTopBOMLine();
            // [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
            mProductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(topBopLine.getItemRevision());
            
            if(mProductRevision == null) {
                MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("NotExistMProduct.MSG"), 
                        registry.getString("Inform.NAME"), MessageBox.INFORMATION); 
                return;
            }
                    
            // �ڵ� Replace �� �������� DB �����Ͽ� �����´�.
            CustomBOPDao dao = new CustomBOPDao();  
            ArrayList<EndItemData> endItemList = dao.findReplacedEndItems(target.getProperty(SDVPropertyConstant.BL_ITEM_PUID),
                    mProductRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
            
            // �ڵ� Replace �� �������� ������ �޼��� �ڽ��� ����Ѵ�.
            if(endItemList == null || endItemList.size() == 0) {
                MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("NotExistReplacedEndItem.MSG"), 
                        registry.getString("Inform.NAME"), MessageBox.INFORMATION); 
                return;
            }
            
            // SR161209-032 �����ؾ� �� �κ�  ����
            // ������ : �躴�� ����
            // ���� ���� : 2017.03.30
            // �ڵ� Replace �� �����۸���Ʈ�� �޾� �װ��� ECO Released Date �� ���� ��������(Parent ItemRevsion) �۾�ǥ�ؼ� Published Date�� ã�ƿ��� ���� �߰�
            
            // ������ ���ؼ� ���� EndItemList �� �Ʒ��� ������ ���� ���͸� �� �� �ٽ� ���� ���� ����
            ArrayList<EndItemData> newEndItemList  = new ArrayList<EndItemData>();
            
            for( int i = 0; i < endItemList.size(); i ++ ) {
            	 // �� ����� �Ǵ� ECO ReleaseDate�� ���� ���� ����
            	Date compareMEcoReleasedDate = null;
            	// �� ����� �Ǵ� ���� �������� Published Date�� ���� ���� ����
            	Date compareOpRevPublishedDate = null;
            	 
            	 EndItemData endItemData = endItemList.get(i);
            	 
            	 String operationItemId = endItemData.getPitem_id();
            	 
            	 // target�� view Window ���� �Ķ���ͷ� �Է��� ID�� BOMLine�� �˻�
            	 TCComponentBOMLine[] searchOpBomLines = target.window().findConfigedBOMLinesForAbsOccID(operationItemId, true, target.window().getTopBOMLine());
            	 TCComponentBOMLine searchOpBomLine = null;
            	 TCComponentItemRevision searchOpItemRevision = null;
            	 String operationItemRevId = "";
            	 if( searchOpBomLines.length != 0 && searchOpBomLines != null ) {
            		 searchOpBomLine = searchOpBomLines[0];
            		 //BOM Line ���� ���� ItemReivsion ����
            		 searchOpItemRevision = searchOpBomLine.getItemRevision();
            		 // Item Revision���� ���� Revision ID ����
            		 operationItemRevId = searchOpItemRevision.getProperty( SDVPropertyConstant.ITEM_REVISION_ID );
            	 } else {
            		 continue;
            	 }
            	 
            	 // EndItem ����Ʈ�� OPeration Item ID�� �۾�ǥ�ؼ��� �˻�
            	 TCComponentItemRevision searchWorkStandardDoc = CustomUtil.findItemRevision( "M7_ProcessSheetRevision", "KPS-" + operationItemId, operationItemRevId + "A" );
            	 
            	 if( searchOpItemRevision != null ) {
            		 // �˻��� Revision�� ECO�� ã�� ���� RelatedComponents�� ã��
            		 TCComponent relatedComps[] = searchOpItemRevision.getRelatedComponents();
            		 if( relatedComps != null && relatedComps.length != 0 ) {
            			 for( int j = 0 ; j < relatedComps.length ; j ++ ) {
            				 TCComponent relatedComp = relatedComps[j]; 
            				 // Related Components �߿���  Type �� MECO Ÿ�� ����
            				 if( relatedComp.getType().equals( SDVTypeConstant.MECO_ITEM_REV ) ) {
            					 TCComponentItemRevision mEcoRevision = (TCComponentItemRevision) relatedComp;
            					 // ����� MECO Ÿ�Կ��� date_release �Ӽ��� ����
            					 compareMEcoReleasedDate = mEcoRevision.getDateProperty("date_released");
            					 break;
            				 }
            			 }
            		 }
            	 }
            	 
            	 if( searchWorkStandardDoc != null ) {
            		 // �˻��� �۾�ǥ�ؼ����� Published Date �Ӽ� ���� ����
            		 TCComponentItemRevision workStandardDoc = (TCComponentItemRevision) searchWorkStandardDoc;
            		 compareOpRevPublishedDate = workStandardDoc.getDateProperty( SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
            	 }
            	 
            	 if( compareMEcoReleasedDate != null && !compareMEcoReleasedDate.equals( "" ) && compareOpRevPublishedDate != null && !compareOpRevPublishedDate.equals( "" ) ) {
            		 
            		 // ����� �Ӽ����� �� ��¥�� �Լ� Date Ŭ������ compareTo �Լ� ���
            		 int compare = compareMEcoReleasedDate.compareTo(compareOpRevPublishedDate);
            		 
            		 // int compare = A.compareTo(B) �Ұ�� 
            		 // compare ���� 0���� ũ�� A �� ��¥�� �� ���� ���̰�
            		 // compare ���� -1 �̸� B�� ��¥�� �� ���� ��
            		 // 0�� ��� A��B�� ���� ��¥
            		 if( compare > 0 ) {
            			 newEndItemList.add(endItemData);
            		 }
            	 }
            } // SR161209-032 �����ؾ� �� �κ�  ��
            
            BOMTreeTable[] tables = getTables();
            
            // ���� 
            // ������ : �躴�� ����
            // ���� ���� : setBOPView �޼��忡 ���� �Ķ���� ���� endItemList -> newEndItemList �� ����
            // ���� ���� : SR161209-032 �������� ���� ���� �� ����
            List<TCComponentBOPLine> endItemLines = setBOPView(tables[0], newEndItemList);
            
            
            
            if(endItemLines.size() == 0) {
                MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("NotExistReplacedEndItem.MSG"), 
                        registry.getString("Inform.NAME"), MessageBox.INFORMATION);
                return;
            }
            
            if(displayMode == 2 && tables[1] != null) {
                setBOMView(tables[1], endItemLines);
            }
            
            for(BOMTreeTable table : tables) {
                if(table != null) {
                    table.refreshLineBgColors();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("Error.MSG") + "\n" + e.getMessage(), 
                    registry.getString("Error.NAME"), MessageBox.ERROR);        
        } finally {
            BOMLineNode.setPropertyLoading(true);
            target.window().fireComponentChangeEvent();
        }
    }
    
    private List<TCComponentBOPLine> setBOPView(BOMTreeTable table, ArrayList<EndItemData> endItemList) throws TCException, ExpandVetoException {
        List<TCComponentBOPLine> bopLines = new ArrayList<TCComponentBOPLine>();
        
        BOMTreeTableModel tableModel = (BOMTreeTableModel) table.getTreeTableModel();
        
        for(int i = 0; i < endItemList.size(); i++) {
            EndItemData endItemData = endItemList.get(i);
            String operationId = endItemData.getPitem_id();
            String operationRevId = endItemData.getPitem_revision_id();
            String occPuid = endItemData.getOcc_puid();

            TCComponentBOPLine operationLine = null;
            if(latestOperation != null && operationId.equals(latestOperation.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                operationLine = latestOperation;
            } else {
                operationLine = findOperation(target, operationId, operationRevId);
                latestOperation = operationLine;
            }
            
            if(operationLine != null) {
                expandBOM(tableModel, operationLine);

                TCComponentBOPLine endItem = findEndItemByOccPuid(occPuid);
                if(endItem != null) {
                    BOMLineNode node = tableModel.getNode(endItem);

                    if(node != null) {
                        endItem.setDefaultBackgroundColor(highlightColor);

                        // Pack�Ǿ� �ִ� �����۵��� UnPack�ÿ��� ǥ�õǱ� ���� ó��
                        Iterator<BOMLineNode> iter = node.packedAppearances();
                        while(iter.hasNext()) {
                            TCComponentBOPLine packedBOMLine = (TCComponentBOPLine) iter.next().getBOMLine();
                            packedBOMLine.setDefaultBackgroundColor(highlightColor);

                            if(!bopLines.contains(packedBOMLine)) {
                                bopLines.add(packedBOMLine);
                            }
                        }
                        
                        bopLines.add(endItem);        
                        // [SR150421-019][20150518] shcho, Pack �Ǿ��ִ� E/Item�� ��� Unpack�� �Ͽ� ��� ���������� ǥ�� �ǵ��� ����
                        endItem.unpack();
                        endItem.parent().refresh();
                    }
                }
            }
        }
        
        return bopLines;
    }
    
    private void setBOMView(BOMTreeTable table, List<TCComponentBOPLine> endItemLines) throws Exception {
        BOMTreeTableModel tableModel = (BOMTreeTableModel) table.getTreeTableModel();
        
        for(TCComponentBOPLine endItem : endItemLines) {
            TCComponentBOMLine endItemBomline = SDVBOPUtilities.getAssignSrcBomLine(table.getBOMWindow(), endItem);
            // [SR150421-019][20150518] shcho, Pack �Ǿ��ִ� E/Item�� ��� Unpack�� �Ͽ� ��� ���������� ǥ�� �ǵ��� ����
            endItemBomline.unpack();
            endItemBomline.parent().refresh();
            
            if(endItemBomline != null) {
                endItemBomline.setDefaultBackgroundColor(highlightColor);
                expandBOM(tableModel, endItemBomline.parent());
            }
        }
    }
    
    private BOMTreeTable[] getTables() throws Exception {
        BOMTreeTable[] tables = new BOMTreeTable[2];

        // ���� BOP�� Active �� BOP Table�� �����´�.
        MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
        tables[0] = application.getViewableTreeTable();
        clearBackgroundColor(tables[0]);

        // MPP�� ���µǾ� �ִ� View�� �߿��� MProduct�� ����� View�� ���̺��� ã�´�.
        AbstractViewableTreeTable[] treeTables = application.getViewableTreeTables();
        for(AbstractViewableTreeTable treeTable : treeTables) {
            if(treeTable instanceof CMEBOMTreeTable) {
                if(mProductRevision.equals(treeTable.getBOMRoot().getItemRevision())) {
                    if(displayMode == 2) {
                        tables[1] = (BOMTreeTable) treeTable;
                    }
                    
                    clearBackgroundColor(treeTable);
                    break;
                }
            }
        }
    
        return tables;
    }
    
    private void clearBackgroundColor(AbstractViewableTreeTable table) {
        TreeTableNode[] allNodes = table.getAllNodes(BOMLineNode.class);
        if(allNodes != null) {
            for(TreeTableNode node : allNodes) {
                ((BOMLineNode) node).setBackgroundColor(null);
            }
        }
    }
    
    private TCComponentBOPLine findOperation(TCComponentBOPLine parent, String operationId, String operationRevId) throws TCException {
        if(isOperation(parent)) {
            if(operationId.equals(parent.getProperty(SDVPropertyConstant.BL_ITEM_ID)) && 
                    operationRevId.equals(parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID))) {
                latestOperation = parent;
                return parent;
            }
            return null;
        }
        
        if(isProcess(parent)) {
            AIFComponentContext[] contexts = parent.getChildren();
            if(contexts != null) {
                for(AIFComponentContext context : contexts) {
                    parent = findOperation((TCComponentBOPLine) context.getComponent(), operationId, operationRevId);
                    if(parent != null) {
                        return parent;
                    }
                }
            }
        }

        return null;
    }
    
    private TCComponentBOPLine findEndItemByOccPuid(String occPuid) throws TCException {
        AIFComponentContext[] contexts = latestOperation.getChildren();
        
        if(contexts != null) {
            for(AIFComponentContext context : contexts) {
                TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
                /* [SR150421-019][20150518] shcho, Pack �Ǿ��ִ� E/Item�� ��� Unpack�� �Ͽ� ��� ���������� ǥ�� �ǵ��� ����
                // [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item �޴� ��� ����
                if (bopLine.isPacked())
                {
                    bopLine.unpack();
                    bopLine.refresh();
                }
                */
                if(occPuid.equals(bopLine.getProperty(SDVPropertyConstant.BL_OCC_FND_OBJECT_ID))) {
                    return bopLine;
                }
            }
            /*[SR150421-019][20150518] shcho, Pack �Ǿ��ִ� E/Item�� ��� Unpack�� �Ͽ� ��� ���������� ǥ�� �ǵ��� ����
            // [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item �޴� ��� ����
            latestOperation.refresh();
            */
        }
        
        return null;
    }

    private boolean isOperation(TCComponentBOPLine bopLine) throws TCException {
        String type = bopLine.getItem().getType();
        if (SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM.equals(type)) {
            return true;
        }

        return false;
    }
    
    private boolean isProcess(TCComponentBOPLine bopLine) throws TCException {
        TCComponentItem item = bopLine.getItem();
        if (item instanceof TCComponentMEProcess) {
            return true;
        }

        return false;
    }
    
    private void expandBOM(BOMTreeTableModel tableModel, TCComponentBOMLine bomline) throws TCException, ExpandVetoException {
        List<TCComponentBOMLine> parentList = new ArrayList<TCComponentBOMLine>();
        parentList.add(bomline);
        
        while(true) {
            bomline = (TCComponentBOMLine) bomline.parent();
            parentList.add(0, bomline);
            if(target.equals(bomline) || bomline.isRoot()) {
                break;
            }
        }
        
        for(TCComponentBOMLine parent : parentList) {
            BOMLineNode node = tableModel.getNode(parent);
            if(node != null && !tableModel.isNodeExpanded(node)) {
                node.loadChildren();
                tableModel.expandNode(node);
            }
        }
    }
}
