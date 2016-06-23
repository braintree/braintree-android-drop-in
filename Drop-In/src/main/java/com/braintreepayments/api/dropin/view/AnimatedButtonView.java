package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ViewAnimator;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;

public class AnimatedButtonView extends RelativeLayout {

    private ViewAnimator mViewAnimator;
    private Button mNext;

    private OnClickListener mNextOnClickListener;

    public AnimatedButtonView(Context context) {
        super(context);
        init(context);
    }

    public AnimatedButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnimatedButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }
        LayoutInflater.from(context).inflate(R.layout.bt_animated_button_view, this);

        mViewAnimator = (ViewAnimator)findViewById(R.id.view_animator);
        mNext = (Button)findViewById(R.id.next_button);
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        mViewAnimator.setInAnimation(fadeIn);
        mViewAnimator.setOutAnimation(fadeOut);
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewAnimator.showNext();
                if (mNextOnClickListener != null) {
                    mNextOnClickListener.onClick(v);
                }
            }
        });
    }

    public void setNextButtonOnClickListener(OnClickListener onClickListener) {
        mNextOnClickListener = onClickListener;
    }

    public void setNextButtonText(String text) {
        mNext.setText(text);
    }
}
