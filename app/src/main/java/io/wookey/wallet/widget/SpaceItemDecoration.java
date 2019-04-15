package io.wookey.wallet.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int left;
    private int top;
    private int right;
    private int bottom;

    public SpaceItemDecoration(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = left;
        outRect.right = right;
        outRect.bottom = bottom;
        outRect.top = top;
//        if (parent.getChildLayoutPosition(view) == 0) {
//            outRect.top = top;
//        } else {
//            outRect.top = 0;
//        }
    }
}
