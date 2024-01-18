package com.symc.common.job.listener;

import java.util.HashMap;

import com.symc.common.exception.JobExecuteException;

public class TaskThreadPool {
    private HashMap<String, JobStatusInfo> taskThreadMap;
    
    public TaskThreadPool() {
        taskThreadMap = new HashMap<String, JobStatusInfo>();
    }
    
    public synchronized JobStatusInfo setStartTaskThreadPool(String jobClassName, long startTime) throws Exception {
        /*
        JobStatusInfo jobStatusInfo = taskThreadMap.get(jobClassName);
        if(jobStatusInfo != null) {
            if(jobStatusInfo.isExcute()) {
                throw new JobExecuteException(jobClassName + " : Job이 실행 중이므로 Task를 Skip합니다.");
            }
        }
        */
        String[] mapJobClassNameList = taskThreadMap.keySet().toArray(new String[taskThreadMap.size()]);
        for (String mapJobClassName : mapJobClassNameList) {
            JobStatusInfo jobStatusInfo = taskThreadMap.get(mapJobClassName);
            if(jobStatusInfo.isExcute()) {
                throw new JobExecuteException(mapJobClassName + " : Job이 실행 중이므로 Task를 Skip합니다.");
            }
        }
        return createJobStatusInfo(jobClassName, startTime);
    }
    
    public synchronized JobStatusInfo setEndTaskThreadPool(String jobClassName, long endTime, String logFilePath) throws Exception {
        JobStatusInfo jobStatusInfo = taskThreadMap.get(jobClassName);
        if(jobStatusInfo == null) {            
            throw new JobExecuteException(jobClassName + " : Job이 실행상태가 아니므로 Job 이벤트를 처리할 수 없습니다.");
        }
        jobStatusInfo.setExcute(false);
        jobStatusInfo.setEndTime(endTime);
        jobStatusInfo.setDelayTime(jobStatusInfo.getEndTime() - jobStatusInfo.getStartTime());
        jobStatusInfo.setLogFilePath(logFilePath);
        return jobStatusInfo;
    }
    
    public synchronized boolean getRunningJobStatus(String jobClassName) throws Exception {
        JobStatusInfo jobStatusInfo = taskThreadMap.get(jobClassName);
        if(jobStatusInfo == null) {
            throw new Exception("실행 중인 Task가 아닙니다.");
        }
        return jobStatusInfo.isExcute();
    }
    
    private synchronized JobStatusInfo createJobStatusInfo(String jobClassName, long startTime) {
        JobStatusInfo jobStatusInfo = new JobStatusInfo();
        jobStatusInfo.setExcute(true);
        jobStatusInfo.setStartTime(startTime);
        taskThreadMap.put(jobClassName, jobStatusInfo);
        return jobStatusInfo;
    }
    
    public synchronized HashMap<String, JobStatusInfo> getTaskThreadMap() {
        return this.taskThreadMap;
    }
    
}
