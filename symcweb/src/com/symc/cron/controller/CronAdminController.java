package com.symc.cron.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set; //

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey; 
import org.quartz.JobKey;
import org.quartz.impl.triggers.AbstractTrigger;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.symc.common.job.listener.JobStatusInfo;
import com.symc.common.job.listener.TaskThreadPool;
import com.symc.common.util.ContextUtil;
import com.symc.common.util.DateUtil;
import com.symc.work.service.EnvService;

/**
 * [20151216][ymjang] 로그파일사이즈 항목 추가
 * [20240220] [sjh] quartz 라이브러리 버전 변경으로 인한 Trigger 관련 코드 변경
 */
@Controller
@RequestMapping("/cron/*")
public class CronAdminController {
    @Resource(name="schedulerFactoryBean")
    private StdScheduler schedulerFactoryBean;


    public static final String TRIGGER_GROUP = "DEFAULT";
    public static final String JOB_GROUP = "DEFAULT";
    
    /**
     * Login Page
     *
     * @method login
     * @date 2013. 6. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @RequestMapping("/login")
    public ModelAndView login(HttpSession session) throws Exception {
    	System.out.println(session.getAttribute("CONFIRM"));
        ModelAndView mv = new ModelAndView();
        if(session.getAttribute("CONFIRM") != null) {
            return this.checkLogin(null, session, null, null);
        }
        mv.setViewName("cron/login");
        return mv;
    }

    /**
     * Check Login
     *
     * @method checkLogin
     * @date 2013. 6. 26.
     * @param
     * @return ModelAndView
     * @exception
     * @throws
     * @see
     */
    @RequestMapping("/checkLogin")
    public ModelAndView checkLogin(Model model, HttpSession session, @RequestParam(value = "id", required = false) String id, @RequestParam(value = "passwd", required = false) String passwd) throws Exception {
        ModelAndView mv = new ModelAndView();
        EnvService envService = (EnvService) ContextUtil.getBean("envService");
        String confirmLoginId = envService.getTCWebEnv().get("TC_DEMON_ID");
        String confirmLoginPasswd = envService.getTCWebEnv().get("TC_DEMON_PASSWD");
        try {
            confirmLoginPasswd = new String(Base64.decodeBase64(confirmLoginPasswd.getBytes()));
            if (session.getAttribute("CONFIRM") != null || (confirmLoginId.equals(id) && confirmLoginPasswd.equals(passwd))) {
                session.setAttribute("CONFIRM", "Y");
                mv.addObject("TASK_LIST", this.getTaskList());
                mv.setViewName("cron/process");
            } else {
                mv.setViewName("cron/login");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mv.setViewName("cron/login");
        }
        return mv;
    }

    /**
     *
     *
     * @method getTaskList
     * @date 2013. 7. 9.
     * @param
     * @return ArrayList<HashMap<String,String>>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<HashMap<String, String>> getTaskList() throws Exception  {
    	//[20240220] triggers List 타입으로 변경
        //String[] triggers = schedulerFactoryBean.getTriggerNames(TRIGGER_GROUP);
    	List<TriggerKey> triggers = new ArrayList<TriggerKey>(schedulerFactoryBean.getTriggerKeys(GroupMatcher.triggerGroupEquals(TRIGGER_GROUP)));
        ArrayList<HashMap<String, String>> triggerList = new ArrayList<HashMap<String, String>>();
        
        //[20240220] trigger 관련된 부분 수정
        //triggers.length() -> triggers.size()
        //.getTrigger(triggers[i], TRIGGER_GROUP) -> .getTrigger(TriggerKey.triggerKey(triggers.get(i).getName(), TRIGGER_GROUP))
        //trigger.getName() -> trigger.getKey().getName()
        //.getJobDetail(trigger.getJobName(), JOB_GROUP) -> .getJobDetail(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP))
        //.getTriggerState(trigger.getName(),TRIGGER_GROUP) -> .getTriggerState(TriggerKey.triggerKey(trigger.getKey().getName(), TRIGGER_GROUP))
        for (int i = 0; i < triggers.size(); i++) {
            HashMap<String, String> datas = new HashMap<String, String>();
            Trigger trigger = schedulerFactoryBean.getTrigger(TriggerKey.triggerKey(triggers.get(i).getName(), TRIGGER_GROUP));
            TaskThreadPool taskThreadPool = (TaskThreadPool)ContextUtil.getBean("taskThreadPool");
            HashMap<String, JobStatusInfo> taskThreadMap = taskThreadPool.getTaskThreadMap();
            JobStatusInfo jobStatusInfo = taskThreadMap.get(schedulerFactoryBean.getJobDetail(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP)).getJobClass().getName());
            datas.put("triggerId", trigger.getKey().getName());
            datas.put("description", schedulerFactoryBean.getJobDetail(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP)).getDescription());
            datas.put("status", schedulerFactoryBean.getTriggerState(TriggerKey.triggerKey(trigger.getKey().getName(), TRIGGER_GROUP)) + "");
            datas.put("startTime", (jobStatusInfo == null)?"-":DateUtil.getTimeInMillisToDate(jobStatusInfo.getStartTime()));
            datas.put("endTime", (jobStatusInfo == null)?"-":DateUtil.getTimeInMillisToDate(jobStatusInfo.getEndTime()));
            datas.put("delayTime", (jobStatusInfo == null)?"-":((jobStatusInfo.getDelayTime())/1000.0f) + "");
            datas.put("nextFireTime", DateUtil.formatTime(trigger.getNextFireTime()));
            datas.put("isExecute", (jobStatusInfo == null)?"-":(jobStatusInfo != null )?jobStatusInfo.isExcute()+"" : "");
            
            // [20151216][ymjang] 로그파일사이즈 항목 추가
            datas.put("logFileSize", "0");
            if (jobStatusInfo != null)
            {
            	if (jobStatusInfo.getLogFilePath() != null)
            	{
                    File downloadFile = new File(jobStatusInfo.getLogFilePath());
                    if (downloadFile != null)
                    {
    	                datas.put("logFileSize", String.valueOf(downloadFile.length()));
    	            }
            	}
            }
            
            triggerList.add(datas);
        }
        return triggerList;
    }

    /**
     *
     *
     * @method setPauseProcess
     * @date 2013. 7. 9.
     * @param
     * @return ModelAndView
     * @exception
     * @throws
     * @see
     */
    @RequestMapping("/setPauseProcess")
    public ModelAndView setPauseProcess(Model model, @RequestParam(value="id",required=true)String id) throws Exception {
    	//[20240220] trigger 관련된 부분 수정
    	//trigger.getJobName() -> trigger.getJobKey().getName()
    	//.getTriggerState(id,TRIGGER_GROUP) -> .getTriggerState(TriggerKey.triggerKey(id, TRIGGER_GROUP))
    	//.getTrigger(id, TRIGGER_GROUP) -> .getTrigger(TriggerKey.triggerKey(id, TRIGGER_GROUP))
    	//.pauseJob(trigger.getJobName(), JOB_GROUP)) -> .pauseJob(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP))
        ModelAndView mv = new ModelAndView();
        mv.addObject("PreStatus",  schedulerFactoryBean.getTriggerState(TriggerKey.triggerKey(id, TRIGGER_GROUP)));
        Trigger trigger = schedulerFactoryBean.getTrigger(TriggerKey.triggerKey(id, TRIGGER_GROUP));
        mv.addObject("JobName",  trigger.getJobKey().getName());
        schedulerFactoryBean.pauseJob(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP));
        //[20240220] trigger 클래스에서 setMisfireInstruction가 quartz 2.2.1에서 삭제됨
        //AbstractTrigger 클래스 사용하는 것으로 변경
        //trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        AbstractTrigger AbsTrigger = (AbstractTrigger) trigger;
        AbsTrigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        mv.addObject("PostStatus",  schedulerFactoryBean.getTriggerState(TriggerKey.triggerKey(id, TRIGGER_GROUP))); 
        mv.setViewName("jsonView");
        return mv;        
    }

