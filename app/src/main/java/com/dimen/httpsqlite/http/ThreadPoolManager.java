package com.dimen.httpsqlite.http;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 文件名：com.dimen.customhttp.http
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public class ThreadPoolManager {
    private static final String TAG = "ThreadPoolManager";

    private static ThreadPoolManager mThreadPoolManager = new ThreadPoolManager();

    private LinkedBlockingDeque<Future<?>> taskQuene = new LinkedBlockingDeque<>();

    private ThreadPoolExecutor mThreadPoolExecutor;

    public ThreadPoolManager() {
        mThreadPoolExecutor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4), handler);

        mThreadPoolExecutor.execute(runnable);
    }

    public static ThreadPoolManager getInstance() {
        return mThreadPoolManager;
    }
    public <T> boolean removeTask(FutureTask futureTask)
    {
        boolean result=false;
        /**
         * 阻塞式队列是否含有线程
         */
        if(taskQuene.contains(futureTask))
        {
            taskQuene.remove(futureTask);
        }else
        {
            result=mThreadPoolExecutor.remove(futureTask);
        }
        return  result;
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true)
            {
                FutureTask futrueTask=null;

                try {
                    /**
                     * 阻塞式函数
                     */
                    Log.i(TAG,"等待队列     "+taskQuene.size());
                    futrueTask= (FutureTask) taskQuene.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(futrueTask!=null)
                {
                    mThreadPoolExecutor.execute(futrueTask);
                }
                Log.i(TAG,"线程池大小      "+mThreadPoolExecutor.getPoolSize());
            }

        }
    };

    public <T> void execute(FutureTask<T> futureTask) throws InterruptedException {
        taskQuene.put(futureTask);
    }

    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                taskQuene.put(new FutureTask<Object>(r, null) {
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
