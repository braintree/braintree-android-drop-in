package com.braintreepayments.api;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

import androidx.annotation.DrawableRes;

public enum DropInPaymentMethodType {
    // `getFrontResource` is pulling icons from android-card-form, `R.drawable` icons are drop-in internal

    AMEX(CardType.AMEX.getFrontResource()),
    GOOGLE_PAY(R.drawable.bt_ic_google_pay),
    DINERS(CardType.DINERS_CLUB.getFrontResource()),
    DISCOVER(CardType.DISCOVER.getFrontResource()),
    JCB(CardType.JCB.getFrontResource()),
    MAESTRO(CardType.MAESTRO.getFrontResource()),
    MASTERCARD(CardType.MASTERCARD.getFrontResource()),
    PAYPAL(R.drawable.bt_ic_paypal),
    VISA(CardType.VISA.getFrontResource()),
    PAY_WITH_VENMO(R.drawable.bt_ic_venmo),
    UNIONPAY(CardType.UNIONPAY.getFrontResource()),
    HIPER(CardType.HIPER.getFrontResource()),
    HIPERCARD(CardType.HIPERCARD.getFrontResource()),
    UNKNOWN(CardType.UNKNOWN.getFrontResource()),
    ;

    @DrawableRes
    private final int drawable;

    DropInPaymentMethodType(@DrawableRes int drawable) {
        this.drawable = drawable;
    }

    /**
     * @return An id representing a {@link android.graphics.drawable.Drawable} icon for the current
     * {@link DropInPaymentMethodType}.
     */
    @DrawableRes
    public int getDrawable() {
        return drawable;
    }
}
