package com.rmondjone.lockscreen;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

/**
 * Created by zhangzheng on 2017/7/13.
 */
public class AppUtils {
    private static final String TAG = "AppUtils";

    private static int STATUSBAR_HEIGHT = -1;

    public static boolean isContairStatusBar() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
    }

    public static int getStatusBarHeight(Context context) {
        if (STATUSBAR_HEIGHT == -1) {
            STATUSBAR_HEIGHT = getStatusHeightImp(context);
        }
        return STATUSBAR_HEIGHT;
    }

    private static int getStatusHeightImp(Context context) {
        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 获取应用版本号
     *
     * @param context
     * @return 版本号
     */
    public static String getAppVersion(Context context) {
        String versionName = "1.0.0";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            Log.e(TAG, "VersionInfo", e);
        }
        return versionName;
    }

    /**
     * 注释：是不是手机号
     * 时间：2018/9/26 0026 8:55
     * 作者：郭翰林
     *
     * @param phone
     * @return
     */
    public static boolean isValidMobilePhone(String phone) {
        return phone.startsWith("1") && phone.length() == 11;
    }

    /**
     * 注释：根据名称获取资源ID
     * 时间：2018/12/7 0007 14:07
     * 作者：郭翰林
     *
     * @param context
     * @param resourceName
     * @return
     */
    public static int getResourceIdFromName(Context context, String resourceName) {
        Resources res = context.getResources();
        int picid = res.getIdentifier(resourceName, "drawable", context.getPackageName());
        return picid;
    }

}
