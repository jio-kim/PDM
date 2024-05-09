package com.kgm.common.util;

import java.util.Calendar;

public class DateUtil {
    /**
     * ���� ����
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
        return (c.get(Calendar.YEAR) + "�� " + (c.get(Calendar.MONTH) + 1) + "�� " + c.get(Calendar.DAY_OF_MONTH) + "�� " + c.get(Calendar.HOUR_OF_DAY) + "�� " + c.get(Calendar.MINUTE) + "�� " + c.get(Calendar.SECOND) + "." + c.get(Calendar.MILLISECOND) + "��");
    }
   
    /**
     * ���� ���ϸ� ���� - LOG ������
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
