package org.apache.commons.ftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FTPClientUtil {

    private final FTPClient ftpClient;

    public FTPClientUtil(String server, int port, String user, String password) throws IOException {
        ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        ftpClient.login(user, password);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // 바이너리 파일 타입으로 설정
    }
    
    public boolean isBOP(String localVersion) throws IOException {
    	File localFile = new File(localVersion);
    	String BOP = null;
    	boolean isBOP = false;
    	Scanner scan = new Scanner(localFile);
    	while (scan.hasNext()) {
    		if((BOP = scan.nextLine()).startsWith("isBOP")) {
    			String[] BOPs = BOP.split(":");
    			isBOP = Boolean.parseBoolean(BOPs[1]);
    		}
    	}
    	scan.close();
    	return isBOP;
    }
    
    public void checkVersion(String remoteVersion, String localVersion, String tempFilePath) throws IOException {
    	File localFile = new File(localVersion);
    	File tempFile = new File(tempFilePath);
    	if(!tempFile.exists())
    	{
    		try 
            {
            	tempFile.createNewFile();
            } 
            catch (Exception e) 
            {
                System.out.println("path createNewFile Error : "+e.toString());
            }
    	}
    	try (FileOutputStream output = new FileOutputStream(tempFilePath)) {
            ftpClient.retrieveFile(remoteVersion, output);
        }
    }
    
    public String findVersion (File localFile) throws IOException {
    	String version = null;
    	String localVersion = null;
    	Scanner scan = new Scanner(localFile);
    	while (scan.hasNext()) {
    		if((localVersion = scan.nextLine()).startsWith("version")) {
    			String[] Versions = localVersion.split(":");
    			version = Versions[1];
    		}
    	}
    	scan.close();
    	return version;
    }
    
    // 파일 Version 확인
    public ArrayList downloadList(String localVersion, String tempFilePath) throws IOException {
    	ArrayList downloadList = new ArrayList();
    	int localVer;
    	int remoteVer;
    	String lVer = null;
    	String rVer = null;
    	int listVer;
    	File localFile = new File(localVersion);
    	File tempFile = new File(tempFilePath);
    	String[] fileName;
    	try{
    		// 로컬 Version 확인
    		lVer = findVersion(localFile);
    		localVer = Integer.parseInt(lVer.replaceAll("\\.", "").trim());
    		rVer = findVersion(tempFile);
    		remoteVer = Integer.parseInt(rVer.replaceAll("\\.", "").trim());
    		
    		// Temp 파일의 내용을 저장함.
    		Scanner scan = new Scanner(tempFile);
    		while(scan.hasNext())
            {
    			String line = scan.nextLine();
    			if(!(line.startsWith("version"))) {
    				fileName = line.split(";");
            		listVer = Integer.parseInt(fileName[1].replaceAll("\\.", "").trim());
            		if (listVer > localVer && listVer <= remoteVer) { 
            			downloadList.add(fileName[0]);
            		}
    			}
            }
    		scan.close();
    	} 
    	catch(FileNotFoundException  e) 
        {
            e.printStackTrace();
            System.out.println("fileReader 에러 : " + e.toString());
        }
    	
    	return downloadList;
    }

    // 파일을 FTP 서버에 업로드하는 메소드
    public boolean uploadFile(String localFilePath, String remoteFilePath) throws IOException {
        try (FileInputStream input = new FileInputStream(localFilePath)) {
            return ftpClient.storeFile(remoteFilePath, input);
        }
    }

    // FTP 서버에서 파일을 다운로드하는 메소드
    public boolean downloadFile(ArrayList downloadList, Boolean isBOP) throws IOException {
    	String downloadFileName = null;
    	String remoteFilePath = null;
    	String localFilePath = null;
    	File file = null;
    	ArrayList isUpdate = new ArrayList();
    	//C:\Siemens\TC13\portal\abcdefg.jar;1.3.4
    	
    	for (int i=0; i<downloadList.size(); i++) {
    		downloadFileName = (String) downloadList.get(i);
    		
    		int lastIndex = downloadFileName.lastIndexOf("\\");
    		String pathWithoutFileName = downloadFileName.substring(0, lastIndex);
    		
    		String[] pathParts = pathWithoutFileName.split("\\\\");
    		StringBuilder paths = new StringBuilder(pathParts[0] + "\\");
    		
            for (int s = 1; s < pathParts.length; s++) {
            	if (s > 1) {
            		paths.append("\\");
                }
            	paths.append(pathParts[s]);
                file = new File(paths.toString());
                if(!file.exists()) {
    				try 
    	            {
    					file.mkdir();
    	            }
    	            catch (Exception e) 
    	            {
    	                System.out.println("path mkdir Error : "+e.toString());
    	            }
    			}
            }
            
    		localFilePath = downloadFileName.replaceAll("\\\\", "/");
    		lastIndex = downloadFileName.lastIndexOf("\\");
    		if(isBOP) {
    			remoteFilePath = "/BOP/"+downloadFileName.substring(lastIndex + 1);
    		} else {
    			remoteFilePath = "/nonBOP/"+downloadFileName.substring(lastIndex + 1);
    		}
    		try (FileOutputStream output = new FileOutputStream(localFilePath)) {
                boolean isSuccess = ftpClient.retrieveFile(remoteFilePath, output);
                System.out.println(downloadFileName + " 파일 다운로드 성공 여부 : " + isSuccess);
                isUpdate.add(isSuccess);
            }
    	}
    	if(!(isUpdate.contains(false) || isUpdate.isEmpty())) {
    		return true;
		} else {
			return false;
		}
    }
    
    // 로컬 Version 업데이트
    public void updateVersion(String localVersion, String tempFilePath) throws IOException {
    	FileWriter fw = null;
    	File tempFile = new File(tempFilePath);
    	File localFile = new File(localVersion);
    	String remoteVersion = findVersion(tempFile);
    	String version = "version:"+remoteVersion+"\n";
    	
    	String line = null;
    	
    	
    	Scanner scan = new Scanner(localFile);
    	while(scan.hasNext()) {
    		
    		if(!(line = scan.nextLine()).startsWith("version")) {
    			version += line;
    		}
    	}
    	
    	// 로컬 Version 업데이트
    	if(localFile.exists()) {
    		try {
    			fw = new FileWriter(localVersion, false);
        		fw.write(version);
        		fw.close();
    		}
    		catch (IOException e){
    			e.printStackTrace();
                System.out.println("fileWriter 에러 : "+e.toString());
    		}
    		
    	}
    	scan.close();
    }
    
    // Temp File 삭제
    public void deleteTempFile(String tempFilePath) throws IOException {
    	File tempFile = new File(tempFilePath);
    	if(tempFile.exists()) {
    		System.gc();
    		System.runFinalization();
    		System.out.println("Temp 파일 삭제 성공 여부 : " + tempFile.delete());
    	}
    }
    
    public void deleteCache() throws IOException {
    	String user = System.getProperty("user.name");
    	File cacheFile = new File("C:/Users/"+user+"/Teamcenter");
    	try {
    		if (cacheFile.exists()) {
    			FileUtils.cleanDirectory(cacheFile);//하위 폴더와 파일 모두 삭제
    			
    	  	  	if (cacheFile.isDirectory()) {
    	  	  		cacheFile.delete(); // 대상폴더 삭제
    	  	  	}
    	    } else {
    	    	System.out.println("File not exists");
                return;
    	    }
    	} catch (IOException e) {
    		System.out.println("cleanDirectory 에러 : "+e.toString());
    	}
    }

    // FTP 서버 연결을 종료하는 메소드
    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }
    
}
