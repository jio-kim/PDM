package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.WeldPointCreationService;

public class WeldPointCreationJob extends ExecuteTask {
	WeldPointCreationService weldPointCreationService;

	public WeldPointCreationJob() {
		super(WeldPointCreationJob.class);
	}

	public void setWeldPointCreationService(WeldPointCreationService weldPointCreationService) {
		this.weldPointCreationService =  weldPointCreationService;
	}

	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
			log.append(IFConstants.TEXT_RETURN);
			log.append(weldPointCreationService.startWeldPointService());
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}
		return log.toString();
	}
}
