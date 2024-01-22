/**
 *
 */
package com.symc.plm.me.sdv.operation.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMECfgLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * [SR141219-020][20150108] shcho, Open with Time 창에서의 Activity 작업순서 불일치 및 순서 편집 불가 대응 신규 화면 추가
 */
public class ManageActivityActionOperation extends AbstractSDVActionOperation {

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public ManageActivityActionOperation(int actionId, String ownerId, IDataSet dataSet) {
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void executeOperation() throws Exception {
        // MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "Action Operation OK.", registry.getString("Inform.NAME"), MessageBox.INFORMATION);

        IDataSet dataSet = getDataSet();
        TCComponentBOMLine operationLine = (TCComponentBOMLine) dataSet.getValue("activityView", "OperationLine");
        Object newDataMap = dataSet.getValue("activityView", "newTableList");
        Object oldDataMap = dataSet.getValue("activityView", "oldTableList");
        /**
         * [SR190104-050] 윤순식 부장 요청 Bop Activity 편집 후 저장시 걸리는 시간 단축 요청
         * 기존 로직 : createActivities -> resaveActivities 로 변경
         * 기존 Activity 편집 후 저장 시에는 공법 하위의 모든 Activity들을 삭제 재생성 한뒤에 속성정보를 저장 했으나
         * 불필요한 로직으로 인해 소요되는 시간이 많아 로직 변경
         * 편집 전후의 Activity들의 개수를 비교 하여 같으면 속성정보만 변경 
         * 다를 경우에는 차이나는 개수만큼 삭제 및 추가 생성을 하여 Activity 속성 정보 저장하여 소요 시간 단축
         */
        List<HashMap<String, Object>> tableList = ((List<HashMap<String, Object>>) newDataMap);
        List<HashMap<String, Object>> oldTableList = ((List<HashMap<String, Object>>) oldDataMap);
        

        TCSession session = CustomUtil.getTCSession();
        Markpoint mp = new Markpoint(session);
        try {
        	
        	/**
             * [SR190104-050] 윤순식 부장 요청 Bop Activity 편집 후 저장시 걸리는 시간 단축 요청
             * 기존 로직 : createActivities -> resaveActivities 로 변경
             * 기존 Activity 편집 후 저장 시에는 공법 하위의 모든 Activity들을 삭제 재생성 한뒤에 속성정보를 저장 했으나
             * 불필요한 로직으로 인해 소요되는 시간이 많아 로직 변경
             * 편집 전후의 Activity들의 개수를 비교 하여 같으면 속성정보만 변경 
             * 다를 경우에는 차이나는 개수만큼 삭제 및 추가 생성을 하여 Activity 속성 정보 저장하여 소요 시간 단축
             */
//            createActivities( tableList, operationLine);
            resaveActivities( oldTableList, tableList, operationLine);
        } catch (Exception ex) {
            mp.rollBack();
            setErrorMessage(ex.getMessage());
            setExecuteError(ex);
            throw ex;
        }

        mp.forget();
    }

    /**
     * <현재 사용중> Activity 생성 (BOMWindow 생성하지않고 ActivityLine 구성)
     * 
     * @method createActivities
     * @date 2014. 1. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createActivities(List<HashMap<String, Object>> tableList, TCComponentBOMLine operationLine) throws Exception, TCException {
    	long startTime = System.currentTimeMillis();
    	
    	
        // Child Activity Remove
        TCComponentCfgActivityLine[] childActivityList = getChildActivityList(operationLine);
        for (TCComponentCfgActivityLine meActivityLine : childActivityList) {
            TCComponentMECfgLine parentLine = meActivityLine.parent();
            ActivityUtils.removeActivity(meActivityLine);
            parentLine.save();
        }
        TCComponent root = operationLine.getReferenceProperty("bl_me_activity_lines");
        // activity refresh
        refreshBOMLine(root);
        refreshBOMLine(operationLine);

        // Activity 생성
        TCComponent[] afterTCComponents = null;
        if (tableList != null && tableList.size() > 0) {
            TCComponent[] rootActivityComponent = new TCComponent[tableList.size()];
            TCComponent[] lastChildActivityComponent = new TCComponent[tableList.size()];
            for (int i = 0; i < rootActivityComponent.length; i++) {
                rootActivityComponent[i] = root;
            }
            afterTCComponents = ActivityUtils.createActivities(rootActivityComponent, lastChildActivityComponent, "Activity");
            // 생성 성공시 속성 업데이트
            if (afterTCComponents != null && afterTCComponents.length > 0) {
                for (int i = 0; i < tableList.size(); i++) {
                    HashMap<String, Object> tableMap = tableList.get(i);
                    TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) afterTCComponents[i];
                    TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();

                    for (String key : tableMap.keySet()) {
                        // 비 저장 항목 : 건너뛴다.
                        if (key.equals("seq")) {
                            continue;
                        }
                        // double 저장
                        if (key.equals(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)) {
                            double timeSystemUnitTime = 0.0;
                            if (!StringUtils.isEmpty((String) tableMap.get(key))) {
                                timeSystemUnitTime = Double.parseDouble((String) tableMap.get(key));
                            }
                            activity.setDoubleProperty(key, timeSystemUnitTime);
                        }

                        // LOV Value 저장
                        else if (key.equals(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY)) {
                            String value = tableMap.get(key).toString();
                            if (value != null) {
                                activity.setStringProperty(key, getCategoryLovValue(value));
                            }
                        }

                        // 배열 Value 저장
                        else if (key.equals(SDVPropertyConstant.ACTIVITY_WORKER)) {
                            String value = tableMap.get(key).toString();
                            if (value != null) {
                                String[] arrValue = new String[] { value };
                                activity.getTCProperty(key).setStringValueArray(arrValue);
                            }
                        }

                        // String 저장
                        else {
                            String value = tableMap.get(key).toString();
                            if (value != null) {
                                activity.setStringProperty(key, value);
                            }
                        }
                    }

                    // ITK에서 object_name 이 변경되면 자동으로 m7_ENG_NAME을 리셋한다.
                    // 때문에 가장 마지막에 m7_ENG_NAME을 다시 셋팅하여 전체 m7_ENG_NAME이 사라지는 일이 없도록 한다.
                    String engName = (String) tableMap.get(SDVPropertyConstant.ACTIVITY_ENG_NAME);
                    if (engName != null) {
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).setStringValue(engName);
                    }

                    activity.save();
                    root.save();
                }
                // 생성 실패시
            } else {
                for (int i = 0; i < childActivityList.length; i++) {
                    if (root instanceof TCComponentMECfgLine) {
                        ActivityUtils.addActivity((TCComponentMECfgLine) root, (TCComponentMEActivity) childActivityList[i].getUnderlyingComponent());
                    } else if (root instanceof TCComponentMEActivity) {
                        ActivityUtils.addActivity((TCComponentMEActivity) root, (TCComponentMEActivity) childActivityList[i].getUnderlyingComponent());
                    }
                }
                root.save();
            }
        }
        long endTime = System.currentTimeMillis();
        
        System.out.println("소요시간 : " + (endTime - startTime ) + "ms" ) ;
    }
    
    
    
    /**
     * [SR190104-050] 윤순식 부장 요청 Bop Activity 편집 후 저장시 걸리는 시간 단축 요청
     * 기존 로직 : createActivities -> resaveActivities 로 변경
     * 기존 Activity 편집 후 저장 시에는 공법 하위의 모든 Activity들을 삭제 재생성 한뒤에 속성정보를 저장 했으나
     * 불필요한 로직으로 인해 소요되는 시간이 많아 로직 변경
     * 편집 전후의 Activity들의 개수를 비교 하여 같으면 속성정보만 변경 
     * 다를 경우에는 차이나는 개수만큼 삭제 및 추가 생성을 하여 Activity 속성 정보 저장하여 소요 시간 단축
     * 
     * @method createActivities
     * @date 2014. 1. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void resaveActivities(List<HashMap<String, Object>> oldTableList, List<HashMap<String, Object>> newTableList, TCComponentBOMLine operationLine) throws Exception, TCException {
    	
    	long startTime = System.currentTimeMillis();
    	TCComponentCfgActivityLine[] childActivityList = getChildActivityList(operationLine);
    	 TCComponent root = operationLine.getReferenceProperty("bl_me_activity_lines");
         // activity refresh
         refreshBOMLine(root);
         refreshBOMLine(operationLine);
    	if( oldTableList.size() ==  newTableList.size() ) {
    		long equalQuantityStartTime = System.currentTimeMillis();
    		saveActivitiesProperties( root, newTableList, childActivityList);
    		 long equalQuantityEndTime = System.currentTimeMillis();
    	        System.out.println("같은 개수 소요시간 : " + (equalQuantityEndTime - equalQuantityStartTime ) + "ms" ) ;
    		
    	} else {
    		long notEqualQuantityStartTime = System.currentTimeMillis();
    		TCComponent[] afterTCComponents = null;
    		int compareSize = ( newTableList.size() - oldTableList.size() );
    		
    		if( compareSize > 0 ) {
    			// Activity가 추가 되어 개수 만큼 생성
    			
    			TCComponent[] rootActivityComponent = new TCComponent[compareSize];
                TCComponent[] lastChildActivityComponent = new TCComponent[compareSize];
                for (int i = 0; i < rootActivityComponent.length; i++) {
                    rootActivityComponent[i] = root;
                }
    			TCComponent[] activities =  ActivityUtils.createActivities(rootActivityComponent, lastChildActivityComponent, "Activity");

    			childActivityList = getChildActivityList(operationLine);
                refreshBOMLine(root);
                refreshBOMLine(operationLine);
                saveActivitiesProperties( root, newTableList, childActivityList);
                long notEqualQuantityEndTime = System.currentTimeMillis();
    	        System.out.println(" 추가 생성 소요시간 : " + (notEqualQuantityEndTime - notEqualQuantityStartTime ) + "ms" ) ;
    			
    		} else {
    			// Activity가 삭제 되어 개수만큼 삭제
    			   compareSize = - compareSize;
    			
    			for ( int i = childActivityList.length - 1; i >=  (childActivityList.length - compareSize); i -- ) {
    	            TCComponentMECfgLine parentLine = childActivityList[i].parent();
    	            ActivityUtils.removeActivity(childActivityList[i]);
    	            parentLine.save();
    	        }
    			childActivityList = getChildActivityList(operationLine);
    			refreshBOMLine(root);
    			refreshBOMLine(root);
    	        refreshBOMLine(operationLine);
    			saveActivitiesProperties( root, newTableList, childActivityList);
    			long notEqualQuantityEndTime = System.currentTimeMillis();
    	        System.out.println(" 삭제 소요시간 : " + (notEqualQuantityEndTime - notEqualQuantityStartTime ) + "ms" ) ;
    		}
    		
    		
    		
    	}
       
    	System.gc();
        long endTime = System.currentTimeMillis();
        System.out.println("총 소요시간 : " + (endTime - startTime ) + "ms" ) ;
    }
    
    
    public void saveActivitiesProperties ( TCComponent root ,List<HashMap<String, Object>> newTableList, TCComponentCfgActivityLine[] childActivityList ) throws Exception, TCException {
    	boolean chageFlag = false;
    	 for (int i = 0; i < newTableList.size(); i++) {
             HashMap<String, Object> tableMap = newTableList.get(i);
             TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) childActivityList[i];
             TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();

             for (String key : tableMap.keySet()) {
                 // 비 저장 항목 : 건너뛴다.
                 if (key.equals("seq")) {
                     continue;
                 }
                 // double 저장
                 if (key.equals(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)) {
                     double timeSystemUnitTime = 0.0;
                     if (!StringUtils.isEmpty((String) tableMap.get(key))) {
                         timeSystemUnitTime = Double.parseDouble((String) tableMap.get(key));
                     } 
                     
                     double activyValue = activity.getDoubleProperty(key);
                     if( activyValue != timeSystemUnitTime) {
                    	 chageFlag = true;
                    	 activity.setDoubleProperty(key, timeSystemUnitTime);
                     }
                 }

                 // LOV Value 저장
                 else if (key.equals(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY)) {
                     String value = tableMap.get(key).toString();
                     if (value != null) {
                    	 String activyValue = activity.getStringProperty(key);
                    	 if( !activyValue.equals(getCategoryLovValue(value))) {
                    		 chageFlag = true;
                    		 activity.setStringProperty(key, getCategoryLovValue(value));
                    	 }
                     }
                     
                 }

                 // 배열 Value 저장
                 else if (key.equals(SDVPropertyConstant.ACTIVITY_WORKER)) {
                     String value = tableMap.get(key).toString();
                     if (value != null) {
                         String[] arrValue = value.split(",");
                         String[] actitypeArray =  activity.getTCProperty(key).getStringValueArray();
                         
                         try {
                        	 if( actitypeArray.length == 0 ) {
                        		  actitypeArray =  new String[] { "" };
                        		 
                        	 } else {
                        		 for( int j = 0; j < actitypeArray.length; j ++ ) {
                        			 if( actitypeArray[j] == null ) {
                            			 actitypeArray[j] =  "";
                            		 } else {
                            			 actitypeArray[j] = actitypeArray[j].trim();
                            		 }
                        		 }
                        	 }
                        	 
                        	 if( arrValue.length > 1) {
                        		 for( int j = 0; j < arrValue.length; j++ ) {
                        			 arrValue[j] = arrValue[j].trim();
                        		 }
                        	 }
                        	 
                         } catch ( ArrayIndexOutOfBoundsException e  ) {
                        	System.out.println( "에러 항목" + tableMap.get("seq") + "	행수 : " + (i + 1));
                         }
                         
                         if( !Arrays.equals(arrValue, actitypeArray)) {
                        	 chageFlag = true;
                        	 activity.getTCProperty(key).setStringValueArray(arrValue);
                         }
                     }
                 }

                 // String 저장
                 else {
                     String value = tableMap.get(key).toString();
                     if (value != null) {
                    	 String activyValue = activity.getStringProperty(key);
                    	 if( !activyValue.equals(value)) {
                    		 chageFlag = true;
                    		 activity.setStringProperty(key, value);
                    	 }
                     }
                 }
             }

             // ITK에서 object_name 이 변경되면 자동으로 m7_ENG_NAME을 리셋한다.
             // 때문에 가장 마지막에 m7_ENG_NAME을 다시 셋팅하여 전체 m7_ENG_NAME이 사라지는 일이 없도록 한다.
             String engName = (String) tableMap.get(SDVPropertyConstant.ACTIVITY_ENG_NAME);
             if (engName != null) {
            	 
            	 if( !activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).equals(engName)) {
            		 chageFlag = true;
            		 activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).setStringValue(engName);
            	 }
             }
             
//             if( chageFlag ) {
//            	 try {
//            		 
//            		 activity.save();
//            	 } catch(Exception e) {
//            		 e.getStackTrace();
//            	 }
//            	 
//             }
             root.save();
         }
    }

    /**
     * Activity Category를 LOV로 변환한다.
     * 
     * @method getCategoryLovValue
     * @date 2013. 12. 10.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getCategoryLovValue(String category) {
        if ("작업자정미".equals(category)) {
            category = "01";
        } else if ("자동".equals(category)) {
            category = "02";
        } else if ("보조".equals(category)) {
            category = "03";
        } else {
            category = "NA";
        }
        return category;
    }

    private TCComponentCfgActivityLine[] getChildActivityList(TCComponentBOMLine bomLine) throws Exception {
        ArrayList<TCComponentCfgActivityLine> childActivityList = new ArrayList<TCComponentCfgActivityLine>();
        TCComponent root = bomLine.getReferenceProperty("bl_me_activity_lines");
        if (root != null) {
            if (root instanceof TCComponentCfgActivityLine) {
                TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) root);
                for (TCComponent childLine : childLines) {
                    if (childLine instanceof TCComponentCfgActivityLine) {
                        childActivityList.add((TCComponentCfgActivityLine) childLine);
                    }
                }
            }
        }
        return childActivityList.toArray(new TCComponentCfgActivityLine[childActivityList.size()]);
    }

    /**
     * BOMLine refresh
     * 
     * @method refreshBOMLine
     * @date 2013. 12. 12.
     * @param
     * @exception
     * @return void
     * @throws
     * @see
     */
    private void refreshBOMLine(Object tcComponent) throws Exception {
        // Activity Refresh
        if (tcComponent instanceof TCComponentMECfgLine) {
            TCComponentMECfgLine tcComponentMECfgLine = (TCComponentMECfgLine) tcComponent;
            tcComponentMECfgLine.clearCache();
            tcComponentMECfgLine.window().fireChangeEvent();
            tcComponentMECfgLine.refresh();
        } else if (tcComponent instanceof TCComponentCfgAttachmentLine) {
            TCComponentCfgAttachmentLine tcComponentCfgAttachmentLine = (TCComponentCfgAttachmentLine) tcComponent;
            tcComponentCfgAttachmentLine.clearCache();
            tcComponentCfgAttachmentLine.window().fireChangeEvent();
            tcComponentCfgAttachmentLine.refresh();
        }
        // BOMLine Refresh
        else if (tcComponent instanceof TCComponentBOMLine) {
            TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) tcComponent;
            tcComponentBOMLine.clearCache();
            tcComponentBOMLine.refresh();
            tcComponentBOMLine.window().newIrfWhereConfigured(tcComponentBOMLine.getItemRevision());
            tcComponentBOMLine.window().fireComponentChangeEvent();
        }
    }

}
