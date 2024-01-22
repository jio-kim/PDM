/**
 * 
 */
package com.symc.plm.me.sdv.view.migration;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.sdv.service.migration.job.peif.PEIFTCDataExecuteJob;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.util.Utilities;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

/**
 * Class Name : AbstractMigrationViewPane
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
// public class AbstractMigrationViewPane extends AbstractSDVViewPane {
public class PEIFMigrationViewPane extends Composite {
    private Text filePathText;
    private Button openButton;
    private Button searchMECOButton;
    private Button btnOverride;
    private Tree tree;
    private Text logText;
    private Shell shell;
    private Text mecoText;
    private PEIFTCDataExecuteJob executeJob;
//    private String defaultExcelFolder = "Z:\\TcM_Interface\\Interface";
    private String defaultExcelFolder = "X:\\TcM_Interface\\Interface";
    
    TCComponentMfgBvrProcess processLine;
    private Text startNumText;
    private Label lblNewLabel;
    private Label lblNewLabel_1;
    private Text totalRowCountText;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    // public AbstractMigrationViewPane(Composite parent, int style, String id) {
    // super(parent, style, id);
    public PEIFMigrationViewPane(Composite parent, int style, TCComponentMfgBvrProcess processLine) {
		super(parent, style);
		shell = this.getShell();
		setLayout(new FormLayout());
		this.processLine = processLine;

		Group grpImport = new Group(this, SWT.NONE);
		grpImport.setLayout(new GridLayout(9, false));
		FormData fd_grpImport = new FormData();
		fd_grpImport.top = new FormAttachment(0, 10);
		fd_grpImport.left = new FormAttachment(0, 10);
		fd_grpImport.right = new FormAttachment(100, -10);
		grpImport.setLayoutData(fd_grpImport);

		btnOverride = new Button(grpImport, SWT.CHECK);
		btnOverride.setText("Override");
		btnOverride.setSelection(true); //[SR없음][20150223]shcho, Override 체크박스에 Default로 체크 되도록 변경 (이종화 차장님 요청사항)

		mecoText = new Text(grpImport, SWT.BORDER);
		GridData gd_mecoText = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_mecoText.widthHint = 98;
		mecoText.setLayoutData(gd_mecoText);
		mecoText.setEditable(false);

		searchMECOButton = new Button(grpImport, SWT.NONE);
		searchMECOButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IDialog dialog;
				try
				{
					dialog = UIManager.getDialog(getShell(), "symc.me.bop.SearchMECODlg");
					dialog.open();
					IDataSet datSet = dialog.getSelectDataSetAll();
					if (datSet == null)
						return;
					IDataMap mecoDataMap = datSet.getDataMap("mecoSearch");
					if (mecoDataMap != null && mecoDataMap.containsKey("mecoNo"))
					{
						mecoText.setText(mecoDataMap.getStringValue("mecoNo"));
					}
				} catch (Exception ex)
				{
					ex.printStackTrace();
					MessageDialog.openError(AIFUtility.getActiveDesktop().getShell(), "Error", ex.getMessage());
				}
			}
		});
		searchMECOButton.setText("Search MECO");

		filePathText = new Text(grpImport, SWT.BORDER);
		GridData gd_filePathText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_filePathText.widthHint = 197;
		filePathText.setLayoutData(gd_filePathText);
		filePathText.setText(defaultExcelFolder);
		filePathText.setEditable(false);

		openButton = new Button(grpImport, SWT.NONE);
        openButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    filePeIfOpen();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        openButton.setText("Open");

        Group importGroup = new Group(this, SWT.NONE);
        fd_grpImport.bottom = new FormAttachment(importGroup, -6);
        
        lblNewLabel = new Label(grpImport, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
        lblNewLabel.setText("\uC2DC\uC791\uC21C\uC11C:");
        
        startNumText = new Text(grpImport, SWT.BORDER | SWT.RIGHT);
        startNumText.setToolTipText("\uC624\uB958\uAC00 \uBC1C\uC0DD\uD560 \uACBD\uC6B0 \uC0AC\uC6A9.\r\n\uC624\uB958 \uB85C\uADF8\uC5D0\uC11C \uC624\uB958\uAC00 \uBC1C\uC0DD\uD55C \uBC88\uD638\uB97C \uADF8\uB300\uB85C \uC785\uB825\uD558\uBA74 \uB428.\r\n\uD558\uB098\uB9CC \uC2E4\uD589\uD558\uACE0 \uC2F6\uB2E4\uBA74 \uB4A4\uCABD text\uC5D0 +1 \uB41C \uAC12\uC744 \uB123\uC5B4\uC8FC\uBA74 \uB428.\r\n\uC608) 124 ~ 125 (124 1\uAC1C\uB9CC \uC2E4\uD589\uB428)\r\n     124 ~ 126 (124, 125 2\uAC1C \uC2E4\uD589\uB428)\r\n\uB4A4\uC5D0 \uC544\uBB34\uAC83\uB3C4 \uC548\uC801\uC73C\uBA74 \uB05D\uAE4C\uC9C0 \uC2E4\uD589\uB428.");
        GridData gd_startNumText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_startNumText.widthHint = 60;
        startNumText.setLayoutData(gd_startNumText);
        
        lblNewLabel_1 = new Label(grpImport, SWT.NONE);
        lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblNewLabel_1.setText("~");
        
        totalRowCountText = new Text(grpImport, SWT.BORDER | SWT.RIGHT);
        totalRowCountText.setToolTipText("\uC624\uB958\uAC00 \uBC1C\uC0DD\uD560 \uACBD\uC6B0 \uC0AC\uC6A9.\r\n\uC624\uB958 \uB85C\uADF8\uC5D0\uC11C \uC624\uB958\uAC00 \uBC1C\uC0DD\uD55C \uBC88\uD638\uB97C \uADF8\uB300\uB85C \uC785\uB825\uD558\uBA74 \uB428.\r\n\uD558\uB098\uB9CC \uC2E4\uD589\uD558\uACE0 \uC2F6\uB2E4\uBA74 \uB4A4\uCABD text\uC5D0 +1 \uB41C \uAC12\uC744 \uB123\uC5B4\uC8FC\uBA74 \uB428.\r\n\uC608) 124 ~ 125 (124 1\uAC1C\uB9CC \uC2E4\uD589\uB428)\r\n     124 ~ 126 (124, 125 2\uAC1C \uC2E4\uD589\uB428)\r\n\uB4A4\uC5D0 \uC544\uBB34\uAC83\uB3C4 \uC548\uC801\uC73C\uBA74 \uB05D\uAE4C\uC9C0 \uC2E4\uD589\uB428.");
        GridData gd_totalRowCountText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_totalRowCountText.widthHint = 60;
        totalRowCountText.setLayoutData(gd_totalRowCountText);
        importGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
        FormData fd_importGroup = new FormData();
        fd_importGroup.top = new FormAttachment(0, 108);
        fd_importGroup.left = new FormAttachment(0, 10);
        fd_importGroup.right = new FormAttachment(100, -10);
        importGroup.setLayoutData(fd_importGroup);

        tree = new Tree(importGroup, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        Group logGroup = new Group(this, SWT.NONE);
        fd_importGroup.bottom = new FormAttachment(logGroup, -6);
        logGroup.setText("Log");
        logGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
        FormData fd_logGroup = new FormData();
        fd_logGroup.top = new FormAttachment(0, 447);
        fd_logGroup.bottom = new FormAttachment(100, -10);
        fd_logGroup.left = new FormAttachment(0, 10);
        fd_logGroup.right = new FormAttachment(100, -10);
        logGroup.setLayoutData(fd_logGroup);
        logText = new Text(logGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public void filePeIfOpen() throws Exception {
        DirectoryDialog directoryDialog = new DirectoryDialog(shell);
        directoryDialog.setFilterPath(defaultExcelFolder);
        directoryDialog.setMessage("Please select a directory and click OK");
        filePathText.setText(directoryDialog.open());
    }

    /**
     * 실행..
     * 
     * @method execute
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void execute(final Shell shell, final Button executeButton, final Button cancelButton) {
        if (StringUtils.isEmpty(mecoText.getText())) {
            MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", "MECO를 선택하세요.");
            mecoText.setFocus();
            return;
        }
        if (StringUtils.isEmpty(filePathText.getText())) {
            MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", "작업폴더를 선택하세요.");
            filePathText.setFocus();
            return;
        }
        // Log 초기화 및 생성
        createLog();
        // Dialog Button Disabled
        executeButton.setEnabled(false);
        cancelButton.setEnabled(false);
        setEnableButtons(false);
        executeJob = new PEIFTCDataExecuteJob(shell, "PE I/F Excete...", tree, logText, processLine, filePathText.getText(), mecoText.getText(), btnOverride.getSelection(), startNumText.getText(), totalRowCountText.getText());
        executeJob.addJobChangeListener(new JobChangeAdapter() {
            public void done(IJobChangeEvent event) {
                if (event.getResult().isOK()) {
                    shell.getDisplay().syncExec(new Runnable() {
                        public void run() {
                            MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", "완료!!");
                            // Dialog Button Enable
                            executeButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            setEnableButtons(true);
                        }
                    });
                } else {
                    shell.getDisplay().syncExec(new Runnable() {
                        public void run() {
                            // Dialog Button Enable
                            executeButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            setEnableButtons(true);
                        }
                    });
                }
            }
        });
        // execute job
        executeJob.schedule();
    }

    public void stop() {
        if (executeJob != null) {
            executeJob.setStopflag(true);
        }
    }

    /**
     * Log 초기화 및 생성
     * 
     * @method createLog
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createLog() {
        // Log Clear
        logText.setText("");
    }

    /**
     * @return the openButton
     */
    public void setEnableButtons(final boolean check) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                openButton.setEnabled(check);
                searchMECOButton.setEnabled(check);
                btnOverride.setEnabled(check);
            }
        });
    }
    

    public String getMecoTextValue() {
        return mecoText.getText();
    }

    public void setMecoTextValue(String mecoNo) {
        this.mecoText.setText(mecoNo);
    }
}
