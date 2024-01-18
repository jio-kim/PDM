package com.symc.work.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;

public class EcoRptMailSendService {

	public EcoRptMailSendService() {
	}

	@SuppressWarnings("unchecked")
	public Object startService() throws Exception {

		StringBuffer log = new StringBuffer();
		try {
			
	        DateFormat df = new SimpleDateFormat("yyyyMMdd"); 
			HashMap<String, String> parmaMap = new HashMap<String, String>();
	        parmaMap.put("TODAY", df.format(new Date()));
	        
			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
			HashMap<String, String> resultMap = (HashMap<String, String>) commonDao.selectOne("com.symc.dcs.getHolidayYN", parmaMap);
			
			// 휴무일의 경우는메일을 발송하지 않는다. 
			if (!resultMap.get("HOLY").toString().equals("Y"))
			{
				sendMail(log);
			}
			
		} catch (Exception e) {
			throw e;
		}
		
		return log.toString();
	}

	@SuppressWarnings("unchecked")
	public void sendMail(StringBuffer log) throws Exception {
		
		List<HashMap<String, String>> toUserList = getToUserList();
		
		if (toUserList == null || toUserList.size() <= 0)
			return;
		
		DataSet ds = null;
		boolean isFirst = false;
		for (HashMap<String, String> toUserMap : toUserList) {
			
			String toUserId = toUserMap.get("RECEIVER_ID");
			String senderId = toUserMap.get("SENDER_ID");
			String senderTeam = toUserMap.get("SENDER_TEAM");
			String senderHname = toUserMap.get("SENDER_HNAME");
			
			isFirst = true;
			String title = null;
			String body = null;
			List<HashMap<String, String>> stdInfoList = getStdInfoList(toUserId, senderId);
			for (HashMap<String, String> stdInfoMap : stdInfoList) {
				
				String masterPuid = stdInfoMap.get("MASTER_PUID");
				String projectNo = (stdInfoMap.get("PROJECT_NO") == null ? "" : stdInfoMap.get("PROJECT_NO"));
				String ospecId = (stdInfoMap.get("OSPEC_ID") == null ? "" : stdInfoMap.get("OSPEC_ID"));
				String chageDesc = (stdInfoMap.get("CHANGE_DESC") == null ? "" : stdInfoMap.get("CHANGE_DESC"));
				String description = (stdInfoMap.get("DESCRIPTION") == null ? "" : stdInfoMap.get("DESCRIPTION"));
				String ecoCompleteReqDate = (stdInfoMap.get("ECO_COMPLETE_REQ_DATE") == null ? "" : stdInfoMap.get("ECO_COMPLETE_REQ_DATE"));

				if (isFirst) {
					title = projectNo + " (" + ospecId + ") " + chageDesc + " " + " 관련하여 다음과 같이 검토를 의뢰하오니 회신부탁드립니다.";
					body = "<PRE>";
					body += "안녕하세요?. " + senderTeam + " " + senderHname + " 입니다.";
					body += "<BR><BR>" + projectNo + " (" + ospecId + ") " + chageDesc + " " + " 관련하여 다음과 같이 검토를 의뢰하오니 회신부탁드립니다.";
					isFirst = false;
				}
				
			    body += "<BR><BR>-------------------------------------------------------------------------------------------";
			    body += "<BR><BR>";
			    body += "◆ 기본정보";
			    body += "<BR>";
			    body += "Project       : " + projectNo + "<BR>";
			    body += "O/Spec        : " + ospecId + "<BR>";
			    body += "변경세부내용  : " + description + "<BR>";
			    body += "ECO 요청일자  : " + ecoCompleteReqDate + "<BR>";
			    body += "<BR><BR>";
			    
			    body += "◆ 상세";
				List<HashMap<String, String>> ecoRptList = getEcoRptList(masterPuid);
				for (HashMap<String, String> ecoRptMap : ecoRptList) {
					
					String functionId = (ecoRptMap.get("FUNCTION_ID") == null ? "" : ecoRptMap.get("FUNCTION_ID"));
					String partName = (ecoRptMap.get("PART_NAME") == null ? "" : ecoRptMap.get("PART_NAME"));
					String note = (ecoRptMap.get("DESCRIPTION") == null ? "" : ecoRptMap.get("DESCRIPTION"));
					
				    body += "<BR><BR>";
				    body += "Function      : " + functionId + "<BR>";
				    body += "Part Name     : " + partName + "<BR>";
				    body += "비고          : " + note;
				}
			}
			
		    body += "<BR><BR>";
			body += "<BR>" + "감사합니다.";
			body += "</PRE>";
			
			ds = new DataSet();
			ds.put("the_sysid", "NPLM");	
			ds.put("the_sabun", senderId);
			//ds.put("the_sabun", "148757");
			ds.put("the_title", title);
			ds.put("the_remark", body);
			ds.put("the_tsabun", toUserId);
			//ds.put("the_tsabun", "148757");

			try {
				// [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
				TcCommonDao.getTcCommonDao().update("com.symc.meco.sendMailEai", ds);
				//TcCommonDao.getTcCommonDao().update("com.symc.meco.sendMail", ds);
				
				// 메일전송상태 저장
				updateMailSendDate(toUserId, senderId);
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> getToUserList() throws Exception {

		// 완료요청일 1일 전에 메일 발송함. 
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, 1);
		
        DateFormat df = new SimpleDateFormat("yyyyMMdd"); 
        String tomorrow = df.format(today.getTime());
        //tomorrow = "20160824";
		HashMap<String, String> parmaMap = new HashMap<String, String>();
        parmaMap.put("ECO_COMPLETE_REQ_DATE", tomorrow);
        
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap<String, String>> resultList = (List<HashMap<String, String>>) commonDao.selectList("com.symc.ecorpt.getToUserList", parmaMap);

		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> getStdInfoList(String toUserId, String senderId) throws Exception {

		// 완료요청일 1일 전에 메일 발송함. 
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, 1);
		
        DateFormat df = new SimpleDateFormat("yyyyMMdd"); 
        String tomorrow = df.format(today.getTime());
        //tomorrow = "20160824";
		HashMap<String, String> parmaMap = new HashMap<String, String>();
        parmaMap.put("ECO_COMPLETE_REQ_DATE", tomorrow);
		parmaMap.put("TO_USER", toUserId);
        parmaMap.put("REGISTER_ID", senderId);
        
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap<String, String>> resultList = (List<HashMap<String, String>>) commonDao.selectList("com.symc.ecorpt.getStdInfoList", parmaMap);

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> getEcoRptList(String masterPuid) throws Exception {

		HashMap<String, String> parmaMap = new HashMap<String, String>();
		parmaMap.put("MASTER_PUID", masterPuid);
        
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap<String, String>> resultList = (List<HashMap<String, String>>) commonDao.selectList("com.symc.ecorpt.getEcoRptList", parmaMap);

		return resultList;
	}

	public void updateMailSendDate(String toUserId, String senderId) throws Exception {

		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, 1);
		
        DateFormat df = new SimpleDateFormat("yyyyMMdd"); 
        String tomorrow = df.format(today.getTime());
        //tomorrow = "20160824";
		HashMap<String, String> parmaMap = new HashMap<String, String>();
        parmaMap.put("ECO_COMPLETE_REQ_DATE", tomorrow);
		parmaMap.put("TO_USER", toUserId);
        parmaMap.put("REGISTER_ID", senderId);
        
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		commonDao.update("com.symc.ecorpt.updateMailSendDate", parmaMap);
	}
	
	// 고정길이 문자열 구하기 (문자열 뒤에 Space 채우기)
	public String getFixedLengthString(String val, int len) {
		try 
		{
		   String lsTemp = "";
		   byte [] tmpString = val.getBytes();
		     
		   if(len <= tmpString.length) 
			   return val;
		      
		   for(int i = len - tmpString.length; i > 0 ; i--) {
			   lsTemp += " ";
		   }
		   return val + lsTemp;
		}
	   catch(Exception e) {
		   return val;
	   }
	}
}
