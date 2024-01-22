package com.symc.plm.me.common;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SDVStringUtiles {

    public static String dateToString(Date date, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }
    
    /**
     * String 문자에서 Double 값을 가져옴
     * Double 형으로 변환되지 않는 경우는 0으로 return 함
     * @method getDoubleFromString 
     * @date 2014. 1. 8.
     * @param
     * @return double
     * @exception
     * @throws
     * @see
     */
    public static double getDoubleFromString(String inputString)
    {
        double doubleValue = 0;
        StringBuilder sb = new StringBuilder();
        String regEx = "(?!=\\d\\.\\d\\.)([\\d.]+)";
        Pattern pattern = Pattern.compile(regEx);

        Matcher match = pattern.matcher(inputString);
        while (match.find()) {
            sb.append(match.group());
        }        
        try {
            doubleValue = Double.parseDouble(sb.toString());
        } catch (NumberFormatException ex) {
            return 0; 
        }
        return doubleValue; 
    }
    
	/**
	 * 주어진 한글이 포함된 문자열을 byte 형식으로 바꿨을때 byte형의 길이가 주어진 targetByteSize 이내의 크기인
	 * 문자열로 잘라서 Return 한다.
	 * 
	 * @param inputStr 한글이 포함된 원본 문자열
	 * @param cheracterSetName 문자열 Byte 처리할때 사용할 CharacterSet Name (EUC-KR, UTF-8, UTF-16, MS949 ...)
	 * @param targetByteSize 문자열을 byte 형태로 변환시 byte Size
	 * @return 주어진 byte size 또는 그보다 작은 크기의 문자열
	 */
	public static String getByteSizedStr(String inputStr, String cheracterSetName, int targetByteSize){
		String returnStr = null;
	
		for (int lastIndex = inputStr.length(); inputStr!=null && lastIndex >= 0; lastIndex--) {
			String tempStr = inputStr.substring(0, lastIndex);
			if(tempStr!=null && tempStr.trim().length()>0){
				int currentLength = 0;
				try {
					currentLength = tempStr.getBytes(cheracterSetName).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				if(currentLength<=targetByteSize){
					returnStr = tempStr;
					break;
				}
			}
		}
		
		return returnStr;
	}

}
