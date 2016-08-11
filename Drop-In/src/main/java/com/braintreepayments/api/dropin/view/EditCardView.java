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
import android.widget.LinearLayout;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.cardform.OnCardFormFieldFocusedListener;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;

public class EditCardView extends LinearLayout implements OnCardFormFieldFocusedListener, OnClickListener {

    private static final String EXTRA_SUPER_STATE = "com.braintreepayments.api.dropin.view.EXTRA_SUPER_STATE";
    private static final String EXTRA_VISIBLE = "com.braintreepayments.api.dropin.view.EXTRA_VISIBLE";

    private CardForm mCardForm;
    private AnimatedButtonView mAnimatedButtonView;

    private Configuration mConfiguration;
    private AddPaymentUpdateListener mListener;

    public EditCardView(Context context) {
        super(context);
        init();
    }

    public EditCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        setOrientation(VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.bt_edit_card, this);

        mCardForm = (CardForm) findViewById(R.id.bt_card_form);
        mCardForm.setOnFormFieldFocusedListener(this);

        mAnimatedButtonView = (AnimatedButtonView) findViewById(R.id.bt_animated_button_view);
        mAnimatedButtonView.setClickListener(this);
    }

    public void setup(Activity activity, Configuration configuration) {
        mConfiguration = configuration;

        mCardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(configuration.isCvvChallengePresent())
                .postalCodeRequired(configuration.isPostalCodeChallengePresent())
                .setup(activity);
    }

    public void setAddPaymentUpdatedListener(AddPaymentUpdateListener listener) {
        mListener = listener;
    }

    public CardForm getCardForm() {
        return mCardForm;
    }

    public void setCardNumber(String cardNumber) {
        mCardForm.getCardEditText().setText(cardNumber);
    }

    public void setErrors(ErrorWithResponse errors) {
        BraintreeError cardErrors = errors.errorFor("creditCard");
        if (cardErrors != null) {
            if (cardErrors.errorFor("number") != null) {
                mCardForm.setCardNumberError(getContext().getString(R.string.bt_card_number_invalid));
            }

            if (cardErrors.errorFor("expirationYear") != null ||
                    cardErrors.errorFor("expirationMonth") != null ||
                    cardErrors.errorFor("expirationDate") != null) {
                mCardForm.setExpirationError(getContext().getString(R.string.bt_expiration_invalid));
            }

            if (cardErrors.errorFor("cvv") != null) {
                mCardForm.setCvvError(getContext().getString(R.string.bt_cvv_invalid));
            }

            if (cardErrors.errorFor("billingAddress") != null) {
                mCardForm.setPostalCodeError(getContext().getString(R.string.bt_postal_code_invalid));
            }
        }
    }

    public void useUnionPay(Activity activity, boolean useUnionPay) {
        if (useUnionPay) {
            mCardForm.cardRequired(true)
                    .expirationRequired(true)
                    .cvvRequired(true)
                    .postalCodeRequired(mConfiguration.isPostalCodeChallengePresent())
                    .mobileNumberRequired(true)
                    .setup(activity);
        }
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            mListener.onPaymentUpdated(this);
        }
    }

    @Override
    public void onCardFormFieldFocused(View field) {
        if (field instanceof CardEditText && mListener != null) {
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
