package com.kgm.common.utils;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;

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
     * ���ڼ� ������ ���ڿ��� �ɰ��� String[] �� Return.
     * 
     * @author jclee
     * @param strValue : ���ڿ�
     * @param iLength : ����
     * @return String[] : ���
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
     * CELL_TYPE_NUMERIC�� ��� Integer�� Casting�Ͽ� ��ȯ��
     * Long ������ ���� ���Ұ�� �ٸ��� �����ؾ� ��.
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
                
                // Integer�� Casting�Ͽ� ��ȯ��
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
     * String ���ڰ� null �̸� "" ����
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
            return str.trim();
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
}
