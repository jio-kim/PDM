package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.work.service.TcLOVService;

/**
 * SUPPLY_MODE LOV 등록
 *
 *
 */
public class SetSupplyModeJob extends ExecuteTask {
    TcLOVService tcLOVService;
    public SetSupplyModeJob() {
        super(SetSupplyModeJob.class);
    }

    public void setTcLOVService(TcLOVService tcLOVService) {
        this.tcLOVService = tcLOVService;
    }

    @Override
    public String startTask() throws Exception {
      tcLOVService.createEnvValues("S7_SUPPLY_MODE");
      return "";
    }

}
