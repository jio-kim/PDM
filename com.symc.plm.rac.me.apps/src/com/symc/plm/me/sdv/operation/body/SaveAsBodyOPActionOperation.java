/**
 *
 */
package com.symc.plm.me.sdv.operation.body;

import java.util.ArrayList;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.AISInstructionDatasetCopyUtil;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.sdv.view.body.CreateBodyOPView;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 *[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
 *
 */
public class SaveAsBodyOPActionOperation extends AbstractSDVActionOperation {
	
	private Registry registry = Registry.getRegistry(SaveAsBodyOPActionOperation.class);
	private TCComponentItemRevision newOperationRevision;
	private TCComponentBOMLine targetBOMLine;
	/**
	 * @param actionId
	 * @param ownerId
	 * @param dataSet
	 */
	public SaveAsBodyOPActionOperation(int actionId, String ownerId, IDataSet dataSet) {
		super(actionId, ownerId, dataSet);
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
	 */
	@Override
	public void startOperation(String commandId) {
		System.out.println("SaveAsBodyOPActionOperation [Start]");
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVOperation#endOperation()
	 */
	@Override
	public void endOperation() {

		System.out.println("Body..................");

        if(newOperationRevision!=null){
        	
			TCComponent[] koDataSets = null;
			try {
				koDataSets = newOperationRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
			if(koDataSets==null){
		        String newOpItemId = null;
		        String newOpItemRevisionId = null; 
				try {
					newOpItemId = newOperationRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
					newOpItemRevisionId = newOperationRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
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
						newOperationRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, korSheetDataSet);
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
				
			}

			TCComponent[] dataSets = null;
			try {
				dataSets = newOperationRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			if(dataSets!=null){
				try {
					newOperationRevision.remove(SDVTypeConstant.PROCESS_SHEET_EN_RELATION, dataSets);
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
				root = newOperationRevision.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
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
				newOperationRevision.save();
			} catch (TCException e) {
				e.printStackTrace();
			}
        }

	}

	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
		IDataSet dataSet = getDataSet();

		Object meco_no = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
		
		// [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
		if(meco_no!=null){
			if(meco_no instanceof TCComponentItemRevision){
				TCComponentItemRevision mecoRevision = isOwnedMECO((TCComponentItemRevision)meco_no);
				if(mecoRevision==null){
					throw new Exception("Check MECO owning user");
				}
			}
		}

		Object parentStationObject = dataSet.getValue("saveAsOPView", SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
		Object targetOPObject = dataSet.getValue("saveAsOPView", CreateBodyOPView.BodyOPViewType);
		String op_code = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
		String kor_name = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_KOR_NAME);
		String eng_name = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_ENG_NAME);
		String bop_ver = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
		String station_code = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_STATION_CODE);
		String shop_code = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_SHOP);
		String vehicle_code = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
		String worker_count = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);
		String dr_code = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_DR);
		String kpc_type = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_KPC);
		Object isAltObj = dataSet.getValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_IS_ALTBOP);
		String altPrefix = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
		ArrayList<?> inst_dwg_no = (ArrayList<?>) dataSet.getListValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
		/**
    	 * 이종화 차장님 요청
    	 * 공법 생성 화면에서 특별 특성 속성입력란 추가
    	 */
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		String specialChar = dataSet.getStringValue("saveAsOPView", SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		TCSession session = CustomUtil.getTCSession();
		Markpoint mp = new Markpoint(session);
		try
		{
			if (targetOPObject == null || targetOPObject.toString().length() == 0)
			{
				throw new NullPointerException("Target Operation is null.");
			}
			
			if(targetOPObject!=null && targetOPObject instanceof TCComponentBOMLine){
				this.targetBOMLine = (TCComponentBOMLine) targetOPObject;
			}

	    	if (! ((TCComponentBOMLine) targetOPObject).parent().getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE).equals(station_code))
	    	{
	    		//TODOS 여기에서 어디에 붙일지를 찾고 하는 코딩이 필요.
//	    		((TCComponentBOMLine) targetOPObject).window().
//	    		AIFComponentContext[] childLines = ((TCComponentBOMLine) targetOPObject).parent().parent().getChildren();
//	    		for (AIFComponentContext childLine : childLines)
//	    		{
//
//	    		}
	    	}

			if (op_code == null || op_code.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (kor_name == null || kor_name.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.OPERATION_REV_KOR_NAME + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (eng_name == null || eng_name.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.OPERATION_REV_ENG_NAME + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (bop_ver == null || bop_ver.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.OPERATION_REV_BOP_VERSION + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (station_code == null || station_code.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.OPERATION_REV_STATION_CODE + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (shop_code == null || shop_code.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.OPERATION_REV_SHOP + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (vehicle_code == null || vehicle_code.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (isAltObj != null && ! ((Boolean) isAltObj) && (meco_no == null || meco_no.toString().trim().length() == 0))
			{
				throw new NullPointerException(SDVPropertyConstant.SHOP_REV_MECO_NO + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

//	    	if (! ((TCComponentBOMLine) target_op_object).parent().getProperty(SDVPropertyConstant.BL_STATION_STATION_CODE).equals(station_code))
//	    	{
//
//	    	}

			String item_id = (((Boolean) isAltObj) ? altPrefix + "-" : "") + vehicle_code + "-" + shop_code + "-" + station_code + op_code + "-" + bop_ver;
			op_code = station_code + op_code;

			TCComponentItem opItem = ((TCComponentBOMLine) targetOPObject).getItemRevision().saveAsItem(item_id, SDVPropertyConstant.ITEM_REV_ID_ROOT);
//			TCComponentItem opItem = SYMTcUtil.createItem(session, item_id, kor_name == null ? "" : kor_name, "", SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM, SDVPropertyConstant.ITEM_REV_ID_ROOT);
			TCComponentItemRevision opRev = opItem.getLatestItemRevision();
			if(opRev!=null){
				this.newOperationRevision = opRev;
			}

			if (kor_name != null && kor_name.trim().length() > 0)
			{
				opRev.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, kor_name);
				opRev.getItem().setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, kor_name);
			}

			if (shop_code != null && shop_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_SHOP, shop_code);

			if (vehicle_code != null && vehicle_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vehicle_code);

			if (station_code != null && station_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_STATION_CODE, station_code);

			if (op_code != null && op_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, op_code);

			if (bop_ver != null && bop_ver.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bop_ver);

			if (worker_count != null && worker_count.trim().length() > 0)
				opRev.setIntProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT, Integer.valueOf(worker_count));

			if (dr_code != null && dr_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_DR, dr_code);

			if (kpc_type != null && kpc_type.trim().length() > 0)
				if (kpc_type.trim().equals("Y"))
					opRev.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_KPC, true);

			if (inst_dwg_no != null && inst_dwg_no.size() > 0)
				opRev.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).setStringValueArray(inst_dwg_no.toArray(new String[0]));

			if (((Boolean) isAltObj))
			{
				opRev.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, true);
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, altPrefix);
			}

			if (meco_no != null)
			{
//				// 공법에 MECO 값을 입력 할시 기존에 MECO 값을 가지고 있는데 다른 값을 입력 할 경우 새로운 값이 입력 안됨
//				TCComponent origine_MECO = opRev.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
//				
//				if( origine_MECO != null ) {
//					opRev.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, null);
//				}
				
				opRev.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, (TCComponent) meco_no);
				opRev.save();

				// MECO Solution에 연결
				((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_SOLUTION_ITEM, opRev);
			}
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////
			if( specialChar != null ) {
				
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialChar );
			}
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
			/**
			 * [SR180622-024] 이종화 차장님 요청 공법 복제시 영문 이름 복사 금지
			 * 				  초도일 경우 영문이름 복사 방지 
			 */
			if (eng_name != null && eng_name.trim().length() > 0) {
				if( !meco_no.toString().startsWith("PBI")) {
					opRev.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME, eng_name);
				}
			}

			//TODOS 작업표준서를 삭제하는 부분이 필요. DeepCopyRule에 의해 작업표준서를 복제하지 않으면 삭제할 필요는 없다.
			// 국문작업표준서 템플릿 연결
			// [SR150323-035][20150507]shcho, 차체 공법 Copy 기능 오류 수정 (작업표준서 template add하지 않도록 막음)
			// opRev.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, SDVBOPUtilities.getTemplateDataset(SDVTypeConstant.PROCESS_SHEET_TEMPLATE_PREF_NAME, null));

			// [SR150714-039][20151117] taeku.jeong 작업그림 Data-set의 암호를 기준에 맞도록 변경함.
       	 	AISInstructionDatasetCopyUtil aisInstructionDatasetCopyUtil = new AISInstructionDatasetCopyUtil(opRev.getSession());
       	 	aisInstructionDatasetCopyUtil.assemblyInstructionSheetPasswordRefresh(opRev);

			// Add to parent BOMLine
			if (parentStationObject != null && parentStationObject instanceof TCComponentBOMLine)
			{
				//TODOS 임시로 막아놨음. 상위를 찾아서 붙이도록 위에서 처리해서 추가하도록..
				TCComponentBOMLine opLine = ((TCComponentBOMLine) parentStationObject).add(opItem, null);
				
	            // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
	        	BOPLineUtility.updateLineToOperationAbsOccId(opLine);				
				
				//[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
				//opLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");

                SDVBOPUtilities.executeExpandOneLevel();

				((TCComponentBOMLine) parentStationObject).save();
				((TCComponentBOMLine) parentStationObject).window().save();
				((TCComponentBOMLine) parentStationObject).refresh();
			}

//			opRev.save();
//			opItem.save();
		}
		catch (Exception ex)
		{
			mp.rollBack();
			setErrorMessage(ex.getMessage());
			setExecuteError(ex);
			throw ex;
		}

		mp.forget();
	}

	/**
	 * [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
	 * MECO의 Owner 와 현재 Login 한 User가 다른 경우 Operation을 더이상 진행 할 수 없도록 한다.
	 * @return
	 */
	private TCComponentItemRevision isOwnedMECO(TCComponentItemRevision mecoRevision){
		
		TCComponentItemRevision ownMecoItemRevision = null;
	
    	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoRevision, (TCSession)this.getSession());
    	ownMecoItemRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
		
	    return ownMecoItemRevision;
	}
}
