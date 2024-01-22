/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.job;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.exception.SkipException;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;

/**
 * Class Name : TCDataValidateJob
 * Class Description :
 * 
 * @date 2013. 11. 20.
 * 
 */
public abstract class TCDataMigrationJob extends Job {
    protected Cursor waitCursor;
    protected Cursor arrowCursor;
    protected Tree tree;
    protected Text logText;
    protected Shell shell;

    /**
     * @param name
     */
    public TCDataMigrationJob(Shell shell, String name, Tree tree, Text logText) {
        super(name);
        this.shell = shell;
        waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        arrowCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_ARROW);
        this.tree = tree;
        this.logText = logText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor progressMonitor) {
        Status status = null;
        try {
            shell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    shell.setCursor(waitCursor);
                }
            });
            executePre();
            execute();
            status = new Status(IStatus.OK, getName(), "Job Completed");
        } catch (Exception e) {
        	if((e instanceof SkipException)==false){
        		e.printStackTrace();
        	}
            status = new Status(IStatus.ERROR, getName(), e.getMessage());
        } finally {
            // 실행 후 처리
            try {
                executePost();
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        shell.setCursor(arrowCursor);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return status;
    }

    /**
     * Tree를 가지고 Item 하위 구조를 Expand All 한다.
     * 
     * @method searchTCDataItemList
     * @date 2013. 11. 20.
     * @param
     * @return ArrayList<TCData>
     * @exception
     * @throws
     * @see
     */
    public ArrayList<TCData> expandAllTCDataItem() throws Exception {
        // expand All pre
        expandAllTCDataItemPre();
        // expand All execute
        ArrayList<TCData> expandAllItems = null;
        final ArrayList<TCData> totalExpandAllItems = new ArrayList<TCData>();
        TreeItem[] childItems = tree.getItems();
        for (int i = 0; i < childItems.length; i++) {
        	 totalExpandAllItems.addAll(totalExpandAllItems.size(), expandTCDataItem((TCData) childItems[i], expandAllItems));
        }

//        ArrayList<TCData> rootLevelItems = new ArrayList<TCData>();
//        ImportCoreService.syncGetChildItem(shell, tree, rootLevelItems);
//        for (int i = 0; i < rootLevelItems.size(); i++) {
//            totalExpandAllItems.addAll(totalExpandAllItems.size(), expandTCDataItem(rootLevelItems.get(i), expandAllItems));
//        }
        // expand All post
        expandAllTCDataItemPost(totalExpandAllItems);
        
        return expandAllItems;
    }

    /**
     * Item 하위 구조를 Expand
     * 
     * @method expandTCDataItem
     * @date 2013. 11. 20.
     * @param
     * @return ArrayList<TCData>
     * @exception
     * @throws
     * @see
     */
    public ArrayList<TCData> expandTCDataItem(TCData tcData, ArrayList<TCData> datas) throws Exception {
        if (datas == null) {
            datas = new ArrayList<TCData>();
        }
        datas.add(tcData);
        setDataItem(datas.size() - 1, tcData);
        
        TreeItem[] childItems = tcData.getItems();
        for (int i = 0; i < childItems.length; i++) {
            TCData childData = (TCData)childItems[i];
            expandTCDataItem(childData, datas);
        }        
        
//        ArrayList<TCData> itemList = new ArrayList<TCData>();
//        ImportCoreService.syncGetItems(shell, tcData, itemList);
//        for (int i = 0; i < itemList.size(); i++) {
//            TCData childData = itemList.get(i);
//            expandTCDataItem(childData, datas);
//        }
        return datas;
    }

    /**
     * @return the tree
     */
    public Tree getTree() {
        return tree;
    }

    /**
     * @return the logText
     */
    public Text getLogText() {
        return logText;
    }

    protected abstract void setDataItem(int index, TCData tcData) throws Exception;

    protected abstract void expandAllTCDataItemPre() throws Exception;

    protected abstract void expandAllTCDataItemPost(ArrayList<TCData> expandAllItems) throws Exception;

    protected abstract void executePre() throws Exception;

    protected abstract void execute() throws Exception;

    protected abstract void executePost() throws Exception;
}
