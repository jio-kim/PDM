package com.kgm.common.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class SYMStringUtil
{

	/**
	 * ���ڿ��� NULL�϶� ������� �����Ѵ�. Trim()�����.
	 * @param str
	 * @return
	 */
	public static String notNullString(String str)
	{
		return (str == null) ? "" : str.trim();
	}

	/**
	 * �� �� ����Ʈ���� ����Ͽ� ������.
	 * @param str
	 * @return
	 * @author s.j park
	 */
	public static final int getByteSizeToComplex(String str)
	{
		int en = 0;
		int ko = 0;
		int etc = 0;

		char[] string = str.toCharArray();

		for (int j = 0; j < string.length; j++)
		{
			if (string[j] >= 'A' && string[j] <= 'z')
			{
				en++;
			} else if (string[j] >= '\uAC00' && string[j] <= '\uD7A3')
			{
				ko++;
				ko++;
			}
			// ������ ������ ���� ��쿡 ���� üũ �߰�
			else if (string[j] >= '\u3131' && string[j] <= '\u3163')
			{
				ko++;
				ko++;
			} else
			{
				etc++;
			}
		}
		return (en + ko + etc);
	}

	/**
	 * ���ް��� ����Ʈ ����ŭ �߶� ������.
	 * @param str
	 * @param len
	 * @return
	 */
	public static String getStringByteSizeToComplex(String str, int len)
	{
		int maxLen = 0;

		ArrayList<Character> alRet = new ArrayList<Character>();
		char[] string = str.toCharArray();
		for (int j = 0; j < string.length; j++)
		{

			if (maxLen >= len)
			{
				break;
			}
			if (string[j] >= 'A' && string[j] <= 'z')
			{
				maxLen++;

			} else if (string[j] >= '\uAC00' && string[j] <= '\uD7A3')
			{
				if (maxLen + 2 > len)
				{
					break;
				}
				maxLen += 2;
			} else if (string[j] >= '\u3131' && string[j] <= '\u3163')
			{
				if (maxLen + 2 > len)
				{
					break;
				}
				maxLen += 2;
			} else
			{
				maxLen++;
			}
			alRet.add(string[j]);
		}
		char[] retChar = new char[alRet.size()];
		for (int i = 0; i < alRet.size(); i++)
		{
			retChar[i] = ((Character) alRet.get(i)).charValue();
		}
		return new String(retChar);
	}

	/**
	 * Ư������ ���� �����ϴ� �޼ҵ�
	 * @param temp
	 * @param c
	 * @return int
	 * @author Jeahun Lee
	 */
	public static int getNumberSpecialWord(String temp, char c)
	{
		int intNum = 0;
		for (int i = 0; i < temp.length(); i++)
		{
			if (temp.charAt(i) == c)
			{
				intNum++;
			}
		}
		return intNum;
	}

	/**
	 * ���ҽ� ���鿡�� Ű���� �о ������
	 * @param RESOURCE_BUNDLE
	 * @param paramString
	 * @return
	 */
	public static String getString(ResourceBundle RESOURCE_BUNDLE, String paramString)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(paramString);
		} catch (MissingResourceException localMissingResourceException)
		{
		}
		return '!' + paramString + '!';
	}

	/**
	  * ���ڿ��� ����Ʈ������ üũ�Ͽ� �ڸ���, ���ϴ� ���ڿ��� �����δ�.
	  * @param str - ��� ���ڿ�
	  * @param i - �ڸ������ ��ġ
	  * @param trail - �����̰����ϴ� ���ڿ�
	  * @return String - ó���� ������ڿ�
	  */
	public static String cropByte(String str, int i, String trail)
	{
		if (str == null || "".equals(str))
			return "";
		String tmp = str;
		int slen = 0, blen = 0;
		boolean flag = false;
		char c;
		while (blen < i)
		{
			c = tmp.charAt(slen);
			//2011-03-24 �ڼ�ȣ
			//���ϴ� byte ����ŭ ���ڸ� �ڸ��� �κ�...
			//�������� 1byte�� �� �߸��� ��� �߻���.
//            blen++;
//            slen++;
//            if (c > 127)
//                blen++; // 2-byte character..
			int cByte = String.valueOf(c).getBytes().length;
			if ((cByte + blen) > i)
			{
				break;
			}
			blen += cByte;
			slen++;
			//-������.
			if (tmp.substring(slen).equals(""))
			{
				flag = true;
				break;
			}
		}
		tmp = tmp.substring(0, slen);
		if (!flag)
			tmp += trail;
		return tmp;
	}

	/**
	 * 2���� �迭 ��ġ�� - distArr�� srcArr�� ������ ��� �����Ѵ�.
	 * @param distArr
	 * @param srcArr
	 * @return
	 */
	public static String[] addToArray(String[] distArr, String[] srcArr)
	{
		return addToArray(distArr, srcArr, "");
	}

	/**
	 * 2���� �迭�� ��ġ�� - prefixStr�� �Ѿ�� ���� �̿��Ͽ� ��ġ�� ���� ���ڸ� ������ �� ����.
	 * @param distArr
	 * @param srcArr
	 * @param prefixStr
	 * @return
	 */
	public static String[] addToArray(String[] distArr, String[] srcArr, String prefixStr)
	{
		ArrayList<String> al = new ArrayList<String>();
		for (int i = 0; i < distArr.length; i++)
		{
			al.add(distArr[i]);
		}
		for (int i = 0; i < srcArr.length; i++)
		{
			if (!al.contains(srcArr[i]))
			{
				al.add(prefixStr + srcArr[i]);
			}
		}
		return al.toArray(new String[al.size()]);
	}

	/**
	 * 2���� List��ġ��.
	 * @param distArr
	 * @param srcArr
	 * @return
	 */
	public static String[] addToArray(String[] distArr, ArrayList<String> srcArr)
	{
		return addToArray(distArr, srcArr.toArray(new String[srcArr.size()]), "");
	}

	/**
	 * 2���� List��ġ�� - prefixStr�� �Ѿ�� ���� �̿��Ͽ� ��ġ�� ���� ���ڸ� ������ �� ����. 
	 * @param distArr
	 * @param srcArr
	 * @param prefixStr
	 * @return
	 */
	public static String[] addToArray(String[] distArr, ArrayList<String> srcArr, String prefixStr)
	{
		return addToArray(distArr, srcArr.toArray(new String[srcArr.size()]), prefixStr);
	}

	/**
	 * ����Ʈ���� ���ڿ� ����
	 * @param arr
	 * @return
	 */
	public static String getStringToArray(ArrayList<String> arr)
	{
		return getStringToArray(arr.toArray(new String[arr.size()]), null);
	}

	/**
	 * ����Ʈ���� ���ڿ� ���� - ����� Ư�����ڸ� �̿��� WRAPPING�۾� �ʿ�� �����.
	 * @param arr
	 * @param wrapStr
	 * @return
	 */
	public static String getStringToArray(ArrayList<String> arr, String wrapStr)
	{
		return getStringToArray(arr.toArray(new String[arr.size()]), wrapStr);
	}

	/**
	 * �迭���� ���ڿ� ����
	 * @param arr
	 * @return
	 */
	public static String getStringToArray(String[] arr)
	{
		return getStringToArray(arr, null);
	}

	/**
	 * �迭���� ���ڿ� ���� - ����� Ư�����ڸ� �̿��� WRAPPING�۾� �ʿ�� �����.
	 * @param arr
	 * @param wrapStr
	 * @return
	 */
	public static String getStringToArray(String[] arr, String wrapStr)
	{
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < arr.length; i++)
		{
			sb.append(arr[i]);
			if (i < arr.length - 1)
			{
				if (wrapStr != null)
				{
					sb.append(wrapStr).append(", ").append(wrapStr);
				} else
				{
					sb.append(", ");
				}
			}
		}
		if (arr.length > 0 && wrapStr != null)
		{
			return wrapStr + sb.toString() + wrapStr;
		} else
		{
			return sb.toString();
		}
	}

	/**
	 * MyBatis �������� ����� ���ڿ� ���� aaa', 'bbb', 'ccc �� ���� ������.
	 * @param arr
	 * @return
	 */
	public static String getQueryInStringToArray(ArrayList<String> arr)
	{
		return getQueryInStringToArray(arr.toArray(new String[arr.size()]));
	}

	/**
	 * MyBatis�� ���ؼ� In������ �� �� �����Ƿ� �Ѱ��϶��� '�� ������ �ʰ� 2�� �̻��� ���� '�� ���̵� ó���� ���� '�� �����.
	 * @param arr
	 * @return
	 */
	public static String getQueryInStringToArray(String[] arr)
	{
		if (arr.length == 1)
		{
			return arr[0];
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; i++)
		{
			sb.append("'").append(arr[i]).append("'");
			if (i < arr.length - 1)
			{
				sb.append(",");
			}
		}
		return sb.toString().substring(1, sb.toString().length() - 1);
	}

	/**
	 * TCException�� �޾Ƽ� �������ڿ� ������.
	 * @param e
	 * @return
	 */
	public static String getErrorString(TCException e)
	{
		String[] errors = e.errorStrings;
		StringBuffer retStr = new StringBuffer().append(errors[0]);
		for (int i = 1; i < errors.length; i++)
		{
			retStr.append("\r\n").append(errors[i]);
		}
		if (errors.length > 1)
		{
			return "[" + retStr.toString() + "]";
		} else
		{
			return retStr.toString();
		}
	}

	/**
	 * ���� �߼۽� ���Ǵ� �޼ҵ� �⺻ ���̾ƿ� HTML�� �����Ѵ�.
	 * @param contents
	 * @return
	 */
	public static String getSendMailString(String contents)
	{
		try
		{
			FileInputStream fis = new FileInputStream(TcDefinition.getWorkFlowTemplateDir() + "script.html.template");
			InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(reader);
//	         BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(TcDefinition.getWorkFlowTemplateDir() + "script.html.template"),"UTF-8"));
			String tmpLine;
			String fullContents = "";
			while ((tmpLine = br.readLine()) != null)
			{
				if (tmpLine.trim().equals("${contents}"))
					tmpLine = contents;
				fullContents += tmpLine;
			}
			br.close();
			reader.close();
			fis.close();

			fullContents = fullContents.replace("\r", "");
			fullContents = fullContents.replaceAll("&nbsp;", "");
			fullContents = fullContents.replace("</textarea>", "");
			fullContents = fullContents.replace("<textarea id=\"content1\" name=\"content\" style=\"width:100%;height:100%;visibility:hidden;\">", "");
			fullContents = fullContents.replace("<script charset=\"utf-8\" src=\"./addscript.js\"></script>", "");
			fullContents = fullContents.replace("<script charset=\"utf-8\" src=\"./kindeditor.js\"></script>", "");
			fullContents = fullContents.replaceAll("&lt;", "<");
			fullContents = fullContents.replaceAll("lt;", "<");
			fullContents = fullContents.replaceAll("&gt;", ">");
			fullContents = fullContents.replaceAll("gt;", ">");
			System.out.println(fullContents);

			return fullContents;

		} catch (Exception e)
		{
			MessageBox.post(e);
		}
		return contents;
	}

	/**
	 * MyBatis�� ���� ���۽� ������ ���ڿ��� �����Ѵ�.
	 * @param content
	 * @return
	 */
	public static String convertForQuery(String content)
	{
//		 content=content.replaceAll("&","&amp;");
		content = content.replaceAll("<", "&lt;");
		content = content.replaceAll("'", "&acute;");
		content = content.replaceAll(">", "&gt;");
		content = content.replaceAll("\"", "&cute;");
//		 content=content.replaceAll("\n", "<br>");
//		 content=content.replaceAll(" ", "&nbsp;"); 
		return content;
	}

	/**
	 * �˻��� TC���� ���Ǵ� ���ϵ� ���ڸ� DB�� ���ϵ� ���ڷ� �������ش�.
	 * @param content
	 * @return
	 */
	public static String queryString(String content)
	{
		if (content == null)
			return "";
		return convertForQuery(content.trim()).replaceAll("[*]", "%").replaceAll("[?]", "_");
	}

	/**
	 * Sring�� �������� �������� �Ǵ��ϴ� �޼ҵ�
	 * @param itemRevisionId
	 * @return
	 * boolean
	 */
	public static boolean isNumber(String temp)
	{
		boolean check = true;
		for (int i = 0; i < temp.length(); i++)
		{
			if (!Character.isDigit(temp.charAt(i)))
			{
				check = false;
				break;
			}
		}
		return check;
	}

	/**
	 * ���ڸ� �����ϴ� �޼ҵ�
	 * @param str
	 * @return
	 */
	public static int getExtractNumber(String str)
	{
		Pattern pattern = Pattern.compile("([^0-9])");
		Matcher matcher = pattern.matcher(str);
		String temp = matcher.replaceAll("");
		if (temp.length() == 0)
		{
			return 0;
		} else
		{
			return Integer.parseInt(temp);
		}

	}
	
	/**
	 * SRME:: [][20140708] swyoon �ɼ� Size Check.
	 */		
	public static int getConvertedLength(String condition){
		
		return convertToSimple(condition).length();
	}
	
	// [CSH]4000 byte �߰����� ���� ����.
	public static int getConvertedLength_(String condition){
		
		String str = convertToSimple(condition);
		
		return str.replace("@", " OR ").length();
	}
	
	/**
	 * SRME:: [][20140811] swyoon TC Condition ==> H-BOM Type Condition ���� ����.
	 */		
	public static String convertToSimple(String condition){
		
		if( condition == null){
			condition = "";
		}
		
        //[SR141218-015][2014-12-18] ymjang, �ɼǿ� "'" ���� ���Ե� ���, Option Converting ����
		// F920Y2012:'3W1' = "3W17" and F920Y2012:'3WD' = "3WDD" ... �� ���, Pattern Match ���� ������.
		condition = condition.replaceAll("'", "");

		String result = "";
		Pattern p = Pattern.compile("F\\w*:\\w{3} = ");
		Matcher m = p.matcher(condition.trim());
		while (m.find()) {
			System.out.println(m.start() + " " + m.group());
			result = m.replaceAll("");
			System.out.println("result : " + result);
		}
		
		if( result != null ){
			result = result.replaceAll("\"", "");
			result = result.replaceAll("  or ", " or ");
			result = result.replaceAll(" or  ", " or ");
			result = result.replaceAll(" or ", "@");
			
			return result; 
		}
		
		return result;
	}	
}
