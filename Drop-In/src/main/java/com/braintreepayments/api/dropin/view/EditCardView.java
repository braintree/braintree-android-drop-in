package com.braintreepayments.api.dropin.view;

import android.app.Activity;
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
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.cardform.OnCardFormFieldFocusedListener;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;

public class EditCardView extends RelativeLayout implements OnCardFormFieldFocusedListener {

    private static final String EXTRA_SUPER_STATE = "com.braintreepayments.api.dropin.view.EXTRA_SUPER_STATE";
    private static final String EXTRA_VISIBLE = "com.braintreepayments.api.dropin.view.EXTRA_VISIBLE";

    private CardForm mCardForm;
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

        mCardForm = (CardForm) findViewById(R.id.bt_card_form);
        mCardForm.setOnFormFieldFocusedListener(this);

        mAnimatedButtonView = (AnimatedButtonView) findViewById(R.id.animated_button_view);
        mAnimatedButtonView.setNextButtonOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPaymentUpdated(EditCardView.this);
                }
            }
        });
    }

    public void setup(Activity activity, Configuration configuration) {
        mCardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(true)
                .setup(activity);
    }

    @Override
    public void onCardFormFieldFocused(View field) {
        if (field instanceof CardEditText && mListener != null) {
            mListener.onBackRequested(EditCardView.this);
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

    public void setAddPaymentUpdatedListener(AddPaymentUpdateListener listener) {
        mListener = listener;
    }

    public String getCvv() {
        return mCardForm.getCvv();
    }

    public String getExpirationDate() {
        return mCardForm.getExpirationMonth() + "/" + mCardForm.getExpirationYear();
    }

    public String getMobileCountryCode() {
        return mCardForm.getCountryCode();
    }

    public String getPhoneNumber() {
        return mCardForm.getMobileNumber();
    }

    public void setCardNumber(String cardNumber) {
        ((EditText) findViewById(R.id.bt_card_form_card_number)).setText(cardNumber);
    }

    public void useUnionPay(Activity activity, boolean useUnionPay) {
        if (useUnionPay) {
            mCardForm.cardRequired(true)
                    .expirationRequired(true)
                    .cvvRequired(true)
                    .postalCodeRequired(false)
                    .mobileNumberRequired(useUnionPay)
                    .setup(activity);
        }
    }
}
