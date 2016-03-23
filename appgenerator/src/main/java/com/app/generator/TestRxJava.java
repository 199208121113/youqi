package com.app.generator;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by root on 16-3-22.
 */
public class TestRxJava {
    public static void main(String[] args){
        log("main(),MainThreadID=" + getCurThreadId());
        Subscription subscription = Observable.just("3", "4")
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        log("map(),tid=" + getCurThreadId());
                        int i = Integer.parseInt(s);
                        return i;
                    }
                }).flatMap(new Func1<Integer, Observable<String[]>>() {
                    @Override
                    public Observable<String[]> call(Integer integer) {
                        log("flatMap(),tid=" + getCurThreadId());
                        String arr[] = new String[integer];
                        for (int i = 0; i < arr.length; i++) {
                            arr[i] = "" + (i + 1);
                        }
                        return Observable.just(arr);
                    }
                })
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<String[]>() {
                    @Override
                    public void onCompleted() {
                        String fileName = System.currentTimeMillis()+".txt";
                        try {
                            File ff = new File("/root/aaaaaa/"+fileName);
                            RandomAccessFile raf = new RandomAccessFile(ff,"rw");
                            raf.writeUTF(fileName);
                            raf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        log("onCompleted(),tid=" + getCurThreadId());
                    }

                    @Override
                    public void onError(Throwable e) {
                        log("onError(),tid=" + getCurThreadId());
                    }

                    @Override
                    public void onNext(String[] arr) {
                        StringBuilder sb = new StringBuilder();
                        for (String s : arr) {
                            sb.append(s).append(",");
                        }
                        log("onNext(),tid=" + getCurThreadId() + ",sb=" + sb);
                    }
                });

        log("isUnsubscribed()=" + subscription.isUnsubscribed());
    }

    private static long getCurThreadId(){
        return Thread.currentThread().getId();
    }

    private static final void log(String s){
        System.err.println(s);
    }

    private static void testBus(){

    }
}
