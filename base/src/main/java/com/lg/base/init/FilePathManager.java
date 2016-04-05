package com.lg.base.init;

import android.content.Context;
import android.os.Environment;

import com.lg.base.bus.LogUtil;
import com.lg.base.utils.IOUtil;
import com.lg.base.utils.StringUtil;

import java.io.File;

/**
 * 文件缓存管理类
 * Created by liguo on 2015/11/13.
 */
public class FilePathManager {
    private static final String TAG = FilePathManager.class.getSimpleName();

    private static String dir_path_on_clear_cache = null;
    private static String dir_path_on_uninstall = null;
    private static String dir_path_on_user = null;
    public static void init(Context ctx) {
        /*
            (1)内置私有目录(可以不考虑),因为有SharedPreferenceManager代替
            (2)外置目录
                (a)app缓存目录,即在清理垃圾的时候就会删除的目录[2]
                (b)app卸载后会删除的目录[4]
                (c)app卸载后都不会删除的永久目录[8]
         */
        File f1 = ctx.getExternalFilesDir(null);
        if(f1 != null){
            dir_path_on_clear_cache = f1.getAbsolutePath();
        }
        File f2 = ctx.getExternalCacheDir();
        if(f2 != null){
            dir_path_on_uninstall = f2.getAbsolutePath();
        }
        File f3 = Environment.getExternalStorageDirectory();
        if(f3 != null){
            dir_path_on_user = f3.getAbsolutePath()+"/"+ctx.getPackageName();
        }
        makeAllDirs();
    }

    private static void makeAllDirs(){
        String[] dirs = {dir_path_on_clear_cache,dir_path_on_uninstall,dir_path_on_user};
        for (String dir : dirs){
            if(StringUtil.isNotEmpty(dir)){
                boolean created = IOUtil.mkDir(dir);
                if(!created) {
                    LogUtil.e(TAG, dir + " create failed");
                }
            }else{
                LogUtil.e(TAG, dir + " is empty");
            }
        }
    }

    public static String getFileByDelModel(FileDelMode mode){
        if(mode == FileDelMode.del_on_clear_cache){
            return dir_path_on_clear_cache+"/";
        }
        if(mode == FileDelMode.del_on_uninstall){
            return dir_path_on_uninstall+"/";
        }
        if(mode == FileDelMode.del_on_user){
            return dir_path_on_user+"/";
        }
        return dir_path_on_clear_cache+"/";
    }

    public static String getFilePathByClearCache(){
        return getFileByDelModel(FileDelMode.del_on_clear_cache);
    }

    public static String getFilePathByUnInstall(){
        return getFileByDelModel(FileDelMode.del_on_uninstall);
    }

    public static String getFilePathByUser(){
        return getFileByDelModel(FileDelMode.del_on_user);
    }

    public enum FileDelMode{

        //清除缓存时被删除
        del_on_clear_cache,

        //app卸载椒被删除
        del_on_uninstall,

        //只能用户手动删除
        del_on_user
    }
}
