package org.sdv.core.ui.operation;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.IExcelTransformer;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;


public abstract class ExcelSDVOperation extends AbstractSDVOperation {
    public static final int SDV_EXCEL_OPEN = 0;
    public static final int SDV_EXCEL_SAVE = 1;

    public String operationId;
    public int mode;
    public String templatePreference;
    public IExcelTransformer formatter;

    protected XSSFWorkbook workbook;

    public ExcelSDVOperation(String jobName) {
        super(jobName);
    }

    public final String getOperationId() {
        return operationId;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getTemplatePreference() {
        return templatePreference;
    }

    public void setTemplatePreference(String templatePreference) {
        this.templatePreference = templatePreference;
    }

    public IExcelTransformer getFormatter() {
        return formatter;
    }

    public void setFormatter(IExcelTransformer formatter) {
        this.formatter = formatter;
    }

    /**
     * @param operationId
     *            the operationId to set
     */
    public final void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            beforeExecuteOperation();
            executeExcelOperation();
            afterExecuteOperation();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "ERROR", MessageBox.ERROR);
        }
    }

    public void beforeExecuteOperation() throws Exception {

        if(this.mode == SDV_EXCEL_OPEN) {

        }
    }

    abstract public void executeExcelOperation() throws Exception;

    abstract public void afterExecuteOperation() throws Exception;

}
