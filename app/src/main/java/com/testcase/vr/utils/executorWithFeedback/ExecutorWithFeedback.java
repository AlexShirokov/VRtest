package com.testcase.vr.utils.executorWithFeedback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by AlexShredder on 26.06.2016.
 */
public class ExecutorWithFeedback {
    private static final String TAG = "4ls-MyExecutor";
    private static ExecutorService executor;
    private static int count = 1;
    private static Handler handler;
    private static int jobsCount;

    private static void init(){
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),new LowPriorityThreadFactory());
        jobsCount=0;
    }

    public static synchronized void run(MyJob r){

        if (executor==null) init();
        jobsCount++;
        executor.execute(r);

    }

    private static synchronized Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    public static boolean hasJobs(){
        return jobsCount!=0;
    }

    static class LowPriorityThreadFactory implements ThreadFactory{

        @Override
        public Thread newThread(Runnable runnable) {

            Thread thread = new Thread(runnable);
            thread.setName("Low-"+String.valueOf(count));
            thread.setPriority(4);
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable throwable) {
                    Log.d(TAG,"Thread = "+t.getName()+", error = "+throwable.getMessage());
                }
            });
            count++;
            return thread;
        };

    }

    public abstract static class MyJob implements Runnable{

        private OnCompleteListener onCompleteListener;

        @Override
        public final void run() {
            final Bundle result = new Bundle();
            Bundle preResult;
            String hasError = null;
            try {
                preResult = doJob();
                result.putAll(preResult);
            } catch (Exception e) {
                e.printStackTrace();
                hasError = e.getMessage();
            } finally {
                jobsCount--;
            }
            if (onCompleteListener!=null) {
                if (hasError!=null) result.putString("error",hasError);
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        onCompleteListener.onComplete(result);
                    }
                });
            }
        }

        protected abstract Bundle doJob();

        public MyJob setOnCompleteListener(OnCompleteListener listener){
            onCompleteListener = listener;
            return this;
        }

        public void execute(){
            ExecutorWithFeedback.run(this);
        }

    }

    static public void stop(){
        if (executor!=null && !executor.isTerminated()) executor.shutdown();
        executor=null;
    }

    public interface OnCompleteListener{
        void onComplete(Bundle result);
    }

}
