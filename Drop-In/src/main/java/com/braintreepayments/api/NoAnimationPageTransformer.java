package com.braintreepayments.api;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

class NoAnimationPageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position < -1 || position > 1) {
            // page is either offscreen to the left or offscreen to the right
            page.setAlpha(0.0f);
        } else {
            // page is visible
            page.setAlpha(1.0f);
        }
    }
}
