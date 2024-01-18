package com.ssangyong.soa.bop.intest;

import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import com.ssangyong.CommonConstants;
import com.ssangyong.common.remote.SecurityUtil;
import com.ssangyong.soa.bop.reports.ExcelWorkBookWriter;

public class JTests {

	public static void main(String[] args) {
		JTests jTests = new JTests();
		jTests.test();
	}
	
	public JTests(){
		
	}
	
	public void test(){
		
		String encodedPassword = new String(Base64.encodeBase64("if_system".getBytes()));
		System.out.println("encodedPassword = "+encodedPassword);
		
		String password = new String(Base64.decodeBase64("aWZfc3lzdGVt".getBytes()));
		System.err.println("password = "+password);
		
		String ID = "a";
		String enId = null;
		try {
			enId = SecurityUtil.encrypt(ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String deId = null;
		try {
			deId = SecurityUtil.decrypt(enId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("ID = "+ID);
		System.out.println("enId = "+enId);
		System.out.println("deId = "+deId);
		
	}
	
	   public void test1(){
		
		String reportFilePath = CommonConstants.REPORT_FILE_PATH+"/A.xlsx";
		ExcelWorkBookWriter excelWorkBookWriter = new ExcelWorkBookWriter(reportFilePath);
		
		int templateDataStartRowIndex = 5;	// Index No 0 부터 시작됨.
		int templateDataStartColumnIndex = 0;
		int templateReadyLastRowIndex = templateDataStartRowIndex+3;
		
		try {
			excelWorkBookWriter.readyFile(templateDataStartRowIndex, templateDataStartColumnIndex, templateReadyLastRowIndex);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		Object[] rowData = new Object[30];
 		for (int i = 0; i < 30; i++) {
 			String a = "Col"+i;
 			rowData[i] = a;
		}
		
 		for (int i = 0; i < 30; i++) {
 			excelWorkBookWriter.writeRow("Sheet1", i, rowData);
		}
		
		
    	// Window를 닫는다.
    	try {
			excelWorkBookWriter.closeWorkBook();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void dateConditionTest(){
    	Date revisionLastModifyDate = new Date(2016, 06, 21, 8, 56);
    	Date instructionImageLastModifyDate = new Date(2016, 06, 21, 8, 56);
    	Date bomViewLastModifyDate = new Date(2016, 06, 20, 8, 56);
    	Date lastEPLLoadDate = new Date(2016, 06, 20, 14, 16);
        Date instructionPublishDate = new Date(2016, 06, 21, 8, 56);
        
        if(lastEPLLoadDate.after(revisionLastModifyDate) &&
        		lastEPLLoadDate.after(instructionImageLastModifyDate) &&
        		lastEPLLoadDate.after(bomViewLastModifyDate) ){
        	if(instructionPublishDate.after(lastEPLLoadDate)){
        		// OK
        		System.out.println("OK");
        	}else{
        		// Publisth를 다시 해야 합니다.
        		System.out.println("Publisth를 다시 해야 합니다.");
        	}
        }else{
        	// EPL Load 해야 합니다.
        	System.out.println("EPL Load 해야 합니다.");
        }
	}

}
