package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.soa.biz.Session;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.InterfaceCostService;

public class JobForInterfaceCost extends ExecuteTask {
	private InterfaceCostService interfaceCostService;
    private Session session;

	public JobForInterfaceCost() {
		super(JobForInterfaceCost.class);
	}

	public void setInterfaceCostService(InterfaceCostService interfaceCostService) {
		this.interfaceCostService =  interfaceCostService;
	}

    public void setSession(Session session) {
        this.session = session;
    }

	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
			log.append(IFConstants.TEXT_RETURN);
			if (interfaceCostService != null)
			{
                if (session != null)
                    interfaceCostService.setSession(session);

			    log.append(interfaceCostService.startInterfaceCostService());
			}
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}
		return log.toString();
	}
}
