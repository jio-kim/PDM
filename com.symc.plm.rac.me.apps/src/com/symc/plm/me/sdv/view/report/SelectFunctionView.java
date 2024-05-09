package com.symc.plm.me.sdv.view.report;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.kgm.common.swtsearch.SearchItemDialog;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;

public class SelectFunctionView extends AbstractSDVViewPane {
	
	private Text txtFunction;
	private IDataMap localDataMap;

    public SelectFunctionView(Composite parent, int style, String id) {
        super(parent, style, id);
        
        localDataMap = new RawDataMap();
    }

    @Override
    protected void initUI(Composite parent) {
    	Composite composite = new Composite(parent, SWT.NONE);
    	composite.setLayout(new GridLayout(3, false));
		
		Label lbFunction = new Label(composite, SWT.NONE);
		lbFunction.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lbFunction.setText("Function");
		
		GridData gd_txtFunction = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtFunction.widthHint = 250;
		txtFunction = new Text(composite, SWT.BORDER);
		txtFunction.setLayoutData(gd_txtFunction);
		txtFunction.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
		txtFunction.setEditable(false);
		
		Button btnFunctionSearch = new Button(composite, SWT.NONE);
		btnFunctionSearch.setText("Search");
		btnFunctionSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnFunctionSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	SearchItemDialog itemDialog = new SearchItemDialog(getShell(), SWT.MULTI, "S7_Function");
    			TCComponentItem[] selectedItems = (TCComponentItem[]) itemDialog.open();
    			
    			try {
	    			if(selectedItems != null) {
	    				String item_id = "";
						for (int i = 0; i < selectedItems.length; i++) {
								item_id += selectedItems[i].getProperty("item_id");
							if (i < selectedItems.length - 1) {
								item_id += ", ";
							}
						}
						
						txtFunction.setText(item_id);
	    				localDataMap.put("selectedFunctions", selectedItems, IData.OBJECT_FIELD);
	    			}
    			} catch (TCException e1) {
    				e1.printStackTrace();
    			}
            }
        });
		
//        Label OpenOrSave_label = new Label(composite, SWT.NONE);
//        OpenOrSave_label.setAlignment(SWT.CENTER);
//        OpenOrSave_label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
//        OpenOrSave_label.setText("Are you want to open or save the report?");
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    	this.localDataMap = dataMap;
    }

    @Override
    public IDataMap getLocalDataMap() {
        return this.localDataMap;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return this.localDataMap;
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
