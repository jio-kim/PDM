package com.ssangyong.common.utils;

import javax.mail.MessagingException;

public class TEST {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		GmailSendMailImpl send = new GmailSendMailImpl();
//		send.setUsername("djkim@tncp.co.kr");
//		send.setPassword("dj1901");
		try {
			send.sendMessage("djkim@tncp.co.kr", new String[]{"dejoong.kim@gmail.com"}, "test", "contents");
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

}
