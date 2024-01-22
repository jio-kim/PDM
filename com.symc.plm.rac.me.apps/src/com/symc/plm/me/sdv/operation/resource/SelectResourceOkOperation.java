/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Class Name : SDVSampleOkOperation
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class SelectResourceOkOperation extends AbstractSDVActionOperation {

    private String dialogIdCrumb;

    /**
     * @param operationId
     * @param ownerId
     * @param dataSet
     */
    public SelectResourceOkOperation(int operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
    }

    public SelectResourceOkOperation(String operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
    }

    public SelectResourceOkOperation(String operationId, String ownerId, Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
    }
    
    public SelectResourceOkOperation(int operationId, String ownerId, Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        IDataSet dataset = getDataSet();
        if (dataset.containsMap("Assy:Equip:Create")) {
            final RawDataMap datamap = (RawDataMap) dataset.getDataMap("Assy:Equip:Create");
            dialogIdCrumb = datamap.getStringValue("dialogIdCrumb");

            Display.getDefault().syncExec(new Runnable() {

                public void run() {
                    try {
                        AbstractSDVSWTDialog currentDialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
                        currentDialog.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    } finally {
                        OpenDialog openDialog = new OpenDialog(datamap);
                        openDialog.run();
                    }
                }
            });
        }
    }

    class OpenDialog extends Thread {
        RawDataMap datamap;

        OpenDialog(RawDataMap datamap) {
            this.datamap = datamap;
        }

        @Override
        public void run() {
            AbstractSDVSWTDialog createResourceDialog = null;

            Map<String, Object> paramKeyMap = new HashMap<String, Object>();
            paramKeyMap.put("paramKey", datamap);

            try {
                createResourceDialog = (AbstractSDVSWTDialog) UIManager.getDialog(AIFUtility.getActiveDesktop().getShell(), "symc.me.resource." + dialogIdCrumb + ".CreateEquipmentDialog");
            } catch (Exception e) {
                e.printStackTrace();
            }
            createResourceDialog.setParameters(paramKeyMap);
            openDialog(createResourceDialog);
        }

        private void openDialog(final AbstractSDVSWTDialog dialog) {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    dialog.open();
                }
            });
        }
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }
}
