package com.symc.plm.me.common.viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;

import com.teamcenter.rac.kernel.TCComponent;

public abstract class AbstractSDVViewer extends Composite {
    
    protected Composite parent;
    protected TCComponent targetComp;
    private boolean isEditable;
    
    public static String SKIP_ENABLE = "skip";
    
    public AbstractSDVViewer(Composite parent) {
        super(parent, SWT.None);
        this.parent = parent;
        setLayout(new FillLayout());
        createPanel(this);
    }
    
    public void setComponent(TCComponent targetComp) {
        this.targetComp = targetComp;
    }
    
    public TCComponent getComponent() {
        return targetComp;
    }
    
    public void setControlSkipEnable(Control control, boolean flag) {
        if(flag) {
            control.setData(SKIP_ENABLE, "true");
        } else {
            control.setData(SKIP_ENABLE, null);
        }
    }
    
    public void refresh() {
        load();
    }
    
    public void setEditable(boolean editable) {
        isEditable = editable;
    }
    
    public boolean isEditable() {
        return isEditable;
    }
    
    public void setControlReadOnly(Control control) {
        //composite.setEnabled(false);
        if(control instanceof Composite) {
            Control[] children = ((Composite)control).getChildren();
            for(Control child : children) {
                if(child.getData(SKIP_ENABLE) != null) {
                    continue;
                }
                if(child instanceof AbstractSDVViewer) {
                    AbstractSDVViewer symcViewer = (AbstractSDVViewer)child;
                    symcViewer.setControlReadOnly(symcViewer);
                    continue;
                } else if(child instanceof Label) {
                    continue;
                } else if(child instanceof Combo || child instanceof DateTime || child instanceof StyledText) {
                    child.setEnabled(false);
                }
                if(child instanceof Composite || child instanceof ScrolledComposite || child instanceof TabFolder || child instanceof Table) {
                    setControlReadOnly((Composite)child);
                } else {
                    child.setEnabled(false);
                }
            }
        } else {
            control.setEnabled(false);
        }
    }
    
    public void setControlReadWrite(Control control) {
        //composite.setEnabled(true);
            Control[] children = ((Composite)control).getChildren();
            for(Control child : children) {
                if(child.getData(SKIP_ENABLE) != null) {
                    continue;
                }
                if(child instanceof AbstractSDVViewer) {
                    AbstractSDVViewer symcViewer = (AbstractSDVViewer)child;
                    symcViewer.setControlReadWrite(symcViewer);
                    continue;
                } else if(child instanceof Composite) {
                    child.setEnabled(true);
                    setControlReadWrite((Composite)child);
                } else {
                    child.setEnabled(true);
                }
            }
    }
    
    

    public boolean isDirty() {
        if(this.isDisposed() || targetComp == null || !targetComp.isCheckedOut()) {
            return false;
        }
        return true;
    }
    
    public abstract void load();
    
    public abstract void save();
    
    public abstract boolean isSavable();
    
    public abstract void createPanel(Composite parent);

    //public abstract Composite getComposite();
    
}
