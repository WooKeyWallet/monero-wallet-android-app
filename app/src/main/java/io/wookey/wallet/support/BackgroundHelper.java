package io.wookey.wallet.support;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

import android.view.View;

import io.wookey.wallet.R;
import io.wookey.wallet.support.utils.DisplayHelper;
import io.wookey.wallet.support.utils.DrawableHelper;
import io.wookey.wallet.support.utils.SelectorFactory;
import io.wookey.wallet.support.utils.ShadowDrawable;


public class BackgroundHelper {

    public static Drawable getButtonBackground(Context context, @ColorRes int colorRes) {
        return DrawableHelper.getSolidShapeDrawable(ContextCompat.getColor(context, colorRes), DisplayHelper.dpToPx(25));
    }

    public static Drawable getButtonBackground(Context context) {
        return getButtonBackground(context, R.color.color_AEB6C1, R.color.color_002C6D);
    }

    public static Drawable getButtonBackground(Context context, @ColorRes int colorRes, @ColorRes int enabledColorRes) {
        Drawable drawable = DrawableHelper.getSolidShapeDrawable(ContextCompat.getColor(context, colorRes), DisplayHelper.dpToPx(25));
        Drawable drawableEnabled = DrawableHelper.getSolidShapeDrawable(ContextCompat.getColor(context, enabledColorRes), DisplayHelper.dpToPx(25));
        return SelectorFactory.newGeneralSelector()
                .setDefaultDrawable(drawableEnabled)
                .setDisabledDrawable(drawable)
                .create();
    }

    public static Drawable getCheckBoxButton(Context context) {
        return SelectorFactory
                .newGeneralSelector()
                .setDefaultDrawable(context, R.drawable.icon_unchecked)
                .setCheckedDrawable(context, R.drawable.icon_checked)
                .create();
    }

    public static Drawable getBackground(Context context, @ColorRes int colorRes, int radius) {
        return DrawableHelper.getSolidShapeDrawable(ContextCompat.getColor(context, colorRes), radius);
    }

    public static Drawable getStokeBackground(Context context, @ColorRes int colorRes, int strokeWidth, int radius) {
        return DrawableHelper.getStrokeShapeDrawable(ContextCompat.getColor(context, colorRes), strokeWidth, radius);
    }

    public static Drawable getEditBackground(Context context) {
        return getEditBackground(context, DisplayHelper.dpToPx(5));
    }

    public static Drawable getEditBackground(Context context, int radius) {
        return DrawableHelper.getSolidShapeDrawable(ContextCompat.getColor(context, R.color.color_F3F4F6), radius);
    }

    public static Drawable getSelectorDrawable(Context context, @DrawableRes int defaultDrawable, @DrawableRes int selectedDrawable) {
        return SelectorFactory
                .newGeneralSelector()
                .setDefaultDrawable(context, defaultDrawable)
                .setSelectedDrawable(context, selectedDrawable)
                .create();
    }

    public static ColorStateList getSelectorText(Context context) {
        return SelectorFactory
                .newColorSelector()
                .setDefaultColor(ContextCompat.getColor(context, R.color.color_9E9E9E))
                .setSelectedColor(ContextCompat.getColor(context, R.color.color_002C6D))
                .create();
    }

    public static Drawable getDotDrawable(Context context, @ColorRes int colorRes, int diameter) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(ContextCompat.getColor(context, colorRes));
        gradientDrawable.setSize(diameter, diameter);
        return gradientDrawable;
    }

    public static Drawable getRedDotDrawable(Context context) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(ContextCompat.getColor(context, R.color.color_FF3A5C));
        gradientDrawable.setSize(DisplayHelper.dpToPx(9), DisplayHelper.dpToPx(9));
        return gradientDrawable;
    }


    public static void setItemShadowBackground(View view) {
        ShadowDrawable.setShadowDrawable(view,
                view.getContext().getResources().getColor(R.color.color_FFFFFF),
                DisplayHelper.dpToPx(5),
                Color.parseColor("#38C8C6C6"),
                DisplayHelper.dpToPx(3), 0, 0);
    }

    public static Drawable getDashDrawable(Context context, @ColorRes int colorRes, int width, float dashWidth, float dashGap) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.LINE);
        gradientDrawable.setStroke(width, ContextCompat.getColor(context, colorRes), dashWidth, dashGap);
        return gradientDrawable;
    }

    public static Drawable getDashDrawable(Context context) {
        return getDashDrawable(context, R.color.color_CECECE, 1, 10, 10);
    }
}
