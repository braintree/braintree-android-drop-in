package com.braintreepayments.api;

import androidx.annotation.Nullable;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.cardform.utils.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum PaymentMethodType {
    // `getFrontResource` is pulling icons from android-card-form, `R.drawable` icons are drop-in internal
    AMEX(CardType.AMEX.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_amex, com.braintreepayments.api.dropin.R.string.bt_descriptor_amex, "American Express", CardType.AMEX),
    GOOGLE_PAYMENT(com.braintreepayments.api.dropin.R.drawable.bt_ic_google_pay, 0, com.braintreepayments.api.dropin.R.string.bt_descriptor_google_pay, "Google Pay", null),
    DINERS(CardType.DINERS_CLUB.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_diners_club, com.braintreepayments.api.dropin.R.string.bt_descriptor_diners, "Diners", CardType.DINERS_CLUB),
    DISCOVER(CardType.DISCOVER.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_discover, com.braintreepayments.api.dropin.R.string.bt_descriptor_discover, "Discover", CardType.DISCOVER),
    JCB(CardType.JCB.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_jcb, com.braintreepayments.api.dropin.R.string.bt_descriptor_jcb, "JCB", CardType.JCB),
    MAESTRO(CardType.MAESTRO.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_maestro, com.braintreepayments.api.dropin.R.string.bt_descriptor_maestro, "Maestro", CardType.MAESTRO),
    MASTERCARD(CardType.MASTERCARD.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_mastercard, com.braintreepayments.api.dropin.R.string.bt_descriptor_mastercard, "MasterCard", CardType.MASTERCARD),
    PAYPAL(com.braintreepayments.api.dropin.R.drawable.bt_ic_paypal, com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_paypal, com.braintreepayments.api.dropin.R.string.bt_descriptor_paypal, "PayPal", null),
    VISA(CardType.VISA.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_visa, com.braintreepayments.api.dropin.R.string.bt_descriptor_visa, "Visa", CardType.VISA),
    PAY_WITH_VENMO(com.braintreepayments.api.dropin.R.drawable.bt_ic_venmo, com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_venmo, com.braintreepayments.api.dropin.R.string.bt_descriptor_pay_with_venmo, "Venmo", null),
    UNIONPAY(CardType.UNIONPAY.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_unionpay, com.braintreepayments.api.dropin.R.string.bt_descriptor_unionpay, "UnionPay", CardType.UNIONPAY),
    HIPER(CardType.HIPER.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_hiper, com.braintreepayments.api.dropin.R.string.bt_descriptor_hiper, "Hiper", CardType.HIPER),
    HIPERCARD(CardType.HIPERCARD.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_hipercard, com.braintreepayments.api.dropin.R.string.bt_descriptor_hipercard, "Hipercard", CardType.HIPERCARD),
    UNKNOWN(CardType.UNKNOWN.getFrontResource(), com.braintreepayments.api.dropin.R.drawable.bt_ic_vaulted_unknown, R.string.bt_descriptor_unknown, "Unknown", CardType.UNKNOWN);

    private final int mIconDrawable;
    private final int mVaultedDrawable;
    private final int mLocalizedName;
    private String mCanonicalName;
    private CardType mCardType;

    PaymentMethodType(int iconDrawable, int vaultedDrawable, int localizedName, String canonicalName, CardType cardType) {
        mIconDrawable = iconDrawable;
        mVaultedDrawable = vaultedDrawable;
        mLocalizedName = localizedName;
        mCanonicalName = canonicalName;
        mCardType = cardType;
    }

    /**
     * @param paymentMethodType A {@link String} representing a canonical name for a payment
     * method.
     * @return a {@link PaymentMethodType} for for the given {@link String}, or {@link
     * PaymentMethodType#UNKNOWN} if no match could be made.
     */
    public static PaymentMethodType forType(@Nullable String paymentMethodType) {
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
     * @param supportedCardTypes a {@link Set<String>} of supported card types to parse into
     * {@link CardType}s.
     * @return a {@link CardType[]} containing the mapped {@link CardType}s from the supplied
     * supportedCardTypes.
     */
    public static CardType[] getCardsTypes(Set<String> supportedCardTypes) {
        List<CardType> convertedCardTypes = new ArrayList<>();
        for (String cardType : supportedCardTypes) {
            PaymentMethodType paymentMethodType = PaymentMethodType.forType(cardType);
            if (paymentMethodType != UNKNOWN && paymentMethodType.mCardType != null) {
                convertedCardTypes.add(paymentMethodType.mCardType);
            }
        }

        return convertedCardTypes.toArray(new CardType[convertedCardTypes.size()]);
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
