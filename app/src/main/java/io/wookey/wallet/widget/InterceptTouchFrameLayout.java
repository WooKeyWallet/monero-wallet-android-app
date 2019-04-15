package io.wookey.wallet.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class InterceptTouchFrameLayout extends FrameLayout {
    public InterceptTouchFrameLayout(Context context) {
        super(context);
    }

    public InterceptTouchFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptTouchFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InterceptTouchFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
