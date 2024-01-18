package com.ssangyong.common.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.teamcenter.rac.util.Registry;

/**
 * 
 * 문자 관련 유틸
 * 
 */
public class StringUtil {

    /**
     * 상속관계에 따른 TextBundle Getter 하위 Class에서 Override된 Text Property 인경우 상위 Text
     * Property는 무시함
     * 
     * ClassName+"."+MiddleName+"."+BundleName ex) BundleWorkDialog.MSG.ERROR
     * 
     * @param registry
     *            : TC Registry
     * @param strBundleName
     *            : Text Property 마지막 Name
     * @param strMiddleName
     *            : Text Property 중간에 포함되는 Name
     * @param dlgClass
     *            : 현재 기능을 호출한 Java Class Instance
     * @return Text Property
     */
    public static String getTextBundle(Registry registry, String strBundleName, String strMiddleName, Class<?> dlgClass) {
        // Middle Name 구성
        String middleName = ".";
        if (strMiddleName != null && !strMiddleName.equals("")) {
            middleName = "." + strMiddleName + ".";
        }
        // Full Bundle Name
        String strFullBundleName = dlgClass.getSimpleName() + middleName + strBundleName;
        String strBundleText = registry.getString(strFullBundleName);
        // 현재 Class Instance에 정의 되어 있지 않다면 상위 Class에서 찾음
        if (strBundleText == null || strBundleText.equals("") || strFullBundleName.equals(strBundleText)) {           
            // BundleWorkDialog(최상위) Class 에서도 Bundle을 찾을 수 없다면 공백을 Return
            if (dlgClass.getClass().getSimpleName().equals(dlgClass.getSimpleName())) {
                return "";
            } else {
                // 상위 Class에서 찾음(재귀호출)
                Class<?> supDlgClass = dlgClass.getSuperclass();
                strBundleText = getTextBundle(registry, strBundleName, strMiddleName, supDlgClass);
            }
        }
        return strBundleText;
    }

    /**
     * 상속관계에 따른 TextBundle Array Getter 하위 Class에서 Override된 Text Property 인경우
     * 상위 Text Property는 무시함
     * 
     * ClassName+"."+MiddleName+"."+BundleName ex)
     * BundleWorkDialog.MSG.ClassArray
     * 
     * @param registry
     *            : TC Registry
     * @param strBundleName
     *            : Text Property 마지막 Name
     * @param strMiddleName
     *            : Text Property 중간에 포함되는 Name
     * @param dlgClass
     *            : 현재 기능을 호출한 Java Class Instance
     * @return Text Property Array
     */
    public static String[] getTextBundleArray(Registry registry, String strBundleName, String strMiddleName, Class<?> dlgClass) {
        String middleName = ".";
        if (strMiddleName != null && !strMiddleName.equals("")) {
            middleName = "." + strMiddleName + ".";
        }
        String[] szBundleText = null;
        String strFullBundleName = dlgClass.getSimpleName() + middleName + strBundleName;
        String strBundleText = registry.getString(strFullBundleName);
        if (strBundleText == null || strBundleText.equals("") || strFullBundleName.equals(strBundleText)) {
            if (dlgClass.getClass().getSimpleName().equals(dlgClass.getSimpleName())) {
                return null;
            } else {
                Class<?> supDlgClass = dlgClass.getSuperclass();
                szBundleText = getTextBundleArray(registry, strBundleName, strMiddleName, supDlgClass);
            }
        } else {
            szBundleText = StringUtil.getSplitString(strBundleText, ",");
        }
        return szBundleText;
    }

    /**
     * splitter로 구별되는 문자열을 쪼개어 Vector로 return한다. 빈문자열은 문자열로 간주안함.
     * 
     * @method getSplitString
     * @date 2013. 2. 6.
     * @param
     * @return
     * @exception
     * @throws
     * @see
     */
    public static String[] getSplitString(String strValue, String splitter) {
        if (strValue == null || strValue.length() == 0) {
            return null;
        }
        if (splitter == null) {
            return null;
        }
        StringTokenizer split = new StringTokenizer(strValue, splitter);
        ArrayList<String> strList = new ArrayList<String>();
        while (split.hasMoreTokens()) {
            strList.add(new String(split.nextToken().trim()));
        }
        String[] szValue = new String[strList.size()];
        for (int i = 0; i < strList.size(); i++) {
            szValue[i] = strList.get(i);
        }
        return szValue;
    }   
    
