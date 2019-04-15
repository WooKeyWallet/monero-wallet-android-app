package io.wookey.wallet.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GradeIndicator extends View {

    public static final float DENSITY = Resources.getSystem()
            .getDisplayMetrics().density;

    private Paint mPaint;
    private int mTotalGrade = 4;
    private int mCurrentGrade = 0;

    public GradeIndicator(Context context) {
        super(context);
        init();
    }

    public GradeIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradeIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int top = 0;
        float height = ((float) getHeight()) / (mTotalGrade * 2 - 1);
        for (int i = 0; i < mTotalGrade; i++) {
            if (i < mTotalGrade - mCurrentGrade) {
                mPaint.setColor(Color.parseColor("#DEDEDE"));
            } else {
                mPaint.setColor(Color.parseColor("#26B479"));
            }
            canvas.drawRect(0, top, width, top + height, mPaint);
            top += height * 2;
        }
    }

    public void setTotalGrade(int total) {
        if (total < mCurrentGrade) {
            throw new IllegalArgumentException("total can't smaller than current");
        }
        mTotalGrade = total;
        requestLayout();
        invalidate();
    }

    public void setCurrentGrade(int current) {
        if (current > mTotalGrade) {
            throw new IllegalArgumentException("current can't bigger than total");
        }
        mCurrentGrade = current;
        requestLayout();
        invalidate();
    }

    private static float dp2px(float dp) {
        return (int) (dp * DENSITY + 0.5f);
    }
}
