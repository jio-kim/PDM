package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.work.service.DCSService;

public class DCSJob extends ExecuteTask {

	DCSService dCSService;

	public DCSJob() {
		super(DCSJob.class);
	}

	public void setDCSService(DCSService dCSService) {
		this.dCSService = dCSService;
	}

	@Override
	public String startTask() throws Exception {
		dCSService.startService();

		return null;
	}

}
