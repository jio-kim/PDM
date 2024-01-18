package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.soa.biz.Session;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.OccEffectivityIdService;

public class OccEffectivityIdJob extends ExecuteTask {

	private OccEffectivityIdService occEffectivityIdService;
	private Session session;
	
	public OccEffectivityIdJob() {
		super(OccEffectivityIdJob.class);
	}

	public void setOccEffectivityIdService(OccEffectivityIdService occEffectivityIdService) {
		this.occEffectivityIdService =  occEffectivityIdService;
	}

	public void setSession(Session session) {
        this.session = session;
    }
	
	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
		    if (occEffectivityIdService != null)
		    {
		        if (session != null)
		        	occEffectivityIdService.setSession(session);

	            log.append(occEffectivityIdService.startService());
		    }
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}

		return log.toString();
	}

}
