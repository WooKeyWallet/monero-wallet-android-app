package io.wookey.wallet.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import io.wookey.wallet.R;

/**
 * 自定义模仿IOS效果的Dialog
 */

public class IOSDialog implements View.OnClickListener {

    /**
     * utils
     */
    private final int LEFT = 0;
    private final int RIGHT = 1;

    /**
     * 资源ID
     */
    @android.support.annotation.IdRes
    int mTitleView_ID = R.id.ios_dialog_title;
    @android.support.annotation.IdRes
    int mContentView_ID = R.id.ios_dialog_content;
    @android.support.annotation.IdRes
    int mBottomView_ID = R.id.ios_dialog_bottom;

    private Dialog mDialog = null;
    private Context mContext;
    private DisplayMetrics mDisplayMetrics = null;
    private IOSDialogLeftListener mLeftListener = null;
    private IOSDialogRightListener mRightListener = null;
    private int mBottomDefaultColor = Color.rgb(9, 145, 252);  //底部文字的默认颜色
    private boolean mHasContentView = true;
    private float mWindowWidthPercentage = 0.75f; //宽所占的屏幕的百分比
    private int mWindowVerticalMargin = 50; //dp
    private boolean mContentViewCanScroll = false;  //ContentView是否可以滑动
    private boolean mIsOnlyRight = false;           //不是只有右边

    /**
     * 对话框的根布局
     */
    private RelativeLayout mRootLayout = null;


    /**
     * 对话框背景
     */
    private GradientDrawable mBackgroundShape = new GradientDrawable(); //对话框背景色
    private int mBackgroundShapeColor = Color.rgb(255, 255, 255);  //背景颜色
    private int mRadius = 10;  //背景圆角矩形的弧度大小 dp

    /**
     * 标题视图
     */
    private TextView mTitleView = null;       //标题
    private String mTitleText = "标题";    //标题内容
    private int mTitleTextSize = 18;   //标题文字大小；单位sp
    private int mTitleTextColor = Color.BLACK;
    private int mTitleViewPaddingLeft;     //dp
    private int mTitleViewPaddingRight;    //dp
    private int mTitleViewPaddingTop = 15;   //dp
    private int mTitleViewPaddingBottom = 5; //dp

    /**
     * 内容视图
     */
    private TextView mContentView = null;     //内容
    private String mContentText = "内容";
    private int mContentTextSize = 14;     //sp
    private int mContentTextColor = Color.BLACK;
    private boolean mContentTextBold = false;
    private int mContentViewPaddingLeft = 20;   //dp
    private int mContentViewPaddingRight = 20;  //dp
    private int mContentViewPaddingTop = 0;    //dp
    private int mContentViewPaddingBottom = 20;  //dp


    /**
     * 底部视图
     */
    private LinearLayout mBottomLayout;

    /**
     * 底部左边文字视图
     */
    private TextView mLeftView = null;       //下边左方按钮
    private String mLeftText = "取消";
    private int mLeftTextSize = 14;       //sp
    private int mLeftTextColor = mBottomDefaultColor;

    /**
     * 底部右边文字视图
     */
    private TextView mRightView = null;      //下边右方按钮
    private String mRightText = "确定";
    private int mRightTextSize = 14;      //sp
    private int mRightTextColor = mBottomDefaultColor;

    /**
     * 线条的颜色
     */
    private int mLineColor = Color.rgb(203, 203, 203);   //横线，竖线颜色


    @Override
    public void onClick(View v) {
        int result = (int) v.getTag();
        if (result == LEFT) {
            dismiss();
            if (mLeftListener != null) {
                mLeftListener.onClick(mDialog);
            }
        } else if (result == RIGHT) {
            dismiss();
            if (mRightListener != null) {
                mRightListener.onClick(mDialog);
            }
        }
    }

    public interface IOSDialogLeftListener {
        public void onClick(Dialog dialog);
    }

    public interface IOSDialogRightListener {
        public void onClick(Dialog dialog);
    }

    public IOSDialog(Context context) {
        mContext = context;
        mDisplayMetrics = mContext.getResources().getDisplayMetrics();
        mRadius = (int) (mRadius * mDisplayMetrics.density);   //5dp
        mDialog = new Dialog(mContext); //创建对话框
        mDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }


