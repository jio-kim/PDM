package com.ssangyong.common.utils;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;

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
     * 글자수 단위로 문자열을 쪼개어 String[] 로 Return.
     * 
     * @author jclee
     * @param strValue : 문자열
     * @param iLength : 길이
     * @return String[] : 결과
     */
    public static String[] getSplitString(String strValue, int iLength) {
    	if (strValue == null || strValue.length() == 0) {
            return null;
        }

        if (iLength == 0) {
            return null;
        }
        
        if (strValue.length() < iLength) {
			return new String[] {strValue};
		}

        String[] szValue = new String[strValue.length() / iLength];
        for (int inx = 0; inx < strValue.length() / iLength; inx++) {
			szValue[inx] = strValue.substring(inx * iLength, strValue.length() <= ((inx + 1) * iLength) ? strValue.length() : ((inx + 1) * iLength));
		}
        
        return szValue;
    }
    
    /**
     * Excel Cell Value Return
     * 
     * CELL_TYPE_NUMERIC인 경우 Integer로 Casting하여 반환함
     * Long 형태의 값을 원할경우 다르게 구현해야 함.
     * 
     * @param cell
     * @return
     */
    public static String getCellText(Cell cell)
    {
        String value = "";
        if (cell != null)
        {
            
            switch (cell.getCellType())
            {
                case XSSFCell.CELL_TYPE_FORMULA:
                    value = cell.getCellFormula();
                    break;
                
                // Integer로 Casting하여 반환함
                case XSSFCell.CELL_TYPE_NUMERIC:
                    value = "" + (int) cell.getNumericCellValue();
                    break;
                
                case XSSFCell.CELL_TYPE_STRING:
                    value = "" + cell.getStringCellValue();
                    break;
                
                case XSSFCell.CELL_TYPE_BLANK:
                    // value = "" + cell.getBooleanCellValue();
                    value = "";
                    break;
                
                case XSSFCell.CELL_TYPE_ERROR:
                    value = "" + cell.getErrorCellValue();
                    break;
                default:
            }
            
        }
        
        return value;
    }
    
    /**
     * String 문자가 null 이면 "" 리턴
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
            return str.trim();
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
}
