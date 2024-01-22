package com.symc.plm.me.sdv.dialog.meco;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.util.SDVSpringContextUtil;
import org.sdv.core.util.UIUtil;

import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.symc.plm.me.sdv.view.meco.ECOChangeHistoryView;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.Registry;

public class ECOChangeHistoryDialog extends SYMCAbstractDialog {

    private Registry registry;
    private ECOChangeHistoryView ecoChangeHistoryView;
    private Button excelExportButton, closeButton;

    public ECOChangeHistoryDialog(Shell paramShell) {
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
        ecoChangeHistoryView = new ECOChangeHistoryView(parentScrolledComposite);

        return ecoChangeHistoryView.getComposite();
    }

    protected void createButtonsForButtonBar(Composite parent) {
        registry = Registry.getRegistry("com.ssangyong.common.common");

        excelExportButton = createButton(parent, 2, "Excel", false);
        excelExportButton.setImage(registry.getImage("Excel.ICON"));
        excelExportButton.setText("Export");
        excelExportButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (ecoChangeHistoryView.getConditionInfo() != null) {
                        Map<String, Object> dataMap = new HashMap<String, Object>();

                        dataMap.put("conditionMap", ecoChangeHistoryView.getConditionInfo());
                        dataMap.put("dataList", ecoChangeHistoryView.getTableDataList());

                        String operationId = "symc.me.report.ECOChangeHistoryOperation";
                        ISDVOperation operation = (ISDVOperation) SDVSpringContextUtil.getBean(operationId);
                        operation.setParameter(null, dataMap, null);
                        AIFUtility.getDefaultSession().queueOperation((Job) operation);
                    }
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

        getShell().setText("ECO History");
        getShell().setSize(new Point(1000, 800));
        UIUtil.centerToParent(getShell().getParent().getShell(), getShell());
    }

    @Override
    protected boolean validationCheck() {
        return false;
    }

    @Override
    protected boolean apply() {
        return false;
    }

}
