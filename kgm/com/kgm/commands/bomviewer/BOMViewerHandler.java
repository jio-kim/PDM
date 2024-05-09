package com.kgm.commands.bomviewer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;

public class BOMViewerHandler extends AbstractHandler {
    public BOMViewerHandler() {
    }

    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = workbenchWindow.getActivePage();
        IViewPart showView = null;
        
        try {
        	String sRandom = String.valueOf(Math.random());
            showView = page.showView("com.kgm.commands.bomviewer.BOMViewerViewPart", sRandom, IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            e.printStackTrace();
        }

        BOMViewerViewPart viewPart = (BOMViewerViewPart) showView;
        
        BOMViewerPanel panel = viewPart.getPanel();
        
        TCComponent component = getTarget();
        panel.setComponent(component);
        panel.search();
        
        return viewPart;
    }
    
    /**
     * 
     * @return
     */
    public TCComponent getTarget() {
        TCComponent target = null;
        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

        if(aaifcomponentcontext != null && aaifcomponentcontext.length == 1)
        {
            target = (TCComponent) aaifcomponentcontext[0].getComponent();
            return target;
        }
        
        return target;
    }
}