package com.symc.plm.me.sdv.view.report;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.teamcenter.rac.util.Registry;

public class OPMasterListOptionView extends AbstractSDVViewPane {

    private IDataMap localDataMap;

    /**
     * @wbp.parser.constructor
     */
    public OPMasterListOptionView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {
        Registry registry = Registry.getRegistry(this);

        final int[] selectedValue = new int[3];

        localDataMap = new RawDataMap();
        localDataMap.put("selectedValue", selectedValue, IData.OBJECT_FIELD);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));

        final Button workInfo_CheckButton = new Button(composite, SWT.CHECK);
        workInfo_CheckButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(workInfo_CheckButton.getSelection()) {
                    selectedValue[0] = 1;
                } else {
                    selectedValue[0] = 0;
                }
                localDataMap.put("selectedValue", selectedValue, IData.OBJECT_FIELD);
            }
        });
        workInfo_CheckButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        workInfo_CheckButton.setText(registry.getString("WorkInfo.NAME"));

        final Button eItemInfo_CheckButton = new Button(composite, SWT.CHECK);
        eItemInfo_CheckButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(eItemInfo_CheckButton.getSelection()) {
                    selectedValue[1] = 2;
                } else {
                    selectedValue[1] = 0;
                }
                localDataMap.put("selectedValue", selectedValue, IData.OBJECT_FIELD);
            }
        });
        eItemInfo_CheckButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        eItemInfo_CheckButton.setText(registry.getString("EndItemInfo.NAME"));

        final Button subsidiary_CheckButton = new Button(composite, SWT.CHECK);
        subsidiary_CheckButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(subsidiary_CheckButton.getSelection()) {
                    selectedValue[2] = 3;
                } else {
                    selectedValue[2] = 0;
                }
                localDataMap.put("selectedValue", selectedValue, IData.OBJECT_FIELD);
            }
        });
        subsidiary_CheckButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        subsidiary_CheckButton.setText(registry.getString("SubsidiaryInfo.NAME"));
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
