package com.kgm.soa.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SecurityUtil;
import com.kgm.soa.biz.Session;
import com.kgm.soa.bop.reports.EndItemMasterListReport;
import com.kgm.soa.bop.reports.EquipmentListReport;
import com.kgm.soa.bop.reports.OperationMasterListReport;
import com.kgm.soa.bop.reports.SubsidiaryMaterialListReport;
import com.kgm.soa.bop.reports.ToolListReport;
import com.kgm.soa.bop.util.LogFileUtility;
import com.teamcenter.soa.client.Connection;

public class BOPReportService extends Thread {

	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
	private DateFormat fileNameDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private LogFileUtility logFileUtility;
	private Date startDate;
	
	private Session tcSession;
	private TcLoginService tcLoginService;
	private Connection connection;
	private String requestUserId = null;

	private DataSet dataSet;
	private String validationMessage;
	private String typeOfReport;
	
	private boolean isTestRunMode = false;
	
	public void setTestRunnMode( boolean testRun){
		isTestRunMode = testRun;
	}

	public Object makeReport(DataSet ds) throws Exception {
		
		startDate = new Date();
		String timeStr = fileNameDf.format(startDate);
		this.logFileUtility = new LogFileUtility("BOPReportBuilder["+timeStr+"].txt");
		logFileUtility.setTimmerStarat();
		
		System.out.println("this.logFileUtility.getFilePath() = "+this.logFileUtility.getFilePath());

		this.dataSet = ds;

		String returnMessage = null;

		boolean isValid = validateParametersAndLogin();
		if (isValid == true) {
			
			if(isTestRunMode==false){
				this.start();
			}else{
				this.run();
			}

			returnMessage = "Succeed\n"
					+"The program has been successfully driven.\n"
					+ "Once the report is complete, it will be sent to the e-mail.";
			
			String tempMessage = 
					"\n======================================================\n"
					+ " Validation Message\n"+
					"======================================================";
			this.logFileUtility.writeBlankeRowReport(1);
			this.logFileUtility.writeReport(tempMessage+
					"\n"+returnMessage+
					"\n======================================================");
		} else {
			
			returnMessage = "Failure\n"
					+"There is an error in the request information.\n"
					+ "Please refer to the following message.\n\n" + validationMessage;
			
			String tempMessage = 
					"\n======================================================\n"
					+ " Validation Message\n"+
					"======================================================";
			this.logFileUtility.writeBlankeRowReport(1);
			this.logFileUtility.writeReport(tempMessage+
					"\n"+returnMessage+
					"\n======================================================");
			
			if(this.connection!=null){
				tcSession.logout();
				this.logFileUtility.writeBlankeRowReport(1);
				this.logFileUtility.writeReport("Sign out");
			}
			
			this.logFileUtility.writeBlankeRowReport(1);
			this.logFileUtility.writeReport("Start : " + df.format(startDate));
			this.logFileUtility.writeReport("End : " + df.format(new Date()));
			
			this.logFileUtility.writeBlankeRowReport(1);
			this.logFileUtility.writeReport("End of report program");
		}

		return returnMessage;
	}

