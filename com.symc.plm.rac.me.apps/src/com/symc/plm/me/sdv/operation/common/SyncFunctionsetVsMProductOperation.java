/**
 * 
 */
package com.symc.plm.me.sdv.operation.common;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.SYMCClass;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SyncFunctionsetVsMProductOperation
 * Class Description : M Prdoduct�� Product Function�� Sync �ϴ� Operation
 * 
 * [SR140724-013][20140725] shcho, Product�� M-Product Function Sync ��� �߰��� ���� Class �ű� ����
 * 
 */
@SuppressWarnings({ "unused" })
public class SyncFunctionsetVsMProductOperation extends AbstractSDVActionOperation {

    private TCSession tcSession = null;
    private Registry registry = null;

    private TCComponentBOMLine srcTopBomLine = null; // ������ ���� Function�� ���� Top BomLine
    private TCComponentBOMLine targetTopBomLine = null; // ��� Top Bomline
    private ArrayList<TCComponentItemRevision> srcFunctions;// ������ ���� Function ����Ʈ

    private IDataSet dataSet = null;
    private boolean isValidOK = true;

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public SyncFunctionsetVsMProductOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.operation.common.common");
    }

    @Override
    public void startOperation(String commandId) {
        try {
            tcSession = (TCSession) getSession();
            tcSession.setStatus(registry.getString("FindingFunction.MSG"));

            dataSet = getDataSet();
            // Product ItemRevision
            TCComponentItemRevision srcItemRevision = (TCComponentItemRevision) dataSet.getValue("syncFunction", "SRC_PRODUCT_REV");
            srcFunctions = getFunctionList(srcItemRevision.getProperty("item_id"));
            // // Product Top BOM Window
            // TCComponentBOMWindow srcTopBomWindow = SDVBOPUtilities.getBOMWindow(srcItemRevision, "Latest Working", "bom_view");
            // // Product Top BOMLINE
            // srcTopBomLine = srcTopBomWindow.getTopBOMLine();
            // // ������ ���� Function ����Ʈ ��������
            // srcFunctions = getFunctionList(srcTopBomLine);

            if (srcFunctions == null || srcFunctions.size() == 0) {
                MessageBox.post(registry.getString("NotFunctionDefine.MSG"), registry.getString("Warning.NAME"), MessageBox.WARNING);
                isValidOK = false;
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setAbortRequested(true);
        }

        isValidOK = true;
    }

    /**
     * ������ ���� Function ����Ʈ�� �������� �Լ�
     * API�� �۾��ð� 1�ð� �̻� �ҿ�� ���Ͽ� DB ������ �̿��Ͽ� Function ������ �������� ������ ��ü
     * 
     * @param srcBomLine
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private ArrayList<TCComponentItemRevision> getFunctionList(String productID) throws Exception {
        ArrayList<TCComponentItemRevision> functionItemRevList = new ArrayList<TCComponentItemRevision>();
        ArrayList<HashMap<String, String>> searchResult = null;

        // Query ���� �̿��� Function �˻�
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        // SYMCRemoteUtil remote = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
        DataSet ds = new DataSet();
        ds.put("PRODUCT_ID", productID);
        searchResult = (ArrayList<HashMap<String, String>>) remote.execute("com.kgm.service.FunctionService", "serchProductFunction", ds);
        System.out.println(searchResult);

        // �˻� ����� Function Item ã��
        for (HashMap<String, String> functionIDMap : searchResult) {
            String functionID = functionIDMap.get("ITEM_ID");
            String functionRevID = functionIDMap.get("ITEM_REV_ID");
            // TCComponentItem functionItem = CustomUtil.findItem(SYMCClass.S7_FNCPARTTYPE, functionID);
            TCComponentItemRevision functionItemRev = CustomUtil.findItemRevision(SYMCClass.S7_FNCPARTREVISIONTYPE, functionID, functionRevID);
            functionItemRevList.add(functionItemRev);
        }

        return functionItemRevList;
    }

    /**
     * ������ ���� Function ����Ʈ�� �������� �Լ�
     * �۾��ð� 1�ð� �̻� �ҿ�� ���Ͽ� ��� ����.
     * DB ������ �̿��Ͽ� Function ������ �������� ������ ��ü
     * 
     * @param srcBomLine
     * @return
     * @throws Exception
     */
    // private LinkedHashMap<String, TCComponentBOMLine> getFunctionList(TCComponentBOMLine srcBomLine) throws Exception {
    // LinkedHashMap<String, TCComponentBOMLine> functionBOMLineList = new LinkedHashMap<String, TCComponentBOMLine>();
    // AIFComponentContext[] childrenAIFCompContext = srcBomLine.getChildren();
    // if (childrenAIFCompContext.length > 0) {
    // for (AIFComponentContext childAIFCompContext : childrenAIFCompContext) {
    // TCComponentBOMLine childBOMLine = (TCComponentBOMLine) childAIFCompContext.getComponent();
    // TCComponentItemRevision itemRevision = childBOMLine.getItemRevision();
    // String itemRevType = itemRevision.getTypeComponent().toString();
    // String itemID = itemRevision.getProperty("item_id");
    // if (itemRevType.equals(SYMCClass.S7_VARIANTPARTREVISIONTYPE)) {
    // // �ֿ� Variant Revision Type �� ��� ���ȣ���Ͽ� childBOMLine(Function)�� ã�´�.
    // functionBOMLineList.putAll(getFunctionList(childBOMLine));
    // } else if (itemRevType.equals(SYMCClass.S7_FNCPARTREVISIONTYPE)) {
    // // �ֿ� Function Revision Type �� ���
    // if (!functionBOMLineList.containsKey(itemID)) {
    // // ����Ͽ� ���� ��쿡�� ��´�.
    // functionBOMLineList.put(itemID, childBOMLine);
    // }
    // } else {
    // // �� �� �ǳʶڴ�.
    // continue;
    // }
    // }
    // }
    //
    // return functionBOMLineList;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        if (!isValidOK)
            return;
        try {
            // MPPAppication
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            // ���� BOM WINDOW
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

            // ��� BOMLine
            targetTopBomLine = bomWindow.getTopBOMLine();

            // Function Sync (Function ����)
            if (srcFunctions != null && srcFunctions.size() > 0) {
                tcSession.setStatus(registry.getString("addingFunction.MSG"));

                // Set<String> keySet = srcFunctions.keySet();
                // for (String functionID : keySet) {
                // // targetTopBomLine�� �������� �ʴ� Function�� �����Ѵ�.
                // if (!findTargetFunction(targetTopBomLine, functionID)) {
                // TCComponentBOMLine srcFunctionBOMLine = srcFunctions.get(functionID);
                // targetTopBomLine.add(srcFunctionBOMLine, false);
                // }
                // }

                for (TCComponentItemRevision srcFunctionitemRevision : srcFunctions) {
                    String srcFuncItemID = srcFunctionitemRevision.getProperty("item_id");

                    // targetTopBomLine�� �������� �ʴ� Function�� �����Ѵ�.
                    if (!findTargetFunction(targetTopBomLine, srcFuncItemID)) {
                        targetTopBomLine.add(null, srcFunctionitemRevision, null, false);
                    }
                }
            }
        } catch (Exception ex) {
            setAbortRequested(true);
            isValidOK = false;
            Dialog dialog = (Dialog) UIManager.getAvailableDialog("symc.me.bop.SyncFunctionsetVsMProductDialog");
            Shell shell = dialog.getShell();
            MessageBox.post(shell, ex.getMessage(), registry.getString("Inform.NAME"), MessageBox.INFORMATION);
            throw ex;
        }

    }

    /**
     * Target BOM Line ������ �ش� Function�� ���� �ϴ��� �˻�
     * 
     * @param bomLine
     * @param srcFuncItemID
     * @throws TCException
     */
    public boolean findTargetFunction(TCComponentBOMLine bomLine, String srcFuncItemID) throws TCException {
        AIFComponentContext[] childrenCompContexts = bomLine.getChildren();
        boolean isExist = false;
        for (AIFComponentContext childCompContext : childrenCompContexts) {
            TCComponentBOMLine childTargetBomLine = (TCComponentBOMLine) childCompContext.getComponent();
            String targetFuncItemID = childTargetBomLine.getItem().getProperty("item_id");
            if (srcFuncItemID.equals(targetFuncItemID)) {
                isExist = true;
            }
        }
        return isExist;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
        if (!isValidOK)
            return;
        MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("Complete.MSG"), registry.getString("Inform.NAME"), MessageBox.INFORMATION);
    }

}
