package io.wookey.wallet.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

public class CancelledEditText extends AppCompatEditText {

    private Drawable[] mDrawables;
    private Drawable mCancelledDrawable;

    public CancelledEditText(Context context) {
        super(context);
        init();
    }

    public CancelledEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CancelledEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDrawables = getCompoundDrawablesRelative();
        mCancelledDrawable = mDrawables[2];
        setRightIcon(null);
    }

    public void setRightIcon(Drawable drawable) {
        setCompoundDrawablesWithIntrinsicBounds(mDrawables[0], mDrawables[1], drawable, mDrawables[3]);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        // 从有文字删除到无文字
        if ((s == null || s.length() == 0) && before > 0) {
            startAnimator(255, 0);
            return;
        }

        // 从无文字到有文字
        if ((s != null && s.length() > 0) && start == 0) {
            setRightIcon(mCancelledDrawable);
            startAnimator(0, 255);
        }
    }

    private void startAnimator(int start, int end) {
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mCancelledDrawable != null) {
                    mCancelledDrawable.setAlpha((Integer) animation.getAnimatedValue());
                }
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                valueAnimator.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mCancelledDrawable != null) {
                    if (event.getX() <= (getWidth() - getPaddingRight())
                            && event.getX() >= (getWidth() - getPaddingRight() - mCancelledDrawable.getBounds().width())) {
                        setText("");
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
