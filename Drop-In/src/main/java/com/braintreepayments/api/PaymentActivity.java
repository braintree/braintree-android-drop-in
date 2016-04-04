package com.braintreepayments.api;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.AnimationUtils;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.view.PaymentMethodHorizontalScrollView;

public class PaymentActivity extends AppCompatActivity implements OnGlobalLayoutListener, AnimatorListener {

    private View mContainer;
    private Toolbar mToolbar;
    private PaymentMethodHorizontalScrollView mHorizontalScrollView;
    private boolean mExpanded = false;
    private int mOriginalHalfSheetHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bt_payment_activity);

        mContainer = findViewById(R.id.container_view);
        mToolbar = (Toolbar) findViewById(R.id.coordinator_toolbar);
        mHorizontalScrollView = (PaymentMethodHorizontalScrollView) findViewById(R.id.bt_available_payment_methods);
        mHorizontalScrollView.setAndroidPayEnabled(true);
        mHorizontalScrollView.setPayPalEnabled(true);

        getWindow().findViewById(R.id.bt_payment_method_activity_root)
                .getViewTreeObserver().addOnGlobalLayoutListener(this);
        slideUp();
    }

    @Override
    public void onGlobalLayout() {
        if (mOriginalHalfSheetHeight == 0) {
            mOriginalHalfSheetHeight = mContainer.getHeight();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.bt_slide_in_up, R.anim.bt_slide_out_down);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mExpanded = !mExpanded;
    }

    public void onClick(View v) {
       performExpandContractAnimation();
    }

    private void performExpandContractAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();

        float toolBarStartY, toolBarEndY, containerStartHeight, containerEndHeight;
        if (!mExpanded) {
            toolBarStartY = 0;
            toolBarEndY = -mToolbar.getHeight();
            containerStartHeight = mOriginalHalfSheetHeight;
            containerEndHeight = windowHeight() - mToolbar.getHeight();
        } else {
            toolBarStartY = -mToolbar.getHeight();
            toolBarEndY = 0;
            containerStartHeight = mContainer.getHeight();
            containerEndHeight = mOriginalHalfSheetHeight;
        }

        ObjectAnimator toolbarAnimation = ObjectAnimator.ofFloat(mToolbar, "translationY", toolBarStartY, toolBarEndY);

        ValueAnimator containerAnimation = ValueAnimator.ofFloat(containerStartHeight, containerEndHeight);
        containerAnimation.setTarget(mContainer);
        containerAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mContainer.getLayoutParams().height = ((Float) animation.getAnimatedValue()).intValue();
                mContainer.requestLayout();
            }
        });

        animatorSet.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        animatorSet.playTogether(containerAnimation, toolbarAnimation);
        animatorSet.addListener(this);
        animatorSet.start();
    }

    private float windowHeight() {
        return getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
    }

    private void slideUp() {
        findViewById(android.R.id.content).startAnimation(AnimationUtils.loadAnimation(this, R.anim.bt_slide_in_up));
    }

    @Override
    public void onAnimationStart(Animator animation) {}

    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationRepeat(Animator animation) {}
}
