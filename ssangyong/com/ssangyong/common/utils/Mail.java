package com.ssangyong.common.utils;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {
	private Authenticator authenticator;
	private Properties properties;

	public Mail() {
		setAuthenticator("dejoong.kim", "djss1109");
		properties=new Properties();
		setProperties();
	}
	
	public Mail(String id, String password) {
		setAuthenticator(id, password);
		properties=new Properties();
		setProperties();
	}
		
	private void setAuthenticator(final String id, final String password) {
		authenticator = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(id, password);
			}
		};
	}
 
	private void setProperties() {
		properties.clear();
//		String[][] serverInfo = PreferenceService.getValueAndDisplayValues("WIQ2MailServerInfo");
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	}

	public void sendEmail(String from, String to, String cc, String subject, String content) {
		Session mailSession = Session.getDefaultInstance(properties, authenticator);
		Message message = new MimeMessage(mailSession);
		try {
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			if (cc != null && !cc.trim().equals("")) {
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
			}
			message.setSubject(subject); 
			message.setContent(content, "text/html; charset=EUC-KR"); 
			message.setSentDate(new Date());
			Transport.send(message); 
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
