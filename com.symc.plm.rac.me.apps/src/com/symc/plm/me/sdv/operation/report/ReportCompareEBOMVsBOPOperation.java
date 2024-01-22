package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;

public class ReportCompareEBOMVsBOPOperation extends SimpleSDVExcelOperation {

    public ReportCompareEBOMVsBOPOperation() {

    }

    @SuppressWarnings("unchecked")
    @Override
    protected IDataSet getData() throws Exception {
        Map<String, Object> paramMap = getParamters();
        ArrayList<HashMap<String, Object>> dataList = (ArrayList<HashMap<String, Object>>) paramMap.get("dataList");
        HashMap<String, Object> conditionMap = (HashMap<String, Object>) paramMap.get("conditionMap");

        IDataSet dataSet = convertToDataSet("CompareEBOMVsBOPList", dataList);
        IDataMap dataMap = new RawDataMap();
        dataMap.put("conditionMap", conditionMap, IData.OBJECT_FIELD);
        dataMap.put("excelExportDate", ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm"));
        dataSet.addDataMap("additionalInfo", dataMap);

        return dataSet;
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            IDataSet dataSet = getData();
            if (dataSet != null) {
                String defaultFileName = "CompareEBOMVsBOPList" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }
        } catch (Exception e) {
            setExecuteError(e);
            // MessageBox에 보여줄 메시지
            // 구현하지 않으면 default 메시지를 보여준다.
            // setErrorMessage("");
        }
    }

    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        dataMap.put(dataName, dataList, IData.TABLE_FIELD);
        dataSet.addDataMap(dataName, dataMap);

        return dataSet;
    }

}
