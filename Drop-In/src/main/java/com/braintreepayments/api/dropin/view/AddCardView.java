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
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.OnCardFormValidListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardEditText.OnCardTypeChangedListener;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddCardView extends LinearLayout implements OnCardFormSubmitListener, OnCardFormValidListener,
        OnClickListener, OnCardTypeChangedListener {

    private static final String PARENT_STATE = "com.braintreepayments.api.dropin.view.PARENT_STATE";
    private static final String CARD_NUMBER = "com.braintreepayments.api.dropin.view.CARD_NUMBER";

    private CardType[] mSupportedCardTypes;
    private CardForm mCardForm;
    private SupportedCardTypesView mSupportedCardTypesView;
    private AnimatedButtonView mAnimatedButtonView;
    private AddPaymentUpdateListener mListener;
    private String mCardNumber;

    public AddCardView(Context context) {
        super(context);
        init();
    }

    public AddCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AddCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AddCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        setOrientation(VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.bt_add_card, this, true);

        mCardForm = findViewById(R.id.bt_card_form);
        mSupportedCardTypesView = findViewById(R.id.bt_supported_card_types);
        mAnimatedButtonView = findViewById(R.id.bt_animated_button_view);
    }

    public void setup(Activity activity, Configuration configuration, boolean unionpaySupported) {
        mCardForm.getCardEditText().displayCardTypeIcon(false);

        mCardForm.cardRequired(true).setup(activity);
        mCardForm.setOnCardTypeChangedListener(this);
        mCardForm.setOnCardFormValidListener(this);
        mCardForm.setOnCardFormSubmitListener(this);

        Set<String> cardTypes = new HashSet<>(configuration.getCardConfiguration().getSupportedCardTypes());
        if (!unionpaySupported) {
            cardTypes.remove(PaymentMethodType.UNIONPAY.getCanonicalName());
        }
        mSupportedCardTypes = PaymentMethodType.getCardsTypes(cardTypes);
        mSupportedCardTypesView.setSupportedCardTypes(mSupportedCardTypes);

        mAnimatedButtonView.setVisibility(configuration.getUnionPay().isEnabled() ? VISIBLE : GONE);
        mAnimatedButtonView.setClickListener(this);

        if (mCardNumber != null) {
            mCardForm.getCardEditText().setText(mCardNumber);
            mCardNumber = null;
        }
    }

    public void setAddPaymentUpdatedListener(AddPaymentUpdateListener listener) {
        mListener = listener;
    }

    public CardForm getCardForm() {
        return mCardForm;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mAnimatedButtonView.showButton();

        if (visibility == VISIBLE) {
            mCardForm.getCardEditText().requestFocus();
        }
    }

    public void showCardNotSupportedError() {
        mCardForm.getCardEditText().setError(getContext().getString(R.string.bt_card_not_accepted));
        mAnimatedButtonView.showButton();
    }

    public boolean isCardNumberError(ErrorWithResponse errors) {
        BraintreeError formErrors = errors.errorFor("creditCard");
        return formErrors != null && formErrors.errorFor("number") != null;
    }

    public void setErrors(ErrorWithResponse errors) {
        BraintreeError formErrors = errors.errorFor("creditCard");

        if (formErrors != null) {
            if (formErrors.errorFor("number") != null) {
                mCardForm.setCardNumberError(getContext().getString(R.string.bt_card_number_invalid));
            }
        }

        mAnimatedButtonView.showButton();
    }

    @Override
    public void onCardTypeChanged(CardType cardType) {
        if (cardType == CardType.EMPTY) {
            mSupportedCardTypesView.setSupportedCardTypes(mSupportedCardTypes);
        } else {
            mSupportedCardTypesView.setSelected(cardType);
        }
    }

    @Override
    public void onClick(View view) {
        if (isValid()) {
            callAddPaymentUpdateListener();
        } else {
            mAnimatedButtonView.showButton();

            if (!mCardForm.isValid()) {
                mCardForm.validate();
            } else if (!isCardTypeValid()) {
                showCardNotSupportedError();
            }
        }
    }

    @Override
    public void onCardFormSubmit() {
        if (isValid()) {
            mAnimatedButtonView.showLoading();
            callAddPaymentUpdateListener();
        } else {
            if (!mCardForm.isValid()) {
                mCardForm.validate();
            } else if (!isCardTypeValid()) {
                showCardNotSupportedError();
            }
        }
    }

    @Override
    public void onCardFormValid(boolean valid) {
        if (isValid()) {
            mAnimatedButtonView.showLoading();
            callAddPaymentUpdateListener();
        }
    }

    private boolean isValid() {
        return mCardForm.isValid() && isCardTypeValid();
    }

    private boolean isCardTypeValid() {
        return Arrays.asList(mSupportedCardTypes).contains(mCardForm.getCardEditText()
                .getCardType());
    }

    private void callAddPaymentUpdateListener() {
        if (mListener != null) {
            mListener.onPaymentUpdated(this);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(PARENT_STATE, super.onSaveInstanceState());
        bundle.putString(CARD_NUMBER, mCardForm.getCardNumber());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            mCardNumber = ((Bundle) state).getString(CARD_NUMBER);
            state = ((Bundle) state).getParcelable(PARENT_STATE);
        }

        super.onRestoreInstanceState(state);
    }
}
