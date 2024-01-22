/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVQueryUtils;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.services.NavigatorOpenService;
import com.teamcenter.soa.client.model.LovValue;

import common.Logger;

/**
 * Class Name : RegisterSWMDocOperation
 * Class Description :
 * 
 * @date 2013. 11. 12.
 * 
 */
public class RegisterSWMDocOperation extends AbstractSDVActionOperation {
    private static final Logger logger = Logger.getLogger(RegisterSWMDocOperation.class);

    // 사용자가 선택한 값
    private IDataMap dataMap;

    // 표준작업요령서 폴더
    private TCComponent stdWorkMethodFolder;

    // 표준작업요령서 하위 차종 폴더
    private TCComponent vehicleFolder;

    // 생성된 표준작업요령서 아이템
    TCComponentItem docItem;

    private String shopCode;
    private String category;
    private String vehicleNo;
    private String referenceItemId;
    private String group;
    private String referenceObjectName;

    public RegisterSWMDocOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public RegisterSWMDocOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    /**
     * 검색 조건(사용자가 선택한 값)
     * 
     * @throws Exception
     */
    @Override
    public void executeOperation() throws Exception {
        IDataSet dataset = getDataSet();
        dataMap = dataset.getDataMap("registerSWMDocView");

        shopCode = dataMap.getStringValue(SDVPropertyConstant.SWM_SHOP_CODE);
        category = dataMap.getStringValue(SDVPropertyConstant.SWM_CATEGORY);
        vehicleNo = dataMap.getStringValue(SDVPropertyConstant.SWM_VEHICLE_CODE);
        referenceItemId = dataMap.getStringValue(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO);
        group = dataMap.getStringValue(SDVPropertyConstant.SWM_GROUP);
        referenceObjectName = dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME);

        String seq = search();

