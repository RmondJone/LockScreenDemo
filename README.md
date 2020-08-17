Android自定义锁屏

### 一、使用场景
某些场景，需要监听用户的锁屏事件，再次打开锁屏之后显示自己的锁屏页面，这个锁屏页面有可能在做一些计时等操作，类似于Keep的锁屏跑步计时功能。
### 二、代码实现
废话不多，直接上代码，我们假设有2个Activity，一个A，一个B。在A中监听用户的锁屏，B是我们自定义的锁屏页面。

页面A的实现
```java
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
        //请求存储权限
        PermissionUtils.applicationPermissions(this, new PermissionUtils.PermissionListener() {
            @Override
            public void onSuccess(Context context) {
            }

            @Override
            public void onFailed(Context context) {

            }
        //在锁屏页面显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        setContentView(R.layout.activity_load);
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

```
这边页面A页必须要设置锁屏页显示，否则无法正常调整，另外跳转B时需要添加`Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS`标记
```java
//在锁屏页面显示
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
   setShowWhenLocked(true);
} else {
   getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
}

//跳转添加标记
Intent intent = new Intent(activity, MainActivity.class);
intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
activity.startActivity(intent);
```
页面B的实现
```java
package com.rmondjone.lockscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        //开启侧滑退出
        openSlidingExitToWindow(getWindow(), false);
        setContentView(R.layout.activity_main);
        mImageView=findViewById(R.id.img_back);
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
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activity.startActivity(intent);
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

```

这里有几个点，需要详细描述，一个是页面B需要设置背景透明的主题，要不然侧滑时看不到页面A。
```xml
    <activity android:name=".MainActivity" android:theme="@style/TransparentTheme"/>
```
```xml
    <style name="TransparentTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:colorBackgroundCacheHint">@null</item>
        <item name="android:windowIsTranslucent">true</item>
        <item name="android:windowAnimationStyle">@android:style/Animation</item>
    </style>
```
我们这边是使用了桌面主题壁纸作为页面B的背景，当然这边你也可以自定义背景，这个都无所谓，但是如果你用了桌面的壁纸就需要注意权限的申请，这边需要用到存储权限。
```java
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

    mImageView=findViewById(R.id.img_back);
    mImageView.setImageBitmap(getWallPaper());
```
另一个就是返回键的屏蔽。
```java
    @Override
    public void onBackPressed() {

    }
```
三、Demo地址
#### 欢迎Star: [https://github.com/RmondJone/LockScreenDemo](https://github.com/RmondJone/LockScreenDemo)

