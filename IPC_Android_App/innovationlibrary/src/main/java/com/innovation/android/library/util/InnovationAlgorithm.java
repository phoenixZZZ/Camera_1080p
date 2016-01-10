package com.innovation.android.library.util;

import com.jiuan.oa.android.library.util.MD5Util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 创新平台加密算法
 */
public class InnovationAlgorithm {

    private InnovationAlgorithm() {
    }

    public static String SHA1(String salt, String password) {
        return SHA1(salt, password, null, null);
    }

    public static String SHA1(String salt, String password, String charsetNameMD5, String charsetNameSHA1) {
        try {
            String md5Lower = MD5Util.get32MD5Lower(salt + password, charsetNameMD5);
            if (charsetNameSHA1 == null) {
                charsetNameSHA1 = "UTF-8";
            }
            byte[] strTemp = null;
            try {
                strTemp = (md5Lower + password).getBytes(charsetNameSHA1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            MessageDigest mdTemp = MessageDigest.getInstance("SHA-1");
            mdTemp.update(strTemp);
            byte[] b = mdTemp.digest();
            return MD5Util.hexDigits(b, 0, b.length, MD5Util.HEX_DIGITS_LOWER);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
