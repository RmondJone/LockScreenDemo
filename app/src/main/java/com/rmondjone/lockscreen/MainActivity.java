package com.rmondjone.lockscreen;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.Permission;

public class MainActivity extends AppCompatActivity {
    private SuperSwipeLayout swipLayout;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置导航栏透明
        StatusBarHelper.fitsSystemWindows(this);
        StatusBarHelper.translucent(this);
        //在锁屏页面显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        //开启侧滑退出
        openSlidingExitToWindow(getWindow(), false);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.img_back);
        mImageView.setImageBitmap(getWallPaper());
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * 注释：跳转
     * 时间：2020/8/17 0017 11:37
     * 作者：郭翰林
     *
     * @param activity
     */
    public static void gotoMe(Activity activity) {
        PermissionUtils.applicationPermissions(activity, new PermissionUtils.PermissionListener() {
            @Override
            public void onSuccess(Context context) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                activity.startActivity(intent);
            }

            @Override
            public void onFailed(Context context) {

            }
        }, Permission.Group.STORAGE);
    }

    /**
     * 注释：打开侧滑退出
     * 时间：2020/8/17 0017 11:36
     * 作者：郭翰林
     *
     * @param window
     * @param addReplaceStatus
     */
    public void openSlidingExitToWindow(Window window, boolean addReplaceStatus) {
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        View childContair = decorView.getChildAt(0);
        decorView.removeView(childContair);

        swipLayout = new SuperSwipeLayout(childContair, window);

        LinearLayout ll_contair = new LinearLayout(childContair.getContext());
        ll_contair.setOrientation(LinearLayout.VERTICAL);
        if (addReplaceStatus) {
            swipLayout.addView(getStatusBarView(swipLayout.getContext()), 0);
        }
        ll_contair.addView(swipLayout, new LinearLayout.LayoutParams(-1, -1));

        decorView.addView(ll_contair, new FrameLayout.LayoutParams(-1, -1));
        //开启侧滑
        swipLayout.setSwipe(true);
    }

    private View getStatusBarView(Context context) {
        View statusBarView = new View(context);
        statusBarView.setBackgroundResource(android.R.color.transparent);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(-1, AppUtils.getStatusBarHeight(context));
        statusBarView.setLayoutParams(lp);
        return statusBarView;
    }

    /**
     * 注释：获取系统壁纸
     * 时间：2020/8/17 0017 10:18
     * 作者：郭翰林
     *
     * @return
     */
    public Bitmap getWallPaper() {
        WallpaperManager wallpaperManager = WallpaperManager
                .getInstance(MainActivity.this);
        // 获取当前壁纸
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();

        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        int with = bm.getHeight() * widthPixels / heightPixels > bm.getWidth() ? bm.getWidth() : bm.getHeight() * widthPixels / heightPixels;
        Bitmap pbm = Bitmap.createBitmap(bm, 0, 0, with, bm.getHeight());
        // 设置 背景
        return pbm;
    }
}
