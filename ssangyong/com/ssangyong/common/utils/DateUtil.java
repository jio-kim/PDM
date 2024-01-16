package com.ssangyong.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.teamcenter.rac.kernel.TCDateEncoder;

public class DateUtil {
	public static String getNewYearMonthValue(String oldDate){
		Date tempDate = TCDateEncoder.getDate(oldDate);
		Calendar cal = Calendar.getInstance ( );
		cal.setTime(tempDate);
		cal.set( Calendar.DAY_OF_MONTH, 0 );
		cal.set( Calendar.HOUR_OF_DAY, 0 );
		cal.set( Calendar.MINUTE, 0 );
		cal.set( Calendar.SECOND, 0 );
		cal.set( Calendar.MILLISECOND, 0 );
		cal.add(Calendar.DAY_OF_MONTH, 1);
		String tempDateValue = TCDateEncoder.getDateString(cal.getTime()); 
		return tempDateValue;
	}
	
	/**
     * ÇöÀç Data¸¦ Return
     * 
     * @param format
     *            : SimpleDataFormat String
     * @return
     */
    public static String getClientDay(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new java.util.Date());
    }

    public static String getCurrentDateIndex() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddaHHmmss",
                java.util.Locale.KOREA);
        return sdf.format(cal.getTime());
    }
}
