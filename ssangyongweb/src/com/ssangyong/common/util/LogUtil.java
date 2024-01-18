package com.ssangyong.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.ssangyong.common.remote.DataSet;

public class LogUtil {
	
	private static Logger logger = Logger.getLogger(LogUtil.class);
    
    public LogUtil()
    {
    }

	/**
     * Dataset Param Key 및 Value 출력
     * @method formatTime 
     * @date 2013. 4. 11.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static void error(String msg, DataSet dataSets ) {
        
    	try {
            
    		logger.error(formatStr(msg));
            
        	if (dataSets != null)  {
        		
        		Iterator<String> keys = dataSets.keySet().iterator();
        		while( keys.hasNext() ){            
        			String key = keys.next();
        			if (dataSets.get(key) == null) {
            			logger.error(key + " : ");
        			} else {
        				try {
            				String value = dataSets.get(key).toString();
            				if (value == null) {
                    			logger.error("[Param] " + key + " : ");
            				} else {
            					logger.error("[Param] " + key + " : " + value);
            				}
    					} catch (Exception e) {
                			logger.error("[Param] " + key + " : error");
    					}
        			}
        		}
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    /**
     * 20180427 LOG 추가
     * 
     */
    public static void error(String msg, Object object ) {
        
    	try {
            
    		logger.error(formatStr(msg));
            
        	if (object != null)  {
        		
        		logger.error(object);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Debug 출력
     * @param msg
     */
    public static void debug(String msg)
    {
    	logger.debug(formatStr(msg));
    }
    
    /**
     * Key = Value 형식 메세지 Console 출력
     * @param key
     * @param msg
     */
    public static void debug(String key, Object msg)
    {
    	logger.debug(formatStr("key : " + key + ", value :" + msg));
    }
    
    
    /**
     * Inform 출력
     * @param msg
     */
    public static void info(String msg)
    {
    	logger.info(formatStr(msg));
    }
    
    /**
     * Warn 출력
     * @param msg
     */
    public static void warn(String msg)
    {
    	logger.warn(formatStr(msg));
    }
    
    /**
     * Error 출력
     * @param msg
     */
    public static void error(String msg)
    {
    	logger.error(formatStr(msg));
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