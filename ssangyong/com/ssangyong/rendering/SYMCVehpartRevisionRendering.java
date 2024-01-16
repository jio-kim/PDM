package com.ssangyong.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;

import com.ssangyong.commands.partmaster.vehiclepart.VehiclePartMasterInfoPanel;
import com.ssangyong.common.SYMCText;
import com.ssangyong.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.util.IPageComplete;

/**
 * Vehicle Part ViewerTab에서 사용하는 Rendering Class
 * 
 * ### 설정 방법 #####
 * properties) viewer.properties => S7_VehpartRevision.COMPONENTVIEWER=com.ssangyong.rendering.SYMCVehpartRevisionRendering
 * preference) defaultViewerConfig => S7_VehpartRevision.SYMCPropertyViewer
 */
public class SYMCVehpartRevisionRendering extends AbstractSYMCViewer implements IPageComplete {

    private VehiclePartMasterInfoPanel infoPanel;

    public SYMCVehpartRevisionRendering(Composite parent) {
        super(parent);
    }

    /**
     * 화면 생성
     */
    @Override
    public void createPanel(Composite parent) {
        ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        infoPanel = new VehiclePartMasterInfoPanel(sc, SWT.NONE);
        infoPanel.setViewMode();
        infoPanel.setRenderingMode();
        // Composite composite = infoPanel.getComposite();
        sc.setContent(infoPanel);
        // [SR140324-030][20140626] KOG Veh. Part Revision Rendering Viewer Size 변경.
        // [SR140729-026][20140806] jclee Veh. Part Revision Rendering Viewer Size 변경.
        // sc.setMinSize(760, 800);
        sc.setMinSize(770, 850);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

    }

    /**
     * 저장 여부 확인
     */
    @Override
    public boolean isDirty() { // TODO
        return infoPanel.isModified();
    }

    @Override
    public boolean isPageComplete() {

        return true;
    }

    /**
     * Page Load시 호출
     * 
     */
    @Override
    public void load() {

        if (infoPanel != null) {
            infoPanel.refreshData();
        }

    }

    public void setControlReadWrite(Control control) {
        super.setControlReadWrite(control);
        infoPanel.setViewMode();
    }

    /**
     * Save Action
     */
    @Override
    public void save() {
        infoPanel.saveAction();
    }

    /**
     * Save Validation
     */
    @Override
    public boolean isSavable() {
        return infoPanel.isSavable();
    }

    /**
     * [SR140324-030][20140626] KOG DEV Veh. Part Revision rendering Viewer
     * SES Spec No. Text Enable > True, Editable > false
     */
    @Override
    public void setControlReadOnly(Control control) {
        if (control instanceof Composite) {
            Control[] children = ((Composite) control).getChildren();
            for (Control child : children) {
                if (child.getData(SKIP_ENABLE) != null) {
                    continue;
                }
                if (child instanceof AbstractSYMCViewer) {
                    AbstractSYMCViewer symcViewer = (AbstractSYMCViewer) child;
                    symcViewer.setControlReadOnly(symcViewer);
                    continue;
                } else if (child instanceof Label) {
                    continue;
                } else if (child instanceof Combo || child instanceof DateTime || child instanceof StyledText) {
                    if (child instanceof SYMCText) {
                        SYMCText text = (SYMCText) child;
                        Object object = text.getData();
                        if (object != null && object instanceof String) {
                            String data = (String) object;
                            if ("SES_SPEC_NO_TEXT".equals(data)) {
                                text.setEditable(false);
                                continue;
                            }
                        }
                    }
                    child.setEnabled(false);
                }
                if (child instanceof Composite || child instanceof ScrolledComposite || child instanceof TabFolder || child instanceof Table) {
                    setControlReadOnly((Composite) child);
                } else {
                    child.setEnabled(false);
                }
            }
        } else {
            control.setEnabled(false);
        }
    }
}
