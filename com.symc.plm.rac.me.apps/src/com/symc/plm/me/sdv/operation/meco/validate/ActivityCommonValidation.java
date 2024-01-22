/**
 * 
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;

/**
 * Class Name : ActivityCommonValidation
 * Class Description :
 * 
 * @date 2014. 1. 28.
 *       Activity 공통 Validation
 */
public class ActivityCommonValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.operation.meco.validate.OperationValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        // Validation 항목별 에러 메세지 모음
        StringBuilder resultBuilders = new StringBuilder();

        AIFComponentContext[] activityParentComp = target.getRelated(SDVPropertyConstant.BL_ACTIVITY_LINES);
        TCComponentCfgActivityLine rootActLine = null;
        TCComponentMEActivity rootActivity = null;
        TCComponent[] childComps = null;
        
        if(activityParentComp.length > 0) {
        	
        	rootActLine =(TCComponentCfgActivityLine) activityParentComp[0].getComponent();
        	
        	if(null != rootActLine){
        		
        		rootActivity = (TCComponentMEActivity) rootActLine.getUnderlyingComponent();
        		
        		if(null != rootActivity) {
        			
        			childComps = ActivityUtils.getSortedActivityChildren(rootActivity);
        			
        			
        			if(null != childComps) {
        				
        		        for (TCComponent childComp : childComps) {
        		            TCComponentMEActivity childActivity = (TCComponentMEActivity) childComp;
        		            childActivity.refresh();
        		            String errorMsg = ""; // 에러 메세지
        		            String systemCode = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
        		            String category = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);

        		            /**
        		             * 1. Activity Category(NA) 에 값이 입력 안될때 체크함
        		             */

        		            // (체크) Activity Category("NA") 이면 오류
        		            if (category.equals("NA")) {
        		                errorMsg = getMessage(ERROR_TYPE_ACTIVITY_CATEGOLRY_EMPTY, systemCode);
        		                resultBuilders.append(errorMsg);
        		            }
        		        }
        			}
        		}
        	}
        }
        


        if (resultBuilders.length() > 0)
            result = resultBuilders.toString();

    }

}
