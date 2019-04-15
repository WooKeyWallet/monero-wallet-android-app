package io.wookey.wallet.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

/**
 * DividerItemDecoration is a {@link RecyclerView.ItemDecoration} that can be used as a divider
 * between items of a {@link LinearLayoutManager}. It supports both {@link #HORIZONTAL} and
 * {@link #VERTICAL} orientations.
 *
 * <pre>
 *     mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
 *             mLayoutManager.getOrientation());
 *     recyclerView.addItemDecoration(mDividerItemDecoration);
 * </pre>
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private static final String TAG = "DividerItem";

    private int mDividerColor = Color.parseColor("#DEDEDE");
    private int mDividerSize = 1;
    private Paint mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    private int mOrientation;

    private final Rect mBounds = new Rect();
    private int mMarginStart;
    private int mMarginTop;
    private int mMarginEnd;
    private int mMarginBottom;


    /**
     * Sets the orientation for this divider. This should be called if
     * {@link RecyclerView.LayoutManager} changes orientation.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public DividerItemDecoration setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
        return this;
    }

    public DividerItemDecoration setDividerColor(int color) {
        mDividerColor = color;
        return this;
    }

    public DividerItemDecoration setDividerSize(int size) {
        mDividerSize = size;
        return this;
    }

    public DividerItemDecoration setMarginStart(int margin) {
        mMarginStart = margin;
        return this;
    }

    public DividerItemDecoration setMarginTop(int margin) {
        mMarginTop = margin;
        return this;
    }

    public DividerItemDecoration setMarginEnd(int margin) {
        mMarginEnd = margin;
        return this;
    }

    public DividerItemDecoration setMarginBottom(int margin) {
        mMarginBottom = margin;
        return this;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int left;
        final int right;
        //noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
            final int top = bottom - mDividerSize;
            mDividerPaint.setColor(mDividerColor);
            mDividerPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(left + mMarginStart, top + mMarginTop, right - mMarginEnd, bottom - mMarginBottom, mDividerPaint);
        }
        canvas.restore();
    }

    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int top;
        final int bottom;
        //noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            canvas.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
            final int right = mBounds.right + Math.round(child.getTranslationX());
            final int left = right - mDividerSize;
            mDividerPaint.setColor(mDividerColor);
            mDividerPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(left + mMarginStart, top + mMarginTop, right - mMarginEnd, bottom - mMarginBottom, mDividerPaint);
        }
        canvas.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            outRect.set(0, 0, 0, mDividerSize);
        } else {
            outRect.set(0, 0, mDividerSize, 0);
        }
    }
}
