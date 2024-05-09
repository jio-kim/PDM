package com.kgm.soa.common.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class LogUtil
{
    private static Logger log4jLogger = Logger.getLogger(LogUtil.class);
    
    public LogUtil()
    {
    }
    
    /**
     * 메세지 Console 출력
     * @param msg
     */
    public static void out(String msg)
    {
        System.out.println(formatStr(msg));
    }
    
    /**
     * Key = Value 형식 메세지 Console 출력
     * @param key
     * @param msg
     */
    public static void out(String key, Object msg)
    {
        TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
        Date now = new Date();
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        dtf.setTimeZone(tz);
        String currentTime = dtf.format(now);
        
        System.out.printf("[" + currentTime + "]" + " key : %s , value : %s %n", key, msg);
    }
    
    /**
     * 메세지 작업결과 메시지 파일 출력
     * @param msg
     * @throws IOException
     */
    public static void fout(String mgs) throws IOException
    {
        fout(null, null, null, null, mgs);
    }
    
    /**
     * 메세지 작업결과 메시지 파일 출력
     * @param filename
     * @param mgs
     * @throws IOException
     */
    public static void fout(String filename, String mgs) throws IOException
    {
        fout(filename, null, null, null, mgs);
    }
    
    /**
     * 메세지 파일 출력
     * @param msg
     * @throws IOException
     */
    public static void fout(String filename, String classname, String method, String exception, String msg)
            throws IOException
    {
        TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
        Date now = new Date();
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        dtf.setTimeZone(tz);
        String currentTime = dtf.format(now);
        
        //String logPath = System.getProperty("user.home") + "\\webapp\\PMS\\logs";
        String logPath = "D:\\webapp\\soaweb\\logs";
        File logDir = new File(logPath);
        if (!logDir.isDirectory())
        {
            if (!logDir.mkdirs())
            {
                LogUtil.out("\n" + formatStr("Failed to create log directory!"));
            }
        }

        String logFile = null;        
        if (filename != null && filename.length() > 0 )
        {
            logFile = logPath + "\\" + filename + "_" + df.format(now) + ".log";
        }
        else
        {
            //logFile = System.getProperty("user.home") + "\\SMart\\PMS\\logs\\explm_fout_" + df.format(now) + ".log";
            logFile = "D:\\webapp\\soaweb\\logs\\fout_" + df.format(now) + ".log";
        }
                
        FileWriter fileWriter = new FileWriter(logFile, true);
        fileWriter.write("############################################################################################\r\n");
        fileWriter.write("CLASS         : " + (classname == null ? "" : classname) + "\r\n");
        fileWriter.write("METHOD        : " + (method == null ? "" : method) + "\r\n");
        fileWriter.write("EXCEPTION     : " + (exception == null ? "" : exception) + "\r\n");
        fileWriter.write("TIME          : " + (currentTime == null ? "" : currentTime) + "\r\n");
        fileWriter.write("MESSAGE       : " + "\r\n");
        fileWriter.write(msg + "\r\n");
        fileWriter.write("############################################################################################\r\n");
        fileWriter.flush();
        fileWriter.close();
    }
    
    /**
     * Debug 출력
     * @param msg
     */
    public static void debug(String msg)
    {
        log4jLogger.debug("\n" + formatStr(msg));
    }
    
    /**
     * Key = Value 형식 메세지 Console 출력
     * @param key
     * @param msg
     */
    public static void debug(String key, Object msg)
    {
        log4jLogger.debug("\n" + formatStr("key : " + key + ", value :" + msg));
    }
    
    
    /**
     * Inform 출력
     * @param msg
     */
    public static void info(String msg)
    {
        log4jLogger.info("\n" + formatStr(msg));
    }
    
    /**
     * Warn 출력
     * @param msg
     */
    public static void warn(String msg)
    {
        log4jLogger.warn("\n" + formatStr(msg));
    }
    
    /**
     * Error 출력
     * @param msg
     */
    public static void error(String msg)
    {
        log4jLogger.error("\n" + formatStr(msg));
    }
    
    /**
     * 일정한 Format으로 메세지 변환
     * @param str
     * @return
     */
    private static String formatStr(String str)
    {
    	if (str == null || str.equals(""))
    		return "";
    	
        TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
        Date now = new Date();
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        dtf.setTimeZone(tz);
        String currentTime = dtf.format(now);

        String message = "";
        if (str.indexOf("\n") != -1)
        {
            String[] tArray = str.split("\n");
            for (int i = 0; i < tArray.length; i++)
            {
                message += "[" + currentTime + "]" + tArray[i];
            }
        }
        else
        {
            message = "[" + currentTime + "]" + str;
        }
        return message;
    }
}
