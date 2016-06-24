package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;

public class EditCardView extends RelativeLayout {

    private static final String EXTRA_SUPER_STATE = "com.braintreepayments.api.dropin.view.EXTRA_SUPER_STATE";
    private static final String EXTRA_VISIBLE = "com.braintreepayments.api.dropin.view.EXTRA_VISIBLE";

    private EditText mCardNumber;
    private EditText mExpirationDate;
    private EditText mCvv;
    private View mUnionPayGroup;
    private EditText mMobileCountryCode;
    private EditText mPhoneNumber;
    private AnimatedButtonView mAnimatedButtonView;

    private AddPaymentUpdateListener mListener;

    public EditCardView(Context context) {
        super(context);
        init(context);
    }

    public EditCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }
        LayoutInflater.from(context).inflate(R.layout.bt_edit_card, this);

        mCardNumber = (EditText)findViewById(R.id.edit_card_number);
        mExpirationDate = (EditText)findViewById(R.id.expiration_date);
        mCvv = (EditText)findViewById(R.id.cvv);
        mUnionPayGroup = findViewById(R.id.union_pay_group);
        mMobileCountryCode = (EditText)findViewById(R.id.country_code_number);
        mPhoneNumber = (EditText)findViewById(R.id.phone_number);
        mAnimatedButtonView = (AnimatedButtonView) findViewById(R.id.animated_button_view);

        mCardNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackRequested(EditCardView.this);
                }
            }
        });
        mAnimatedButtonView.setNextButtonOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPaymentUpdated(EditCardView.this);
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

    public String getCvv() {
        return mCvv.getText().toString();
    }

    public String getExpirationDate() {
        return mExpirationDate.getText().toString();
    }

    public String getMobileCountryCode() {
        return mMobileCountryCode.getText().toString();
    }

    public String getPhoneNumber() {
        return mPhoneNumber.getText().toString();
    }

    public void setCardNumber(String cardNumber) {
        mCardNumber.setText(cardNumber);
    }

    public void useUnionPay(boolean useUnionPay) {
        mUnionPayGroup.setVisibility(useUnionPay? VISIBLE : GONE);
    }
}
