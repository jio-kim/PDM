package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.service.CheckWiringPartService;

public class CheckWiringPartJob extends ExecuteTask
{

	private CheckWiringPartService checkWiringPartService;

	public CheckWiringPartJob()
	{
		super(CheckWiringPartJob.class);
	}

	public void setCheckWiringPartService(CheckWiringPartService checkWiringPartService)
	{
		this.checkWiringPartService = checkWiringPartService;
	}

	@Override
	public String startTask() throws Exception
	{
		StringBuffer log = new StringBuffer();
		try
		{
			if (checkWiringPartService != null)
			{
				log.append(checkWiringPartService.startService());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append(StringUtil.getStackTraceString(e));
		}
		return log.toString();
	}

}
