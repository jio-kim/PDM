package com.symc.work.job;

import com.symc.common.job.task.ExecuteTask;
import com.symc.common.util.ContextUtil;
import com.symc.work.service.NotTransSearchECOService;
import com.symc.work.service.TcPeIFService;

/**
 * @author slobbie
 *
 *미전송 ECO검색을 위한 JOB
 *IF_PE_BATCH_TIME Table의 시작 시각을 체크하여,
 *그 이후에 Release된 ECO를 검색하고,
 *ECO의 Affected Project를 추출
 *Project의 연관 Product를 추출
 *검색된 Product를 IF_PE_Product Table에 등록.
 *
 */
public class NotTransSearchECOJob extends ExecuteTask {
	NotTransSearchECOService notTransSearchECOService;

    public NotTransSearchECOJob() {
        super(NotTransSearchECOJob.class);
    }

    public void setNotTransSearchECOService(NotTransSearchECOService notTransSearchECOService) {
        this.notTransSearchECOService = notTransSearchECOService;
    }

    @Override
    public String startTask() throws Exception {
    	System.out.println("SearchNotTransECOJob 실행");
    	TcPeIFService tcPeIFService = (TcPeIFService) ContextUtil.getBean("tcPeIFService");

    	long startTime = System.currentTimeMillis();
    	try{
	    	notTransSearchECOService.updateProductStat();
	    	notTransSearchECOService.createNotTransECO();

            // 배치시간을 IF_PE_BATCH_TIME 테이블에 등록
            if(tcPeIFService != null) {
                tcPeIFService.createBatchTime(startTime, System.currentTimeMillis());
            }

    	}catch(Exception e){
    		throw e;
    	}
//    	notTransSearchECOService.testCGR();
      return "";
    }
}
