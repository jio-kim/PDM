package com.ssangyong.soa.bop.util;

import com.ssangyong.soa.biz.Session;
import com.ssangyong.soa.service.TcLoginService;
import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidCredentialsException;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.exceptions.CanceledOperationException;

public class LoginTest {
	
	public static void main(String[] args) {

		LoginTest loginTest = new LoginTest(); 
		loginTest.login();
	}
	
	public LoginTest(){

	}
	
	public void login(){
		
		System.out.println("Ka Ka Ka...");
		
		TcLoginService tcLoginService = new TcLoginService();
		Session tcSession = null;
		try {
			tcSession = tcLoginService.getTcSession();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Connection connection = tcSession.getConnection();
		
		String[] kkk = null;
		try {
			kkk = ((Connection)connection).getCredentialManager().getCredentials(new InvalidCredentialsException());
		} catch (CanceledOperationException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < kkk.length; i++) {
			System.out.println("kkk["+i+"] = "+kkk[i]);
		}

		tcSession.logout();
		
	}
	
}
