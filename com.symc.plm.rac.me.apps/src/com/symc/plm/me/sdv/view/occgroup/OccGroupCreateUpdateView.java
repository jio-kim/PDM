package com.symc.plm.me.sdv.view.occgroup;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.sdv.operation.occgroup.OccGroupCreateUpdateInitOperation;
import com.teamcenter.rac.kernel.TCComponentBOPLine;

/**
 * [SR150529-025][20150828] shcho, 누적파트 생성/업데이트 방법 추가 개발 - Shop, Line 단위로 일괄적으로 누적파트 생성(업데이트) 할 수 있도록 기능 개선
 * 
 */
public class OccGroupCreateUpdateView extends AbstractSDVViewPane {

    private Label targetOccGroup_label;
    private Text targetOccGroup_text;
    private List<?> targetStationList;
    private TCComponentBOPLine targetShop;
    private Composite composite;

    private IDataMap curDataMap;

    public OccGroupCreateUpdateView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        targetOccGroup_label = new Label(composite, SWT.NONE);
        targetOccGroup_label.setAlignment(SWT.CENTER);
        targetOccGroup_label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        
        targetOccGroup_text = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        targetOccGroup_text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
        
    }


    private IDataMap saveData()
    {
        RawDataMap savedDataMap = new RawDataMap();

        savedDataMap.put("targetStation", targetStationList, IData.LIST_FIELD);
        savedDataMap.put("targetShop", targetShop, IData.OBJECT_FIELD);

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
        return new OccGroupCreateUpdateInitOperation();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initalizeData(int result, IViewPane owner, IDataSet dataset)
    {
        try {
            if (result == SDVInitEvent.INIT_SUCCESS)
            {
                if (dataset != null)
                {
                    targetStationList = dataset.getListValue("OccGroupCreateUpdateView", "targetStation");
                    targetShop = (TCComponentBOPLine) dataset.getData("targetShop");
                    List<String> bopLineList = (List<String>) dataset.getData("bopLineList");

                    if (targetStationList != null && targetStationList.size() > 0) {

                        StringBuffer strTargetList = new StringBuffer();
                        StringBuffer message = new StringBuffer();
                        message.append("Do you want to update the OccGroup ?");
                        message.append(System.lineSeparator());
                        message.append(System.lineSeparator());
                        message.append(" --------------------------------- Modified List ---------------------------------- ");
                        message.append(System.lineSeparator());
                        message.append(System.lineSeparator());

                        if(bopLineList == null || bopLineList.size() == 0) {
                            strTargetList.append("There is no modified BOPlines.");
                            strTargetList.append(System.lineSeparator());
                            strTargetList.append("Or this is the first time to create a Accumulated_Parts.");
                        } else {
                            for (String bopLine : bopLineList) {
                                strTargetList.append(bopLine);
                                strTargetList.append(System.lineSeparator());
                            }
                        }
                        targetOccGroup_label.setText(message.toString());
                        targetOccGroup_text.setText(strTargetList.toString());

                        composite.layout();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void uiLoadCompleted() {

    }

}
