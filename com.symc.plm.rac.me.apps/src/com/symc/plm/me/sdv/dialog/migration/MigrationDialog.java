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

import com.symc.plm.me.sdv.view.migration.ExcelMigrationViewPane;

/**
 * Class Name : MigrationDialog
 * Class Description : 
 * @date 2013. 11. 19.
 *
 */
public class MigrationDialog extends Dialog {
    ExcelMigrationViewPane excelMigrationViewPane;
    /**
     * Create the dialog.
     * @param parentShell
     */
    public MigrationDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));        
        excelMigrationViewPane = new ExcelMigrationViewPane(container, SWT.NONE);
        
        return container;
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
        Button validateButton = createButton(parent, 2, "Validate", true);
        validateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                excelMigrationViewPane.validate();
            }
        });
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(640, 570);
    }

}
