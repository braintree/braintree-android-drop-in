package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;

public class EnrollmentCardView extends RelativeLayout {

    private static final String EXTRA_SUPER_STATE = "com.braintreepayments.api.dropin.view.EXTRA_SUPER_STATE";
    private static final String EXTRA_VISIBLE = "com.braintreepayments.api.dropin.view.EXTRA_VISIBLE";

    private EditText mSmsCode;
    private AnimatedButtonView mAnimatedButtonView;
    private Button mSmsHelpButton;

    private AddPaymentUpdateListener mListener;

    public EnrollmentCardView(Context context) {
        super(context);
        init(context);
    }

    public EnrollmentCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EnrollmentCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if(isInEditMode()) {
            return;
        }
        LayoutInflater.from(context).inflate(R.layout.bt_enrollment_card, this, true);
        mSmsCode = (EditText)findViewById(R.id.sms_code);
        mSmsHelpButton = (Button)findViewById(R.id.sms_help_button);
        mAnimatedButtonView = (AnimatedButtonView)findViewById(R.id.animated_button_view);

        mAnimatedButtonView.setNextButtonOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPaymentUpdated(EnrollmentCardView.this);
                }
            }
        });
        mAnimatedButtonView.setNextButtonText(getContext().getString(R.string.bt_confirm));

        mSmsHelpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackRequested(EnrollmentCardView.this);
                }
            }
        });
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle state = new Bundle();
        state.putParcelable(EXTRA_SUPER_STATE, superState);
        state.putBoolean(EXTRA_VISIBLE, getVisibility() == VISIBLE);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle)state;
            setVisibility(bundle.getBoolean(EXTRA_VISIBLE)? VISIBLE : GONE);
            super.onRestoreInstanceState(bundle.getParcelable(EXTRA_SUPER_STATE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mAnimatedButtonView.setVisibility(visibility);
    }

    public void setAddPaymentUpdatedListener(AddPaymentUpdateListener listener) {
        mListener = listener;
    }

    public String getSmsCode() {
        return mSmsCode.getText().toString();
    }
}