    /**
     * 设置为可滑动ContentView时监听,动态修改高度
     */
    ViewTreeObserver.OnGlobalLayoutListener mMeasureHeightListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int sum = mTitleView.getHeight() + mContentView.getHeight() + mBottomLayout.getHeight();
            int statusBarHeight = 0;
            int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                statusBarHeight = mContext.getResources().getDimensionPixelSize(resourceId);
            }
            if (sum <= (mDisplayMetrics.heightPixels - mWindowVerticalMargin * 2 * mDisplayMetrics.density - statusBarHeight)) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, sum);
                mRootLayout.setLayoutParams(params);
            }
            if (Build.VERSION.SDK_INT >= 16) {
                mRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
                mRootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        }
    };

    //加载基本布局
    public IOSDialog layout() {

        mBackgroundShape.setColor(mBackgroundShapeColor);
        mBackgroundShape.setCornerRadius(mRadius);  //pixel
        mRootLayout = new RelativeLayout(mContext);
        mRootLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        if (mHasContentView) {
            if (mContentViewCanScroll) {
                mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(mMeasureHeightListener);
            }
        }

        mTitleView = new TextView(mContext);
        mTitleView.setText(mTitleText);
        mTitleView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        mTitleView.setTextColor(mTitleTextColor);
        mTitleView.setTextSize(mTitleTextSize);
        mTitleView.setGravity(Gravity.CENTER);
        mTitleView.setId(mTitleView_ID);
        mTitleView.setPadding((int) (mTitleViewPaddingLeft * mDisplayMetrics.density), (int) (mTitleViewPaddingTop * mDisplayMetrics.density),
                (int) (mTitleViewPaddingRight * mDisplayMetrics.density), (int) (mTitleViewPaddingBottom * mDisplayMetrics.density));
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mTitleView.setLayoutParams(titleParams);
        mRootLayout.addView(mTitleView);//添加标题

        if (mHasContentView) {
            if (mContentViewCanScroll) {  //内容可滑动
                ScrollView mScrollView = new ScrollView(mContext);
                mScrollView.setId(mContentView_ID);
                RelativeLayout.LayoutParams scrollViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                scrollViewParams.addRule(RelativeLayout.BELOW, mTitleView_ID);
                scrollViewParams.addRule(RelativeLayout.ABOVE, mBottomView_ID);
                mScrollView.setLayoutParams(scrollViewParams);

                mContentView = new TextView(mContext);
                mContentView.setText(mContentText);
                if (mContentTextBold) {
                    mContentView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                }
                mContentView.setTextColor(mContentTextColor);
                mContentView.setTextSize(mContentTextSize);
                mContentView.setGravity(Gravity.CENTER_HORIZONTAL);
                mContentView.setPadding((int) (mContentViewPaddingLeft * mDisplayMetrics.density), (int) (mContentViewPaddingTop * mDisplayMetrics.density),
                        (int) (mContentViewPaddingRight * mDisplayMetrics.density), (int) (mContentViewPaddingBottom * mDisplayMetrics.density));
                mContentView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
                mScrollView.addView(mContentView);

                mRootLayout.addView(mScrollView);//添加内容

            } else {   //内容不可滑动
                mContentView = new TextView(mContext);
                mContentView.setText(mContentText);
                if (mContentTextBold) {
                    mContentView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                }
                mContentView.setId(mContentView_ID);
                mContentView.setTextColor(mContentTextColor);
                mContentView.setTextSize(mContentTextSize);
                mContentView.setGravity(Gravity.CENTER_HORIZONTAL);
                mContentView.setPadding((int) (mContentViewPaddingLeft * mDisplayMetrics.density), (int) (mContentViewPaddingTop * mDisplayMetrics.density),
                        (int) (mContentViewPaddingRight * mDisplayMetrics.density), (int) (mContentViewPaddingBottom * mDisplayMetrics.density));
                RelativeLayout.LayoutParams scrollView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                scrollView.addRule(RelativeLayout.BELOW, mTitleView_ID);
                mContentView.setLayoutParams(scrollView);

                mRootLayout.addView(mContentView);//添加内容
            }
        }

        //底部部分
        mBottomLayout = new LinearLayout(mContext);
        mBottomLayout.setId(mBottomView_ID);
        RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (mHasContentView) { //含有ContentView
            if (mContentViewCanScroll) {
                layoutParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            } else {
                layoutParam.addRule(RelativeLayout.BELOW, mContentView_ID);
            }
        } else {  //不含ContventView
            layoutParam.addRule(RelativeLayout.BELOW, mTitleView_ID);
        }

        mBottomLayout.setLayoutParams(layoutParam);
        mBottomLayout.setOrientation(LinearLayout.VERTICAL);

        TextView hozLine = new TextView(mContext);
        LinearLayout.LayoutParams lineParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        hozLine.setLayoutParams(lineParam);
        hozLine.setBackgroundColor(mLineColor);
        mBottomLayout.addView(hozLine); //添加横线


        LinearLayout mBottom = new LinearLayout(mContext);
        mBottom.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mBottom.setGravity(Gravity.CENTER_VERTICAL);
        mBottom.setOrientation(LinearLayout.HORIZONTAL);

        if (!mIsOnlyRight) {
            mLeftView = new TextView(mContext);
            mLeftView.getPaint().setFakeBoldText(true);
            mLeftView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            mLeftView.setText(mLeftText);
            mLeftView.setPadding(0, (int) (15 * mDisplayMetrics.density), 0, (int) (15 * mDisplayMetrics.density));
            mLeftView.setTextColor(mLeftTextColor);
            mLeftView.setTextSize(mLeftTextSize);
            mLeftView.setGravity(Gravity.CENTER);
            mLeftView.setTag(LEFT);
            mLeftView.setOnClickListener(this);

            TextView verLine = new TextView(mContext);
            verLine.setLayoutParams(new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT));
            verLine.setBackgroundColor(mLineColor);

            mBottom.addView(mLeftView);
            mBottom.addView(verLine);
        }

        mRightView = new TextView(mContext);
        mRightView.getPaint().setFakeBoldText(true);//加粗
        mRightView.setText(mRightText);
        mRightView.setTextColor(mRightTextColor);
        mRightView.setTextSize(mRightTextSize);
        mRightView.setGravity(Gravity.CENTER);
        mRightView.setPadding(0, (int) (15 * mDisplayMetrics.density), 0, (int) (15 * mDisplayMetrics.density));
        mRightView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        mRightView.setTag(RIGHT);
        mRightView.setOnClickListener(this);


        mBottom.addView(mRightView);
        mBottomLayout.addView(mBottom);
        mRootLayout.addView(mBottomLayout);
        config();
        return this;
    }

    private void config() {
        if (Build.VERSION.SDK_INT >= 16) {
            mRootLayout.setBackground(mBackgroundShape);
        } else {
            mRootLayout.setBackgroundDrawable(mBackgroundShape);
        }

        //去除蓝色顶部横线
        int dividerID = mContext.getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = mDialog.findViewById(dividerID);
        if (divider != null) {
            divider.setBackgroundColor(Color.TRANSPARENT);
        }

        //去背景框颜色
        //mDialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //设置Dialog的窗体大小
        Window wm = mDialog.getWindow();
        WindowManager.LayoutParams lp = wm.getAttributes();
        lp.width = (int) (mDisplayMetrics.widthPixels * mWindowWidthPercentage);  //设置窗体的宽度
        wm.setAttributes(lp);

        mDialog.setContentView(mRootLayout);
        if (mContentViewCanScroll) {
            View view = (View) mRootLayout.getParent();
            if (view != null) {
                view.setPadding(0, (int) (mWindowVerticalMargin * mDisplayMetrics.density), 0, (int) (mWindowVerticalMargin * mDisplayMetrics.density));
            }
        }
    }

    /**
     * *************************************-------对外暴露的方法(大部分方法都应该在layout方法之前调用）---------**********************************************
     */


    public IOSDialog setIOSDialogLeftListener(IOSDialogLeftListener listener) {
        if (listener != null) {
            mLeftListener = listener;
        }
        return this;
    }

    public IOSDialog setIOSDialogRightListener(IOSDialogRightListener listener) {
        if (listener != null) {
            mRightListener = listener;
        }
        return this;
    }

    /**
     * Dialog 背景相关-----------------
     */
    public IOSDialog background(@ColorInt int backgroundColor) {
        mBackgroundShapeColor = backgroundColor;
        return this;
    }

    //单位 dp
    public IOSDialog radius(int radius) {
        if (radius > 0)
            mRadius = (int) (radius * mDisplayMetrics.density);
        return this;
    }

    /**
     * 标题相关--------------------
     */
    public IOSDialog titleText(String title) {
        mTitleText = title;
        if (mTitleView != null) {
            mTitleView.setText(mTitleText);
        }
        return this;
    }

    public IOSDialog titleTextColor(@ColorInt int titleTextColor) {
        mTitleTextColor = titleTextColor;
        if (mTitleView != null) {
            mTitleView.setTextColor(mTitleTextColor);
        }
        return this;
    }

    //单位 dp
    public IOSDialog titlePadding(int padding) {
        if (padding >= 0) {
            titlePadding(padding, padding, padding, padding);
        }
        return this;
    }

    //单位 dp
    public IOSDialog titlePadding(int leftPadding, int topPadding, int rightPadding, int bottomPadding) {
        if (leftPadding >= 0) {
            mTitleViewPaddingLeft = leftPadding;
        }

        if (topPadding >= 0) {
            mTitleViewPaddingTop = topPadding;
        }

        if (rightPadding >= 0) {
            mTitleViewPaddingRight = rightPadding;
        }

        if (bottomPadding >= 0) {
            mTitleViewPaddingRight = bottomPadding;
        }
        if (mTitleView != null) {
            mTitleView.setPadding((int) (mTitleViewPaddingLeft * mDisplayMetrics.density), (int) (mTitleViewPaddingTop * mDisplayMetrics.density),
                    (int) (mTitleViewPaddingRight * mDisplayMetrics.density), (int) (mTitleViewPaddingBottom * mDisplayMetrics.density));
        }
        return this;
    }

    /**
     * 中间内容相关--------------------
     */
    public IOSDialog contentText(String content) {
        if (!TextUtils.isEmpty(content)) {
            mContentText = content;
            if (mContentView != null) {
                if (mHasContentView) {
                    if (mContentViewCanScroll) {
                        if (mRootLayout != null) {
                            mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(mMeasureHeightListener);
                        }
                    }
                }
                mContentView.setText(mContentText);
            }
        }
        return this;
    }

    public IOSDialog contentTextColor(@ColorInt int textColor) {
        mContentTextColor = textColor;
        if (mContentView != null) {
            mContentView.setTextColor(mContentTextColor);
        }
        return this;
    }

    public IOSDialog contentTextSize(int textSize) {
        mContentTextSize = textSize;
        if (mContentView != null) {
            mContentView.setTextSize(mContentTextSize);
        }
        return this;
    }

    public IOSDialog contentTextBold(boolean contentTextBold) {
        mContentTextBold = contentTextBold;
        if (mContentView != null) {
            if (mContentTextBold) {
                mContentView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
        }
        return this;
    }

    public IOSDialog contentViewTextGravity(int gravity) {
        if (mContentView != null) {
            mContentView.setGravity(gravity);
        }
        return this;
    }

    public IOSDialog contentViewCanScroll(boolean canScroll) {
        mContentViewCanScroll = canScroll;
        return this;
    }

    public IOSDialog contentViewPadding(int padding) {
        if (padding >= 0) {
            contentViewPadding(padding, padding, padding, padding);
        }
        return this;
    }

    public IOSDialog contentViewPadding(int leftPadding, int topPadding, int rightPadding, int bottomPadding) {
        if (leftPadding >= 0) {
            mContentViewPaddingLeft = leftPadding;
        }

        if (topPadding >= 0) {
            mContentViewPaddingTop = topPadding;
        }

        if (rightPadding >= 0) {
            mContentViewPaddingRight = rightPadding;
        }

        if (bottomPadding >= 0) {
            mContentViewPaddingRight = bottomPadding;
        }
        if (mContentView != null) {
            mContentView.setPadding((int) (mContentViewPaddingLeft * mDisplayMetrics.density), (int) (mContentViewPaddingTop * mDisplayMetrics.density),
                    (int) (mContentViewPaddingRight * mDisplayMetrics.density), (int) (mContentViewPaddingBottom * mDisplayMetrics.density));
        }
        return this;
    }


    /**
     * 设置底部文字的颜色--------------
     */

    public IOSDialog bottomTextColor(@ColorInt int leftColor, @ColorInt int rightColor) {
        mLeftTextColor = leftColor;
        mRightTextColor = rightColor;
        if (mLeftView != null) {
            mLeftView.setTextColor(mLeftTextColor);
        }
        if (mRightView != null) {
            mRightView.setTextColor(mRightTextColor);
        }
        return this;
    }

    public IOSDialog leftText(String left) {
        if (!TextUtils.isEmpty(left)) {
            mLeftText = left;
            if (mLeftView != null) {
                mLeftView.setText(mLeftText);
            }
        }
        return this;
    }

    public IOSDialog rightText(String right) {
        if (!TextUtils.isEmpty(right)) {
            mRightText = right;
            if (mRightView != null) {
                mRightView.setText(mRightText);
            }
        }
        return this;
    }


    public IOSDialog isOnlyRight(boolean isOnlyRight) {
        mIsOnlyRight = isOnlyRight;
        return this;
    }

    /**
     * Dialog 线条相关------------------
     */
    public IOSDialog lineColor(@ColorInt int lineColor) {
        mLineColor = lineColor;
        return this;
    }

    /**
     * Dialog utils相关----------------
     */
    public IOSDialog cancelAble(boolean cancelAble) {
        if (mDialog != null) {
            mDialog.setCancelable(cancelAble);
        }
        return this;
    }

    public IOSDialog hasContentView(boolean hasContent) {
        mHasContentView = hasContent;
        return this;
    }

    public IOSDialog show() {
        if (mDialog != null) {
            mDialog.show();
        }
        return this;
    }

    public IOSDialog dismiss() {
        if (mDialog != null
                && mContext != null
                && mContext instanceof Activity
                && !((Activity) mContext).isDestroyed()
                && !((Activity) mContext).isFinishing()) {
            mDialog.dismiss();
        }
        return this;
    }

    public IOSDialog windowWidthpercentage(float percentage) {
        if (percentage >= 0 && percentage <= 1) {
            mWindowWidthPercentage = percentage;
        }
        return this;
    }

    public IOSDialog windowVertailMargin(int WindowVertailMargin) {
        mWindowVerticalMargin = WindowVertailMargin;
        return this;
    }

}
