package com.symc.plm.rac.prebom.common.viewer;

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;

import com.kgm.common.utils.ProgressBar;
import com.teamcenter.rac.kernel.TCComponent;

public abstract class AbstractPreSYMCViewer extends Composite {
    
    protected Composite parent;
    protected TCComponent targetComp;
    private boolean isEditable;
    
    public static String SKIP_ENABLE = "skip";
    
    public AbstractPreSYMCViewer(Composite parent) {
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
                if(child instanceof AbstractPreSYMCViewer) {
                    AbstractPreSYMCViewer symcViewer = (AbstractPreSYMCViewer)child;
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
                if(child instanceof AbstractPreSYMCViewer) {
                    AbstractPreSYMCViewer symcViewer = (AbstractPreSYMCViewer)child;
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
    
    
    /**
     * 변경부분이 있는지 Check.<br>
     * TC 기본 Check는 속성마다 입력된 control의 값과 TCProperty 값을 비교하여 틀릴경우 false.
     * @return 속성 수정(변경) 내용이 있으면 true를 반환하도록 한다.
     */
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
    
    /**
     * Added by yunjae
     * Date : 2014.09.24
     * For showing Progress 
     */
    private boolean isShowProgress;
    private ProgressBar progressShell;
    private Shell thisShell;
    private Frame thisFrame;
    
    protected void showProgress(boolean show, Shell shell){
        thisShell = shell;
        if(this.isShowProgress != show){
            if(show){
                if(progressShell == null)
                {
                    try
                    {
                        thisShell.getDisplay().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                progressShell = new ProgressBar(thisShell);
                                progressShell.start();
                            }
                        });
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }else if(progressShell != null){
                lockParent(false);

                thisShell.getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        progressShell.close();
                        progressShell = null;
                    }
                });
            }

            isShowProgress = show;
        }
    }
    
    protected void showProgress(boolean show, Shell shell, final boolean onTop){
        thisShell = shell;
        if(this.isShowProgress != show){
            if(show){
                if(progressShell == null)
                {
                    try
                    {
                        thisShell.getDisplay().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                progressShell = new ProgressBar(thisShell);
                                progressShell.start();
                                progressShell.setAlwaysOnTop(onTop);
                            }
                        });
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }else if(progressShell != null){
                lockParent(false);

                thisShell.getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        progressShell.close();
                        progressShell = null;
                    }
                });
            }

            isShowProgress = show;
        }
    }
    
    protected void lockParent(boolean lock)
    {
        for (Control childControl : thisShell.getChildren())
        {
            childControl.setEnabled(lock ? false : true);
            childControl.update();
        }
//      getShell().setEnabled(! lock);
//      getShell().update();
    }   
    
}
