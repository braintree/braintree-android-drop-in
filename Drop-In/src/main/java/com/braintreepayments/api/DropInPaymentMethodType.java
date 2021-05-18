package com.braintreepayments.api;

import androidx.annotation.Nullable;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum DropInPaymentMethodType {
    // `getFrontResource` is pulling icons from android-card-form, `R.drawable` icons are drop-in internal
    AMEX(CardType.AMEX.getFrontResource(), R.drawable.bt_ic_vaulted_amex, R.string.bt_descriptor_amex, "American Express", CardType.AMEX),
    GOOGLE_PAYMENT(R.drawable.bt_ic_google_pay, 0, R.string.bt_descriptor_google_pay, "Google Pay", null),
    DINERS(CardType.DINERS_CLUB.getFrontResource(), R.drawable.bt_ic_vaulted_diners_club, R.string.bt_descriptor_diners, "Diners", CardType.DINERS_CLUB),
    DISCOVER(CardType.DISCOVER.getFrontResource(), R.drawable.bt_ic_vaulted_discover, R.string.bt_descriptor_discover, "Discover", CardType.DISCOVER),
    JCB(CardType.JCB.getFrontResource(), R.drawable.bt_ic_vaulted_jcb, R.string.bt_descriptor_jcb, "JCB", CardType.JCB),
    MAESTRO(CardType.MAESTRO.getFrontResource(), R.drawable.bt_ic_vaulted_maestro, R.string.bt_descriptor_maestro, "Maestro", CardType.MAESTRO),
    MASTERCARD(CardType.MASTERCARD.getFrontResource(), R.drawable.bt_ic_vaulted_mastercard, R.string.bt_descriptor_mastercard, "MasterCard", CardType.MASTERCARD),
    PAYPAL(R.drawable.bt_ic_paypal, R.drawable.bt_ic_vaulted_paypal, R.string.bt_descriptor_paypal, "PayPal", null),
    VISA(CardType.VISA.getFrontResource(), R.drawable.bt_ic_vaulted_visa, R.string.bt_descriptor_visa, "Visa", CardType.VISA),
    PAY_WITH_VENMO(R.drawable.bt_ic_venmo, R.drawable.bt_ic_vaulted_venmo, R.string.bt_descriptor_pay_with_venmo, "Venmo", null),
    UNIONPAY(CardType.UNIONPAY.getFrontResource(), R.drawable.bt_ic_vaulted_unionpay, R.string.bt_descriptor_unionpay, "UnionPay", CardType.UNIONPAY),
    HIPER(CardType.HIPER.getFrontResource(), R.drawable.bt_ic_vaulted_hiper, R.string.bt_descriptor_hiper, "Hiper", CardType.HIPER),
    HIPERCARD(CardType.HIPERCARD.getFrontResource(), R.drawable.bt_ic_vaulted_hipercard, R.string.bt_descriptor_hipercard, "Hipercard", CardType.HIPERCARD),
    UNKNOWN(CardType.UNKNOWN.getFrontResource(), R.drawable.bt_ic_vaulted_unknown, R.string.bt_descriptor_unknown, "Unknown", CardType.UNKNOWN);

    private final int mIconDrawable;
    private final int mVaultedDrawable;
    private final int mLocalizedName;
    private String mCanonicalName;
    private CardType mCardType;

    DropInPaymentMethodType(int iconDrawable, int vaultedDrawable, int localizedName, String canonicalName, CardType cardType) {
        mIconDrawable = iconDrawable;
        mVaultedDrawable = vaultedDrawable;
        mLocalizedName = localizedName;
        mCanonicalName = canonicalName;
        mCardType = cardType;
    }

    /**
     * @param paymentMethodType A {@link String} representing a canonical name for a payment
     * method.
     * @return a {@link DropInPaymentMethodType} for for the given {@link String}, or {@link
     * DropInPaymentMethodType#UNKNOWN} if no match could be made.
     */
    public static DropInPaymentMethodType forType(@Nullable String paymentMethodType) {
        for (DropInPaymentMethodType type : values()) {
            if (type.mCanonicalName.equals(paymentMethodType)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * @param paymentMethodNonce A {@link PaymentMethodNonce} to get the {@link DropInPaymentMethodType} of.
     * @return a {@link DropInPaymentMethodType} for the given {@link PaymentMethodNonce}, or {@link DropInPaymentMethodType#UNKNOWN}
     * if no match could be made.
     */
    public static DropInPaymentMethodType forType(PaymentMethodNonce paymentMethodNonce) {
        // TODO: revisit type label
        return UNKNOWN;
//        return forType(paymentMethodNonce.getTypeLabel());
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
            DropInPaymentMethodType paymentMethodType = DropInPaymentMethodType.forType(cardType);
            if (paymentMethodType != UNKNOWN && paymentMethodType.mCardType != null) {
                convertedCardTypes.add(paymentMethodType.mCardType);
            }
        }

        return convertedCardTypes.toArray(new CardType[convertedCardTypes.size()]);
    }

    /**
     * @return An id representing a {@link android.graphics.drawable.Drawable} icon for the current
     * {@link DropInPaymentMethodType}.
     */
    public int getDrawable() {
        return mIconDrawable;
    }

    /**
     * @return An id representing a {@link android.graphics.drawable.Drawable} vaulted icon for the current
     * {@link DropInPaymentMethodType}.
     */
    public int getVaultedDrawable() {
        return mVaultedDrawable;
    }

    /**
     * @return An id representing a localized {@link String} for the current {@link
     * DropInPaymentMethodType}.
     */
    public int getLocalizedName() {
        return mLocalizedName;
    }

    /**
     * @return A {@link String} name of the {@link DropInPaymentMethodType} as it is categorized by
     * Braintree.
     */
    public String getCanonicalName() {
        return mCanonicalName;
    }
}
