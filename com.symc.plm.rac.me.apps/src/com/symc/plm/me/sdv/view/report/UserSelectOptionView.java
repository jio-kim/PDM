package com.symc.plm.me.sdv.view.report;

import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class UserSelectOptionView extends AbstractSDVViewPane {

    /**
     * @wbp.parser.constructor
     */
    public UserSelectOptionView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Label OpenOrSave_label = new Label(composite, SWT.NONE);
        OpenOrSave_label.setAlignment(SWT.CENTER);
        OpenOrSave_label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
        OpenOrSave_label.setText("Are you want to open or save the report?");
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
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
