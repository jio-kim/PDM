package com.kgm.commands.mig;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.mig.exception.MigrationException;
import com.kgm.commands.mig.service.MigrationValidationService;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class MigrationDialog extends Dialog {

    public Object result;
    public Shell shell;
    public Registry registry;
    public Text progressingText;
    public Combo comboObjectType;
    public Combo comboBatchType;
    public Combo comboSeqType;
    public Button btnValidation;
    public Button btnExecutingJob;
    public Button btnClose;
    public ProgressBar progressBar;
    public MigrationDialog migrationDialog;
    public MigrationValidationService migrationValidationService;    

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public MigrationDialog(Shell parent, int style) {
        super(parent, style);
    }

    /**
     * Open the dialog.
     * 
     * @return the result
     */
    public Object open() {
        this.migrationDialog = this;
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.registry = Registry.getRegistry(this);
        // this.shell = new Shell(parent.getDisplay(), style);
        shell = new Shell(getParent(), getStyle());
        shell.setSize(500, 441);
        shell.setText(StringUtil.getTextBundle(this.registry, "title", null, this.getClass()));
        Rectangle screen = this.shell.getDisplay().getMonitors()[0].getBounds();
        Rectangle shellBounds = this.shell.getBounds();
        this.shell.setBounds((screen.width - shellBounds.width) / 2, (screen.height - shellBounds.height) / 2, shellBounds.width, shellBounds.height);
        shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        shell.setBackgroundMode(SWT.INHERIT_FORCE);
        createUI();
        addListener();
        // this.checkDialogOpen(); // Open전 체크
    }

    /**
     * 
     * 
     * @method createUI
     * @date 2013. 2. 6.
     * @param
     * @return
     * @exception
     * @throws
     * @see
     */
    private void createUI() {
        Label lblObjectType = new Label(shell, SWT.NONE);
        lblObjectType.setBounds(10, 27, 81, 25);
        lblObjectType.setText(StringUtil.getTextBundle(this.registry, "objectTypeLabel", null, this.getClass()));

        Label lblSeqType = new Label(shell, SWT.NONE);
        lblSeqType.setText(StringUtil.getTextBundle(this.registry, "seqTypeLabel", null, this.getClass()));
        lblSeqType.setBounds(10, 54, 67, 25);
        
        Label lblBatchType = new Label(shell, SWT.NONE);
        lblBatchType.setText(StringUtil.getTextBundle(this.registry, "batchTypeLabel", null, this.getClass()));
        lblBatchType.setBounds(10, 86, 67, 25);       

        this.comboObjectType = new Combo(shell, SWT.READ_ONLY);
        this.comboObjectType.setItems(new String[] { "MIG_PRODUCT", "MIG_PROJECT", "MIG_ENGDOC", "MIG_FILE", "MIG_FUNCTION", "MIG_MATERIAL", "MIG_STDPART", "MIG_VARIANT", "MIG_VEHPART" });
        this.comboObjectType.select(0);
        this.comboObjectType.setBounds(97, 27, 187, 25);        
        
        this.comboSeqType = new Combo(shell, SWT.READ_ONLY);
        this.comboSeqType.setItems(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"});        
        this.comboSeqType.setBounds(97, 54, 187, 25);
        this.comboSeqType.select(0);
        
        this.comboBatchType = new Combo(shell, SWT.READ_ONLY);
        this.comboBatchType.setItems(new String[] { "Create", "Modify" });
        this.comboBatchType.select(0);
        this.comboBatchType.setBounds(97, 83, 187, 23);
        this.comboBatchType.setEnabled(false);

        this.btnValidation = new Button(shell, SWT.NONE);
        this.btnValidation.setBounds(309, 54, 76, 25);
        this.btnValidation.setText(StringUtil.getTextBundle(this.registry, "validationButton", null, this.getClass()));

        this.btnExecutingJob = new Button(shell, SWT.NONE);
        this.btnExecutingJob.setText(StringUtil.getTextBundle(this.registry, "executingJobButton", null, this.getClass()));
        this.btnExecutingJob.setBounds(391, 54, 93, 25);
        this.btnExecutingJob.setEnabled(false);

        Group grpMonitoringProgress = new Group(shell, SWT.NONE);
        grpMonitoringProgress.setText(StringUtil.getTextBundle(this.registry, "monitoringProgressGroup", null, this.getClass()));
        grpMonitoringProgress.setBounds(10, 142, 474, 231);

        progressingText = new Text(grpMonitoringProgress, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        progressingText.setBounds(10, 22, 454, 173);
        progressingText.setEditable(false);

        this.progressBar = new ProgressBar(grpMonitoringProgress, SWT.HORIZONTAL | SWT.SMOOTH);
        this.progressBar.setBounds(10, 204, 454, 17);
        this.progressBar.setMinimum(0);
        this.progressBar.setMinimum(100);

        this.btnClose = new Button(shell, SWT.NONE);
        this.btnClose.setText(StringUtil.getTextBundle(this.registry, "closeButton", null, this.getClass()));
        this.btnClose.setBounds(212, 379, 76, 25);     
       
    }

    /**
     * Event 등록
     * 
     * @method addListener
     * @date 2013. 2. 6.
     * @param
     * @return
     * @exception
     * @throws
     * @see
     */
    private void addListener() {
        // Validation Button
        this.btnValidation.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                final String objectType = migrationDialog.comboObjectType.getItem(migrationDialog.comboObjectType.getSelectionIndex());
                final String batchType = migrationDialog.comboBatchType.getItem(migrationDialog.comboBatchType.getSelectionIndex());
                final String seqType = migrationDialog.comboSeqType.getItem(migrationDialog.comboSeqType.getSelectionIndex());
                new Job("validationJob") {
                    @Override
                    protected IStatus run(IProgressMonitor arg0) {
                        try {
                            migrationValidationService = new MigrationValidationService(migrationDialog);
                            migrationValidationService.validation(objectType, batchType, seqType);
                        } catch (MigrationException me) {
                            MessageBox.post(shell, me.getMessage(), "Notification", 2);
                        } catch (Exception ue) {
                            ue.printStackTrace();
                            MessageBox.post(shell, "import Unknown Exception!", "Notification", 2);
                        }
                        return Status.OK_STATUS;
                    }
                }.schedule();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // Executing Job Button
        this.btnExecutingJob.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                final String objectType = migrationDialog.comboObjectType.getItem(migrationDialog.comboObjectType.getSelectionIndex());
                final String batchType = migrationDialog.comboBatchType.getItem(migrationDialog.comboBatchType.getSelectionIndex());
                final String seqType = migrationDialog.comboSeqType.getItem(migrationDialog.comboSeqType.getSelectionIndex());
                if(migrationValidationService != null) {
                    new Job("executingJob") {
                        @Override
                        protected IStatus run(IProgressMonitor arg0) {
                            try {                                
                                migrationValidationService.executingJob(objectType, batchType, seqType);
                            } catch (MigrationException me) {
                                MessageBox.post(shell, me.getMessage(), "Notification", 2);
                            } catch (Exception ue) {
                                ue.printStackTrace();
                                MessageBox.post(shell, "import Unknown Exception!", "Notification", 2);
                            }
                            return Status.OK_STATUS;
                        }
                    }.schedule();
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // 닫기 버튼
        this.btnClose.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // dialog Close
                shell.dispose();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        this.comboObjectType.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                progressingText.setText("");
                progressBar.setSelection(0);
                btnExecutingJob.setEnabled(false);                
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
          });
    }

    /**
     * Validation Button 제어
     * 
     * @method validationBtnenable
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void validationBtnenable(boolean enabled) {
        this.btnValidation.setEnabled(enabled);
    }

    /**
     * Dialog Open전 사전체크
     * 
     * @method checkDialogOpen
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    // private void checkDialogOpen() {
    // TCComponentBOMLine[] targetBOMLines = null;
    // InterfaceAIFComponent[] targetComps =
    // AIFUtility.getCurrentApplication().getTargetComponents();
    // if (targetComps == null || targetComps.length == 0) {
    // this.msgFnItemOpenStr();
    // }
    // targetBOMLines = new TCComponentBOMLine[targetComps.length];
    // for (int i = 0; i < targetComps.length; i++) {
    // if (!(targetComps[i] instanceof TCComponentBOMLine)) {
    // this.msgFnItemOpenStr();
    // }
    // targetBOMLines[i] = (TCComponentBOMLine) targetComps[i];
    // }
    // // BOMLine Load 체크
    // if (targetBOMLines == null || targetBOMLines.length == 0) {
    // this.msgFnItemOpenStr();
    // }
    // TCComponentBOMLine topLine;
    // try {
    // topLine = targetBOMLines[0].window().getTopBOMLine();
    // // Function Item 체크
    // if (!"S7_Function".equals(topLine.getItem().getType())) {
    // MessageBox.post(shell,
    // StringUtil.getTextBundle(this.registry, "needFunctionItemType", null,
    // this.getClass()), "Notification", 2);
    // shell.dispose();
    // }
    // } catch (TCException e) {
    // e.printStackTrace();
    // MessageBox.post(shell, "Teamcenter Error!",
    // "Notification", 2);
    // shell.dispose();
    // }
    // }

}
