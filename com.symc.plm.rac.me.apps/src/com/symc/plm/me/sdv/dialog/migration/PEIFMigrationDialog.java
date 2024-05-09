/**
 * 
 */
package com.symc.plm.me.sdv.dialog.migration;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.utils.StringUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.view.migration.PEIFMigrationViewPane;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCException;

/**
 * Class Name : PEIFMigrationDialog
 * Class Description :
 * 
 * @date 2013. 11. 19.
 * 
 */
public class PEIFMigrationDialog extends Dialog {
    PEIFMigrationViewPane peIFMigrationViewPane;
    TCComponentMfgBvrProcess processLine;
    Button stopButton;
    Button executeButton;

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public PEIFMigrationDialog(Shell parentShell, TCComponentMfgBvrProcess processLine) {
        super(parentShell);
        this.processLine = processLine;
    }

    /**
     * Dialog 상단 Close 버튼을 제거
     */
    @Override
    protected void setShellStyle(int arg0) {
        // Use the following not to show the default close X button in the title bar
        super.setShellStyle(SWT.TITLE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        String addonTitle = "";
        try {
            addonTitle = this.processLine.getItemRevision().toDisplayString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        shell.setText("PE I/F Dialog [" + addonTitle + "]");
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        peIFMigrationViewPane = new PEIFMigrationViewPane(container, SWT.NONE, processLine);
        
        /*
         * 20140417
         *  PE I/F Migration Pop-up 창 Open 시 해당 Line의 Working MECO No. 자동 할당 기능 추가 
         */
        String mecoNoProp = "";

        try {
             mecoNoProp =  processLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
        } catch (TCException e) {
            e.printStackTrace();
        }
        
        String mecoNo = StringUtil.nullToString(mecoNoProp).split("/")[0];
        peIFMigrationViewPane.setMecoTextValue(mecoNo);
        
        return container;
    }

    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        stopButton = createButton(parent, 90, "Stop", true);
        stopButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                peIFMigrationViewPane.stop();
            }
        });
        executeButton = createButton(parent, 100, "Execute", true);
        executeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                peIFMigrationViewPane.execute(getShell(), executeButton, getButton(IDialogConstants.CANCEL_ID));
            }
        });
        // Cancel BUtton을 Close버튼으로 이용
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(1024, 768);
    }
}
