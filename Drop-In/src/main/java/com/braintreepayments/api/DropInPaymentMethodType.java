package com.braintreepayments.api;

public enum DropInPaymentMethodType {
    AMEX("American Express"),
    GOOGLE_PAY("Google Pay"),
    DINERS("Diners"),
    DISCOVER("Discover"),
    JCB("JCB"),
    MAESTRO("Maestro"),
    MASTERCARD("MasterCard"),
    PAYPAL("PayPal"),
    VISA("Visa"),
    PAY_WITH_VENMO("Venmo"),
    UNIONPAY("UnionPay"),
    HIPER("Hiper"),
    HIPERCARD("Hipercard"),
    UNKNOWN("Unknown"),
    ;

    static DropInPaymentMethodType from(String value) {
        for (DropInPaymentMethodType type : DropInPaymentMethodType.values()) {
            if (type.canonicalName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    private final String canonicalName;

    DropInPaymentMethodType(String canonicalName) {
        this.canonicalName = canonicalName;
    }
}
