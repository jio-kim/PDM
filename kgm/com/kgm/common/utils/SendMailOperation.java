package com.kgm.common.utils;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMailOperation {
	private String fromAddress;
	private String[] toAddresses;
	private String subject;
	private String message;
	private File[] files;

	public SendMailOperation(String title, String message, String fromAddress, List<String> toAddresses, File logFile) throws Exception {
		this.subject = title;
		this.message = message;
		this.fromAddress = fromAddress;
		this.toAddresses = toAddresses.toArray(new String[] {});
		if (logFile != null && logFile.exists()) {
			this.files = new File[] { logFile };
		}
		execute();
	}

	public void execute() throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", "10.224.130.69");
		Session session = Session.getInstance(props);
		Multipart mp = new MimeMultipart();
		// create a message
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(fromAddress));
		// 받는사람
		InternetAddress[] toAddress = new InternetAddress[toAddresses.length];
		for (int i = 0; i < toAddress.length; i++) {
			toAddress[i] = new InternetAddress(toAddresses[i]);
		}
		msg.setRecipients(MimeMessage.RecipientType.TO, toAddress);
		// 제목
		msg.setSubject(subject, "euc-kr");
		// 내용
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setContent(message, "text/html; charset=euc-kr");
		mp.addBodyPart(mbp1);

		// 파일첨부
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				MimeBodyPart mbp2 = new MimeBodyPart();
				FileDataSource fds = new FileDataSource(files[i]);
				mbp2.setDataHandler(new DataHandler(fds));
				mbp2.setFileName(convertToISO8859(fds.getName()));
				mp.addBodyPart(mbp2);
			}
		}

		// 메시지 add
		msg.setContent(mp);
		// header 에 날짜 삽입
		msg.setSentDate(new Date());
		// send the message
		Transport.send(msg);
	}

	private String convertToISO8859(String strStr) throws java.io.UnsupportedEncodingException {
		if (strStr == null) {
			return null;
		} else {
			return new String(strStr.getBytes("KSC5601"), "8859_1");
		}
	}

	public static void main(String[] args) {
		try {
			List<String> to = new ArrayList<String>();
			to.add("jongchan.han@doosan.com");
			new SendMailOperation("테스트", "테스트 메시지", "jongchan.han@doosan.com", to, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
