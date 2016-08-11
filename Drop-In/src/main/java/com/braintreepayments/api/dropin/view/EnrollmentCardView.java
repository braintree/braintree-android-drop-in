package com.braintreepayments.api.dropin.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.cardform.utils.ViewUtils;

public class EnrollmentCardView extends LinearLayout implements OnClickListener {

    private static final String EXTRA_SUPER_STATE = "com.braintreepayments.api.dropin.view.EXTRA_SUPER_STATE";
    private static final String EXTRA_VISIBLE = "com.braintreepayments.api.dropin.view.EXTRA_VISIBLE";

    private EditText mSmsCode;
    private TextView mSmsSentTextView;
    private AnimatedButtonView mAnimatedButtonView;
    private Button mSmsHelpButton;

    private AddPaymentUpdateListener mListener;

    public EnrollmentCardView(Context context) {
        super(context);
        init();
    }

    public EnrollmentCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EnrollmentCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EnrollmentCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        setOrientation(VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.bt_enrollment_card, this, true);

        mSmsCode = (EditText) findViewById(R.id.bt_sms_code);
        mSmsSentTextView = (TextView) findViewById(R.id.bt_sms_sent_text);
        mSmsHelpButton = (Button) findViewById(R.id.bt_sms_help_button);
        mAnimatedButtonView = (AnimatedButtonView) findViewById(R.id.bt_animated_button_view);

        mAnimatedButtonView.setClickListener(this);
        mSmsHelpButton.setOnClickListener(this);
    }

    public void setup(Activity activity) {
        boolean isDarkBackground = ViewUtils.isDarkBackground(activity);
        ((ImageView) findViewById(R.id.bt_sms_code_icon))
                .setImageResource(isDarkBackground ? R.drawable.bt_ic_sms_code_dark : R.drawable.bt_ic_sms_code);
    }

    public void setAddPaymentUpdatedListener(AddPaymentUpdateListener listener) {
        mListener = listener;
    }

    public void setPhoneNumber(String phoneNumber) {
        mSmsSentTextView.setText(getContext().getString(R.string.bt_sms_code_sent_to, phoneNumber));
    }

    public String getSmsCode() {
        return mSmsCode.getText().toString();
    }

    @Override
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }

        if (view == mAnimatedButtonView) {
            mListener.onPaymentUpdated(this);
        } else if (view == mSmsHelpButton) {
            mListener.onBackRequested(this);
        }
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
}
