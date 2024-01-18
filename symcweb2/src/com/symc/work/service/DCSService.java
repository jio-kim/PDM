package com.symc.work.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;

/**
 *  [DCS 현업 사용문제 신고내역] [20150701][ymjang] 메일 발송 문구 전체에 Footer 문구 추가 요청
 *  [DCS 현업 사용문제 신고내역] [20150708][ymjang] 당일 결재 요청일 로 부터 +1일 이후부터 메일 발송 
 *  [DCS 현업 사용문제 신고내역] [20150714][ymjang] 메일 제목에 문서 정보 표시
 *  [DCS 현업 사용문제 신고내역] [20150716][ymjang] 문서 정보에 결재구분 표시 추가
 *  [SR150810-045][20150917][ymjang] 당사 Work Calandar 적용 및 협의완료 문서 View 방식변경 요청
 *  [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
 *  [20170102][ymjang] 휴무일 결과셋 Nullpointer Exception 오류 수정
 *  
 */
public class DCSService {

	public DCSService() {

	}

	public void startService() throws Exception {
		try {
			
			// [SR150810-045][20150917][ymjang] 당사 Work Calandar 적용 및 협의완료 문서 View 방식변경 요청
			HashMap<String, String> parmaMap = new HashMap<String, String>();
			
	        DateFormat df = new SimpleDateFormat("yyyyMMdd"); 
	        parmaMap.put("TODAY", df.format(new Date()));
	        
			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
			HashMap<String, String> resultMap = (HashMap<String, String>) commonDao.selectOne("com.symc.dcs.getHolidayYN", parmaMap);
			
			//[20170102][ymjang] 휴무일 결과셋 Nullpointer Exception 오류 수정
			// 휴무일의 경우는메일을 발송하지 않는다. 
			if (resultMap != null && !resultMap.get("HOLY").toString().equals("Y"))
			{
				sendMail();
			}
			
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * [DCS 현업 사용문제 신고내역] [20150714][ymjang] 메일 제목에 문서 정보 표시
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void sendMail() throws Exception {
		
		List<HashMap<String, String>> toUserList = searchStandbyPerformUserInfoList();
		for (HashMap<String, String> toUserMap : toUserList) {
			
			String toUserID = toUserMap.get("USERID");
			String toUserName = toUserMap.get("USERNAME");
			String toGroupName = toUserMap.get("GROUPNAME");
			String titleItemID = toUserMap.get("ITEMID");
			String titleItemRevID = toUserMap.get("REVID");
			String titleSystemName = toUserMap.get("SYSTEM_NAME");
			String titleOwningGroup = toUserMap.get("OWNING_GROUP");
			String titleOwningUser = toUserMap.get("OWNING_USER");
			int titleRCNT = Integer.parseInt(toUserMap.get("RCNT"));

			String title = null;
			if (titleRCNT > 0)
				title = "DCS " + titleItemID + " " + titleSystemName + " " + titleOwningGroup + " 외 " + titleRCNT + "건 수신결재 요청";
			else
				title = "DCS " + titleItemID + " " + titleSystemName + " " + titleOwningGroup + " 수신결재 요청";
			
			List<HashMap<String, String>> resultList = searchStandbyPerformTaskInfoList(toUserID);
			
			String body = "<PRE>";
				   body += "안녕하세요. DCS 시스템 관리자입니다.";
				   body += "<BR><BR>" + toGroupName + " " + toUserName + " 님에게 다음과 같은 결재대기 설계구상서가 존재합니다.";
				   body += "<BR>" + "신속한 결재를 부탁드립니다.";

			for (HashMap<String, String> resultMap : resultList) {
				
				String itemId = resultMap.get("ITEMID");
				String revId = resultMap.get("REVID");
				String groupName = resultMap.get("GROUPNAME");
				String userId = resultMap.get("USERID");
				String userName = resultMap.get("USERNAME");		
				// [DCS 현업 사용문제 신고내역] [20150716][ymjang] 문서 정보에 결재구분 표시 추가
				String epmJobGb = resultMap.get("EPMJOB_GB");
				
				// [DCS 현업 사용문제 신고내역] [20150714][ymjang] 메일 제목에 문서 정보 표시
				String systemName = resultMap.get("SYSTEM_NAME");
				String owning_group = resultMap.get("OWNING_GROUP");
				String owning_user = resultMap.get("OWNING_USER");
			           
				body += "<BR><BR>" + "* [" + epmJobGb + "] " + itemId + "_" + revId + " " + systemName + " " + owning_group + " " + owning_user;
			}
			
			body += getFooter();
			body += "</PRE>";

			DataSet ds = new DataSet();
			ds.put("the_sysid", "NPLM");	
			ds.put("the_sabun", "NPLM");
			//ds.put("the_title", "DCS 수신결재 요청");
			ds.put("the_title", title);
			ds.put("the_remark", body);
			ds.put("the_tsabun", toUserID);
			
			try {
				// [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
				TcCommonDao.getTcCommonDao().update("com.symc.meco.sendMailEai", ds);
				//TcCommonDao.getTcCommonDao().update("com.symc.meco.sendMail", ds);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/*
	@SuppressWarnings("unchecked")
	public void sendMail() throws Exception {
		List<HashMap<String, String>> resultList = searchStandbyPerformTaskInfoList();
		for (HashMap<String, String> resultMap : resultList) {
			String itemId = resultMap.get("ITEMID");
			String revId = resultMap.get("REVID");
			String groupName = resultMap.get("GROUPNAME");
			String userId = resultMap.get("USERID");
			String userName = resultMap.get("USERNAME");		
			
			// [DCS 현업 사용문제 신고내역] [20150714][ymjang] 메일 제목에 문서 정보 표시
			String systemName = resultMap.get("SYSTEM_NAME");
			String owning_group = resultMap.get("OWNING_GROUP");
			String owning_user = resultMap.get("OWNING_USER");

			String title = "DCS " + itemId + " " + systemName + " " + owning_group + " 수신결재 요청";
			
			// [DCS 현업 사용문제 신고내역] [20150701][ymjang] 메일 발송 문구 전체에 Footer 문구 추가 요청
		    String footer  = "<BR>" + "--------------";
		   	   	   footer += "<BR>" + "**Teamcenter DCS를 처음 이용하시는 분은 ";
		           footer += "<BR>" + "  1.비전넷→전산지원→IT지식센터→FAQ 92번 게시글 DCS 매뉴얼 참조 ";
		           footer += "<BR>" + "<a href='http://150.1.154.4/jsp/common/docuForward01.jsp?userid=001721&url=/base/owa/odocu_view&no=82902563'> " 
				                + "  2.기술관리(2015-116) Teamcenter DCS 재개발 변경사항 공지』 업무연락 참조</a>";
		           
			String body = "<PRE>";
			body += "안녕하세요. DCS 시스템 관리자입니다.";
			body += "<BR><BR>" + groupName + " " + userName + "님은 다음과 같은 설계구상서 협의 검토 대기자로 지정되셨습니다.";
			body += "<BR>" + "신속히 검토 결과 입력 부탁드립니다.";
			body += "<BR><BR>" + "* " + itemId + "_" + revId + " " + systemName + " " + owning_group + " " + owning_user;
			body += footer;
			body += "</PRE>";

			DataSet dataSet = new DataSet();
			dataSet.put("the_sysid", "NPLM");	
			dataSet.put("the_sabun", "NPLM");
			//dataSet.put("the_title", "DCS 수신결재 요청");
			dataSet.put("the_title", title);
			dataSet.put("the_remark", body);
			dataSet.put("the_tsabun", userId);

			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
			commonDao.update("com.symc.meco.sendMail", dataSet);
		}
	}
	*/
	
	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> searchStandbyPerformUserInfoList() throws Exception {
		List<HashMap<String, String>> resultList = null;

		HashMap<String, String> parmaMap = new HashMap<String, String>();
		
		// [DCS 현업 사용문제 신고내역] [20150708][ymjang] 당일 결재 요청일 로 부터 +1일 이후부터 메일 발송 
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, -1);
		
        DateFormat df = new SimpleDateFormat("yyyyMMdd"); 
        String yesterday = df.format(today.getTime());
        parmaMap.put("TODAY", yesterday);
        
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		resultList = (List<HashMap<String, String>>) commonDao.selectList("com.symc.dcs.searchStandbyPerformUserInfoList", parmaMap);

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> searchStandbyPerformTaskInfoList(String userID) throws Exception {
		List<HashMap<String, String>> resultList = null;

		HashMap<String, String> parmaMap = new HashMap<String, String>();
		
		// [DCS 현업 사용문제 신고내역] [20150708][ymjang] 당일 결재 요청일 로 부터 +1일 이후부터 메일 발송 
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, -1);
		
        DateFormat df = new SimpleDateFormat("yyyyMMdd"); 
        String yesterday = df.format(today.getTime());
        parmaMap.put("TODAY", yesterday);
        parmaMap.put("PUSER_ID", userID);
        
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		resultList = (List<HashMap<String, String>>) commonDao.selectList("com.symc.dcs.searchStandbyPerformTaskInfoList", parmaMap);

		return resultList;
	}

	/**
	 * [DCS 현업 사용문제 신고내역] [20150701][ymjang] 메일 발송 문구 전체에 Footer 문구 추가 요청
	 * @return
	 */
	public String getFooter() {
		
	    String footer  = "<BR>" + "--------------";
	   	   	   footer += "<BR>" + "**Teamcenter DCS를 처음 이용하시는 분은 ";
	           footer += "<BR>" + "  1.비전넷→전산지원→IT지식센터→FAQ 92번 게시글 DCS 매뉴얼 참조 ";
	           footer += "<BR>" + "<a href='http://150.1.154.4/jsp/common/docuForward01.jsp?userid=001721&url=/base/owa/odocu_view&no=82902563'> " 
			                + " 2.기술관리(2015-116) Teamcenter DCS 재개발 변경사항 공지』 업무연락 참조</a>";
	
	   return footer;
	}
	
}
