package com.ssangyong.common.remote;

import java.security.Key;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;


public class SecurityUtil {
	
	private static String keyString = "I love Tivoly!!";

	private static String key() {
		return keyString;
	}

	private static Key getKey() throws Exception {
		return (key().length() == 24) ? getKey2(key()) : getKey1(key());
	}

	private static Key getKey1(String keyValue) throws Exception {
		DESKeySpec desKeySpec = new DESKeySpec(keyValue.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		Key key = keyFactory.generateSecret(desKeySpec);
		return key;
	}

	private static Key getKey2(String keyValue) throws Exception {
		DESedeKeySpec desKeySpec = new DESedeKeySpec(keyValue.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
		Key key = keyFactory.generateSecret(desKeySpec);
		return key;
	}

	public static String encrypt(String ID) throws Exception {
		if (ID == null || ID.length() == 0)
			return "";

		String instance = (key().length() == 24) ? "DESede/ECB/PKCS5Padding"
				: "DES/ECB/PKCS5Padding";
		javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(instance);
		cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getKey());
		String amalgam = ID;
		byte[] inputBytes1 = amalgam.getBytes("UTF8");
		byte[] outputBytes1 = cipher.doFinal(inputBytes1);

		String outputStr1 = new String(BASE64EncoderStream.encode(outputBytes1));
		return outputStr1;
	}

	public static String decrypt(String codedID) throws Exception {
		if (codedID == null || codedID.length() == 0)
			return "";

		String instance = (key().length() == 24) ? "DESede/ECB/PKCS5Padding"
				: "DES/ECB/PKCS5Padding";
		javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(instance);
		cipher.init(javax.crypto.Cipher.DECRYPT_MODE, getKey());

		byte[] inputBytes1 = BASE64DecoderStream.decode(codedID.getBytes());
		byte[] outputBytes2 = cipher.doFinal(inputBytes1);
		String strResult = new String(outputBytes2, "UTF8");
		return strResult;
	}

}
