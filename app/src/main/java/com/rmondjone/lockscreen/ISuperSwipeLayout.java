package com.rmondjone.lockscreen;

import android.graphics.Canvas;

/**
 * Created by zhangzheng on 2017/7/27.
 */
public interface ISuperSwipeLayout {

    int hashCode();

    boolean isUserCacheBitmap();

    int getMeasuredWidth();

    int getMeasuredHeight();

    void dispatchDraw(Canvas canvas);

    void setOffset(float offset);
}
