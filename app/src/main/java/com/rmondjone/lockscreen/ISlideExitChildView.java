package com.rmondjone.lockscreen;

import android.view.MotionEvent;

/**
 * Created by zhangzheng on 2017/8/2.
 */
public interface ISlideExitChildView {

    boolean intercept(MotionEvent ev);
}
