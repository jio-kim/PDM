package com.kgm.soa.util;

import java.io.FileOutputStream;
import java.io.IOException;

public class LogUtil {
    /**
     * String을 받아 로그를 기록한다.
     * 
     * @method saveLog 
     * @date 2013. 4. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void saveLog(String strFileFullPath, String writeableStr, Class<?> cls) {
        if(strFileFullPath == null ||  writeableStr == null) {
            return;
        }
        // Import 작업 정보를 File에 저장
        try {
            FileOutputStream fos = null;
            fos = new FileOutputStream(strFileFullPath);
            fos.write(writeableStr.getBytes());
            fos.close();
        } catch (IOException e) {
            System.err.println("[Error] Log File Write error : " + cls.toString());
            e.printStackTrace();            
        }
    }
}
