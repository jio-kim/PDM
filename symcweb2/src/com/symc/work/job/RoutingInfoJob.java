package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.RoutingInfoService;

public class RoutingInfoJob extends ExecuteTask {

	private RoutingInfoService routingInfoService;

	public RoutingInfoJob() {
		super(RoutingInfoJob.class);
	}

	public void setRoutingInfoService(RoutingInfoService routingInfoService) {
		this.routingInfoService =  routingInfoService;
	}

	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
			log.append(IFConstants.TEXT_RETURN);
			log.append(routingInfoService.startService());
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}
		return log.toString();
	}

}
