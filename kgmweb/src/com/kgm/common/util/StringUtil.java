package com.kgm.common.util;

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
 * ���� ���� ��ƿ
 * 
 */
public class StringUtil {

    /**
     * ��Ӱ��迡 ���� TextBundle Getter ���� Class���� Override�� Text Property �ΰ�� ���� Text
     * Property�� ������
     * 
     * ClassName+"."+MiddleName+"."+BundleName ex) BundleWorkDialog.MSG.ERROR
     * 
     * @param registry
     *            : TC Registry
     * @param strBundleName
     *            : Text Property ������ Name
     * @param strMiddleName
     *            : Text Property �߰��� ���ԵǴ� Name
     * @param dlgClass
     *            : ���� ����� ȣ���� Java Class Instance
     * @return Text Property
     */
    public static String getTextBundle(Registry registry, String strBundleName, String strMiddleName, Class<?> dlgClass) {
        // Middle Name ����
        String middleName = ".";
        if (strMiddleName != null && !strMiddleName.equals("")) {
            middleName = "." + strMiddleName + ".";
        }
        // Full Bundle Name
        String strFullBundleName = dlgClass.getSimpleName() + middleName + strBundleName;
        String strBundleText = registry.getString(strFullBundleName);
        // ���� Class Instance�� ���� �Ǿ� ���� �ʴٸ� ���� Class���� ã��
        if (strBundleText == null || strBundleText.equals("") || strFullBundleName.equals(strBundleText)) {           
            // BundleWorkDialog(�ֻ���) Class ������ Bundle�� ã�� �� ���ٸ� ������ Return
            if (dlgClass.getClass().getSimpleName().equals(dlgClass.getSimpleName())) {
                return "";
            } else {
                // ���� Class���� ã��(���ȣ��)
                Class<?> supDlgClass = dlgClass.getSuperclass();
                strBundleText = getTextBundle(registry, strBundleName, strMiddleName, supDlgClass);
            }
        }
        return strBundleText;
    }

    /**
     * ��Ӱ��迡 ���� TextBundle Array Getter ���� Class���� Override�� Text Property �ΰ��
     * ���� Text Property�� ������
     * 
     * ClassName+"."+MiddleName+"."+BundleName ex)
     * BundleWorkDialog.MSG.ClassArray
     * 
     * @param registry
     *            : TC Registry
     * @param strBundleName
     *            : Text Property ������ Name
     * @param strMiddleName
     *            : Text Property �߰��� ���ԵǴ� Name
     * @param dlgClass
     *            : ���� ����� ȣ���� Java Class Instance
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
     * splitter�� �����Ǵ� ���ڿ��� �ɰ��� Vector�� return�Ѵ�. ���ڿ��� ���ڿ��� ���־���.
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
     * String ���ڰ� null �̸� "" ����
     * String ���ڰ� "null" �̸� "" ����
     * String ���ڰ� ������ trim ó�� �� ����
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
     * ���ڿ��� Ư�����ڸ� ġȯ�Ѵ�
     * 
     * @param str
     *            ����ڿ�
     * @param src
     *            ġȯ���� ����
     * @param tgt
     *            ġȯ�� ����
     * @return �ϼ��� ���ڿ�
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
     * properties���� ��ũ�� ���ڿ��� �����´�. EX) test={0}��° ���ڿ� StringUtil.getString(registry, "test", new String[]{"1"}) ==> 1��° ���ڿ�
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
     * Map �������� Value String�� Trim ó���Ѵ�. 
     * (Map Value�� Collection, Map Type, Timestamp Type�̿ܿ��� ���� toString�Ѵ�.)
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
	 * �־��� �ѱ��� ���Ե� ���ڿ��� byte �������� �ٲ����� byte���� ���̰� �־��� targetByteSize �̳��� ũ����
	 * ���ڿ��� �߶� Return �Ѵ�.
	 * 
	 * @param inputStr �ѱ��� ���Ե� ���� ���ڿ�
	 * @param cheracterSetName ���ڿ� Byte ó���Ҷ� ����� CharacterSet Name (EUC-KR, UTF-8, UTF-16, MS949 ...)
	 * @param targetByteSize ���ڿ��� byte ���·� ��ȯ�� byte Size
	 * @return �־��� byte size �Ǵ� �׺��� ���� ũ���� ���ڿ�
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
