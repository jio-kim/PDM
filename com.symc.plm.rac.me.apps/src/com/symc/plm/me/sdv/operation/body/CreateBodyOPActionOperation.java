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
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 *[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
 *[UPGRADE] Mark Point 제거. 에러발생후 다시 진행이 안됨 
 *
 */
public class CreateBodyOPActionOperation extends AbstractSDVActionOperation {
	private Registry registry;

	/**
	 * @param actionId
	 * @param ownerId
	 * @param dataSet
	 */
	public CreateBodyOPActionOperation(int actionId, String ownerId, IDataSet dataSet) {
		super(actionId, ownerId, dataSet);

        registry = Registry.getRegistry(CreateBodyOPActionOperation.class);
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
	 */
	@Override
	public void startOperation(String commandId) {
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVOperation#endOperation()
	 */
	@Override
	public void endOperation() {
	}

	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
		IDataSet dataSet = getDataSet();
	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경  아래 getValue부분에 화면의 key값과 속성값 변경 */
//		Object meco_no = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
		Object meco_no = dataSet.getValue(SDVPropertyConstant.MECO_SELECT, SDVPropertyConstant.MECO_REV);
//		Object meco_no = dataSet.getValue("mecoSelect", "mecoRev");
		// [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
		if(meco_no!=null){
			if(meco_no instanceof TCComponentItemRevision){
				TCComponentItemRevision mecoRevision = isOwnedMECO((TCComponentItemRevision)meco_no);
				if(mecoRevision==null){
					throw new Exception("Check MECO owning user");
				}
			}
		}

		Object parentStationObject = dataSet.getValue("createOPView", SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
		String op_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
		String kor_name = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_KOR_NAME);
		String eng_name = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_ENG_NAME);
		String bop_ver = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
		String line_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_LINE);
		String station_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_STATION_CODE);
		String shop_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_SHOP);
		String vehicle_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
		String worker_count = dataSet.getStringValue("createOPView", SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);
		String dr_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_DR);
		String kpc_type = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_KPC);
		
		 /**
    	 * 이종화 차장님 요청
    	 * 공법 생성 화면에서 특별 특성 속성입력란 추가
    	 */
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		String specialCharic_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		Object isAltObj = dataSet.getValue("createOPView", SDVPropertyConstant.OPERATION_REV_IS_ALTBOP);
		String altPrefix = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
		ArrayList<?> inst_dwg_no = (ArrayList<?>) dataSet.getListValue("createOPView", SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);

		TCSession session = CustomUtil.getTCSession();
		//[UPGRADE] Mark
		//Markpoint mp = new Markpoint(session);
		try
		{
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

			String item_id = (((Boolean) isAltObj) ? altPrefix + "-" : "") + vehicle_code + "-" + shop_code + "-" + station_code + op_code + "-" + bop_ver;
			op_code = station_code + op_code;

			TCComponentItem opItem = SYMTcUtil.createItem(session, item_id, kor_name == null ? "" : kor_name, "", SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM, SDVPropertyConstant.ITEM_REV_ID_ROOT);
			TCComponentItemRevision opRev = opItem.getLatestItemRevision();

			if (shop_code != null && shop_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_SHOP, shop_code);

			if (vehicle_code != null && vehicle_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vehicle_code);

			if (line_code != null && line_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_LINE, line_code);

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
			
			 /**
	    	 * 이종화 차장님 요청
	    	 * 공법 생성 화면에서 특별 특성 속성입력란 추가
	    	 */
	        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if (specialCharic_code != null && specialCharic_code.trim().length() > 0)
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialCharic_code);
	        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			if (inst_dwg_no != null && inst_dwg_no.size() > 0)
				opRev.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).setStringValueArray(inst_dwg_no.toArray(new String[0]));

			if (((Boolean) isAltObj))
			{
				opRev.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, true);
				opRev.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, altPrefix);
			}

			if (meco_no != null)
			{
				opRev.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, (TCComponent) meco_no);

				// MECO Solution에 연결
				((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_SOLUTION_ITEM, opRev);
			}

			if (eng_name != null && eng_name.trim().length() > 0)
			    opRev.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME, eng_name);

			// 국문작업표준서 템플릿 연결
			if (isAltObj != null && !((Boolean) isAltObj))
				opRev.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, SDVBOPUtilities.getTemplateDataset(SDVTypeConstant.PROCESS_SHEET_TEMPLATE_PREF_NAME, item_id + "/000", item_id));

			// Add to parent BOMLine
			if (parentStationObject != null && parentStationObject instanceof TCComponentBOMLine)
			{
				TCComponentBOMLine opLine = ((TCComponentBOMLine) parentStationObject).add(null, opRev, null, false);
				
	            // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
	        	BOPLineUtility.updateLineToOperationAbsOccId(opLine);
				
				//[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
				//opLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");

                SDVBOPUtilities.executeExpandOneLevel();

//				opLine.save();
//				opLine.window().save();
//				opLine.parent().refresh();
				opLine.window().refresh();
			}

//			opRev.save();
//			opItem.save();
		}
		catch (Exception ex)
		{
			//mp.rollBack();
			setErrorMessage(ex.getMessage());
			setExecuteError(ex);
			throw ex;
		}

		//mp.forget();
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
