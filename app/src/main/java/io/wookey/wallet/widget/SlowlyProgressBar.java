package io.wookey.wallet.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

public class SlowlyProgressBar {

    private ProgressBar progressBar;
    private boolean isStart = false;

    public SlowlyProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void onProgressStart() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setAlpha(1.0f);
    }

    public void onProgressChange(int newProgress) {
        int currentProgress = progressBar.getProgress();
        if (newProgress >= 100 && !isStart) {
            isStart = true;
            progressBar.setProgress(newProgress);
            startDismissAnimation(progressBar.getProgress());
        } else {
            startProgressAnimation(newProgress, currentProgress);
        }
    }

    /**
     * 进度条平滑递增
     *
     * @param newProgress
     * @param currentProgress
     */
    private void startProgressAnimation(int newProgress, int currentProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", currentProgress, newProgress);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    /**
     * 进度条平滑消失动画
     *
     * @param progress
     */
    private void startDismissAnimation(final int progress) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(progressBar, "alpha", 1.0f, 0.0f);
        animator.setDuration(1500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                int offset = 100 - progress;
                progressBar.setProgress((int) (progress + offset * fraction));
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                progressBar.setProgress(0);
                progressBar.setVisibility(View.GONE);
                isStart = false;
            }
        });

        animator.start();
    }
}
