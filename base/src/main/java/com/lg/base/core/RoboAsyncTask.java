package com.lg.base.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 *
 * Created by liguo on 2015/11/9.
 */
public abstract class RoboAsyncTask<T> implements Callable<T> {

    protected abstract T doInBackground() throws Exception;

    protected void onPreExecute() {

    }

    protected void onException(Exception e) {

    }

    protected void onSuccess(T result) {

    }

    protected void onFinally() {

    }

    public static Handler getTaskHandler(){
        return TaskHandlerFactory.INSTANCE;
    }

    private static class TaskHandlerFactory{
       private static Handler INSTANCE = new Handler(Looper.getMainLooper());
    }

    @Override
    public final T call() throws Exception {
        T result = null;
        try {
            getTaskHandler().post(new TaskCallback<>(this,0));
            result = doInBackground();
            getTaskHandler().post(new TaskCallback<>(this,1).setResult(result));
        } catch (Exception e) {
            getTaskHandler().post(new TaskCallback<>(this,2).setErr(e));
        }finally{
            getTaskHandler().post(new TaskCallback<>(this,3));
        }
        return result;
    }

    public void execute(){
        if(ft != null){
            return;
        }
        ft = new FutureTask<>(this);
        new Thread(ft).start();
    }

    @SuppressWarnings("unused")
    public final boolean isCancelled() {
        return ft.isCancelled();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return ft.cancel(mayInterruptIfRunning);
    }

    private FutureTask<T> ft = null;

    private static class TaskCallback<T> implements Runnable{
        private T result;
        private Exception err;
        private RoboAsyncTask<T> task;
        private int mode;

        public TaskCallback(RoboAsyncTask<T> task, int mode) {
            this.task = task;
            this.mode = mode;
        }

        @Override
        public void run() {
            if(mode == 0){
                task.onPreExecute();
            }else if(mode == 1){
                task.onSuccess(this.result);
            }else if(mode == 2){
                task.onException(this.err);
            }else if(mode == 3){
                task.onFinally();
            }

        }

        public TaskCallback setResult(T result) {
            this.result = result;
            return this;
        }

        public TaskCallback setErr(Exception err) {
            this.err = err;
            return this;
        }
    }
}
