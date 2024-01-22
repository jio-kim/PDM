/**
 * 
 */
package com.symc.plm.me.sdv.view.plant;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyShopView
 * Class Description :
 * 
 * @date 2013. 11. 1.
 * 
 */
public class ApplyAlternativeStationView extends AbstractSDVViewPane {

    @SuppressWarnings("unused")
    private Registry registry;

    private Composite topComposite;

    private TCComponentBOMLine targetBOMLine;

    private Label label;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public ApplyAlternativeStationView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @SuppressWarnings("unused")
    @Override
    protected void initUI(Composite parent) {
        String dialogId = UIManager.getCurrentDialog().getId();

        try {
            registry = Registry.getRegistry(this);

            topComposite = new Composite(parent, SWT.NONE);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.verticalSpacing = 10;
            gridLayout.horizontalSpacing = 20;
            gridLayout.marginLeft = 10;
            gridLayout.marginRight = 10;
            gridLayout.marginHeight = 10;
            topComposite.setLayout(gridLayout);
            
            
            AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getAvailableDialog("symc.me.bop.ApplyAlternativeStationDialog");
            dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));

            label = new Label(topComposite, SWT.NONE);
            label.setText("");
            
            label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        RawDataMap rawDataMap = new RawDataMap();
        if (targetBOMLine != null ) {
            rawDataMap.put("tcComponentBOMLine", targetBOMLine, IData.OBJECT_FIELD);
        }
        return rawDataMap;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        Map<String, Object> paramMap = getParameters();

        if (paramMap != null) {
            targetBOMLine = (TCComponentBOMLine) paramMap.get("tcComponentBOMLine");
        }

        // Dialog Open시 기본 값 셋팅
         setDefaultValue(targetBOMLine);

        return null;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void uiLoadCompleted() {

    }

    /**
     * Dialog에 Default값 입력하는 함수 (현재 선택된 Item의 ID를 Dialog 속성에 반영)
     * 
     * @param targetObjet
     */
    public void setDefaultValue(TCComponentBOMLine targetBOMLine) {
         if (targetBOMLine != null) {
            try {

                String itemID = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                label.setText("Apply Alternative(" + itemID + ") To Production(" + itemID.substring(itemID.indexOf("-") +1) + ") ?");
            } catch (Exception e) {
                e.printStackTrace();
            }
         }
    }

}
