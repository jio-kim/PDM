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

import com.ssangyong.commands.partmaster.stdpart.StdPartMasterInfoPanel;
import com.ssangyong.common.SYMCText;
import com.ssangyong.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.util.IPageComplete;

/**
 * Standard Part ViewerTab에서 사용하는 Rendering Class
 * 
 * ### 설정 방법 #####
 * properties) viewer.properties => S7_StdpartRevision.COMPONENTVIEWER=com.ssangyong.rendering.SYMCStdpartRevisionRendering
 * preference) defaultViewerConfig => S7_StdpartRevision.SYMCPropertyViewer
 */
public class SYMCStdpartRevisionRendering extends AbstractSYMCViewer implements IPageComplete {

    private StdPartMasterInfoPanel infoPanel;

    public SYMCStdpartRevisionRendering(Composite parent) {
        super(parent);
    }

    /**
     * 화면 생성
     */
    @Override
    public void createPanel(Composite parent) {
        ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        infoPanel = new StdPartMasterInfoPanel(sc, SWT.NONE);
        infoPanel.setViewMode();

        // Composite composite = infoPanel.getComposite();
        sc.setContent(infoPanel);
        sc.setMinSize(800, 600);
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

    @Override
    public void load() {
        // [SR140324-030][20140626] KOG DEV Std. Part Rendering Refresh Data.
        if (infoPanel != null) {
            infoPanel.refreshData();
        }
    }

    public void setControlReadWrite(Control control) {
        super.setControlReadWrite(control);
        infoPanel.setViewMode();
    }

    @Override
    public void save() {

        infoPanel.saveAction();
    }

    @Override
    public boolean isSavable() {

        return infoPanel.isSavable();
    }

    /**
     * [SR140324-030][20140626] KOG Std. Part Revision rendering Viewer
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
