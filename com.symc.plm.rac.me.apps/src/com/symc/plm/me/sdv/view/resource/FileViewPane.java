/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

/**
 * Class Name : Creation_File
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class FileViewPane extends AbstractSDVViewPane {
    /* CAD File */
    private Text cadText;
    private Button cadFileButton;
    private Button cadDelButton;
    /* ZIP File */
    private Text zipText;
    private Button zipFileButton;
    private Button zipDelButton;
    /* CGR File */
    private Text cgrText;
    private Button cgrFileButton;
    private Button cgrDelButton;
    /* PDF File */
    private Text pdfText;
    private Button pdfFileButton;
    private Button pdfDelButton;

    public static final String CAD_EXT = "CATPart,jt,dwg";
    public static final String ZIP_EXT = "zip";
    public static final String CGR_EXT = "cgr";
    public static final String PDF_EXT = "pdf";

    public static final String FILE_ROOT_PATH = "C:\\";

    protected String resultfilterPath; // FilterPath 정보 저장

    private boolean isModified = false;

    // private HashMap<String, String> oldFileNameMap;
    private boolean isCreated = false;
    private HashMap<String, String> newFileNameMap = new HashMap<String, String>();

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public FileViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {
        // setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gl_composite = new GridLayout(4, false);
        gl_composite.verticalSpacing = 0;
        gl_composite.horizontalSpacing = 10;
        gl_composite.marginLeft = 10;
        gl_composite.marginRight = 10;
        gl_composite.marginHeight = 0;
        composite.setLayout(gl_composite);

        Label cadLabel = new Label(composite, SWT.NONE);
        cadLabel.setText("CAD");

        cadText = new Text(composite, SWT.BORDER);
        cadText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        cadText.setEditable(false);

        cadFileButton = new Button(composite, SWT.NONE);
        GridData gd_cadFileButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_cadFileButton.widthHint = 50;
        cadFileButton.setLayoutData(gd_cadFileButton);
        cadFileButton.setText("File");

        cadDelButton = new Button(composite, SWT.NONE);
        GridData gd_cadDelButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_cadDelButton.widthHint = 50;
        cadDelButton.setLayoutData(gd_cadDelButton);
        cadDelButton.setText("Delete");

        Label zipLabel = new Label(composite, SWT.NONE);
        zipLabel.setText("ZIP");

        zipText = new Text(composite, SWT.BORDER);
        zipText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        zipText.setEditable(false);

        zipFileButton = new Button(composite, SWT.NONE);
        GridData gd_zipFileButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_zipFileButton.widthHint = 50;
        zipFileButton.setLayoutData(gd_zipFileButton);
        zipFileButton.setText("File");

        zipDelButton = new Button(composite, SWT.NONE);
        GridData gd_zipDelButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_zipDelButton.widthHint = 50;
        zipDelButton.setLayoutData(gd_zipDelButton);
        zipDelButton.setText("Delete");

        Label cgrLabe = new Label(composite, SWT.NONE);
        cgrLabe.setText("CGR");

        cgrText = new Text(composite, SWT.BORDER);
        cgrText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        cgrText.setEditable(false);

        cgrFileButton = new Button(composite, SWT.NONE);
        GridData gd_cgrFileButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_cgrFileButton.widthHint = 50;
        cgrFileButton.setLayoutData(gd_cgrFileButton);
        cgrFileButton.setText("File");

        cgrDelButton = new Button(composite, SWT.NONE);
        GridData gd_cgrDelButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_cgrDelButton.widthHint = 50;
        cgrDelButton.setLayoutData(gd_cgrDelButton);
        cgrDelButton.setText("Delete");

        Label pdfLabe = new Label(composite, SWT.NONE);
        pdfLabe.setText("PDF");

        pdfText = new Text(composite, SWT.BORDER);
        pdfText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        pdfText.setEditable(false);

        pdfFileButton = new Button(composite, SWT.NONE);
        GridData gd_pdfFileButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_pdfFileButton.widthHint = 50;
        pdfFileButton.setLayoutData(gd_pdfFileButton);
        pdfFileButton.setText("File");

        pdfDelButton = new Button(composite, SWT.NONE);
        GridData gd_pdfDelButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_pdfDelButton.widthHint = 50;
        pdfDelButton.setLayoutData(gd_pdfDelButton);
        pdfDelButton.setText("Delete");

        resultfilterPath = FILE_ROOT_PATH;
        cadFileButton.addSelectionListener(createSelectionListener(cadText, CAD_EXT));
        zipFileButton.addSelectionListener(createSelectionListener(zipText, ZIP_EXT));
        cgrFileButton.addSelectionListener(createSelectionListener(cgrText, CGR_EXT));
        pdfFileButton.addSelectionListener(createSelectionListener(pdfText, PDF_EXT));

        cadDelButton.addSelectionListener(deleteSelectionListener(cadText, CAD_EXT));
        zipDelButton.addSelectionListener(deleteSelectionListener(zipText, ZIP_EXT));
        cgrDelButton.addSelectionListener(deleteSelectionListener(cgrText, CGR_EXT));
        pdfDelButton.addSelectionListener(deleteSelectionListener(pdfText, PDF_EXT));

    }

    /**
     * new SelectionListener (Create Dataset)
     * 
     * @param text
     * @param fileExtention
     * @return
     */
    public SelectionListener createSelectionListener(final Text text, final String fileExtention) {
        return new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fileDialogOpen(text, fileExtention);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        };
    }

    /**
     * new SelectionListener (Delete)
     * 
     * @param text
     * @param fileExtention
     * @return
     */
    public SelectionListener deleteSelectionListener(final Text text, final String extention) {
        return new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // setTextFieldValue(text, fileType);
                if (text.getText().length() > 0) {
                    text.setText("");

                    String[] arrFileExt = extention.split(",");
                    for (String fileExt : arrFileExt) {
                        newFileNameMap.put(fileExt, "");
                    }
                    isModified = true;
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        };
    }

    /**
     * FileDialog Open
     * 
     * @param text
     * @param fileExtentionKey
     */
    public void fileDialogOpen(Text text, String fileExtentionKey) {
        String[] arrFileExt = fileExtentionKey.split(",");
        ArrayList<String> filterExtList = new ArrayList<String>();

        // CAD 파일의 경우 첫 행에 전체 확장자 셋팅 (예 : *.catpart;*.jt;*.dwg)
        if (arrFileExt.length > 1) {
            filterExtList.add("*." + fileExtentionKey.replace(",", ";*."));
        }

        // 나머지 개별 확장자 생성 (예 : *.pdf )
        for (int i = 0; i < arrFileExt.length; i++) {
            filterExtList.add("*." + arrFileExt[i]);
        }

        FileDialog dialog = null;
        if (arrFileExt.length > 1) {
            dialog = new FileDialog(getShell(), SWT.MULTI);
        } else {
            dialog = new FileDialog(getShell(), SWT.SINGLE);
        }

        dialog.setFilterExtensions(filterExtList.toArray(new String[filterExtList.size()]));

        // Dialog Open
        String selectedResult = dialog.open();

        // 선택 결과 오류 처리1
        if (selectedResult == null || selectedResult.equals("")) {
            return;
        }

        // 선택 결과 오류 처리2
        String[] fileNames = dialog.getFileNames();
        if (fileNames == null || fileNames.length == 0) {
            return;
        }

        // 선택 파일 View Text상자에 표기하기
        StringBuilder strViewFilePath = new StringBuilder();

        if (fileNames.length == 1) {
            strViewFilePath.append(dialog.getFilterPath() + java.io.File.separatorChar + fileNames[0]);
        } else {
            for (int j = 0; j < fileNames.length; j++) {
                if (j > 0) {
                    strViewFilePath.append(", ");
                }

                strViewFilePath.append(fileNames[j]);
            }
        }

        text.setText(strViewFilePath.toString());
        resultfilterPath = dialog.getFilterPath();

        /* 선택 파일을 newFileNameMap 에 저장 (Dataset 생성시 사용할 실제 파일 정보임) */
        setNewFileNameMap(arrFileExt, dialog.getFilterPath(), fileNames);
        isModified = true;
    }

    /**
     * File Dialog에서 선택한 파일을 newFileNameMap 에 저장 (Dataset 생성시 사용할 실제 파일 정보임)
     * 
     * @param arrFileExt
     * @param dialog
     * @param fileNames
     */
    public void setNewFileNameMap(String[] arrFileExt, String filePath, String[] fileNames) {

        // CATpart, jt, dwg 파일의 경우 3개를 셋트로 등록하기 위해 초기값으로 빈 값을 Map에 모두 생성
        // Revise시 새로 지정한 파일은 2번째for문을 통해 실질 값이 등록 될 것이고, 삭제해야 할 대상은 ""(공백)값을 가지게 하여 Dataset생성시 삭제 대상으로 사용한다.
        if (arrFileExt.length > 1) {
            for (String fileExt : arrFileExt) {
                if (arrFileExt.length > 1) {
                    newFileNameMap.put(fileExt, "");
                }
            }
        }

        // 선택 파일을 newFileNameMap 에 저장
        for (int k = 0; k < fileNames.length; k++) {
            String realFileExt = fileNames[k].substring(fileNames[k].lastIndexOf(".") + 1);
            if(realFileExt.equalsIgnoreCase("CATPart")) {
                realFileExt = "CATPart";
            } else if(realFileExt.equalsIgnoreCase("dwg")) {
                realFileExt = "dwg";
            } else if(realFileExt.equalsIgnoreCase("jt")) {
                realFileExt = "jt";
            }
            
            newFileNameMap.put(realFileExt, filePath + java.io.File.separatorChar + fileNames[k]);
        }
    }

    // /**
    // * Delete 버튼 클릭시 Text 상자에 값을 원상태로 복원하는 함수
    // *
    // * @param text
    // * @param fileType
    // */
    // public void setTextFieldValue(final Text text, final String fileType) {
    // String oldFileName = oldFileNameMap.get(fileType);
    // if (oldFileName == null || oldFileName.length() == 0) {
    // text.setText("");
    // } else {
    // text.setText(oldFileName);
    // }
    // }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        RawDataMap targetMap = new RawDataMap();
        boolean isModified = checkModified();

        targetMap.put("isModified", isModified, IData.BOOLEAN_FIELD);
        for (String key : newFileNameMap.keySet()) {
            targetMap.put(key, newFileNameMap.get(key), IData.STRING_FIELD);
        }
        return targetMap;
    }

    /**
     * 화면에서 변경된 사항이 있는지 검사 (변경사항 있으면 true Return)
     */
    public boolean checkModified() {
        // 처음 생성시에는 무조건 true 반환
        // if (oldFileNameMap.get("Create") != null && oldFileNameMap.get("Create").equals("Create")) {
        // return true;
        // }
        if (isCreated) {
            return isCreated;
        }

        return isModified;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new AbstractSDVInitOperation() {

            @SuppressWarnings("unchecked")
            public IDataMap getInitData() {
                Map<String, Object> paramMap = getParameters();
                Map<String, Object> datasetInform = null;
                IDataMap dataMap = null;
                // Revise
                if (paramMap.containsKey("itemProperties") && paramMap.containsKey("revisionProperties") && paramMap.containsKey("Dataset")) {
                    datasetInform = (Map<String, Object>) paramMap.get("Dataset");
                    dataMap = new RawDataMap();
                    for (String key : datasetInform.keySet()) {
                        dataMap.put(key, datasetInform.get(key), IData.OBJECT_FIELD);
                    }
                }
                return dataMap;
            }

            @Override
            public void executeOperation() throws Exception {
                IDataSet dataset = new DataSet();
                dataset.addDataMap("Dataset", getInitData());

                setData(dataset);
            }

        };
    }

    @Override
    public void initalizeData(int result, IViewPane owner, IDataSet dataset) {
        RawDataMap dataMap = (RawDataMap) dataset.getDataMap("Dataset");
        // oldFileNameMap = new HashMap<String, String>();

        if (dataMap == null) {
            // oldFileNameMap.put("Create", "Create");
            isCreated = true;
        } else {
            StringBuilder cadDatasetFiles = new StringBuilder();
            for (String key : dataMap.keySet()) {
                String fileType = key.substring(key.lastIndexOf(".") + 1);
                if (fileType.equalsIgnoreCase("catpart") || fileType.equalsIgnoreCase("jt") || fileType.equalsIgnoreCase("dwg")) {
                    cadDatasetFiles.append("<Dataset>" + key + " ");
                    cadText.setText(cadDatasetFiles.toString());
                } else if (fileType.equalsIgnoreCase(ZIP_EXT)) {
                    zipText.setText("<Dataset>" + key);
                } else if (fileType.equalsIgnoreCase(CGR_EXT)) {
                    cgrText.setText("<Dataset>" + key);
                } else if (fileType.equalsIgnoreCase(PDF_EXT)) {
                    pdfText.setText("<Dataset>" + key);
                }
            }
        }
    }

    @Override
    public void uiLoadCompleted() {
        // oldFileNameMap.put("CATPart", cadText.getText());
        // oldFileNameMap.put(ZIP_EXT, zipText.getText());
        // oldFileNameMap.put(CGR_EXT, cgrText.getText());
        // oldFileNameMap.put(PDF_EXT, pdfText.getText());
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

}
