package com.kgm.soa.common.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    /**
     * Date를 String으로 가져온다.
     * 
     * @method formatTime 
     * @date 2013. 6. 26.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String formatTime(Date date) {
        if(date == null) return "";
        if(date.getTime() == 0) {
            return "";
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date.getTime());
        return (c.get(Calendar.YEAR) + "년 " + (c.get(Calendar.MONTH) + 1) + "월 " + c.get(Calendar.DAY_OF_MONTH) + "일 " + c.get(Calendar.HOUR_OF_DAY) + "시 " + c.get(Calendar.MINUTE) + "분 " + c.get(Calendar.SECOND) + "." + c.get(Calendar.MILLISECOND) + "초");
    }
    
    /**
     * Time을 String Date로 변경
     * 
     * @method getTimeInMillisToDate 
     * @date 2013. 6. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getTimeInMillisToDate(long time) {
        return formatTime(new Date(time));
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
