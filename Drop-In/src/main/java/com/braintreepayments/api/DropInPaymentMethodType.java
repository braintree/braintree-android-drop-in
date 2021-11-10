package com.braintreepayments.api;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

import androidx.annotation.DrawableRes;

public enum DropInPaymentMethodType {

    // `getFrontResource` is pulling icons from android-card-form, `R.drawable` icons are drop-in internal
    AMEX(CardType.AMEX.getFrontResource(), R.drawable.bt_ic_vaulted_amex),
    GOOGLE_PAY(R.drawable.bt_ic_google_pay, 0),
    DINERS(CardType.DINERS_CLUB.getFrontResource(), R.drawable.bt_ic_vaulted_diners_club),
    DISCOVER(CardType.DISCOVER.getFrontResource(), R.drawable.bt_ic_vaulted_discover),
    JCB(CardType.JCB.getFrontResource(), R.drawable.bt_ic_vaulted_jcb),
    MAESTRO(CardType.MAESTRO.getFrontResource(), R.drawable.bt_ic_vaulted_maestro),
    MASTERCARD(CardType.MASTERCARD.getFrontResource(), R.drawable.bt_ic_vaulted_mastercard),
    PAYPAL(R.drawable.bt_ic_paypal, R.drawable.bt_ic_vaulted_paypal),
    VISA(CardType.VISA.getFrontResource(), R.drawable.bt_ic_vaulted_visa),
    PAY_WITH_VENMO(R.drawable.bt_ic_venmo, R.drawable.bt_ic_vaulted_venmo),
    UNIONPAY(CardType.UNIONPAY.getFrontResource(), R.drawable.bt_ic_vaulted_unionpay),
    HIPER(CardType.HIPER.getFrontResource(), R.drawable.bt_ic_vaulted_hiper),
    HIPERCARD(CardType.HIPERCARD.getFrontResource(), R.drawable.bt_ic_vaulted_hipercard),
    UNKNOWN(CardType.UNKNOWN.getFrontResource(), R.drawable.bt_ic_vaulted_unknown),
    ;

    @DrawableRes
    private final int drawable;

    @DrawableRes
    private final int vaultedDrawable;

    DropInPaymentMethodType(@DrawableRes int drawable, @DrawableRes int vaultedDrawable) {
        this.drawable = drawable;
        this.vaultedDrawable = vaultedDrawable;
    }

    /**
     * @return An id representing a {@link android.graphics.drawable.Drawable} icon for the current
     * {@link DropInPaymentMethodType}.
     */
    @DrawableRes
    public int getDrawable() {
        return drawable;
    }

    @DrawableRes
    int getVaultedDrawable() {
        return vaultedDrawable;
    }
}
