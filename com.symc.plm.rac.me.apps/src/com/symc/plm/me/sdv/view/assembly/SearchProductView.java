/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVSortListenerFactory;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SearchMecoView
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class SearchProductView extends AbstractSDVViewPane {
    private Text txtId;
    private Text txtName;
    private Table table;
    private IDataMap resultDataMap = null;
    private Registry registry = null;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public SearchProductView(Composite parent, int style, String id) {
        super(parent, style, id);

    }

    protected void initUI(Composite parent) {
        registry = Registry.getRegistry(this);
        // FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        // fillLayout.marginWidth = 5;
        // fillLayout.marginHeight = 5;
        // setLayout(fillLayout);

        Composite compositCondition = new Composite(parent, SWT.NONE);
        GridLayout gl_compositCondition = new GridLayout(3, false);
        compositCondition.setLayout(gl_compositCondition);

        Label lblId = new Label(compositCondition, SWT.NONE);
        GridData gd_lblId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblId.widthHint = 105;
        lblId.setLayoutData(gd_lblId);
        lblId.setText(registry.getString("ProductId.NAME"));

        txtId = new Text(compositCondition, SWT.BORDER);
        GridData gd_txtId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtId.widthHint = 160;
        txtId.setLayoutData(gd_txtId);

        Button btnSearch = new Button(compositCondition, SWT.NONE);
        GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnSearch.widthHint = 60;
        gd_btnSearch.minimumWidth = 100;
        btnSearch.setLayoutData(gd_btnSearch);
        btnSearch.setText(registry.getString("Search.NAME"));

        btnSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                searchAction();
            }
        });

        Label lblName = new Label(compositCondition, SWT.NONE);
        lblName.setText(registry.getString("ProductName.NAME"));

        txtName = new Text(compositCondition, SWT.BORDER);
        GridData gd_textName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_textName.widthHint = 160;
        txtName.setLayoutData(gd_textName);
        new Label(compositCondition, SWT.NONE);
        GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSprator.horizontalSpan = 3;
        Label lSeparator = new Label(compositCondition, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.setLayoutData(gdSprator);

        table = new Table(compositCondition, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd_table.heightHint = 100;
        table.setLayoutData(gd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tblclmnNo = new TableColumn(table, SWT.NONE);
        tblclmnNo.setWidth(120);
        tblclmnNo.setText(registry.getString("ProductId.NAME"));
        tblclmnNo.addListener(SWT.Selection, SDVSortListenerFactory.getListener(SDVSortListenerFactory.STRING_COMPARATOR));

        TableColumn tblclmnName = new TableColumn(table, SWT.NONE);
        tblclmnName.setWidth(250);
        tblclmnName.setText(registry.getString("ProductName.NAME"));
        tblclmnName.addListener(SWT.Selection, SDVSortListenerFactory.getListener(SDVSortListenerFactory.STRING_COMPARATOR));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                int selectedIndex = table.getSelectionIndex();
                if (selectedIndex < 0)
                    return;
                resultDataMap = new RawDataMap();

                TableItem tableItem = table.getItem(selectedIndex);
                TCComponentItemRevision revision = (TCComponentItemRevision) tableItem.getData();

                resultDataMap.put("SRC_PRODUCT_REV", revision, IData.OBJECT_FIELD);
                // Double Click 시 확인 버튼 실행
                if (e.count != 2)
                    return;
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        ((Dialog) UIManager.getCurrentDialog()).close();
                    }
                });
                // Button button = dialog.getShell().getDefaultButton();
                // button.notifyListeners(SWT.Selection, new Event());
            }

        });
        
        getShell().setDefaultButton(btnSearch);

    }

    private void searchAction() {
        try {
            table.removeAll();
            String itemId = txtId.getText().trim();
            String itemName = txtName.getText().trim();

            // 검색
            TCComponent[] resultList = searchProductItem(itemId, itemName);
            // 검색 결과
            displayResultData(resultList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public TCComponent[] searchProductItem(String itemId, String itemName) throws Exception {
        ArrayList<String> entry = new ArrayList<String>();
        ArrayList<String> value = new ArrayList<String>();
        String queryName = "Item...";
        if (!itemId.isEmpty()) {
            entry.add("ItemID");
            value.add(itemId);
        }
        if (!itemName.isEmpty()) {
            entry.add("Name");
            value.add(itemName);
        }
        entry.add("Type");
        value.add("S7_Product");

        TCComponent[] comps = CustomUtil.queryComponent(queryName, entry.toArray(new String[entry.size()]), value.toArray(new String[value.size()]));
        return comps;
    }

    private void displayResultData(TCComponent[] resultList) throws Exception {

        if (resultList == null || resultList.length == 0) {
            // 메세지 처리

        } else {
            for (TCComponent comp : resultList) {
                TCComponentItem item = (TCComponentItem) comp;
                String itemId = item.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                String name = item.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME);
                TableItem rowItem = new TableItem(table, SWT.NONE);
                rowItem.setText(0, itemId);
                rowItem.setText(1, name);
                rowItem.setData(item.getLatestItemRevision());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        return resultDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        return resultDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {

    }

}
