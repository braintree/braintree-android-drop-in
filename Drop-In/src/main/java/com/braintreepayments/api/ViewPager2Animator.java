package com.braintreepayments.api;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

class ViewPager2Animator {

    private final int animationDuration;

    ViewPager2Animator(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    void animateToPosition(ViewPager2 viewPager, int position) {
        animateToPosition(viewPager, position, null);
    }

    void animateToPosition(final ViewPager2 viewPager, int position, @Nullable final AnimationCompleteCallback callback) {
        int itemWidth = viewPager.getWidth();
        int currentPosition = viewPager.getCurrentItem();
        int dx = itemWidth * (position - currentPosition);

        ValueAnimator dragAnimator = ValueAnimator.ofInt(0, dx);
        dragAnimator.addUpdateListener(new IncrementalAnimatorUpdateListener() {
            @Override
            void onAnimationUpdate(ValueAnimator valueAnimator, int increment) {
                float value = (float) (-1 * increment);
                viewPager.fakeDragBy(value);
            }
        });

        dragAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                viewPager.beginFakeDrag();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewPager.endFakeDrag();
                viewPager.post(() -> {
                    if (callback != null) {
                        callback.onAnimationComplete();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        dragAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        dragAnimator.setDuration(animationDuration);
        dragAnimator.start();
    }
}