	public boolean validateParametersAndLogin() {
		boolean valid = true;

		String encUserId = null;
		String encPassword = null;
		if(this.dataSet.get("userId")!=null){
			encUserId = this.dataSet.get("userId").toString();
		}
		if(this.dataSet.get("password")!=null){
			encPassword = this.dataSet.get("password").toString();
		}
		
		if(this.dataSet.get("typeOfReport")!=null){
			this.typeOfReport = this.dataSet.get("typeOfReport").toString();
		}
		
		if(encUserId!=null && encUserId.trim().length()>0){
			try {
				requestUserId = SecurityUtil.decrypt(encUserId);
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}
		String userPassword = null;
		if(encPassword!=null && encPassword.trim().length()>0){
			try {
				userPassword = SecurityUtil.decrypt(encPassword);
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}

		this.logFileUtility.writeBlankeRowReport(1);
		String tempMessage = 
				"\n==================\n"
				+ " Parameter \n"+
				"==================";
		this.logFileUtility.writeBlankeRowReport(1);
		this.logFileUtility.writeReport(tempMessage);
		this.logFileUtility.writeReport("userId = " + requestUserId);
		this.logFileUtility.writeReport("userPassword(encryption) = " + encPassword);
		this.logFileUtility.writeReport("typeOfReport = " + this.typeOfReport);
		this.logFileUtility.writeBlankeRowReport(1);
		
		// Login 을 통한 Login 정보 확인
		if(requestUserId!=null && userPassword!=null){
			tcLoginService = new TcLoginService();
			Exception exception = null;
			try {
				this.tcSession = tcLoginService.getTcSession(requestUserId, userPassword, "");
				if(tcSession!=null){
					this.connection = this.tcSession.getConnection();
				}
			} catch (Exception e) {
				exception = e;
				this.logFileUtility.writeBlankeRowReport(1);
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			if(exception!=null){
				String exceptionMessage = exception.getMessage();
				if(exceptionMessage!=null){
					exceptionMessage = "Unable to login.\n"+exceptionMessage;
				}else{
					exceptionMessage = "Unable to login.";
				}
				updateValidationMessage(exceptionMessage);
				valid = false;
			}
		}else{
			updateValidationMessage("Unable to find login information.");
			valid = false;
		}
		
		//-------------------------------------------------------------
		// Report Type에 따른 입력  Paramenter 확인
		//-------------------------------------------------------------

		// Common Attribute 정의
		String ccObjectName = null;
		if(this.dataSet.get("ccObjectName")!=null){
			ccObjectName = this.dataSet.get("ccObjectName").toString();
		}
		
		if(ccObjectName==null || (ccObjectName!=null && ccObjectName.trim().length()<1)){
			updateValidationMessage("CC Name not found");
			valid = false;
		}
		
		// End Item Master List
		if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("End Item Master List Report")==true){

		// Subsidiary Material List
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Subsidiary Material List Report")==true){
			
		// Subsidiary Material List
		// [SR180212-044] BOP Paint 부자재 Report 추가 하위 매뉴 개발 의뢰
		// 기존에는 편성 버전이 "00" 인 공법만 추출 했으나 모든 공법에 대한 부자재 추출	
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Subsidiary Material All List Report")==true){
			
		// Operation Master List
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Operation Master List Report")==true){
			
		// 이종화 차장 요청 
		// Tool List SOA 추가
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Tool Master List Report")==true){
			
		// Equipment SOA 추가	
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Equipment Master List Report")==true){
			
		}else{
			String typeNotFountMessage = "Type of report that is not registered.";
			updateValidationMessage(typeNotFountMessage);
			valid = false;
		}

		return valid;
	}
	
	private void updateValidationMessage(String message){
		if(this.validationMessage==null){
			this.validationMessage = message;
		}else{
			this.validationMessage = this.validationMessage +"\n"+message;
		}
	}

	@Override
	public void run() {
		
		this.logFileUtility.writeBlankeRowReport(1);
		this.logFileUtility.writeReport("Starting a report program");

		// Common
		String ccObjectName = this.dataSet.get("ccObjectName").toString();
		
		this.logFileUtility.writeBlankeRowReport(1);
		this.logFileUtility.writeReport("Report Type : End Item Master Report");
		this.logFileUtility.writeReport("Target CC Name : "+ccObjectName);

		// End Item Master List
		if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("End Item Master List Report")==true){
			EndItemMasterListReport endItemMasterListReport = new EndItemMasterListReport(this.connection, ccObjectName);
			endItemMasterListReport.makeReport();
			endItemMasterListReport.sendResultMail(typeOfReport, requestUserId);
			
		// Subsidiary Material List
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Subsidiary Material List Report")==true){
			// 구현해야 됨. (2016.11.01)
			SubsidiaryMaterialListReport subsidiaryMaterialListReport = new SubsidiaryMaterialListReport(this.connection, ccObjectName);
			subsidiaryMaterialListReport.makeReport();
			subsidiaryMaterialListReport.sendResultMail(typeOfReport, requestUserId);
		
		// Paint 에서만 사용 
	    // 편성버전이 "00" 이 아니라도 부자재 리스트를 뽑아내기 위해 조건문 추가
		// Subsidiary Material All List	
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Subsidiary Material All List Report")==true){
			// 구현해야 됨. (2016.11.01)
			SubsidiaryMaterialListReport subsidiaryMaterialListReport = new SubsidiaryMaterialListReport(this.connection, ccObjectName, true);
			subsidiaryMaterialListReport.makeReport();
			subsidiaryMaterialListReport.sendResultMail(typeOfReport, requestUserId);
			
		// Operation Master List
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Operation Master List Report")==true){
			// 구현해야 됨. (2016.11.01)
			OperationMasterListReport operationMasterListReport = new OperationMasterListReport(this.connection, ccObjectName);
			/////////////////////////////////////////////////////////////////////////////////////
			/*
			 * 수정점 : 20200330
			 * 수정내용 : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
			 * 			  "작업정보" 추가를 위해  로컬 메뉴에서 SoaWeb 서버로  selectedValue  파라미터 전달
			 * 			   
			 */
			if( this.dataSet.get("selectedValue") != null ) {
				operationMasterListReport.setSelectedValue(this.dataSet.get("selectedValue").toString());
			}
			/////////////////////////////////////////////////////////////////////////////////////
			operationMasterListReport.makeReport();
			operationMasterListReport.sendResultMail(typeOfReport, requestUserId);
			
			
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Tool Master List Report")==true){
			// 구현해야 됨. (2016.11.01)
			ToolListReport toolMaterialListReport = new ToolListReport(this.connection, ccObjectName);
			toolMaterialListReport.makeReport();
			toolMaterialListReport.sendResultMail(typeOfReport, requestUserId);
			
			
		}else if(this.typeOfReport!=null && this.typeOfReport.trim().equalsIgnoreCase("Equipment Master List Report")==true){
			// 구현해야 됨. (2016.11.01)
			EquipmentListReport equipmentMaterialListReport = new EquipmentListReport(this.connection, ccObjectName);
			equipmentMaterialListReport.makeReport();
			equipmentMaterialListReport.sendResultMail(typeOfReport, requestUserId);
		}

		if(this.connection!=null){
			tcSession.logout(this.connection);
			this.logFileUtility.writeBlankeRowReport(1);
			this.logFileUtility.writeReport("Sign out");
		}
		
		this.logFileUtility.writeBlankeRowReport(1);
		this.logFileUtility.writeReport("Start : " + df.format(startDate));
		this.logFileUtility.writeReport("End : " + df.format(new Date()));
		
		this.logFileUtility.writeBlankeRowReport(1);
		this.logFileUtility.writeReport("End of report program");
	}

}
