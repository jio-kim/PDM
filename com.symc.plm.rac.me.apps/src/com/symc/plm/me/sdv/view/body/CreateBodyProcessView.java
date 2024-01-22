/**
 * 
 */
package com.symc.plm.me.sdv.view.body;

import java.util.HashMap;
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

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.body.CreateBodyProcessInitOperation;
import com.symc.plm.me.sdv.view.meco.MecoSelectView;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyProcessView
 * Class Description :
 * [CF-3537] [20230131]isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경
 * @date 2013. 11. 28.
 * 
 */
public class CreateBodyProcessView extends AbstractSDVViewPane {
    private SDVText textShop;
    private SDVText textLine;
    private SDVText textStation;
    private SDVText textPlanningVer;
    private SDVText textStationKorName;
    private SDVText textStationEngName;
    private Registry registry;
    private TCComponentBOMLine parentLine;
    private String altPrefix;
    private boolean isAlt;
    private String product_code;
    private String vehicle_code;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public CreateBodyProcessView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {
        try {
            registry = Registry.getRegistry(CreateBodyShopView.class);

            Composite mainView = new Composite(parent, SWT.NONE);
            mainView.setLayout(new GridLayout(1, false));

            Group grpStation = new Group(mainView, SWT.NONE);
            grpStation.setText(registry.getString("CreateStationDialog.Station.Group.Name", "Station Properties"));
            grpStation.setLayout(new GridLayout(8, false));
            grpStation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

            Label lblShop = new Label(grpStation, SWT.NONE);
            lblShop.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblShop.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_SHOP));

            textShop = new SDVText(grpStation, SWT.BORDER | SWT.READ_ONLY);
            textShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textShop.setEditable(false);

            Label lblLine = new Label(grpStation, SWT.NONE);
            lblLine.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblLine.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_LINE));

            textLine = new SDVText(grpStation, SWT.BORDER | SWT.READ_ONLY);
            textLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textLine.setEditable(false);

            Label lblStation = new Label(grpStation, SWT.NONE);
            lblStation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblStation.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_STATION_CODE));

            textStation = new SDVText(grpStation, SWT.BORDER | SWT.SINGLE);
            textStation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textStation.setTextLimit(3);
            textStation.setInputType(SDVText.NUMERIC);
            textStation.setMandatory(true);

            Label lblPlanningVer = new Label(grpStation, SWT.NONE);
            lblPlanningVer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblPlanningVer.setVisible(false);
            lblPlanningVer.setEnabled(false);
            lblPlanningVer.setText("Planning Ver.");

            textPlanningVer = new SDVText(grpStation, SWT.BORDER | SWT.SINGLE);
            textPlanningVer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textPlanningVer.setMandatory(true);
            textPlanningVer.setVisible(false);
            textPlanningVer.setEditable(false);
            textPlanningVer.setText("00");

            Label lblStationKorName = new Label(grpStation, SWT.NONE);
            lblStationKorName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblStationKorName.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.ITEM_OBJECT_NAME));

            textStationKorName = new SDVText(grpStation, SWT.BORDER | SWT.SINGLE);
            textStationKorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
            textStationKorName.setMandatory(true);
            textStationKorName.setTextLimit(80);

            Label lblStationEngName = new Label(grpStation, SWT.NONE);
            lblStationEngName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblStationEngName.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV, SDVPropertyConstant.STATION_ENG_NAME));

            textStationEngName = new SDVText(grpStation, SWT.BORDER | SWT.SINGLE);
            textStationEngName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
            textStationEngName.setMandatory(true);
            textStationEngName.setTextLimit(80);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param paramters
     *            the parameters to set
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        if (parameters != null) {
            if (parameters.containsKey(SDVPropertyConstant.LINE_REV_CODE)) {
                Object lineObject = parameters.get(SDVPropertyConstant.LINE_REV_CODE);
                if (lineObject instanceof TCComponentBOMLine)
                    parentLine = (TCComponentBOMLine) lineObject;
            }

            if (parameters.containsKey(SDVPropertyConstant.STATION_SHOP))
                textShop.setText(parameters.get(SDVPropertyConstant.STATION_SHOP).toString());

            if (parameters.containsKey(SDVPropertyConstant.STATION_LINE))
                textLine.setText(parameters.get(SDVPropertyConstant.STATION_LINE).toString());

            if (parameters.containsKey(SDVPropertyConstant.STATION_STATION_CODE))
                textStation.setText(parameters.get(SDVPropertyConstant.STATION_STATION_CODE).toString());
//            if (parameters.containsKey("PlanningVer"))
//                textPlanningVer.setText(parameters.get("PlanningVer").toString());
            if (parameters.containsKey(SDVPropertyConstant.STATION_BOP_VERSION))
            	textPlanningVer.setText(parameters.get(SDVPropertyConstant.STATION_BOP_VERSION).toString());

            if (parameters.containsKey(SDVPropertyConstant.ITEM_OBJECT_NAME))
                textStationKorName.setText(parameters.get(SDVPropertyConstant.ITEM_OBJECT_NAME).toString());

            if (parameters.containsKey(SDVPropertyConstant.STATION_ENG_NAME))
                textStationEngName.setText(parameters.get(SDVPropertyConstant.STATION_ENG_NAME).toString());
        }
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        RawDataMap mecoData = new RawDataMap();

        mecoData.put(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, parentLine, IData.OBJECT_FIELD);
        mecoData.put(SDVPropertyConstant.STATION_SHOP, textShop.getText());
        mecoData.put(SDVPropertyConstant.STATION_LINE, textLine.getText());
        mecoData.put(SDVPropertyConstant.STATION_STATION_CODE, textStation.getText());
