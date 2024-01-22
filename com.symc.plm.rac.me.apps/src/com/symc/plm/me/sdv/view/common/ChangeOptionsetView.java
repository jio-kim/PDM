package com.symc.plm.me.sdv.view.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

public class ChangeOptionsetView extends AbstractSDVViewPane {

    private Label product_label;
    private TCComponentBOMLine targetProduct;
    private IDataMap curDataMap;

    public ChangeOptionsetView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        product_label = new Label(composite, SWT.NONE);
        product_label.setAlignment(SWT.CENTER);
        product_label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
    }


    private IDataMap saveData()
    {
        RawDataMap savedDataMap = new RawDataMap();

        savedDataMap.put("ChangeOptionsetView", targetProduct, IData.OBJECT_FIELD);

        return savedDataMap;
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        return this.curDataMap;
    }


    @Override
    public IDataMap getLocalSelectDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new AbstractSDVInitOperation() {
            @Override
            public void executeOperation() throws Exception {
                try {
                    TCComponentBOMLine mProduct = getTarget();

                    if (mProduct != null) {
                        targetProduct = mProduct.window().getTopBOMLine();
                        RawDataMap targetDataMap = new RawDataMap();
                        targetDataMap.put("TargetProduct", targetProduct, IData.OBJECT_FIELD);
                        DataSet targetDataset = new DataSet();
                        targetDataset.addDataMap(getId(), targetDataMap);
                        setData(targetDataset);

                    }
                } catch (Exception ex) {
                    throw ex;
                }
            }
        };
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null) {
                TCComponentBOMLine productBomline = (TCComponentBOMLine)dataset.getData("TargetProduct");
                product_label.setText("Do you want to change option " + productBomline.toString() + "M-Product");
            }
        }
    }

    @Override
    public void uiLoadCompleted() {

    }

    public TCComponentBOMLine getTarget() {
        TCComponentBOMLine target = null;
        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();

        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

        if(aaifcomponentcontext != null && aaifcomponentcontext.length == 1 && (aaifcomponentcontext[0].getComponent() instanceof TCComponentBOMLine))
        {
            target = (TCComponentBOMLine)aaifcomponentcontext[0].getComponent();
            return target;
        }
        return null;
    }
}
