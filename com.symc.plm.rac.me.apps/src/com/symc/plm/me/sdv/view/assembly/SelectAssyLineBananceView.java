/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.util.Registry;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Class Name : SelectAssyLineBananceType
 * Class Description : 조립 공정편성표. 타입 선택
 * 
 * @date 2013. 12. 16.
 * 
 */
public class SelectAssyLineBananceView extends AbstractSDVViewPane {

    private Button btnIsMaxTime = null;
    private Button btnIsRepVehicle = null;
    private boolean isMaxWorkTime = true;
    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public SelectAssyLineBananceView(Composite parent, int style, String id) {
        super(parent, style, id);

    }

    @Override
    protected void initUI(Composite parent) {
        Registry registry = Registry.getRegistry(this);
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gl_composite = new GridLayout(2, false);
        gl_composite.marginLeft = 30;
        gl_composite.marginTop = 30;
        gl_composite.marginHeight = 0;
        gl_composite.marginWidth = 0;
        composite.setLayout(gl_composite);

        btnIsMaxTime= new Button(composite, SWT.FLAT | SWT.RADIO);
        btnIsMaxTime.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isMaxWorkTime = true;
            }
        });
        GridData gd_btnIsMaxTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnIsMaxTime.widthHint = 160;
        btnIsMaxTime.setLayoutData(gd_btnIsMaxTime);
        btnIsMaxTime.setSelection(true);
        btnIsMaxTime.setText(registry.getString("BasedOnMaxWorkTime.NAME"));

        btnIsRepVehicle = new Button(composite, SWT.RADIO);
        btnIsRepVehicle.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isMaxWorkTime = false;
            }
        });
        btnIsRepVehicle.setText(registry.getString("BasedOnRepVehicle.NAME"));
        composite.setTabList(new Control[] { btnIsMaxTime, btnIsRepVehicle });

    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
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
        RawDataMap dataMap = new RawDataMap();
        dataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, isMaxWorkTime, IData.BOOLEAN_FIELD);
        return dataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {        
        return getLocalDataMap();
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
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
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
