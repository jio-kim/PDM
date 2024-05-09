package com.kgm.commands.vpm.report;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import oracle.sql.TIMESTAMP;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.vpm.report.dao.CustomReportDao;
import com.kgm.commands.vpm.report.utils.ExcelUtilForReport;
import com.kgm.common.SYMCDateTimeButton;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.remote.DataSet;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.ui.services.NavigatorOpenService;
import com.teamcenter.rac.util.MessageBox;

public class VehPartReportDialog extends SYMCAbstractDialog {

    private Text partNo;

    private Label countLabel;

    private Combo validCombo, statusCombo, nonEPLCombo;

    private SYMCDateTimeButton searchDateFrom, searchDateTo;

    private Button searchButton, reTryStatusButton, userSkipStatusButton, excelExportButton;

    private Table resultTable;
    private String[] columnName = new String[] { "ECO No", "Part Origin", "Part No", "Revision", "��ȿ(Y)/����ȿ(N)", "NON EPL", "CAD TYPE", "����", "MSG", "I/F DATE", "Modify DATE" };
    private int[] columnSize = new int[] { 90, 90, 120, 60, 110, 80, 150, 60, 170, 140, 180 };

    private Button closeButton;

    private WaitProgressBar waitProgress;
    
    private CustomReportDao dao;
    /*
    * #STATUS ����
    * a. V(I/F ��� - ���� Default ����(I) ���� Validate ���� �� ����)
    * b. C(����:Item Created)
    * c. O(����:��ȿ��� Item Created)
    * d. U(����:Item Revised)
    * e. S(����: TC�� �����ϴ� ��ȿ������ REVISE SKIP)
    * f. T(����: VPM CHANGE_TYPE SKIP)
    * g. P(ChangeOwner ����� ����)
    * h. F(����)
    * i. M(����: ����� SKIP)
    */
    private static final String[] STAT_COMBO_MSG = new String[] { "ALL", "F (����)", "P (ChangeOwner ����� ����)", "C (����:Item Created)", "O (����:��ȿ��� Item Created)", "U (����:Item Revised)", "S (����: TC�� �����ϴ� ��ȿ������ REVISE SKIP)", "T (����: VPM CHANGE_TYPE SKIP)", "M (����SKIP)", "V (��ϴ��)" };
    private static final String[] STAT_COMBO_CODE = new String[] { "ALL", "F", "P", "C", "O", "U", "S", "T", "M", "V" };
    
    private static final String[] VALID_COMBO_MSG = new String[] { "ALL", "Y(��ȿ)", "N(����ȿ)" };
    private static final String[] VALID_COMBO_CODE = new String[] { "ALL", "Y", "N" };
    

    public VehPartReportDialog(Shell parent, int _selection) {
        super(parent, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM | _selection);
        if(dao == null) {
            dao = new CustomReportDao();
        }
    }

    @Override
    protected boolean apply() {
        return false;
    }

