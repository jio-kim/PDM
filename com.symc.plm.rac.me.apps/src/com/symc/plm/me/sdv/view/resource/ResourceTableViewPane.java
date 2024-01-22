/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.awt.Frame;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : Resource_View_Table
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class ResourceTableViewPane extends Composite {
    private TCTable table;
    private String queryName;
    private Label resultCountLabel;
    private static TCSession session;

    /*
     * QRY_dataset_display_option Value
     * Queries only the latest version of a dataset. Valid values are 1 to query all versions of a dataset, 2 to query only the latest version.
     */
    private static final String PREFERENCE_SERVICE_VALUE = "2";
    private static final String PREFERENCE_SERVICE_NAME = "QRY_dataset_display_option";

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     * @param tableHead
     */
    public ResourceTableViewPane(Composite parent, int style, String queryName) {
        super(parent, style);
        session = SDVBOPUtilities.getTCSession();
        this.queryName = queryName;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        Composite composite = new Composite(this, SWT.EMBEDDED);
        composite.setLayoutData(BorderLayout.CENTER);

        Frame frame = SWT_AWT.new_Frame(composite);

        Panel panel = new Panel();
        frame.add(panel);
        panel.setLayout(new java.awt.BorderLayout(0, 0));

        JRootPane rootPane = new JRootPane();
        panel.add(rootPane);
        JScrollPane scrollPane = new JScrollPane();

        try {

            TCComponentQuery queryComponent = getQueryComponent(queryName);
            // String[] columnNames = new String[]{SDVPropertyConstant.ITEM_ITEM_ID, SDVPropertyConstant.ITEM_REVISION_ID, SDVPropertyConstant.ITEM_OBJECT_TYPE, SDVPropertyConstant.ITEM_OBJECT_NAME, SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST};
            // table = new TCTable(session, headColumn);
            // table = new TCTable(arg0, arg1, arg2)
            // SYMC_Search_Equipment_Revision_ColumnPreferences
            // SYMC_Search_Subsidiary_Revision_ColumnPreferences
            String s = (new StringBuilder()).append(queryComponent.toString()).append("_").append("ColumnPreferences").toString();
            String s1 = (new StringBuilder()).append(queryComponent.toString()).append("_").append("ColumnWidthPreferences").toString();
            boolean isEnableDnDrop = false;

            table = new TCTable(session, s, s1, queryComponent.getQueryResultType().getType(), isEnableDnDrop);

            rootPane.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
            scrollPane.getViewport().add(table);
            table.setEditable(false);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } catch (TCException e) {
            e.printStackTrace();
        }

        Composite composite2 = new Composite(this, SWT.NONE);
        composite2.setLayoutData(BorderLayout.SOUTH);
        composite2.setLayout(new FormLayout());
        composite2.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        resultCountLabel = new Label(composite2, SWT.NONE);
        FormData fbresultCount = new FormData();
        fbresultCount.bottom = new FormAttachment(100);
        fbresultCount.right = new FormAttachment(100, -5);
        fbresultCount.width = 50;
        resultCountLabel.setLayoutData(fbresultCount);
        resultCountLabel.setAlignment(SWT.RIGHT);
        resultCountLabel.setText("0");
    }

    /**
     * @return
     * @throws TCException
     */
    protected TCComponentQuery getQueryComponent(String queryName) throws TCException {
        TCComponentQueryType queryType = (TCComponentQueryType) SDVBOPUtilities.getTCSession().getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(queryName);
        return query;
    }

    /**
     * 자원 검색 수행 (SavedQuery를 사용)
     * 
     * @param queryName
     * @param searchConditionMap
     */
    public void searchResource(String queryName, HashMap<String, String> searchConditionMap) {
        // 현재 검색 결과 삭제
        table.removeAllRows();

        // queryEntries, entryValues 셋팅
        ArrayList<String> queryEntries = new ArrayList<String>();
        ArrayList<String> entryValues = new ArrayList<String>();
        for (String key : searchConditionMap.keySet()) {
            String value = searchConditionMap.get(key);
            if (value.length() > 0 && value != null) {
                queryEntries.add(key);
                entryValues.add(value);
            }
        }

        /*
         * queryEntries, entryValues 샘플
         * String[] queryEntries = {"Korean Name", "Spec Korean"};
         * String[] entryValues= {"실러", ""};
         * String[] queryEntries = {"OwningUser", "OwningGroup"};
         * String[] entryValues= {"shcho", "MFG"};
         * TCComponent[] tcComponents = queryComponent(queryName, queryEntries, entryValues);
         */

        try {
            // 쿼리 검색 수행 (queryComponent)
            TCComponent[] tcComponents = queryComponent(queryName, queryEntries.toArray(new String[queryEntries.size()]), entryValues.toArray(new String[entryValues.size()]));

            // 검색 결과 table에 삽입
            int searchedItemCount = 0;
            for (TCComponent component : tcComponents) {
                if (component instanceof TCComponentItemRevision) {
                    TCComponentItemRevision compItemRevision = (TCComponentItemRevision) component;
                    // 최종 Revision만 검색 결과로 사용
                    if (compItemRevision.getProperty("item_revision_id").equals(compItemRevision.getItem().getLatestItemRevision().getProperty("item_revision_id"))) {
                        table.addRows(compItemRevision);
                        searchedItemCount++;
                    }
                }
            }

            // 결과 Count
            resultCountLabel.setText(String.valueOf(searchedItemCount));

            // 검색 결과가 없으면 메시지 표시
            if (searchedItemCount == 0) {
                MessageBox.post(getShell(), "No Result.", "INFORMATION", MessageBox.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 이미 만들어져 있는 Saved query를 이용하여 imanComponent를 검색하는 method이다.
     * 
     * @param savedQueryName
     *            String 저장된 query name
     * @param entryName
     *            String[] 검색 조건 name(오리지날 name)
     * @param entryValue
     *            String[] 검색 조건 value
     * @return TCComponent[] 검색 결과
     * @throws Exception
     * 
     */
    public static TCComponent[] queryComponent(String savedQueryName, String[] entryName, String[] entryValue) throws Exception {

        int scope = TCPreferenceService.TC_preference_user;
//        session.getPreferenceService().setString(scope, PREFERENCE_SERVICE_NAME, PREFERENCE_SERVICE_VALUE);
        session.getPreferenceService().setStringValueAtLocation(PREFERENCE_SERVICE_NAME, PREFERENCE_SERVICE_VALUE, TCPreferenceLocation.convertLocationFromLegacy(scope));

        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(savedQueryName);
        String[] queryEntries = session.getTextService().getTextValues(entryName);
        for (int i = 0; queryEntries != null && i < queryEntries.length; i++) {
            if (queryEntries[i] == null || queryEntries[i].equals("")) {
                queryEntries[i] = entryName[i];
            }
        }
        return query.execute(queryEntries, entryValue);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    /**
     * TCTable에 선택된 Item을 return 하는 함수
     * 
     * @return InterfaceAIFComponent[]
     */
    public InterfaceAIFComponent[] getSelectedItems() {
        return table.getSelectedComponents();
    }

    /**
     * TCTable에 있는 모든 Component return하는 함수
     * 
     * @return AIFComponentContext[]
     */
    public AIFComponentContext[] getComponentList() {
        return table.getRows(0, table.getRowCount() - 1);
    }

    /**
     * TCTable에 표시된 모든 값을 return 하는 함수
     * 20201118 seho table의 값을 가져올때 구분자 %%를 사용하면 실제 데이터 %%가 존재할 경우 한칸씩 밀리는 증상 발생. 구분자를 %%로 하면 안된다.
     * 
     * @return List<List<String>>
     */
    public List<List<String>> getAllRowValues() {
//        String separator = "%%";
//        int[] columnIndexes = new int[table.getColumnCount()];
//        for (int i = 0; i < columnIndexes.length; i++) {
//            columnIndexes[i] = i;
//        }

//        return table.getRowData(separator, false, columnIndexes);
    	List<List<String>> allDataList = new ArrayList<List<String>>();
		List<String> columnList = new ArrayList<String>();
    	for(int c=0;c<table.getColumnCount();c++)
    	{
    		columnList.add(table.getColumnName(c));
    	}
		allDataList.add(columnList);
    	for(int i = 0;i<table.getRowCount();i++)
    	{
    		List<String> rowDataList = new ArrayList<String>();
    		Object[] rowObject = table.getRowData(i);
    		for(int j=0;j<rowObject.length;j++)
    		{
    			rowDataList.add(rowObject[j].toString());
    		}
    		allDataList.add(rowDataList);
    	}
    	return allDataList;
    }

}
