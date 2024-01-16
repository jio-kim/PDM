package com.ssangyong.common.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.oroinc.net.ftp.FTPClient;
import com.ssangyong.common.SYMCClass;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

public class FTPConnection {
	private FTPClient client;
	private String host = "150.1.154.4";
	private int port = 21;
	private String id = "s078897";
	private String password = "s078897";
	private String serverLoc = "/Cubic/pis/2013";

	/**
	 * 인트라넷(Vision-NET)에 ECI 문서 파일을 로딩하는 데 사용 함.
	 * 옵션에 저장된 FTP 접속 정보를 가져옴 
	 * @throws Exception
	 */
	public FTPConnection() throws Exception {
		String[] ftpInfos = FTP_HOST_INFO();

		if(ftpInfos.length > 0){
			this.host = ftpInfos[0];
			this.port = Integer.parseInt(ftpInfos[1]);
			this.id = ftpInfos[2];
			this.password = ftpInfos[3];
			//폴더는 년도 별로 관리 함.
			SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
			String year = simpleDateformat.format(new Date());
			this.serverLoc = ftpInfos[4]+year;

		}
		connect();
	}
	
	/**
	 * 파라미터를 입력 받아 ftp connection 생성
	 */
	public FTPConnection(String host, int port, String id, String password, String serverLoc) throws Exception {
		this.host = host;
		this.port = port;
		this.id = id;
		this.password = password;
		this.serverLoc = serverLoc;
		connect();
	}
	
	/**
	 * Active mode로 연결
	 * @throws Exception
	 */
	private void connect() throws Exception {
		client = new FTPClient();
		client.connect(host, port);
		client.login(id, password);
		client.changeWorkingDirectory(serverLoc);
		client.setFileType(FTPClient.BINARY_FILE_TYPE);
		// To use Active mode : passive mode is default : Fixed : 20130506, DJKIM,
		// 원래는 돼었는데 갑자기 안돼 물어 보니 패시브 모드 해제 하라고... BY 김동훈 GJ
		client.enterLocalActiveMode();
	}
	
	public void disconnect() throws Exception {
		if(client.isConnected()){
			client.logout();
			client.disconnect();
		}
	}
	
	/**
	 * 다운로드
	 * @param file
	 * @param ftpPath
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public boolean download(File file, String ftpPath, String fileName) throws Exception{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		String ftpFile = "";
		if(ftpPath == null)
			ftpFile = serverLoc;
		else
			ftpFile = ftpPath;
		
		ftpFile = ftpFile + "/" + fileName;
		boolean isSuccess = client.retrieveFile(ftpFile, bos);
		if(bos != null) bos.close();
		return isSuccess;
	}

	/**
	 * 업로드
	 * @param name
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public boolean upload(String name, File file) throws Exception {
		boolean isSuccess = client.storeFile(name, new FileInputStream(file));//덮어쓰기
//		boolean isSuccess = client.storeUniqueFile(name, new FileInputStream(file));//
		return isSuccess;
	}
	
	public boolean delete(String ftpPath, String fileName) throws Exception {
		String ftpFile = "";
		if(ftpPath == null)
			ftpFile = serverLoc;
		else
			ftpFile = ftpPath;
		
		ftpFile = ftpFile + "/" + fileName;
		boolean isSuccess = client.deleteFile(ftpFile);
		return isSuccess;
	}
	
	/**
	 * 파일명 변경
	 * @param oldName
	 * @param newName
	 * @return
	 * @throws Exception
	 */
	public boolean rename(String oldName, String newName) throws Exception {
		boolean isSuccess = client.rename(serverLoc+"/"+oldName, serverLoc+"/"+newName);
		return isSuccess;
	}
	
	public FTPClient getClient(){
		return this.client;
	}
	
	/** 
	 * TC Preference에서 FTP Connection 정보를 가져 옴
	 * [0=URL,1=PORT,2=USER,3=PASS,4=ROOT] 구분자는 콤마(,)
	 * @return
	 */
	public static final String[] FTP_HOST_INFO(){
		TCSession session = CustomUtil.getTCSession();
		TCPreferenceService preferenceService = session.getPreferenceService();

		//String ftpInfos = preferenceService.getString(TCPreferenceService.TC_preference_all, SYMCClass.SYMC_FTP_INFO);
		String ftpInfos = preferenceService.getStringValue(SYMCClass.SYMC_FTP_INFO);
		return ftpInfos.split(",");
	}
}