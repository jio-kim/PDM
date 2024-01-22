/**
 * 
 */
package com.symc.plm.me.sdv.view.body;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.body.CreateBodyLineInitOperation;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : UpdateWeldCondSheetView
 * Class Description :
 * [NON-SR][20160217] taeku.jeong, 용접조건표가 개정만 되는 경우 개정 이력이 기록되지 않는 경우가 있어 이를 일괄 Update 하는 기능을위한 View 생성
 * 
 * @date 2016. 02. 17.
 * 
 */
public class UpdateWeldCondSheetView extends AbstractSDVViewPane {
    private SDVText txtShop;
    private SDVLOVComboBox lovLine;
    
    public String shopCode;
    public String vehicle_code;
    public String product_code;
    private Registry registry;
    private TCComponentBOMLine parentShopLine;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public UpdateWeldCondSheetView(Composite parent, int style, String id) {
        super(parent, style, id);
        ((GridData) getRootContext().getLayoutData()).heightHint = 50;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void initUI(Composite parent) {
        super.initUI(parent);

        registry = Registry.getRegistry(UpdateWeldCondSheetView.class);

        try {
            Group group = new Group(parent, SWT.NONE);
            group.setText(registry.getString("CreateShopDialog.Line.Group.Name", "Line Properties"));
            GridLayout gl_group = new GridLayout(2, false);
            gl_group.horizontalSpacing = 10;
            gl_group.marginLeft = 5;
            gl_group.marginHeight = 20;
            gl_group.verticalSpacing = 20;
            group.setLayout(gl_group);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        if (parameters != null) {
            if (parameters.containsKey(SDVPropertyConstant.SHOP_REV_SHOP_CODE)) {
                Object shopObject = parameters.get(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
                if (shopObject instanceof TCComponentBOMLine)
                    parentShopLine = (TCComponentBOMLine) shopObject;
            }

        }
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
        RawDataMap mecoData = new RawDataMap();

        mecoData.put(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, parentShopLine, IData.OBJECT_FIELD);
        mecoData.put(SDVPropertyConstant.LINE_REV_SHOP_CODE, shopCode);
        mecoData.put(SDVPropertyConstant.LINE_REV_VEHICLE_CODE, vehicle_code);
        mecoData.put(SDVPropertyConstant.LINE_REV_PRODUCT_CODE, product_code);

        return mecoData;
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
        return new CreateBodyLineInitOperation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        // 오퍼의 결과를 화면에 설정하는 함수
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null) {
            	
                AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
                parentShopLine = (TCComponentBOMLine) dataset.getValue(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
                IViewPane mecoView = dialog.getView("mecoView");
                try {
                	//  Shop Code Like "B1"
                	shopCode = parentShopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
                    vehicle_code = parentShopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);
                    product_code = parentShopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
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
