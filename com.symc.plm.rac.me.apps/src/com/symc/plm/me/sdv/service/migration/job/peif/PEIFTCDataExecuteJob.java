/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.job.peif;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.symc.plm.me.sdv.service.migration.exception.StopException;
import com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.work.peif.PEIFJobWork;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Utilities;

/**
 * Class Name : PEIFTCDataExecuteJob
 * Class Description :
 * 
 * @date 2013. 11. 20.
 * 
 */
public class PEIFTCDataExecuteJob extends TCDataMigrationJob {
    protected String mecoNo;
    protected boolean isOverride;
    protected String folderPath;
    public String startIndex;
    public String totalRowCount;
    protected PEIFJobWork peIFJobWork;
    protected int status = -1;
    protected boolean stopflag;

    TCComponentMfgBvrProcess processLine;
    public static final int STATUS_VALIDATE = 0;
    public static final int STATUS_EXECUTE = 1;

    /**
     * @param shell
     * @param name
     * @param tree
     * @param handleObjectMap
     */
    public PEIFTCDataExecuteJob(Shell shell, String name, Tree tree, Text logText, TCComponentMfgBvrProcess processLine, String folderPath, String mecoNo, boolean isOverride, String _startIndex, String _totalRowCount) {
        super(shell, name, tree, logText);
        this.mecoNo = mecoNo;
        this.folderPath = folderPath;
        this.processLine = processLine;
        this.isOverride = isOverride;
        startIndex = _startIndex;
        totalRowCount = _totalRowCount;
        this.stopflag = false;
        
//        String lineProcessId = null;
//		try {
//			lineProcessId = processLine.getItem().getProperty("item_id");
//		} catch (TCException e) {
//			e.printStackTrace();
//		}
//        
//		String latestInterfaceLineProcessId = Utilities.getCookie("BOPCustomCookie", "Pe2TCM_LineProcessId", true);
//		if(lineProcessId!=null && latestInterfaceLineProcessId!=null){
//			if(lineProcessId.trim().equalsIgnoreCase(latestInterfaceLineProcessId.trim())){
//				isReInterface = true;
//			}
//		}
//		
//		System.out.println("lineProcessId = "+lineProcessId);
//		System.out.println("latestInterfaceLineProcessId = "+latestInterfaceLineProcessId);
//		System.out.println("$ isReInterface = "+isReInterface);
//		
//		if(lineProcessId!=null && lineProcessId.trim().length()>0){
//			Utilities.setCookie("BOPCustomCookie", true, "Pe2TCM_LineProcessId", lineProcessId);
//		}
        
        peIFJobWork = new PEIFJobWork(shell, this, processLine, folderPath, mecoNo, isOverride);
//        peIFJobWork.setReinterface(isReInterface);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#setDataItem(int, com.symc.plm.me.sdv.service.migration.model.TCData)
     */
    @Override
    protected void setDataItem(int index, TCData tcData) throws Exception {
        peIFJobWork.setDataItem(status, index, tcData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#expandAllTCDataItemPre()
     */
    @Override
    protected void expandAllTCDataItemPre() throws Exception {
        // 강제종료 flag
        if (stopflag) {
            throw new StopException("사용자 강제 종료");
        }
        peIFJobWork.expandAllTCDataItemPre(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob#expandAllTCDataItemPost(java.util.ArrayList)
     */
    @Override
    protected void expandAllTCDataItemPost(ArrayList<TCData> expandAllItems) throws Exception {
        peIFJobWork.expandAllTCDataItemPost(status, expandAllItems);
        // 강제종료 flag
        if (stopflag) {
            throw new StopException("사용자 강제 종료");
        }
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
        peIFJobWork.executeWork();
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
     * @return the logText
     */
    public Text getLogText() {
        return logText;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the mecoNo
     */
    public String getMecoNo() {
        return mecoNo;
    }

    /**
     * @return the peIFJobWork
     */
    public PEIFJobWork getPeIFJobWork() {
        return peIFJobWork;
    }

    /**
     * @return the stopflag
     */
    public boolean isStopflag() {
        return stopflag;
    }

    /**
     * @param stopflag
     *            the stopflag to set
     */
    public void setStopflag(boolean stopflag) {
        this.stopflag = stopflag;
    }

}
