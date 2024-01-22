/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.data.IDataMap;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ExportSWMDocOperation
 * Class Description : (List button action)검색 결과에서 선택 한 표준작업요령서 아이템 하위 dataset에 첨부된 표준작업요령서를 Local영역에 다운
 * 
 * @date 2013. 12. 6.
 * 
 */
public class ExportSWMDocOperation extends AbstractSDVActionOperation {
    private Registry registry;
    private static String filePath;

    public ExportSWMDocOperation(int actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
        registry = Registry.getRegistry(this);
    }

    public ExportSWMDocOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
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
        List<HashMap<String, Object>> opList = null;
        IDataSet dataset = getDataSet();
        IDataMap dataMap = dataset.getDataMap("searchListSWMDocView");

        if (!dataMap.containsKey("targetOperationList")) {
            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("SelectOneTargetItem.MESSAGE"), "Warning", MessageBox.WARNING);
            return;
        } else {
            opList = (List<HashMap<String, Object>>) dataMap.getTableValue("targetOperationList");
            if (opList.size() == 0) {
                MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("SelectOneTargetItem.MESSAGE"), "Warning", MessageBox.WARNING);
                return;
            }
        }

        openDirectoryDialog();

        if (filePath != null) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", registry.getString("DownloadWorkingComplete.MESSAGE"));
                }
            });
        }

        for (int i = 0; i < opList.size(); i++) {
            String itemId = registry.getString((String) opList.get(i).get(SDVPropertyConstant.ITEM_ITEM_ID));
            String revisionId = (String) opList.get(i).get(SDVPropertyConstant.ITEM_REVISION_ID);

            TCComponentItemRevision revision = CustomUtil.findItemRevision(SDVTypeConstant.STANDARD_WORK_METHOD_ITEM_REV, itemId, revisionId);
            if (revision != null) {
                TCComponent[] components = revision.getRelatedComponents("IMAN_specification");
                for (TCComponent component : components) {
                    if (component instanceof TCComponentDataset) {
                        TCComponentDataset tcDataset = (TCComponentDataset) component;
                        TCComponentTcFile[] files = tcDataset.getTcFiles();
                        if (files != null) {
                            for (TCComponentTcFile file : files) {
                                String fileName = ((String) file.getProperty("original_file_name")).split(".xlsx")[0] + "_" + ExcelTemplateHelper.getToday("yyyyMMdd") + ".xlsx";
                                file.getFile(filePath, fileName);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public static void openDirectoryDialog() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Shell shell = AIFUtility.getActiveDesktop().getShell();
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SAVE);
                filePath = dialog.open();
            }
        });
    }

}
