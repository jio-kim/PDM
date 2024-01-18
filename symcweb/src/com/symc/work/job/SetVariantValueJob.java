package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.work.service.TcVariantService;

public class SetVariantValueJob extends ExecuteTask {
    TcVariantService tcVariantService;

    public SetVariantValueJob() {
        super(SetVariantValueJob.class);
    }

    public void setTcVariantService(TcVariantService tcVariantService) {
        this.tcVariantService = tcVariantService;
    }

    @Override
    public String startTask() throws Exception {
        tcVariantService.createVariantValue();
      return "";
    }
}
