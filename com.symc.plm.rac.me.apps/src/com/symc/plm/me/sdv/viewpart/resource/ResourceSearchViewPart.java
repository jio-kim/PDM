package com.symc.plm.me.sdv.viewpart.resource;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.IHandlerService;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.viewpart.AbstractSDVViewPart;

import com.symc.plm.me.sdv.operation.resource.ExportExcelOperation;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.view.resource.ResourceTabViewPane;
import com.symc.plm.me.sdv.view.resource.ResourceTableViewPane;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.symc.plm.activator.Activator;
import com.teamcenter.rac.util.MessageBox;

public class ResourceSearchViewPart extends AbstractSDVViewPart {
    private Action excelExportAction;
    private Action reviseAction;
    private ResourceTabViewPane resourceTabViewPane;
    private Action assignAction;
    private Action sendToAction;

    public ResourceSearchViewPart() {

    }

    @Override
    protected void initUI(Composite container) {
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        resourceTabViewPane = new ResourceTabViewPane(container, SWT.NONE);

        createActions();
        initializeToolBar();
    }

    protected void createActions() {
        {
            // 자원 할당
            assignAction = new Action("Assign Resource", Activator.imageDescriptorFromPlugin("com.symc.plm.rac.me.apps", "icons/mrmadd_16.png")) {
                @Override
                public void run() {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            assignResource();
                        }
                    });
                }
            };

            // Send to My Teamcenter
            sendToAction = new Action("Send To My Teamcenter", Activator.imageDescriptorFromPlugin("com.teamcenter.rac.ui", "icons_16/navigator_16.png")) {
                @Override
                public void run() {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            sendToMyTeamcenter();
                        }
                    });
                }
            };
            
            // Revise (자원 Revise)
            reviseAction = new Action("Revise Resource", Activator.imageDescriptorFromPlugin("com.symc.plm.rac.me.apps", "icons/ReviseEquipment.png")) {
                @Override
                public void run() {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            reviseResource();
                        }
                    });
                }
            };

            // Excel Export (검색 Table Excel 출력)
            excelExportAction = new Action("Excel Export", Activator.imageDescriptorFromPlugin("com.symc.plm.rac.me.apps", "icons/excel_16.png")) {
                @Override
                public void run() {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            exportExcel();
                        }
                    });
                }
            };
        }
    }

    protected void initializeToolBar() {
        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
        toolbarManager.add(assignAction);
        toolbarManager.add(sendToAction);
        toolbarManager.add(reviseAction);
        toolbarManager.add(excelExportAction);
    }

    public ResourceTableViewPane getCurrentTable() {
        ResourceTableViewPane currentTable = resourceTabViewPane.getCurrentTable();
        return currentTable;
    }
    
    public Button getCurrentSearchButton() {
        Button button = resourceTabViewPane.getCurrentSearchButton();
        return button;
    }

    /**
     * My Teamcenter로 보내기
     */
    private void sendToMyTeamcenter() {
        InterfaceAIFComponent[] seletedComponents = getCurrentTable().getSelectedItems();
        if (seletedComponents == null || seletedComponents.length == 0) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "Please select resource.", "ERROR", MessageBox.ERROR);
        }

        // SendTo 실행
        else {
            com.teamcenter.rac.common.Activator.getDefault().openPerspective("com.teamcenter.rac.ui.perspectives.navigatorPerspective");
            com.teamcenter.rac.common.Activator.getDefault().openComponents("com.teamcenter.rac.ui.perspectives.navigatorPerspective", seletedComponents);
        }
    }

    /**
     * 자원 할당 함수
     */
    private void assignResource() {
        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
        try {
            handlerService.executeCommand("symc.me.resource.AssignResourceCommand", null);
        } catch (Exception e) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "ERROR", MessageBox.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * 자원 Revise 함수
     */
    private void reviseResource() {
        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
        try {
            InterfaceAIFComponent[] seletedComponents = getCurrentTable().getSelectedItems();
            if (seletedComponents == null || seletedComponents.length == 0) {
                throw new Exception("Please select resource.");
            }

            InterfaceAIFComponent targetComponent = seletedComponents[0];

            String itemId = targetComponent.getProperty("item_id");
            String itemType = targetComponent.getType();

            String bopType = ResourceUtilities.getBOPType(itemId);
            String resoureType = StringUtils.containsIgnoreCase(itemType, "Tool") ? "Tool" : StringUtils.containsIgnoreCase(itemType, "Subsidiary") ? "Subsidiary" : "Equipment";

            if (resoureType.equals("Subsidiary")) {
                throw new Exception("Can not revise subsidiary materials.");
            }

            if (bopType == null || resoureType == null) {
                throw new Exception("Can not find a suitable command id.");
            }

            // Command ID 유형 6가지
            // symc.me.resource.Body.ReviseEquipmentCommand
            // symc.me.resource.Body.ReviseToolCommand
            // symc.me.resource.Assy.ReviseEquipmentCommand
            // symc.me.resource.Assy.ReviseToolCommand
            // symc.me.resource.Paint.ReviseEquipmentCommand
            // symc.me.resource.Paint.ReviseToolCommand
            handlerService.executeCommand("symc.me.resource." + bopType + ".Revise" + resoureType + "Command", null);
        } catch (Exception e) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "ERROR", MessageBox.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Excel Export (검색 Table Excel 출력) 함수
     */
    protected void exportExcel() {
        ExportExcelOperation exportExcelOperation = new ExportExcelOperation(getCurrentTable().getAllRowValues());
        try {
            exportExcelOperation.executeOperation();
        } catch (Exception e) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "ERROR", MessageBox.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    protected void assignTCComponent() {
        MessageBox.post(getShell(), "검색 창 숨기기. (기능 미반영)", "INFORMATION", MessageBox.INFORMATION);
    }

    @Override
    public void setFocus() {

    }

    @Override
    protected void afterCreateContents() {

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
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

}
