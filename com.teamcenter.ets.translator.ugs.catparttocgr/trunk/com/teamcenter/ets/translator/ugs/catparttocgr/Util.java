package com.teamcenter.ets.translator.ugs.catparttocgr;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.ssangyong.common.remote.DataSet;

public class Util {
	
    /**
     * Upload FTP File
     * @메소드명 : uploadFtpFile
     * @작성자 : cho9271.cho
     * @작성일 : 2012. 9. 4.
     * @param ip
     * @param port
     * @param id
     * @param password
     * @param uploaddir
     * @param makedir
     * @param files
     * @param log
     * @return
     * @throws Exception
     * @설명 :
     * @수정이력 - 수정일,수정자,수정내용
     *
     */
    public static boolean uploadFtpFile(String ip, int port, String id, String password, String uploaddir, String makedir, File file) throws Exception {
        if(file == null ) {
            return false;
        }
        boolean result = false;
        FTPClient ftp = null;
        int reply = 0;
        try {
            ftp = new FTPClient();
            ftp.connect(ip, port);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return result;
            }
            if (!ftp.login(id, password)) {
                ftp.logout();
                return result;
            }
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            ftp.changeWorkingDirectory(uploaddir);
            // 디렉토리 생성
            if (makedir != null && !makedir.isEmpty()) {
                ftp.makeDirectory(makedir);
                ftp.changeWorkingDirectory(makedir);
            }

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                result = ftp.storeFile(file.getName(), fis);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                result = false;
                throw ioe;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                
                ftp.logout();
            }
            
        } catch (SocketException se) {
            throw se;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw e;
        } finally {
            if (ftp != null && ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        return result;
    }

    /**
     * Get FTP File
     * @메소드명 : getFtpFile
     * @작성자 : cho9271.cho
     * @작성일 : 2012. 8. 17.
     * @param ip
     * @param port
     * @param id
     * @param password
     * @param localdir
     * @param serverdir
     * @param fileName
     * @param log
     * @return
     * @throws Exception
     * @설명 :
     * @수정이력 - 수정일,수정자,수정내용
     *
     */
    public static File getFtpFile(String ip, int port, String id, String password, String localdir, String serverdir, String fileName, Logger log) throws Exception {
        FTPClient ftp = null;
        File f = null;
        int reply = 0;
        try {
            ftp = new FTPClient();
            ftp.connect(ip, port);
            reply = ftp.getReplyCode();
            String errorMsg = "";
            if (!FTPReply.isPositiveCompletion(reply)) {
                errorMsg = "[ERROR] FTP access error!";
                ftp.disconnect();
//                log.error(errorMsg);
                throw new Exception(errorMsg);
            }
            if (!ftp.login(id, password)) {
                errorMsg = "[ERROR] FTP login error!";
                ftp.logout();
                log.error(errorMsg);
                throw new Exception(errorMsg);
            }
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            ftp.changeWorkingDirectory(serverdir);
            f = new File(localdir, fileName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                boolean isSuccess = ftp.retrieveFile(fileName, fos);
                if (isSuccess) {
                    log.debug("[SUCCESS] FTP the file has finished downloading.");
                } else {
                    errorMsg = "[ERROR] FTP file download error.!";
                    log.error(errorMsg);
                    throw new Exception(errorMsg);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (fos != null)
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        throw ex;
                    }
            }
            ftp.logout();
        } catch (SocketException se) {
            se.printStackTrace();
            throw se;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (ftp != null && ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return f;
    }

    /**
     * FTP 접속 후 패스폴더에 있는 파일 리스트를 조회한다.
     * @메소드명 : getFtpFileList
     * @작성자 : cho9271.cho
     * @작성일 : 2012. 9. 17.
     * @param ip
     * @param port
     * @param id
     * @param password
     * @param path
     * @param log
     * @return
     * @throws Exception
     * @설명 :
     * @수정이력 - 수정일,수정자,수정내용
     *
     */
    public static FTPFile[] getFtpFileList(String ip, int port, String id, String password, String path, Logger log) throws Exception {
        FTPFile[] ftpFileList = null;
        FTPClient ftp = null;
        int reply = 0;
        try {
            ftp = new FTPClient();
            ftp.connect(ip, port);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new Exception("FTP Server Error ("+reply+")");
            }
            if (!ftp.login(id, password)) {
                ftp.logout();
                throw new Exception("FTP Login Error");
            }
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            ftp.changeWorkingDirectory(path);
            ftpFileList = ftp.listFiles();
            ftp.logout();
        } catch (SocketException se) {
            se.printStackTrace();
            throw se;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (ftp != null && ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return ftpFileList;
    }

    // 파일 로그를 생성.
	void setLog(String s) throws IOException {
  
		File zLogFile = new File( "c:\\temp\\log.txt" );			
		zLogFile.createNewFile();	
	      
		BufferedWriter out = new BufferedWriter(new FileWriter(zLogFile));                                    
		out.write(s);	      
		out.newLine();	      
		out.close();		
	} 
	
	protected static Object execute(String servletUrlStr, String class_name, String method, DataSet paramData, boolean blnReturn) throws Exception {
		Object obj = null;
		ObjectOutputStream output = null;
		ObjectInputStream input = null;

		try {
			if (paramData == null) {
				paramData = new DataSet();
			}

			paramData.setString("class_name", class_name);
			paramData.setString("method", method);
			paramData.setBoolean("blnReturn", blnReturn);

			String strParameter = "";

			URL url = new URL(servletUrlStr);
			URLConnection urlConn = url.openConnection();

			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);

			urlConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			urlConn.setRequestProperty("Content-length", String.valueOf(strParameter.length()));

			output = new ObjectOutputStream(urlConn.getOutputStream());
			output.writeObject(paramData);

			input = new ObjectInputStream(new BufferedInputStream(urlConn.getInputStream()));

			obj = input.readObject();

			if (obj instanceof Exception) {
				throw (Exception) obj;
			}
		} finally {
			if (output != null)
				output.close();
			if (input != null)
				input.close();
		}

		return obj;
	}
	
	public static Properties getDefaultProperties() throws FileNotFoundException, IOException{
		String serviceName = "catparttocgr";
	      String classPath = Util.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	      StringBuffer sb = new StringBuffer(classPath);
	      Integer idx = sb.indexOf("DispatcherClient");
	      String dpRoot = classPath.substring(1, idx);
	      String transPath = dpRoot + "Module" + File.separator + "Translators";
	      String path = transPath + File.separator + serviceName + File.separator + serviceName + ".properties";
	      Properties prop = new Properties();
	      prop.load(new FileInputStream(path));
	      
	      return prop;
	}
}
