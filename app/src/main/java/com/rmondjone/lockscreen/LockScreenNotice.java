package com.rmondjone.lockscreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author 郭翰林
 * @date 2020/8/18 0018 9:06
 * 注释:锁屏通知
 */
public class LockScreenNotice extends AppCompatActivity {
    private Button lockNoticeButton;
    private Button stopNoticeButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        //设置锁屏通知
        lockNoticeButton = findViewById(R.id.button_lock_notice);
        lockNoticeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationService.class);
            startService(intent);
        });
        //停止锁屏通知
        stopNoticeButton = findViewById(R.id.button_stop_notice);
        stopNoticeButton.setOnClickListener(v -> {
            Intent intent = new Intent("com.rmondjone.lockscreen.close");
            intent.setClassName(this, "com.rmondjone.lockscreen.NotificationReceiver");
            sendBroadcast(intent);
        });
    }
}
