package com.kgm.soa.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Clob;
import java.sql.SQLException;

public class StringUtil {
    public static String getStackTraceString(Exception ex) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pinrtStream = new PrintStream(out);
        ex.printStackTrace(pinrtStream);
        return out.toString();
    }

    /**
     * String 문자가 null 이면 "" 리턴 String 문자가 "null" 이면 "" 리턴 String 문자가 있으면 trim
     * 처리 후 리턴
     *
     * @method nullToString
     * @param
     * @return
     * @exception
     * @throws
     * @see
     */
    public static String nullToString(String str) {
        if (str == null) {
            return "";
        } else {
            if ("null".equals(str.trim())) {
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

    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim()))
            return true;
        else
            return false;
    }

    public static String clobToString(Clob clob) throws SQLException, IOException {
		if (clob == null) {
			return "";
		}
		StringBuffer strOut = new StringBuffer();
		String str = "";
		BufferedReader br = new BufferedReader(clob.getCharacterStream());
		while ((str = br.readLine()) != null) {
			strOut.append(str);
		}
		return strOut.toString();
	}
}
