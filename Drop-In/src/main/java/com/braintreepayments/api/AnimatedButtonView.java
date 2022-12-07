package com.braintreepayments.api;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ViewAnimator;

import com.braintreepayments.api.dropin.R;

class AnimatedButtonView extends RelativeLayout implements OnClickListener {

    private ViewAnimator viewAnimator;
    private OnClickListener onClickListener;

    public AnimatedButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AnimatedButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        LayoutInflater.from(getContext()).inflate(R.layout.bt_animated_button_view, this);

        viewAnimator = findViewById(R.id.bt_view_animator);
        Button button = findViewById(R.id.bt_button);
        button.setOnClickListener(this);

        viewAnimator.setInAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        viewAnimator.setOutAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.bt_AnimatedButtonAttributes);
        button.setText(attributes.getString(R.styleable.bt_AnimatedButtonAttributes_bt_buttonText));
        attributes.recycle();
    }

    @Override
    public void onClick(View view) {
        if (!isLoading()) {
            showLoading();
            if (onClickListener != null) {
                onClickListener.onClick(this);
            }
        }
    }

    private boolean isLoading() {
        return (viewAnimator.getDisplayedChild() == 1);
    }

    public void showButton() {
        if (viewAnimator.getDisplayedChild() == 1) {
            viewAnimator.showPrevious();
        }
    }

    public void showLoading() {
        if (viewAnimator.getDisplayedChild() == 0) {
            viewAnimator.showNext();
        }
    }

    public void setClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
