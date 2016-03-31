package com.lg.base.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.StatFs;

import com.lg.base.core.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class IOUtil {
	private static final String tag = "IOUtil";
	/**
	 * 检查是否存在外部存储器
	 * 
	 * @return
	 */
	public static boolean isExternalStorageExist() {
		String status = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(status);
	}

	public static String getRootDirectory() {
		return Environment.getRootDirectory().getAbsolutePath();
	}

	public static boolean rename(String fn, String newfn) {
		boolean b = true;
		try {
			b = new File(fn).renameTo(new File(newfn));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	/** 获取sd卡可用空间 */
	public static float getAvailCountForSDCard(String dir) {
		
		Double kb = -1d;
		if (dir != null && dir.trim().length() > 0) {
			if(dir.endsWith("/")){
				dir = dir.substring(0,dir.length()-1);
			}
			StatFs sf = new StatFs(dir);
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();
			long availCount = sf.getAvailableBlocks();
			LogUtil.d(tag, "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount / 1024 + "KB");
			LogUtil.d(tag, "可用的block数目：:" + availCount + ",剩余空间:" + availCount * blockSize / 1024 + "KB");
			kb = NumberUtil.formatNumber(availCount * 1f * blockSize / 1024);
		}
		return kb.floatValue();
	}

	/**
	 * 获取外部存储器路径
	 * 
	 * @return
	 * @throws java.io.FileNotFoundException
	 */
	public static String getExternalStoragePath() {
		if (!isExternalStorageExist())
			throw new RuntimeException("E000001");
		String esp = Environment.getExternalStorageDirectory().getPath();
		if (esp == null)
			throw new RuntimeException("E000002");
		if (!esp.startsWith("/"))
			esp = "/" + esp;
		if (!esp.endsWith("/"))
			esp += "/";
		return esp;
	}

	public static boolean mkDir(String path) {
		if(path == null || path.trim().length() == 0)
			return false;
		if(path.endsWith("/")){
			int n = path.lastIndexOf("/");
			path = path.substring(0,n);
		}
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				return true;
			} else {
				boolean del = file.delete();
				if(!del){
					LogUtil.d(tag,"del="+del);
				}
			}
		}
		return file.mkdirs();
	}

	public static long getFileDate(String fn) {
		File file = new File(fn);
		if (!file.exists())
			return 0;
		long dt = file.lastModified();
		file = null;
		return dt;
	}

	public static long getFileSize(String fn) {
		File file = new File(fn);
		if (!file.exists())
			return 0;
		long len = file.length();
		file = null;
		return len;
	}

	public static byte[] getFileForBytes(String fileFullName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        InputStream is = null;
        byte[] data = null;
        try {
            is = new FileInputStream(fileFullName);
            while (true) {
				int ri = is.read(buffer, 0, buffer.length);
				if (ri <= 0)
					break;
				baos.write(buffer, 0, ri);
                baos.flush();
			}
            data = baos.toByteArray();
		}finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                }
            }
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return data;
	}

    public static String getFileForText(String fileFullPath) throws Exception{
        return FileUtil.getTextByFilePath(fileFullPath);
    }

	public static boolean fileExist(String path) {
		if (path == null || path.trim().length() == 0)
			return false;
		File file = new File(path);
		return file.exists();
	}

	public static boolean isDirectory(String path) {
		if (path == null || path.trim().length() == 0)
			return false;
		File file = new File(path);
		return file.isDirectory();
	}

	public static boolean saveFileForBytes(String fullFileName, byte[] data) throws Exception {
		if (data == null)
			throw new IllegalArgumentException("data is null");
		if (fullFileName == null)
			throw new IllegalArgumentException("filename is null");
        ByteArrayInputStream bais = new ByteArrayInputStream(data,0,data.length);
        return saveFileForInputStream(fullFileName, bais);
	}

	public static boolean saveFileForInputStream(String fileFullPath, InputStream is) throws Exception{
		boolean writed = false;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        File tmpFile = new File(fileFullPath);
        if(tmpFile.getParentFile().exists()==false){
            IOUtil.mkDir(tmpFile.getParentFile().getAbsolutePath());
        }
        bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
        bis = new BufferedInputStream(is);
        int rec = -1;
        byte[] buf = new byte[4096];
        try {
            while((rec = bis.read(buf,0,buf.length)) != -1){
                bos.write(buf,0,rec);
                bos.flush();
            }
        } finally {
            if(bos != null){
                try {
                    bos.close();
                } catch (Exception e) {
                }
            }
            if(bis != null){
                try {
                    bis.close();
                } catch (Exception e) {
                }
            }
        }
        return writed;
	}
	
	public static boolean saveFileForText(String fileFullPath, String text) throws Exception{
        return saveFileForText(fileFullPath,text,false);
	}
	
	public static boolean saveFileForText(String fileFullPath,String text,boolean append) throws Exception{
		return FileUtil.saveTextToFilePath(fileFullPath, text, append);
	}
	
	public static boolean saveFileForBitmap(String fileFullPath,Bitmap bmp,final int quality) throws Exception{
		boolean saved = false;
		if(bmp == null || bmp.isRecycled())
			return saved;
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(fileFullPath));
			bmp.compress(CompressFormat.JPEG, quality,bos);
			bos.flush();
			saved = true;
		} finally{
			if(bos != null){
				try {
					bos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			bos = null;
		}
		return saved;
	}

	public static void deleteByFilePath(String path) {
		File file = new File(path);
		if (!file.exists())
			return;
		if (!file.delete())
			file.deleteOnExit();
	}

	public static void deleteByFile(File file) {
		if (file == null || !file.exists())
			return;
		boolean suc = file.delete();
		if (!suc) {
			file.deleteOnExit();
		}
	}

	/** 删除目录及目录下的所有文件*/
	public static void deleteDir(File dir) {
		if (dir == null)
			return;
		if (!dir.exists())
			return;
		if(dir.isFile()){
			boolean del = dir.delete();
			if(!del){
				dir.deleteOnExit();
			}
			return;
		}
		if (!dir.isDirectory())
			return;
		try {
			File[] allFiles = dir.listFiles();
			if (allFiles == null || allFiles.length == 0) {
				boolean del = dir.delete();
				if(!del){
					dir.deleteOnExit();
				}
				return;
			}
			for (File f : allFiles) {
				if (f.isDirectory()) {
					deleteDir(f);
					boolean del =f.delete();
					if(!del){
						f.deleteOnExit();
					}
				} else if (f.isFile()) {
					boolean del =f.delete();
					if(!del){
						f.deleteOnExit();
					}
				}
			}
			boolean del =dir.delete();
			if(!del){
				dir.deleteOnExit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	
	public static boolean deleteFileByRename(String path){
		File ff = new File(path);
		if(!ff.exists())
			return false;
		File newFile = new File(path+"new");
		boolean renamed = ff.renameTo(newFile);
		boolean deleted = false;
		if(renamed){
			deleted=newFile.delete();
		}else{
			deleted=ff.delete();
		}
		return deleted;
	}

	public static byte[] serialize(Object o) throws Exception {
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			serialize(o, baos);
			byte bs[] = baos.toByteArray();
			return bs;
		} catch (Exception e) {
			throw e;
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (Exception e2) {
				} finally {
					baos = null;
				}
			}
		}
	}

	public static void serialize(Object o, String fn) throws Exception {
		FileOutputStream fos = new FileOutputStream(fn);
		serialize(o, fos);
	}

	public static void serialize(Object o, OutputStream ostrm) throws Exception {
		if (ostrm == null)
			throw new IOException("output stream is null");
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(ostrm);
			os.writeObject(o);
			os.close();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e2) {
				} finally {
					os = null;
				}
			}
		}
	}

	public static Object unSerialize(byte[] bytes) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try {
			Object o = unSerialize(bais);
			return o;
		} finally {
			if (bais != null)
				try {
					bais.close();
				} catch (Exception e1) {
				} finally {
					bais = null;
				}
		}
	}

	public static Object unSerialize(InputStream istrm) throws Exception {
		ObjectInputStream is = null;
		Object o;
		try {
			is = new ObjectInputStream(istrm);
			o = is.readObject();
			is.close();
			is = null;
			return o;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				} finally {
					is = null;
				}
			}
			if (istrm != null) {
				try {
					istrm.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				} finally {
					istrm = null;
				}
			}
		}
	}

	/** 获得问件名后缀 */
	public static String getFileSuffix(String fn) {
		if (fn == null || fn.trim().length() == 0)
			return null;
		int index = fn.lastIndexOf(".");
		if (index < 0)
			return null;
		return fn.substring(index);
	}
	
	public static String getFileSuffix(File f){
		return getFileSuffix(f.getAbsolutePath());
	}
	
	public static String getFileName(String fn){
		if (fn == null || fn.trim().length() == 0 || fn.endsWith("/"))
			return null;
		int index = fn.lastIndexOf("/");
		if (index < 0)
			return fn;
		return fn.substring(index + 1);
	}
	
	public static String getFileName(File f){
		return getFileName(f.getAbsolutePath());
	}
	
	public static String getFileNameNoSuffix(String fn){
		String str = getFileName(fn);
		if(str == null || str.trim().length() == 0)
			return null;
		int end = str.indexOf(".");
		if(end >= 0){
			str = str.substring(0,end);
		}
		return str.trim().replace(" ","").toLowerCase();
	}
	
	public static String getFileNameNoSuffix(File f){
		return getFileNameNoSuffix(f.getAbsolutePath());
	}

	/**
	 * 单个文件复制
	 * @param targetFilePath 目标路径
	 * @param sourceFilePath 源路径
	 * @return 是否复制成功
	 */
	public static boolean copyFile(String targetFilePath, String sourceFilePath) {
		if (targetFilePath == null || targetFilePath.trim().length() == 0)
			return false;
		if (sourceFilePath == null || sourceFilePath.trim().length() == 0)
			return false;
		File sourceFile = new File(sourceFilePath);
		if (!sourceFile.exists())
			return false;

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(sourceFilePath));
			bos = new BufferedOutputStream(new FileOutputStream(targetFilePath));
			byte[] b = new byte[1024];
			int n = 0;
			while ((n = bis.read(b, 0, b.length)) != -1) {
				bos.write(b, 0, n);
			}
			bos.flush();
			bos.close();
			bos = null;
			bis.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
