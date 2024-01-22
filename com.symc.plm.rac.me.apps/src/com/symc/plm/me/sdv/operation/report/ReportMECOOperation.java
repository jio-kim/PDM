package com.symc.plm.me.sdv.operation.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.ssangyong.rac.kernel.SYMCBOPEditData;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;

/**
 * [SR140611-055][20140609] byKim
 * ① MECO Report에 작성자 승인요청일자 누락
 */
public class ReportMECOOperation extends SimpleSDVExcelOperation {

    public ReportMECOOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        try {
            IDataSet dataSet = getData();
            if (dataSet != null) {
                String defaultFileName = dataSet.getDataMap("mecoInfo").getStringValue(SDVPropertyConstant.ITEM_ITEM_ID) + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }
        } catch (Exception e) {
            setExecuteError(e);
            // MessageBox에 보여줄 메시지
            // 구현하지 않으면 default 메시지를 보여준다.
            // setErrorMessage("");
        }
    }

    @Override
    protected IDataSet getData() throws Exception {
        IDataSet dataSet = new DataSet();
        IDataMap mecoInfoMap = new RawDataMap();
        IDataMap signatureInfoMap = new RawDataMap();
        IDataMap mecoEPLInfoMap = new RawDataMap();
        IDataMap additionalInfoMap = new RawDataMap();
        dataSet.addDataMap("mecoInfo", mecoInfoMap);
        dataSet.addDataMap("signatureInfo", signatureInfoMap);
        dataSet.addDataMap("mecoEPLInfo", mecoEPLInfoMap);
        dataSet.addDataMap("additionalInfo", additionalInfoMap);

        InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
        if (component != null && component instanceof TCComponentChangeItemRevision) {
            TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) component;
            TCComponentChangeItemRevision mecoRevision = null;
            if (changeRevision.getType().equals(SDVTypeConstant.MECO_ITEM_REV)) {
                mecoRevision = changeRevision;
            }

            // Print Date
            additionalInfoMap.put("print_date", ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm:ss"));

            // MECO A - MECO Info
            mecoInfoMap = getMECOInfo(mecoRevision, mecoInfoMap);

            // MECO A - Signature Info
            signatureInfoMap = getSignatureInfo(mecoRevision, signatureInfoMap);

            // MECO EPL
            mecoEPLInfoMap.put("mecoEPL", getMECOEPLList(mecoRevision), IData.OBJECT_FIELD);

        }

        return dataSet;
    }

    /**
     * MECO A MECO Info
     *
     * @method getMECOInfo
     * @date 2014. 2. 13.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    private IDataMap getMECOInfo(TCComponentItemRevision mecoRevision, IDataMap mecoInfoMap) throws TCException {
        // MECO Date
        mecoInfoMap.put(SDVPropertyConstant.ITEM_DATE_RELEASED, getFormatDate(mecoRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED), "yyyy-MM-dd HH:mm"));

        // MECO Number
        mecoInfoMap.put(SDVPropertyConstant.ITEM_ITEM_ID, mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

        // MECO Dept
        TCComponentUser user = (TCComponentUser) mecoRevision.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
        TCComponentPerson person = (TCComponentPerson) user.getReferenceProperty("person");
        mecoInfoMap.put(TCComponentPerson.PROP_PA6, person.getProperty(TCComponentPerson.PROP_PA6));

        // 차종
        mecoInfoMap.put(SDVPropertyConstant.MECO_PROJECT, mecoRevision.getProperty(SDVPropertyConstant.MECO_PROJECT));

        // Status
        mecoInfoMap.put("m7_MECO_MATURITY", mecoRevision.getProperty("m7_MECO_MATURITY"));

        // Eff.Date
        mecoInfoMap.put(SDVPropertyConstant.MECO_EFFECT_DATE, mecoRevision.getProperty(SDVPropertyConstant.MECO_EFFECT_DATE));

        // Eff.Event
        mecoInfoMap.put(SDVPropertyConstant.MECO_EFFECT_EVENT, mecoRevision.getProperty(SDVPropertyConstant.MECO_EFFECT_EVENT));

        // Change Reason
        mecoInfoMap.put(SDVPropertyConstant.MECO_CHANGE_REASON, SDVLOVUtils.getLovValueDesciption("M7_MECO_REASON", mecoRevision.getProperty(SDVPropertyConstant.MECO_CHANGE_REASON)));

        // Change Description
        mecoInfoMap.put(SDVPropertyConstant.ITEM_OBJECT_DESC, mecoRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));

        return mecoInfoMap;
    }

    /**
     * MECO A Signature Info
     *
     * @method getSignatureInfo
     * @date 2014. 2. 13.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    private IDataMap getSignatureInfo(TCComponentItemRevision mecoRevision, IDataMap signatureInfoMap) throws TCException {
        AIFComponentContext[] context = mecoRevision.whereReferenced();
        if (context != null) {
            for (int i = 0; i < context.length; i++) {
                TCComponent component = (TCComponent) context[i].getComponent();
                if (component instanceof TCComponentTask) {
                    TCComponentTask task = (TCComponentTask) component;
                    TCComponentTask[] subTask = task.getSubtasks();
                    if (subTask != null) {
                        for (int j = 0; j < subTask.length; j++) {
                            if (subTask[j].getName().equals("Creator")) {
                                signatureInfoMap.put("creator", getPersonInfo(subTask[j]), IData.OBJECT_FIELD);
                            } else if (subTask[j].getName().equals("Sub Team Leader")) {
                                signatureInfoMap.put("subTeamLeader", getPersonInfo(subTask[j]), IData.OBJECT_FIELD);
                            } else if (subTask[j].getName().equals("Team Leader")) {
                                signatureInfoMap.put("teamLeader", getPersonInfo(subTask[j]), IData.OBJECT_FIELD);
                            } else if (subTask[j].getName().equals("BOP ADMIN")) {
                                signatureInfoMap.put("bopAdmin", getPersonInfo(subTask[j]), IData.OBJECT_FIELD);
                            }
                        }
                    }
                }
            }
        }

        return signatureInfoMap;
    }

    /**
     * MECO A Person Info
     * [SR140611-055][20140609] byKim ① MECO Report에 작성자 승인요청일자 누락
     *
     * @method getPersonInfo
     * @date 2014. 2. 13.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    private IDataMap getPersonInfo(TCComponentTask task) throws TCException {
        IDataMap dataMap = new RawDataMap();

        TCComponentSignoff[] signoff = task.getValidSignoffs();
        if (signoff != null && signoff.length > 0) {
            TCComponentPerson person = (TCComponentPerson) signoff[0].getGroupMember().getUser().getReferenceProperty("person");
            dataMap.put("decision_date", getFormatDate(signoff[0].getDecisionDate(), "yyyy-MM-dd HH:mm"), IData.OBJECT_FIELD);
            if (dataMap.get("decision_date").getStringValue().equals("")) {
                dataMap.put("decision_date", getFormatDate(task.getDateProperty("creation_date"), "yyyy-MM-dd HH:mm"));
            }
            dataMap.put("user_name", person.getProperty("user_name"));
            dataMap.put(TCComponentPerson.PROP_PA10, person.getProperty(TCComponentPerson.PROP_PA10));
            dataMap.put(TCComponentPerson.PROP_PA6, person.getProperty(TCComponentPerson.PROP_PA6));
            dataMap.put("comments", signoff[0].getProperty("comments"));
        }

        return dataMap;
    }

    /**
     * MECO EPL
     *
     * @method getMECOEPLList
     * @date 2014. 2. 13.
     * @param
     * @return ArrayList<SYMCBOPEditData>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<SYMCBOPEditData> getMECOEPLList(TCComponentChangeItemRevision mecoRevision) throws Exception {
        CustomMECODao dao = new CustomMECODao();
        ArrayList<SYMCBOPEditData> dataList = dao.selectMECOEplList(mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

        return dataList;
    }

    /**
     * 날짜 포맷 변경
     *
     * @method getFormatDate
     * @date 2014. 2. 13.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getFormatDate(Date date, String format) {
        String strDate = "";

        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            strDate = simpleDateFormat.format(date);
        }

        return strDate;
    }

}