//        mecoData.put("PlanningVer", textPlanningVer.getText());
        mecoData.put(SDVPropertyConstant.STATION_BOP_VERSION, textPlanningVer.getText());
        mecoData.put(SDVPropertyConstant.ITEM_OBJECT_NAME, textStationKorName.getText());
        mecoData.put(SDVPropertyConstant.STATION_ENG_NAME, textStationEngName.getText());
        mecoData.put(SDVPropertyConstant.STATION_IS_ALTBOP, isAlt, IData.BOOLEAN_FIELD);
        mecoData.put(SDVPropertyConstant.STATION_ALT_PREFIX, altPrefix);
        mecoData.put(SDVPropertyConstant.STATION_VEHICLE_CODE, vehicle_code);
        mecoData.put(SDVPropertyConstant.STATION_PRODUCT_CODE, product_code);

        return mecoData;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new CreateBodyProcessInitOperation();
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null) {
                AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();

                Object parentObject = dataset.getValue(SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
                if (parentObject != null)
                	parentLine = (TCComponentBOPLine) parentObject;
                try {
                	if (parentLine != null)
                	{
                		textShop.setText(parentLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_SHOP_CODE));
                		textShop.setEnabled(false);
                		textLine.setText(parentLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
                		textLine.setEnabled(false);
                		isAlt = parentLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.LINE_REV_IS_ALTBOP);
                		altPrefix = parentLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_ALT_PREFIX);
                		vehicle_code = parentLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_VEHICLE_CODE);
                		product_code = parentLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_PRODUCT_CODE);
                	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
                	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 */
//                		IViewPane mecoView = dialog.getView("mecoView");
                		IViewPane mecoView = dialog.getView(SDVPropertyConstant.MECO_SELECT);
                		if (isAlt) {
                			dialog.setAddtionalTitle("Alternative BOP");
                			dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));
//                			((SelectedMECOView) mecoView).setAlternative(false);
                			((MecoSelectView) mecoView).setAlternative(false);
                		}
                		else
                		{
                			TCComponent mecoItemRevision = parentLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.LINE_REV_MECO_NO);
                			if (mecoItemRevision != null)
                			{
                				if (! CustomUtil.isReleased(mecoItemRevision))
                				{
                					HashMap<String, Object> dataMap = new HashMap<String, Object>();
                					
                					dataMap.put(SDVTypeConstant.MECO_ITEM_REV, mecoItemRevision);
                					
                					mecoView.setParameters(dataMap);
                				}
                			}
                		}
//                        ((SelectedMECOView) mecoView).visibleSearchBtn(false);
                        ((MecoSelectView) mecoView).visibleSearchBtn(false);
                	}
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void uiLoadCompleted() {

    }

    @Override
    public Composite getRootContext() {
        return null;
    }

}
