package com.fg114.main.util;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import com.fg114.main.app.*;


/**
 * 加密解密处理
 * @author wfc
 */
public class CipherUtils {

	/**
	 * 小秘书加密
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static String encodeXms(String content) throws Exception {
		 byte[] rawKey = toBytes(Settings.KK);
		 byte[] result = AES.encrypt(rawKey, content.getBytes("UTF-8"));
		 return toHexString(result);
	}

	/**
	 * byte数组转为16进制字符串
	 * @param input
	 * @return
	 */
	public static String toHexString(byte[] input) {
		StringBuffer sb = new StringBuffer(input.length);
		String sTemp;
		for (int i = 0; i < input.length; i++) {
			sTemp = Integer.toHexString(0xFF & input[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}
	
	/**
	 * 16进制字符串转为byte数组
	 * @param hex
	 * @return
	 */
	public static byte[] toBytes(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * MD5加密
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static byte[] toMd5(String input) throws Exception {
		return toMd5(input, "UTF-8");
	}

	/**
	 * MD5加密
	 * @param input
	 * @param encoding
	 * @throws Exception
	 */
	public static byte[] toMd5(String input, String encoding) throws Exception {
		byte[] data = input.getBytes(encoding);
		return toMd5(data);
	}

	/**
	 * MD5加密
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] toMd5(byte[] data) throws NoSuchAlgorithmException {
		byte[] messageDigest;
		MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
		digest.update(data);
		messageDigest = digest.digest();
		return messageDigest;
	}

	/**
	 * 用MD5处理过的Key做AES加密
	 * @param seed
	 * @param clearText
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptAesByMd5Key(byte[] seed, byte[] clearText) throws Exception {
		return AES.encryptByMd5Key(seed, clearText);
	}

	/**
	 * AES加密
	 * @param seed
	 * @param cleartText
	 * @return
	 * @throws Exception
	 */
	public static String encryptAes(String seed, String cleartText) throws Exception {
		return AES.encrypt(seed, cleartText);
	}

	/**
	 * AES加密
	 * @param seed
	 * @param clearText
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptAes(byte[] seed, byte[] clearText) throws Exception {
		return AES.encrypt(seed, clearText);
	}

	/**
	 * 用MD5处理过的Key做AES解密
	 * @param seed
	 * @param encryptedText
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptAesByMd5Key(byte[] seed, byte[] encryptedText) throws Exception {
		return AES.decryptByMd5Key(seed, encryptedText);
	}

	/**
	 * AES解密
	 * @param seed
	 * @param encryptedText
	 * @return
	 * @throws Exception
	 */
	public static String decryptAes(String seed, String encryptedText) throws Exception {
		return AES.decrypt(seed, encryptedText);
	}

	/**
	 * AES解密
	 * @param seed
	 * @param encryptedText
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptAes(byte[] seed, byte[] encryptedText) throws Exception {
		return AES.decrypt(seed, encryptedText);
	}

	/**
	 * 转为Base64字符串
	 * @param input
	 * @return
	 */
	public static String toBase64(byte[] input) {
		return Base64.encodeToString(input, Base64.DEFAULT);
	}

	/**
	 * 转为Base64字符串
	 * @param input
	 * @return
	 */
	public static String toBase64(byte[] input, int flags) {
		return Base64.encodeToString(input, flags);
	}

	/**
	 * 将Base64字符串解码
	 * @param input
	 * @return
	 */
	public static byte[] fromBase64(byte[] input) {
		return Base64.decode(input, Base64.DEFAULT);
	}

	/**
	 * 将Base64字符串解码
	 * @param input
	 * @return
	 */
	public static byte[] fromBase64(byte[] input, int flags) {
		return Base64.decode(input, flags);
	}

	static class AES {

		/**
		 * 用MD5处理过的Key做加密
		 * @param seed
		 * @param clearText
		 * @return
		 * @throws Exception
		 */
		public static byte[] encryptByMd5Key(byte[] seed, byte[] clearText) throws Exception {
			byte[] rawKey = toMd5(seed);
			String key = toHexString(rawKey);
			byte[] result = encrypt(rawKey, clearText);
			return result;
		}

		/**
		 * 用MD5处理过的Key做解密
		 * @param seed
		 * @param encryptedText
		 * @return
		 * @throws Exception
		 */
		public static byte[] decryptByMd5Key(byte[] seed, byte[] encryptedText) throws Exception {
			byte[] rawKey = toMd5(seed);
			byte[] result = decrypt(rawKey, encryptedText);
			return result;
		}

		/**
		 * 使用AES加密
		 * @param seed
		 * @param clearText
		 * @return
		 * @throws Exception
		 */
		public static String encrypt(String seed, String clearText) throws Exception {
			byte[] rawKey = getRawKey(seed.getBytes("UTF-8"));
			byte[] result = encrypt(rawKey, clearText.getBytes("UTF-8"));
			return toHexString(result);
		}

		/**
		 * 使用AES加密
		 * @param raw
		 * @param clearText
		 * @return
		 * @throws Exception
		 */
		public static byte[] encrypt(byte[] raw, byte[] clearText) throws Exception {
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encrypted = cipher.doFinal(clearText);
			return encrypted;
		}

		/**
		 * 使用AES解密
		 * @param seed
		 * @param encryptedText
		 * @return
		 * @throws Exception
		 */
		public static String decrypt(String seed, String encryptedText) throws Exception {
			byte[] rawKey = getRawKey(seed.getBytes("UTF-8"));
			byte[] enc = toBytes(encryptedText);
			byte[] result = decrypt(rawKey, enc);
			return new String(result, "UTF-8");
		}

		/**
		 * 使用AES解密
		 * @param raw
		 * @param encryptedText
		 * @return
		 * @throws Exception
		 */
		public static byte[] decrypt(byte[] raw, byte[] encryptedText) throws Exception {
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] decrypted = cipher.doFinal(encryptedText);
			return decrypted;
		}

		/**
		 * 根据Seed生成Key
		 * @param seed
		 * @return
		 * @throws Exception
		 */
		private static byte[] getRawKey(byte[] seed) throws Exception {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(seed);
			kgen.init(128, sr); // 192 and 256 bits may not be available
			SecretKey skey = kgen.generateKey();
			byte[] raw = skey.getEncoded();
			return raw;
		}
	}
}
