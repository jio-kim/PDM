package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.work.service.SWMService;

public class SWMJob extends ExecuteTask {

	SWMService  sWmService;

	public SWMJob() {
	    super(SWMJob.class);
	}

	public void setsWmService(SWMService sWmService) {
        this.sWmService = sWmService;
    }

	@Override
	public String startTask() throws Exception {
		sWmService.startService();
		return null;
	}

}
