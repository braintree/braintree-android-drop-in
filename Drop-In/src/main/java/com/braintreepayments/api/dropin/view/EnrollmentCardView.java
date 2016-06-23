package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;

public class EnrollmentCardView extends RelativeLayout {

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
        mAnimatedButtonView.setNextButtonText("[TR] Confirm");

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
