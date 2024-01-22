package com.symc.plm.rac.prebom.ccn.operation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.rac.prebom.ccn.commands.dao.CustomCCNDao;
import com.symc.plm.rac.prebom.ccn.excel.common.ExcelTemplateHelper;
import com.symc.plm.rac.prebom.ccn.excel.transformer.CCNDataExcelTransformer;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.SDVLOVUtils;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;

public class PreCCNReportOperation extends AbstractAIFOperation{
    
    TCComponentItemRevision ccnRevision = null;
    TCSession session;
    private String ccnNo = "";
    private String templateName = "S7_TEM_DocItemID_CCN";

    public PreCCNReportOperation(TCComponentItemRevision revision){
        this.ccnRevision = revision;
    }

    @Override
    public void executeOperation() throws Exception {
        session = (TCSession) getSession();
        try {
            IDataSet dataSet = getData();

            if(dataSet != null) {
                String defaultFileName = ccnNo + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                CCNDataExcelTransformer transformer = new CCNDataExcelTransformer();
                transformer.print(0, templateName, defaultFileName, dataSet);
                //AssignmentWeldPointsListDataExcelTransformer transformer = new AssignmentWeldPointsListDataExcelTransformer();
                //transformer.print(1, "M7_TEM_DocItemID_OperationMasterList", defaultFileName, dataSet);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    protected IDataSet getData() throws Exception {
        
        List<HashMap<String, Object>> ospecTrimList = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> masterList = new ArrayList<HashMap<String, Object>>();
        
        String ospecNo = "";
        String projectCode = "";
        String systemCode = "";
        String projectType = "";
        String projectTypeDesc = "";
        String affetedSysCode = "";
        String changeReason = "";
        String gateNo = "";
        String ccnDesc = "";
        String createDateString = "";
        String releaseString = "";
        
        String creator = "";
        String deptName = "";
        
        boolean regulation = false;
        boolean costDown = false;
        boolean orderingSpec = false;
        boolean qualityImprovement = false;
        boolean correctionEpl = false;
        boolean stylingUpDate = false;
        boolean weightChange = false;
        boolean materialCostChange = false;
        boolean theOthers = false;
        
        Date createDate = new Date(); 
        Date releaseDate = new Date();

        InterfaceAIFComponent component =  AIFUtility.getCurrentApplication().getTargetComponent();
        if(component != null && component instanceof TCComponentChangeItemRevision) {
            TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) component;

            // product Code
            ccnNo = changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
            ospecNo = changeRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
            projectCode = changeRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
            systemCode = changeRevision.getProperty(PropertyConstant.ATTR_NAME_SYSTEMCODE);
            projectType = changeRevision.getProperty(PropertyConstant.ATTR_NAME_PROJECTTYPE);
            projectTypeDesc = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_PROJECTTYPE, projectType);
            affetedSysCode = changeRevision.getProperty(PropertyConstant.ATTR_NAME_AFFECTEDSYSCODE);
            changeReason = changeRevision.getProperty(PropertyConstant.ATTR_NAME_CHGREASON);
            gateNo = changeRevision.getProperty(PropertyConstant.ATTR_NAME_GATENO);
            ccnDesc = changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC);
            createDate = changeRevision.getDateProperty(PropertyConstant.ATTR_NAME_CREATIONDATE);
            releaseDate = changeRevision.getDateProperty(PropertyConstant.ATTR_NAME_DATERELEASED);
            
            TCComponentUser owingUser = (TCComponentUser) ccnRevision.getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGUSER);
            TCComponentGroup owingGroup = (TCComponentGroup) ccnRevision.getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGGROUP);
            creator = owingUser.getUserId();
            deptName = owingGroup.getGroupName();
            
            regulation = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_REGULATION);
            costDown = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_COSTDOWN);
            orderingSpec = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_ORDERINGSPEC);
            qualityImprovement = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT);
            correctionEpl = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL);
            stylingUpDate = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_STYLINGUPDATE);
            weightChange = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_WEIGHTCHANGE);
            materialCostChange = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE);
            theOthers = changeRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_THEOTHERS);
            
            ospecTrimList = selectOSpecHeaderInfoList(ospecNo);
//            ospecTrimList = selectOSpecHeaderInfoList("OSI-A149-000");
            masterList = selectMasterInfoList(ccnNo);
//            masterList = selectMasterInfoList("CNX100C101504");
            
            ArrayList<HashMap< String, Object>> resultList = selectMasterSystemCode(ccnNo);
