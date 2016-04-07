package com.lg.base.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.lg.base.bus.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;

/**
 * Image utilities
 */
public class ImageUtil {

	private static final String TAG = "ImageUtil";

	/**
	 * Get a bitmap from the image path
	 *
	 * @param imagePath
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final String imagePath) {
		return getBitmap(imagePath, 1);
	}

	/**
	 * Get a bitmap from the image path
	 *
	 * @param imagePath
	 * @param sampleSize
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final String imagePath, int sampleSize) {
		final Options options = new Options();
		options.inDither = false;
		options.inSampleSize = sampleSize;

		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(imagePath, "r");
			return BitmapFactory.decodeFileDescriptor(file.getFD(), null, options);
		} catch (IOException e) {
			Log.d(TAG, e.getMessage(), e);
			return null;
		} finally {
			if (file != null)
				try {
					file.close();
				} catch (IOException e) {
					Log.d(TAG, e.getMessage(), e);
				}
		}
	}

	/**
	 * Get a bitmap from the image
	 *
	 * @param image
	 * @param sampleSize
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final byte[] image, int sampleSize) {
		final Options options = new Options();
		options.inDither = false;
		options.inSampleSize = sampleSize;
		return BitmapFactory.decodeByteArray(image, 0, image.length, options);
	}

	/**
	 * Get scale for image of size and max height/width
	 *
	 * @param size
	 * @param width
	 * @param height
	 * @return scale
	 */
	public static int getScale(Point size, int width, int height) {
		if (size.x > width || size.y > height)
			return Math.max(Math.round((float) size.y / (float) height), Math.round((float) size.x / (float) width));
		else
			return 1;
	}

