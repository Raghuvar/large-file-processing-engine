package com.postman.executor;

import java.util.concurrent.*;

public class ExecutorFactory {


    public static ExecutorService getBlockingExecutorService(int nThreads){
        ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(2*nThreads);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nThreads, nThreads, 0L,
                TimeUnit.MILLISECONDS, blockingQueue);


        executor.setRejectedExecutionHandler(getBlockingRejectionPolicy());
        return executor;
    }


    /**
     * @return  {@link java.util.concurrent.RejectedExecutionHandler}
     * 1. In other handler policy main thread start executing the job, so if main-thread
     * got long running job, in that case main-thread may keep working and other may remain idle.
     * 2. This policy would block the main thread on submitting job instead of executing the job
     */
    private static RejectedExecutionHandler getBlockingRejectionPolicy(){
        return new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (!executor.isShutdown()){
                    try {
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        System.out.println("Queue not accepting new request. Something wrong !!!");
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}