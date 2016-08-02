package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ViewAnimator;

import com.braintreepayments.api.dropin.R;

public class AnimatedButtonView extends RelativeLayout implements OnClickListener {

    private ViewAnimator mViewAnimator;
    private Button mNext;

    private OnClickListener mNextOnClickListener;

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

        mViewAnimator = (ViewAnimator)findViewById(R.id.view_animator);
        mNext = (Button) findViewById(R.id.next_button);
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        mViewAnimator.setInAnimation(fadeIn);
        mViewAnimator.setOutAnimation(fadeOut);
        mNext.setOnClickListener(this);

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.bt_AnimatedButtonAttributes);
        mNext.setText(attributes.getString(R.styleable.bt_AnimatedButtonAttributes_bt_buttonText));
        attributes.recycle();
    }

    @Override
    public void onClick(View view) {
        showLoadingAnimation();
        if (mNextOnClickListener != null) {
            mNextOnClickListener.onClick(view);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(VISIBLE == visibility) {
            mViewAnimator.setDisplayedChild(0);
        }
    }

    public void showLoadingAnimation() {
        mViewAnimator.showNext();
    }

    public void setNextButtonOnClickListener(OnClickListener onClickListener) {
        mNextOnClickListener = onClickListener;
    }

    public void setNextButtonText(String text) {
        mNext.setText(text);
    }
}
