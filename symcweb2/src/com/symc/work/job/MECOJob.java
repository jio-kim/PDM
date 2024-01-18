package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.work.service.MECOService;

public class MECOJob extends ExecuteTask {

	MECOService mECOService;

	public MECOJob() {
		super(MECOJob.class);
	}

	public void setMECOService(MECOService mECOService) {
		this.mECOService =  mECOService;
	}

	@Override
	public String startTask() throws Exception {
		mECOService.startService();
		return null;
	}

}
