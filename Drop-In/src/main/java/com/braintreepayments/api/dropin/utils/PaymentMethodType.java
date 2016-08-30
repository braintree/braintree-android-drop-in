package com.braintreepayments.api.dropin.utils;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.cardform.utils.CardType;

public enum PaymentMethodType {

    AMEX(CardType.AMEX.getFrontResource(), R.drawable.bt_ic_vaulted_amex, R.string.bt_descriptor_amex, "American Express"),
    ANDROID_PAY(R.drawable.bt_ic_android_pay, 0, R.string.bt_descriptor_android_pay, "Android Pay"),
    DINERS(CardType.DINERS_CLUB.getFrontResource(), R.drawable.bt_ic_vaulted_diners_club, R.string.bt_descriptor_diners, "Diners"),
    DISCOVER(CardType.DISCOVER.getFrontResource(), R.drawable.bt_ic_vaulted_discover, R.string.bt_descriptor_discover, "Discover"),
    JCB(CardType.JCB.getFrontResource(), R.drawable.bt_ic_vaulted_jcb, R.string.bt_descriptor_jcb, "JCB"),
    MAESTRO(CardType.MAESTRO.getFrontResource(), R.drawable.bt_ic_vaulted_maestro, R.string.bt_descriptor_maestro, "Maestro"),
    MASTERCARD(CardType.MASTERCARD.getFrontResource(), R.drawable.bt_ic_vaulted_mastercard, R.string.bt_descriptor_mastercard, "MasterCard"),
    PAYPAL(R.drawable.bt_ic_paypal, R.drawable.bt_ic_vaulted_paypal, R.string.bt_descriptor_paypal, "PayPal"),
    VISA(CardType.VISA.getFrontResource(), R.drawable.bt_ic_vaulted_visa, R.string.bt_descriptor_visa, "Visa"),
    PAY_WITH_VENMO(R.drawable.bt_ic_venmo, R.drawable.bt_ic_vaulted_venmo, R.string.bt_descriptor_pay_with_venmo, "Venmo"),
    UNIONPAY(CardType.UNIONPAY.getFrontResource(), R.drawable.bt_ic_vaulted_unionpay, R.string.bt_descriptor_unionpay, "UnionPay"),
    UNKNOWN(CardType.UNKNOWN.getFrontResource(), R.drawable.bt_ic_vaulted_unknown, R.string.bt_descriptor_unknown, "Unknown");

    private final int mIconDrawable;
    private final int mVaultedDrawable;
    private final int mLocalizedName;
    private String mCanonicalName;

    PaymentMethodType(int iconDrawable, int vaultedDrawable, int localizedName, String canonicalName) {
        mIconDrawable = iconDrawable;
        mVaultedDrawable = vaultedDrawable;
        mLocalizedName = localizedName;
        mCanonicalName = canonicalName;
    }

    /**
     * @param paymentMethodType A {@link String} representing a canonical name for a payment
     * method.
     * @return a {@link PaymentMethodType} for for the given {@link String}, or {@link
     * PaymentMethodType#UNKNOWN} if no match could be made.
     */
    public static PaymentMethodType forType(String paymentMethodType) {
        for (PaymentMethodType type : values()) {
            if (type.mCanonicalName.equals(paymentMethodType)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * @param paymentMethodNonce A {@link PaymentMethodNonce} to get the {@link PaymentMethodType} of.
     * @return a {@link PaymentMethodType} for the given {@link PaymentMethodNonce}, or {@link PaymentMethodType#UNKNOWN}
     * if no match could be made.
     */
    public static PaymentMethodType forType(PaymentMethodNonce paymentMethodNonce) {
        return forType(paymentMethodNonce.getTypeLabel());
    }

    /**
     * @return An id representing a {@link android.graphics.drawable.Drawable} icon for the current
     * {@link PaymentMethodType}.
     */
    public int getDrawable() {
        return mIconDrawable;
    }

    /**
     * @return An id representing a {@link android.graphics.drawable.Drawable} vaulted icon for the current
     * {@link PaymentMethodType}.
     */
    public int getVaultedDrawable() {
        return mVaultedDrawable;
    }

    /**
     * @return An id representing a localized {@link String} for the current {@link
     * PaymentMethodType}.
     */
    public int getLocalizedName() {
        return mLocalizedName;
    }

    /**
     * @return A {@link String} name of the {@link PaymentMethodType} as it is categorized by
     * Braintree.
     */
    public String getCanonicalName() {
        return mCanonicalName;
    }
}
