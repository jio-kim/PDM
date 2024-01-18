package com.ssangyong.soa.bop.reports;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ssangyong.soa.biz.Session;
import com.ssangyong.soa.bop.util.LogFileUtility;
import com.ssangyong.soa.service.TcLoginService;
import com.teamcenter.soa.client.Connection;

public class BOPReportBuilder  {
	
	LogFileUtility logFileUtility;
	Session tcSession;
	Connection connection;

	public BOPReportBuilder(){
		DateFormat fileNameDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStr = fileNameDf.format(new Date());
		this.logFileUtility = new LogFileUtility("BOPReportBuilder["+timeStr+"].txt");
	}
	
	protected void logIn(){
		TcLoginService tcLoginService = new TcLoginService();
		try {
			tcSession = tcLoginService.getTcSession();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	protected void logOut(){
		if(this.tcSession!=null){
			this.tcSession.logout();
		}
	}
	
}
