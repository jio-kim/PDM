/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.TcDefinition;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCReservationService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ModifySWMDocOperation
 * Class Description :
 * 
 * @date 2013. 12. 23.
 * 
 */
public class ModifySWMDocOperation extends AbstractSDVActionOperation {
    private Registry registry;
    private IDataMap dataMap;
    private IDataSet dataset;
    private String itemId;
    private String referenceItemId;
    private String group;
    private String referenceObjectName;
    private Date discardDate;
    TCComponentDataset dataSet;
    TCComponentItemRevision itemRev;

    public ModifySWMDocOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        dataset = getDataSet();
        dataMap = dataset.getDataMap("registerSWMDocView");

        itemRev = (TCComponentItemRevision) dataMap.getValue("targetComp");
        if (!itemRev.isCheckedOut()) {
            return;
        }

        referenceItemId = dataMap.getStringValue(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO);
        group = dataMap.getStringValue(SDVPropertyConstant.SWM_GROUP);
        referenceObjectName = dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME);
        discardDate = (Date) dataMap.getValue(SDVPropertyConstant.ITEM_M7_DISCARD_DATE);

        if (!modifySWMItemValidate()) {
            return;
        }

        itemRev.setProperty(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO, referenceItemId);
        itemRev.setProperty(SDVPropertyConstant.SWM_GROUP, group);
        itemRev.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, referenceObjectName);
        itemRev.setDateProperty(SDVPropertyConstant.ITEM_M7_DISCARD_DATE, discardDate);

        Map<String, Object> paramMap = getParameters();
        if (SYMTcUtil.isCheckedOut(itemRev) && paramMap.containsKey("checkinflag")) {
            String checkinflag = (String) paramMap.get("checkinflag");
            if (checkinflag.equals("false")) {
                IDialog dialog = UIManager.getCurrentDialog();
                if (dialog.getShell() == null) {
                    dialog = UIManager.getAvailableDialog("symc.dialog.modifySWMDocDialog");
                }
                MessageBox.post(dialog.getShell(), registry.getString("SaveEditWasSuccessful.MESSAGE"), "Information", MessageBox.INFORMATION);
            }
        }

        saveAndCheckIn();
    }

    public boolean modifySWMItemValidate() {

        // 수정 조건에 관련근거가 필요 합니다
        if (referenceItemId == null || referenceItemId.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.modifySWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("ModifyConditionIsRequiredReferenceField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        // 수정 조건에 직이 필요 합니다
        if (group == null || group.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.modifySWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("ModifyConditionIsRequiredGroupField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        // 수정 조건에 작업명이 필요 합니다
        if (referenceObjectName == null || referenceObjectName.trim().length() == 0) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.modifySWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("ModifyConditionIsRequiredWorkNameField.MESSAGE"), "Warning", MessageBox.WARNING);
            return false;
        }

        return true;
    }

    public void saveAndCheckIn() throws Exception {
        Map<String, Object> paramMap = getParameters();
        if (SYMTcUtil.isCheckedOut(itemRev) && paramMap.containsKey("checkinflag")) {
            String checkinflag = (String) paramMap.get("checkinflag");
            if (checkinflag.equals("true")) {

                datasetModify();

                TCReservationService itemRevisioncheckIn = itemRev.getSession().getReservationService();
                try {
                    itemRevisioncheckIn.unreserve(itemRev);
                } catch (TCException e) {
                    e.printStackTrace();
                }
                IDialog dialog = UIManager.getCurrentDialog();
                if (dialog.getShell() == null) {
                    dialog = UIManager.getAvailableDialog("symc.dialog.modifySWMDocDialog");
                }
                MessageBox.post(dialog.getShell(), registry.getString("SaveAndCheck-InwasSuccessful.MESSAGE"), "Information", MessageBox.INFORMATION);
            }
        }
    }

    /**
     * 데이터셋에 기존 표준작업요령서 엑셀 파일을 수정 파일로 업로드
     * 
     */
    public void datasetModify() throws Exception {
        File updateFile = updateFile(itemId, referenceItemId, referenceObjectName);
        //[UPGRADE][240320] NULL 처리
        if(updateFile == null)
        	return ;
        Vector<File> importFiles = new Vector<File>();
        importFiles.add(updateFile);

        AIFComponentContext[] contexts = itemRev.getChildren();
        if (contexts != null) {
            for (int i = 0; i < contexts.length; i++) {
                if (contexts[i].getComponent() instanceof TCComponentDataset) {
                    dataSet = (TCComponentDataset) contexts[i].getComponent();
                    break;
                }
            }
        }

        if (dataSet != null) {
            CustomUtil.removeAllNamedReferenceOfSWM((TCComponentDataset) dataSet);
            SYMTcUtil.importFiles(dataSet, importFiles);
            updateFile.delete();
        }
    }

    public File updateFile(String itemId, String referenceItemId, String referenceObjectName) throws Exception {
        TCSession session = CustomUtil.getTCSession();
        File file = getFile(itemRev, session);
        //[UPGRADE][240320] NULL 처리
        if(file == null)
        	return null;
        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);

        // 관련근거(참조 된 item ID)
        Row rowReferenceItemId = sheet.getRow(11);
        rowReferenceItemId.getCell(24).setCellValue(referenceItemId);

        // 작업명(참조 된 공법명)
        Row rowReferenceObjectName = sheet.getRow(12);
        rowReferenceObjectName.getCell(13).setCellValue(referenceObjectName);

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }

    private static File getFile(TCComponentItemRevision itemRevision, TCSession session) throws Exception {
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        datasets = CustomUtil.getDatasets(itemRevision, TcDefinition.TC_SPECIFICATION_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        //[UPGRADE][240320] NULL 처리
        if(datasets == null || datasets.size() == 0)
        	return null;
        File[] localfile = null;
        localfile = CustomUtil.exportDataset(datasets.get(0), session.toString());
        return localfile[0];
    }

}
