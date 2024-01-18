package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.soa.biz.Session;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.BPInterfaceService;

public class BPInterfaceJob extends ExecuteTask {

	private BPInterfaceService bpInterfaceService;
	private Session session;
	
	public BPInterfaceJob() {
		super(BPInterfaceJob.class);
	}

	public void setBpInterfaceService(BPInterfaceService bpInterfaceService) {
		this.bpInterfaceService =  bpInterfaceService;
	}

	public void setSession(Session session) {
        this.session = session;
    }
	
	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
		    if (bpInterfaceService != null)
		    {
		        if (session != null)
		        	bpInterfaceService.setSession(session);

	            log.append(bpInterfaceService.startService());
		    }
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}

		return log.toString();
	}

}
