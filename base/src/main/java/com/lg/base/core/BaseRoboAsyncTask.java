package com.lg.base.core;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.OperationCanceledException;

import com.lg.base.account.AccountUtils;
import com.lg.base.dialog.ProxyOnDismissListener;
import com.lg.base.task.OnTaskRunningListener;
import com.lg.base.task.Status;
import com.lg.base.ui.dialog.LightAlertDialog;
import com.lg.base.ui.dialog.LightNetWorkSetDialog;
import com.lg.base.utils.ExceptionUtil;
import com.lg.base.utils.NetworkUtil;
import com.lg.base.utils.StringUtil;
import com.lg.base.utils.ToastUtil;

import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;


public abstract class BaseRoboAsyncTask<T> extends RoboAsyncTask<T> {

    public static final long MINUTES_1 = 1000 * 60;

    public static final long DAY_1 = MINUTES_1 * 60 * 24;

    private WeakReference<Activity> mActivity;
    public BaseRoboAsyncTask(Activity activity) {
        this.mActivity = new WeakReference<Activity>(activity);
    }
    public Activity getActivityContext(){
        return mActivity.get();
    }

    protected abstract T run() throws Exception;

    /** 返回该Exception是否需要重试 */
    public static boolean isDontNeedRetryException(Exception err) {
        boolean needRetry = false;
        if ((err instanceof OperationCanceledException) || (err instanceof AccountsException)) {
            needRetry = true;
        }else if(OnTaskRunningListener.OPERATION_CANCELED_FLAG.equals(err.getMessage())){
            needRetry = true;
        }
        return needRetry;
    }

    private Status curStatus = Status.PENDING;

    public Status getCurStatus() {
        return curStatus;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        curStatus = Status.CANCELED;
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public final T doInBackground() throws Exception {
        curStatus = Status.RUNNING;
        Exception err = null;
        T result = null;
        try {
            result = run();
        } catch (Exception e) {
            err = e;
        }
        if (err == null) {
            curStatus = Status.ERROR_STOPED;
            return result;
        }
        if(isDontNeedRetryException(err)){
            throw err;
        }
        final int retryCount = Math.min(0,getRetryCount());
        /** 开始重试 */
        for (int i = 1; i <= retryCount; i++) {
            Exception retryErr = null;
            try {
                result = run();
            } catch (Exception e) {
                retryErr = e;
            }
            if (retryErr == null) {
                err = null;
                break;
            } else {
                err = retryErr;
                if(isDontNeedRetryException(err)){
                    break;
                }
            }
        }
        if (err != null) {
            curStatus = Status.ERROR_STOPED;
            throw err;
        }
        curStatus = Status.FINISHED;
        return result;
    }

    /** 执行重试的次数，0表示 不执行重试 次数 */
    protected int getRetryCount() {
        return 0;
    }

    // ==================网络设置dialog=================
    AlertDialog networkDialog = null;

    protected AlertDialog showNetWorkDialog(String title, String message) {
        if (networkDialog != null)
            closeNetWorkDialog();
        networkDialog = LightNetWorkSetDialog.create(mActivity.get(), title, message);
        networkDialog.show();
        return networkDialog;
    }

    protected void closeNetWorkDialog() {
        if (networkDialog == null)
            return;
        networkDialog.dismiss();
        networkDialog = null;
    }

    @Override
    protected void onException(Exception e) {
        final boolean avilable = NetworkUtil.isAvailable(mActivity.get());

        LogUtil.e(TAG, "ExceptionInfo:", e);
        if (!isOpened()) {
            return;
        }

        if (!avilable) {
            ToastUtil.show(mActivity.get(), "网络已断开,请检查网络!");
            return;
        }
        if (e instanceof HttpHostConnectException) {
            showErrorDialog("网络不稳定,请检查网络或稍后重试");
        } else if (e instanceof ConnectTimeoutException) {
            showErrorDialog("连接超时");
        } else if (e instanceof SocketTimeoutException) {
            //服务器处理超时造成的
        } else if (e instanceof UnknownHostException) {
            showErrorDialog("网络不稳定,请重试[103]");
        } else if (e instanceof SSLException) {
            //网络太慢造成的
        }else if(e instanceof ProtocolException){
            showErrorDialog("网络不稳定,请重试[101]");
        }else if(e instanceof NoHttpResponseException){
            showErrorDialog("网络不稳定,请重试[102]");
        }else if(e instanceof OperationCanceledException){
            // nothing to do
        }else if(e instanceof AccountsException){
            // nothing to do
        } else {
            String errMsg = getErrorMsgStr(e);
            showErrorDialog(errMsg);
        }
    }

    public static String getErrorMessage(Exception e){
        String errMsg = null;
        if (e instanceof HttpHostConnectException) {
            errMsg="网络不稳定";
        } else if (e instanceof ConnectTimeoutException) {
            errMsg="网络超时";
        } else if (e instanceof SocketTimeoutException) {
            errMsg="服务器处理超时";
        } else if (e instanceof UnknownHostException) {
            errMsg="网络不稳定,请重试[103]";
        } else if (e instanceof SSLException) {
            //网络太慢造成的
        }else if(e instanceof ProtocolException){
            errMsg="网络不稳定,请重试[101]";
        }else if(e instanceof NoHttpResponseException){
            errMsg="网络不稳定,请重试[102]";
        }else{
            errMsg = getErrorMsgStr(e);
        }
        return errMsg;
    }

    public static String getErrorMsgStr(Exception e){
        String errMsg = e.getMessage();
        if (errMsg == null || errMsg.trim().length() == 0) {
            errMsg = ExceptionUtil.getStackTrace(e);
        }
        return errMsg;
    }

    /** 是否弹出错误信息的Dialog */
    protected boolean isOpened() {
        return true;
    }

    protected void showErrorDialog(String errMsg) {
        Activity act = mActivity.get();
        if(act == null)
            return;
        AlertDialog dialog = LightAlertDialog.create(act);
        dialog.setTitle("提示");
        String errSux = getExceptionTitle();
        if (!StringUtil.isEmpty(errSux)) {
            errSux += "#";
        }
        dialog.setMessage(errSux + errMsg);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new ProxyOnDismissListener(mActivity.get(),getOnDialogCloseListener(),null));
        dialog.show();
    }

