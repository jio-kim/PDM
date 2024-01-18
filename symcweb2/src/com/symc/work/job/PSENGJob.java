package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.work.service.PSENGService;

/**
 * [SR141119-021][20150119] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰
 * 1. 최초 생성
 *
 */
public class PSENGJob extends ExecuteTask {

    PSENGService pSENGService;

	public PSENGJob() {
		super(PSENGJob.class);
	}

	public void setPSENGService(PSENGService pSENGService) {
		this.pSENGService =  pSENGService;
	}

	@Override
	public String startTask() throws Exception {
	    pSENGService.startService();
		return null;
	}

}
