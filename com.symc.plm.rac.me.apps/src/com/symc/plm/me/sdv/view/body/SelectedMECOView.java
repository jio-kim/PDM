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
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.dialog.common.SearchTypedItemDialog;
import com.symc.plm.me.sdv.view.common.SearchTypedItemView;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SelectedMECOView
 * Class Description :
 * @date 2013. 11. 1.
 *
 */
public class SelectedMECOView extends AbstractSDVViewPane {
    private SDVText textMECONo;
    private Registry registry;
    private Button btnSearchMECO;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public SelectedMECOView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        try
        {
            registry = Registry.getRegistry(CreateBodyShopView.class);

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(1, false));

            Group grpMECO = new Group(composite, SWT.NONE);
            grpMECO.setText(registry.getString("MECOView.MECO.Group.Name", "MECO"));
            grpMECO.setLayout(new GridLayout(5, false));
            grpMECO.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

            Label lblMECONo = new Label(grpMECO, SWT.NONE);
            lblMECONo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblMECONo.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_MECO_NO));

            textMECONo = new SDVText(grpMECO, SWT.BORDER | SWT.READ_ONLY);
            textMECONo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            textMECONo.setMandatory(true);

            btnSearchMECO = new Button(grpMECO, SWT.NONE);
            btnSearchMECO.setText(registry.getString("MECOView.MECO.Search.Button", "Search MECO"));
            btnSearchMECO.addSelectionListener(new SelectionListener() {
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
            new Label(grpMECO, SWT.NONE);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Alternative BOP 생성시 MECO 뷰에 상태를 비활성으로 바꾼다
     *
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
    		textMECONo.setText("");
    		textMECONo.setData(null);

    		textMECONo.setBackground(getShell().getBackground());
    	}
    	else
    		textMECONo.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));

    	textMECONo.setMandatory(state);
        textMECONo.setEnabled(state);
        btnSearchMECO.setEnabled(state);
    }

    /**
     * 용접공법생성시 MECOBtn 을 컨트롤 하기위해 만든 Method
     *
     * @method visibleSearchBtn
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void visibleSearchBtn(boolean state)
    {
        btnSearchMECO.setEnabled(state);
    }

	@Override
	public void setLocalDataMap(IDataMap dataMap) {
	}

    /**
     * @param paramters
     *            the parameters to set
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
    	if (parameters != null && parameters.containsKey(SDVTypeConstant.MECO_ITEM_REV))
    	{
    		textMECONo.setText(parameters.get(SDVTypeConstant.MECO_ITEM_REV).toString());
    		textMECONo.setData(parameters.get(SDVTypeConstant.MECO_ITEM_REV));
    	}
    	else
    	{
    	    textMECONo.setText("");
    	    textMECONo.setData("");
    	}
    }

	@Override
	public IDataMap getLocalDataMap() {
		return getLocalSelectDataMap();
	}

	@Override
	public IDataMap getLocalSelectDataMap() {
		RawDataMap retMap = new RawDataMap();
		retMap.put(SDVPropertyConstant.SHOP_REV_MECO_NO, textMECONo.getData(), IData.OBJECT_FIELD);

		return retMap;
	}

	@Override
	public Composite getRootContext() {
		return null;
	}

	@Override
	public AbstractSDVInitOperation getInitOperation() {
		return null;
	}

	@Override
	public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
	}

	@Override
	public void uiLoadCompleted() {
	}

}
