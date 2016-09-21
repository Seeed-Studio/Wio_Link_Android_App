package cc.seeed.iot.util;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

/**
 * MS5
 */
public class EncryptUtil {
    // 全局数组
    private final static String[] strDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    //md5 加密
    public static String md5(String str) {
        String encode = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            encode = byteToString(md.digest(str.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        System.out.println("md5: " + encode);
        return encode;
    }


    /**
     * BASE64解密
     * @param key
     * @return
     * @throws Exception
     */
	public static String decryptBASE64(String key){
//    	String encode = byteToString((new BASE64Decoder()).decodeBuffer(key));
		String encode = "";
		try {
			encode = new String((new BASE64Decoder()).decodeBuffer(key),"GB2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("解密BASE64: "+encode);
		return encode;
	}

    /**
     * BASE64加密
     *
     * @return
     * @throws Exception
     */
    public static String encryptBASE64(byte[] key){
		String encode = (new BASE64Encoder()).encodeBuffer(key);
		return encode;
	}

    // 转换字节数组为16进制字串
    private static String byteToString(byte[] bByte) {
        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < bByte.length; i++) {
            sBuffer.append(byteToArrayString(bByte[i]));
        }
        return sBuffer.toString();
    }

    // 返回形式为数字跟字符串
    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        // System.out.println("iRet="+iRet);
        if (iRet < 0) {
            iRet += 256;
        }
        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }

    /**
     * 加密
     * @param data
     * @param key
     * @return
     */
    public static String encrypt(String data, String key) {
        key = md5(key);
        Log.d("TAG","md5: "+key);
        int x = 0;
        int len = data.length();
        int lkey = key.length();
        String ch = "";
        String str = "";
        for (int i = 0;i < len;i++){
            if (x == lkey){
                x = 0;
            }
            ch +=  key.charAt(x);
            x++;
        }


        for (int i = 0 ; i < len ;i++){
            str += (char)(((int)(data.charAt(i))+((int)(ch.charAt(i))))%128);
        }
        Log.d("TAG","str: "+str);
        return encryptBASE64(str.getBytes());
    }

    /**
     * 解密
     * @param data
     * @param key
     * @return
     */
    public static String decrypt(String data,String key){
        key = md5(key);
        int x = 0;
        data = decryptBASE64(data);
        int len = data.length();
        int lkey = key.length();
        String ch = "";
        String str = "";
        for (int i = 0;i < len ;i++){
            if (x == lkey){
                x = 0;
            }
            ch += key.substring(x,x+1);
            x++;
        }
        for (int i = 0;i < len ;i++){
            if ((int)(data.charAt(i)) < (int)(ch.charAt(i))){
                str += (char)((int)(data.charAt(i))+128 - (int)(ch.charAt(i)));
            }else {
                str += (char)((int)(data.charAt(i)) - (int)(ch.charAt(i)));
            }
        }

        return str;
    }

    public static String SHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
