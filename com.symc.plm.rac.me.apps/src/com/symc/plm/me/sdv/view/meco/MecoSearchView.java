/**
 * 
 */
package com.symc.plm.me.sdv.view.meco;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVSortListenerFactory;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SearchMecoView
 * Class Description :
 * 
 * [SR없음][20150305]shcho, 결제가 진행중인 MECO는 검색결과에 표시되지 않도록 수정 
 * 
 * @date 2013. 11. 13.
 * 
 */
public class MecoSearchView extends AbstractSDVViewPane {
    private Text txtMecoId;
    private Text txtMecoName;
    private SDVLOVComboBox lovCreator;
    private Button btnCheckButton;
    private boolean isOwningUser;
    private Table table;
    private TCSession tcSession = null;
    private IDataMap resultDataMap = null;
    private Button btnRelevantProject;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public MecoSearchView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        tcSession = SDVBOPUtilities.getTCSession();
        Registry registry = Registry.getRegistry(this);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Composite compositCondition = new Composite(composite, SWT.NONE);
        GridLayout gl_compositCondition = new GridLayout(4, false);
        compositCondition.setLayout(gl_compositCondition);
        // Layout Data
        GridData gd_grpShop = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        compositCondition.setLayoutData(gd_grpShop);

        Label lblMeco = new Label(compositCondition, SWT.NONE);
        GridData gd_lblMeco = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblMeco.widthHint = 105;
        lblMeco.setLayoutData(gd_lblMeco);
        lblMeco.setText(registry.getString("MecoNo.NAME"));

