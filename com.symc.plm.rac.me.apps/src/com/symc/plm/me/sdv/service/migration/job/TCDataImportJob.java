/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.job;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.symc.plm.me.sdv.service.migration.formatter.IImportFormat;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;

/**
 * Class Name : TCDataImportJob
 * Class Description :
 * 
 * @date 2013. 11. 20.
 * 
 */
public class TCDataImportJob extends TCDataMigrationJob {

    IImportFormat importFormmat;

    /**
     * @param shell
     * @param name
     * @param tree
     * @param handleObjectMap
     */
    public TCDataImportJob(Shell shell, String name, Tree tree, Text logText, IImportFormat importFormmat) {
        super(shell, name, tree, logText);
        this.importFormmat = importFormmat;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#setDataItem(int, com.symc.plm.me.sdv.service.migration.model.TCData)
     */
    @Override
    protected void setDataItem(int index, TCData tcData) throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#expandAllTCDataItemPre()
     */
    @Override
    protected void expandAllTCDataItemPre() throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#expandAllTCDataItemPost(java.util.ArrayList)
     */
    @Override
    protected void expandAllTCDataItemPost(ArrayList<TCData> expandAllItems) throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#executePre()
     */
    @Override
    protected void executePre() throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#execute()
     */
    @Override
    protected void execute() throws Exception {
        importTCData();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#executePost()
     */
    @Override
    protected void executePost() throws Exception {

    }

    /**
     * Import ผ๖วเ
     * 
     * @method importTCData
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unused")
    protected void importTCData() throws Exception {
        Tree importTree = this.importFormmat.getImportTree();
    }

}
