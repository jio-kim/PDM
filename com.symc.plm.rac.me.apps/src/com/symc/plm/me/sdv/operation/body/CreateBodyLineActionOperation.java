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
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyLineActionOperation
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * [SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
 * 
 * @date 2013. 12. 9.
 * 
 */
public class CreateBodyLineActionOperation extends AbstractSDVActionOperation {
    private Registry registry;

    /**
     * @param actionId
     * @param ownerId
     * @param dataset
     */
    public CreateBodyLineActionOperation(int actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);

        registry = Registry.getRegistry(CreateBodyLineActionOperation.class);
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

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        IDataSet dataSet = getDataSet();
	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경  아래 getValue부분에 화면의 key값과 속성값 변경 */
//        Object meco_no = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
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

        Object parentShopObject = dataSet.getValue("CreateBodyLineView", SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
        String shop_code = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_SHOP_CODE);
        String line_code = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_CODE);
        String kor_name = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.ITEM_OBJECT_NAME);
        String eng_name = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_ENG_NAME);
        String jph = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_JPH);
        String allowance = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_ALLOWANCE);
        Object isAltObj = dataSet.getValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_IS_ALTBOP);
        String altPrefix = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_ALT_PREFIX);
        String vehicle_code = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_VEHICLE_CODE);
        String product_code = dataSet.getStringValue("CreateBodyLineView", SDVPropertyConstant.LINE_REV_PRODUCT_CODE);
        String bop_ver = dataSet.getStringValue("CreateBodyLineView", "PlanningVer");

        TCSession session = CustomUtil.getTCSession();
        Markpoint mp = new Markpoint(session);

        try {
            
            if (shop_code == null || shop_code.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.LINE_REV_SHOP_CODE + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }
            
            if (line_code == null || line_code.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.LINE_REV_CODE + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (kor_name == null || kor_name.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.ITEM_OBJECT_NAME + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (eng_name == null || eng_name.trim().length() == 0) {
                throw new NullPointerException(SDVPropertyConstant.LINE_REV_ENG_NAME + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

//            if (jph == null || jph.trim().length() == 0) {
//                throw new NullPointerException(SDVPropertyConstant.LINE_REV_JPH + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
//            }
            
            if (bop_ver == null || bop_ver.trim().length() == 0) {
                throw new NullPointerException("PlanningVer" + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }

            if (isAltObj != null && isAltObj.toString().toUpperCase().equals("FALSE") && (meco_no == null || meco_no.toString().trim().length() == 0)) {
                throw new NullPointerException(SDVPropertyConstant.SHOP_REV_MECO_NO + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
            }
            
            String item_id = (((Boolean) isAltObj) ? altPrefix + "-" : "") + SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shop_code + "-" + line_code + "-" + product_code + "-" + bop_ver;
            
            TCComponentItem lineItem = SYMTcUtil.createItem(session, item_id, kor_name == null ? "" : kor_name, "", SDVTypeConstant.BOP_PROCESS_LINE_ITEM, SDVPropertyConstant.ITEM_REV_ID_ROOT);
            TCComponentItemRevision lineRev = lineItem.getLatestItemRevision();
            
            if (shop_code != null && shop_code.trim().length() > 0)
                lineRev.setProperty(SDVPropertyConstant.LINE_REV_SHOP_CODE, shop_code);
            
            if (line_code != null && line_code.trim().length() > 0)
                lineRev.setProperty(SDVPropertyConstant.LINE_REV_CODE, line_code);
            
            if (kor_name != null && kor_name.trim().length() > 0)
                lineRev.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, kor_name);
            
            if (eng_name != null && eng_name.trim().length() > 0)
                lineRev.setProperty(SDVPropertyConstant.LINE_REV_ENG_NAME, eng_name);

            //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
            if (jph != null && jph.trim().length() > 0)
                //lineRev.setIntProperty(SDVPropertyConstant.LINE_REV_JPH, Integer.valueOf(jph));
                lineRev.setDoubleProperty(SDVPropertyConstant.LINE_REV_JPH, Double.parseDouble(jph));
                
            
            if (allowance != null && allowance.trim().length() > 0)
                lineRev.setDoubleProperty(SDVPropertyConstant.LINE_REV_ALLOWANCE, Double.valueOf(allowance));
            
            if (vehicle_code != null && vehicle_code.trim().length() > 0)
                lineRev.setProperty(SDVPropertyConstant.LINE_REV_VEHICLE_CODE, vehicle_code);
            
            if (product_code != null && product_code.trim().length() > 0)
                lineRev.setProperty(SDVPropertyConstant.LINE_REV_PRODUCT_CODE, product_code);

            lineRev.setProperty(SDVPropertyConstant.LINE_REV_PROCESS_TYPE, ((TCComponentBOMLine) parentShopObject).getItemRevision().getStringProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE));

            if (((Boolean) isAltObj))
            {
                lineRev.setLogicalProperty(SDVPropertyConstant.LINE_REV_IS_ALTBOP, true);
                lineRev.setProperty(SDVPropertyConstant.LINE_REV_ALT_PREFIX, altPrefix);
            }

            if (meco_no != null)
            {
                lineRev.setReferenceProperty(SDVPropertyConstant.LINE_REV_MECO_NO, (TCComponent) meco_no);

                // MECO Solution에 연결
                ((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_SOLUTION_ITEM, lineRev);
            }

            // Add to parent BOMLine
            if (parentShopObject != null && parentShopObject instanceof TCComponentBOMLine)
            {
                TCComponentBOMLine opLine = ((TCComponentBOMLine) parentShopObject).add(null, lineRev, null, false);
                
                // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
            	BOPLineUtility.updateLineToOperationAbsOccId(opLine);
                
                //[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
                //opLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
                opLine.window().refresh();

                SDVBOPUtilities.executeExpandOneLevel();
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
