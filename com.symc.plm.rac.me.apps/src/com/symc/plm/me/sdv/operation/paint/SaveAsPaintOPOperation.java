/**
 * 
 */
package com.symc.plm.me.sdv.operation.paint;

import java.util.ArrayList;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.AISInstructionDatasetCopyUtil;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.BOPLineUtility;
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
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * 
 * [SR150624-021][20150624] shcho, 공법 복사시 Home의 NewStuff폴더에 만들어지던 것을 BOPLine에 바로 등록하도록 변경
 * Class Name : SaveAsPaintOPOperation
 * Class Description :
 * 
 * @date 2013. 12. 5.
 * 
 */
public class SaveAsPaintOPOperation extends AbstractSDVActionOperation {
    private IDataSet dataSet = null;
    private TCComponentItem createdItem = null;
    private static String DEFAULT_REV_ID = "000";
    private TCComponentBOMLine newOpBOMLine = null;
    private TCComponentBOMLine srcBOMLine = null;
    private Registry registry = null;

    public SaveAsPaintOPOperation(int actionId, String ownerId, IDataSet dataSet) {
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
            //removeEndItemsFromOperation(newOpBOMLine);
            
            if (!isAbortRequested()) {
                registry =Registry.getRegistry(this);
                MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), ("Opearation(%0) was added to BOPLine(%1).").replace("%0", newOpBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID)).replace("%1", srcBOMLine.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID)), registry.getString("Inform.NAME"), MessageBox.INFORMATION);
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
        dataSet = getDataSet();
        
        // [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
        TCComponentItemRevision mecoRevision = isOwnedMECO();
        if(mecoRevision==null){
        	throw new Exception("Check MECO owning user");
        }
        
        try {
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();

            TCComponentBOMLine shopBOMLine = mfgApp.getBOMWindow().getTopBOMLine(); // Shop BOPLine
            srcBOMLine = mfgApp.getSelectedBOMLines()[0];

            String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
            String shopCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
            String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
            String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
            // Item Id 조합
            String itemId = vechicleCode + "-" + shopCode + "-" + opCode + "-" + bopVersion;

            String korName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);

            /**
             * Item 생성
             */
            createdItem = srcBOMLine.getItemRevision().saveAsItem(itemId, DEFAULT_REV_ID, korName, "", false, null);

            /**
             * 속성 Update
             */
            setProperties();
            
            /**
             * 공법Revision에 작업표준서를 붙임
             */
            // attachProcessExcelToOP(createdItem.getLatestItemRevision());
            // [SR150714-039][20151117] taeku.jeong 공법 복사 시 작업그림 Data-set도 복사되도록 보완
            TCComponentItemRevision orignItemRevision = srcBOMLine.getItemRevision();
            TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
       	 	AISInstructionDatasetCopyUtil aisInstructionDatasetCopyUtil = new AISInstructionDatasetCopyUtil(createdItemRevision.getSession());
       	 	aisInstructionDatasetCopyUtil.assemblyInstructionSheetCopy(orignItemRevision, createdItemRevision);


            /**
             * BOP Line을 추가
             */
            newOpBOMLine = addOperationBOPLine(srcBOMLine.parent(), createdItem);
            
            /**
             * MECO에 생성된 Item Revision을 붙임
             */
            AddRevisionToMecoRevision(createdItem);

            /**
             * 윈도우 저장
             */
            shopBOMLine.window().save();
                    
        } catch (Exception ex) {

            setAbortRequested(true);
            throw ex;
        }
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
        String opEngName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_ENG_NAME);

        
        //createdItem.setProperty("uom_tag", "EA");

        /**
         * Revision 속성 Update
         */
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
        
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME, opEngName);
        /**
         * MECO
         */
        //String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision)dataSet.getValue("mecoSelect", "mecoRev");
        /**
         * 공법정보
         */
        String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
        String shopCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);

        String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
        String productCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
        String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        String dr = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_DR);

        String workerCount = dataSet.getStringValue("opInform", SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);

        String lineCode = dataSet.getStringValue("opInform", SDVPropertyConstant.STATION_LINE);
        String stationCode = dataSet.getStringValue("opInform", SDVPropertyConstant.STATION_STATION_CODE);
        /**
    	 * 이종화 차장님 요청
    	 * 공법 생성 화면에서 특별 특성 속성입력란 추가
    	 */
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        String specialChar = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        @SuppressWarnings("unchecked")
        ArrayList<String> dwgNoList = (ArrayList<String>) dataSet.getListValue("opInform", SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
        boolean isKPC = (Boolean) dataSet.getValue(SDVPropertyConstant.OPERATION_REV_KPC);

        createdItemRevision.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoRevision);

        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vechicleCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SHOP, shopCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bopVersion);

        createdItemRevision.setProperty(SDVPropertyConstant.STATION_LINE, lineCode);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_STATION_CODE, stationCode);

        if (!workerCount.isEmpty())
            createdItemRevision.setIntProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT, Integer.parseInt(workerCount));

        if (!dr.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_DR, dr);

        if (dwgNoList.size() > 0) {
            String[] dwgNoArray = dwgNoList.toArray(new String[dwgNoList.size()]);
            createdItemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).setStringValueArray(dwgNoArray);
        }

        createdItemRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_KPC, isKPC);
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialChar);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        String revision  = opRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
        TCComponentDataset procDataSet = SDVBOPUtilities.getTemplateDataset("M7_TEM_DocItemID_ProcessSheet_Kor", itemId +"/"+revision, itemId);
        opRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, procDataSet);

        /*
        TCComponentDataset srcDataSet = null;
        TCComponent tempTCComponet = srcBOMLine.getItemRevision().getRelatedComponent(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
        if(tempTCComponet instanceof TCComponentDataset) {
            srcDataSet = (TCComponentDataset) tempTCComponet;
        }
        
        if(srcDataSet != null) {
            TCComponentTcFile[] tcFiles = srcDataSet.getTcFiles();
            File file = tcFiles[0].getFile(null, opRevision.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID) +  ".xlsx");
            TCComponentDataset newDataset = SDVBOPUtilities.createDataset(file.getAbsolutePath(), itemId+"/"+revision);
            if(newDataset != null) {
                opRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, newDataset);
            }
        }
        */
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
        String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItem mecoItem = SDVBOPUtilities.FindItem(mecoNo, SDVTypeConstant.MECO_ITEM);
        if (mecoItem == null)
            return;
        TCComponentItemRevision mecoItemRevision = mecoItem.getLatestItemRevision();
        mecoItemRevision.add("CMHasSolutionItem", createdItem.getLatestItemRevision());

    }
    
	/**
     * [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
     * MECO의 Owner 와 현재 Login 한 User가 다른 경우 Operation을 더이상 진행 할 수 없도록 한다.
     * @return
     */
    private TCComponentItemRevision isOwnedMECO(){
    	
    	TCComponentItemRevision mecoItemRevision =  null;
        String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        if(mecoNo!=null && mecoNo.trim().length()>0){
        	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoNo, (TCSession)this.getSession());
        	mecoItemRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
        }
		
        return mecoItemRevision;
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

        // 수량 입력
        //newOpBOMLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
        
        // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
    	BOPLineUtility.updateLineToOperationAbsOccId(newOpBOMLine);        
        
        return newOpBOMLine;

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

}
