package com.kgm.common.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.kgm.common.SYMCClass;
import com.oroinc.net.ftp.FTPClient;
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
	 * ��Ʈ���(Vision-NET)�� ECI ���� ������ �ε��ϴ� �� ��� ��.
	 * �ɼǿ� ����� FTP ���� ������ ������ 
	 * @throws Exception
	 */
	public FTPConnection() throws Exception {
		String[] ftpInfos = FTP_HOST_INFO();

		if(ftpInfos.length > 0){
			this.host = ftpInfos[0];
			this.port = Integer.parseInt(ftpInfos[1]);
			this.id = ftpInfos[2];
			this.password = ftpInfos[3];
			//������ �⵵ ���� ���� ��.
			SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
			String year = simpleDateformat.format(new Date());
			this.serverLoc = ftpInfos[4]+year;

		}
		connect();
	}
	
	/**
	 * �Ķ���͸� �Է� �޾� ftp connection ����
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
	 * Active mode�� ����
	 * @throws Exception
	 */
	private void connect() throws Exception {
		client = new FTPClient();
		client.connect(host, port);
		client.login(id, password);
		client.changeWorkingDirectory(serverLoc);
		client.setFileType(FTPClient.BINARY_FILE_TYPE);
		// To use Active mode : passive mode is default : Fixed : 20130506, DJKIM,
		// ������ �ž��µ� ���ڱ� �ȵ� ���� ���� �нú� ��� ���� �϶��... BY �赿�� GJ
		client.enterLocalActiveMode();
	}
	
	public void disconnect() throws Exception {
		if(client.isConnected()){
			client.logout();
			client.disconnect();
		}
	}
	
	/**
	 * �ٿ�ε�
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
	 * ���ε�
	 * @param name
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public boolean upload(String name, File file) throws Exception {
		boolean isSuccess = client.storeFile(name, new FileInputStream(file));//�����
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
	 * ���ϸ� ����
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
	 * TC Preference���� FTP Connection ������ ���� ��
	 * [0=URL,1=PORT,2=USER,3=PASS,4=ROOT] �����ڴ� �޸�(,)
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