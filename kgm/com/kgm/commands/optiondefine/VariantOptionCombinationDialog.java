package com.kgm.commands.optiondefine;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.optiondefine.excel.ValidationOptionCombinationExcel;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class VariantOptionCombinationDialog extends Dialog {

    protected Object result;
    protected Shell shlUploadOptionCombination;
    protected Registry registry;
    private Text downloadText;
    private Text uploadText;
    private Text progressingText;
    private Button btnDownload;
    private Button btnUpload;
    private Button btnValidation;
    private Button btnRun;
    private Button btnClose;
    private ValidationOptionCombinationExcel excelOp;
    private String strTemplateDSName = "Upload_OptionCombination_template";

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public VariantOptionCombinationDialog(Shell parent, int style) {
        super(parent, style);
    }

    /**
     * Open the dialog.
     * 
     * @return the result
     */
    public Object open() {
        createContents();
        shlUploadOptionCombination.open();
        shlUploadOptionCombination.layout();
        Display display = getParent().getDisplay();
        while (!shlUploadOptionCombination.isDisposed()) {
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
        shlUploadOptionCombination = new Shell(getParent(), getStyle());
        shlUploadOptionCombination.setSize(450, 388);
        shlUploadOptionCombination.setText(StringUtil.getTextBundle(this.registry, "title", null, this.getClass()));
        Rectangle screen = this.shlUploadOptionCombination.getDisplay().getMonitors()[0].getBounds();
        Rectangle shellBounds = this.shlUploadOptionCombination.getBounds();
        this.shlUploadOptionCombination.setBounds((screen.width - shellBounds.width) / 2, (screen.height - shellBounds.height) / 2, shellBounds.width, shellBounds.height);
        shlUploadOptionCombination.setBackground(shlUploadOptionCombination.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        shlUploadOptionCombination.setBackgroundMode(SWT.INHERIT_FORCE);
        createUI();
        addListener();
        this.checkDialogOpen(); // Open전 체크
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
        Label lblTemplateFile = new Label(shlUploadOptionCombination, SWT.NONE);
        lblTemplateFile.setBounds(10, 27, 81, 25);
        lblTemplateFile.setText(StringUtil.getTextBundle(this.registry, "templateFileLabel", null, this.getClass()));

        downloadText = new Text(shlUploadOptionCombination, SWT.BORDER);
        downloadText.setText(strTemplateDSName);
        downloadText.setForeground(shlUploadOptionCombination.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        downloadText.setBackground(shlUploadOptionCombination.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        downloadText.setEnabled(false);
        downloadText.setEditable(false);
        downloadText.setBounds(97, 21, 255, 25);

        Label lblUploadFile = new Label(shlUploadOptionCombination, SWT.NONE);
        lblUploadFile.setText(StringUtil.getTextBundle(this.registry, "uploadFileLabel", null, this.getClass()));
        lblUploadFile.setBounds(10, 60, 67, 25);

        uploadText = new Text(shlUploadOptionCombination, SWT.BORDER);
        uploadText.setEnabled(false);
        uploadText.setEditable(false);
        uploadText.setBounds(97, 54, 255, 25);

        this.btnDownload = new Button(shlUploadOptionCombination, SWT.NONE);
        this.btnDownload.setBounds(358, 21, 76, 25);
        this.btnDownload.setText(StringUtil.getTextBundle(this.registry, "downButton", null, this.getClass()));

        this.btnUpload = new Button(shlUploadOptionCombination, SWT.NONE);
        this.btnUpload.setText(StringUtil.getTextBundle(this.registry, "uploadButton", null, this.getClass()));
        this.btnUpload.setBounds(358, 53, 76, 25);

        this.btnValidation = new Button(shlUploadOptionCombination, SWT.NONE);
        this.btnValidation.setText(StringUtil.getTextBundle(this.registry, "validationButton", null, this.getClass()));
        this.btnValidation.setBounds(126, 100, 76, 25);
        this.btnValidation.setEnabled(false);

        this.btnRun = new Button(shlUploadOptionCombination, SWT.NONE);
        this.btnRun.setText(StringUtil.getTextBundle(this.registry, "runButton", null, this.getClass()));
        this.btnRun.setBounds(208, 100, 76, 25);
        this.btnRun.setEnabled(false);

        Group grpMonitoringProgress = new Group(shlUploadOptionCombination, SWT.NONE);
        grpMonitoringProgress.setText("Monitoring Progress");
        grpMonitoringProgress.setBounds(10, 142, 424, 180);

        progressingText = new Text(grpMonitoringProgress, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        progressingText.setBounds(10, 22, 404, 148);

        this.btnClose = new Button(shlUploadOptionCombination, SWT.NONE);
        this.btnClose.setText(StringUtil.getTextBundle(this.registry, "closeButton", null, this.getClass()));
        this.btnClose.setBounds(191, 327, 76, 25);
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
        // 템플릿 다운로드 버튼
        this.btnDownload.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                downloadTemplate();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // 업로드 버튼
        this.btnUpload.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(shlUploadOptionCombination, SWT.OPEN);
                fileDialog.setFilterExtensions(new String[] { "*.xlsx", "*.xls", "*.xltx", "*.*" });
                uploadText.setText(fileDialog.open());
                excelOp = null;
                excelOp = new ValidationOptionCombinationExcel(shlUploadOptionCombination, VariantOptionCombinationDialog.class, registry, progressingText);
                excelOp.load(uploadText, btnValidation, btnRun);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // Validation 버튼
        this.btnValidation.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                excelOp.doValidation(btnRun);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // Run 버튼
        this.btnRun.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                excelOp.run();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // 닫기 버튼
        this.btnClose.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // dialog Close
                shlUploadOptionCombination.dispose();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

    }

    private void downloadTemplate() {
        try {
            FileDialog fDialog = new FileDialog(shlUploadOptionCombination, SWT.SINGLE | SWT.SAVE);
            fDialog.setFilterNames(new String[] { "Excel File" });
            fDialog.setFileName(strTemplateDSName + ".xltx");
            // *.xls, *.xlsx Filter 설정
            fDialog.setFilterExtensions(new String[] { "*.xltx" });
            fDialog.open();
            String strfileName = fDialog.getFileName();
            if ((strfileName == null) || (strfileName.equals(""))) {
                return;
            }
            String strDownLoadFilePath = fDialog.getFilterPath() + File.separatorChar + strfileName;
            File checkFile = new File(strDownLoadFilePath);
            if (checkFile.exists()) {
                org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shlUploadOptionCombination, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);                
                box1.setMessage(strDownLoadFilePath + " " + StringUtil.getTextBundle(this.registry, "confirmExcelOverrideWrite", null, this.getClass()));                
                if (box1.open() != SWT.OK) {
                    return;
                }
            }
            File tempFile = SYMTcUtil.getTemplateFile(CustomUtil.getTCSession(), strTemplateDSName, null);
            if (checkFile.exists()) {
                checkFile.delete();
            }
            tempFile.renameTo(new File(strDownLoadFilePath));            
            MessageBox.post(shlUploadOptionCombination, strDownLoadFilePath + " " + StringUtil.getTextBundle(this.registry, "excelDownloadCompleted", null, this.getClass()), "Notification", 2);
        } catch (Exception e) {
            MessageBox.post(shlUploadOptionCombination, e.toString(), "Notification", 2);
        }
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
    private void checkDialogOpen() {
        TCComponentBOMLine[] targetBOMLines = null;
        InterfaceAIFComponent[] targetComps = AIFUtility.getCurrentApplication().getTargetComponents();
        if (targetComps == null || targetComps.length == 0) {
            this.msgFnItemOpenStr();
        }
        targetBOMLines = new TCComponentBOMLine[targetComps.length];
        for (int i = 0; i < targetComps.length; i++) {
            if (!(targetComps[i] instanceof TCComponentBOMLine)) {
                this.msgFnItemOpenStr();
            }
            targetBOMLines[i] = (TCComponentBOMLine) targetComps[i];
        }
        // BOMLine Load 체크
        if (targetBOMLines == null || targetBOMLines.length == 0) {
            this.msgFnItemOpenStr();
        }
        TCComponentBOMLine topLine;
        try {
            topLine = targetBOMLines[0].window().getTopBOMLine();
            // Function Item 체크
            if (!"S7_Function".equals(topLine.getItem().getType())) {
                MessageBox.post(shlUploadOptionCombination, StringUtil.getTextBundle(this.registry, "needFunctionItemType", null, this.getClass()), "Notification", 2);
                shlUploadOptionCombination.dispose();
            }
        } catch (TCException e) {
            e.printStackTrace();
            MessageBox.post(shlUploadOptionCombination, "Teamcenter Error!", "Notification", 2);
            shlUploadOptionCombination.dispose();
        }
    }

    /**
     * BOMLine Open Error Msg
     * 
     * @method msgFnItemOpenStr
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void msgFnItemOpenStr() {
        MessageBox.post(shlUploadOptionCombination, StringUtil.getTextBundle(this.registry, "functionItemStrManOpen", null, this.getClass()), "Notification", 2);
        if(!shlUploadOptionCombination.isDisposed()) {
            shlUploadOptionCombination.dispose();
        }
    }
}
