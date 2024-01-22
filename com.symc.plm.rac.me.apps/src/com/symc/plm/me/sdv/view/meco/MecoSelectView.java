/**
 * 
 */
package com.symc.plm.me.sdv.view.meco;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : MecoSelectView
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class MecoSelectView extends AbstractSDVViewPane {
    private SDVText txtMeco;
    private IDataMap mecoDataMap = null;
    private Button btnSearch;
    /**
     * @param parent
     * @param style
     * @param id
     */
    public MecoSelectView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        Registry registry = Registry.getRegistry(this);

        Group grpMeco = new Group(parent, SWT.NONE);
        grpMeco.setText("MECO");
        grpMeco.setLayout(new GridLayout(1, false));

        Composite composite = new Composite(grpMeco, SWT.NONE);
        GridLayout gl_composite = new GridLayout(3, false);
        gl_composite.marginLeft = 10;
        composite.setLayout(gl_composite);
        // Grid Data
        // Layout Data
        GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        composite.setLayoutData(gd_composite);

        Label lblMeco = new Label(composite, SWT.NONE);
        GridData gd_lblMeco = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblMeco.widthHint = 105;
        lblMeco.setLayoutData(gd_lblMeco);

        lblMeco.setText(registry.getString("MecoNo.NAME"));

        txtMeco = new SDVText(composite, SWT.BORDER);
        GridData gd_txtMeco = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtMeco.widthHint = 160;
        txtMeco.setLayoutData(gd_txtMeco);

        btnSearch = new Button(composite, SWT.NONE);
        GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnSearch.widthHint = 60;
        gd_btnSearch.minimumWidth = 100;
        btnSearch.setLayoutData(gd_btnSearch);

        btnSearch.setText(registry.getString("Search.NAME"));

        txtMeco.setEnabled(false);
        txtMeco.setMandatory(true);
        
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * 공법 수정 화면 추가로 인한 로직 수정 
         * Dilaog Id 가 Modify 일때만 작동
         * MECO 는 수정 불가 
         * 공법의 Item ID 에 영향을 주는 속성 정보들도 수정 불가
         */
        try {
        	
        	IDialog dialog = UIManager.getCurrentDialog();
        	String dilaogId = dialog.getId();
        	
        	if( dilaogId.contains("Modify")) {
        		 MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        		 TCComponentBOMLine selectedBOPLine = mfgApp.getSelectedBOMLines()[0];
        		 TCComponent refComp = selectedBOPLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
                 if (refComp == null)
                     return;
                 
                 TCComponentItemRevision mecoRevision = (TCComponentItemRevision) refComp;
                 String mecoNo = mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                 if( mecoNo != null && !mecoNo.equals("") ) {
                	 txtMeco.setText(mecoNo);
                	 btnSearch.setEnabled(false);
                 }
        		 
        	}
        	
        } catch(Exception e) {
        	e.printStackTrace();
        }
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        btnSearch.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                IDialog dialog;
                try {
                    dialog = UIManager.getDialog(getShell(), "symc.me.bop.SearchMECODlg");
                    dialog.open();
                    IDataSet datSet = dialog.getSelectDataSetAll();
                    if (datSet == null)
                        return;
                    mecoDataMap = datSet.getDataMap("mecoSearch");
                    if (mecoDataMap != null && mecoDataMap.containsKey("mecoNo")) {
                        txtMeco.setText(mecoDataMap.getStringValue("mecoNo"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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

        return mecoDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        return mecoDataMap;
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
        return new InitOperation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_FAILED)
            return;
        if (dataset == null)
            return;
        IDataMap dataMap = dataset.getDataMap(this.getId());
        loadInitData(dataMap);
    }

    private void loadInitData(IDataMap dataMap) {
        if (dataMap == null)
            return;
        txtMeco.setText(dataMap.getStringValue("mecoNo"));
        mecoDataMap = dataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {

    }

    // // @Override
    // public void setParameters(Map<String, Object> paramters) {
    // super.setParameters(paramters);
    // }

    /**
     * 초기 Data Load Operation
     * Class Name : InitOperation
     * Class Description :
     * 
     * @date 2013. 12. 3.
     * 
     */
    public class InitOperation extends AbstractSDVInitOperation implements InterfaceAIFOperationListener {

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {
            IDataMap displayDataMap = new RawDataMap();
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            try {

                TCComponentBOMLine selectedBOPLine = mfgApp.getSelectedBOMLines()[0];

                String selectedItemType = selectedBOPLine.getItem().getType();
                // 선택된 것이 Line, 공정이면
                boolean isCopyMecoType = selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                if (!isCopyMecoType)
                    return;
                TCComponent refComp = selectedBOPLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
                if (refComp == null)
                    return;

                TCComponentItemRevision mecoRevision = (TCComponentItemRevision) refComp;

                if (CustomUtil.isReleased(mecoRevision))
                    return;

                displayDataMap.put("mecoNo", mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                displayDataMap.put("mecoRev", mecoRevision, IData.OBJECT_FIELD);

                DataSet viewDataSet = new DataSet();
                viewDataSet.addDataMap(MecoSelectView.this.getId(), displayDataMap);
                setData(viewDataSet);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.InterfaceAIFOperationListener#endOperation()
         */
        @Override
        public void endOperation() {

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.InterfaceAIFOperationListener#startOperation(java.lang.String)
         */
        @Override
        public void startOperation(String arg0) {

        }
    }
//    [CF3537] 	[개선과제]MECO 결재 거부 후 공법 개정 불가 
//    * BOP에서 사용하는 MECO 검색 화면 변경 하면 서 추가됨 
    public void visibleSearchBtn(boolean state){
    	btnSearch.setEnabled(state);
    }
    
    
    /**
     * Alternative BOP 생성시 MECO 뷰에 상태를 비활성으로 바꾼다
     * [CF3537] 	[개선과제]MECO 결재 거부 후 공법 개정 불가 
     * BOP에서 사용하는 MECO 검색 화면 변경 하면 서 추가됨 
     * @method setAlternative
     * @date 2013. 12. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setAlternative(boolean state)
    {
    	if (! state)
    	{
    		txtMeco.setText("");
    		txtMeco.setData(null);

    		txtMeco.setBackground(getShell().getBackground());
    	}
    	else
    		txtMeco.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));

    	txtMeco.setMandatory(state);
    	txtMeco.setEnabled(state);
    	btnSearch.setEnabled(state);
    }
}
