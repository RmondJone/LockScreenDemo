package com.rmondjone.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

/**
 * @author 郭翰林
 * @date 2020/8/18 0018 14:36
 * 注释:
 */
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.rmondjone.lockscreen.close".equals(action)) {
            EventBus.getDefault().post(new NoticeEvent());
            Log.e("NotificationReceiver", "关闭Service通知");
        }
    }
}