    protected ProxyOnDismissListener.DialogCloseCallBack getOnDialogCloseListener() {
        return null;
    }

    protected String getExceptionTitle() {
        return "";
    }

    private Bundle mExtraParam = null;
    public Bundle getExtraParam() {
        return mExtraParam;
    }
    public void setExtraParam(Bundle mExtraParam) {
        this.mExtraParam = mExtraParam;
    }

    /** 删除旧帐户 */
    protected boolean removeOldAccount(AccountManager manager,Account account){
        boolean removed = true;
        String accountName = "";
        try {
            if(account != null && manager != null) {
                accountName = account.name;
                AccountManagerFuture<Boolean> amf = manager.removeAccount(account, null, null);
                removed = amf.getResult(20, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.i(TAG, "account[" + accountName + "],removed=" + removed);
        return removed;
    }

    /** 添加新帐户 */
    protected boolean createOrUpdateAccount(String loginName,String loginPwd,AccountManager am){
        if(am == null)
            return false;

        Account oldAccount = null;
        try {
            oldAccount = AccountUtils.getAccount(am);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //是否与上次登录的帐户相同
        boolean isSameAccount = false;
        if(oldAccount != null){
            String oldName = StringUtil.toLowerCase(oldAccount.name).trim();
            String newName = StringUtil.toLowerCase(loginName).trim();
            if(oldName.equals(newName)){
                isSameAccount = true;
                am.setPassword(oldAccount, loginPwd);
                am.setUserData(oldAccount, "uid", loginName);
                am.setUserData(oldAccount, "pwd", loginPwd);
                am.setUserData(oldAccount,"login_name",loginName);
            }else {
                removeOldAccount(am, oldAccount);
            }
        }

        Account newAccount = new Account(loginName, mActivity.get().getPackageName());
        Bundle bd = new Bundle();
        bd.putString("uid", loginName);
        bd.putString("pwd", loginPwd);
        bd.putString("login_name", loginName);
        boolean added = am.addAccountExplicitly(newAccount, loginPwd, bd);
        LogUtil.i(TAG, "account[" + newAccount.name + "],added=" + added+",isSameAccount="+isSameAccount);
        return added;
    }

    /** 从Account帐户中获取密码 */
    protected String getPasswordFromAccount(Account account,AccountManager am){
        String loginPwd = null;
        try {
            loginPwd = am.getPassword(account);
            if(StringUtil.isEmpty(loginPwd)){
                String newPwd = am.getUserData(account,"pwd");
                if(newPwd != null && newPwd.trim().length() > 0){
                    loginPwd = newPwd;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        if(StringUtil.isEmpty(loginPwd)){
            loginPwd = "";
        }
        return loginPwd;
    }

    /** 获取已经登录的帐户 */
    protected Account getLoginedAccount(AccountManager accountManager){
        Account account = null;
        Account[] accounts = null;
        try {
            accounts = AccountUtils.getAccounts(accountManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (accounts != null && accounts.length > 0) {
            account = accounts[0];
        }
        return account;
    }
}
