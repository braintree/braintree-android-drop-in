package com.braintreepayments.api;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public enum DropInPaymentMethod {

    // `getFrontResource` is pulling icons from android-card-form, `R.drawable` icons are drop-in internal
    AMEX(CardType.AMEX.getFrontResource(), R.drawable.bt_ic_vaulted_amex, R.string.bt_descriptor_amex),
    GOOGLE_PAY(R.drawable.bt_ic_google_pay, 0, R.string.bt_descriptor_google_pay),
    DINERS_CLUB(CardType.DINERS_CLUB.getFrontResource(), R.drawable.bt_ic_vaulted_diners_club, R.string.bt_descriptor_diners),
    DISCOVER(CardType.DISCOVER.getFrontResource(), R.drawable.bt_ic_vaulted_discover, R.string.bt_descriptor_discover),
    JCB(CardType.JCB.getFrontResource(), R.drawable.bt_ic_vaulted_jcb, R.string.bt_descriptor_jcb),
    MAESTRO(CardType.MAESTRO.getFrontResource(), R.drawable.bt_ic_vaulted_maestro, R.string.bt_descriptor_maestro),
    MASTERCARD(CardType.MASTERCARD.getFrontResource(), R.drawable.bt_ic_vaulted_mastercard, R.string.bt_descriptor_mastercard),
    PAYPAL(R.drawable.bt_ic_paypal, R.drawable.bt_ic_vaulted_paypal, R.string.bt_descriptor_paypal),
    VISA(CardType.VISA.getFrontResource(), R.drawable.bt_ic_vaulted_visa, R.string.bt_descriptor_visa),
    VENMO(R.drawable.bt_ic_venmo, R.drawable.bt_ic_vaulted_venmo, R.string.bt_descriptor_pay_with_venmo),
    UNIONPAY(CardType.UNIONPAY.getFrontResource(), R.drawable.bt_ic_vaulted_unionpay, R.string.bt_descriptor_unionpay),
    HIPER(CardType.HIPER.getFrontResource(), R.drawable.bt_ic_vaulted_hiper, R.string.bt_descriptor_hiper),
    HIPERCARD(CardType.HIPERCARD.getFrontResource(), R.drawable.bt_ic_vaulted_hipercard, R.string.bt_descriptor_hipercard),
    UNKNOWN(CardType.UNKNOWN.getFrontResource(), R.drawable.bt_ic_vaulted_unknown, R.string.bt_descriptor_unknown);

    @DrawableRes
    private final int drawable;

    @DrawableRes
    private final int vaultedDrawable;

    @StringRes
    private final int localizedName;

    DropInPaymentMethod(@DrawableRes int drawable, @DrawableRes int vaultedDrawable, @StringRes int localizedName) {
        this.drawable = drawable;
        this.vaultedDrawable = vaultedDrawable;
        this.localizedName = localizedName;
    }

    /**
     * @return An id representing the {@link android.graphics.drawable.Drawable} icon resource for this payment method.
     */
    @DrawableRes
    public int getDrawable() {
        return drawable;
    }

    /**
     * @return An id representing the localized {@link String} string resource for this payment method.
     */
    @StringRes
    public int getLocalizedName() {
        return localizedName;
    }

    @DrawableRes
    int getVaultedDrawable() {
        return vaultedDrawable;
    }
}
