package com.symc.plm.me.sdv.operation;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IExcelTransformer;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

import common.Logger;

public abstract class SimpleSDVExcelOperation extends AbstractTCSDVOperation {

    private static final Logger logger = Logger.getLogger(SimpleSDVExcelOperation.class);
    private Registry registry = Registry.getRegistry(this);

    public static final int FAIL = -1;
    public static final int SUCCESS = 0;

    // 처리 결과와 오류를 저장하는 필드 (기본은 성공을 가르키고 오류가 날경우 FAIL로 등록한다.)
    private int executeResult = SUCCESS;
    private String errorMessage;
    private Throwable error;

    protected int mode;
    protected String dialogId;
    protected String templatePreference;
    protected IExcelTransformer transformer;
    protected String titleDescription;
    protected IDataMap localDataMap;

    @Override
    public void startOperation(String commandId) {
        final Shell shell = AIFUtility.getActiveDesktop().getShell();

        shell.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                int returnValue = -1;

                try {
                    IDialog dialog = UIManager.getDialog(shell, dialogId);
                    ((DialogStubBean) dialog.getStub()).setDescription(titleDescription);
                    returnValue = dialog.open();

                    // 공법(작업표준서) 마스터인 경우
                    if(dialog.getView("opMasterListOptionView") != null) {
                        localDataMap = ((AbstractSDVViewPane) dialog.getView("opMasterListOptionView")).getLocalDataMap();
                    // 조립 공정 편성표 일 경우(선택창 정보 가져옴)
                    } else if(dialog.getView("selectAssyLineBananceView") != null) {
                        localDataMap = ((AbstractSDVViewPane) dialog.getView("selectAssyLineBananceView")).getLocalDataMap();
                    } 
                    // Compare E-BOM vs BOP일 경우 Function 선택창 정보 가져옴
                    else if(dialog.getView("selectFunctionView") != null) {
                    	localDataMap = ((AbstractSDVViewPane) dialog.getView("selectFunctionView")).getLocalDataMap();
                    }

                    if(returnValue == 0 || returnValue == 1) {
                        logger.debug("User Cancled.");
                        cancel();
                    } else if(returnValue == 2) {
                        // Excel Save
                        mode = 1;
                    } else if(returnValue == 3) {
                        // Excel Open
                        mode = 0;
                    }
                } catch(Exception e) {
                    logger.error(e);
                    returnValue = -1;
                    cancel();
                    MessageBox.post(shell, getErrorMessage(), "Report", MessageBox.ERROR);
                }
            }
        });
    }

    @Override
    public void endOperation() {
        if(mode == 1 && ExcelTemplateHelper.exportPath == null) {
            logger.debug("User Cancled.");
            cancel();

            // executeResult 초기화
            setExecuteResult(SUCCESS);

            return;
        }

        if(getExecuteResult() == FAIL) {
            try {
                Throwable throwable = getExecuteError();
                if(throwable != null) {
                    logger.error(throwable);
                }
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), getErrorMessage(), "Report", MessageBox.ERROR);
            } catch(Exception ex) {
                logger.error(ex);
            }

            // executeResult 초기화
            setExecuteResult(SUCCESS);

            return;
        }

        if(transformer.getTemplateFile() != null) {
            // Excel Open
            if(mode == 0) {
                transformer.openFile();
                return;
            } else {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", "Complete the export operation.");
                    }
                });
            }
        }
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public IExcelTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(IExcelTransformer transformer) {
        this.transformer = transformer;
    }

    public String getTemplatePreference() {
        return templatePreference;
    }

    public void setTemplatePreference(String templatePreference) {
        this.templatePreference = templatePreference;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getTitleDescription() {
        return titleDescription;
    }

    public void setTitleDescription(String titleDescription) {
        this.titleDescription = titleDescription;
    }

    public IDataMap getLocalDataMap() {
        return localDataMap;
    }

    public void setLocalDataMap(IDataMap localDataMap) {
        this.localDataMap = localDataMap;
    }

    public int getExecuteResult() {
        return executeResult;
    }

    protected void setExecuteResult(int result) {
        this.executeResult = result;
    }

    public Throwable getExecuteError() {
        return this.error;
    }

    protected void setExecuteError(Throwable th) {
        this.error = th;
        setExecuteResult(FAIL);
    }

    public String getErrorMessage() {
        return (this.errorMessage != null) ? this.errorMessage : registry.getString("report.defaultError");
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected abstract IDataSet getData() throws Exception;

}
