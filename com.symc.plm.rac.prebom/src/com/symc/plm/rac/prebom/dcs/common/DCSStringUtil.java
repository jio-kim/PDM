package com.symc.plm.rac.prebom.dcs.common;

public class DCSStringUtil {

	public DCSStringUtil() {

	}

	public static boolean isNumber(String str) {
		boolean isNumber = false;

		try {
			Integer.parseInt(str);

			isNumber = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isNumber;
	}

	public static boolean isDouble(String str) {
		boolean isDouble = false;
		
		// [20151222][ymjang] NumberFormatException 오류 수정
		if (isEmpty(str)) return false;
		
		try {
			Double.parseDouble(str);
			
			isDouble = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isDouble;
	}

	public static boolean isEmpty(String str) {
		if (str == null || str.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isExceeded(String temp, int length) {
		String[] tempArr = getParseStringByBytes(temp, length);
		if (tempArr.length > 1) {
			return true;
		}

		return false;
	}

	public static String[] getParseStringByBytes(String str, int length) {
		if (str == null) {
			return null;
		}

		String[] ary = null;
		try {
			byte[] strBytes = str.getBytes("UTF-8");
			int strLength = strBytes.length;

			int index = 0;
			int minus_byte_num = 0;
			int offset = 0;

			int hangul_byte_num = 3;

			if (strLength > length) {
				int aryLength = (strLength / length) + (strLength % length != 0 ? 1 : 0);
				ary = new String[aryLength];

				for (int i = 0; i < aryLength; i++) {
					minus_byte_num = 0;
					offset = length;
					if (index + offset > strBytes.length) {
						offset = strBytes.length - index;
					}

					for (int j = 0; j < offset; j++) {
						if (((int) strBytes[index + j] & 0x80) != 0) {
							minus_byte_num++;
						}
					}

					if (minus_byte_num % hangul_byte_num != 0) {
						offset -= minus_byte_num % hangul_byte_num;
					}

					ary[i] = new String(strBytes, index, offset, "UTF-8");
					index += offset;
				}
			} else {
				ary = new String[] { str };
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ary;
	}

}
