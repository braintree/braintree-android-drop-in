package com.braintreepayments.api.dropin.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
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
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;

public class EditCardView extends LinearLayout implements OnCardFormFieldFocusedListener, OnClickListener,
        OnCardFormSubmitListener {

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
        mAnimatedButtonView = (AnimatedButtonView) findViewById(R.id.bt_animated_button_view);
    }

    public void setup(Activity activity, Configuration configuration) {
        mConfiguration = configuration;

        mCardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(configuration.isCvvChallengePresent())
                .postalCodeRequired(configuration.isPostalCodeChallengePresent())
                .setup(activity);
        mCardForm.setOnCardFormSubmitListener(this);

        mAnimatedButtonView.setClickListener(this);
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
        BraintreeError formErrors = errors.errorFor("unionPayEnrollment");
        if (formErrors == null) {
            formErrors = errors.errorFor("creditCard");
        }

        if (formErrors != null) {
            if (formErrors.errorFor("number") != null) {
                mCardForm.setCardNumberError(getContext().getString(R.string.bt_card_number_invalid));
            }

            if (formErrors.errorFor("expirationYear") != null ||
                    formErrors.errorFor("expirationMonth") != null ||
                    formErrors.errorFor("expirationDate") != null) {
                mCardForm.setExpirationError(getContext().getString(R.string.bt_expiration_invalid));
            }

            if (formErrors.errorFor("cvv") != null) {

                mCardForm.setCvvError(getContext().getString(R.string.bt_cvv_invalid,
                        getContext().getString(
                                mCardForm.getCardEditText().getCardType().getSecurityCodeName())));
            }

            if (formErrors.errorFor("billingAddress") != null) {
                mCardForm.setPostalCodeError(getContext().getString(R.string.bt_postal_code_invalid));
            }

            if (formErrors.errorFor("mobileCountryCode") != null) {
                mCardForm.setCountryCodeError(getContext().getString(R.string.bt_country_code_invalid));
            }

            if (formErrors.errorFor("mobileNumber") != null) {
                mCardForm.setMobileNumberError(getContext().getString(R.string.bt_mobile_number_invalid));
            }
        }

        mAnimatedButtonView.showButton();
    }

    public void useUnionPay(Activity activity, boolean useUnionPay, boolean debitCard) {
        mCardForm.getExpirationDateEditText().setOptional(false);
        mCardForm.getCvvEditText().setOptional(false);

        if (useUnionPay) {
            if (debitCard) {
                mCardForm.getExpirationDateEditText().setOptional(true);
                mCardForm.getCvvEditText().setOptional(true);
            }

            mCardForm.cardRequired(true)
                    .expirationRequired(true)
                    .cvvRequired(true)
                    .postalCodeRequired(mConfiguration.isPostalCodeChallengePresent())
                    .mobileNumberRequired(true)
                    .mobileNumberExplanation(getContext().getString(R.string.bt_unionpay_mobile_number_explanation))
                    .setup(activity);
        }
    }

    @Override
    public void onCardFormSubmit() {
        if (mCardForm.isValid()) {
            mAnimatedButtonView.showLoading();

            if (mListener != null) {
                mListener.onPaymentUpdated(this);
            }
        } else {
            mAnimatedButtonView.showButton();
            mCardForm.validate();
        }
    }

    @Override
    public void onClick(View view) {
        onCardFormSubmit();
    }

    @Override
    public void onCardFormFieldFocused(View field) {
        if (field instanceof CardEditText && mListener != null) {
            mListener.onBackRequested(this);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mAnimatedButtonView.showButton();

        if (visibility == VISIBLE) {
            if (!mCardForm.getExpirationDateEditText().isValid() ||
                    TextUtils.isEmpty(mCardForm.getExpirationDateEditText().getText())) {
                mCardForm.getExpirationDateEditText().requestFocus();
            } else if (mCardForm.getCvvEditText().getVisibility() == VISIBLE &&
                    (!mCardForm.getCvvEditText().isValid() ||
                    TextUtils.isEmpty(mCardForm.getCvvEditText().getText()))) {
                mCardForm.getCvvEditText().requestFocus();
            } else if (mCardForm.getPostalCodeEditText().getVisibility() == VISIBLE &&
                    !mCardForm.getPostalCodeEditText().isValid()) {
                mCardForm.getPostalCodeEditText().requestFocus();
            } else if (mCardForm.getCountryCodeEditText().getVisibility() == VISIBLE &&
                    !mCardForm.getCountryCodeEditText().isValid()) {
                mCardForm.getCountryCodeEditText().requestFocus();
            } else if (mCardForm.getMobileNumberEditText().getVisibility() == VISIBLE &&
                    !mCardForm.getMobileNumberEditText().isValid()) {
                mCardForm.getMobileNumberEditText().requestFocus();
            } else {
                mAnimatedButtonView.requestButtonFocus();
                mCardForm.closeSoftKeyboard();
            }

            mCardForm.setOnFormFieldFocusedListener(this);
        } else {
            mCardForm.setOnFormFieldFocusedListener(null);
        }
    }
}