//            ArrayList<HashMap< String, Object>> resultList = selectMasterSystemCode("CNX100C101504");
            String sysCodes = "";
            for (int i = 0; i < resultList.size(); i++) {
                if (null != resultList.get(i)) {
                    sysCodes += (String) resultList.get(i).get("MASTER_LIST_SYSCODE");
                    if ((i + 1) == resultList.size()) {
                        break;
                    }
                    sysCodes += ", ";
                }
            }
            affetedSysCode = sysCodes;

            createDateString = ExcelTemplateHelper.getDate(createDate, "yyyy-MM-dd HH:mm");
            if (null != releaseDate) {
                releaseString = ExcelTemplateHelper.getDate(releaseDate, "yyyy-MM-dd HH:mm");
            }
        }

        IDataSet dataSet = convertToDataSet("masterList", masterList);
        IDataMap dataMap = new RawDataMap();
        dataMap = convertToDataMap("ospecTrimList", ospecTrimList);

        dataMap.put(PropertyConstant.ATTR_NAME_ITEMID, ccnNo);
        dataMap.put(PropertyConstant.ATTR_NAME_OSPECNO, ospecNo);
        dataMap.put(PropertyConstant.ATTR_NAME_PROJCODE, projectCode);
        dataMap.put(PropertyConstant.ATTR_NAME_SYSTEMCODE, systemCode);
        dataMap.put(PropertyConstant.ATTR_NAME_PROJECTTYPE, projectTypeDesc);
        dataMap.put(PropertyConstant.ATTR_NAME_AFFECTEDSYSCODE, affetedSysCode);
        dataMap.put(PropertyConstant.ATTR_NAME_CHGREASON, changeReason);
        dataMap.put(PropertyConstant.ATTR_NAME_GATENO, gateNo);
        dataMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, ccnDesc);
        
        dataMap.put(PropertyConstant.ATTR_NAME_REGULATION, String.valueOf(regulation));
        dataMap.put(PropertyConstant.ATTR_NAME_COSTDOWN, String.valueOf(costDown));
        dataMap.put(PropertyConstant.ATTR_NAME_ORDERINGSPEC, String.valueOf(orderingSpec));
        dataMap.put(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT, String.valueOf(qualityImprovement));
        dataMap.put(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL, String.valueOf(correctionEpl));
        dataMap.put(PropertyConstant.ATTR_NAME_STYLINGUPDATE, String.valueOf(stylingUpDate));
        dataMap.put(PropertyConstant.ATTR_NAME_WEIGHTCHANGE, String.valueOf(weightChange));
        dataMap.put(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE, String.valueOf(materialCostChange));
        dataMap.put(PropertyConstant.ATTR_NAME_THEOTHERS, String.valueOf(theOthers));
        
        dataMap.put(PropertyConstant.ATTR_NAME_CREATIONDATE, createDateString);
        dataMap.put(PropertyConstant.ATTR_NAME_DATERELEASED, releaseString);
        dataMap.put("creator", creator);
        dataMap.put("deptName", deptName);
        dataMap.put("excelExportDate", ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm"));
        dataSet.addDataMap("mainInfo", dataMap);

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
    *
    *
    * @method convertToDataMap
    * @date 2015. 4. 21.
    * @param
    * @return IDataSet
    * @exception
    * @throws
    * @see
    */
   private IDataMap convertToDataMap(String dataName, List<HashMap<String, Object>> dataList) {
       IDataMap dataMap = new RawDataMap();
       dataMap.put(dataName, dataList, IData.TABLE_FIELD);

       return dataMap;
   }


    private ArrayList<HashMap< String, Object>> selectOSpecHeaderInfoList(String ospecNo) {
        CustomCCNDao dao = null;
        ArrayList<HashMap< String, Object>> resultList = new ArrayList<HashMap< String, Object>>();
        try {
            dao = new CustomCCNDao();
            resultList = dao.selectOSpecHeaderInfoList(ospecNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    private ArrayList<HashMap< String, Object>> selectMasterInfoList(String ccnId) {                                                
        CustomCCNDao dao = null;
        ArrayList<HashMap< String, Object>> resultList = new ArrayList<HashMap< String, Object>>();
        try {
            dao = new CustomCCNDao();
            resultList = dao.selectMasterInfoList(ccnId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    private ArrayList<HashMap< String, Object>> selectMasterSystemCode(String ccnId) {                                                
        CustomCCNDao dao = null;
        ArrayList<HashMap< String, Object>> resultList = new ArrayList<HashMap< String, Object>>();
        try {
            dao = new CustomCCNDao();
            resultList = dao.selectMasterSystemCode(ccnId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
