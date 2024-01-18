package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.soa.biz.Session;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.FullPreProductInterfaceService;
import com.symc.work.service.FullPreProductNotInterfaceService;

public class FullPreProductNotInterfaceJob extends ExecuteTask {
    private FullPreProductNotInterfaceService preProductNotInterfaceService;
    private Session session;

	public FullPreProductNotInterfaceJob() {
		super(FullPreProductNotInterfaceJob.class);
	}

	public void setFullPreProductNotInterfaceService(FullPreProductNotInterfaceService preProductNotInterfaceService) {
		this.preProductNotInterfaceService =  preProductNotInterfaceService;
	}

    public void setSession(Session session) {
        this.session = session;
    }
    
    public void setPreProductNotInterfaceService(FullPreProductNotInterfaceService preProductNotInterfaceService) {
        this.preProductNotInterfaceService =  preProductNotInterfaceService;
    }

	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
		    if (preProductNotInterfaceService != null)
		    {
		        if (session != null)
		        	preProductNotInterfaceService.setSession(session);

	            log.append(preProductNotInterfaceService.startPreProductInterfaceService());
		    }
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}

		return log.toString();
	}
}
