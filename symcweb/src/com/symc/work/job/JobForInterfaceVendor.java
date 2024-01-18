package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.soa.biz.Session;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.InterfaceVendorService;

public class JobForInterfaceVendor extends ExecuteTask {
	private InterfaceVendorService interfaceVendorService;
    private Session session;

	public JobForInterfaceVendor() {
		super(JobForInterfaceVendor.class);
	}

	public void setInterfaceVendorService(InterfaceVendorService interfaceVendorService) {
		this.interfaceVendorService =  interfaceVendorService;
	}

    public void setSession(Session session) {
        this.session = session;
    }

	@Override
	public String startTask() throws Exception {
		StringBuffer log = new StringBuffer();
		try{
			log.append(IFConstants.TEXT_RETURN);
			if (interfaceVendorService != null)
			{
                if (session != null)
                    interfaceVendorService.setSession(session);

			    log.append(interfaceVendorService.startInterfaceVendorService());
			}
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}
		return log.toString();
	}
}
