package news.com.sample;

import android.app.Application;
import android.content.Intent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import news.com.sample.service.NewsFetchService;

public class NewsApplication extends Application {

    private static NewsApplication sInstance = null;
    private ThreadPoolExecutor mThreadPoolExecutor;
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        //Periodic customized sync interval
        CustomConfigController.SYNC_INTERVAL = 2 * 60 * 60 * 1000L;
        //Just in case query increases thread Pool can be used to handle the db operations on different thread.
        mThreadPoolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

//        Intent intent = new Intent(this, NewsFetchService.class);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            this.startForegroundService(intent);
//        } else {
//            this.startService(intent);
//        }
    }

    public static NewsApplication getApplicationInstance() {
        return sInstance;
    }

    public void execute(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);

    }
}
