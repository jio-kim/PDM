package com.kgm.common.remote;

import java.io.File;

import com.kgm.soa.bop.util.LogFileUtility;
import com.kgm.soa.dao.EnvDao;
import com.kgm.soa.dao.SendMailEAIDao;

public class SendMailEAIUtil
{

	public void sendMailEAI(String typeOfReport, String targetInfoStr, LogFileUtility logFileUtility, String reportFilePath, boolean isSuccess, String userId)
	{
    	File reportFile = new File(reportFilePath);
    	String mailSubject = "[PLM] " + typeOfReport + " : " + targetInfoStr;
    	String mailContenText = "";
		String startTimeStr = logFileUtility.getStartTimeString();
		String elapsedTimeStr = logFileUtility.getElapsedTime();

		if (isSuccess)
		{
			mailContenText = "요청하신 " + typeOfReport + " 작성이 완료 되었습니다.<BR>첨부된 파일을 참조 부탁 드립니다."
							+ "<BR><BR>"
							+ "<BR>대상 : " + targetInfoStr
							+ "<BR>시작시간 : " + startTimeStr
							+ "<BR>소요시간 : " + elapsedTimeStr
							+ "<BR>Report File : " + "<a href='" + reportFile.getAbsolutePath() + "'>" + reportFile.getName() + "</a>"
							+ "<BR><BR>감사합니다.";
	
		}else
		{
			reportFile = new File(logFileUtility.getFilePath());
			mailContenText = "요청하신 " + typeOfReport + " 작성 과정에 오류가 발생 되었습니다.<BR>System 관리자에게 문의 하십시오."
						+ "<BR><BR>"
						+ "<BR>대상 : " + targetInfoStr
						+ "<BR>시작시간 : " + startTimeStr
						+ "<BR>소요시간 : " + elapsedTimeStr
						+ "<BR>Error File : " + reportFile.getAbsolutePath()
						+ "<BR><BR>감사합니다.";
		}

		EnvDao envDao = new EnvDao();
		String plmAdmins = envDao.getTCWebEnv().get("PLM_ADMIN");
		if(userId.equalsIgnoreCase("infodba") || userId.equalsIgnoreCase("infodba0"))
		{
			String[] ids = plmAdmins.split(",");
			if(ids != null && ids.length > 0)
			{
				userId = ids[0];
			}
		}
		//메일 발송..
		DataSet ds = new DataSet();
		ds.put("the_sysid", "NPLM");
		ds.put("the_sabun", userId);
		ds.put("the_title", mailSubject);
		ds.put("the_remark", mailContenText);
		ds.put("the_tsabun", userId + "," + plmAdmins);
		//메일 발송
		SendMailEAIDao sendMailEAIdao = new SendMailEAIDao();
		try
		{
			sendMailEAIdao.sendMailEAI(ds);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
