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
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.OnCardFormValidListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardEditText.OnCardTypeChangedListener;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;

public class AddCardView extends LinearLayout implements OnCardFormSubmitListener, OnCardFormValidListener,
        OnClickListener, OnCardTypeChangedListener {

    private static final String PARENT_STATE = "com.braintreepayments.api.dropin.view.PARENT_STATE";
    private static final String CARD_NUMBER = "com.braintreepayments.api.dropin.view.CARD_NUMBER";

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

        mCardForm = (CardForm) findViewById(R.id.bt_card_form);
        mSupportedCardTypesView = (SupportedCardTypesView) findViewById(R.id.bt_supported_card_types);
        mAnimatedButtonView = (AnimatedButtonView) findViewById(R.id.bt_animated_button_view);

        mAnimatedButtonView.setClickListener(this);
    }

    public void setup(Activity activity, Configuration configuration) {
        mCardForm.cardRequired(true)
                .setup(activity);
        mCardForm.getCardEditText().setDisplayCardTypeIcon(false);
        mCardForm.setOnCardTypeChangedListener(this);
        mCardForm.setOnCardFormValidListener(this);
        mCardForm.setOnCardFormSubmitListener(this);

        mSupportedCardTypesView.setSupportedCardTypes(CardType.values());

        mAnimatedButtonView.setVisibility(configuration.getUnionPay().isEnabled() ? VISIBLE : GONE);

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

    @Override
    public void onCardTypeChanged(CardType cardType) {
        if (cardType == CardType.EMPTY) {
            mSupportedCardTypesView.setSupportedCardTypes(CardType.values());
        } else {
            mSupportedCardTypesView.setSelected(cardType);
        }
    }

    @Override
    public void onClick(View view) {
        if (mCardForm.isValid()) {
            callAddPaymentUpdateListener();
        } else {
            mAnimatedButtonView.showButton();
            mCardForm.validate();
        }
    }

    @Override
    public void onCardFormSubmit() {
        if (mCardForm.isValid()) {
            mAnimatedButtonView.showLoading();
            callAddPaymentUpdateListener();
        } else {
            mCardForm.validate();
        }
    }

    @Override
    public void onCardFormValid(boolean valid) {
        if (valid) {
            mAnimatedButtonView.showLoading();
            callAddPaymentUpdateListener();
        }
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
