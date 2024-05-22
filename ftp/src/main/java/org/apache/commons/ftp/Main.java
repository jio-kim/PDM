package org.apache.commons.ftp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main 
{	
	public static void main(String[] args) {
		String hostIP = "10.50.50.176";
		int hostPort = 21;
		String clientID = "user";
		String ClientPW = "user";
		String remoteVersion = "/version.txt";
		String localVersion = "./version.txt";
		String tempFilePath = "./tempVersion.txt";
	    String batchFilePath;
		try {
			// FTP 연결
			FTPClientUtil ftpClientUtil = new FTPClientUtil(hostIP, hostPort, clientID, ClientPW);
			// 로컬 version.txt의 BOP 여부
			boolean isBOP = ftpClientUtil.isBOP(localVersion);
			if(isBOP) {
				remoteVersion = "/BOP/version.txt";
			} else remoteVersion = "/nonBOP/version.txt";
			
			// 서버와 로컬의 Version 확인
			ftpClientUtil.checkVersion(remoteVersion, localVersion, tempFilePath);
			
			// 다운로드할 파일 리스트 구성
			ArrayList downloadList = ftpClientUtil.downloadList(localVersion, tempFilePath);
			System.out.println(downloadList);
			
			boolean isUpdate = ftpClientUtil.downloadFile(downloadList, isBOP);
			// 다운로드 진행 후 로컬 Version 업데이트 및  Temp 파일 삭제 
			ftpClientUtil.updateVersion(localVersion, tempFilePath);
			ftpClientUtil.deleteTempFile(tempFilePath);
			
			// FTP 연결 종료
			ftpClientUtil.disconnect();
			
			// Batch
			String batchFileName = isUpdate ? "dwAndStart.bat" : "start.bat";
		       File batchFile = new File(batchFileName);

		       if (!batchFile.exists()) {
		           System.out.println("해당 경로에 " + batchFileName + " 파일이 없습니다.");
		           try {
		               Thread.sleep(5000); // 5초간 일시 중지
		           } catch (InterruptedException e) {
		               e.printStackTrace();
		           }
		       } else {
		           batchFilePath = batchFile.getAbsolutePath();
		           try {
		               ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/c", batchFilePath);
		               builder.directory(batchFile.getParentFile());
		               Process process = builder.start();
		               process.waitFor();
		           } catch (IOException | InterruptedException e) {
		               e.printStackTrace();
		           }
		       }
			
			
			// 서버 Version 파일 다운로드
//			boolean downloadResult = ftpClientUtil.downloadFile("/appdata/files/uploadTest.png", "D:/Test/downloadTest.png");
//			System.out.println("Download result: " + downloadResult); // Download result: true
//			
//	        // 파일 업로드 테스트
//	        boolean uploadResult = ftpClientUtil.uploadFile("D:/Test/aiden_icon.png", "/appdata/files/uploadTest.png");
//	        System.out.println("Upload result: " + uploadResult); // Upload result: true
//	
//	        // 파일 다운로드 테스트
//	        boolean downloadResult = ftpClientUtil.downloadFile("/appdata/files/uploadTest.png", "D:/Test/downloadTest.png");
//	        System.out.println("Download result: " + downloadResult); // Download result: true
	
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}