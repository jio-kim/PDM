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
 * Class Description : M Prdoduct에 Product Function을 Sync 하는 Operation
 * 
 * [SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가를 위한 Class 신규 생성
 * 
 */
@SuppressWarnings({ "unused" })
public class SyncFunctionsetVsMProductOperation extends AbstractSDVActionOperation {

    private TCSession tcSession = null;
    private Registry registry = null;

    private TCComponentBOMLine srcTopBomLine = null; // 복사할 원본 Function을 지닌 Top BomLine
    private TCComponentBOMLine targetTopBomLine = null; // 대상 Top Bomline
    private ArrayList<TCComponentItemRevision> srcFunctions;// 복사할 원본 Function 리스트

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
            // // 복사할 원본 Function 리스트 가져오기
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
     * 복사할 원본 Function 리스트를 가져오는 함수
     * API는 작업시간 1시간 이상 소요로 인하여 DB 쿼리를 이용하여 Function 합집합 가져오는 것으로 대체
     * 
     * @param srcBomLine
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private ArrayList<TCComponentItemRevision> getFunctionList(String productID) throws Exception {
        ArrayList<TCComponentItemRevision> functionItemRevList = new ArrayList<TCComponentItemRevision>();
        ArrayList<HashMap<String, String>> searchResult = null;

        // Query 서비스 이용한 Function 검색
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        // SYMCRemoteUtil remote = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
        DataSet ds = new DataSet();
        ds.put("PRODUCT_ID", productID);
        searchResult = (ArrayList<HashMap<String, String>>) remote.execute("com.kgm.service.FunctionService", "serchProductFunction", ds);
        System.out.println(searchResult);

        // 검색 결과로 Function Item 찾기
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
     * 복사할 원본 Function 리스트를 가져오는 함수
     * 작업시간 1시간 이상 소요로 인하여 사용 안함.
     * DB 쿼리를 이용하여 Function 합집합 가져오는 것으로 대체
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
    // // 쌍용 Variant Revision Type 인 경우 재귀호출하여 childBOMLine(Function)을 찾는다.
    // functionBOMLineList.putAll(getFunctionList(childBOMLine));
    // } else if (itemRevType.equals(SYMCClass.S7_FNCPARTREVISIONTYPE)) {
    // // 쌍용 Function Revision Type 인 경우
    // if (!functionBOMLineList.containsKey(itemID)) {
    // // 대상목록에 없는 경우에만 담는다.
    // functionBOMLineList.put(itemID, childBOMLine);
    // }
    // } else {
    // // 그 외 건너뛴다.
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
            // 현재 BOM WINDOW
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

            // 대상 BOMLine
            targetTopBomLine = bomWindow.getTopBOMLine();

            // Function Sync (Function 복사)
            if (srcFunctions != null && srcFunctions.size() > 0) {
                tcSession.setStatus(registry.getString("addingFunction.MSG"));

                // Set<String> keySet = srcFunctions.keySet();
                // for (String functionID : keySet) {
                // // targetTopBomLine에 존재하지 않는 Function만 복사한다.
                // if (!findTargetFunction(targetTopBomLine, functionID)) {
                // TCComponentBOMLine srcFunctionBOMLine = srcFunctions.get(functionID);
                // targetTopBomLine.add(srcFunctionBOMLine, false);
                // }
                // }

                for (TCComponentItemRevision srcFunctionitemRevision : srcFunctions) {
                    String srcFuncItemID = srcFunctionitemRevision.getProperty("item_id");

                    // targetTopBomLine에 존재하지 않는 Function만 복사한다.
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
     * Target BOM Line 하위에 해당 Function이 존재 하는지 검색
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
