package com.ssangyong.common.util;

import java.util.Calendar;

public class DateUtil {
    /**
     * 날자 포맷
     * 
     * @method formatTime 
     * @date 2013. 4. 11.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String formatTime(long lTime) {
        if(lTime == 0) {
            return "";
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(lTime);
        return (c.get(Calendar.YEAR) + "년 " + (c.get(Calendar.MONTH) + 1) + "월 " + c.get(Calendar.DAY_OF_MONTH) + "일 " + c.get(Calendar.HOUR_OF_DAY) + "시 " + c.get(Calendar.MINUTE) + "분 " + c.get(Calendar.SECOND) + "." + c.get(Calendar.MILLISECOND) + "초");
    }
   
    /**
     * 날자 파일명 생성 - LOG 생성용
     * 
     * @method getLogFileName 
     * @date 2013. 4. 11.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getLogFileName(String extFileName) {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR)+"_"+(c.get(Calendar.MONTH)+1)+"_"+c.get(Calendar.DAY_OF_MONTH)+"_"+c.get(Calendar.HOUR_OF_DAY)+"_"+c.get(Calendar.MINUTE)+"_"+c.get(Calendar.SECOND)+"_"+c.get(Calendar.MILLISECOND)+"." + extFileName;
    }
}
