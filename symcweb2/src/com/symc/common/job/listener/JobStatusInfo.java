package com.symc.common.job.listener;


public class JobStatusInfo {    
    public boolean isExcute = false;
    public long startTime;
    public long endTime;
    public long delayTime;
    String logFilePath;    

    public boolean isExcute() {
        return isExcute;
    }

    public void setExcute(boolean isExcute) {
        this.isExcute = isExcute;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }
    
}
