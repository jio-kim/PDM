package com.symc.plm.me.sdv.view.resource;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.sdv.core.common.exception.ValidateSDVException;
import org.springframework.util.StringUtils;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ResourceTabViewPane extends Composite {
    private Registry registry;

    /* Tab 목록 */
    public static final String TAB_HEAD_LIST = "Equipment,Tool,Subsidirary";

    /* TabItem List */
    private TabFolder tabFolder;

    /* TabItem List */
    private ArrayList<TabItem> tabItemList;

    /* Button List */
    private ArrayList<Button> buttonList;

    /* ResourceTableViewPane List */
    private ArrayList<ResourceTableViewPane> tableViewPaneList;
    private ResourceTableViewPane resourceTableViewPane;

    /* ResourceSearchViewPane List */
    private ArrayList<ResourceSearchViewPane> searchViewPaneList;
    private ResourceSearchViewPane searchViewPane;

    /* Query Name */
    public static final String EQUIP_QUERY_NAME = "SYMC_Search_Equipment_Revision";
    public static final String TOOL_QUERY_NAME = "SYMC_Search_Tool_Revision";
    public static final String SUBSIDIARY_QUERY_NAME = "SYMC_Search_Subsidiary_Revision";

    // public static final String JIGFIXTURE_QUERY_NAME = "SYMC_Search_JigFixture_Revision";
    // public static final String ROBOT_QUERY_NAME = "SYMC_Search_Robot_Revision";
    // public static final String GUN_QUERY_NAME = "SYMC_Search_Gun_Revision";

    /**
     * Create the ResourceTabViewPane.
     * 
     * @param parent
     * @param style
     */
    public ResourceTabViewPane(Composite parent, int style) {
        super(parent, style);
        registry = Registry.getRegistry(this);

        // 화면 UI 그리기
        initUI();

        // UI생성 후 ExpandBar안의 ExpandItem 크기 조정
        resize();
    }

    protected void initUI() {

        setLayout(new FillLayout(SWT.HORIZONTAL));
        setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

        /* TabFolder 생성 */
        Composite viewComposite = new Composite(this, SWT.NONE);
        viewComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
        tabFolder = new TabFolder(viewComposite, SWT.NONE);
        String[] tabHeadList = getTabHead();

        tabItemList = new ArrayList<TabItem>();
        buttonList = new ArrayList<Button>();
        tableViewPaneList = new ArrayList<ResourceTableViewPane>();
        searchViewPaneList = new ArrayList<ResourceSearchViewPane>();

        /* ----------------------------------------------------------------------------- */

        for (int i = 0; i < tabHeadList.length; i++) {
            /* TabItem 생성 */
            String tabHeadName = tabHeadList[i];
            tabItemList.add(i, new TabItem(tabFolder, SWT.NONE));
            tabItemList.get(i).setText(tabHeadName);

            /* TabItem에 Composite 생성 */
            Composite tabItemComposite = new Composite(tabFolder, SWT.NONE);
            // tabItemComposite.setLayout(new FillLayout(SWT.VERTICAL));
            tabItemComposite.setLayout(new FormLayout());
            tabItemList.get(i).setControl(tabItemComposite);

            /* TabItem의 Composite 상단을 ExpandBar로 구성 ------------------------------------------ */
            final ExpandBar expandBar = new ExpandBar(tabItemComposite, SWT.NONE);
            expandBar.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

            FormData tabItemCompositeFormData1 = new FormData();
            tabItemCompositeFormData1.top = new FormAttachment(0, +5);
            tabItemCompositeFormData1.left = new FormAttachment(0, 0);
            tabItemCompositeFormData1.right = new FormAttachment(100, 0);
            tabItemCompositeFormData1.bottom = new FormAttachment(0, +10);

            expandBar.setLayoutData(tabItemCompositeFormData1);

            /* ExpandBar에 ExpandItem 생성 */
            final ExpandItem expandItem = new ExpandItem(expandBar, SWT.NONE);
            expandItem.setExpanded(true);
            expandItem.setText("Search Condition");

            /* ExpandItem에 SearchComposite 생성 */
            Composite SearchComposite = new Composite(expandBar, SWT.NONE);
            expandItem.setControl(SearchComposite);
            expandItem.setHeight(expandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y + 100);

            /* SearchComposite 에 검색 버튼 및 검색 필드 생성 */
            // 검색 버튼 생성
            SearchComposite.setLayout(new FormLayout());
            buttonList.add(new Button(SearchComposite, SWT.PUSH));
            // buttonList.get(i).setText(tabHeadName + " Search");
            buttonList.get(i).setText("Search");
            buttonList.get(i).setToolTipText(tabHeadName + "toolTip");

            FormData bttnformData = new FormData();
            bttnformData.top = new FormAttachment(0, +5);
            bttnformData.right = new FormAttachment(100, -5);

            buttonList.get(i).setLayoutData(bttnformData);

            // 검색 필드 생성 (해당 쿼리의 속성 리스트를 읽어들여 검색필드에 뿌려준다)
            searchViewPane = new ResourceSearchViewPane(SearchComposite, SWT.NONE, getQueryType(i));
            FormData searchViewFormData = new FormData();
            searchViewFormData.top = new FormAttachment(buttonList.get(i), +1);
            searchViewFormData.left = new FormAttachment(0, 0);
            searchViewFormData.right = new FormAttachment(100, 0);
            searchViewFormData.bottom = new FormAttachment(100, 0);
            searchViewPane.setLayoutData(searchViewFormData);
            searchViewPaneList.add(searchViewPane);
            /* ------------------------------------------------------------------------------------- */

            /* TabItem의 Composite 하단을 Table을 담고 있는 Composite로 구성 -------------------------- */
            Composite resultComposite = new Composite(tabItemComposite, SWT.NONE);

            FormData tabItemCompositeFormData3 = new FormData();
            tabItemCompositeFormData3.top = new FormAttachment(expandBar, +5, SWT.BOTTOM);
            tabItemCompositeFormData3.left = new FormAttachment(expandBar, 0, SWT.LEFT);
            tabItemCompositeFormData3.right = new FormAttachment(expandBar, 0, SWT.RIGHT);
            tabItemCompositeFormData3.bottom = new FormAttachment(100, -5);

            resultComposite.setLayoutData(tabItemCompositeFormData3);
            resultComposite.setLayout(new FillLayout());

            // 리스트 테이블 생성
            resourceTableViewPane = new ResourceTableViewPane(resultComposite, SWT.NONE, getQueryType(i));
            tableViewPaneList.add(resourceTableViewPane);
            /* ------------------------------------------------------------------------------------- */

            /* ExpandBar Event 추가 --------------------------------------------------------------- */
            expandBar.addExpandListener(new ExpandListener() {
                @Override
                public void itemExpanded(ExpandEvent e) {
                    resizeExpandBar(expandBar.getItem(0).getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y, expandBar);
                }

                @Override
                public void itemCollapsed(ExpandEvent e) {
                    resizeExpandBar(0, expandBar);
                }
            });
            /* ------------------------------------------------------------------------------------- */
        }

        /* Button Event */
        // Equipment Button
        buttonList.get(0).addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    searchResourceEvenet(0, registry.getString("Equipment.Query.NAME"));
                } catch (Exception e2) {
                    MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e2.getMessage(), "ERROR", MessageBox.ERROR);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // Tool Button
        buttonList.get(1).addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    searchResourceEvenet(1, registry.getString("Tool.Query.NAME"));
                } catch (Exception e2) {
                    MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e2.getMessage(), "ERROR", MessageBox.ERROR);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        // Subsidiary Button
        buttonList.get(2).addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    searchResourceEvenet(2, registry.getString("Subsidiary.Query.NAME"));
                } catch (Exception e2) {
                    MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e2.getMessage(), "ERROR", MessageBox.ERROR);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

    }

    // /**
    // * @param tableIndex
    // * @param tableHeadColumn
    // */
    // protected ArrayList<String> getPropertiesName(int tableIndex) {
    // ArrayList<String> list = new ArrayList<String>();
    // // 설비 Table Column
    // if (tableIndex == 0) {
    // list.add(SDVPropertyConstant.ITEM_ITEM_ID);
    // list.add(SDVPropertyConstant.ITEM_REVISION_ID);
    // list.add(SDVPropertyConstant.ITEM_OBJECT_TYPE);
    // list.add(SDVPropertyConstant.ITEM_OBJECT_NAME);
    // list.add(SDVPropertyConstant.EQUIP_ENG_NAME);
    // list.add(SDVPropertyConstant.EQUIP_SHOP_CODE);
    // list.add(SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY);
    // list.add(SDVPropertyConstant.EQUIP_MAIN_CLASS);
    // list.add(SDVPropertyConstant.EQUIP_SUB_CLASS);
    // list.add(SDVPropertyConstant.EQUIP_VEHICLE_CODE);
    // list.add(SDVPropertyConstant.EQUIP_STATION_CODE);
    // list.add(SDVPropertyConstant.EQUIP_POSITION_CODE);
    // list.add(SDVPropertyConstant.EQUIP_LINE_CODE);
    // list.add(SDVPropertyConstant.EQUIP_AXIS);
    // list.add(SDVPropertyConstant.EQUIP_SERVO);
    // list.add(SDVPropertyConstant.EQUIP_ROBOT_TYPE);
    // list.add(SDVPropertyConstant.EQUIP_MAKER_NO);
    // list.add(SDVPropertyConstant.EQUIP_SPEC_KOR);
    // list.add(SDVPropertyConstant.EQUIP_SPEC_ENG);
    // list.add(SDVPropertyConstant.EQUIP_CAPACITY);
    // list.add(SDVPropertyConstant.EQUIP_MAKER);
    // list.add(SDVPropertyConstant.EQUIP_NATION);
    // list.add(SDVPropertyConstant.EQUIP_INSTALL_YEAR);
    // list.add(SDVPropertyConstant.EQUIP_PURPOSE_KOR);
    // list.add(SDVPropertyConstant.EQUIP_PURPOSE_ENG);
    // list.add(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
    // }
    //
    // // 공구 Table Column
    // if (tableIndex == 1) {
    // list.add(SDVPropertyConstant.ITEM_ITEM_ID);
    // list.add(SDVPropertyConstant.ITEM_REVISION_ID);
    // list.add(SDVPropertyConstant.ITEM_OBJECT_TYPE);
    // list.add(SDVPropertyConstant.ITEM_OBJECT_NAME);
    // list.add(SDVPropertyConstant.TOOL_ENG_NAME);
    // list.add(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY);
    // list.add(SDVPropertyConstant.TOOL_MAIN_CLASS);
    // list.add(SDVPropertyConstant.TOOL_SUB_CLASS);
    // list.add(SDVPropertyConstant.TOOL_PURPOSE);
    // list.add(SDVPropertyConstant.TOOL_SPEC_CODE);
    // list.add(SDVPropertyConstant.TOOL_SPEC_KOR);
    // list.add(SDVPropertyConstant.TOOL_SPEC_ENG);
    // list.add(SDVPropertyConstant.TOOL_TORQUE_VALUE);
    // list.add(SDVPropertyConstant.TOOL_UNIT_USAGE);
    // list.add(SDVPropertyConstant.TOOL_MATERIAL);
    // list.add(SDVPropertyConstant.TOOL_MAKER);
    // list.add(SDVPropertyConstant.TOOL_MAKER_AF_CODE);
    // list.add(SDVPropertyConstant.TOOL_TOOL_SHAPE);
    // list.add(SDVPropertyConstant.TOOL_TOOL_LENGTH);
    // list.add(SDVPropertyConstant.TOOL_TOOL_SIZE);
    // list.add(SDVPropertyConstant.TOOL_TOOL_MAGNET);
    // list.add(SDVPropertyConstant.TOOL_VEHICLE_CODE);
    // list.add(SDVPropertyConstant.TOOL_STAY_TYPE);
    // list.add(SDVPropertyConstant.TOOL_STAY_AREA);
    // list.add(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
    // }
    //
    // // 부자재 Table Column
    // if (tableIndex == 2) {
    // list.add(SDVPropertyConstant.ITEM_ITEM_ID);
    // list.add(SDVPropertyConstant.ITEM_REVISION_ID);
    // list.add(SDVPropertyConstant.ITEM_OBJECT_TYPE);
    // list.add(SDVPropertyConstant.ITEM_OBJECT_NAME);
    // list.add(SDVPropertyConstant.SUBSIDIARY_MATERIAL_TYPE);
    // list.add(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP);
    // list.add(SDVPropertyConstant.SUBSIDIARY_PARTQUAL);
    // list.add(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR);
    // list.add(SDVPropertyConstant.SUBSIDIARY_SPEC_ENG);
    // list.add(SDVPropertyConstant.SUBSIDIARY_OLDPART);
    // list.add(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT);
    // list.add(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT);
    // list.add(SDVPropertyConstant.SUBSIDIARY_MAKER);
    // list.add(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
    //
    // // try {
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.ITEM_ITEM_ID));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.ITEM_REVISION_ID));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.ITEM_OBJECT_TYPE));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.ITEM_OBJECT_NAME));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_ENG_NAME));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_MATERIAL_TYPE));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_PARTQUAL));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SPEC_KOR));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SPEC_ENG));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_OLDPART));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_BUY_UNIT));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_MAKER));
    // // list.add(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST));
    // // } catch (Exception e) {
    // // e.printStackTrace();
    // // }
    //
    // }
    // return list;
    // }

    // UI생성 후 ExpandBar안의 ExpandItem 크기 조정
    private void resize() {
        for (int i = 0; i < tabItemList.size(); i++) {
            Composite tabItemComposite = (Composite) tabItemList.get(i).getControl();
            Control[] controls = tabItemComposite.getChildren();
            int expandItemHeight = 0;
            for (Control control : controls) {
                if (control instanceof ExpandBar) {
                    ExpandItem expandItem = ((ExpandBar) control).getItem(0);
                    expandItemHeight = expandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                    expandItem.setHeight(expandItemHeight);

                    resizeExpandBar(expandItemHeight, ((ExpandBar) control));
                }
            }
        }
    }

    /**
     * ExpandBar의 높이를 ExpandItem의 높이에 맞게 재설정 한다.
     * 
     * @param expandItemHeight
     * @param control
     */
    public void resizeExpandBar(int expandItemHeight, ExpandBar expandBar) {
        FormData tabItemCompositeFormData1 = (FormData) expandBar.getLayoutData();
        tabItemCompositeFormData1.top = new FormAttachment(0, +5);
        tabItemCompositeFormData1.left = new FormAttachment(0, 0);
        tabItemCompositeFormData1.right = new FormAttachment(100, 0);
        tabItemCompositeFormData1.bottom = new FormAttachment(0, expandItemHeight + 34);

        /*
         * 중요!!
         * LayoutData 크기 변경 후 반드시 부모의 layout을 호출하여 반영하여야 한다. (그렇지 않으면 화면에 반영안됨)
         */
        expandBar.getParent().layout();
    }

    /**
     * 
     */
    private void searchResourceEvenet(int index, String queryName) throws Exception {
        if (searchViewPaneList.get(index) == null) {
            throw new ValidateSDVException("SearchViewPane이 없습니다. - " + index);
        }

        HashMap<String, String> searchConditionMap = searchViewPaneList.get(index).getCondition();

        int emptyCount = 0;
        for (String key : searchConditionMap.keySet()) {
            if (StringUtils.isEmpty(searchConditionMap.get(key))) {
                emptyCount++;
            }
        }

        if (emptyCount == searchConditionMap.size()) {
            throw new ValidateSDVException("No criteria input.");
        }

        tableViewPaneList.get(index).searchResource(queryName, searchConditionMap);
    }

    public ResourceTableViewPane getCurrentTable() {
        final ArrayList<Integer> arrIndex = new ArrayList<Integer>();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                arrIndex.add(tabFolder.getSelectionIndex());
            }
        });

        return tableViewPaneList.get(arrIndex.get(0));
    }

    public Button getCurrentSearchButton() {
        final ArrayList<Integer> arrIndex = new ArrayList<Integer>();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                arrIndex.add(tabFolder.getSelectionIndex());
            }
        });
        return buttonList.get(arrIndex.get(0));
    }

    /**
     * Query Name을 찾아오는 함수
     * 
     * @param int
     * @return String
     */
    private String getQueryType(int tabNum) {
        String queryName = "";

        switch (tabNum) {
        case 0:
            queryName = EQUIP_QUERY_NAME;
            break;
        case 1:
            queryName = TOOL_QUERY_NAME;
            break;
        case 2:
            queryName = SUBSIDIARY_QUERY_NAME;
            break;

        default:
            queryName = EQUIP_QUERY_NAME;
            break;
        }

        return queryName;
    }

    /**
     * Tab 목록을 가져오는 함수
     * 
     * @return String[]
     */
    private String[] getTabHead() {
        String[] arrHeadList = TAB_HEAD_LIST.split(",");
        return arrHeadList;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
