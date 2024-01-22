/**
 * 
 */
package com.symc.plm.me.sdv.view.migration;

import java.io.File;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.symc.plm.me.sdv.service.migration.formatter.BOPExcelFormat;
import com.symc.plm.me.sdv.service.migration.formatter.IImportFormat;
import com.symc.plm.me.sdv.service.migration.job.TCDataImportJob;
import com.symc.plm.me.sdv.service.migration.job.TCDataValidatetJob;

/**
 * Class Name : AbstractMigrationViewPane
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
// public class AbstractMigrationViewPane extends AbstractSDVViewPane {
public class ExcelMigrationViewPane extends Composite {
    private Text filePathText;
    private Button openButton;
    Button peIfRadioButton;
    Button excelRadioButton;
    private Tree tree;
    private Text logText;
    private Shell shell;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    // public AbstractMigrationViewPane(Composite parent, int style, String id) {
    // super(parent, style, id);
    public ExcelMigrationViewPane(Composite parent, int style) {
        super(parent, style);
        shell = this.getShell();
        setLayout(new FormLayout());

        Group grpImport = new Group(this, SWT.NONE);
        grpImport.setLayout(new FormLayout());
        FormData fd_grpImport = new FormData();
        fd_grpImport.top = new FormAttachment(0, 10);
        fd_grpImport.left = new FormAttachment(0, 10);
        fd_grpImport.right = new FormAttachment(100, -10);
        grpImport.setLayoutData(fd_grpImport);

        filePathText = new Text(grpImport, SWT.BORDER);
        filePathText.setEditable(false);
        FormData fd_filePathText = new FormData();
        filePathText.setLayoutData(fd_filePathText);

        openButton = new Button(grpImport, SWT.NONE);
        fd_filePathText.right = new FormAttachment(openButton, -6);
        fd_filePathText.top = new FormAttachment(openButton, 2, SWT.TOP);
        openButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    fileExcelOpen();
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
        importGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
        FormData fd_importGroup = new FormData();
        fd_importGroup.top = new FormAttachment(0, 123);
        fd_importGroup.left = new FormAttachment(0, 10);
        fd_importGroup.right = new FormAttachment(100, -10);
        importGroup.setLayoutData(fd_importGroup);

        tree = new Tree(importGroup, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        Group logGroup = new Group(this, SWT.NONE);
        fd_importGroup.bottom = new FormAttachment(logGroup, -6);
        logGroup.setText("Log");
        logGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
        FormData fd_logGroup = new FormData();
        fd_logGroup.top = new FormAttachment(0, 397);
        fd_logGroup.bottom = new FormAttachment(100, -10);
        fd_logGroup.left = new FormAttachment(0, 10);
        fd_logGroup.right = new FormAttachment(100, -10);

        Group grpType = new Group(grpImport, SWT.NONE);
        fd_filePathText.left = new FormAttachment(grpType, 189);
        grpType.setText("Type");
        FormData fd_grpType = new FormData();
        fd_grpType.top = new FormAttachment(0);
        fd_grpType.left = new FormAttachment(0, 10);
        fd_grpType.bottom = new FormAttachment(0, 46);
        fd_grpType.right = new FormAttachment(0, 178);
        grpType.setLayoutData(fd_grpType);

        excelRadioButton = new Button(grpType, SWT.RADIO);
        excelRadioButton.setBounds(10, 20, 56, 16);
        excelRadioButton.setText("Excel");
        excelRadioButton.setSelection(true);

        peIfRadioButton = new Button(grpType, SWT.RADIO);
        peIfRadioButton.setText("PEIF");
        peIfRadioButton.setBounds(89, 20, 49, 16);
        logGroup.setLayoutData(fd_logGroup);

        logText = new Text(logGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public void validate() {
        HashMap<String, Object> handelObjectMap = new HashMap<String, Object>();
        handelObjectMap.put("LOG_TEXT", logText);
        TCDataValidatetJob job = new TCDataValidatetJob(shell, "valdate", tree, logText);
        // AIFUtility.getDefaultSession().queueOperation(job);
        job.schedule();
    }

    public void fileExcelOpen() throws Exception {
        FileDialog fDialog = new FileDialog(this.shell, SWT.SINGLE);
        fDialog.setFilterNames(new String[] { "Excel File" });
        // *.xls, *.xlsx Filter 설정
        fDialog.setFilterExtensions(new String[] { "*.xls *.xlsx" });
        fDialog.setFileName("*.xlsx");
        fDialog.open();
        if (fDialog.getFilterPath() == null || fDialog.getFilterPath().equals("")) {
            return;
        }
        String strfileName = fDialog.getFileName();
        if ((strfileName == null) || (strfileName.equals(""))) {
            return;
        }
        String strDownLoadFilePath = fDialog.getFilterPath() + File.separatorChar + strfileName;
        // File checkFile = new File(strDownLoadFilePath);

        // if (checkFile.exists()) {
        // org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
        // box1.setMessage(strDownLoadFilePath + " - 파일이 이미존재 합니다.");
        // if (box1.open() != SWT.OK) {
        // return;
        // }
        // }
        this.filePathText.setText(strDownLoadFilePath);
        // Formatter 설정 (Tree 구성)
        IImportFormat importFormat = new BOPExcelFormat(shell, tree, strDownLoadFilePath);
        // Execute Import Job
        TCDataImportJob job = new TCDataImportJob(shell, "importExcel", tree, logText, importFormat);
        job.schedule();
    }

}