        txtMecoId = new Text(compositCondition, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtMeco = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtMeco.widthHint = 160;
        txtMecoId.setLayoutData(gd_txtMeco);

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
        new Label(compositCondition, SWT.NONE);
        
        Label lblMecoName = new Label(compositCondition, SWT.NONE);
        lblMecoName.setText(registry.getString("MecoName.NAME"));

        txtMecoName = new Text(compositCondition, SWT.BORDER | SWT.SINGLE);
        GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_text.widthHint = 160;
        txtMecoName.setLayoutData(gd_text);
        new Label(compositCondition, SWT.NONE);
        new Label(compositCondition, SWT.NONE);
        
        Label lblCreator = new Label(compositCondition, SWT.NONE);
        lblCreator.setText(registry.getString("Creator.NAME"));

        lovCreator = new SDVLOVComboBox(compositCondition, SWT.BORDER | SWT.SINGLE , tcSession, "User Ids");
        GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_combo.widthHint = 200;
        lovCreator.setLayoutData(gd_combo);

        btnCheckButton = new Button(compositCondition, SWT.CHECK);
        btnCheckButton.setText("Owned MECO");
        GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSprator.horizontalSpan = 3;

        btnCheckButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                isOwningUser = btnCheckButton.getSelection();
                try {
                    if (isOwningUser) {
                        tcSession = SDVBOPUtilities.getTCSession();
                        lovCreator.setSelectedIndex(-1);
                        lovCreator.setSelectedString(tcSession.getUser().toString());
                        lovCreator.setEnabled(false);
                    } else {
                        lovCreator.setSelectedIndex(-1);
                        lovCreator.setEnabled(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 작업 하면서 추가 
	         선택한 타겟의 프로젝트만 검색 할 수 있도록 체크 버튼 추가 */
        btnRelevantProject = new Button(compositCondition, SWT.CHECK);
        btnRelevantProject.setText("Relevant Project");
		GridData gdRelevantProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdRelevantProject.horizontalSpan = 1;
		btnRelevantProject.setLayoutData(gdRelevantProject);
		btnRelevantProject.setSelection(true);
        
        Label lSeparator = new Label(compositCondition, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.setLayoutData(gdSprator);

        table = new Table(compositCondition, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        table.setLayoutData(gd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        gd_table.heightHint = 100;

        TableColumn mecoNoColumn = new TableColumn(table, SWT.NONE);
        mecoNoColumn.setWidth(90);

        mecoNoColumn.setText("ID");

        mecoNoColumn.addListener(SWT.Selection, SDVSortListenerFactory.getListener(SDVSortListenerFactory.STRING_COMPARATOR));

        TableColumn creationDateColumn = new TableColumn(table, SWT.NONE);
        creationDateColumn.setWidth(120);
        creationDateColumn.setText("Date Created");

        creationDateColumn.addListener(SWT.Selection, SDVSortListenerFactory.getListener(SDVSortListenerFactory.STRING_COMPARATOR));

        TableColumn mecoNamColumn = new TableColumn(table, SWT.NONE);
        mecoNamColumn.setWidth(250);
        mecoNamColumn.setText("Description");

        mecoNamColumn.addListener(SWT.Selection, SDVSortListenerFactory.getListener(SDVSortListenerFactory.STRING_COMPARATOR));

        TableColumn creatorColumn = new TableColumn(table, SWT.NONE);
        creatorColumn.setWidth(120);
        creatorColumn.setText(registry.getString("Creator.NAME"));

        creatorColumn.addListener(SWT.Selection, SDVSortListenerFactory.getListener(SDVSortListenerFactory.STRING_COMPARATOR));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                int selectedIndex = table.getSelectionIndex();
                if (selectedIndex < 0)
                    return;
                resultDataMap = new RawDataMap();

                TableItem tableItem = table.getItem(selectedIndex);
                String mecoNo = (String) tableItem.getText();
                Object data = tableItem.getData();
                if (data == null) {
                    try {
                        TCComponentItem mecoItem = SDVBOPUtilities.FindItem(mecoNo, "Item");
                        data = mecoItem.getLatestItemRevision();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                resultDataMap.put("mecoNo", mecoNo);
                resultDataMap.put("mecoRev", data, IData.OBJECT_FIELD);
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
        btnCheckButton.setSelection(true);
        btnCheckButton.notifyListeners(SWT.Selection, new Event());
        
        txtMecoId.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                searchAction();
            }
        });
        
        txtMecoName.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                searchAction();
            }
        });
        
    }

    /**
     * 검색 이벤트 수행
     * 
     * [SR없음][20150305]shcho, 결제가 진행중인 MECO는 검색결과에 표시되지 않도록 수정 
     * [CF-3537] [20230131]isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 
     * 
     * @method searchAction
     * @date 2013. 11. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void searchAction() {
        try {
            tcSession = SDVBOPUtilities.getTCSession();
            table.removeAll();
            String mecoId = txtMecoId.getText().trim();
            String mecoName = txtMecoName.getText().trim();
            String creatorId = "";
            isOwningUser = btnCheckButton.getSelection();

            if (isOwningUser)
                creatorId = tcSession.getUser().getUserId();
            else {
                if (lovCreator.getSelectedObject() != null)
                    creatorId = lovCreator.getSelectedObject().toString();
            }
            // 검색
            TCComponent[] resultList = searchMECO(mecoId, mecoName, creatorId);
            
            ArrayList<TCComponent> revList = new ArrayList<TCComponent>();
            for (TCComponent result : resultList)
            {
//                if (CustomUtil.isWorkingStatus(result)) {
        	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
        	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 
        	         기존에는 CustomUtil.isWorkingStatus(result)만 사용해서 반려된 MECO가 검색 되지 않아서 result.getProperty("process_stage_list").contains("Creator")를 추가함*/
                if (CustomUtil.isWorkingStatus(result) || result.getProperty("process_stage_list").contains("Creator")) {
                    revList.add(result);                    
                }
            }
            
            TCComponent[] workingMECORevList = new TCComponent[revList.size()];
            workingMECORevList =  revList.toArray(workingMECORevList);
            
            // 검색 결과
            displayResultData(workingMECORevList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * MECO 검색 Saved Query 실행
     * 
     * @method searchMECO
     * @date 2013. 11. 14.
     * @param
     * @return TCComponent[]
     * @exception
     * @throws
     * @see
     */
    public TCComponent[] searchMECO(String itemId, String itemName, String creatorId) throws Exception {
        ArrayList<String> entry = new ArrayList<String>();
        ArrayList<String> value = new ArrayList<String>();
        String queryName = "SYMC_Search_Working_MECO_Revision";
        boolean isRelevantProject = btnRelevantProject.getSelection();
        if (!itemId.isEmpty()) {
            entry.add("MECONO");
            value.add(itemId);
        }else{
//        	MECONO가 null인 경우 * 를 기본값으로 사용하여 검색 속도 를 빠르게 함
            entry.add("MECONO");
            value.add("*");        	
        }
        if (!itemName.isEmpty()) {
            entry.add("Description");
            value.add(itemName);
        }
        if (!creatorId.isEmpty()) {
            entry.add("OWNINGUSERID");
            value.add(creatorId);
        }
	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 
	     btnRelevantProject버튼이 체크 되어 있을시 선택한 타겟의 프로젝트만 검색 되도록 수정*/
        if(isRelevantProject){
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();
            TCComponentBOMLine bomLine = bomWindow.getTopBOMLine();
            
            TCComponentItemRevision itemRevision = bomLine.getItemRevision();
            TCComponentItem productItem = CustomUtil.findItem("Item", itemRevision.getProperty("m7_PRODUCT_CODE"));
            TCComponentItemRevision revision = productItem.getLatestItemRevision();
    		String sProjectCode = revision.getProperty("s7_PROJECT_CODE");
        	entry.add("PROJECT");
        	value.add(sProjectCode);        	
        }

        TCComponent[] comps = CustomUtil.queryComponent(queryName, entry.toArray(new String[entry.size()]), value.toArray(new String[value.size()]));
        return comps;
    }

    private void displayResultData(TCComponent[] resultList) throws Exception {

        if (resultList == null || resultList.length == 0) {
            // 메세지 처리

        } else {
            for (TCComponent comp : resultList) {
                TCComponentItemRevision mecoRevision = (TCComponentItemRevision) comp;
                String mecoId = mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                // [Non SR] 윤순식 부장 요청 
                // MECO Change 기능에서 MEW(용접점 MECO 검색 안되는 현상 수정
//                if (mecoId.startsWith("MEW"))
//                    continue;
                String mecoDesc = mecoRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC);
                String creator = mecoRevision.getProperty(SDVPropertyConstant.ITEM_OWNING_USER);
                String creationDate = mecoRevision.getProperty(SDVPropertyConstant.ITEM_CREATION_DATE);
                TableItem rowItem = new TableItem(table, SWT.NONE);
                rowItem.setText(0, mecoId);
                rowItem.setText(1, creationDate);
                rowItem.setText(2, mecoDesc);
                rowItem.setText(3, creator);
                rowItem.setData(mecoRevision);
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
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getRootContext()
     */
    @Override
    public Composite getRootContext() {
        return null;
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