        if (seq != null) {
            createItem(seq);
        }
    }

    /**
     * 차종별 일련번호 검색
     * 
     * @throws Exception
     */
    public String search() throws Exception {
        String vehicleNo = dataMap.getStringValue(SDVPropertyConstant.SWM_VEHICLE_CODE);
        TCComponent[] qryResult = SDVQueryUtils.executeSavedQuery("SYMC_Search_StdWorkMethod", new String[] { "m7_VEHICLE_CODE" }, new String[] { vehicleNo });
        ArrayList<String> list = new ArrayList<String>();

        String seq = null;
        if (qryResult != null && qryResult.length >= 1) {
            for (int i = 0; i < qryResult.length; i++) {
                String itemId = qryResult[i].getProperty("item_id");
                itemId = itemId.substring(9, 12);
                String num = itemId;
                int numAdd = Integer.parseInt(num) + 1;
                String seqNum = String.format("%03d", numAdd);
                list.add(seqNum);
            }
            Collections.sort(list);
            seq = list.get(list.size() - 1);
        } else {
            seq = "001";
        }

        return seq;
    }

    /**
     * 폴더 , 아이템, 데이터셋 생성
     * 
     * @throws Exception
     */
    public void createItem(String seq) throws Exception {
        TCSession session = CustomUtil.getTCSession();
        Markpoint mp = new Markpoint(session);

        TCComponentFolder[] folders = CustomUtil.findFolder("표준작업요령서", "Folder", "*");

        // 표준작업요령서 폴더 없으면 폴더 생성
        if (folders.length == 0) {
            stdWorkMethodFolder = CustomUtil.createFolder("표준작업요령서", "표준작업요령서 폴더 desc", "Folder");
            session.getUser().getHomeFolder().add("contents", stdWorkMethodFolder);
        } else {
            stdWorkMethodFolder = folders[0];
        }

        // 차종 폴더없으면 폴더 생성
        AIFComponentContext[] vehicleFolders = stdWorkMethodFolder.getChildren();
        String subVehicleFolderCompName;
        InterfaceAIFComponent subVehicleFolderComp;
        for (AIFComponentContext subVehicleFolders : vehicleFolders) {
            subVehicleFolderComp = subVehicleFolders.getComponent();
            subVehicleFolderCompName = subVehicleFolderComp.toString();

            if (subVehicleFolderCompName.equalsIgnoreCase(vehicleNo)) {
                vehicleFolder = (TCComponent) subVehicleFolderComp;
            }
        }
        if (vehicleFolder == null) {
            vehicleFolder = CustomUtil.createFolder(vehicleNo, "차종 폴더 desc", "Folder");
            stdWorkMethodFolder.add("contents", vehicleFolder);
        }

        // 아이템 생성(입력받은 속성값 추가)
        String itemId = vehicleNo + "-" + shopCode + "-" + category + "-" + seq;
        docItem = CustomUtil.createPasteItem(vehicleFolder, "contents", "M7_StdWorkMethod", itemId, "000", referenceObjectName, "");

        TCComponentItemRevision itemRevision = docItem.getLatestItemRevision();

        // 생성된 아이템 뷰텝으로 보여주기
        NavigatorOpenService openService = new NavigatorOpenService();
        openService.open(itemRevision);
        mp.forget();

        itemRevision.setProperty(SDVPropertyConstant.SWM_VEHICLE_CODE, vehicleNo);
        itemRevision.setProperty(SDVPropertyConstant.SWM_SHOP_CODE, shopCode);
        itemRevision.setProperty(SDVPropertyConstant.SWM_CATEGORY, category);
        itemRevision.setProperty(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO, referenceItemId);
        itemRevision.setProperty(SDVPropertyConstant.SWM_GROUP, group);
        itemRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, referenceObjectName);

        dataMap.put(SDVPropertyConstant.ITEM_ITEM_ID, itemId, IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.ITEM_CREATION_DATE, new Date(), IData.OBJECT_FIELD);
        dataMap.put(SDVPropertyConstant.ITEM_OWNING_USER, itemRevision.getProperty(SDVPropertyConstant.ITEM_OWNING_USER), IData.STRING_FIELD);

        List<LovValue> vehicleCodeList = getLOVValues(ExcelTemplateHelper.getTCSession(), "M7_VEHICLE_NO");
        for (LovValue vehicleCodeElement : vehicleCodeList) {
            if (vehicleNo.equals(vehicleCodeElement.getValue())) {
                String value = (String) vehicleCodeElement.getValue();
                String desc = vehicleCodeElement.getDescription();
                dataMap.put(SDVPropertyConstant.SWM_VEHICLE_CODE, value + " (" + desc + ")");
            }
        }
        List<LovValue> categoryList = getLOVValues(ExcelTemplateHelper.getTCSession(), "M7_SWM_CATEGORY");
        for (LovValue categoryElement : categoryList) {
            if (category.equals(categoryElement.getValue())) {
                dataMap.put(SDVPropertyConstant.SWM_CATEGORY, categoryElement.getDescription(), IData.STRING_FIELD);
            }
        }

        IDataSet dataset = new DataSet();
        dataset.addDataMap("registerSWMDocView", dataMap);
        RegisterSWMDocExcelTransformer transformer = new RegisterSWMDocExcelTransformer();
        transformer.print(ExcelTemplateHelper.EXCEL_OPEN, "M7_TEM_DocItemID_StdWorkMethod", itemId + "_StdWorkMethod", dataset);
        File file = transformer.getTemplateFile();
        if (file != null) {
            Vector<File> datasetFile = new Vector<File>();
            datasetFile.add(file);
            TCComponentDataset createDataSet = SYMTcUtil.createDataSet(CustomUtil.getTCSession(), itemRevision, "MSExcelX", itemId, datasetFile);
            file.delete();
            logger.debug("DataSet Created :" + createDataSet);
        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", "Create Std.Instruction of Work Complete. Excle work is need.");
            }
        });
    }

    public static List<LovValue> getLOVValues(TCSession session, String lovName) throws TCException {
        TCComponentListOfValuesType type = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
        TCComponentListOfValues[] values = type.find(lovName);

        return values[0].getListOfValues().getValues();
    }

}
