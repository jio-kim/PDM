package com.kgm.commands.ec.eco;

import java.net.ConnectException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.kgm.common.SYMCLOVCombo;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.rac.kernel.SYMCPartListData;
import com.kgm.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20170116][ymjang] ECO D 지 저장 오류 수정
 * [20240306][UPGRADE] Table Column 사이즈 조절
 */
public class ECOPartListSWTRendering extends AbstractSYMCViewer {

    private Table table;
    private Color modifiedColor;
    private Color modifiableColor;
    private Composite main;
    private ArrayList<TableItem> modifiedTableData;
    /** Part No */
    public static final int PART_NO = 7;
    /** Column Index of PLT Stock */
    public static final int PLT_STK = 18;
    /** Column Index of A/S Stock */
    public static final int AS_STK = 19;
    /** Column Index of Cost */
    public static final int COST = 20;
    /** Column Index of Tool */
    public static final int TOOL = 21;
    /** Column Index of Change Description */
    public static final int CHANGE_DESC = 23;

    public ECOPartListSWTRendering(Composite parent) {
        super(parent);
    }

    /**
     * Panel 생성
     */
    @Override
    public void createPanel(Composite parent) {
        Display display = getDisplay();
        modifiedColor = new Color(display, 255, 225, 225);
        modifiableColor = new Color(display, 218, 237, 190);
        main = parent.getParent();

        table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createTableColumn("No", 40);
        createTableColumn("Proj.", 50);
        //createTableColumn("Find No", 57);
        //createTableColumn("C/T", 35);
        createTableColumn("Find No", 66);
        createTableColumn("C/T", 36);
        createTableColumn("Parent No", 100);
        createTableColumn("Parent Rev", 70);
        createTableColumn("Part Origin", 25);
        createTableColumn("Part No", 100);
        //createTableColumn("Part Rev", 60);
        createTableColumn("Part Rev", 68);
        createTableColumn("Part Name", 180);
        createTableColumn("Supply Mode", 90);
        createTableColumn("QTY", 35);
        createTableColumn("ALT", 40);
        createTableColumn("SEL", 40);
        createTableColumn("CAT", 50);
        createTableColumn("Color", 45);
        createTableColumn("Color Section", 120);
        createTableColumn("Module Code", 120);
        createTableColumn("PLT Stk", 80);
        createTableColumn("A/S Stk", 80);
        createTableColumn("Cost", 60);
        createTableColumn("Tool", 60);
        createTableColumn("Shown-On", 90);
        createTableColumn("Change Desc", 180);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if(isEditable() && e.button == 1) {
                    processCellEdit(e);
                }
            }
        });
        parent.pack();
        table.setSize(main.getClientArea().width, main.getClientArea().height);
    }

    /**
     * Create Table Column
     * 
     * @method createTableColumn
     * @date 2013. 3. 4.
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
     * Table Cell Mouse Down Event
     * 
     * @method addCellEditEvent 
     * @date 2013. 3. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void processCellEdit(MouseEvent event) {
        int[] selInxs = table.getSelectionIndices();
        if(selInxs != null && selInxs.length > 1) {
            return;
        }
        final TableEditor editor = new TableEditor(table);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;        
        Point pt = new Point(event.x, event.y);
        final int row = table.getSelectionIndex();
        if(row == -1 || row > table.getItemCount() - 1) {
            return;
        }
        final TableItem item = table.getItem(row);
        for (int i = 8 ; i < table.getColumnCount() ; i++) {
            Rectangle rect = item.getBounds(i);
            if (!rect.contains(pt)) {
                continue;
            }
            final int column = i;
            Scrollable cellComponent = getCellComponent(item, column);
            if (cellComponent == null) {
                continue;
            } else {
                final Control rendererComponent = (Control)cellComponent;
                Listener componentListener = new Listener() {
                    public void handleEvent(final Event e) {
                        String newValue = null;
                        if(rendererComponent instanceof SYMCLOVCombo) {
                            if(column == PLT_STK || column== AS_STK) {
                                newValue = ((SYMCLOVCombo)rendererComponent).getTextDesc();
                            } else {
                                newValue = ((SYMCLOVCombo)rendererComponent).getText();
                            }
                        } else if(rendererComponent instanceof Text) {
                            newValue = ((Text)rendererComponent).getText();
                        }
                        switch (e.type) {
                        case SWT.FocusOut:
                            setItemDataValue(item, column, newValue);
                            rendererComponent.dispose();
                            break;
                        case SWT.Selection:
                            setItemDataValue(item, column, newValue);
                            break;
                        case SWT.Traverse:
                            switch (e.detail) {
                            case SWT.TRAVERSE_RETURN:
                                setItemDataValue(item, column, newValue);
                            case SWT.TRAVERSE_ESCAPE:
                                rendererComponent.dispose();
                                e.doit = false;
                            }
                            break;
                        }
                    }
                };
                rendererComponent.addListener(SWT.FocusOut, componentListener);
                rendererComponent.addListener(SWT.Traverse, componentListener);
                editor.setEditor(rendererComponent, item, column);
                if(rendererComponent instanceof SYMCLOVCombo) {
                    if(column == PLT_STK || column == AS_STK) {
                        ((SYMCLOVCombo)rendererComponent).setTextDesc(item.getText(column));
                    } else {
                        ((SYMCLOVCombo)rendererComponent).setText(item.getText(column));
                    }
                } else if(rendererComponent instanceof Text) {
                    ((Text)rendererComponent).setText(item.getText(column));
                }
                rendererComponent.setFocus();

                return;
            }
        }
    }

    /**
     * Cell 편집 속성 제어
     * 
     * @method getCellComponent
     * @date 2013. 3. 5.
     * @param
     * @return Scrollable
     * @exception
     * @throws
     * @see
     */
    public Scrollable getCellComponent(TableItem item, int colunIndex) {
        String ct = ((SYMCPartListData)item.getData()).getChangeType();
        switch (colunIndex) {
        case PLT_STK:// OLD 등록
        case AS_STK:// OLD 등록
            if(ct.equals("D")) {
                return new SYMCLOVCombo(table, "S7_PLANT_AS_STOCK", SYMCLOVCombo.VIEW_DESC, false);
            }
            return null;
        case COST:// NEW 등록
            if(!ct.equals("D")) {
                return new Text(table, SWT.NONE);
            }
            return null;
        case TOOL:// NEW 등록
            if(!ct.equals("D")) {
                return new SYMCLOVCombo(table, "S7_YN");
            }
            return null;
        case CHANGE_DESC:// NEW, OLD 둘다 등록
            return new Text(table, SWT.NONE);
        default:
            return null;
        }
    }

    /**
     * Cell Component 변경 후 속성 업데이트
     * 
     * @method setItemDataValue
     * @date 2013. 3. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setItemDataValue(TableItem item, int colunIndex, String value) {
        value = StringUtil.nullToString(value);
        item.setText(colunIndex, value); // Set Text
        boolean modified = false;
        SYMCPartListData editData = (SYMCPartListData)item.getData();
        switch (colunIndex) {
        case PLT_STK:
            if (!value.equals(StringUtil.nullToString(editData.getPltStk()))) {
                modified = true;
            }
            break;
        case AS_STK:
            if (!value.equals(StringUtil.nullToString(editData.getAsStk()))) {
                modified = true;
            }
            break;
        case COST:
            if (!value.equals(StringUtil.nullToString(editData.getCost()))) {
                modified = true;
            }
            break;
        case TOOL:
            if (!value.equals(StringUtil.nullToString(editData.getTool()))) {
                modified = true;
            }
            break;
        case CHANGE_DESC:
            if (!value.equals(StringUtil.nullToString(editData.getDesc()))) {
                modified = true;
            }
            break;
        default:
        }
        if(modified) {
            item.setBackground(colunIndex, modifiedColor);
            if (!modifiedTableData.contains(item)) {
                modifiedTableData.add(item);
            }
        } else {
            item.setBackground(colunIndex, modifiableColor);
            if(modifiedTableData.contains(item) && !isTableItemChanged(editData, item)) {
                modifiedTableData.remove(item);
            }
        }
    }
    
    private boolean isTableItemChanged(SYMCPartListData partListData, TableItem item) {
        if (!StringUtil.nullToString(partListData.getPltStk()).equals(StringUtil.nullToString(item.getText(PLT_STK)))) {
            return true;
        }
        if (!StringUtil.nullToString(partListData.getAsStk()).equals(StringUtil.nullToString(item.getText(AS_STK)))) {
            return true;
        }
        if (!StringUtil.nullToString(partListData.getCost()).equals(StringUtil.nullToString(item.getText(COST)))) {
            return true;
        }
        if (!StringUtil.nullToString(partListData.getTool()).equals(StringUtil.nullToString(item.getText(TOOL)))) {
            return true;
        }
        if (!StringUtil.nullToString(partListData.getDesc()).equals(StringUtil.nullToString(item.getText(CHANGE_DESC)))) {
            return true;
        }
        return false;
    }

    public void setControlReadOnly(Control composite) {
        setEditable(false);
    }

    public void setControlReadWrite(Control composite) {
        setEditable(true);
    }

    @Override
    public void load() {
        new Job("Load...") {
            protected IStatus run(IProgressMonitor arg0) {
                try {
                    getDisplay().syncExec(new Runnable() {
                        public void run() {
                            setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
                        }
                    });
                    setTableData();
                    return Status.OK_STATUS;
                } catch(final Exception e) {
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
            }
        }.schedule();
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
    @SuppressWarnings("unchecked")
    private void setTableData() throws Exception {
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        // Modify List Clear
        if (modifiedTableData == null) {
            modifiedTableData = new ArrayList<TableItem>();
        } else {
            modifiedTableData.clear();
        }
        DataSet ds = new DataSet();
        ds.put("ecoNo", AIFUtility.getCurrentApplication().getTargetComponent().getProperty("item_id"));
        final ArrayList<SYMCPartListData> data = (ArrayList<SYMCPartListData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOPartList", ds);
        getDisplay().syncExec(new Runnable() {
            public void run() {
                table.removeAll(); // 테이블 데이터 삭제
                if (data == null || data.size() == 0) {
                    return;
                }
                for (int i = 0; i < data.size(); i++) {
                    TableItem item = new TableItem(table, SWT.None);
                    String[] itemData = new String[24];
                    // No.
                    itemData[0] = (i + 1) + "";
                    // Proj.
                    itemData[1] = data.get(i).getProject();
                    // SEQ
                    itemData[2] = data.get(i).getSeq();
                    // C/T
                    itemData[3] = data.get(i).getChangeType();
                    // Parent No
                    itemData[4] = data.get(i).getParentNo();
                    // Parent Rev
                    itemData[5] = data.get(i).getParentRev();
                    // Part Origin
                    itemData[6] = data.get(i).getPartOrigin();
                    // Part No
                    itemData[7] = data.get(i).getPartNo();
                    // Part Rev
                    itemData[8] = data.get(i).getPartRev();
                    // Part Name
                    itemData[9] = data.get(i).getPartName();
                    // Supply Mode
                    itemData[10] = data.get(i).getSupplyMode();
                    // QTY
                    itemData[11] = data.get(i).getQty();
                    // ALT
                    itemData[12] = data.get(i).getAlt();
                    // SEL
                    itemData[13] = data.get(i).getSel();
                    // CAT
                    itemData[14] = data.get(i).getCat();
                    // Color Id
                    itemData[15] = data.get(i).getColorSection();
                    // Color Section
                    itemData[16] = data.get(i).getColorSection();
                    // Module Code
                    itemData[17] = data.get(i).getModuleCode();
                    // PLT Stk
                    itemData[18] = data.get(i).getPltStk();
                    // A/S Stk
                    itemData[19] = data.get(i).getAsStk();
                    // Cost
                    itemData[20] = data.get(i).getCost();
                    // Tool
                    itemData[21] = data.get(i).getTool();
                    // Shown-On
                    itemData[22] = data.get(i).getShownOn();
                    // Change Desc
                    itemData[23] = data.get(i).getDesc();
                    
                    item.setText(itemData);
                    item.setData(data.get(i));
                    if(isEditable()) {
                        setModifiableCellColor(item, data.get(i));
                    }
                }
            }
        });
    }

    /**
     * 편집 가능한 Cell Color 설정
     * 
     * @method setModifiableCellColor
     * @date 2013. 3. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setModifiableCellColor(TableItem item, SYMCPartListData partListData) {
        String ct = partListData.getChangeType();
        if(ct.equals("D")) {
            item.setBackground(PLT_STK, modifiableColor);
            item.setBackground(AS_STK, modifiableColor);
        } else {
            item.setBackground(COST, modifiableColor);
            item.setBackground(TOOL, modifiableColor);
        }
        item.setBackground(CHANGE_DESC, modifiableColor);
    }

    @Override
    public void save() {
        if (modifiedTableData != null && modifiedTableData.size() > 0) {
            main.getDisplay().syncExec(new Runnable() {
                public void run() {
                    SYMCRemoteUtil remote = new SYMCRemoteUtil();
                    try {
                        setCursor(new Cursor(main.getShell().getDisplay(), SWT.CURSOR_WAIT));
                        DataSet ds = new DataSet();
                        
                        //[20170116][ymjang] ECO D 지 저장 오류 수정
                        ArrayList<SYMCPartListData> partList = new ArrayList<SYMCPartListData>();
                        SYMCPartListData paretListData = null;
                        for (int i = 0; i < modifiedTableData.size(); i++) {
                        	paretListData = new SYMCPartListData ();
                        	TableItem item = (TableItem) modifiedTableData.get(i);
                        	paretListData.setPartNo(item.getText(PART_NO));
                        	paretListData.setPltStk(item.getText(PLT_STK));
                        	paretListData.setAsStk(item.getText(AS_STK));
                        	paretListData.setCost(item.getText(COST));
                        	paretListData.setTool(item.getText(TOOL));
                        	paretListData.setDesc(item.getText(CHANGE_DESC));
                        	
                        	partList.add(paretListData);
						}
                        
                        ds.put("bomEditData", partList);
                        Boolean updateStatus = (Boolean) remote.execute("com.kgm.service.ECOHistoryService", "updateECOPartListProperties", ds);
                        if (updateStatus == Boolean.FALSE) {
                            throw new Exception("Properties Update Error");
                        }
                        setTableData(); // 테이블 데이터 Refresh.
                    } catch (ConnectException ce) {
                        ce.printStackTrace();
                        MessageBox.post(getShell(), ce.getMessage(), "Notification", 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.post(getShell(), e.getMessage(), "Notification", 1);
                    } finally {
                        setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
                    }
                }
            });
        }
    }

    @Override
    public boolean isSavable() {
        return true;
    }

    /**
     * 테이블 수정데이터가 있는지 확인한다.
     */
    @Override
    public boolean isDirty() {
        if (modifiedTableData.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

}
