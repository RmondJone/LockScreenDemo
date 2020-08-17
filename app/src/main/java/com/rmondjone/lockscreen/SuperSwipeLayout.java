package com.rmondjone.lockscreen;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhangzheng on 2016/11/2.
 */

public class SuperSwipeLayout extends LinearLayout implements ISuperSwipeLayout {

    public static List<ISuperSwipeLayout> swiplayouts = new ArrayList<>();
    private final int SCREEN_WIDTH = DeviceUtils.getScreenWidth(getContext());
    private final Window window;
    private float offset;
    private VelocityTracker velocity;
    private boolean isClose;
    private Drawable mShadowDrawable;
    private boolean isSwipe = true;
    private Rect rect = new Rect();
    private Paint paint = new Paint();
    private boolean isUserCacheBitmap = true;

    public void setSwipe(boolean swipe) {
        isSwipe = swipe;
    }

    @Override
    public boolean isUserCacheBitmap() {
        return isUserCacheBitmap;
    }

    public void setUserCacheBitmap(boolean userCacheBitmap) {
        isUserCacheBitmap = userCacheBitmap;
    }

    public SuperSwipeLayout(View childView, Window window) {
        super(childView.getContext());
        this.window = window;
        setOrientation(VERTICAL);
        addView(childView);
        setClickable(true);
        mShadowDrawable = getResources().getDrawable(R.mipmap.shadow_left);
        paint.setColor(Color.parseColor("#E84820"));
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        swiplayouts.add(this);
    }


    @Override
    protected void onDetachedFromWindow() {
        setOffset(SCREEN_WIDTH);
        super.onDetachedFromWindow();
        swiplayouts.remove(this);
    }


    @Override
    public void dispatchDraw(Canvas canvas) {
        synchronized (this) {
            int save = canvas.save();
            canvas.translate(offset, 0);
            drawShadow(canvas);
            super.dispatchDraw(canvas);
            canvas.restoreToCount(save);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        synchronized (this) {
            super.draw(canvas);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            setOffset(0);
            invalidate();
        }
    }

    private void drawShadow(Canvas canvas) {
        mShadowDrawable.setBounds(-mShadowDrawable.getIntrinsicWidth(), 0, 0, getBottom());
        mShadowDrawable.draw(canvas);
    }

    private ISuperSwipeLayout getPreSwipLayout() {
        if (swiplayouts.size() > 1) {
            ISuperSwipeLayout swipLayout = swiplayouts.get(swiplayouts.size() - 2);
            if (swipLayout != this) {
                return swipLayout;
            }
        }
        return null;
    }

    private float downX;
    private float downY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSwipe) {
            return false;
        }
        if (isInterceptByChild(ev, this)) {
            return false;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = ev.getRawX();
            downY = ev.getRawY();
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float ofsetX = ev.getRawX() - downX;
            float ofsetY = Math.abs(ev.getRawY() - downY);
            int criticalValue = ev.getRawX() < AppUtils.getStatusBarHeight(getContext()) ? 4 : 10;
            if (downX < DeviceUtils.dip2px(getContext(), 40)
                    && downY > AppUtils.getStatusBarHeight(getContext()) + DeviceUtils.dip2px(getContext(), 50)
                    && ofsetX > criticalValue && ofsetY < 50) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isInterceptByChild(MotionEvent ev, View view) {
        if (view instanceof ISlideExitChildView
                && ((ISlideExitChildView) view).intercept(ev) && eventPointInView(ev, view)) {
            return true;
        }
        if (view instanceof ViewGroup) {
            int childCount = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                if (isInterceptByChild(ev, child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Rect childViewRect = new Rect();

    private boolean eventPointInView(MotionEvent ev, View view) {
        view.getGlobalVisibleRect(childViewRect);
        return childViewRect.contains((int) ev.getRawX(), (int) ev.getRawY());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isSwipe) {
            return false;
        }
        if (velocity == null) {
            velocity = VelocityTracker.obtain();
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = ev.getX();
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            velocity.addMovement(ev);
            velocity.computeCurrentVelocity(1000);
            float offset = ev.getX() - downX;
            if (offset < 0) {
                offset = 0;
            }
            if (offset > SCREEN_WIDTH) {
                offset = SCREEN_WIDTH;
            }
            setOffset(offset);
        } else if (ev.getAction() == MotionEvent.ACTION_UP
                || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            cancleAnim(velocity.getXVelocity(), velocity.getYVelocity());
            velocity.recycle();
            velocity = null;
        }
        return super.onTouchEvent(ev);
    }

    private void cancleAnim(float XVelocity, float YVelocity) {
        final float endOffset = (offset > SCREEN_WIDTH / 2 || (XVelocity > 2000 && XVelocity > YVelocity * 1.5)) ? SCREEN_WIDTH : 0;
        isClose = endOffset == SCREEN_WIDTH;
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "Offset", offset, endOffset);
        anim.setDuration((long) (Math.abs(endOffset - offset) / 3));
        anim.start();
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isClose) {
                    if (isDialog()) {
                        SuperSwipeLayout.this.setVisibility(View.GONE);
                        SuperSwipeLayout.this.post(new Runnable() {
                            @Override
                            public void run() {
                                if (getDialog() != null) {
                                    getDialog().dismiss();
                                }
                            }
                        });
                    } else {
                        if (getActivity() != null) {
                            //解决页面中如果包含SurfaceView,由于SurfaceView和其他视图处于不同视图容器中,待侧滑动画执行完毕之后SurfaceView会再次出现的BUG
                            //如果把下一行注释，则涉及到相机调用的页面侧滑退出会出现回闪的现象
                            //参考链接：https://blog.csdn.net/TuGeLe/article/details/79199119
                            setSurfaceViewGone(getActivity().getWindow().getDecorView());
                            getActivity().finish();
                        }
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void setOffset(float offset) {
        if (offset == this.offset) {
            return;
        }
        this.offset = offset;
        invalidate();
        offsetPreLayoutIfNotCache();
    }

    /**
     * 注释：视图中如果包含SurfaceView则设置为隐藏
     * 时间：2018/10/23 0023 17:43
     * 作者：郭翰林
     *
     * @param view
     */
    private void setSurfaceViewGone(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = ((ViewGroup) view);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt instanceof SurfaceView) {
                    childAt.setVisibility(GONE);
                }
                //递归子视图
                setSurfaceViewGone(childAt);
            }
        }
    }

    private void offsetPreLayoutIfNotCache() {
        ISuperSwipeLayout preSwipLayout = getPreSwipLayout();
        if (preSwipLayout != null) {
            preSwipLayout.setOffset(-SCREEN_WIDTH / 2 + offset / 2);
        }
    }

    private Activity getActivity() {
        if (!isDialog()) {
            return (Activity) getContext();
        }
        return null;
    }

    private Dialog getDialog() {
        if (isDialog()) {
            return (Dialog) getWindow().getCallback();
        }
        return null;
    }

    private Window getWindow() {
        return window;
    }

    private boolean isDialog() {
        return !(getContext() instanceof Activity);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect.set(0, 0, w, h);
    }


}
