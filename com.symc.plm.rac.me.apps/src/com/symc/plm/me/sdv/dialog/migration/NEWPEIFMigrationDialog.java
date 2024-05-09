/**
 * 
 */
package com.symc.plm.me.sdv.dialog.migration;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Synchronizer;

import com.kgm.common.utils.StringUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.view.migration.NEWPEIFMigrationViewPane;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCException;

/**
 * Class Name : PEIFMigrationDialog
 * Class Description :
 * 
 * @date 2013. 11. 19.
 * 
 */
public class NEWPEIFMigrationDialog extends Dialog implements SelectionListener{
	
    NEWPEIFMigrationViewPane peIFMigrationViewPane;
    TCComponentMfgBvrProcess processLine;
    Button stopButton;
    Button validationButton;
    Button executeButton;

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public NEWPEIFMigrationDialog(Shell parentShell, TCComponentMfgBvrProcess processLine) {
        super(parentShell);
        this.processLine = processLine;
    }

	/**
     * Dialog 상단 Close 버튼을 제거
     */
    @Override
    protected void setShellStyle(int arg0) {
        // Use the following not to show the default close X button in the title bar
        //super.setShellStyle(SWT.TITLE | SWT.APPLICATION_MODAL);
        super.setShellStyle(SWT.TITLE );
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
        shell.setText("PE-To TC Migration [" + addonTitle + "]");
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
        peIFMigrationViewPane = new NEWPEIFMigrationViewPane(container, SWT.NONE, processLine);
        
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
        stopButton.addSelectionListener(this);
        
        validationButton = createButton(parent, 100, "Validation", true);
        validationButton.addSelectionListener(this);
        
        executeButton = createButton(parent, 110, "Execute", true);
        executeButton.addSelectionListener(this);
        
        // Cancel BUtton을 Close버튼으로 이용
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
        
		Synchronizer aSynchronizer =  new Synchronizer(Display.getDefault());
		Display.getCurrent().setSynchronizer(aSynchronizer);

    }
    
    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(1024, 768);
    }

    /**
     * 생성된 Button의 Event 처리
     */
	@Override
	public void widgetSelected(SelectionEvent paramSelectionEvent) {
		if(executeButton!=null && paramSelectionEvent.getSource().equals(executeButton)==true){
			
			Display.getDefault().asyncExec(new Runnable() {
			//Display.getDefault().syncExec(new Runnable() {
			    public void run() {
			    	peIFMigrationViewPane.execute(getShell(), validationButton, executeButton, getButton(IDialogConstants.CANCEL_ID));
			    }
			});
		}else if(validationButton!=null && paramSelectionEvent.getSource().equals(validationButton)==true){
			
			Display.getDefault().asyncExec(new Runnable() {
			//Display.getDefault().syncExec(new Runnable() {
			    public void run() {
			    	peIFMigrationViewPane.validation(getShell(), validationButton, executeButton, getButton(IDialogConstants.CANCEL_ID));
			    }
			});
		}else if(stopButton!=null && paramSelectionEvent.getSource().equals(stopButton)==true){
			
			Display.getDefault().asyncExec(new Runnable() {
			    public void run() {
			    	peIFMigrationViewPane.stop();
			    }
			});
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
		
	}
}
