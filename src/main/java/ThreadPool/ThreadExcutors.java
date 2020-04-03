package ThreadPool;

import java.util.concurrent.*;

public enum ThreadExcutors {
    INSTANCE(5,5,5,5);
    private ExecutorService executorService;
    ThreadExcutors(int coresize,int maxsize,int arrayblockingqueuesize,int waitTime){
        executorService = new ThreadPoolExecutor(coresize,maxsize,waitTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(arrayblockingqueuesize),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
    public ExecutorService getExecutor(){
        return executorService;
    }
}
