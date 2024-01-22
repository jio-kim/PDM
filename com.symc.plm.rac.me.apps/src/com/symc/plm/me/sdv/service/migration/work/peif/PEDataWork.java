/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.work.peif;

import org.eclipse.swt.widgets.Shell;

import com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;

/**
 * Class Name : TCDataWork
 * Class Description :
 * 
 * @date 2013. 11. 25.
 * 
 */
public class PEDataWork {
    public static final String COMPLETED_MESSAGE = "OK";

    protected Shell shell;
    protected TCDataMigrationJob tcDataMigrationJob;
    protected TCComponentMfgBvrProcess processLine;
    protected String mecoNo;
    protected boolean isOverride;

    public PEDataWork(Shell shell, TCDataMigrationJob tcDataMigrationJob, TCComponentMfgBvrProcess processLine, String mecoNo, boolean isOverride) {
        this.shell = shell;
        this.tcDataMigrationJob = tcDataMigrationJob;
        this.processLine = processLine;
        this.mecoNo = mecoNo;
        this.isOverride = isOverride;
    }

    /**
     * @return the tcDataMigrationJob
     */
    public TCDataMigrationJob getTcDataMigrationJob() {
        return tcDataMigrationJob;
    }

}
