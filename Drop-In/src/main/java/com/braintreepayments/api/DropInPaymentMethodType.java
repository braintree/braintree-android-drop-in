package com.braintreepayments.api;

import com.braintreepayments.cardform.utils.CardType;

public enum DropInPaymentMethodType {
    AMEX("American Express", CardType.AMEX),
    GOOGLE_PAY("Google Pay", null),
    DINERS("Diners", CardType.DINERS_CLUB),
    DISCOVER("Discover", CardType.DISCOVER),
    JCB("JCB", CardType.JCB),
    MAESTRO("Maestro", CardType.MAESTRO),
    MASTERCARD("MasterCard", CardType.MASTERCARD),
    PAYPAL("PayPal", null),
    VISA("Visa", CardType.VISA),
    PAY_WITH_VENMO("Venmo", null),
    UNIONPAY("UnionPay", CardType.UNIONPAY),
    HIPER("Hiper", CardType.HIPER),
    HIPERCARD("Hipercard", CardType.HIPERCARD),
    UNKNOWN("Unknown", CardType.UNKNOWN),
    ;

    static DropInPaymentMethodType from(String value) {
        for (DropInPaymentMethodType type : DropInPaymentMethodType.values()) {
            if (type.canonicalName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    private final CardType cardType;
    private final String canonicalName;

    DropInPaymentMethodType(String canonicalName, CardType cardType) {
        this.canonicalName = canonicalName;
        this.cardType = cardType;
    }

    /**
     * @return A {@link String} name of the {@link DropInPaymentMethodType} as it is categorized by
     * Braintree.
     */
    String getCanonicalName() {
        return canonicalName;
    }

    public CardType getCardType() {
        return cardType;
    }
}
