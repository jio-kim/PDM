package com.kgm.commands.faq;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
 */
public class FaqHandler extends AbstractHandler {

    public FaqHandler() {

    }

    @Override
    public Object execute(ExecutionEvent paramExecutionEvent) throws ExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = workbenchWindow.getActivePage();
        try {
            page.showView("com.kgm.commands.faq.FaqViewPart");
        } catch (PartInitException e) {
            e.printStackTrace();
        }

        return null;
    }

}
