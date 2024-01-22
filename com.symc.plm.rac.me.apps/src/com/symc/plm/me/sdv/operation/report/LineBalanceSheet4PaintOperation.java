package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

/**
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 *
 */
public class LineBalanceSheet4PaintOperation extends SimpleSDVExcelOperation {

    private Map<String, Integer> propertyMap = new HashMap<String, Integer>();
    private double shopJPH = 0;
    private double lineJPH = 0;

    private Registry registy;

    @Override
    public void executeOperation() throws Exception {
        try {
            registy = Registry.getRegistry(this);
            shopJPH = 0;
            lineJPH = 0;
            IDataSet dataSet = getData();

            if(dataSet != null) {
                String defaultFileName = registy.getString("exportLineBalancingPaint.FileName", "LineBalancingListPaint") + "_" + SDVStringUtiles.dateToString(new Date(), "yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }
        } catch(Exception e) {
            setExecuteError(e);
            // MessageBox에 보여줄 메시지
            // 구현하지 않으면 default 메시지를 보여준다.
            //setErrorMessage("");
        }
    }

    @Override
    protected IDataSet getData() throws TCException {
        propertyMap.put(SDVPropertyConstant.BL_ITEM_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_OBJECT_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.ACTIVITY_WORK_TIME, SDVPropertyConstant.TYPE_DOUBLE);

        String revisionRule = "";
        String variantRule = "";
        String shopCode = "";
        String productCode = "";

        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String,Object>>();

        InterfaceAIFComponent component =  AIFUtility.getCurrentApplication().getTargetComponent();
        if(component != null && component instanceof TCComponentBOPLine) {
            TCComponentBOPLine shopComp = (TCComponentBOPLine) component;
            if(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM.equals(shopComp.getItem().getType())) {
                // Shop JPH
                //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
                String jph = shopComp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_JPH);
                if(jph != null && !"".equals(jph)) {
                    shopJPH = Double.parseDouble(jph);
                }

                // Shop Code
                shopCode =  shopComp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);

                // ProductCode
                productCode = shopComp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);

                // Revision Rule
                TCComponentBOPWindow window = (TCComponentBOPWindow) shopComp.window();
                TCComponentRevisionRule revisionRuleComp = window.getRevisionRule();
                if(revisionRuleComp != null) {
                    revisionRule = revisionRuleComp.toStringWithEffectivity();
                }

                // Variant
                variantRule = SDVBOPUtilities.getBOMConfiguredVariantSetToString(window);
            }

            dataList = getChildrenList(dataList, (TCComponentBOPLine) component);
        }

        IDataSet dataSet = convertToDataSet("operationList", dataList);
        IDataMap dataMap = new RawDataMap();

        // Shop JPH
        //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
        dataMap.put(SDVPropertyConstant.SHOP_REV_JPH, shopJPH, IData.STRING_FIELD);

        // Shop Code
        dataMap.put(SDVPropertyConstant.SHOP_REV_SHOP_CODE, shopCode, IData.STRING_FIELD);

