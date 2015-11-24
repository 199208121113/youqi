package com.lg.base.utils;

import android.util.Base64;

import java.io.Serializable;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256Cipher implements Serializable {

    private static final long serialVersionUID = 1L;

    public static byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public static String AES_Encode(String str, String key) {
        String enc = "";
        try {
            byte[] textBytes = str.getBytes("UTF-8");
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
            enc = Base64.encodeToString(cipher.doFinal(textBytes), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enc;
    }

    public static String AES_Decode(String str, String key) {
        String dec = "";
        try {
            byte[] textBytes = Base64.decode(str, 0);
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
            dec = new String(cipher.doFinal(textBytes), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dec;
    }
}