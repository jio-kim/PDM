package com.symc.plm.me.sdv.view.meco;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sdv.core.util.UIUtil;

import swing2swt.layout.BorderLayout;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.utils.StringUtil;
import com.ssangyong.rac.kernel.SYMCBOMEditData;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.viewer.AbstractSDVViewer;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * [20140507] Project code Old/New 분리 대응
 *
 * @author bykim
 *
 */
public class ECOChangeHistoryView extends AbstractSDVViewer {

    private Registry registry;

    private Composite csCondition;
    private SWTComboBox cbProduct, cbBISelect, cbChangedChild;
    private DateTime txtFromDate, txtToDate;
    private Table table;

    private Color evenColor;

    private ArrayList<SYMCBOMEditData> arrEplData = null;

    public ECOChangeHistoryView(Composite parent) {
        super(parent);
    }

    @Override
    public void createPanel(Composite parent) {
        registry = Registry.getRegistry("com.ssangyong.common.common");

        Display display = parent.getDisplay();
        evenColor = new Color(display, 192, 214, 248);

        setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite composite = new Composite(this, SWT.NONE);
        composite.setLayout(new BorderLayout());

        csCondition = new Composite(composite, SWT.NONE);
        csCondition.setLayoutData(BorderLayout.NORTH);
        csCondition.setLayout(new GridLayout(8, false));
        csCondition.setBackground(UIUtil.getColor(SWT.COLOR_GRAY));

        Label lbProduct = new Label(csCondition, SWT.NONE);
        lbProduct.setAlignment(SWT.RIGHT);
        lbProduct.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        lbProduct.setText("Product");

        cbProduct = new SWTComboBox(csCondition, SWT.BORDER);
        cbProduct.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        cbProduct.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        SDVLOVUtils.comboValueSetting(cbProduct, "S7_PROJECT_CODE");

        GridData gd_emptyLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_emptyLabel.widthHint = 50;
        Label emptyLabel = new Label(csCondition, SWT.NONE);
        emptyLabel.setLayoutData(gd_emptyLabel);

        Label lblDate = new Label(csCondition, SWT.NONE);
        lblDate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        lblDate.setText("Date");

        txtFromDate = new DateTime(csCondition, SWT.BORDER | SWT.DROP_DOWN);
        GridData gd_txtFromDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtFromDate.widthHint = cbProduct.getSize().x;
        txtFromDate.setLayoutData(gd_txtFromDate);

        Label lbSeparator = new Label(csCondition, SWT.NONE);
        lbSeparator.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        lbSeparator.setText("-");

        txtToDate = new DateTime(csCondition, SWT.BORDER | SWT.DROP_DOWN);
        GridData gd_txtToDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtToDate.widthHint = cbProduct.getSize().x;
        txtToDate.setLayoutData(gd_txtFromDate);

        Button btnSearch = new Button(csCondition, SWT.NONE);
        btnSearch.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        btnSearch.setText("Search");
        btnSearch.setImage(registry.getImage("Search.ICON"));
        btnSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                table.removeAll();
                try {
                    if (validateCondition()) {
                        executeSearchProcess();
                    }
                } catch (Exception e1) {
                    MessageBox.post(getShell(), e1.getMessage(), "Error", MessageBox.ERROR);
                    e1.printStackTrace();
                }

            }
        });

        Label lbBISelect = new Label(csCondition, SWT.NONE);
        lbBISelect.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        lbBISelect.setText("BOM Instance");

        cbBISelect = new SWTComboBox(csCondition, SWT.BORDER);
        cbBISelect.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        cbBISelect.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        String[] biSelect = { "", "All", "Option", "S.Mode", "Etc" };
        SDVLOVUtils.comboValueSetting(cbBISelect, biSelect);

        new Label(csCondition, SWT.NONE);

        Label lbChangeType = new Label(csCondition, SWT.NONE);
        lbChangeType.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        lbChangeType.setText("Change Type");

        cbChangedChild = new SWTComboBox(csCondition, SWT.BORDER);
        cbChangedChild.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        cbChangedChild.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        String[] changeChild = { "", "All", "Add", "Del", "Change" };
        SDVLOVUtils.comboValueSetting(cbChangedChild, changeChild);

        Composite csProcessMonitor = new Composite(composite, SWT.BORDER);
        csProcessMonitor.setLayoutData(BorderLayout.CENTER);
        csProcessMonitor.setLayout(new GridLayout(1, false));

        initTable(csProcessMonitor);
    }

    /**
     *
     *
     * @method initTable
     * @date 2014. 3. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void initTable(Composite parent) {
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gridData.heightHint = 600;

        table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        table.setLayoutData(gridData);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setEnabled(true);

        createTableColumn("No", 50);
        createTableColumn("Proj.", 50);
        createTableColumn("Find No", 80);
        createTableColumn("C/T", 50);
        createTableColumn("Parent No", 100);
        createTableColumn("Parent Rev", 100);
        createTableColumn("Part Origin", 100);
        createTableColumn("Part No", 100);
        createTableColumn("Part Rev", 100);
        createTableColumn("Part Name", 200);
        createTableColumn("IC", 50);
        createTableColumn("Supply Mode", 120);
        createTableColumn("QTY", 50);
        createTableColumn("ALT", 50);
        createTableColumn("SEL", 50);
        createTableColumn("CAT", 50);
        createTableColumn("Color", 50);
        createTableColumn("Color Section", 120);
        createTableColumn("Module Code", 120);
        createTableColumn("PLT Stk", 80);
        createTableColumn("A/S Stk", 80);
        createTableColumn("Cost", 50);
        createTableColumn("Tool", 50);
        createTableColumn("Shown-On", 100);
        createTableColumn("Options", 150);
        createTableColumn("Change Desc", 200);
        createTableColumn("ECO No", 100);
        createTableColumn("Release Date", 150);
    }

    /**
     *
     *
     * @method createTableColumn
     * @date 2014. 3. 25.
     * @param
     * @return TableColumn
     * @exception
     * @throws
     * @see
     */
    private TableColumn createTableColumn(String columnName, int width) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(columnName);
        column.setWidth(width);
        column.setResizable(true);
        column.setMoveable(true);
        return column;
    }

    /**
     *
     *
     * @method getConditionInfo
     * @date 2014. 3. 24.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    public HashMap<String, Object> getConditionInfo() {
        HashMap<String, Object> conditionMap = null;

        try {
            if (validateCondition()) {
                // Product
                String product = cbProduct.getSelectedItem().toString();

                // BOM Instance
                String bomInstance = cbBISelect.getSelectedItem().toString();

                // Change Type
                String changeType = cbChangedChild.getSelectedItem().toString();

                // Date
                String yearFromDate = String.valueOf(txtFromDate.getYear());
                String monthFromDate = String.valueOf(txtFromDate.getMonth() + 1);
                monthFromDate = monthFromDate.length() == 1 ? "0" + monthFromDate : monthFromDate;
                String dayFromDate = String.valueOf(txtFromDate.getDay());
                dayFromDate = dayFromDate.length() == 1 ? "0" + dayFromDate : dayFromDate;

                String yearToDate = String.valueOf(txtToDate.getYear());
                String monthToDate = String.valueOf(txtToDate.getMonth() + 1);
                monthToDate = monthToDate.length() == 1 ? "0" + monthToDate : monthToDate;
                String dayToDate = String.valueOf(txtToDate.getDay());
                dayToDate = dayToDate.length() == 1 ? "0" + dayToDate : dayToDate;

                conditionMap = new HashMap<String, Object>();
                conditionMap.put("product", product);
                conditionMap.put("bomInstance", bomInstance);
                conditionMap.put("changeType", changeType);
                conditionMap.put("fromDate", yearFromDate + "-" + monthFromDate + "-" + dayFromDate);
                conditionMap.put("toDate", yearToDate + "-" + monthToDate + "-" + dayToDate);
            }
        } catch (Exception e) {
            MessageBox.post(getShell(), e.getMessage(), "Error", MessageBox.ERROR);
            e.printStackTrace();
        }

        return conditionMap;
    }

    /**
     *
     *
     * @method getTableDataList
     * @date 2014. 3. 24.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    public List<HashMap<String, Object>> getTableDataList() {
        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

        TableItem[] tableItems = table.getItems();
        for (TableItem tableItem : tableItems) {
            HashMap<String, Object> dataMap = new HashMap<String, Object>();

            dataMap.put("no", tableItem.getText(0));
            dataMap.put("project", tableItem.getText(1));
            dataMap.put("find_no", tableItem.getText(2));
            dataMap.put("ct", tableItem.getText(3));
            dataMap.put("parent_no", tableItem.getText(4));
            dataMap.put("parent_rev", tableItem.getText(5));
            dataMap.put("part_origin", tableItem.getText(6));
            dataMap.put("part_no", tableItem.getText(7));
            dataMap.put("part_rev", tableItem.getText(8));
            dataMap.put("part_name", tableItem.getText(9));
            dataMap.put("ic", tableItem.getText(10));
            dataMap.put("supply_mode", tableItem.getText(11));
            dataMap.put("qty", tableItem.getText(12));
            dataMap.put("alt", tableItem.getText(13));
            dataMap.put("sel", tableItem.getText(14));
            dataMap.put("cat", tableItem.getText(15));
            dataMap.put("color", tableItem.getText(16));
            dataMap.put("color_section", tableItem.getText(17));
            dataMap.put("module_code", tableItem.getText(18));
            dataMap.put("plt_stk", tableItem.getText(19));
            dataMap.put("as_stk", tableItem.getText(20));
            dataMap.put("cost", tableItem.getText(21));
            dataMap.put("tool", tableItem.getText(22));
            dataMap.put("shown_on", tableItem.getText(23));
            dataMap.put("options", tableItem.getText(24));
            dataMap.put("change_desc", tableItem.getText(25));
            dataMap.put("eco_no", tableItem.getText(26));
            dataMap.put("release_date", tableItem.getText(27));

            Color color = tableItem.getBackground();
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();
            if (red == 192 && green == 214 && blue == 248) {
                dataMap.put("isColorSetting", "1");
            }

            dataList.add(dataMap);
        }

        return dataList;
    }

    /**
     *
     *
     * @method validateCondition
     * @date 2014. 3. 25.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean validateCondition() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if ("".equals(BundleUtil.nullToString(cbProduct.getTextField().getText()))) {
            MessageDialog.openInformation(getShell(), "Information", "Should select any value in the Product field.");

            return false;
        }

        Date fromDate = simpleDateFormat.parse(txtFromDate.getYear() + "-" + txtFromDate.getMonth() + "-" + txtFromDate.getDay());
        Date toDate = simpleDateFormat.parse(txtToDate.getYear() + "-" + txtToDate.getMonth() + "-" + txtToDate.getDay());

        long diffDays = CustomUtil.diffOfDate(fromDate, toDate);
        if (diffDays / 30 > 12) {
            MessageDialog.openInformation(getShell(), "Information", "Should set within one year.");

            return false;
        }

        return true;
    }

    /**
     *
     *
     * @method executeSearchProcess
     * @date 2014. 3. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void executeSearchProcess() {
        arrEplData = null;

        new Job("Searching ECO History...") {
            @Override
            protected IStatus run(IProgressMonitor arg0) {
                try {
                    getDisplay().syncExec(new Runnable() {
                        public void run() {
                            setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
                        }
                    });

                    arrEplData = searchEcoHistory();
                    setTableData();
                } catch (final Exception e) {
                    getDisplay().syncExec(new Runnable() {
                        public void run() {
                            MessageBox.post(getShell(), e.getMessage(), "Notification", 2);
                        }
                    });

                    return Status.CANCEL_STATUS;
                } finally {
                    getDisplay().syncExec(new Runnable() {
                        public void run() {
                            setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
                        }
                    });
                }

                return Status.OK_STATUS;
            }
        }.schedule();
    }

    /**
     *
     *
     * @method searchEcoHistory
     * @date 2014. 3. 25.
     * @param
     * @return ArrayList<SYMCBOMEditData>
     * @exception
     * @throws
     * @see
     */
    public ArrayList<SYMCBOMEditData> searchEcoHistory() {
        ArrayList<SYMCBOMEditData> dataList = null;
        CustomMECODao dao = new CustomMECODao();
        final DataSet ds = new DataSet();

        try {
            csCondition.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        ds.put("PRODUCT", cbProduct.getTextField().getText());
                        ds.put("FROM_DATE", simpleDateFormat.parse(txtFromDate.getYear() + "-" + txtFromDate.getMonth() + "-" + txtFromDate.getDay()));
                        ds.put("TO_DATE", simpleDateFormat.parse(txtToDate.getYear() + "-" + txtToDate.getMonth() + "-" + txtToDate.getDay()));
                        ds.put("BI_TYPE", cbBISelect.getTextField().getText());
                        ds.put("CHANGE_CHILD", cbChangedChild.getTextField().getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            dataList = (ArrayList<SYMCBOMEditData>) dao.searchECOEplList(ds);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataList;
    }

    /**
     * DB에서 테이블 데이터를 가져와 랜더링한다.
     *
     * @method setTableData
     * @date 2013. 3. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setTableData() throws Exception {
        final ArrayList<SYMCBOMEditData> dataList = this.arrEplData;

        getDisplay().syncExec(new Runnable() {
            public void run() {
                try {
                    if (dataList == null || dataList.size() == 0) {
                        return;
                    }

                    ArrayList<String> addedEPLs = new ArrayList<String>();
                    for (int i = 0; i < dataList.size(); i++) {
                        String eplId = dataList.get(i).getEplId();
                        if (!addedEPLs.contains(eplId)) {
                            new EPLTableItem(dataList.get(i), true);
                            new EPLTableItem(dataList.get(i), false);
                            addedEPLs.add(eplId);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class EPLTableItem extends TableItem {
        private SYMCBOMEditData bomEditData;
        private boolean hasPart;

        private EPLTableItem(SYMCBOMEditData bomEditData, boolean isOld) {
            super(table, SWT.None);
            this.bomEditData = bomEditData;

            if (isOld) {
                setOldData();
            } else {
                setNewData();
            }

            setRowProperty();
        }

        private void setOldData() {
            if ("".equals(StringUtil.nullToString(bomEditData.getPartNoOld()))) {
                return;
            }
            hasPart = true;

            String[] rowOldItemData = new String[28];
            // No.
            rowOldItemData[0] = getRowNo() + "";
            // Proj.
            // [20140507] Project code Old/New 분리 대응
            rowOldItemData[1] = bomEditData.getProjectOld();
            // SEQ
            rowOldItemData[2] = bomEditData.getSeqOld();
            // C/T
            rowOldItemData[3] = bomEditData.getChangeType().equals("D") ? bomEditData.getChangeType() : "";
            // Parent No
            rowOldItemData[4] = bomEditData.getParentNo();
            // Parent Rev
            rowOldItemData[5] = bomEditData.getParentRev();
            // Part Origin
            rowOldItemData[6] = bomEditData.getPartOriginOld();
            // Part No
            rowOldItemData[7] = bomEditData.getPartNoOld();
            // Part Rev
            rowOldItemData[8] = bomEditData.getPartRevOld();
            // Part Name
            rowOldItemData[9] = bomEditData.getPartNameOld();
            // IC
            rowOldItemData[10] = bomEditData.getIcOld();
            // Supply Mode
            rowOldItemData[11] = bomEditData.getSupplyModeOld();
            // QTY
            rowOldItemData[12] = bomEditData.getQtyOld();
            // ALT
            rowOldItemData[13] = bomEditData.getAltOld();
            // SEL
            rowOldItemData[14] = bomEditData.getSelOld();
            // CAT
            rowOldItemData[15] = bomEditData.getCatOld();
            // Color
            rowOldItemData[16] = bomEditData.getColorIdOld();
            // Color Section
            rowOldItemData[17] = bomEditData.getColorSectionOld();
            // Module Code
            rowOldItemData[18] = bomEditData.getModuleCodeOld();
            // PLT Stk
            rowOldItemData[19] = bomEditData.getPltStkOld();
            // A/S Stk
            rowOldItemData[20] = bomEditData.getAsStkOld();
            // Cost
            rowOldItemData[21] = "";
            // Tool
            rowOldItemData[22] = "";
            // Shown-On
            rowOldItemData[23] = bomEditData.getShownOnOld();
            // Options
            rowOldItemData[24] = bomEditData.getVcOld() != null ? bomEditData.getVcOld().toString() : "";
            // Change Desc
            rowOldItemData[25] = bomEditData.getChangeType().equals("D") ? bomEditData.getChgDesc() : "";
            // ECO No
            rowOldItemData[26] = bomEditData.getEcoNo();
            // Release Date
            rowOldItemData[27] = bomEditData.getReleaseDate();

            setText(rowOldItemData);
        }

        private void setNewData() {
            if ("".equals(StringUtil.nullToString(bomEditData.getPartNoNew()))) {
                return;
            }
            hasPart = true;

            String[] rowNewItemData = new String[28];
            // No.
            rowNewItemData[0] = getRowNo() + "";
            // Proj.
            // [20140507] Project code Old/New 분리 대응
            rowNewItemData[1] = bomEditData.getProjectNew();
            // SEQ
            rowNewItemData[2] = bomEditData.getSeqNew();

            if (!"".equals(StringUtil.nullToString(bomEditData.getPartNoNew()))) {
                // C/T
                rowNewItemData[3] = bomEditData.getChangeType();
            }

            if (!"".equals(StringUtil.nullToString(bomEditData.getPartNoNew()))) {
                // Parent No
                rowNewItemData[4] = bomEditData.getParentNo();
                // Parent Rev
                rowNewItemData[5] = bomEditData.getParentRev();
            }

            // Part Origin
            rowNewItemData[6] = bomEditData.getPartOriginNew();
            // Part No
            rowNewItemData[7] = bomEditData.getPartNoNew();
            // Part Rev
            rowNewItemData[8] = bomEditData.getPartRevNew();
            // Part Name
            rowNewItemData[9] = bomEditData.getPartNameNew();
            // IC
            rowNewItemData[10] = bomEditData.getIcNew();
            // Supply Mode
            rowNewItemData[11] = bomEditData.getSupplyModeNew();
            // QTY
            rowNewItemData[12] = bomEditData.getQtyNew();
            // ALT
            rowNewItemData[13] = bomEditData.getAltNew();
            // SEL
            rowNewItemData[14] = bomEditData.getSelNew();
            // CAT
            rowNewItemData[15] = bomEditData.getCatNew();
            // Color
            rowNewItemData[16] = bomEditData.getColorIdNew();
            // Color Section
            rowNewItemData[17] = bomEditData.getColorSectionNew();
            // Module Code
            rowNewItemData[18] = bomEditData.getModuleCodeNew();
            // PLT Stk
            rowNewItemData[19] = ""; // rows.get(i).getPltStkOld();
            // A/S Stk
            rowNewItemData[20] = ""; // rows.get(i).getAsStkOld();
            // Cost
            rowNewItemData[21] = bomEditData.getCostNew();
            // Tool
            rowNewItemData[22] = bomEditData.getToolNew();
            // Shown-On
            rowNewItemData[23] = bomEditData.getShownOnNew();
            // Options
            rowNewItemData[24] = bomEditData.getVcNew() != null ? bomEditData.getVcNew().toString() : "";
            // Change Desc
            rowNewItemData[25] = bomEditData.getChgDesc();
            // ECO No
            rowNewItemData[26] = bomEditData.getEcoNo();
            // Release Date
            rowNewItemData[27] = bomEditData.getReleaseDate();

            setText(rowNewItemData);
        }

        private int getRowNo() {
            int row = table.indexOf(this);

            return row / 2 + 1;
        }

        public void setRowProperty() {
            int rowNum = getRowNo();
            if (hasPart) {
                setText(0, rowNum + "");
            }

            if (rowNum % 2 == 0) {
                setBackground(evenColor);
            } else {
                setBackground(table.getBackground());
            }
        }

        protected void checkSubclass() {

        }

    }

    public Composite getComposite() {
        return this;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }

    @Override
    public boolean isSavable() {
        return false;
    }

}
