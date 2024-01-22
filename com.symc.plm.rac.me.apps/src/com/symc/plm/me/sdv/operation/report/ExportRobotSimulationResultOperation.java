package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

public class ExportRobotSimulationResultOperation extends SimpleSDVExcelOperation {

    private Registry registry;

    private String productCode = "";

    @Override
    public void executeOperation() throws Exception {
        try {
            registry = Registry.getRegistry(this);

            IDataSet dataSet = getData();
            if(dataSet != null) {
                String defaultFileName = productCode + "_" + registry.getString("Robot3DSimulation.FileName", "Robot3DSimulation") + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }
        } catch(Exception e) {
            setExecuteError(e);
            // MessageBox에 보여줄 메시지
            // 구현하지 않으면 default 메시지를 보여준다.
            // setErrorMessage("");
        }
    }

    @Override
    protected IDataSet getData() throws Exception {
        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

        InterfaceAIFComponent component =  AIFUtility.getCurrentApplication().getTargetComponent();
        if(component != null && component instanceof TCComponentBOPLine) {
            TCComponentBOPLine plantOPArea = (TCComponentBOPLine) component;
            if(SDVTypeConstant.PLANT_OPAREA_ITEM.equals(plantOPArea.getItem().getType())) {
                HashMap<String, Object> dataMap = new HashMap<String, Object>();

                TCComponentBOPLine station = getParentBOPLine(plantOPArea, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                if(station != null) {
                    TCComponentItemRevision stationItemRevision = station.getItemRevision();

                    // Product Code
                    productCode = stationItemRevision.getProperty(SDVPropertyConstant.STATION_PRODUCT_CODE);

                    // Line, Station Code, Vehicle Code, Product Code
                    dataMap.put(SDVPropertyConstant.STATION_LINE, stationItemRevision.getProperty(SDVPropertyConstant.STATION_LINE));
                    dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, stationItemRevision.getProperty(SDVPropertyConstant.STATION_STATION_CODE));
                    dataMap.put(SDVPropertyConstant.STATION_VEHICLE_CODE, stationItemRevision.getProperty(SDVPropertyConstant.STATION_VEHICLE_CODE));
                    dataMap.put(SDVPropertyConstant.STATION_PRODUCT_CODE, stationItemRevision.getProperty(SDVPropertyConstant.STATION_PRODUCT_CODE));
                }

                // Robot Info
                dataMap = getRobotInfo(plantOPArea, dataMap);

                // Gun Info
                dataMap = getGunInfo(plantOPArea, dataMap);

                dataList.add(dataMap);
            }
        }

        IDataSet dataSet = convertToDataSet("operationList", dataList);

        return dataSet;
    }

    /**
     * Robot Info
     *
     * @method getRobotInfo
     * @date 2014. 2. 5.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getRobotInfo(TCComponentBOPLine plantOPArea, HashMap<String, Object> dataMap) throws TCException {
        // Robot Name
        String plantOPAreaId = plantOPArea.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String[] plantOPAreaId_array = plantOPAreaId.split("-");
        String robotName;

        if(plantOPAreaId_array.length > 3) {
            robotName = plantOPAreaId_array[2] + "-" + plantOPAreaId_array[3];
        } else {
            robotName = plantOPAreaId_array[0];
        }
        dataMap.put("robotName", robotName);

        return dataMap;
    }

    /**
     * Gun Info
     *
     * @method getGunInfo
     * @date 2013. 11. 14.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getGunInfo(TCComponentBOPLine plantOPArea, HashMap<String, Object> dataMap) throws TCException {
        List<String> gunList = new ArrayList<String>();

        if(plantOPArea.getChildrenCount() > 0) {
            AIFComponentContext[] plantOPArea_children = plantOPArea.getChildren();
            for(int i = 0; i < plantOPArea_children.length; i++) {
                if(plantOPArea_children[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine child = (TCComponentBOPLine) plantOPArea_children[i].getComponent();
                    if(SDVTypeConstant.BOP_PROCESS_GUN_ITEM.equals(child.getItem().getType())) {
                        // Gun No
                        String gunNo = child.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                        if(gunNo.lastIndexOf("-") != -1) {
                            gunNo = gunNo.substring(gunNo.lastIndexOf("-") + 1);
                        }
                        gunList.add(gunNo);
                    }
                }
            }
        }
        dataMap.put("gunList", gunList);

        return dataMap;
    }

    /**
     * 부모 TCComponentBOPLine return
     *
     * @method getParentBOPLine
     * @date 2014. 2. 5.
     * @param
     * @return TCComponentBOPLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOPLine getParentBOPLine(TCComponentBOPLine bopLine, String itemType) throws TCException {
        TCComponentBOPLine parentBOPLine = null;

        if(bopLine.parent() != null) {
            parentBOPLine = (TCComponentBOPLine) bopLine.parent();
            if(!parentBOPLine.getItem().getType().equals(itemType)) {
                return getParentBOPLine(parentBOPLine, itemType);
            }
        }

        return parentBOPLine;
    }

    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        dataMap.put(dataName, dataList, IData.TABLE_FIELD);
        dataSet.addDataMap(dataName, dataMap);

        return dataSet;
    }

}
