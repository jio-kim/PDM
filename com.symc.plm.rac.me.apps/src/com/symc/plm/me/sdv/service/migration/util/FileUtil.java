/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.springframework.util.StringUtils;

import com.symc.plm.me.utils.BundleUtil;

/**
 * Class Name : FileUtil
 * Class Description :
 * 
 * @date 2013. 11. 21.
 * 
 */
public class FileUtil {

    /**
     * Folder 하위 파일 리스트를 기지고 온다.
     * 
     * @method getFileList
     * @date 2013. 11. 21.
     * @param
     * @return ArrayList<String>
     * @exception
     * @throws
     * @see
     */
    public static ArrayList<String> getFileList(String folderPath) {
        ArrayList<String> fileList = new ArrayList<String>();
        File dirFile = new File(folderPath);
        File[] folderFileList = dirFile.listFiles();
        for (File tempFile : folderFileList) {
            if (tempFile.isFile()) {
                // String tempPath = tempFile.getParent();
                String tempFileName = tempFile.getName();
                // System.out.println("Path=" + tempPath);
                // System.out.println("FileName=" + tempFileName);
                fileList.add(tempFileName);
            }
        }
        return fileList;
    }

    /**
     * File을 읽어 Text를 가져온다.
     * 
     * @method getFileText
     * @date 2013. 11. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getFileText(String filePath) throws Exception {
        StringBuffer texts = new StringBuffer();
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(filePath);
            bis = new BufferedInputStream(fis);
            int data = 0;
            while ((data = bis.read()) != -1) { // -1 -> 파일의 끝
                texts.append((char) data);
            }
        } catch (FileNotFoundException e) {
            throw e;

        } catch (IOException e) {
            throw e;

        } finally { // 파일을 열면 닫아줘야함. 예외처리가 됬을때와 안됫을때 둘다 닫아줘야함.
                    // 둘다 닫아줘야하기 때문에 finally 를 사용!
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                }
            if (bis != null)
                try {
                    bis.close();
                } catch (IOException e) {
                }
        }
        return texts.toString();
    }

    /**
     * 파일 확장자를 제거한 파일명을 가져온다.
     * 
     * @method getExculsiveExtFileName
     * @date 2013. 11. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getExculsiveExtFileName(String fileName) {
        fileName = BundleUtil.nullToString(fileName);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }  

    /**
     * File에 Text를 appand한다.
     * 
     * @method appandFileText
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void appandFileText(String filePath, String text) throws Exception {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        // 리턴 문자 추가
        text += "\r\n";
        RandomAccessFile raf = new RandomAccessFile(new File(filePath), "rw");
        long fileLength = raf.length();
        raf.seek(fileLength); // to the end
        raf.write(text.getBytes());
        raf.close();
    }
}
