package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.EcoRptMailSendService;

public class EcoRptMailSendJob extends ExecuteTask {

	private EcoRptMailSendService ecoRptMailSendService;
	
	public EcoRptMailSendJob() {
		super(EcoRptMailSendJob.class);
	}

	public void setEcoRptMailSendService(EcoRptMailSendService ecoRptMailSendService) {
		this.ecoRptMailSendService =  ecoRptMailSendService;
	}

	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
		    if (ecoRptMailSendService != null)
		    {
	            log.append(ecoRptMailSendService.startService());
		    }
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}

		return log.toString();
	}

}
