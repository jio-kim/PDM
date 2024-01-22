/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;

/**
 * Class Name : MoveBOPLineView
 * Class Description : BOP 이동 View
 * 
 * @date 2014. 2. 17.
 * 
 */
public class MoveAssyOPView extends AbstractSDVViewPane {
    private Table targetTable;
    private Table sourceTable;
    private ArrayList<TCComponentBOMLine> moveBOMList = null; // 이동될 BOP Line 리스트

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public MoveAssyOPView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Group grpTargetBopline = new Group(composite, SWT.NONE);
        grpTargetBopline.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpTargetBopline.setText("Select Target Line");
        grpTargetBopline.setLayout(new GridLayout(1, false));

        targetTable = new Table(grpTargetBopline, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_targetTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_targetTable.heightHint = 280;
        targetTable.setLayoutData(gd_targetTable);
        targetTable.setHeaderVisible(true);
        targetTable.setLinesVisible(true);

        TableColumn tblclmnTargetPartId = new TableColumn(targetTable, SWT.NONE);
        tblclmnTargetPartId.setWidth(160);
        tblclmnTargetPartId.setText("Part ID");

        TableColumn tblclmnTargetRevision = new TableColumn(targetTable, SWT.NONE);
        tblclmnTargetRevision.setWidth(60);
        tblclmnTargetRevision.setText("Revision");

        TableColumn tblclmnTargetRevName = new TableColumn(targetTable, SWT.NONE);
        tblclmnTargetRevName.setWidth(250);
        tblclmnTargetRevName.setText("Rev Name");

        Group grpSourceBoplineList = new Group(composite, SWT.NONE);
        grpSourceBoplineList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpSourceBoplineList.setText("Move Operation List");
        grpSourceBoplineList.setLayout(new GridLayout(1, false));

        sourceTable = new Table(grpSourceBoplineList, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_sourceTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_sourceTable.heightHint = 140;
        sourceTable.setLayoutData(gd_sourceTable);
        sourceTable.setHeaderVisible(true);
        sourceTable.setLinesVisible(true);

        TableColumn tblclmnSrcPartId = new TableColumn(sourceTable, SWT.NONE);
        tblclmnSrcPartId.setWidth(160);
        tblclmnSrcPartId.setText("Part ID");

        TableColumn tblclmnSrcRevision = new TableColumn(sourceTable, SWT.NONE);
        tblclmnSrcRevision.setWidth(60);
        tblclmnSrcRevision.setText("Revision");

        TableColumn tblclmnSrcRevName = new TableColumn(sourceTable, SWT.NONE);
        tblclmnSrcRevName.setWidth(250);
        tblclmnSrcRevName.setText("Rev Name");
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        RawDataMap dataMap = new RawDataMap();
        Object data = null;
        int selectedIndex = targetTable.getSelectionIndex();
        if (selectedIndex >= 0) {
            TableItem tableItem = targetTable.getItem(selectedIndex);
            data = tableItem.getData();
        }
        dataMap.put("TARGET_BOMLINE", data, IData.OBJECT_FIELD);
        dataMap.put("MOVE_LIST", moveBOMList, IData.OBJECT_FIELD);
        return dataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new InitOperation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_FAILED)
            return;
        if (dataset == null)
            return;

        try {
            IDataMap dataMap = dataset.getDataMap(this.getId());
            if (dataMap.getListValue("TARGET_LIST") == null)
                return;
            if (dataMap.getListValue("MOVE_LIST") == null)
                return;

            ArrayList<TCComponentBOMLine> targetBOMList = (ArrayList<TCComponentBOMLine>) dataMap.getListValue("TARGET_LIST");

            moveBOMList = new ArrayList<TCComponentBOMLine>();
            moveBOMList = (ArrayList<TCComponentBOMLine>) dataMap.getListValue("MOVE_LIST");

            for (TCComponentBOMLine targetBOMLine : targetBOMList) {
                TableItem rowItem = new TableItem(targetTable, SWT.NONE);
                String itemId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                String revId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                String objectName = targetBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
                rowItem.setText(0, itemId);
                rowItem.setText(1, revId);
                rowItem.setText(2, objectName);
                rowItem.setData(targetBOMLine);
            }

            for (TCComponentBOMLine moveBOMLine : moveBOMList) {
                TableItem rowItem = new TableItem(sourceTable, SWT.NONE);
                String itemId = moveBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                String revId = moveBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                String objectName = moveBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
                rowItem.setText(0, itemId);
                rowItem.setText(1, revId);
                rowItem.setText(2, objectName);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {

    }

    public class InitOperation extends AbstractSDVInitOperation {

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {

            try {
                IDataMap displayDataMap = new RawDataMap();
                ArrayList<TCComponentBOMLine> targetBOMList = new ArrayList<TCComponentBOMLine>(); // target BOP Line 리스트
                ArrayList<TCComponentBOMLine> moveBOMList = new ArrayList<TCComponentBOMLine>(); // 이동 될 BOP Line 리스트

                MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                TCComponentBOMLine topLine = mfgApp.getBOMWindow().getTopBOMLine();

                /**
                 * Shop의 하위 Line 리스트
                 */
                AIFComponentContext[] aifContexts = topLine.getChildren();
                TCAccessControlService aclService = topLine.getSession().getTCAccessControlService();

                for (AIFComponentContext aifcontext : aifContexts) {
                    TCComponentBOMLine comp = (TCComponentBOMLine) aifcontext.getComponent();
                    String itemId = comp.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    // 미할당 라인 제외
                    if (itemId.indexOf("TEMP") > 0)
                        continue;
                    TCComponentBOMViewRevision bomViewRevision = SDVBOPUtilities.getBOMViewRevision(comp.getItemRevision(), "view");
                    if (bomViewRevision == null)
                        continue;
                    boolean isWriteBOMLine = aclService.checkPrivilege(bomViewRevision, TCAccessControlService.WRITE);
                    // BOMLine 쓰기 권한 없으면 제외
                    if (!isWriteBOMLine)
                        continue;
                    targetBOMList.add(comp);
                }
                /**
                 * 이동될 BOP Line 리스트
                 */
                TCComponentBOMLine[] selectedBOMLines = mfgApp.getSelectedBOMLines();
                for (TCComponentBOMLine selectedBOMLine : selectedBOMLines) {
                    moveBOMList.add(selectedBOMLine);
                }

                displayDataMap.put("TARGET_LIST", targetBOMList, IData.LIST_FIELD);
                displayDataMap.put("MOVE_LIST", moveBOMList, IData.LIST_FIELD);

                DataSet viewDataSet = new DataSet();
                viewDataSet.addDataMap(MoveAssyOPView.this.getId(), displayDataMap);
                setData(viewDataSet);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
