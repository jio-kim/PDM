package com.symc.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
 //[20240216]openJDK11 사용으로 인한 log4j 버전 변경으로 수정
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 소켓 네트워크 통신관련 유틸
 * @클래스명 : NetworkUtil
 * @작성자 : cho9271.cho
 * @작성일 : 2012. 8. 17.
 * @설명 :
 * @수정이력 - 수정일,수정자,수정내용
 *
 */
public class NetworkUtil {

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
    public static boolean uploadFtpFile(String ip, int port, String id, String password, String uploaddir, String makedir, File[] files) throws Exception {
        if(files == null || files.length == 0) {
            return true;
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
            for (int i = 0; i < files.length; i++) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(files[i]);
                    boolean isSuccess = ftp.storeFile(files[i].getName(), fis);
                    if (isSuccess) {
                        System.out.println("[SUCCESS] " + files[i] + " 파일 FTP 업로드 성공");
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    throw ioe;
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            }
            ftp.logout();
            result = true;
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
                log.error(errorMsg);
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

    /**
     * JSON 웹서비스
     * @메소드명 : getJson
     * @작성자 : cho9271.cho
     * @작성일 : 2012. 8. 17.
     * @param serverUrl
     * @param postPara
     * @param flagEncoding
     * @param log
     * @return
     * @throws Exception
     * @설명 :
     * @수정이력 - 수정일,수정자,수정내용
     *
     */
    public static String getJson(String serverUrl, String postPara, boolean flagEncoding, Logger log) throws Exception {
        URL url = null;
        HttpURLConnection conn = null;
        PrintWriter postReq = null;
        BufferedReader postRes = null;
        StringBuilder json = null;
        String line = null;
        json = new StringBuilder();
        try {
            if (flagEncoding) {
                postPara = URLEncoder.encode(postPara, "UTF-8");
            }
            url = new URL(serverUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setRequestProperty("Content-Length", Integer.toString(postPara.length()));
            conn.setDoInput(true);
            postReq = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            postReq.write(postPara);
            postReq.flush();
            postRes = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = postRes.readLine()) != null) {
                json.append(line);
            }
            conn.disconnect();
        } catch (MalformedURLException ex) {
            throw new Exception(ex.getMessage());
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
        return json.toString();
    }

}
