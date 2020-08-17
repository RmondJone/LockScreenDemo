package com.rmondjone.lockscreen;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.Permission;

/**
 * @author 郭翰林
 * @date 2020/8/17 0017 11:31
 * 注释:
 */
public class LoadActivity extends AppCompatActivity {
    Button lockNoticeButton;
    private ScreenListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //设置导航栏透明
        StatusBarHelper.fitsSystemWindows(this);
        StatusBarHelper.translucent(this);
        super.onCreate(savedInstanceState);
        //在锁屏页面显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        setContentView(R.layout.activity_load);
        PermissionUtils.applicationPermissions(this, new PermissionUtils.PermissionListener() {
            @Override
            public void onSuccess(Context context) {
            }

            @Override
            public void onFailed(Context context) {

            }
        }, Permission.Group.STORAGE);
        //设置锁屏通知
        lockNoticeButton = findViewById(R.id.button_lock_notice);
        lockNoticeButton.setOnClickListener(v -> {
            sendLockNotice();
        });
        //设置锁屏监听
        listener = new ScreenListener(this);
        listener.register(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn(Context context) {

            }

            @Override
            public void onScreenOff(Context context) {
                if (context instanceof Activity) {
                    MainActivity.gotoMe(((Activity) context));
                    finish();
                }
            }

            @Override
            public void onUserPresent(Context context) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.unregister();
        }
    }

    /**
     * 注释：发送锁屏通知
     * 时间：2020/8/17 0017 14:38
     * 作者：郭翰林
     */
    private void sendLockNotice() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        //Android 8适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("1", "锁屏通知", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId("1");
        }
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notice);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setCustomBigContentView(remoteViews);
        } else {
            builder.setContent(remoteViews);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setOngoing(true);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setAutoCancel(true);
        //锁屏显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        notificationManager.notify(1, builder.build());
    }
}