    /**
     *
     *
     * @method setResumeProcess
     * @date 2013. 7. 9.
     * @param
     * @return ModelAndView
     * @exception
     * @throws
     * @see
     */
    @RequestMapping("/setResumeProcess")
    public ModelAndView setResumeProcess(Model model, @RequestParam(value="id",required=true)String id) throws Exception {
    	//[20240220] trigger 관련된 부분 수정
    	//.getTriggerState(id, TRIGGER_GROUP) -> .getTriggerState(TriggerKey.triggerKey(id, TRIGGER_GROUP))
    	//.getTrigger(id, TRIGGER_GROUP) -> .getTrigger(TriggerKey.triggerKey(id, TRIGGER_GROUP))
    	//trigger.getJobName() -> trigger.getJobKey().getName()
    	//.resumeJob(trigger.getJobName(), JOB_GROUP) -> resumeJob(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP)
        ModelAndView mv = new ModelAndView();
        mv.addObject("PreStatus",  schedulerFactoryBean.getTriggerState(TriggerKey.triggerKey(id, TRIGGER_GROUP))); 
        Trigger trigger = schedulerFactoryBean.getTrigger(TriggerKey.triggerKey(id, TRIGGER_GROUP));
        mv.addObject("JobName",  trigger.getJobKey().getName());
        schedulerFactoryBean.resumeJob(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP));
        //[20240220] trigger 클래스에서 setMisfireInstruction가 quartz 2.2.1에서 삭제됨
        //AbstractTrigger 클래스 사용하는 것으로 변경
        //trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        AbstractTrigger AbsTrigger = (AbstractTrigger) trigger; 
        AbsTrigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        mv.addObject("PostStatus",  schedulerFactoryBean.getTriggerState(TriggerKey.triggerKey(id, TRIGGER_GROUP)));
        mv.setViewName("jsonView");
        return mv;
    }

    /**
     *
     *
     * @method setPauseAllProcess
     * @date 2013. 7. 9.
     * @param
     * @return ModelAndView
     * @exception
     * @throws
     * @see
     */
    @RequestMapping("/setPauseAllProcess")
    public ModelAndView setPauseAllProcess(Model model) throws Exception {
        ModelAndView mv = new ModelAndView();
        schedulerFactoryBean.pauseAll();
        return mv;
    }

    /**
     *
     *
     * @method setResumeAllProcess
     * @date 2013. 7. 9.
     * @param
     * @return ModelAndView
     * @exception
     * @throws
     * @see
     */
    @RequestMapping("/setResumeAllProcess")
    public ModelAndView setResumeAllProcess(Model model) throws Exception {
        ModelAndView mv = new ModelAndView();
        schedulerFactoryBean.resumeAll();
        return mv;
    }

    /**
     *
     *
     * @method setExceuteNowProcess
     * @date 2013. 7. 9.
     * @param
     * @return ModelAndView
     * @exception
     * @throws
     * @see
     */
    @RequestMapping("/setExceuteNowProcess")
    public ModelAndView setExceuteNowProcess(Model model, @RequestParam(value="id",required=true)String id) throws Exception {
    	//[20240220] trigger 관련된 부분 수정
    	//.getTrigger(id, TRIGGER_GROUP) -> .getTrigger(TriggerKey.triggerKey(id, TRIGGER_GROUP))
    	//.getJobDetail(trigger.getJobName(), JOB_GROUP) -> .getJobDetail(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP))
    	//.triggerJob(trigger.getJobName(), "DEFAULT") -> .triggerJob(JobKey.jobKey(trigger.getKey().getName(), "DEFAULT")
        ModelAndView mv = new ModelAndView();
        Trigger trigger = schedulerFactoryBean.getTrigger(TriggerKey.triggerKey(id, TRIGGER_GROUP));
        TaskThreadPool taskThreadPool = (TaskThreadPool)ContextUtil.getBean("taskThreadPool");
        HashMap<String, JobStatusInfo> taskThreadMap = taskThreadPool.getTaskThreadMap();
        JobStatusInfo jobStatusInfo = taskThreadMap.get(schedulerFactoryBean.getJobDetail(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP)).getJobClass().getName()); 
        if(jobStatusInfo != null && jobStatusInfo.isExcute()) {
            mv.addObject("status",  "runningPass");
        } else {
            mv.addObject("status",  "execute");
            schedulerFactoryBean.triggerJob(JobKey.jobKey(trigger.getKey().getName(), "DEFAULT"));
        }
        mv.setViewName("jsonView");
        return mv;
    }

    /**
     * Log File Download
     *
     * @method download
     * @date 2013. 6. 27.
     * @param
     * @return ModelAndView
     * @exception
     * @throws
     * @see
     */
    @RequestMapping("/logFileDownload")
    public ModelAndView download(@RequestParam(value="id",required=true)String id) throws Exception {
    	//[20240220] trigger 관련된 부분 수정
    	//.getTrigger(id, TRIGGER_GROUP) -> .getTrigger(TriggerKey.triggerKey(id, TRIGGER_GROUP))
    	//.getJobDetail(trigger.getJobName(), JOB_GROUP) -> .getJobDetail(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP))
        Trigger trigger = schedulerFactoryBean.getTrigger(TriggerKey.triggerKey(id, TRIGGER_GROUP));
        TaskThreadPool taskThreadPool = (TaskThreadPool)ContextUtil.getBean("taskThreadPool");
        HashMap<String, JobStatusInfo> taskThreadMap = taskThreadPool.getTaskThreadMap();
        JobStatusInfo jobStatusInfo = taskThreadMap.get(schedulerFactoryBean.getJobDetail(JobKey.jobKey(trigger.getJobKey().getName(), JOB_GROUP)).getJobClass().getName());
        File downloadFile = new File(jobStatusInfo.getLogFilePath());
        return new ModelAndView("downloadView", "downloadFile", downloadFile);
    }
}
