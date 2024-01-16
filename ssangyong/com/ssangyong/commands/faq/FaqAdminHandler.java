package com.ssangyong.commands.faq;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
 */
public class FaqAdminHandler extends AbstractHandler {

    public FaqAdminHandler() {

    }

    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = workbenchWindow.getActivePage();
        try {
            page.showView("com.ssangyong.commands.faq.FaqAdminViewPart");
        } catch (PartInitException e) {
            e.printStackTrace();
        }

        return null;
    }

}
