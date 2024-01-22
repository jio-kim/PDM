/**
 *
 */
package com.symc.plm.me.sdv.view.swm;

import java.util.Map;

import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.ui.OperationBeanFactory;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.ProgressBar;

import com.symc.plm.me.common.SDVText;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Class Name : RegisterSWMOperationSearchView
 * Class Description :
 * 
 * @date 2013. 11. 8.
 * 
 */
public class RegisterSWMOperationSearchView extends AbstractSDVViewPane {
    private SDVText textOperationN0;
    private SDVText textOperationName;
    private String vehicleNo;
    private IDataMap dataMap;
    int selectedValue;

    private boolean isShowProgress = false;
    private ProgressBar progressShell;
    private Shell thisShell;

    public RegisterSWMOperationSearchView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {
        thisShell = getShell();

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(4, false));

        Label lblOperationN0 = new Label(composite, SWT.NONE);
        lblOperationN0.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblOperationN0.setText("공법 NO.");

        textOperationN0 = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        textOperationN0.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        final Button all_checkBox = new Button(composite, SWT.CHECK);
        all_checkBox.setText("REV.ALL");
        all_checkBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (all_checkBox.getSelection()) {
                    selectedValue = 1;
                } else {
                    selectedValue = 0;
                }
            }
        });

        Button searchBtn = new Button(composite, SWT.PUSH);
        GridData gd_searchBtn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_searchBtn.widthHint = 76;
        searchBtn.setLayoutData(gd_searchBtn);
        searchBtn.setText("검색");
        searchBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                String operationClassName = "com.symc.plm.me.sdv.operation.swm.RegisterSWMDocOpSearchOperation";

                try {
                    showProgress(true);

                    final AbstractSDVActionOperation operation = (AbstractSDVActionOperation) OperationBeanFactory.getCommandOperator(operationClassName, 1, getId(), null, getSelectDataSetAll());
                    AIFUtility.getDefaultSession().queueOperation(operation);
                    operation.addOperationListener(new InterfaceAIFOperationListener() {

                        @Override
                        public void startOperation(String arg0) {

                        }

                        @Override
                        public void endOperation() {
                            Display.getDefault().syncExec(new Runnable() {
                                @Override
                                public void run() {
                                    IDataSet dataset = operation.getDataSet();
                                    IDialog dialog = UIManager.getCurrentDialog();
                                    AbstractSDVViewPane viewPane = (AbstractSDVViewPane) dialog.getView("registerSWMOperationSearchResultView");
                                    viewPane.setLocalDataMap(dataset.getDataMap("swmItemList"));

                                    showProgress(false);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Label lblOperationName = new Label(composite, SWT.NONE);
        lblOperationName.setText("공법명");

        textOperationName = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        textOperationName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
    }

    @Override
    public IDataMap getLocalDataMap() {
        Map<String, Object> paramMap = getParameters();
        vehicleNo = (String) paramMap.get("vehicle_no");
        String shopCode = (String) paramMap.get("shop_code");

        dataMap = new RawDataMap();
        dataMap.put("selectedValue", selectedValue, IData.OBJECT_FIELD);
        dataMap.put("operation_no", (String) textOperationN0.getText(), IData.STRING_FIELD);
        dataMap.put("operation_name", (String) textOperationName.getText(), IData.STRING_FIELD);
        dataMap.put("vehicle_no", vehicleNo, IData.STRING_FIELD);
        dataMap.put("shop_code", shopCode, IData.STRING_FIELD);

        return dataMap;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_SUCCESS) {

        }
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {

        return new AbstractSDVInitOperation() {

            @Override
            public void executeOperation() throws Exception {

            }

        };
    }

    protected void showProgress(boolean show) {
        if (this.isShowProgress != show) {
            if (show) {
                if (progressShell == null) {
                    try {
                        progressShell = new ProgressBar(thisShell);
                        progressShell.start();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (progressShell != null) {
                progressShell.close();
                progressShell = null;
            }

            isShowProgress = show;
        }
    }

    @Override
    public void uiLoadCompleted() {

    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public Composite getRootContext() {

        return null;
    }

}
