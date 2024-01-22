package com.symc.plm.me.sdv.operation.ps;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.AISInstructionDatasetCopyUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

public class EngSheetUnProtectOperation extends AbstractSDVActionOperation {

    private Registry registry;

    public EngSheetUnProtectOperation(String actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
        registry = Registry.getRegistry(this);
    }

    public EngSheetUnProtectOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void executeOperation() throws Exception {
        IDataSet dataset = getDataSet();
        if(dataset != null) {
            Collection<IDataMap> dataMaps = dataset.getAllDataMaps();
            if(dataMaps != null) {
                for(IDataMap dataMap : dataMaps) {
                    if(dataMap.containsKey("targetOperationList")) {
                        List<HashMap<String, Object>> opList = dataMap.getTableValue("targetOperationList");
                        if(opList != null) {
                            String viewId = null;
                            if(dataMap.containsKey("viewId")) {
                                viewId = dataMap.getStringValue("viewId");
                            }
                            unProtection(viewId, opList);
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * [SR160324-013] [20160329] taeku.jeong 영문 작업표준서 작성 시 그림부분 수정불가 오류 관련 보완 의뢰건
     * 영문조립작업 표준서의 Workbook, Sheet를 보호해제 하는 Function
     * 영문 조립작업 표준서의 작업 과정에 국문에서 그림이나 Cell을 Copy해서 붙이는경우
     * 기존의 Sheet에 적용되던 Sheet 보호된 것이 그대로 복제되는 경우가 있어서
     * 영문의 경우 Sheet 보호를 해제하는 기능이 추가로 구현 되었음
     * @param viewId
     * @param opList
     */
    private void unProtection(final String viewId, List<HashMap<String, Object>> opList) {
    	
    	System.out.println("viewId = "+viewId);
    	
    	// Preference의 BOP.EngUnProtect.Users 에 등록된 사용자인지 Check 한다. 
    	boolean isContinuAble = false;
    	TCSession tcsession = (TCSession)getSession();
    	String loginUserId = tcsession.getUser().getUid();
    	String[] userIdList = tcsession.getPreferenceService().getStringValues("BOP.EngUnProtect.Users");
    	for (int i = 0;userIdList!=null && i < userIdList.length; i++) {
			if(userIdList[i]!=null && userIdList[i].trim().equalsIgnoreCase(loginUserId.trim())){
				isContinuAble = true;
				break;
			}
		}
    	
    	if(isContinuAble==false){
    		System.out.println("To use this feature, you need to be registered.");
    		return;
    	}
    	
		AISInstructionDatasetCopyUtil aAISInstructionDatasetCopyUtil = new AISInstructionDatasetCopyUtil((TCSession)getSession());
    	
    	for (int i = 0;opList!=null && i < opList.size(); i++) {
            HashMap<String, Object> dataMap = opList.get(i);
            String itemId = (String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
            String itemRevId = (String) dataMap.get(SDVPropertyConstant.ITEM_REVISION_ID);
            
            // 주어진 Item Revision을 찾아서 영문 조립작업표준서 Image가 들어있는 Excel 파일의
            // Work Book & Sheet 보호를 해제 한다.
            System.out.println("Operation = "+itemId+"/"+itemRevId);
            try {
				TCComponentItemRevision itemRevision = CustomUtil.findItemRevision(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, itemId, itemRevId);
            aAISInstructionDatasetCopyUtil.englishAssemblyInstructionSheetUnProtect(itemRevision);
			} catch (Exception e) {
				e.printStackTrace();
			}
            
		}
    }
    

    @Override
    public void endOperation() {

    }

}
