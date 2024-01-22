/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.SimpleSDVDialog;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.sdv.operation.resource.SelectResourceInitOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;
import com.teamcenter.soa.client.model.LovValue;

/**
 * Class Name : SelectResource
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class SelectResourceViewPane extends AbstractSDVViewPane {
    private String viewId;
    private SWTComboBox shopCombo;
    private SWTComboBox resourceCombo;
    private SWTComboBox categoryCombo;
    private Registry registry;
    
    private static final String SHOP_ASSY_NAME="Assy";
    private static final String SHOP_BODY_NAME="Body";
    private static final String SHOP_PAINT_NAME="Paint";

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public SelectResourceViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
        this.viewId = id;
        registry = Registry.getRegistry(this);
    }

    @Override
    protected void initUI(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FormLayout());

        Label lblShop = new Label(composite, SWT.NONE);
        FormData fd_lblShop = new FormData();
        fd_lblShop.top = new FormAttachment(0, 20);
        fd_lblShop.left = new FormAttachment(0, 20);
        fd_lblShop.width = 50;
        lblShop.setLayoutData(fd_lblShop);
        lblShop.setText("Shop");

        Label lblResource = new Label(composite, SWT.NONE);
        FormData fd_lblResource = new FormData();
        fd_lblResource.top = new FormAttachment(lblShop, 20, SWT.BOTTOM);
        fd_lblResource.left = new FormAttachment(lblShop, 0, SWT.LEFT);
        fd_lblResource.width = 50;
        lblResource.setLayoutData(fd_lblResource);
        lblResource.setText("Resouce");
        
        Label lblCategory = new Label(composite, SWT.NONE);
        FormData fb_lblCategory = new FormData();
        fb_lblCategory.top = new FormAttachment(lblResource, 20, SWT.BOTTOM);
        fb_lblCategory.left = new FormAttachment(lblResource, 0, SWT.LEFT);
        fb_lblCategory.width = 50;
        lblCategory.setLayoutData(fb_lblCategory);
        lblCategory.setText("Category");

        shopCombo = new SWTComboBox(composite, SWT.BORDER | SWT.READ_ONLY);
        FormData fd_shopCombo = new FormData();
        fd_shopCombo.top = new FormAttachment(lblShop, 0, SWT.CENTER);
        fd_shopCombo.left = new FormAttachment(lblShop, 20, SWT.RIGHT);
        fd_shopCombo.right = new FormAttachment(100, -20);
        shopCombo.setLayoutData(fd_shopCombo);
        shopCombo.setEnabled(false);

        resourceCombo = new SWTComboBox(composite, SWT.BORDER | SWT.READ_ONLY);
        FormData fd_resourceCombo = new FormData();
        fd_resourceCombo.top = new FormAttachment(lblResource, 0, SWT.CENTER);
        fd_resourceCombo.left = new FormAttachment(lblResource, 20, SWT.RIGHT);
        fd_resourceCombo.right = new FormAttachment(100, -20);
        resourceCombo.setLayoutData(fd_resourceCombo);
        resourceCombo.setEnabled(false);

        categoryCombo = new SWTComboBox(composite, SWT.BORDER | SWT.READ_ONLY);
        FormData fd_categoryCombo = new FormData();
        fd_categoryCombo.top = new FormAttachment(lblCategory, 0, SWT.CENTER);
        fd_categoryCombo.left = new FormAttachment(lblCategory, 20, SWT.RIGHT);
        fd_categoryCombo.right = new FormAttachment(100, -20);
        categoryCombo.setLayoutData(fd_categoryCombo);
        categoryCombo.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        
        Button dialogOpenButton = new Button(composite, SWT.PUSH);
        FormData fd_dialogOpenButton = new FormData();
        fd_dialogOpenButton.top = new FormAttachment(categoryCombo, 10, SWT.BOTTOM);
        fd_dialogOpenButton.left = new FormAttachment(lblCategory, 20, SWT.RIGHT);
        fd_dialogOpenButton.right = new FormAttachment(100, -20);
        dialogOpenButton.setLayoutData(fd_dialogOpenButton);
        dialogOpenButton.setText("¥Ÿ¿Ω");
        
        dialogOpenButton.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getDefault().syncExec(new Runnable() {
                    SimpleSDVDialog createDialog = null;
                    RawDataMap targetDataMap = null;
//                    IDataSet retData = null;
                    
                    public void run() {
                        try {
                            targetDataMap = (RawDataMap) getLocalSelectDataMap();
                            
                            String dialogIdCrumb = targetDataMap.getStringValue("dialogIdCrumb");
                            String resourceType = targetDataMap.getStringValue("resourceType");
                            if(resourceType.equals(registry.getString("Resource.Type.Equip"))) {
                                resourceType = "Equipment";
                            }
                            if(resourceType.equals(registry.getString("Resource.Type.Tool"))) {
                                resourceType = "Tool";
                            }
                            
                            Map<String, Object> paramKeyMap = new HashMap<String, Object>();
                            paramKeyMap.put("paramKey", targetDataMap);
                            paramKeyMap.put("parentDialog", (AbstractSDVSWTDialog) UIManager.getCurrentDialog());
                            
                            createDialog = (SimpleSDVDialog) UIManager.getDialog(AIFUtility.getActiveDesktop().getShell(), "symc.me.resource." + dialogIdCrumb + ".Create" + resourceType + "Dialog");
                            createDialog.setParameters(paramKeyMap);
                            createDialog.open();
//                            retData = createDialog.getSelectDataSetAll();
//                            if (retData != null)
//                            {
//                                Object retObj = retData.getValue(SearchTypedItemView.ReturnSelectedKey);
//                                if (retObj != null)
//                                {
//                                    textMECONo.setData(retObj);
//                                    textMECONo.setText(retObj.toString());
//                                }
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                });

            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setLocalDataMap(IDataMap dataMap) {
        String[] arrViewId = viewId.split(":");
        String bopType = arrViewId[0].substring(0, 1);
        String resourceType = arrViewId[1].toUpperCase();

        List<LovValue> shopLovList = (List<LovValue>) dataMap.getListValue("shopLovList");
        List<LovValue> resourceTypeLovList = (List<LovValue>) dataMap.getListValue("resourceTypeLovList");
        List<LovValue> resourceCategoryLovList = (List<LovValue>) dataMap.getListValue("resourceCategoryLovList");

        SDVLOVUtils.comboValueSetting(shopCombo, shopLovList);
        SDVLOVUtils.comboValueSetting(resourceCombo, resourceTypeLovList);
        SDVLOVUtils.comboValueSetting(categoryCombo, resourceCategoryLovList);

        shopCombo.setSelectedItem(bopType);
        resourceCombo.setSelectedItem(resourceType);

    }

    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        
        try {
            String dialogIdCrumb = "";
            String resourceCategoryValue = null;
            RawDataMap rawDataMap = new RawDataMap();
            rawDataMap.put("createMode", registry.getString("Create.Mode.KEY"));
        
            String shopValue = shopCombo.getSelectedItem().toString();
            String resourceTypeValue = resourceCombo.getSelectedItem().toString();
            Object resourceCategoryObj = categoryCombo.getSelectedItem();
            if(resourceCategoryObj == null) {
                MessageBox.post(getShell(), "Select Resource Category.", "WARNING", MessageBox.WARNING);
                throw new Exception("Select Resource Category.");
            } else {
                resourceCategoryValue = resourceCategoryObj.toString();
            }
            
            if(shopValue != null)  {
                if(shopValue.equals(registry.getString("BOP.Type.Assy"))) dialogIdCrumb=SHOP_ASSY_NAME;
                if(shopValue.equals(registry.getString("BOP.Type.Body"))) dialogIdCrumb=SHOP_BODY_NAME;
                if(shopValue.equals(registry.getString("BOP.Type.Paint"))) dialogIdCrumb=SHOP_PAINT_NAME;
                rawDataMap.put("dialogIdCrumb", dialogIdCrumb);
                rawDataMap.put("shopValue", shopValue);
            }
            if(resourceTypeValue != null)  {
                rawDataMap.put("resourceType", resourceTypeValue);
            }
            if(resourceCategoryValue != null)  {
                rawDataMap.put("resourceCategory", resourceCategoryValue);
            } 
            return rawDataMap;  

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        SelectResourceInitOperation initOperation = new SelectResourceInitOperation(viewId);
        return initOperation;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        String viewName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset.containsMap(viewName)) {
                setLocalDataMap(dataset.getDataMap(viewName));
            }
        }
    }

    @Override
    public void uiLoadCompleted() {

    }

//    protected void closeDialog() {
//        AbstractSDVSWTDialog currentDialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
//        currentDialog.close();
//    }
}
