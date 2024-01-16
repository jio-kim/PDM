package com.ssangyong.common.utils;

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
	 * 문자열이 NULL일때 빈공백을 리턴한다. Trim()적용됨.
	 * @param str
	 * @return
	 */
	public static String notNullString(String str)
	{
		return (str == null) ? "" : str.trim();
	}

	/**
	 * 값 의 바이트수를 계산하여 돌려줌.
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
			// 자음과 모음만 있을 경우에 대한 체크 추가
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
	 * 전달값을 바이트 수만큼 잘라서 리턴함.
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
	 * 특수문자 갯수 리턴하는 메소드
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
	 * 리소스 번들에서 키값을 읽어서 리턴함
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
	  * 문자열을 바이트단위로 체크하여 자르고, 원하는 문자열을 덧붙인다.
	  * @param str - 대상 문자열
	  * @param i - 자르고싶은 위치
	  * @param trail - 덧붙이고자하는 문자열
	  * @return String - 처리후 결과문자열
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
			//2011-03-24 박세호
			//원하는 byte 수만큼 글자를 자르는 부분...
			//기존에는 1byte씩 덜 잘리는 경우 발생함.
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
			//-요기까지.
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
	 * 2개의 배열 합치기 - distArr에 srcArr이 존재할 경우 제외한다.
	 * @param distArr
	 * @param srcArr
	 * @return
	 */
	public static String[] addToArray(String[] distArr, String[] srcArr)
	{
		return addToArray(distArr, srcArr, "");
	}

	/**
	 * 2개의 배열을 합치기 - prefixStr로 넘어온 값을 이용하여 합치기 전에 문자를 변형할 수 있음.
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
	 * 2개의 List합치기.
	 * @param distArr
	 * @param srcArr
	 * @return
	 */
	public static String[] addToArray(String[] distArr, ArrayList<String> srcArr)
	{
		return addToArray(distArr, srcArr.toArray(new String[srcArr.size()]), "");
	}

	/**
	 * 2개의 List합치기 - prefixStr로 넘어온 값을 이용하여 합치기 전에 문자를 변형할 수 있음. 
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
	 * 리스트에서 문자열 추출
	 * @param arr
	 * @return
	 */
	public static String getStringToArray(ArrayList<String> arr)
	{
		return getStringToArray(arr.toArray(new String[arr.size()]), null);
	}

	/**
	 * 리스트에서 문자열 추출 - 추출시 특수문자를 이용한 WRAPPING작업 필요시 사용함.
	 * @param arr
	 * @param wrapStr
	 * @return
	 */
	public static String getStringToArray(ArrayList<String> arr, String wrapStr)
	{
		return getStringToArray(arr.toArray(new String[arr.size()]), wrapStr);
	}

	/**
	 * 배열에서 문자열 추출
	 * @param arr
	 * @return
	 */
	public static String getStringToArray(String[] arr)
	{
		return getStringToArray(arr, null);
	}

	/**
	 * 배열에서 문자열 추출 - 추출시 특수문자를 이용한 WRAPPING작업 필요시 사용함.
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
	 * MyBatis 쿼리문에 사용할 문자열 조작 aaa', 'bbb', 'ccc 와 같이 리턴함.
	 * @param arr
	 * @return
	 */
	public static String getQueryInStringToArray(ArrayList<String> arr)
	{
		return getQueryInStringToArray(arr.toArray(new String[arr.size()]));
	}

	/**
	 * MyBatis를 통해서 In구문에 들어갈 수 있으므로 한개일때는 '을 붙이지 않고 2개 이상일 경우는 '을 붙이되 처음과 끝의 '는 지운다.
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
	 * TCException을 받아서 에러문자열 리턴함.
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
	 * 메일 발송시 사용되는 메소드 기본 레이아웃 HTML을 리턴한다.
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
	 * MyBatis로 쿼리 전송시 문제될 문자열을 변경한다.
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
	 * 검색시 TC에서 사용되는 와일드 문자를 DB의 와일드 문자로 변경해준다.
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
	 * Sring이 숫자인지 문자인지 판단하는 메소드
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
	 * 숫자만 추출하는 메소드
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
	 * SRME:: [][20140708] swyoon 옵션 Size Check.
	 */		
	public static int getConvertedLength(String condition){
		
		return convertToSimple(condition).length();
	}
	
	// [CSH]4000 byte 중간과정 기준 적용.
	public static int getConvertedLength_(String condition){
		
		String str = convertToSimple(condition);
		
		return str.replace("@", " OR ").length();
	}
	
	/**
	 * SRME:: [][20140811] swyoon TC Condition ==> H-BOM Type Condition 으로 변경.
	 */		
	public static String convertToSimple(String condition){
		
		if( condition == null){
			condition = "";
		}
		
        //[SR141218-015][2014-12-18] ymjang, 옵션에 "'" 문자 포함된 경우, Option Converting 오류
		// F920Y2012:'3W1' = "3W17" and F920Y2012:'3WD' = "3WDD" ... 인 경우, Pattern Match 에서 누락됨.
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
