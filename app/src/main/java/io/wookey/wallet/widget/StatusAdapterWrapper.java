package io.wookey.wallet.widget;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.wookey.wallet.R;
import io.wookey.wallet.support.BackgroundHelper;
import io.wookey.wallet.support.utils.DisplayHelper;

public class StatusAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //无需加载中
    private static final int STATUS_LOADING_NONE = 100;
    //加载中
    private static final int STATUS_LOADING = 200;
    //空数据
    private static final int STATUS_EMPTY = 300;
    //加载错误
    private static final int STATUS_ERROR = 400;
    //正常状态
    private static final int STATUS_NORMAL = 500;

    @IntDef({STATUS_LOADING_NONE, STATUS_LOADING, STATUS_EMPTY, STATUS_ERROR,
            STATUS_NORMAL})
    public @interface AdapterStatus {
    }

    @AdapterStatus
    private int mStatus = STATUS_LOADING;

    private final RecyclerView.AdapterDataObserver mObserver =
            new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    //数据为空时自动显示空数据状态
                    if (mStatus == STATUS_NORMAL && getItemCount() == 0) {
                        setEmptyView();
                    }
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                }
            };


    private final RecyclerView.Adapter mInnerAdapter;

    public StatusAdapterWrapper(RecyclerView.Adapter innerAdapter) {
        this.mInnerAdapter = innerAdapter;
        registerAdapterDataObserver(mObserver);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case STATUS_LOADING_NONE:
                return null;
            case STATUS_LOADING:
                return createLoadingViewHolder(parent);
            case STATUS_EMPTY:
                return createEmptyViewHolder(parent);
            case STATUS_ERROR:
                return createErrorViewHolder(parent);
            case STATUS_NORMAL:
            default:
                return mInnerAdapter.onCreateViewHolder(parent, viewType);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mStatus == STATUS_NORMAL) {
            mInnerAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        switch (mStatus) {
            case STATUS_LOADING_NONE:
                return 0;
            case STATUS_LOADING:
            case STATUS_EMPTY:
            case STATUS_ERROR:
                return 1;
            case STATUS_NORMAL:
            default:
                return mInnerAdapter.getItemCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mStatus == STATUS_NORMAL) {
            return mInnerAdapter.getItemViewType(position);
        }
        return mStatus;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        loadMore(recyclerView, recyclerView.getLayoutManager());
    }

    private void loadMore(RecyclerView recyclerView, final RecyclerView.LayoutManager layoutManager) {
        //加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mStatus != STATUS_NORMAL) {
                    return;
                }
                int lastVisibleItemPosition = findLastVisibleItemPosition(layoutManager);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition + 1 == getItemCount()) {
                        onLoadMore();
                    }
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public static int findLastVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
            return findMax(lastVisibleItemPositions);
        }
        return -1;
    }

    /**
     * StaggeredGridLayoutManager时，查找position最大的列
     *
     * @param lastVisiblePositions
     * @return
     */
    public static int findMax(int[] lastVisiblePositions) {
        int max = lastVisiblePositions[0];
        for (int value : lastVisiblePositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public RecyclerView.ViewHolder createLoadingViewHolder(ViewGroup parent) {
        FrameLayout frameLayout = new FrameLayout(parent.getContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.setLayoutParams(lp);

        ProgressBar progressBar = new ProgressBar(parent.getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
        frameLayout.addView(progressBar);

        return new StatusViewHolder(frameLayout);
    }

    public RecyclerView.ViewHolder createErrorViewHolder(ViewGroup parent) {

        LinearLayout linearLayout = new LinearLayout(parent.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lp);

        ImageView imageView = new ImageView(parent.getContext());
        imageView.setImageResource(R.drawable.icon_no_net);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ivLp.gravity = Gravity.CENTER_HORIZONTAL;
        ivLp.topMargin = getTopMargin();
        imageView.setLayoutParams(ivLp);
        linearLayout.addView(imageView);

        TextView textView = new TextView(parent.getContext());
        textView.setText(R.string.no_net);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_9E9E9E));
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLp.gravity = Gravity.CENTER_HORIZONTAL;
        tvLp.topMargin = DisplayHelper.dpToPx(10);
        textView.setLayoutParams(tvLp);
        linearLayout.addView(textView);

        if (getErrorActionVisibility() == View.VISIBLE) {
            TextView actionView = new TextView(parent.getContext());
            LinearLayout.LayoutParams actionLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, DisplayHelper.dpToPx(50));
            actionLp.gravity = Gravity.CENTER_HORIZONTAL;
            actionLp.topMargin = DisplayHelper.dpToPx(50);
            actionView.setLayoutParams(actionLp);
            actionView.setText(R.string.add_address);
            actionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            actionView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_FFFFFF));
            actionView.setBackground(BackgroundHelper.getButtonBackground(parent.getContext(), R.color.color_002C6D));
            actionView.setGravity(Gravity.CENTER);
            actionView.setPadding(DisplayHelper.dpToPx(40), 0, DisplayHelper.dpToPx(40), 0);
            actionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onErrorClick();
                }
            });
            linearLayout.addView(actionView);
        }

        return new StatusViewHolder(linearLayout);
    }

    public RecyclerView.ViewHolder createEmptyViewHolder(ViewGroup parent) {

        LinearLayout linearLayout = new LinearLayout(parent.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lp);

        ImageView imageView = new ImageView(parent.getContext());
        imageView.setImageResource(getEmptyImageResource());
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ivLp.gravity = Gravity.CENTER_HORIZONTAL;
        ivLp.topMargin = getTopMargin();
        imageView.setLayoutParams(ivLp);
        linearLayout.addView(imageView);

        TextView textView = new TextView(parent.getContext());
        textView.setText(getEmptyStringResource());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_9E9E9E));
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLp.gravity = Gravity.CENTER_HORIZONTAL;
        tvLp.topMargin = DisplayHelper.dpToPx(10);
        textView.setLayoutParams(tvLp);
        linearLayout.addView(textView);

        if (getEmptyActionVisibility() == View.VISIBLE) {
            TextView actionView = new TextView(parent.getContext());
            LinearLayout.LayoutParams actionLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, DisplayHelper.dpToPx(50));
            actionLp.gravity = Gravity.CENTER_HORIZONTAL;
            actionLp.topMargin = DisplayHelper.dpToPx(50);
            actionView.setLayoutParams(actionLp);
            actionView.setText(R.string.add_address);
            actionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            actionView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_FFFFFF));
            actionView.setBackground(BackgroundHelper.getButtonBackground(parent.getContext(), R.color.color_002C6D));
            actionView.setGravity(Gravity.CENTER);
            actionView.setPadding(DisplayHelper.dpToPx(40), 0, DisplayHelper.dpToPx(40), 0);
            actionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEmptyClick();
                }
            });
            linearLayout.addView(actionView);
        }

        return new StatusViewHolder(linearLayout);
    }

    public int getTopMargin() {
        return DisplayHelper.dpToPx(95);
    }

    public int getErrorActionVisibility() {
        return View.GONE;
    }

    public int getEmptyImageResource() {
        return R.drawable.icon_no_address;
    }

    public int getEmptyStringResource() {
        return R.string.no_address;
    }

    public int getEmptyActionVisibility() {
        return View.GONE;
    }

    public void setLoadingViewNone() {
        this.mStatus = STATUS_LOADING_NONE;
        notifyDataSetChanged();
    }

    public void setLoadingView() {
        this.mStatus = STATUS_LOADING;
        notifyDataSetChanged();
    }

    public void setEmptyView() {
        this.mStatus = STATUS_EMPTY;
        notifyDataSetChanged();
    }

    public void setErrorView() {
        this.mStatus = STATUS_ERROR;
        notifyDataSetChanged();
    }

    public void setSuccessView() {
        this.mStatus = STATUS_NORMAL;
        notifyDataSetChanged();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {
        public StatusViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void onErrorClick() {

    }

    public void onEmptyClick() {

    }

    public void onLoadMore() {

    }

}
