/**
 * 
 */
package com.symc.plm.me.sdv.view.migration;

import org.apache.commons.lang.StringUtils;
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

import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;

/**
 * Class Name : AbstractMigrationViewPane
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
// public class AbstractMigrationViewPane extends AbstractSDVViewPane {
public class NEWPEIFMigrationViewPane extends Composite {
    private Text filePathText;
    private Button openButton;
    private Button searchMECOButton;
    private Button btnOverride;
    private Tree tree;

	private Text logText;
    private Shell shell;
    private Text mecoText;
    //private PEIFTCDataExecuteJob executeJob;
    private NewPEIFExecution executeJob;
    private String defaultExcelFolder = "X:\\TcM_Interface\\Interface";
    //private String defaultExcelFolder = "E:\\TcM_Interface\\Interface";
    
    TCComponentMfgBvrProcess processLine;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    // public AbstractMigrationViewPane(Composite parent, int style, String id) {
    // super(parent, style, id);
    public NEWPEIFMigrationViewPane(Composite parent, int style, TCComponentMfgBvrProcess processLine) {
        super(parent, style);
        shell = this.getShell();
        setLayout(new FormLayout());
        this.processLine = processLine;

        Group grpImport = new Group(this, SWT.NONE);
        grpImport.setLayout(new FormLayout());
        FormData fd_grpImport = new FormData();
        fd_grpImport.top = new FormAttachment(0, 10);
        fd_grpImport.left = new FormAttachment(0, 10);
        fd_grpImport.right = new FormAttachment(100, -10);
        grpImport.setLayoutData(fd_grpImport);

        filePathText = new Text(grpImport, SWT.BORDER);
        filePathText.setText(defaultExcelFolder);
        filePathText.setEditable(false);
        FormData fd_filePathText = new FormData();
        fd_filePathText.left = new FormAttachment(0, 345);
        filePathText.setLayoutData(fd_filePathText);

        openButton = new Button(grpImport, SWT.NONE);
        fd_filePathText.right = new FormAttachment(openButton, -6);
        fd_filePathText.top = new FormAttachment(openButton, 2, SWT.TOP);
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
        FormData fd_openButton = new FormData();
        fd_openButton.top = new FormAttachment(0, 8);
        fd_openButton.right = new FormAttachment(100, -10);
        openButton.setLayoutData(fd_openButton);
        openButton.setText("Open");

        Group importGroup = new Group(this, SWT.NONE);
        fd_grpImport.bottom = new FormAttachment(importGroup, -6);

        btnOverride = new Button(grpImport, SWT.CHECK);
        FormData fd_btnOverride = new FormData();
        fd_btnOverride.top = new FormAttachment(0, 10);
        fd_btnOverride.left = new FormAttachment(0, 10);
        btnOverride.setLayoutData(fd_btnOverride);
        btnOverride.setText("Override");
        btnOverride.setSelection(true); //[SR없음][20150223]shcho, Override 체크박스에 Default로 체크 되도록 변경 (이종화 차장님 요청사항)

        searchMECOButton = new Button(grpImport, SWT.NONE);
        searchMECOButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IDialog dialog;
                try {
                    dialog = UIManager.getDialog(getShell(), "symc.me.bop.SearchMECODlg");
                    dialog.open();
                    IDataSet datSet = dialog.getSelectDataSetAll();
                    if (datSet == null)
                        return;
                    IDataMap mecoDataMap = datSet.getDataMap("mecoSearch");
                    if (mecoDataMap != null && mecoDataMap.containsKey("mecoNo")) {
                        mecoText.setText(mecoDataMap.getStringValue("mecoNo"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageDialog.openError(AIFUtility.getActiveDesktop().getShell(), "Error", ex.getMessage());
                }
            }
        });
        FormData fd_searchMECOButton = new FormData();
        fd_searchMECOButton.top = new FormAttachment(filePathText, -2, SWT.TOP);
        searchMECOButton.setLayoutData(fd_searchMECOButton);
        searchMECOButton.setText("Search MECO");

        mecoText = new Text(grpImport, SWT.BORDER);
        mecoText.setEditable(false);

        fd_searchMECOButton.left = new FormAttachment(0, 233);
        FormData fd_mecoText = new FormData();
        fd_mecoText.top = new FormAttachment(filePathText, 0, SWT.TOP);
        fd_mecoText.left = new FormAttachment(btnOverride, 37);
        fd_mecoText.right = new FormAttachment(searchMECOButton, -6);
        mecoText.setLayoutData(fd_mecoText);
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
     * Dialog에서 Validation버튼이 눌러지면 호출 하는 Method
     * @param shell
     * @param validationButton
     * @param executeButton
     * @param cancelButton
     */
    public void validation(final Shell shell, final Button validationButton, final Button executeButton, final Button cancelButton) {
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
        validationButton.setEnabled(false);
        executeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        setEnableButtons(true);

        NEWPEIFMigrationViewControll aPEIFMigrationViewControll = new NEWPEIFMigrationViewControll(this);
		executeJob  = new NewPEIFExecution(aPEIFMigrationViewControll);
		boolean isOK = executeJob.runTcDataValidation();
		if(isOK==true){
			executeButton.setEnabled(false);
			executeButton.setEnabled(true);
		}else{
			executeButton.setEnabled(true);
			executeButton.setEnabled(false);
		}

    }

    /**
     * Dialog에서 실행버튼이 눌러지면 호출 하는 Method
     * 
     * @method execute
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void execute(final Shell shell,  final Button validationButton, final Button executeButton, final Button cancelButton) {
    	
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
        validationButton.setEnabled(false);
        executeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        setEnableButtons(true);
        
        // 여기서부터 실제 Tc에 반영하는 Action을 수행 하도록 한다.
        if(executeJob!=null){
        	executeJob.runTcDataInterface();
        }

    }

    /**
     * 선택된 Process Line을 읽어서 Return 한다.
     * @return
     */
    public TCComponentMfgBvrProcess getProcessLine() {
		return processLine;
	}

	public void stop() {
        if (executeJob != null) {
            //executeJob.setStopflag(true);
        	executeJob.setForcedStopFlag(true);
        	
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
    
    public String getMecoText(){
    	if(mecoText!=null){
    		return mecoText.getText();
    	}else{
    		return (String)null;
    	}
    }
    
    public String getWorkPath(){
    	if(filePathText!=null){
    		return filePathText.getText();
    	}else{
    		return (String)null;
    	}
    }
    
    public boolean getOverride(){
    	if(btnOverride!=null){
    		return btnOverride.getSelection();
    	}else{
    		return false;
    	}
    }

    public Tree getTree() {
		return tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

	public Text getLogText() {
		return logText;
	}
}