    /**
     * String 문자가 null 이면 "" 리턴
     * String 문자가 "null" 이면 "" 리턴
     * String 문자가 있으면 trim 처리 후 리턴
     * 
     * @method nullToString    
     * @param
     * @return
     * @exception
     * @throws
     * @see
     */
    public static String nullToString(String str) {
        if(str == null) {
            return "";
        } else {
            if("null".equals(str.trim())) {
                return "";
            } else {
                return str.trim();
            }
        }
    }
    
    /**
     * 문자열중 특정문자를 치환한다
     * 
     * @param str
     *            대상문자열
     * @param src
     *            치환당할 문자
     * @param tgt
     *            치환할 문자
     * @return 완성된 문자열
     */
    public static String replace(String str, String src, String tgt) {
        StringBuffer buf = new StringBuffer();
        String ch = null;

        if (str == null || str.length() == 0)
            return "";

        int i = 0;
        int len = src.length();
        while (i < str.length() - len + 1) {

            ch = str.substring(i, i + len);
            if (ch.equals(src)) {
                buf.append(tgt);
                i = i + len;
            } else {
                buf.append(str.substring(i, i + 1));
                i++;
            }
        }

        if (i < str.length())
            buf.append(str.substring(i, str.length()));

        return buf.toString();
    }
    
    /**
     * properties에서 매크로 문자열을 가져온다. EX) test={0}번째 문자열 StringUtil.getString(registry, "test", new String[]{"1"}) ==> 1번째 문자열
     * 
     * @param registry
     * @param propertyName
     * @param param
     * @return
     */
    public static String getString(Registry registry, String propertyName, String[] param)
    {
      String value = registry.getString(propertyName);
      for (int i = 0; i < param.length; i++)
      {
        value = value.replaceAll("\\{" + i + "\\}", param[i]);
      }

      return value;
    }
    
    public static boolean isEmpty(String str)
    {
      if( str == null || "".equals(str.trim())  )
        return true;
      else
        return false;
    }
    
    /**
     * Map 데이터의 Value String을 Trim 처리한다. 
     * (Map Value가 Collection, Map Type, Timestamp Type이외에는 전부 toString한다.)
     * 
     * @method trimMapString 
     * @date 2013. 4. 9.
     * @param
     * @return HashMap<String,String>
     * @exception
     * @throws
     * @see
     */
    public static HashMap<String, Object> trimMapString(HashMap<String, Object> map) {        
        if(map == null) {
            return null;
        }
        String[] keys = map.keySet().toArray(new String[map.size()]);
        for (int i = 0; i < keys.length; i++) {
            if(map.get(keys[i]) != null) {
                if(map.get(keys[i]) instanceof Collection || map.get(keys[i]) instanceof Map || map.get(keys[i]) instanceof java.sql.Timestamp) {
                    continue;
                }
                map.put(keys[i], nullToString(map.get(keys[i]).toString()));
            }
        }
        return map;
    }
    
    public static String getFileName(File file) throws Exception {
        if (file.isDirectory()) {
            return file.getName();
        }
        return getFileName(file.getName()); 
    }
    
    public static String getFileName(String filePath) throws Exception {
        if(filePath == null || "".equals(filePath)) {
            return null;
        }        
        int fileExtCnt = filePath.lastIndexOf('.');
        int filePathCnt = filePath.lastIndexOf('/');
        if(filePathCnt == -1) {
            filePathCnt = 0;
        } else {
            filePathCnt = filePathCnt + 1;
        }
        if (fileExtCnt > 0) {
            return filePath.substring(filePathCnt, fileExtCnt);
        }
        return null;
    }
    
    public static String getExtension(File file) throws Exception {
        if (file.isDirectory())
            return null;
        return getExtension(file.getName());
    }
    
    public static String getExtension(String filePath) throws Exception {
        if(filePath == null || "".equals(filePath)) {
            return null;
        }        
        int i = filePath.lastIndexOf('.');
        if (i > 0 && i < filePath.length() - 1) {
            return filePath.substring(i + 1).toLowerCase();
        }
        return null;
    }
    
    public static String getStackTraceString(Exception ex) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pinrtStream = new PrintStream(out);
        ex.printStackTrace(pinrtStream);
        return out.toString();
    }
    
	/**
	 * [NON-SR][2017.02.13] taeku.jeong
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
			}else{
				returnStr = tempStr;
				break;
			}
			
		}
		
		return returnStr;
	}
}
