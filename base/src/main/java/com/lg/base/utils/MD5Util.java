package com.lg.base.utils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by liguo on 2015/6/6.
 */
public class MD5Util {
    public static String toMd5(String string) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (Exception e) {
            //ignore
        }
        if(hash == null || hash.length == 0){
            return toMd5_2(string);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return StringUtil.toUpperCase(hex.toString());
    }

    public static String toMd5_2(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        String result = s;
        try {
            byte[] strTemp = s.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            result = new String(str);
        } catch (Exception e) {

        }
        if(StringUtil.isEmpty(result)){
            result = s;
        }
        return StringUtil.toUpperCase(result);
    }

    /** 获取文件md5值 */
    public static String toMd5(File file) throws Exception {
        String value = null;
        FileInputStream in = null;
        byte[] buffer = new byte[8192];
        try {
            in = new FileInputStream(file);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            /*MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            md5.update(byteBuffer);*/
            int n = -1;
            while((n = in.read(buffer,0,buffer.length)) != -1){
                md5.update(buffer,0,n);
            }
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return StringUtil.toUpperCase(value);
    }
}
