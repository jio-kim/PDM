package com.symc.plm.rac.prebom.dcs.common;

import java.io.File;

public class DCSFileUtil {

	public DCSFileUtil() {

	}

	public static boolean isFile(String str) {
		boolean isFile = false;

		File file = new File(str);
		if (file.exists() && file.isFile()) {
			isFile = true;
		}

		return isFile;
	}

	public static String getFileName(String filePath) {
		File file = new File(filePath);
		if (file != null) {
			String fileName = file.getName();

			if (file.isDirectory()) {
				return fileName;
			} else {
				int lastIndex = fileName.lastIndexOf(".");
				if (lastIndex > 0) {
					return fileName.substring(0, lastIndex);
				}
			}
		}

		return null;
	}

	public static String getFileExtension(String filePath) {
		String extension = null;

		int lastIndexOf = filePath.lastIndexOf(".");
		if (lastIndexOf != -1) {
			extension = filePath.substring(lastIndexOf);
		}

		return extension;
	}

}
