/**
 * 
 */
package com.symc.plm.me.sdv.operation.body;

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
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyProcessActionOperation
 * Class Description :
 * 
 * [SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
 * 
 * 
 * @date 2013. 12. 8.
 * 
 */
public class CreateBodyProcessActionOperation extends AbstractSDVActionOperation {
    private Registry registry;

    public CreateBodyProcessActionOperation(int actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);

        registry = Registry.getRegistry(CreateBodyProcessActionOperation.class);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        IDataSet dataSet = getDataSet();        
	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경  아래 getValue부분에 화면의 key값과 속성값 변경*/
//        Object meco_no = dataSet.getValue("mecoView", SDVPropertyConstant.STATION_MECO_NO);
        Object meco_no = dataSet.getValue(SDVPropertyConstant.MECO_SELECT, SDVPropertyConstant.MECO_REV);
        
        // [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
        if(meco_no!=null){
        	if(meco_no instanceof TCComponentItemRevision){
        		TCComponentItemRevision mecoRevision = isOwnedMECO((TCComponentItemRevision)meco_no);
        		if(mecoRevision==null){
        			throw new Exception("Check MECO owning user");
        		}
        	}
        }

        Object parentLineObject = dataSet.getValue("createBodyProcessView", SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
        String shop_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_SHOP);
        String line_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_LINE);
        String station_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_STATION_CODE);
        String product_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_PRODUCT_CODE);
        String vehicle_code = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_VEHICLE_CODE);
//        String bop_ver = dataSet.getStringValue("createBodyProcessView", "PlanningVer");
        String bop_ver = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_BOP_VERSION);
        String kor_name = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.ITEM_OBJECT_NAME);
        String eng_name = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_ENG_NAME);
        Object isAltObj = dataSet.getValue("createBodyProcessView", SDVPropertyConstant.STATION_IS_ALTBOP);
        String altPrefix = dataSet.getStringValue("createBodyProcessView", SDVPropertyConstant.STATION_ALT_PREFIX);

        TCSession session = CustomUtil.getTCSession();
        Markpoint mp = new Markpoint(session);
        try {
            if (isAltObj != null && isAltObj.toString().toUpperCase().equals("FALSE") && (meco_no == null || meco_no.toString().trim().length() == 0)) {
                throw new NullPointerException(SDVPropertyConstant.STATION_MECO_NO + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (shop_code == null || shop_code.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.STATION_SHOP + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (line_code == null || line_code.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.STATION_LINE + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (station_code == null || station_code.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.STATION_STATION_CODE + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (bop_ver == null || bop_ver.trim().length() == 0) {
                throw new NullPointerException("PlanningVer" + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
                // throw new NullPointerException(SDVPropertyConstant.STATION_BOP_VERSION + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (kor_name == null || kor_name.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.ITEM_OBJECT_NAME + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (eng_name == null || eng_name.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.STATION_ENG_NAME + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            String item_id = (((Boolean) isAltObj) ? altPrefix + "-" : "") + SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shop_code + "-" + line_code + "-" + station_code + "-" + product_code + "-" + bop_ver;

            TCComponentItem stationItem = SYMTcUtil.createItem(session, item_id, kor_name == null ? "" : kor_name, "", SDVTypeConstant.BOP_PROCESS_STATION_ITEM, SDVPropertyConstant.ITEM_REV_ID_ROOT);
            TCComponentItemRevision stationRev = stationItem.getLatestItemRevision();

            if (meco_no != null) {
                stationRev.setReferenceProperty(SDVPropertyConstant.STATION_MECO_NO, (TCComponent) meco_no);

                // MECO Solution에 연결
                ((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_SOLUTION_ITEM, stationRev);
            }

            if (shop_code != null && shop_code.trim().length() > 0)
                stationRev.setProperty(SDVPropertyConstant.STATION_SHOP, shop_code);

            if (line_code != null && line_code.trim().length() > 0)
                stationRev.setProperty(SDVPropertyConstant.STATION_LINE, line_code);

            if (station_code != null && station_code.trim().length() > 0)
                stationRev.setProperty(SDVPropertyConstant.STATION_STATION_CODE, station_code);

            // ------------ BMIDE에서 bop_ver 속성 정의 없음-------
            // if (bop_ver != null && bop_ver.trim().length() > 0)
            // stationRev.setProperty(SDVPropertyConstant.STATION_BOP_VERSION, bop_ver);

            if (kor_name != null && kor_name.trim().length() > 0)
                stationRev.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, kor_name);

            if (eng_name != null && eng_name.trim().length() > 0)
                stationRev.setProperty(SDVPropertyConstant.STATION_ENG_NAME, eng_name);

            if (vehicle_code != null && vehicle_code.trim().length() > 0)
                stationRev.setProperty(SDVPropertyConstant.STATION_VEHICLE_CODE, vehicle_code);
            
            if (product_code != null && product_code.trim().length() > 0)
                stationRev.setProperty(SDVPropertyConstant.STATION_PRODUCT_CODE, product_code);

            stationRev.setProperty(SDVPropertyConstant.STATION_PROCESS_TYPE, ((TCComponentBOPLine) parentLineObject).getItemRevision().getStringProperty(SDVPropertyConstant.LINE_REV_PROCESS_TYPE));

            if (((Boolean) isAltObj)) {
                stationRev.setLogicalProperty(SDVPropertyConstant.STATION_IS_ALTBOP, true);
                stationRev.setProperty(SDVPropertyConstant.STATION_ALT_PREFIX, altPrefix);
            }

            // Add to parent BOMLine
            if (parentLineObject != null && parentLineObject instanceof TCComponentBOMLine) {
                TCComponentBOMLine addLine = ((TCComponentBOMLine) parentLineObject).add(null, stationRev, null, false);
                
                // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
            	BOPLineUtility.updateLineToOperationAbsOccId(addLine);
                
				//[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
                //addLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");

                SDVBOPUtilities.executeExpandOneLevel();

                addLine.window().refresh();
            }

        } catch (Exception ex) {
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