	/**
	 * Get size of image
	 *
	 * @param imagePath
	 * @return size
	 */
	public static Point getSize(final String imagePath) {
		final Options options = new Options();
		options.inJustDecodeBounds = true;

		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(imagePath, "r");
			BitmapFactory.decodeFileDescriptor(file.getFD(), null, options);
			return new Point(options.outWidth, options.outHeight);
		} catch (IOException e) {
			Log.d(TAG, e.getMessage(), e);
			return null;
		} finally {
			if (file != null)
				try {
					file.close();
				} catch (IOException e) {
					Log.d(TAG, e.getMessage(), e);
				}
		}
	}

	/**
	 * Get size of image
	 *
	 * @param image
	 * @return size
	 */
	public static Point getSize(final byte[] image) {
		final Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(image, 0, image.length, options);
		return new Point(options.outWidth, options.outHeight);
	}

	/**
	 * Get bitmap with maximum height or width
	 *
	 * @param imagePath
	 * @param width
	 * @param height
	 * @return image
	 */
	public static Bitmap getBitmap(final String imagePath, int width, int height) {
		Point size = getSize(imagePath);
		return getBitmap(imagePath, getScale(size, width, height));
	}

	/**
	 * Get bitmap with maximum height or width
	 *
	 * @param image
	 * @param width
	 * @param height
	 * @return image
	 */
	public static Bitmap getBitmap(final byte[] image, int width, int height) {
		Point size = getSize(image);
		return getBitmap(image, getScale(size, width, height));
	}

	/**
	 * Get bitmap with maximum height or width
	 *
	 * @param image
	 * @param width
	 * @param height
	 * @return image
	 */
	public static Bitmap getBitmap(final File image, int width, int height) {
		return getBitmap(image.getAbsolutePath(), width, height);
	}

	/**
	 * Get a bitmap from the image file
	 *
	 * @param image
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final File image) {
		return getBitmap(image.getAbsolutePath());
	}

	public static Bitmap cropBitmap(Bitmap bitmap, int width, int height) {
		Log.d(TAG, "width,height = " + width + "," + height);

		int origWidth = bitmap.getWidth();
		int origHeight = bitmap.getHeight();

		Log.d(TAG, "origWidth, origHeight = " + origWidth + "," + origHeight);
		float scale = Math.max((float) height / (float) origHeight, (float) width / (float) origWidth);
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap roomedBitmap = Bitmap.createBitmap(bitmap, 0, 0, origWidth, origHeight, matrix, true);
		origWidth = roomedBitmap.getWidth();
		origHeight = roomedBitmap.getHeight();
		Log.d(TAG, "roomedBitmap width,height = " + origWidth + "," + origHeight);

		Bitmap cropedBitmap = Bitmap.createBitmap(roomedBitmap, origWidth / 2 - width / 2, origHeight / 2 - height / 2, width, height);
		Log.d(TAG, "cropedBitmap width,height = " + cropedBitmap.getWidth() + "," + cropedBitmap.getHeight());

		if (roomedBitmap != null)
			roomedBitmap.recycle();

		return cropedBitmap;
	}

	public static Bitmap getBitmapFromFile(String path, int newWidth) {
		if (path == null || path.trim().length() == 0)
			throw new IllegalArgumentException("path is null");
		File f = new File(path);
		if (!f.exists())
			throw new RuntimeException("file:" + path + " not found");
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
		int inSampleSize = opts.outWidth / newWidth;
		if (opts.outWidth >= newWidth) {
			int height = Math.round((opts.outHeight * 1.0f * newWidth) / opts.outWidth);
			opts.outWidth = newWidth;
			opts.outHeight = height;
		}
		if (inSampleSize <= 0) {
			inSampleSize = 1;
		}
		opts.inPreferredConfig = Config.ARGB_8888;
		opts.inSampleSize = inSampleSize;
		opts.inJustDecodeBounds = false;
		opts.inInputShareable = true;
		opts.inPurgeable = true;
		Bitmap bmp = BitmapFactory.decodeFile(f.getPath(), opts);
		return bmp;
	}

	public static Object invoke(String clsName,String methodName,Class<?>[] methodParams,Object entityObj,Object[] args) throws Exception{
		Class<?> cls = Class.forName(clsName);
		Method method = cls.getDeclaredMethod(methodName, methodParams);
		method.setAccessible(true);
		return method.invoke(entityObj, args);
	}

	public static Object invoke(Class<?> cls,String methodName,Class<?>[] methodParams,Object entityObj,Object[] args) throws Exception{
		Method method = cls.getDeclaredMethod(methodName, methodParams);
		method.setAccessible(true);
		return method.invoke(entityObj, args);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getLocalPathFromUri(Uri uri, Activity activity) {
		if(uri == null)
			return null;
		if(Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(activity,uri)){
            /*String wholeID = DocumentsContract.getDocumentId(uri);
            if(wholeID != null && wholeID.trim().length() > 0){
                String id = wholeID.split(":")[1];
                String[] column = { MediaStore.Images.Media.DATA };
                String sel = MediaStore.Images.Media._ID + "=?";
                Cursor cursor = null;
                String filePath = null;
                try {
                    cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[] { id }, null);
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(columnIndex);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    if(cursor != null){
                        cursor.close();
                    }
                }
                if(filePath != null){
                    return filePath;
                }
            }*/
			if (isExternalStorageDocument(uri)) {
				String docId = DocumentsContract.getDocumentId(uri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(uri)) {
				String id = DocumentsContract.getDocumentId(uri);
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(activity, contentUri, null, null);
			} else if (isMediaDocument(uri)) {
				String docId = DocumentsContract.getDocumentId(uri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[] { split[1] };
				return getDataColumn(activity, contentUri, selection, selectionArgs);
			}
		}

		/**
		 (1) file:///sdcard/DCIM/Camera/1397608571759.jpg
		 (2) content://media/external/images/media/178107
		 */
		if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		if("content".equalsIgnoreCase(uri.getScheme())){
			/*Cursor cursor = null;
			String path = null;
			try {
				cursor = activity.managedQuery(uri, new String[] { MediaStore.Images.Media.DATA }, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				path = cursor.getString(column_index);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null && Build.VERSION.SDK_INT < 14) {
					cursor.close();
				}
			}
			return path;*/
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();
			return getDataColumn(activity, uri, null, null);
		}
		return null;
	}

	/**
	 * 设置ImageView跟据图片内容自动缩放
	 *
	 * @param iv
	 * @param widthDp
	 *            水平间隔,单位dp
	 */
	public static void setAutoLayoutParams(ImageView iv, int widthDp) {
		Bitmap bmp = ((BitmapDrawable) iv.getDrawable()).getBitmap();
		if (bmp == null) {
			LogUtil.w(TAG, "setAutoLayoutParams(),bmp is null");
			return;
		}
		LayoutParams lp = iv.getLayoutParams();
		int w = ScreenUtil.getDisplay(iv.getContext()).getWidth() - ScreenUtil.dip2px(iv.getContext(), widthDp);
		if (lp == null) {
			lp = new LayoutParams(w, 0);
		}
		lp.width = w;
		lp.height = Math.round(bmp.getHeight() * 1f * lp.width / bmp.getWidth());
		LogUtil.d(TAG, "setAutoLayoutParams(),lp.width=" + lp.width + " lp.height=" + lp.height);
		iv.setLayoutParams(lp);
	}

	/**
	 * 设置ImageView的LayoutParams
	 *
	 * @param iv
	 * @param widthDp
	 *            LayoutParams的宽度
	 * @param heightDp
	 *            LayoutParams的高度
	 */
	public static void setLayoutParams(View iv, int widthDp, int heightDp) {
		LayoutParams lp = iv.getLayoutParams();
		int w = ScreenUtil.dip2px(iv.getContext(), widthDp);
		int h = ScreenUtil.dip2px(iv.getContext(), heightDp);
		if (lp == null) {
			lp = new LayoutParams(w, h);
		} else {
			lp.width = w;
			lp.height = h;
		}
		iv.setLayoutParams(lp);
	}

	public static void setLayoutParamsByPX(View iv, int widthPX, int heightPX) {
		LayoutParams lp = iv.getLayoutParams();
		if (lp == null) {
			lp = new LayoutParams(widthPX, heightPX);
		} else {
			lp.width = widthPX;
			lp.height = heightPX;
		}
		iv.setLayoutParams(lp);
	}

	public static Bitmap compressImage(Bitmap image, final int kbSize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int options = 90;
		while (baos.toByteArray().length / 1024 > kbSize) {
			baos.reset();
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
			options -= 10;
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		LogUtil.d(TAG, "baos.size()="+baos.size()/1024.0);
		return bitmap;
	}

	public static Bitmap getZipBitmap(String srcPath, int kbSize) {
		Bitmap bitmap = getBitmap(srcPath);
		Bitmap newBitmap = compressImage(bitmap, kbSize);
		bitmap.recycle();
		bitmap = null;
		return newBitmap;
	}

	//----------------------------
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String column = MediaStore.Images.Media.DATA;
		String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

}
