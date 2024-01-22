/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.job;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;

/**
 * Class Name : TCDataExecuteJob
 * Class Description :
 * 
 * @date 2013. 11. 20.
 * 
 */
public class TCDataValidatetJob extends TCDataMigrationJob {

    /**
     * 
     * @param shell
     * @param name
     * @param tree
     * @param handleObjectMap
     */
    public TCDataValidatetJob(Shell shell, String name, Tree tree, Text logText) {
        super(shell, name, tree, logText);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#executePre()
     */
    @Override
    protected void executePre() throws Exception {

    }

    /**
     * Validate
     * 
     * @method validate
     * @date 2013. 11. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void validate() throws Exception {
        expandAllTCDataItem();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#execute()
     */
    @Override
    protected void execute() throws Exception {
        validate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#executePost()
     */
    @Override
    protected void executePost() throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#setDataItem(com.symc.plm.me.sdv.service.migration.model.TCData)
     */
    @Override
    protected void setDataItem(int index, TCData tcData) throws Exception {
        ImportCoreService.syncSetItemSelection(shell, tree, tcData);
        ImportCoreService.syncItemState(shell, tcData, TCData.STATUS_WARNING, "~~~");
        StringBuffer itemText = new StringBuffer();
        ImportCoreService.syncGetItemText(shell, tcData, 0, itemText);
        ImportCoreService.syncSetItemTextField(shell, getLogText(), index + " >> " + itemText.toString() + " STATUS_WARNING : ~~~ " + "\n\r");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#expandAllTCDataItemPre()
     */
    @Override
    protected void expandAllTCDataItemPre() throws Exception {
        ImportCoreService.syncSetItemTextField(shell, getLogText(), " Start... " + "\n\r");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#expandAllTCDataItemPost(java.util.ArrayList)
     */
    @Override
    protected void expandAllTCDataItemPost(ArrayList<TCData> expandAllItems) throws Exception {
        ImportCoreService.syncSetItemTextField(shell, (Text) getLogText(), " End... " + "\n\r");

    }

}
