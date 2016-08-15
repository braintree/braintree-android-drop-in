package com.braintreepayments.api.dropin.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
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

    private CardForm mCardForm;
    private SupportedCardTypesView mSupportedCardTypesView;
    private AnimatedButtonView mAnimatedButtonView;
    private AddPaymentUpdateListener mListener;

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
        mCardForm.setOnCardFormSubmitListener(this);
        mCardForm.setOnCardFormValidListener(this);

        mSupportedCardTypesView = (SupportedCardTypesView) findViewById(R.id.bt_supported_card_types);
        mAnimatedButtonView = (AnimatedButtonView) findViewById(R.id.bt_animated_button_view);

        mAnimatedButtonView.setClickListener(this);
    }

    public void setup(Activity activity, Configuration configuration) {
        mCardForm.cardRequired(true)
                .setup(activity);
        mCardForm.getCardEditText().setDisplayCardTypeIcon(false);
        mCardForm.setOnCardTypeChangedListener(this);

        mSupportedCardTypesView.setSupportedCardTypes(CardType.values());

        mAnimatedButtonView.setVisibility(configuration.getUnionPay().isEnabled() ? VISIBLE : GONE);
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
}
