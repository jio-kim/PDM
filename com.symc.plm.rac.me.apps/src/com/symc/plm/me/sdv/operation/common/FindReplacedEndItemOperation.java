package com.symc.plm.me.sdv.operation.common;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.tree.ExpandVetoException;

import com.ssangyong.dto.EndItemData;
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
 * [SR150122-027][20150210] shcho, Find automatically replaced end item (공법 할당 E/Item의 설계 DPV에 의한 자동 변경 오류 해결) (10버전에서 개발 된 소스 9으로 이식함)
 * [SR150122-027][20150309] shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 * [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item 메뉴 기능 보완 (UnPack 기능 추가)
 * [SR150421-019][20150518] shcho, Pack 되어있는 E/Item의 경우 Unpack을 하여 모두 빨간색으로 표시 되도록 수정
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
            //  BOP Top과 연결된 MProduct를 찾는다.
            TCComponentBOPLine topBopLine = (TCComponentBOPLine) target.window().getTopBOMLine();
            // [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
            mProductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(topBopLine.getItemRevision());
            
            if(mProductRevision == null) {
                MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("NotExistMProduct.MSG"), 
                        registry.getString("Inform.NAME"), MessageBox.INFORMATION); 
                return;
            }
                    
            // 자동 Replace 된 아이템을 DB 쿼리하여 가져온다.
            CustomBOPDao dao = new CustomBOPDao();  
            ArrayList<EndItemData> endItemList = dao.findReplacedEndItems(target.getProperty(SDVPropertyConstant.BL_ITEM_PUID),
                    mProductRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
            
            // 자동 Replace 된 아이템이 없으면 메세지 박스를 출력한다.
            if(endItemList == null || endItemList.size() == 0) {
                MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("NotExistReplacedEndItem.MSG"), 
                        registry.getString("Inform.NAME"), MessageBox.INFORMATION); 
                return;
            }
            
            // SR161209-032 구현해야 될 부분  시작
            // 수정자 : 김병찬 과장
            // 수정 일자 : 2017.03.30
            // 자동 Replace 된 아이템리스트를 받아 그것의 ECO Released Date 와 공법 리비전의(Parent ItemRevsion) 작업표준서 Published Date를 찾아오는 로직 추가
            
            // 쿼리를 통해서 받은 EndItemList 를 아래의 로직을 통해 필터링 한 후 다시 담을 변수 선언
            ArrayList<EndItemData> newEndItemList  = new ArrayList<EndItemData>();
            
            for( int i = 0; i < endItemList.size(); i ++ ) {
            	 // 비교 대상이 되는 ECO ReleaseDate를 담을 변수 선언
            	Date compareMEcoReleasedDate = null;
            	// 비교 대상이 되는 공법 리비전의 Published Date를 담을 변수 선언
            	Date compareOpRevPublishedDate = null;
            	 
            	 EndItemData endItemData = endItemList.get(i);
            	 
            	 String operationItemId = endItemData.getPitem_id();
            	 
            	 // target의 view Window 에서 파라미터로 입력한 ID로 BOMLine을 검색
            	 TCComponentBOMLine[] searchOpBomLines = target.window().findConfigedBOMLinesForAbsOccID(operationItemId, true, target.window().getTopBOMLine());
            	 TCComponentBOMLine searchOpBomLine = null;
            	 TCComponentItemRevision searchOpItemRevision = null;
            	 String operationItemRevId = "";
            	 if( searchOpBomLines.length != 0 && searchOpBomLines != null ) {
            		 searchOpBomLine = searchOpBomLines[0];
            		 //BOM Line 으로 부터 ItemReivsion 추출
            		 searchOpItemRevision = searchOpBomLine.getItemRevision();
            		 // Item Revision으로 부터 Revision ID 추출
            		 operationItemRevId = searchOpItemRevision.getProperty( SDVPropertyConstant.ITEM_REVISION_ID );
            	 } else {
            		 continue;
            	 }
            	 
            	 // EndItem 리스트의 OPeration Item ID로 작업표준서를 검색
            	 TCComponentItemRevision searchWorkStandardDoc = CustomUtil.findItemRevision( "M7_ProcessSheetRevision", "KPS-" + operationItemId, operationItemRevId + "A" );
            	 
            	 if( searchOpItemRevision != null ) {
            		 // 검색된 Revision의 ECO를 찾기 위해 RelatedComponents를 찾음
            		 TCComponent relatedComps[] = searchOpItemRevision.getRelatedComponents();
            		 if( relatedComps != null && relatedComps.length != 0 ) {
            			 for( int j = 0 ; j < relatedComps.length ; j ++ ) {
            				 TCComponent relatedComp = relatedComps[j]; 
            				 // Related Components 중에서  Type 이 MECO 타입 추출
            				 if( relatedComp.getType().equals( SDVTypeConstant.MECO_ITEM_REV ) ) {
            					 TCComponentItemRevision mEcoRevision = (TCComponentItemRevision) relatedComp;
            					 // 추출된 MECO 타입에서 date_release 속성값 추출
            					 compareMEcoReleasedDate = mEcoRevision.getDateProperty("date_released");
            					 break;
            				 }
            			 }
            		 }
            	 }
            	 
            	 if( searchWorkStandardDoc != null ) {
            		 // 검색된 작업표준서에서 Published Date 속성 값을 추출
            		 TCComponentItemRevision workStandardDoc = (TCComponentItemRevision) searchWorkStandardDoc;
            		 compareOpRevPublishedDate = workStandardDoc.getDateProperty( SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
            	 }
            	 
            	 if( compareMEcoReleasedDate != null && !compareMEcoReleasedDate.equals( "" ) && compareOpRevPublishedDate != null && !compareOpRevPublishedDate.equals( "" ) ) {
            		 
            		 // 추출된 속성값을 비교 날짜비교 함수 Date 클래스의 compareTo 함수 사용
            		 int compare = compareMEcoReleasedDate.compareTo(compareOpRevPublishedDate);
            		 
            		 // int compare = A.compareTo(B) 할경우 
            		 // compare 값이 0보다 크면 A 의 날짜가 더 늦은 것이고
            		 // compare 값이 -1 이면 B의 날짜가 더 늦은 것
            		 // 0일 경우 A와B는 같은 날짜
            		 if( compare > 0 ) {
            			 newEndItemList.add(endItemData);
            		 }
            	 }
            } // SR161209-032 구현해야 될 부분  끝
            
            BOMTreeTable[] tables = getTables();
            
            // 수정 
            // 수정자 : 김병찬 과장
            // 수정 내용 : setBOPView 메서드에 들어가는 파라미터 변경 endItemList -> newEndItemList 로 변경
            // 수정 사유 : SR161209-032 구현으로 인한 변수 명 변경
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

                        // Pack되어 있는 아이템들은 UnPack시에도 표시되기 위한 처리
                        Iterator<BOMLineNode> iter = node.packedAppearances();
                        while(iter.hasNext()) {
                            TCComponentBOPLine packedBOMLine = (TCComponentBOPLine) iter.next().getBOMLine();
                            packedBOMLine.setDefaultBackgroundColor(highlightColor);

                            if(!bopLines.contains(packedBOMLine)) {
                                bopLines.add(packedBOMLine);
                            }
                        }
                        
                        bopLines.add(endItem);        
                        // [SR150421-019][20150518] shcho, Pack 되어있는 E/Item의 경우 Unpack을 하여 모두 빨간색으로 표시 되도록 수정
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
            // [SR150421-019][20150518] shcho, Pack 되어있는 E/Item의 경우 Unpack을 하여 모두 빨간색으로 표시 되도록 수정
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

        // 현재 BOP에 Active 된 BOP Table을 가져온다.
        MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
        tables[0] = application.getViewableTreeTable();
        clearBackgroundColor(tables[0]);

        // MPP에 오픈되어 있는 View들 중에서 MProduct와 연결된 View의 테이블을 찾는다.
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
                /* [SR150421-019][20150518] shcho, Pack 되어있는 E/Item의 경우 Unpack을 하여 모두 빨간색으로 표시 되도록 수정
                // [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item 메뉴 기능 보완
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
            /*[SR150421-019][20150518] shcho, Pack 되어있는 E/Item의 경우 Unpack을 하여 모두 빨간색으로 표시 되도록 수정
            // [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item 메뉴 기능 보완
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
