package com.braintreepayments.api;

import android.animation.ValueAnimator;

// Ref: https://lonepine.tistory.com/entry/ViewPager2-set-scroll-Speed
abstract class IncrementalAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

    private int previousValue;

    IncrementalAnimatorUpdateListener() {
        this.previousValue = 0;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        int currentValue = (int) valueAnimator.getAnimatedValue();
        int increment = (currentValue - previousValue);
        onAnimationUpdate(valueAnimator, increment);
        previousValue = currentValue;
    }

    abstract void onAnimationUpdate(ValueAnimator valueAnimator, int increment);
}
