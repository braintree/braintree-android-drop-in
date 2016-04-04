package com.braintreepayments.api.dropin.utils;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

public enum PaymentMethodType {

    AMEX(CardType.AMEX.getFrontResource(), R.string.bt_descriptor_amex, "American Express"),
    ANDROID_PAY(R.drawable.bt_android_pay, R.string.bt_descriptor_android_pay, "Android Pay"),
    DINERS(CardType.DINERS_CLUB.getFrontResource(), R.string.bt_descriptor_diners, "Diners"),
    DISCOVER(CardType.DISCOVER.getFrontResource(), R.string.bt_descriptor_discover, "Discover"),
    JCB(CardType.JCB.getFrontResource(), R.string.bt_descriptor_jcb, "JCB"),
    MAESTRO(CardType.MAESTRO.getFrontResource(), R.string.bt_descriptor_maestro, "Maestro"),
    MASTERCARD(CardType.MASTERCARD.getFrontResource(), R.string.bt_descriptor_mastercard, "MasterCard"),
    PAYPAL(R.drawable.bt_paypal, R.string.bt_descriptor_paypal, "PayPal"),
    VISA(CardType.VISA.getFrontResource(), R.string.bt_descriptor_visa, "Visa"),
    PAY_WITH_VENMO(R.drawable.bt_venmo, R.string.bt_descriptor_pay_with_venmo, "Venmo"),
    UNKNOWN(R.drawable.bt_ic_payment_method_card, R.string.bt_descriptor_unknown, "Credit or Debit Card");

    private final int mDrawable;
    private final int mLocalizedName;
    private String mCanonicalName;

    PaymentMethodType(int drawable, int localizedName, String canonicalName) {
        mDrawable = drawable;
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
     * @return An id representing a {@link android.graphics.drawable.Drawable} icon for the current
     * {@link PaymentMethodType}.
     */
    public int getDrawable() {
        return mDrawable;
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
