package com.lg.base.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Created by 007 on 2015/1/27.
 */
public class FileUtil {
    public static boolean saveTextToFilePath(String fileFullPath, String text) throws Exception{
       return saveTextToFilePath(fileFullPath,text,false);
    }

    public static String getTextByFilePath(String fileFullPath) throws Exception{
        File ff = new File(fileFullPath);
        if(!ff.exists() || ff.length() == 0 ){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(ff),Charset.defaultCharset()));
            int rec = -1;
            char[] buf = new char[8192];
            while( (rec=br.read(buf,0,buf.length)) != -1){
                sb.append(buf,0,rec);
            }
        }finally{
            try {
                if(br != null) {
                    br.close();
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static boolean saveTextToFilePath(String fileFullPath, String text,boolean append) throws Exception{
        if(fileFullPath == null || fileFullPath.trim().length() == 0)
            throw new RuntimeException("fileFullPath is null");
        File tmpFile = new File(fileFullPath).getParentFile();
        if(tmpFile != null && (!tmpFile.exists())){
            IOUtil.mkDir(tmpFile.getAbsolutePath());
        }
        boolean writed = false;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileFullPath,append),Charset.defaultCharset()));
            bw.write(text);
            bw.flush();
            writed = true;
        }finally{
            try {
                if(bw != null) {
                    bw.close();
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return writed;
    }


    public static String getTextByInputStream(InputStream is,Charset charset) throws Exception{
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is,charset));
            int rec = -1;
            char[] buf = new char[8192];
            while( (rec=br.read(buf,0,buf.length)) != -1){
                sb.append(buf,0,rec);
            }
        }finally{
            try {
                if(br != null) {
                    br.close();
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
