/**
 * 
 */
package com.symc.plm.me.sdv.operation.report;

import org.sdv.core.common.IDialog;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.excel.transformer.DownloadSWMDocExcelTransformer;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Class Name : ReportDownloadSWMDocOperation
 * Class Description : 표준작업요령서 검색 결과 Report(표준작업요령 관리대장 출력)
 * 
 * @date 2013. 12. 6.
 * 
 */
public class ReportDownloadSWMDocOperation extends AbstractSDVActionOperation {

    public ReportDownloadSWMDocOperation(String actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        int mode = ExcelTemplateHelper.EXCEL_SAVE;
        String templatePreference = "M7_TEM_DocItemID_StdWorkMethodList";
        String defaultFileName = "표준작업요령 관리대장" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");

        DownloadSWMDocExcelTransformer downloadSWMDocExcelTransformer = new DownloadSWMDocExcelTransformer();
        downloadSWMDocExcelTransformer.print(mode, templatePreference, defaultFileName, getData());
    }

    protected IDataSet getData() throws Exception {
        IDialog dialog = UIManager.getDialog(AIFUtility.getActiveDesktop().getShell(), "symc.dialog.SearchSWMDocDialog");

        IDataSet dataset = new DataSet();
        IDataMap dataMap = new RawDataMap();

        dataMap.put("operationList", ((AbstractSDVViewPane) dialog.getView("searchListSWMDocView")).getLocalDataMap().getTableValue("operationList"), IData.TABLE_FIELD);
        dataset.addDataMap("downloadList", dataMap);

        return dataset;
    }

}
