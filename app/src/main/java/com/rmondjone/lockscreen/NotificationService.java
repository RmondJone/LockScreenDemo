package com.rmondjone.lockscreen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 郭翰林
 * @date 2020/8/18 0018 11:45
 * 注释:
 */
public class NotificationService extends Service {
    private NotificationManager notificationManager;
    private Notification.Builder builder;
    private SimpleDateFormat simpleDateFormat;
    private Runnable mRunnable;
    private ScheduledExecutorService executorService;
    private boolean isStart;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);
        executorService = new ScheduledThreadPoolExecutor(1);
        mRunnable = () -> {
            RemoteViews remoteViews = getNoticeRemoteViews();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setCustomBigContentView(remoteViews);
            } else {
                builder.setContent(remoteViews);
            }
            notificationManager.notify(1, builder.build());
            startForeground(1, builder.build());
        };
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //开始通知栏计时,添加标记防止重复点击
        if (!isStart) {
            isStart = true;
            sendLockNotice();
            executorService.scheduleAtFixedRate(mRunnable, 0, 1000, TimeUnit.MILLISECONDS);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 注释：关闭通知服务
     * 时间：2020/8/18 0018 15:38
     * 作者：郭翰林
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void stopService(NoticeEvent event) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        notificationManager.cancel(1);
        stopSelf();
    }

    /**
     * 注释：发送锁屏通知
     * 时间：2020/8/17 0017 14:38
     * 作者：郭翰林
     */
    private void sendLockNotice() {
        //Android 8适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("1", "锁屏通知", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId("1");
        }
        //自定义通知栏视图
        RemoteViews remoteViews = getNoticeRemoteViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setCustomBigContentView(remoteViews);
        } else {
            builder.setContent(remoteViews);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //常驻通知栏
        builder.setOngoing(true);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        //锁屏显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        notificationManager.notify(1, builder.build());
        startForeground(1, builder.build());
    }

    /**
     * 注释：自定义通知栏
     * 时间：2020/8/18 0018 10:57
     * 作者：郭翰林
     *
     * @return
     */
    private RemoteViews getNoticeRemoteViews() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notice);
        remoteViews.setTextViewText(R.id.notice_time, simpleDateFormat.format(new Date()));
        //设置关闭按钮事件
        Intent intent = new Intent();
        intent.setAction("com.rmondjone.lockscreen.close");
        intent.setClassName(getPackageName(), "com.rmondjone.lockscreen.NotificationReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notice_close, pendingIntent);
        return remoteViews;
    }
}
