/**
 * 
 */
package com.symc.plm.me.sdv.view.body;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.dialog.common.SearchTypedItemDialog;
import com.symc.plm.me.sdv.operation.body.ApplyAlternativeToProductionBOPInitOperation;
import com.symc.plm.me.sdv.view.common.SearchTypedItemView;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class ApplyAlternativeToProductionBOPView extends AbstractSDVViewPane {
	private Registry registry = Registry.getRegistry(ApplyAlternativeToProductionBOPView.class);

	private SDVText textAlternativeBOP;
    private SDVText textMECONo;
    private SDVText textMProduction;
    private Button btnMProduction;

	/**
	 * @param parent
	 * @param style
	 * @param id
	 * @wbp.parser.constructor
	 */
	public ApplyAlternativeToProductionBOPView(Composite parent, int style,
			String id) {
		super(parent, style, id);
	}

	/**
	 * @param parent
	 * @param style
	 * @param id
	 * @param configId
	 */
	public ApplyAlternativeToProductionBOPView(Composite parent, int style,
			String id, int configId) {
		super(parent, style, id, configId);
	}

	/**
	 * @param parent
	 * @param style
	 * @param id
	 * @param configId
	 * @param order
	 */
	public ApplyAlternativeToProductionBOPView(Composite parent, int style,
			String id, int configId, String order) {
		super(parent, style, id, configId, order);
	}

    /* (non-Javadoc)
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
	@Override
	protected void initUI(Composite parent)	{
		registry = Registry.getRegistry(ApplyAlternativeToProductionBOPView.class);

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));
        
        Group grpAlternativeBop = new Group(mainComposite, SWT.NONE);
        grpAlternativeBop.setText(registry.getString("AltToProductView.AltGroup.NAME", "Alternative BOP"));
        grpAlternativeBop.setLayout(new GridLayout(2, false));
        grpAlternativeBop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        Label lblAlternativeBOP = new Label(grpAlternativeBop, SWT.NONE);
        lblAlternativeBOP.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblAlternativeBOP.setText(registry.getString("AltToProductView.AltBOP.LABEL", "Alternative BOP :"));
        
        textAlternativeBOP = new SDVText(grpAlternativeBop, SWT.BORDER | SWT.READ_ONLY);
        textAlternativeBOP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textAlternativeBOP.setMandatory(true);
        
        Group grpProductionBOP = new Group(mainComposite, SWT.NONE);
//        grpProductionBOP.setLayoutData(BorderLayout.CENTER);
        grpProductionBOP.setText(registry.getString("AltToProductView.ProductGroup.NAME", "Apply Target BOP"));
        grpProductionBOP.setLayout(new GridLayout(3, false));
        grpProductionBOP.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        Label lblMECONo = new Label(grpProductionBOP, SWT.NONE);
        lblMECONo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblMECONo.setText(registry.getString("AltToProductView.MECO.LABEL", "MECO No."));
        
        textMECONo = new SDVText(grpProductionBOP, SWT.BORDER | SWT.READ_ONLY);
        textMECONo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textMECONo.setMandatory(true);
        
        Button btnMECOButton = new Button(grpProductionBOP, SWT.NONE);
        btnMECOButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        btnMECOButton.setText(registry.getString("AltToProductView.MECO.BUTTON", "Search MECO"));
        btnMECOButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                Display.getDefault().syncExec(new Runnable() {
                	SearchTypedItemDialog searchItemDialog = null;
                	IDataSet retData = null;

                    public void run() {
                        try {
                        	Map<String, Object> setSearchType = new HashMap<String, Object>();

                        	setSearchType.put(SearchTypedItemView.SEARCH_ITEM_TYPE_KEY, SDVTypeConstant.MECO_ITEM);
                        	setSearchType.put(SearchTypedItemView.SEARCH_RELEASE_TYPE_KEY, SearchTypedItemView.UnReleaseItem);

                        	searchItemDialog = (SearchTypedItemDialog) UIManager.getDialog(getShell(), "symc.me.bop.SearchTypedItemDialog");

                        	searchItemDialog.setParameters(setSearchType);
                        	searchItemDialog.open();
                        	retData = searchItemDialog.getSelectDataSetAll();
                        	if (retData != null)
                        	{
                            	Object retObj = retData.getValue(SearchTypedItemView.ReturnSelectedKey);
                            	if (retObj != null)
                            	{
	                            	textMECONo.setData(retObj);
	                            	textMECONo.setText(retObj.toString());
                            	}
                        	}
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                });

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });


        Label lblProductionBOP = new Label(grpProductionBOP, SWT.NONE);
        lblProductionBOP.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblProductionBOP.setText(registry.getString("AltToProductView.TargetBOP.LABEL", "Production BOP"));
        
        textMProduction = new SDVText(grpProductionBOP, SWT.BORDER | SWT.READ_ONLY);
        textMProduction.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textMProduction.setMandatory(true);
        
        btnMProduction = new Button(grpProductionBOP, SWT.NONE);
        btnMProduction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        btnMProduction.setText(registry.getString("AltToProductView.TargetBOP.BUTTON", "Search M-Product"));
        btnMProduction.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                Display.getDefault().syncExec(new Runnable() {
                	SearchTypedItemDialog searchItemDialog = null;
                	IDataSet retData = null;

                    public void run() {
                        try {
                        	Map<String, Object> setSearchType = new HashMap<String, Object>();

                        	setSearchType.put(SearchTypedItemView.SEARCH_ITEM_TYPE_KEY, SDVTypeConstant.EBOM_MPRODUCT);

                        	searchItemDialog = (SearchTypedItemDialog) UIManager.getDialog(getShell(), "symc.me.bop.SearchTypedItemDialog");

                        	searchItemDialog.setParameters(setSearchType);
                        	searchItemDialog.open();
                        	retData = searchItemDialog.getSelectDataSetAll();
                        	if (retData != null)
                        	{
                            	Object retObj = retData.getValue(SearchTypedItemView.ReturnSelectedKey);
                            	if (retObj != null)
                            	{
	                            	textMProduction.setData(retObj);
	                            	textMProduction.setText(retObj.toString());
                            	}
                        	}
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                });

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
	 */
	@Override
	public void setLocalDataMap(IDataMap dataMap) {

	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
	 */
	@Override
	public IDataMap getLocalDataMap() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
	 */
	@Override
	public IDataMap getLocalSelectDataMap() {
		RawDataMap targetData = new RawDataMap();

		targetData.put(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, textAlternativeBOP.getData(), IData.OBJECT_FIELD);
		targetData.put(SDVTypeConstant.EBOM_MPRODUCT, textMProduction.getData(), IData.OBJECT_FIELD);
		targetData.put(SDVTypeConstant.MECO_ITEM, textMECONo.getData(), IData.OBJECT_FIELD);

		return targetData;
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
	 */
	@Override
	public AbstractSDVInitOperation getInitOperation() {
		return new ApplyAlternativeToProductionBOPInitOperation();
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
	 */
	@Override
	public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
		// 오퍼의 결과를 화면에 설정하는 함수
		if (result == SDVInitEvent.INIT_SUCCESS)
		{
			if (dataset != null)
			{
				TCComponentBOPLine targetObject = (TCComponentBOPLine) dataset.getValue("ApplyAltToTargetItem");

				if (targetObject == null)
					return;

				try
				{
					textAlternativeBOP.setText(targetObject.toString());
					textAlternativeBOP.setData(targetObject);

					if (targetObject.getItem().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
					{
						// 라인을 선택했기 때문에 라인에 대한 대상 BOPShop이 존재하는지 체크하고 MProduct을 설정하도록 한다.
			    		TCComponentItem targetShopItem = ((TCComponentBOPLine) targetObject).parent().getItem();
			    		String targetItemID = targetShopItem.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			    		String altPrefix = targetShopItem.getLatestItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
			    		targetItemID = targetItemID.substring(altPrefix.length() + 1);
			    		TCComponentItem productionBOPShop = SYMTcUtil.findItem(CustomUtil.getTCSession(), targetItemID);
			    		String productCode = productionBOPShop.getLatestItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
			    		TCComponentItem productItem = SYMTcUtil.findItem(CustomUtil.getTCSession(), "M".concat(productCode.substring(1)));

			    		textMProduction.setText(productItem.getLatestItemRevision().toString());
			    		textMProduction.setData(productItem.getLatestItemRevision());
			    		textMProduction.setEnabled(false);
			    		btnMProduction.setEnabled(false);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
	 */
	@Override
	public void uiLoadCompleted() {
	}

}
