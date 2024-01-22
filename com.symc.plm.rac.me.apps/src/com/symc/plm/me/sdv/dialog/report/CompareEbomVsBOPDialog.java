package com.symc.plm.me.sdv.dialog.report;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.util.SDVSpringContextUtil;
import org.sdv.core.util.UIUtil;

import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.symc.plm.me.sdv.view.report.CompareEbomVsBOPView;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.Registry;

public class CompareEbomVsBOPDialog extends SYMCAbstractDialog {

    private Registry registry;
    private CompareEbomVsBOPView compareEbomVsBOPView;
    private Button compareButton, excelExportButton, closeButton;

    public CompareEbomVsBOPDialog(Shell paramShell, int paramInt) {
        super(paramShell, paramInt);
    }

    public CompareEbomVsBOPDialog(Shell paramShell) {
        super(paramShell);
        setApplyButtonVisible(false);
        setOKButtonVisible(false);
    }

    @Override
    protected void createDialogWindow(Composite paramComposite) {
        super.createDialogWindow(paramComposite);
    }

    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        compareEbomVsBOPView = new CompareEbomVsBOPView(parentScrolledComposite);

        return compareEbomVsBOPView.getComposite();
    }

    @Override
    protected boolean validationCheck() {
        return false;
    }

    @Override
    protected boolean apply() {
        return false;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        registry = Registry.getRegistry("com.ssangyong.common.common");

        compareButton = createButton(parent, 0, "Compare", false);
        compareButton.setImage(registry.getImage("compareReport.ICON"));
        compareButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (compareEbomVsBOPView.validationCheck()) {
                        executeCompareProcess();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageDialog.openError(getShell(), "Error", "Compare EBOM vs BOP For Function Error." + "\n" + ex.getMessage());
                }
            }
        });

        excelExportButton = createButton(parent, 2, "Excel", false);
        excelExportButton.setImage(registry.getImage("Excel.ICON"));
        excelExportButton.setText("Export");
        excelExportButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    Map<String, Object> dataMap = new HashMap<String, Object>();
                    dataMap.put("conditionMap", compareEbomVsBOPView.getConditionInfo());
                    dataMap.put("dataList", compareEbomVsBOPView.getTableDataList());

                    String operationId = "symc.me.report.CompareEBOMVsBOPOperation";
                    ISDVOperation operation = (ISDVOperation) SDVSpringContextUtil.getBean(operationId);
                    operation.setParameter(null, dataMap, null);
                    AIFUtility.getDefaultSession().queueOperation((Job) operation);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageDialog.openError(getShell(), "Error", "Excel Export Error." + "\n" + ex.getMessage());
                }
            }
        });

        closeButton = createButton(parent, 3, IDialogConstants.CLOSE_LABEL, false);
        closeButton.setImage(registry.getImage("Cancel_16.ICON"));
        closeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getShell().close();
            }
        });

        getShell().setText("Compare EBOM VS BOP For Function");
        getShell().setSize(new Point(1000, 800));
        UIUtil.centerToParent(getShell().getParent().getShell(), getShell());
    }

    private void executeCompareProcess() {
        new Job("Compare EBOM vs BOP For Function...") {
            @Override
            protected IStatus run(IProgressMonitor arg0) {
                try {
                    getShell().getDisplay().syncExec(new Runnable() {
                        public void run() {
                            getShell().setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
                            compareEbomVsBOPView.compareEBOMVsBOP();
                        }
                    });
                } catch (final Exception e) {
                    getShell().getDisplay().syncExec(new Runnable() {
                        public void run() {
                            MessageDialog.openError(getShell(), "Error", e.getMessage());
                        }
                    });

                    return Status.CANCEL_STATUS;
                } finally {
                    getShell().getDisplay().syncExec(new Runnable() {
                        public void run() {
                            getShell().setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
                        }
                    });
                }

                return Status.OK_STATUS;
            }
        }.schedule();
    }

}
