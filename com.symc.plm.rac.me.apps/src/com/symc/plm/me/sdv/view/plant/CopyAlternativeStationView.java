/**
 * 
 */
package com.symc.plm.me.sdv.view.plant;

import java.util.Map;

import org.eclipse.jface.fieldassist.ControlDecoration;
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
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.kgm.common.utils.SYMDisplayUtil;
import com.symc.plm.me.common.SDVText;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyShopView
 * Class Description :
 * 
 * @date 2013. 11. 1.
 * 
 */
public class CopyAlternativeStationView extends AbstractSDVViewPane {

    private Registry registry;

    private Composite topComposite;

    private Button btnCheckAlt;

    private SDVText textAltPrefix;

    private ControlDecoration decoration;

    private Object targetBOMLine;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public CopyAlternativeStationView(Composite parent, int style, String id) {
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

            drawAlt(topComposite);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ALT Plant �ʵ� ���� �Լ�
     */
    private void drawAlt(Composite composite) {
        AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getAvailableDialog("symc.me.bop.CopyAlternativeStationDialog");
        dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));

        btnCheckAlt = new Button(composite, SWT.CHECK);
        // btnCheckAlt.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_IS_ALTBOP));
        btnCheckAlt.setText(registry.getString("Plant.IsAltPlant.Name", "Is Alt Plant"));
        btnCheckAlt.setSelection(true);
        btnCheckAlt.setEnabled(false);

        textAltPrefix = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        textAltPrefix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textAltPrefix.setEnabled(true);
        textAltPrefix.setTextLimit(4);
        textAltPrefix.setText("ALT");
        textAltPrefix.setInputType(SDVText.ENGUPPERNUM);

        decoration = SYMDisplayUtil.setRequiredFieldSymbol(textAltPrefix);
        decoration.show();
        textAltPrefix.redraw();

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
        if (targetBOMLine != null && textAltPrefix.getText().length() > 0) {
            rawDataMap.put("tcComponentBOMLine", targetBOMLine, IData.OBJECT_FIELD);
            rawDataMap.put(registry.getString("Plant.IsAltPlant.Name", "Is Alt Plant"), true, IData.BOOLEAN_FIELD);
            rawDataMap.put(registry.getString("Plant.AltPrefix.NAME", "Alt Prefix"), textAltPrefix.getText(), IData.STRING_FIELD);
        }
        return rawDataMap;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        Map<String, Object> paramMap = getParameters();

        if (paramMap != null) {
            // if (paramMap.containsKey("targetItemRevision")) {
            targetBOMLine = paramMap.get("tcComponentBOMLine");
            // }
        }

        // Dialog Open�� �⺻ �� ����
        // setDefaultValue(targetObjet);

        return null;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void uiLoadCompleted() {

    }

    /**
     * Dialog�� Default�� �Է��ϴ� �Լ� (���� ���õ� Item�� ID�� Dialog �Ӽ��� �ݿ�)
     * 
     * @param targetObjet
     */
    public void setDefaultValue(Object targetObjet) {
        // if (targetObjet != null) {
        // TCComponentItemRevision targetRevision = (TCComponentItemRevision) targetObjet;
        // try {
        // Boolean isAltPlant = targetRevision.getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
        // String altPrefix = targetRevision.getProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX);
        // String[] idCrumb = targetRevision.getProperty("item_id").split("-");
        //
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
    }

}
