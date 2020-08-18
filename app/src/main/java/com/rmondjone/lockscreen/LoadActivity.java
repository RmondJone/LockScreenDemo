package com.rmondjone.lockscreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.Permission;

/**
 * @author 郭翰林
 * @date 2020/8/17 0017 11:31
 * 注释:
 */
public class LoadActivity extends AppCompatActivity {
    private Button lockButton;
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
        lockButton = findViewById(R.id.button_lock_notice);
        lockButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LockScreenNotice.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.unregister();
        }
    }
}
