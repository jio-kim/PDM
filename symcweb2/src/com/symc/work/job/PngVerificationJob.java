package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.work.service.PngVerificationService;

public class PngVerificationJob extends ExecuteTask {
	private PngVerificationService servicePngVerification;

	public PngVerificationJob() {
		super(PngVerificationJob.class);
	}
	
	public void setPngVerificationService(PngVerificationService servicePngVerification) {
		this.servicePngVerification = servicePngVerification;
	}

	@Override
	public String startTask() throws Exception {
		servicePngVerification.startService();
		return null;
	}

}
