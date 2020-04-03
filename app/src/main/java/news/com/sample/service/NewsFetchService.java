package news.com.sample.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import news.com.sample.CustomConfigController;
import news.com.sample.receiver.AutoStartReceiver;

import static android.app.AlarmManager.INTERVAL_HOUR;


public class NewsFetchService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startCustomForeground();
        else
            startForeground(1, new Notification());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void startCustomForeground() {
        String NOTIFICATION_CHANNEL_ID = "news.com.technewssample";
        String channelName = "NewsFetchService";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        Notification notification = notificationBuilder.setOngoing(true)

                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Log.d("app", "Starting SDCard check alarm");
        Intent sdcardCheckEvent = new Intent(getApplicationContext(), AutoStartReceiver.class);
        sdcardCheckEvent.setAction("ACTION_CALL_API");
        PendingIntent sdcardCheckPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, sdcardCheckEvent, 0);
        if (alarmMgr != null) {
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), CustomConfigController.SYNC_INTERVAL == 0 ? INTERVAL_HOUR : CustomConfigController.SYNC_INTERVAL, sdcardCheckPendingIntent);
        }
        return START_STICKY;
    }
}
