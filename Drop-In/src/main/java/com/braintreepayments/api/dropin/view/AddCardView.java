package com.braintreepayments.api.dropin.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.AddPaymentUpdateListener;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.OnCardFormValidListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;

public class AddCardView extends LinearLayout implements OnCardFormSubmitListener, OnCardFormValidListener,
        OnClickListener {

    private static final String EXTRA_SUPER_STATE = "com.braintreepayments.api.dropin.view.EXTRA_SUPER_STATE";
    private static final String EXTRA_VISIBLE = "com.braintreepayments.api.dropin.view.EXTRA_VISIBLE";

    private CardForm mCardForm;
    private SupportedCardTypesView mSupportedCardTypesView;
    private AnimatedButtonView mAnimatedButtonView;
    private AddPaymentUpdateListener mListener;

    public AddCardView(Context context) {
        super(context);
        init(context);
    }

    public AddCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }

        setOrientation(VERTICAL);

        LayoutInflater.from(context).inflate(R.layout.bt_add_card, this, true);
        mCardForm = (CardForm) findViewById(R.id.bt_card_form);
        mCardForm.setOnCardFormSubmitListener(this);
        mCardForm.setOnCardFormValidListener(this);

        mSupportedCardTypesView = (SupportedCardTypesView) findViewById(R.id.bt_supported_card_types);
        mAnimatedButtonView = (AnimatedButtonView) findViewById(R.id.animated_button_view);

        updateSupportedCardTypes();

        mAnimatedButtonView.setNextButtonOnClickListener(this);
    }

    public void setup(Activity activity) {
        mCardForm.cardRequired(true)
                .setup(activity);

        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        ((CardEditText) findViewById(R.id.bt_card_form_card_number)).setDisplayCardTypeIcon(false);
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            mListener.onPaymentUpdated(this);
        }
    }

    public CardForm getCardForm() {
        return mCardForm;
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

    private void updateSupportedCardTypes() {
        mSupportedCardTypesView.setSupportedCardTypes(CardType.values());
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mAnimatedButtonView.setVisibility(visibility);
    }

    public String getNumber() {
        return mCardForm.getCardNumber();
    }

    public void setAddPaymentUpdatedListener(AddPaymentUpdateListener listener) {
        mListener = listener;
    }

    @Override
    public void onCardFormSubmit() {
        mAnimatedButtonView.showLoadingAnimation();
        if (mListener != null) {
            mListener.onPaymentUpdated(this);
        }
    }

    @Override
    public void onCardFormValid(boolean valid) {
        mAnimatedButtonView.showLoadingAnimation();
        if (mListener != null) {
            mListener.onPaymentUpdated(this);
        }
    }
}
