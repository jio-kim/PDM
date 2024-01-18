package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.soa.biz.Session;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.FullPreProductInterfaceService;

public class FullPreProductInterfaceJob extends ExecuteTask {
    private FullPreProductInterfaceService preProductInterfaceService;
    private Session session;

	public FullPreProductInterfaceJob() {
		super(FullPreProductInterfaceJob.class);
	}

	public void setFullPreProductInterfaceService(FullPreProductInterfaceService preProductInterfaceService) {
		this.preProductInterfaceService =  preProductInterfaceService;
	}

    public void setSession(Session session) {
        this.session = session;
    }
    
    public void setPreProductInterfaceService(FullPreProductInterfaceService preProductInterfaceService) {
        this.preProductInterfaceService =  preProductInterfaceService;
    }

	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
		    if (preProductInterfaceService != null)
		    {
		        if (session != null)
		            preProductInterfaceService.setSession(session);

	            log.append(preProductInterfaceService.startPreProductInterfaceService());
		    }
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}

		return log.toString();
	}
}
