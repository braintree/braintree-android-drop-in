package com.braintreepayments.api.dropin.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.cardform.utils.ViewUtils;
import com.braintreepayments.cardform.view.ErrorEditText;

public class EnrollmentCardView extends LinearLayout implements OnClickListener, OnEditorActionListener {

    private ErrorEditText mSmsCode;
    private TextView mSmsSentTextView;
    private AnimatedButtonView mAnimatedButtonView;
    private Button mSmsHelpButton;

    private boolean mEnrollmentFailed;
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

        mSmsCode = findViewById(R.id.bt_sms_code);
        mSmsCode.setImeOptions(EditorInfo.IME_ACTION_GO);
        mSmsCode.setImeActionLabel(getContext().getString(R.string.bt_confirm), EditorInfo.IME_ACTION_GO);
        mSmsCode.setOnEditorActionListener(this);

        mSmsSentTextView = findViewById(R.id.bt_sms_sent_text);
        mSmsHelpButton = findViewById(R.id.bt_sms_help_button);
        mAnimatedButtonView = findViewById(R.id.bt_animated_button_view);

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
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        mAnimatedButtonView.showLoading();
        onClick(mAnimatedButtonView);
        return true;
    }

    public boolean hasFailedEnrollment() {
        return mEnrollmentFailed;
    }

    public boolean isEnrollmentError(ErrorWithResponse error) {
        if (error != null) {
            BraintreeError enrollmentError = error.errorFor("unionPayEnrollment");
            if (enrollmentError != null && enrollmentError.errorFor("base") != null) {
                return true;
            }
        }

        return false;
    }

    public void setErrors(ErrorWithResponse errors) {
        if (errors.errorFor("unionPayEnrollment") != null) {
            mSmsCode.setError(getContext().getString(R.string.bt_unionpay_sms_code_invalid));
            mEnrollmentFailed = true;
        }

        mAnimatedButtonView.showButton();
    }

    @Override
    public void onClick(View view) {
        if (view == mAnimatedButtonView && TextUtils.isEmpty(mSmsCode.getText())) {
            mAnimatedButtonView.showButton();
            mSmsCode.setError(getContext().getString(R.string.bt_sms_code_required));
        } else if (mListener == null) {
            return;
        } else if (view == mAnimatedButtonView) {
            mListener.onPaymentUpdated(this);
        } else if (view == mSmsHelpButton) {
            mListener.onBackRequested(this);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mAnimatedButtonView.showButton();
        mEnrollmentFailed = false;

        if (visibility == VISIBLE) {
            mSmsCode.requestFocus();
        }
    }
}