    /** ��ư ���� */
    protected void createButtonsForButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, true));
        excelExportButton = new Button(composite, SWT.PUSH);
        excelExportButton.setText("Excel Export");
        excelExportButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    ExcelUtilForReport.exportDataXLS(getShell(), resultTable, "VehPart Report", 0);                    
                } catch(Exception ex) {
                    ex.printStackTrace();
                    MessageBox.post(getShell(), "Excel Export Error", "ERROR", MessageBox.ERROR);
                }
            }
        });
        closeButton = new Button(composite, SWT.PUSH);
        closeButton.setText("Close");
        closeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getShell().close();
            }
        });
    }

    /** Composiste ���� */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        getShell().setText("Monitoring VehPart");
        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout());

        createSearchComposite(composite);
        cteateSearchResultTable(composite);
        return composite;
    }

    /** �˻� ���� Composiste ���� */
    private void createSearchComposite(Composite paramComposite) {
        Composite composite = new Composite(paramComposite, SWT.NONE);
        GridLayout layout = new GridLayout(10, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(gridData);

        // #1
        makeLabel(composite, "PART NO.", 110);
        partNo = new Text(composite, SWT.BORDER);
        gridData = new GridData(120, SWT.DEFAULT);
        partNo.setLayoutData(gridData);
        partNo.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                case SWT.CR:
                    search();
                    break;
                default:
                    // ignore everything else
                }
            }
        });
        makeLabel(composite, "From : ", 110);
        searchDateFrom = new SYMCDateTimeButton(composite);
        gridData = new GridData();
        searchDateFrom.setLayoutData(gridData);
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.MONTH, -3); // 3���� ������ ����
        searchDateFrom.setDate(fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH));

        makeLabel(composite, "To : ", 110);
        searchDateTo = new SYMCDateTimeButton(composite);
        gridData = new GridData();
        searchDateTo.setLayoutData(gridData);
        Calendar toCalendar = Calendar.getInstance(); // ���� ���� ����
        searchDateTo.setDate(toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH));

        Label label = new Label(composite, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 3;
        label.setLayoutData(gridData);

        // �˻� ��ư
        searchButton = new Button(composite, SWT.PUSH);
        searchButton.setText("Search");
        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                search();
            }
        });

        // #2
        makeLabel(composite, "��ȿ/����ȿ", 110);
        validCombo = new Combo(composite, SWT.READ_ONLY);
        validCombo.setItems(VALID_COMBO_MSG);
        validCombo.select(0);
        gridData = new GridData(120, SWT.DEFAULT);
        validCombo.setLayoutData(gridData);
        /*
         * #STATUS ���� a. V(I/F ��� - ���� Default ����(I) ���� Validate ���� �� ����) b.
         * C(����:Item Created) c. O(����:��ȿ��� Item Created) d. U(����:Item Revised)
         * e. S(����: TC�� �����ϴ� ��ȿ������ REVISE SKIP) f. T(����: VPM CHANGE_TYPE SKIP)
         * g. P(ChangeOwner ����� ����) h. F(����) i. R(Latest ���°� Working���� ���� ���� ���)
         * j. M(����: ����� SKIP)
         */
        makeLabel(composite, "����", 110);
        statusCombo = new Combo(composite, SWT.READ_ONLY);
        statusCombo.setItems(STAT_COMBO_MSG);
        statusCombo.select(0);
        gridData = new GridData(120, SWT.DEFAULT);
        statusCombo.setLayoutData(gridData);
        
        makeLabel(composite, "NON EPL", 110);
        nonEPLCombo = new Combo(composite, SWT.READ_ONLY);
        nonEPLCombo.setItems(new String[] { "ALL", "Y", "N" });
        nonEPLCombo.select(0);        
        gridData = new GridData(120, SWT.DEFAULT);
        nonEPLCombo.setLayoutData(gridData);

        Label label7 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 10;
        label7.setLayoutData(gridData);

        Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 10;
        lSeparator.setLayoutData(gridData);

        Composite buttonComposite1 = new Composite(composite, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 10;
        buttonComposite1.setLayoutData(gridData);
        layout = new GridLayout(15, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonComposite1.setLayout(layout);

        countLabel = new Label(buttonComposite1, SWT.RIGHT);
        countLabel.setText("[�� : - ��]");
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        countLabel.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);

        label = new Label(buttonComposite1, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        reTryStatusButton = new Button(buttonComposite1, SWT.PUSH);
        reTryStatusButton.setText("���(V) ���� ����");
        reTryStatusButton.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            public void widgetSelected(SelectionEvent e) {
                TableItem[] selectItems = resultTable.getSelection();
                if(selectItems.length == 0) {
                    return;
                }
                org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.YES | SWT.NO);
                box.setText("Confirm");
                box.setMessage("(" + selectItems.length + ") ���� ���(V) ���·� ���� �Ͻðڽ��ϱ�?");
                if(!(box.open() == SWT.YES)) {
                    return;
                }                
                ArrayList updateList = new ArrayList();                
                waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
                waitProgress.start();
                waitProgress.setStatus("updating.. ");                
                for (int i = 0; i < selectItems.length; i++) {
                    DataSet updateDataSet = new DataSet();                                    
                    updateDataSet.put("IF_STATUS", "V");
                    updateDataSet.put("IF_ERROR_MSG", "USER_CHANGE");
                    updateDataSet.put("ITEM_ID", selectItems[i].getText(2));
                    updateDataSet.put("REVISION_ID", selectItems[i].getText(3));         
                    updateDataSet.put("IF_DATE", selectItems[i].getData("IF_DATE"));
                    updateList.add(updateDataSet);
                }
                try {
                    dao.updateListVehStatus(updateList);                   
                } catch(Exception daoEx) {
                    daoEx.printStackTrace();
                    MessageBox.post(getShell(), "[SYSTEM] Update Error", "ERROR", MessageBox.ERROR);
                } finally {
                    waitProgress.close();
                    // Refresh
                    search();
                }
            }
        });
        gridData = new GridData(140, SWT.DEFAULT);
        gridData.horizontalSpan = 1;
        reTryStatusButton.setLayoutData(gridData);

        userSkipStatusButton = new Button(buttonComposite1, SWT.PUSH);
        userSkipStatusButton.setText("����SKIP(M) ���� ����");
        userSkipStatusButton.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            public void widgetSelected(SelectionEvent e) {
                TableItem[] selectItems = resultTable.getSelection();
                if(selectItems.length == 0) {
                    return;
                }
                org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.YES | SWT.NO);
                box.setText("Confirm");
                box.setMessage("(" + selectItems.length + ") ���� ����SKIP(M) ���·� ���� �Ͻðڽ��ϱ�?");
                if(!(box.open() == SWT.YES)) {
                    return;
                }                
                ArrayList updateList = new ArrayList();                
                waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
                waitProgress.start();
                waitProgress.setStatus("updating.. ");                
                for (int i = 0; i < selectItems.length; i++) {
                    DataSet updateDataSet = new DataSet();                                    
                    updateDataSet.put("IF_STATUS", "M");
                    updateDataSet.put("IF_ERROR_MSG", "USER_CHANGE");
                    updateDataSet.put("ITEM_ID", selectItems[i].getText(2));
                    updateDataSet.put("REVISION_ID", selectItems[i].getText(3));         
                    updateDataSet.put("IF_DATE", selectItems[i].getData("IF_DATE"));
                    updateList.add(updateDataSet);
                }
                try {
                    dao.updateListVehStatus(updateList);                   
                } catch(Exception daoEx) {
                    daoEx.printStackTrace();
                    MessageBox.post(getShell(), "[SYSTEM] Update Error", "ERROR", MessageBox.ERROR);
                } finally {
                    waitProgress.close();
                    // Refresh
                    search();
                }
            }
        });
        gridData = new GridData(140, SWT.DEFAULT);
        gridData.horizontalSpan = 1;
        userSkipStatusButton.setLayoutData(gridData);

    }

    /** �˻� ��� ���̺� ���� */
    private void cteateSearchResultTable(Composite paramComposite) {
        Composite composite = new Composite(paramComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(layoutData);

        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        layoutData.minimumHeight = 400;
        layoutData.horizontalSpan = 3;
        resultTable = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        resultTable.setHeaderVisible(true);
        resultTable.setLinesVisible(true);
        resultTable.setLayoutData(layoutData);
        resultTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                TableItem[] selectItems = resultTable.getSelection();
                try {
                    TCComponentItem item = CustomUtil.findItem(SYMCECConstant.ECOTYPE, (String) selectItems[0].getText(3));
                    if (item != null) {
                        NavigatorOpenService openService = new NavigatorOpenService();
                        openService.open(item);
                        getShell().close();
                    }
                } catch (TCException e1) {
                    e1.printStackTrace();
                }
            }
        });

        int i = 0;
        for (String value : columnName) {
            TableColumn column = new TableColumn(resultTable, SWT.NONE);
            column.setText(value);
            column.setWidth(columnSize[i]);
            i++;
        }
    }

    private void makeLabel(Composite paramComposite, String lblName, int lblSize) {
        GridData layoutData = new GridData(lblSize, SWT.DEFAULT);

        Label label = new Label(paramComposite, SWT.RIGHT);
        label.setText(lblName);
        label.setLayoutData(layoutData);
    }

    @SuppressWarnings({ "rawtypes" })
    private void search() {
        DataSet searchCondition = new DataSet();
        String part_no = partNo.getText();
        if (part_no != null && part_no.length() > 0) {
            part_no = part_no.replace("*", "%");
            searchCondition.put("ITEM_ID", part_no.toUpperCase() + '%');
        }
        String searchDateFromDate = searchDateFrom.getYear() + "-" + String.format("%1$02d", (searchDateFrom.getMonth() + 1)) + "-" + String.format("%1$02d", searchDateFrom.getDay()) + "";
        searchCondition.put("FROM_DATE", searchDateFromDate);
        String searchDateToDate = searchDateTo.getYear() + "-" + String.format("%1$02d", (searchDateTo.getMonth() + 1)) + "-" + String.format("%1$02d", searchDateTo.getDay()) + "";
        searchCondition.put("TO_DATE", searchDateToDate);
        searchCondition.put("IS_VALID", ("ALL".equals(this.getValidCode(validCombo.getSelectionIndex()))) ? null : this.getValidCode(validCombo.getSelectionIndex()));
        searchCondition.put("IF_STATUS", ("ALL".equals(this.getStatusCode(statusCombo.getSelectionIndex()))) ? null : this.getStatusCode(statusCombo.getSelectionIndex()));
        searchCondition.put("NON_EPL", ("ALL".equals(nonEPLCombo.getItem(nonEPLCombo.getSelectionIndex()))) ? null : nonEPLCombo.getItem(nonEPLCombo.getSelectionIndex()));
        waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
        waitProgress.start();
        waitProgress.setStatus("loading.. ");
        // Label ī��Ʈ ����
        countLabel.setText("[�� : - ��]");
        try {           
            ArrayList<HashMap> resultList = dao.getValidateVehPartList(searchCondition);
            // { "ECO No", "Part Origin", "Part No", "Revision", "��ȿ(Y)/����ȿ(N)",
            // "NON EPL", "CAD TYPE", "����", "MSG", "I/F DATE", "Modify DATE" };           
            resultTable.removeAll();
            String rowKey = "";
            Color rowColor = null;                
            int rowCount = 0;
            for (int i = 0; i < resultList.size(); i++) {
                HashMap data = resultList.get(i);
                String key = StringUtil.nullToString((String) data.get("GROUP_KEY"));
                // key�� �ٸ��� �ٸ� Row ������ ����
                if (!rowKey.equals(key)) {
                    rowCount = rowCount + 1;
                    if (rowColor == null || rowColor.getRGB().toString().equals(SWTResourceManager.getColor(SWT.COLOR_GRAY).getRGB().toString())) {
                        rowColor = SWTResourceManager.getColor(SWT.COLOR_WHITE);
                    } else {
                        rowColor = SWTResourceManager.getColor(SWT.COLOR_GRAY);
                    }
                    rowKey = key;
                }
                TableItem item = new TableItem(resultTable, SWT.NONE);
                item.setBackground(rowColor);
                // "ECO No", "Part Origin", "Part No", "Revision",
                // "��ȿ(Y)/����ȿ(N)", "NON EPL", "����", "MSG", "I/F DATE",
                // "Modify DATE"

                item.setText(0, StringUtil.nullToString((String) data.get("S7_ECO_NO")));
                item.setText(1, StringUtil.nullToString((String) data.get("S7_PART_TYPE")));
                item.setText(2, StringUtil.nullToString((String) data.get("PART_NO")));
                item.setText(3, StringUtil.nullToString((String) data.get("REVISION_ID")));
                item.setText(4, StringUtil.nullToString((String) data.get("IS_VALID")));                    
                item.setText(5, StringUtil.nullToString((String) data.get("NON_EPL")));
                item.setText(6, StringUtil.nullToString(this.getDatasetTypes(data)));
                item.setText(7, StringUtil.nullToString((String) data.get("IF_STATUS")));
                item.setText(8, StringUtil.nullToString((String) data.get("IF_ERROR_MSG")));
                item.setText(9, StringUtil.nullToString(((TIMESTAMP) data.get("IF_DATE")).dateValue().toString()));
                Timestamp modifyDate = (Timestamp) data.get("PLAST_MOD_DATE");
                String strModifyDate = "";
                if(modifyDate != null) {
                    strModifyDate = (new Date(modifyDate.getTime())).toString();                        
                }
                item.setText(10, strModifyDate);
                item.setData("IF_DATE", data.get("IF_DATE"));
                item.setData("GROUP_KEY", (String)data.get("GROUP_KEY"));
            }
            // Label ī��Ʈ ����
            countLabel.setText("[�� : " + rowCount + " ��]");
           
        } catch (Exception e) {            
            e.printStackTrace();
            MessageBox.post(getShell(), "Search Error", "ERROR", MessageBox.ERROR);
        } finally {
            waitProgress.close();
        }

    }

    /**
     * Datset List���� CAD TYPE ������ �����´�.
     * 
     * @method getDatasetTypes
     * @date 2013. 5. 30.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String getDatasetTypes(HashMap vehPartRowData) {
        StringBuffer rtDatasetTypes = new StringBuffer();
        List<HashMap> datasetList = (List<HashMap>) vehPartRowData.get("DATASET_FILE_LIST");
        if (datasetList == null || datasetList.size() == 0) {
            return "";
        }
        for (int i = 0; i < datasetList.size(); i++) {
            HashMap dataset = datasetList.get(i);
            String cadType = (String) dataset.get("CAD_TYPE");
            if (i == 0) {
                rtDatasetTypes.append(cadType);
            } else {
                rtDatasetTypes.append(" , ");
                rtDatasetTypes.append(cadType);
            }
        }
        return rtDatasetTypes.toString();
    }

    @Override
    protected boolean validationCheck() {
        return true;
    }
    
    /**
     * Status Code Index�� Status Code�� �����´�. 
     * 
     * @method getStatusCode 
     * @date 2013. 5. 30.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getStatusCode(int codeNo) {
        return STAT_COMBO_CODE[codeNo];        
    }
    
    /**
     * 
     * Validate Code Index�� Validate Code�� �����´�.
     * 
     * @method getValidCode 
     * @date 2013. 5. 31.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getValidCode(int codeNo) {
        return VALID_COMBO_CODE[codeNo];        
    }

}