        // Product Code
        dataMap.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, productCode, IData.STRING_FIELD);

        // Revision Rule
        dataMap.put("RevisionRule", revisionRule, IData.STRING_FIELD);

        // Variant
        dataMap.put("VariantRule", variantRule, IData.STRING_FIELD);

        dataSet.addDataMap("AdditionalInfo", dataMap);

        return dataSet;
    }

    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        dataMap.put(dataName, dataList, IData.TABLE_FIELD);
        dataSet.addDataMap(dataName, dataMap);

        return dataSet;
    }

    /**
     * Shop 하위의 자식 Component들의 정보를 가져온다.
     *
     * @method getChildrenList
     * @date 2013. 10. 28.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
     private List<HashMap<String, Object>> getChildrenList(List<HashMap<String, Object>> dataList, TCComponentBOPLine parentLine) throws TCException {
    	 AIFComponentContext[] context = parentLine.getChildren();
        if(context.length > 0) {
            for(int i = 0; i < context.length; i++) {
                if(context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine)context[i].getComponent();
                    if(SDVTypeConstant.BOP_PROCESS_STATION_ITEM.equals(childLine.getItem().getType())) {
                        HashMap<String, Object> dataMap = convertComponent(childLine);
                        ////////////////////////////////////////////////////////////////////////////////////////////////
                        // BOP Station 에 JHP 속성 추가 로 인한 로직 변경
                        // 현재는 X100 만 해당 되나 모든 차종으로 확대 가능성 있음
                        // 구분 기호는 "2Tone" 으로 비교 하여 구분
                        if(childLine.getStringProperty("object_string").toUpperCase().contains("2Tone".toUpperCase())) {
                        		lineJPH = 0;
                        		String jph = childLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_JPH);
//                        		String jph = String.valueOf(i);
                        		if(jph != null && !"".equals(jph)) {
                                    lineJPH = Double.parseDouble(jph);
                                }
                        }
                        ////////////////////////////////////////////////////////////////////////////////////////////////
                        dataMap = getAdditionalProperty(childLine, dataMap);
                        if((Integer)dataMap.get(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT) > 0) {
                            dataList.add(dataMap);
                        }
                    } else {
                        if(SDVTypeConstant.BOP_PROCESS_LINE_ITEM.equals(childLine.getItem().getType())) {
                        	lineJPH = 0;
                            String jph = childLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_JPH);
                            //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
                            if(jph != null && !"".equals(jph)) {
                                lineJPH = Double.parseDouble(jph);
                            }
                        }

                        getChildrenList(dataList, (TCComponentBOPLine)context[i].getComponent());
                    }
                }
            }
        }

        return dataList;
    }

    /**
     * 기본 속성 외에 계산 또는 조건에 의해 나오는 값은 별도로 저장한다.
     *
     * @method getAdditionalProperty
     * @date 2013. 10. 28.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getAdditionalProperty(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        int workerCountSum = 0;
        double workTimeSum = 0;
        AIFComponentContext[] context = operation.getChildren();
          if(context.length > 0) {
        	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
              HashMap<String, TCComponentBOPLine> sortHashmap = new HashMap<String, TCComponentBOPLine>();
              for(int i = 0; i < context.length; i++) {
            	  if(context[i].getComponent() instanceof TCComponentBOPLine) {
            		  TCComponentBOPLine childLine = (TCComponentBOPLine)context[i].getComponent();
                      if(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM.equals(childLine.getItem().getType())) {
                    	  sortHashmap.put(childLine.getItem().getStringProperty("item_id"), childLine);
                      }
            	  }
              }
              TreeMap<String,TCComponentBOPLine> tm = new TreeMap<String,TCComponentBOPLine>(sortHashmap);
              Set<String> keyset = sortHashmap.keySet();
              Iterator<String> keyiterator = tm.keySet( ).iterator( ); //키값 오름차순 정렬 // 
              String k ="";  
              while(keyiterator.hasNext()) { 
            	  k = (String)keyiterator.next(); 
            	  break;
              }

              TCComponentBOPLine sortBomLine = sortHashmap.get(k);
              String strWorkerCnt = sortBomLine.getItemRevision().getProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);
      		int workerCnt = 0;
      		if(strWorkerCnt != null && !"".equals(strWorkerCnt)) {
      			workerCnt = Integer.parseInt(strWorkerCnt);
      		}
      		
      		if(workerCnt > 0) workerCountSum += workerCnt;
      		
      		double workTime = getOperationWorkTime(sortBomLine);
      		if(workTime > 0) workTimeSum += workTime;
          	///////////////////////////////////////////////////////////////////////////////////////////////////////////// 
        	  
        	  
//            for(int i = 0; i < context.length; i++) {
//            	
//                if(context[i].getComponent() instanceof TCComponentBOPLine) {
//                    TCComponentBOPLine childLine = (TCComponentBOPLine)context[i].getComponent();
//                    if(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM.equals(childLine.getItem().getType())) {
//                    	// 재 수정 해야함
//                    	String opItemId = childLine.getItem().getStringProperty("item_id");
//                    	String[] splitOpItemId = opItemId.split("-");
//                    	if( splitOpItemId[splitOpItemId.length - 1].equals("00")) {
//                    		
//                    		String strWorkerCnt = childLine.getItemRevision().getProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);
//                    		int workerCnt = 0;
//                    		if(strWorkerCnt != null && !"".equals(strWorkerCnt)) {
//                    			workerCnt = Integer.parseInt(strWorkerCnt);
//                    		}
//                    		
//                    		if(workerCnt > 0) workerCountSum += workerCnt;
//                    		
//                    		double workTime = getOperationWorkTime(childLine);
//                    		if(workTime > 0) workTimeSum += workTime;
//                    	}
//                    } // if 문 끝
//                } 
//            }
        }
     
        dataMap.put(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT, workerCountSum);
        dataMap.put(SDVPropertyConstant.ACTIVITY_WORK_TIME, workTimeSum);

        // Line의 JPH 값이 없으면 Shop의 JPH 값 사용
        if(lineJPH > 0) {
            dataMap.put(SDVPropertyConstant.LINE_REV_JPH, lineJPH);
        } else {
            dataMap.put(SDVPropertyConstant.LINE_REV_JPH, shopJPH);
        }

        return dataMap;
    }

    /**
     * 공법 하위의 Activity들의 작업시간을 합산하여 반환한다.
     *
     * @method getOperationWorkTime
     * @date 2013. 10. 28.
     * @param
     * @return double
     * @exception
     * @throws
     * @see
     */
    private double getOperationWorkTime(TCComponentBOPLine operation) throws TCException {
        double workTime = 0;

        TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operation;
        TCComponent root = bvrOperation.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);
        if(root != null) {
            if(root instanceof TCComponentCfgActivityLine) {
                TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                if(children != null) {
                    for(TCComponent child : children) {
                        double unitTime = child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
                        double frequency =  child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
                        workTime += (unitTime * frequency);
                    }
                }
            }
        }

        return workTime;
    }

    /**
     * Component의 속성을 가져와 HashMap으로 저장한다.
     *
     * @method convertComponent
     * @date 2013. 10. 28.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> convertComponent(TCComponentBOPLine component) throws TCException {
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        Iterator<String> iterator = propertyMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            int value = (int) propertyMap.get(key);

            switch(value) {
                case 0x01 : dataMap.put(key, component.getProperty(key)); break;
                case 0x02 : dataMap.put(key, component.getIntProperty(key)); break;
                case 0x03 : dataMap.put(key, component.getDoubleProperty(key)); break;
                case 0x04 : dataMap.put(key, component.getProperty(key)); break;
                case 0x05 : dataMap.put(key, component.getReferenceProperty(key)); break;
                default : break;
            }
        }

        return dataMap;
    }

}
