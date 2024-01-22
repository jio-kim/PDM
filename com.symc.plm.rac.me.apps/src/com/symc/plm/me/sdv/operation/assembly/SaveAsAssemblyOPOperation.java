/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.tree.TreePath;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.AISInstructionDatasetCopyUtil;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.psebase.common.AbstractViewableNode;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.treetable.TreeTableNode;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SaveAsAssemblyOPOperation
 * Class Description :
 * 
 * [SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
 * 
 * @date 2013. 11. 27.
 * 
 */
public class SaveAsAssemblyOPOperation extends AbstractSDVActionOperation {
    private IDataSet dataSet = null;
    private TCComponentItem createdItem = null;
    private static String DEFAULT_REV_ID = "000";
    private TCComponentBOMLine newOpBOMLine = null;
    private TCComponentBOMLine tempBOMLine = null;
    private Registry registry = null;

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public SaveAsAssemblyOPOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
    	
        if(newOpBOMLine!=null){

			TCComponentItemRevision newOpRevision = null;
			try {
				newOpRevision = newOpBOMLine.getItemRevision();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
			if(newOpRevision==null){
				return;
			}
			
			
			TCComponent[] koDataSets = null;
			try {
				koDataSets = newOpRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
			if(koDataSets==null){
		        String newOpItemId = null;
		        String newOpItemRevisionId = null; 
				try {
					newOpItemId = newOpRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
					newOpItemRevisionId = newOpRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
				} catch (TCException e2) {
					e2.printStackTrace();
				}
				TCComponentDataset korSheetDataSet = null;
				if(newOpItemId!=null && newOpItemRevisionId!=null){
					try {
						korSheetDataSet = SDVBOPUtilities.getTemplateDataset("M7_TEM_DocItemID_ProcessSheet_Kor", newOpItemId + "/" + newOpItemRevisionId, newOpItemId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(korSheetDataSet!=null){
					try {
						newOpRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, korSheetDataSet);
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
				
			}

			TCComponent[] dataSets = null;
			try {
				dataSets = newOpRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			if(dataSets!=null){
				try {
					newOpRevision.remove(SDVTypeConstant.PROCESS_SHEET_EN_RELATION, dataSets);
				} catch (TCException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < dataSets.length; i++) {
					try {
						dataSets[i].delete();
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
			}
				
			TCComponent root = null;
			try {
				root = newOpRevision.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
            if(root != null) {
                TCComponentMEActivity rootActivity = null;
				try {
					rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
					if(rootActivity!=null){
						rootActivity.refresh();
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
                
                TCComponent[] children = null;
				try {
					children = ActivityUtils.getSortedActivityChildren(rootActivity);
				} catch (TCException e) {
					e.printStackTrace();
				}

				if(children != null) {
                    int childCnt = children.length;
                    for(int j = 0; j < childCnt; j++) {
                    	try {
							children[j].setProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME, "");
							children[j].save();
						} catch (TCException e) {
							e.printStackTrace();
						}
                    }
                }
            }
            
			try {
				newOpRevision.save();
			} catch (TCException e) {
				e.printStackTrace();
			}
        }
        
        try {
            /**
             * 복제된 End Item 을 제거함
             */
            // 제거는 공법 추가 후 Operation 끝나고 실행함. 이렇게 안하면 Invalid Tag 발생함
            removeEndItemsFromOperation(newOpBOMLine);
            
            if (!isAbortRequested()) {
                String message = registry.getString("OPAddedToTempLine.MSG", "Opearation(%0) was added to Temp Line(%1).").replace("%0", newOpBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID)).replace("%1", tempBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
                MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), message, "Information", MessageBox.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        


    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        try {
            registry = Registry.getRegistry(this);
            dataSet = getDataSet();
            
            // [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
            TCComponentItemRevision mecoRevision = isOwnedMECO();
            if(mecoRevision==null){
            	throw new Exception("Check MECO owning user");
            }

            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            TCComponentBOMLine shopBOMLine = mfgApp.getBOMWindow().getTopBOMLine(); // Shop BOPLine
            TCComponentBOMLine srcBOMLine = mfgApp.getSelectedBOMLines()[0]; // 선택된 BOMLine

            String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
            String lineCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
            String functionCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
            String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
            String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
            // Item Id 조합
            String itemId = vechicleCode + "-" + lineCode + "-" + functionCode + "-" + opCode + "-" + bopVersion;

            String korName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);

            // 미할당 Line을 가져옴
            tempBOMLine = getTempBOPLine(shopBOMLine);

            // 미할당 Line이 Release 되었다면 자동개정함
            ReviseBOMLine(tempBOMLine);

            createdItem = srcBOMLine.getItemRevision().saveAsItem(itemId, DEFAULT_REV_ID, korName, "", false, null);
            /**
             * 속성 Update
             */
            setProperties();

            /**
             * 공법Revision에 작업표준서를 붙임
             */
            //attachProcessExcelToOP(createdItem.getLatestItemRevision());
            // [SR150714-039][20151117] taeku.jeong 공법 복사 시 작업그림 Data-set도 복사되도록 보완
            TCComponentItemRevision orignItemRevision = srcBOMLine.getItemRevision();
            TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
       	 	AISInstructionDatasetCopyUtil aisInstructionDatasetCopyUtil = new AISInstructionDatasetCopyUtil(createdItemRevision.getSession());
       	 	aisInstructionDatasetCopyUtil.assemblyInstructionSheetCopy(orignItemRevision, createdItemRevision);

            /**
             * BOP Line을 추가
             */
            newOpBOMLine = addOperationBOPLine(tempBOMLine, createdItem);
            
            // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
        	BOPLineUtility.updateLineToOperationAbsOccId(newOpBOMLine);

            /**
             * MECO에 생성된 Item Revision을 붙임
             */
            AddRevisionToMecoRevision(createdItem);

            /**
             * 윈도우 저장
             */
            shopBOMLine.window().save();

            /**
             * 미할당 라인 Expand 함
             */
            AbstractViewableTreeTable treetable = mfgApp.getAbstractViewableTreeTable();
            AbstractViewableNode rootNode = treetable.getRootNode();
            Iterator<TreeTableNode> iterator = rootNode.allChildrenIterator(true);
            while (iterator != null && iterator.hasNext()) {
                AbstractViewableNode childNode = (AbstractViewableNode) iterator.next();                
                if (childNode.getName().startsWith(tempBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                    treetable.setSelectionPaths(new TreePath[]{childNode.getTreePath()});
                    SDVBOPUtilities.executeExpandOneLevel();
                    break;
                }
            }

        } catch (Exception ex) {
            setAbortRequested(true);
            ex.printStackTrace();
            throw ex;
        }

    }

    /**
     * 공법 BOPLine을 추가 및 BOP 속성 추가
     * 
     * [SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
     * 
     * @method addOperationBOPLine
     * @date 2013. 11. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine addOperationBOPLine(TCComponentBOMLine targetBOMLine, TCComponentItem createdItem) throws Exception {

        TCComponentBOMLine newOpBOMLine = targetBOMLine.add(null, createdItem.getLatestItemRevision(), null, false);
        TCComponentItemRevision opRevision = newOpBOMLine.getItemRevision();

        String stationNo = opRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO).replace("-", "");// 공정번호
        String workerCode = createdItem.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE).replace("-", "");// 작업자코드
        String seq = createdItem.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);// 작업자 순번
        // 수량 입력
        //newOpBOMLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
        // 공정편성번호 입력
        boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty() || seq.isEmpty(); // 하나라도 값이 없으면 반영안함
        String findNo = stationNo.concat("|").concat(workerCode).concat("|").concat(seq);
        if (findNo.length() > 15 || isExistEmptyValue)
            return newOpBOMLine;
        newOpBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
        return newOpBOMLine;

    }

    /**
     * 속성정보 입력
     * 
     * @method setProperties
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setProperties() throws Exception {

        /**
         * Item 속성 Update
         */
        String workerCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORKER_CODE);
        String processSeq = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_PROCESS_SEQ);
        String workArea = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORKAREA);
        String workUbody = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORK_UBODY);
        String itemUL = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_ITEM_UL);
        boolean maxWorkTimeCheck = (Boolean) dataSet.getValue(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK);
        boolean vehicleCheck = (Boolean) dataSet.getValue(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK);

        // Process Sequence 두자리 입력시 앞에 0 을 붙인다.
        if (processSeq.length() == 2)
            processSeq = "0".concat(processSeq);

        createdItem.setProperty(SDVPropertyConstant.OPERATION_WORKER_CODE, workerCode);
        createdItem.setProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ, processSeq);
        if (!workArea.isEmpty())
            createdItem.setProperty(SDVPropertyConstant.OPERATION_WORKAREA, workArea);
        if (!workUbody.isEmpty())
            createdItem.setProperty(SDVPropertyConstant.OPERATION_WORK_UBODY, workUbody);
        if (!itemUL.isEmpty())
            createdItem.setProperty(SDVPropertyConstant.OPERATION_ITEM_UL, itemUL);

        createdItem.setLogicalProperty(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, maxWorkTimeCheck);
        createdItem.setLogicalProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, vehicleCheck);

        /**
         * Revision 속성 Update
         */
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
        
        
        /**
         * MECO
         */
         String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision) dataSet.getValue("mecoSelect", "mecoRev");
        /**
         * 공법정보
         */
        String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
        String shopCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
        String fcCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
        String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
        String productCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
        String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        String dr = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_DR);
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        String specialChar = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // 공정정보
        String stationNo = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_STATION_NO);
        /**
         * 기타 정보
         */
        @SuppressWarnings("unchecked")
        ArrayList<String> dwgNoList = (ArrayList<String>) dataSet.getListValue("opInform", SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
        String assySystem = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM);

        createdItemRevision.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoRevision);

        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vechicleCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SHOP, shopCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, fcCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bopVersion);

        if (!dr.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_DR, dr);

        if (!stationNo.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO, stationNo);

        if (dwgNoList.size() > 0) {
            String[] dwgNoArray = dwgNoList.toArray(new String[dwgNoList.size()]);
            createdItemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).setStringValueArray(dwgNoArray);
        }
        if (!assySystem.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, assySystem);
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (!specialChar.isEmpty())
        	createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialChar);
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
       // 요청자 : 김용환 차장
        // 수정자 : bc.kim
        // 수정내용 : 공법 복사시 초도가 됨으로 초도의 영문 속성값은 국문 이름으로 바꿈
        
        String korName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME, korName);
        
         //////////////////////////////////////////////////////////////////////////////////////////////////

    }

    /**
     * Shop 하위에 생성된 미할당 Line의 BOMLine을 가져옴
     * 
     * @method getTempBOPLine
     * @date 2013. 11. 21.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine getTempBOPLine(TCComponentBOMLine topBOMLine) throws Exception {

        String shopId = topBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String[] idSplit = shopId.split("-");
        // 미할당 LINE ID
        String tempLineId = idSplit[0] + "-" + idSplit[1] + "-TEMP-" + idSplit[2];

        AIFComponentContext[] aifComps = topBOMLine.getChildren();
        for (AIFComponentContext aifComp : aifComps) {
            TCComponentBOMLine lineBOMLine = (TCComponentBOMLine) aifComp.getComponent();
            String lineId = lineBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            if (tempLineId.equals(lineId))
                return lineBOMLine;
        }
        return null;
    }

    /**
     * MECO에 생성된 Item Revision을 Solution Item에 붙인다.
     * 
     * @method AddRevisionToMecoRevision
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void AddRevisionToMecoRevision(TCComponentItem createdItem) throws Exception {
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision) dataSet.getValue("mecoSelect", "mecoRev");
        if(mecoRevision == null)
            return;
        mecoRevision.add("CMHasSolutionItem", createdItem.getLatestItemRevision());

    }
    
    /**
     * [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
     * MECO의 Owner 와 현재 Login 한 User가 다른 경우 Operation을 더이상 진행 할 수 없도록 한다.
     * @return
     */
    private TCComponentItemRevision isOwnedMECO(){
    	
    	TCComponentItemRevision ownedMecoRevision = null;
    	TCComponentItemRevision mecoRevision = (TCComponentItemRevision) dataSet.getValue("mecoSelect", "mecoRev");
        if(mecoRevision!=null){
        	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoRevision, (TCSession)this.getSession());
        	ownedMecoRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
        }
		
        return ownedMecoRevision;
    }

    /**
     * 작업표준서 Excel Template 파일을 공법아래에 붙임
     * 
     * @method attachProcessExcelToOP
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void attachProcessExcelToOP(TCComponentItemRevision opRevision) throws Exception {
        String itemId = opRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String revision = opRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
        TCComponentDataset procDataSet = SDVBOPUtilities.getTemplateDataset("M7_TEM_DocItemID_ProcessSheet_Kor", itemId + "/" + revision, itemId);
        opRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, procDataSet);
    }

    /**
     * 복제된 End Item을 제거함
     */
    private void removeEndItemsFromOperation(TCComponentBOMLine opBOMLine) throws Exception {

        try {
            ArrayList<TCComponentBOMLine> endItemBOPList = new ArrayList<TCComponentBOMLine>();
            AIFComponentContext[] aifComps = opBOMLine.getChildren();
            for (AIFComponentContext aifComp : aifComps) {
                TCComponentBOMLine childBOMLine = (TCComponentBOMLine) aifComp.getComponent();
                String itemType = childBOMLine.getItem().getType();

                if (!itemType.equals(SDVTypeConstant.EBOM_STD_PART) && !itemType.equals(SDVTypeConstant.EBOM_VEH_PART))
                    continue;
                endItemBOPList.add(childBOMLine);
            }

            SDVBOPUtilities.disconnectObjects(opBOMLine, endItemBOPList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Release된 BOMLINE 개정함
     * 
     * @method ReviseBOMLine
     * @date 2013. 12. 30.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void ReviseBOMLine(TCComponentBOMLine targetBOMLine) throws Exception {

        if (!CustomUtil.isReleased(targetBOMLine.getItemRevision()))
            return;
        String newRev = targetBOMLine.getItem().getNewRev();
        TCComponentItemRevision newRevision = targetBOMLine.getItemRevision().saveAs(newRev);

        targetBOMLine.window().newIrfWhereConfigured(newRevision);
        targetBOMLine.window().fireChangeEvent();
    }
}
