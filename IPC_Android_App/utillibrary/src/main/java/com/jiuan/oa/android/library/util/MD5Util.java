package com.jiuan.oa.android.library.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5处理工具类
 */
public final class MD5Util {

    private MD5Util() {
    }

    public static final String UTF_8 = "UTF-8";

    public static final String ISO_8859_1 = "ISO-8859-1";

    public static final String US_ASCII = "US-ASCII";

    public static final String UTF_16BE = "UTF-16BE";

    public static final String UTF_16LE = "UTF-16LE";

    public static final char HEX_DIGITS_CAPITAL[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final char HEX_DIGITS_LOWER[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final int BUFFER_SIZE = 8 * 1024;

    public static String get32MD5Lower(String s, String charsetName) {
        return getMD5(s, 0, 16, HEX_DIGITS_LOWER, charsetName);
    }

    public static String get32MD5Lower(String s) {
        return get32MD5Lower(s, null);
    }

    public static String get32MD5Lower(File file) {
        return getMD5(file, 0, 16, HEX_DIGITS_LOWER);
    }

    public static String get32MD5Capital(String s, String charsetName) {
        return getMD5(s, 0, 16, HEX_DIGITS_CAPITAL, charsetName);
    }

    public static String get32MD5Capital(String s) {
        return get32MD5Capital(s, null);
    }

    public static String get32MD5Capital(File file) {
        return getMD5(file, 0, 16, HEX_DIGITS_CAPITAL);
    }

    public static String get16MD5Lower(String s, String charsetName) {
        return getMD5(s, 4, 12, HEX_DIGITS_LOWER, charsetName);
    }

    public static String get16MD5Lower(String s) {
        return get16MD5Lower(s, null);
    }

    public static String get16MD5Lower(File file) {
        return getMD5(file, 4, 12, HEX_DIGITS_LOWER);
    }

    public static String get16MD5Capital(String s, String charsetName) {
        return getMD5(s, 4, 12, HEX_DIGITS_CAPITAL, charsetName);
    }

    public static String get16MD5Capital(String s) {
        return get16MD5Capital(s, null);
    }

    public static String get16MD5Capital(File file) {
        return getMD5(file, 4, 12, HEX_DIGITS_CAPITAL);
    }

    public static String getMD5(File file, int start, int end, char hexDigits[]) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[BUFFER_SIZE];
            int byteCount;
            while ((byteCount = bufferedInputStream.read(buffer, 0,
                    buffer.length)) > 0) {
                mdTemp.update(buffer, 0, byteCount);
            }
            bufferedInputStream.close();
            return hexDigits(mdTemp.digest(), start, end, hexDigits);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String hexDigits(byte[] md, int start, int end, char hexDigits[]) {
        /* 创建一个无符号型字符数组，因为要保存高低2个部分，所以乘以2 */
        char str[] = new char[(end - start) * 2];
        int k = 0;
        for (int i = start; i < end; i++) {
            byte b = md[i];
            /* 进行双字节加密 */
            /* 取字节中高4位,右移4位,高位无符号位 */
            str[k++] = hexDigits[(b & 0xf0) >>> 4];
            /* 取字节中低4位 */
            str[k++] = hexDigits[b & 0xf];
        /* 举例:当[b&0xf]内容等于0001时,转换为10进制为1,对应hexDigits[1]其值为1 */
        }
        return new String(str);
    }

    /**
     * 判断两个MD5值是否相同
     */
    public static boolean isEqual(byte[] digestA, byte[] digestB) {
        return MessageDigest.isEqual(digestA, digestB);
    }

    private static String getMD5(String s, int start, int end, char hexDigits[], String charsetName) {
        try {
            /* android中默认编码格式为"UTF-8" */
            byte[] strTemp = null;
            if (charsetName == null) {
                charsetName = "UTF-8";
            }
            try {
                strTemp = s.getBytes(charsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            /* 使用MD5创建MessageDigest对象 */
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            /* 更新MessageDigest对象使用指定的字节 */
            mdTemp.update(strTemp);
            /* 计算并将返回的值存到一个字节数组
               根据需求对该字节数组进行处理 */
            return hexDigits(mdTemp.digest(), start, end, hexDigits);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
