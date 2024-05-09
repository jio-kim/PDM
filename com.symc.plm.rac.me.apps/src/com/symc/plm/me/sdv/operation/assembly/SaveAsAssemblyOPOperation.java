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
 * [SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
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
            
            // [20240430][UPGRADE] save �� The instance is not locked �����߻�, setPropery �� SOA setProperty�� ����ؼ� save(deprecated) �� �ʿ����.
//			try {
//				newOpRevision.save();
//			} catch (TCException e) {
//				e.printStackTrace();
//			}
        }
        
        try {
            /**
             * ������ End Item �� ������
             */
            // ���Ŵ� ���� �߰� �� Operation ������ ������. �̷��� ���ϸ� Invalid Tag �߻���
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
            
            // [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
            TCComponentItemRevision mecoRevision = isOwnedMECO();
            if(mecoRevision==null){
            	throw new Exception("Check MECO owning user");
            }

            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            TCComponentBOMLine shopBOMLine = mfgApp.getBOMWindow().getTopBOMLine(); // Shop BOPLine
            TCComponentBOMLine srcBOMLine = mfgApp.getSelectedBOMLines()[0]; // ���õ� BOMLine

            String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
            String lineCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
            String functionCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
            String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
            String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
            // Item Id ����
            String itemId = vechicleCode + "-" + lineCode + "-" + functionCode + "-" + opCode + "-" + bopVersion;

            String korName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);

            // ���Ҵ� Line�� ������
            tempBOMLine = getTempBOPLine(shopBOMLine);

            // ���Ҵ� Line�� Release �Ǿ��ٸ� �ڵ�������
            ReviseBOMLine(tempBOMLine);

            createdItem = srcBOMLine.getItemRevision().saveAsItem(itemId, DEFAULT_REV_ID, korName, "", false, null);
            /**
             * �Ӽ� Update
             */
            setProperties();

            /**
             * ����Revision�� �۾�ǥ�ؼ��� ����
             */
            //attachProcessExcelToOP(createdItem.getLatestItemRevision());
            // [SR150714-039][20151117] taeku.jeong ���� ���� �� �۾��׸� Data-set�� ����ǵ��� ����
            TCComponentItemRevision orignItemRevision = srcBOMLine.getItemRevision();
            TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
       	 	AISInstructionDatasetCopyUtil aisInstructionDatasetCopyUtil = new AISInstructionDatasetCopyUtil(createdItemRevision.getSession());
       	 	aisInstructionDatasetCopyUtil.assemblyInstructionSheetCopy(orignItemRevision, createdItemRevision);

            /**
             * BOP Line�� �߰�
             */
            newOpBOMLine = addOperationBOPLine(tempBOMLine, createdItem);
            
            // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
        	BOPLineUtility.updateLineToOperationAbsOccId(newOpBOMLine);

            /**
             * MECO�� ������ Item Revision�� ����
             */
            AddRevisionToMecoRevision(createdItem);

            /**
             * ������ ����
             */
            shopBOMLine.window().save();

            /**
             * ���Ҵ� ���� Expand ��
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
     * ���� BOPLine�� �߰� �� BOP �Ӽ� �߰�
     * 
     * [SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
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

        String stationNo = opRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO).replace("-", "");// ������ȣ
        String workerCode = createdItem.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE).replace("-", "");// �۾����ڵ�
        String seq = createdItem.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);// �۾��� ����
        // ���� �Է�
        //newOpBOMLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
        // ��������ȣ �Է�
        boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty() || seq.isEmpty(); // �ϳ��� ���� ������ �ݿ�����
        String findNo = stationNo.concat("|").concat(workerCode).concat("|").concat(seq);
        if (findNo.length() > 15 || isExistEmptyValue)
            return newOpBOMLine;
        newOpBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
        return newOpBOMLine;

    }

    /**
     * �Ӽ����� �Է�
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
         * Item �Ӽ� Update
         */
        String workerCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORKER_CODE);
        String processSeq = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_PROCESS_SEQ);
        String workArea = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORKAREA);
        String workUbody = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORK_UBODY);
        String itemUL = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_ITEM_UL);
        boolean maxWorkTimeCheck = (Boolean) dataSet.getValue(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK);
        boolean vehicleCheck = (Boolean) dataSet.getValue(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK);

        // Process Sequence ���ڸ� �Է½� �տ� 0 �� ���δ�.
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
         * Revision �Ӽ� Update
         */
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
        
        
        /**
         * MECO
         */
         String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision) dataSet.getValue("mecoSelect", "mecoRev");
        /**
         * ��������
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

        // ��������
        String stationNo = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_STATION_NO);
        /**
         * ��Ÿ ����
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
        
       // ��û�� : ���ȯ ����
        // ������ : bc.kim
        // �������� : ���� ����� �ʵ��� ������ �ʵ��� ���� �Ӽ����� ���� �̸����� �ٲ�
        
        String korName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME, korName);
        
         //////////////////////////////////////////////////////////////////////////////////////////////////

    }

    /**
     * Shop ������ ������ ���Ҵ� Line�� BOMLine�� ������
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
        // ���Ҵ� LINE ID
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
     * MECO�� ������ Item Revision�� Solution Item�� ���δ�.
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
     * [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
     * MECO�� Owner �� ���� Login �� User�� �ٸ� ��� Operation�� ���̻� ���� �� �� ������ �Ѵ�.
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
     * �۾�ǥ�ؼ� Excel Template ������ �����Ʒ��� ����
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
     * ������ End Item�� ������
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
     * Release�� BOMLINE ������
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
