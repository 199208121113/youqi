package com.lg.base.core;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 *
 * Created by liguo on 2015/11/9.
 */
public abstract class RoboAsyncTask<T> implements Callable<T> {

    public final String TAG = this.getClass().getSimpleName();

    protected abstract T doInBackground() throws Exception;

    protected void onPreExecute() {

    }

    protected void onException(Exception e) {

    }

    protected void onSuccess(T result) {

    }

    protected void onFinally() {

    }

    @Override
    public final T call() throws Exception {
        T result = null;
        try {
            BaseApplication.getTaskHandler().post(new Runnable() {
                @Override
                public void run() {
                    RoboAsyncTask.this.onPreExecute();
                }
            });
            result = doInBackground();
            BaseApplication.getTaskHandler().post(new RunnableSuccess(result));
        } catch (Exception e) {
            BaseApplication.getTaskHandler().post(new RunnableException(e));
        }finally{
            BaseApplication.getTaskHandler().post(new Runnable() {
                @Override
                public void run() {
                    RoboAsyncTask.this.onFinally();
                }
            });
        }
        return result;
    }

    public void execute(){
        if(ft != null){
            return;
        }
        ft = new FutureTask<T>(this);
        new Thread(ft).start();
    }

    public final boolean isCancelled() {
        return ft.isCancelled();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return ft.cancel(mayInterruptIfRunning);
    }

    private FutureTask<T> ft = null;

    private class RunnableSuccess implements Runnable{
        private T result;
        public RunnableSuccess(T result) {
            this.result = result;
        }
        @Override
        public void run() {
            RoboAsyncTask.this.onSuccess(result);
        }
    }

    private class RunnableException implements Runnable{
        private Exception err;
        public RunnableException(Exception err) {
            this.err = err;
        }
        @Override
        public void run() {
            RoboAsyncTask.this.onException(err);
        }
    }
}
