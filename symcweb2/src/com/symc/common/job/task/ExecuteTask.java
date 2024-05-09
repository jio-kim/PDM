package com.symc.common.job.task;

import java.io.File;
import java.util.Properties;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.kgm.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.exception.JobExecuteException;
import com.symc.common.job.listener.JobStatusInfo;
import com.symc.common.job.listener.TaskThreadPool;
import com.symc.common.util.ContextUtil;
import com.symc.common.util.DateUtil;
import com.symc.common.util.IFConstants;
import com.symc.common.util.LogUtil;
import com.symc.common.util.StringUtil;
import com.symc.work.service.EnvService;

/**
 * [20151217] [ymjang] 오류 발생시 관리자 메일 발송 기능 추가
 */
public abstract class ExecuteTask extends QuartzJobBean {
    public static final String TRIGGER_GROUP = "DEFAULT";
    public static final String JOB_GROUP = "DEFAULT";
    public long startTime;
    public long endTime;
    public long delayTime;

    private String jobClassName;

    public ExecuteTask(Class<?> jobClass) {
        this.jobClassName = jobClass.getName();
    }

    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
        StringBuffer logs = new StringBuffer();
        String logFilePath = "";
        startTime = System.currentTimeMillis();
        String startTimeStr = "Task Started @" + DateUtil.getTimeInMillisToDate(startTime);
        Exception throableExcption = null;
        try {
            // 1. Job Start와 동시에 Lock을 건다. isExcute = true;
            // 만약 실행 중(isExcute == true) 이면 JobExecuteException 예외를 던진다.
            this.setStartTaskThreadPool(this.jobClassName, startTime);
            logs.append(IFConstants.TEXT_RETURN);
            String exeLogStr = startTask();
            logs.append((exeLogStr != null) ? exeLogStr : "");
            logs.append(IFConstants.TEXT_RETURN);
        } catch (Exception e) {
            e.printStackTrace();
            logs.append(IFConstants.TEXT_RETURN + IFConstants.TEXT_RETURN);
            logs.append("********************************* SYSTEM ERROR ***********************************");
            logs.append(IFConstants.TEXT_RETURN);
            logs.append(StringUtil.getStackTraceString(e));
            logs.append(IFConstants.TEXT_RETURN);
            logs.append("**********************************************************************************");
            logs.append(IFConstants.TEXT_RETURN + IFConstants.TEXT_RETURN);
            throableExcption = e;
        } finally {
            endTime = System.currentTimeMillis();
            String endTimeStr = "Task Ended @" + DateUtil.getTimeInMillisToDate(endTime);
            System.out.println(endTimeStr);
            delayTime = endTime - startTime;
            String delayTimeStr = "Task - 소요시간(초.0f) : " + ((delayTime) / 1000.0f);
            System.out.println(delayTimeStr);
            String logDirPath = "";
            try {
                Properties contextProperties = (Properties) ContextUtil.getBean("contextProperties");
                logDirPath = contextProperties.getProperty("task.logPath");
                logFilePath = logDirPath + "/log_[" + this.jobClassName + "]_" + DateUtil.getLogFileName("txt");
                // 2. Job End시 Lock을 해제. isExcute = false;
                // Log 파일 기록
                LogUtil.saveLog(logFilePath, startTimeStr
                        + IFConstants.TEXT_RETURN + IFConstants.TEXT_RETURN
                        + logs.toString()
                        + IFConstants.TEXT_RETURN + IFConstants.TEXT_RETURN
                        + endTimeStr + IFConstants.TEXT_RETURN  + delayTimeStr, this.getClass());
                
                // Task 중복 실행중이면 End Process 처리를 하지않는다. - END 이벤트 처리
                if (throableExcption instanceof JobExecuteException) {
                    throw throableExcption;
                } else {
                	
            		// [20151217] [ymjang] 오류 발생시 관리자 메일 발송 기능 추가
                	JobStatusInfo jobStatusInfo = setEndTaskThreadPool(this.jobClassName, endTime, logFilePath);
                	if (jobStatusInfo == null) return;
                	
            		File logFile = new File(logFilePath);
            		if (logFile == null) return;
        			
            		// 1024 byte 초과시 오류 메일 발송
        			if ( logFile.length() > 1024)
        			{
        				StringBuffer log = new StringBuffer();
        				log.append(IFConstants.TEXT_RETURN);
        				log.append("job class name : " + this.jobClassName);
        				log.append(IFConstants.TEXT_RETURN);
        				log.append("startTime : " + DateUtil.getTimeInMillisToDate(jobStatusInfo.getStartTime()));
        				log.append(IFConstants.TEXT_RETURN);
        				log.append("endTime : " + DateUtil.getTimeInMillisToDate(jobStatusInfo.getEndTime()));
        				log.append(IFConstants.TEXT_RETURN);
        				log.append("delayTime : " + ((jobStatusInfo.getDelayTime())/1000.0f) + "");
        				sendMail(log);
        			}
               }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * End Process - Log 파일 기록
     *
     * @method setEndTaskThreadPool
     * @date 2013. 7. 9.
     * @param
     * @return JobStatusInfo
     * @exception
     * @throws
     * @see
     */
    private JobStatusInfo setEndTaskThreadPool(String jobClassName, long endTime, String logFilePath) throws Exception {
        TaskThreadPool taskThreadPool = (TaskThreadPool) ContextUtil.getBean("taskThreadPool");
        return taskThreadPool.setEndTaskThreadPool(jobClassName, endTime, logFilePath);

    }

    /**
     * Start Process
     *
     * @method setStartTaskThreadPool
     * @date 2013. 7. 9.
     * @param
     * @return JobStatusInfo
     * @exception
     * @throws
     * @see
     */
    private JobStatusInfo setStartTaskThreadPool(String jobClassName, long startTime) throws Exception {
        TaskThreadPool taskThreadPool = (TaskThreadPool) ContextUtil.getBean("taskThreadPool");
        return taskThreadPool.setStartTaskThreadPool(jobClassName, startTime);
    }

    public abstract String startTask() throws Exception;
    
    /**
     * [20151217] [ymjang] 오류 발생시 관리자 메일 발송 기능 추가
     * @param log
     */
    @SuppressWarnings({ "unchecked" })
	private void sendMail(StringBuffer log){

    	try 
    	{
            EnvService envService = (EnvService) ContextUtil.getBean("envService");
            String plm_admin = envService.getTCWebEnv().get("PLM_ADMIN");
            
        	String title = "New PLM : SYMC WEB Scheduler Error 알림";
        	String body = "<PRE>";
    	    body += log.toString();
    	    body += "</PRE>";

        	DataSet ds = new DataSet();
    		ds.put("the_sysid", "NPLM");
    		ds.put("the_sabun", "NPLM");

    		ds.put("the_title", title);
    		ds.put("the_remark", body);
    		ds.put("the_tsabun", plm_admin);
    		//EAI 변경
			TcCommonDao.getTcCommonDao().update("com.symc.interface.sendMailEai", ds);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
}
