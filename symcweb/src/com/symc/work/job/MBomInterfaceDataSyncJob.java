package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.MBomInterfaceDataSyncService;

public class MBomInterfaceDataSyncJob extends ExecuteTask {

	MBomInterfaceDataSyncService mBomInterfaceDataSyncService;

	public MBomInterfaceDataSyncJob() {
		super(MBomInterfaceDataSyncJob.class);
	}

	public void setMBomInterfaceDataSyncService(MBomInterfaceDataSyncService mBomInterfaceDataSyncService) {
		this.mBomInterfaceDataSyncService =  mBomInterfaceDataSyncService;
	}

	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
			log.append(IFConstants.TEXT_RETURN);
			log.append(mBomInterfaceDataSyncService.updateBpnDate());
		}catch(Exception e){
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}
		return log.toString();
	}

}
