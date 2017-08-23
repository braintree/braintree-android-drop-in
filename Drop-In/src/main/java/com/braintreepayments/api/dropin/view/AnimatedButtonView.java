package com.braintreepayments.api.dropin.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ViewAnimator;

import com.braintreepayments.api.dropin.R;

public class AnimatedButtonView extends RelativeLayout implements OnClickListener {

    private ViewAnimator mViewAnimator;
    private Button mButton;
    private OnClickListener mOnClickListener;

    public AnimatedButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AnimatedButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public AnimatedButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        LayoutInflater.from(getContext()).inflate(R.layout.bt_animated_button_view, this);

        mViewAnimator = findViewById(R.id.bt_view_animator);
        mButton = findViewById(R.id.bt_button);
        mButton.setOnClickListener(this);

        mViewAnimator.setInAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        mViewAnimator.setOutAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.bt_AnimatedButtonAttributes);
        mButton.setText(attributes.getString(R.styleable.bt_AnimatedButtonAttributes_bt_buttonText));
        attributes.recycle();

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void onClick(View view) {
        showLoading();
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
    }

    public void showButton() {
        if (mViewAnimator.getDisplayedChild() == 1) {
            mViewAnimator.showPrevious();
        }
    }

    public void showLoading() {
        if (mViewAnimator.getDisplayedChild() == 0) {
            mViewAnimator.showNext();
        }
    }

    public void requestButtonFocus() {
        requestFocus();
    }

    public void setClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
