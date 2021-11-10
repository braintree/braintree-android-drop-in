package com.braintreepayments.api;

import com.braintreepayments.cardform.utils.CardType;

class PaymentMethodInspector {

    private static final String CANONICAL_NAME_AMEX = "American Express";
    private static final String CANONICAL_NAME_DINERS_CLUB = "Diners";
    private static final String CANONICAL_NAME_DISCOVER = "Discover";
    private static final String CANONICAL_NAME_GOOGLE_PAY = "Google Pay";
    private static final String CANONICAL_NAME_HIPER = "Hiper";
    private static final String CANONICAL_NAME_HIPERCARD = "Hipercard";
    private static final String CANONICAL_NAME_JCB = "JCB";
    private static final String CANONICAL_NAME_MAESTRO = "Maestro";
    private static final String CANONICAL_NAME_MASTERCARD = "MasterCard";
    private static final String CANONICAL_NAME_PAYPAL = "PayPal";
    private static final String CANONICAL_NAME_UNION_PAY = "UnionPay";
    private static final String CANONICAL_NAME_VENMO = "Venmo";
    private static final String CANONICAL_NAME_VISA = "Visa";

    String getPaymentMethodDescription(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            return ((CardNonce) paymentMethodNonce).getLastFour();
        } else if (paymentMethodNonce instanceof PayPalAccountNonce) {
            return ((PayPalAccountNonce) paymentMethodNonce).getEmail();
        } else if (paymentMethodNonce instanceof VenmoAccountNonce) {
            return ((VenmoAccountNonce) paymentMethodNonce).getUsername();
        } else if (paymentMethodNonce instanceof GooglePayCardNonce) {
            return ((GooglePayCardNonce) paymentMethodNonce).getLastFour();
        } else {
            return "";
        }
    }

    DropInPaymentMethodType getPaymentMethodType(PaymentMethodNonce paymentMethodNonce) {
        String canonicalName = getPaymentMethodCanonicalName(paymentMethodNonce);
        if (canonicalName != null) {
            switch (canonicalName) {
                case CANONICAL_NAME_AMEX:
                    return DropInPaymentMethodType.AMEX;
                case CANONICAL_NAME_DINERS_CLUB:
                    return DropInPaymentMethodType.DINERS;
                case CANONICAL_NAME_DISCOVER:
                    return DropInPaymentMethodType.DISCOVER;
                case CANONICAL_NAME_JCB:
                    return DropInPaymentMethodType.JCB;
                case CANONICAL_NAME_MAESTRO:
                    return DropInPaymentMethodType.MAESTRO;
                case CANONICAL_NAME_MASTERCARD:
                    return DropInPaymentMethodType.MASTERCARD;
                case CANONICAL_NAME_VISA:
                    return DropInPaymentMethodType.VISA;
                case CANONICAL_NAME_UNION_PAY:
                    return DropInPaymentMethodType.UNIONPAY;
                case CANONICAL_NAME_HIPER:
                    return DropInPaymentMethodType.HIPER;
                case CANONICAL_NAME_HIPERCARD:
                    return DropInPaymentMethodType.HIPERCARD;
                case CANONICAL_NAME_PAYPAL:
                    return DropInPaymentMethodType.PAYPAL;
                case CANONICAL_NAME_VENMO:
                    return DropInPaymentMethodType.PAY_WITH_VENMO;
                case CANONICAL_NAME_GOOGLE_PAY:
                    return DropInPaymentMethodType.GOOGLE_PAY;
                default:
                    return null;
            }
        }
        return null;
    }

    private String getPaymentMethodCanonicalName(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            return ((CardNonce) paymentMethodNonce).getCardType();
        } else if (paymentMethodNonce instanceof PayPalAccountNonce) {
            return CANONICAL_NAME_PAYPAL;
        } else if (paymentMethodNonce instanceof VenmoAccountNonce) {
            return CANONICAL_NAME_VENMO;
        } else if (paymentMethodNonce instanceof GooglePayCardNonce) {
            return CANONICAL_NAME_GOOGLE_PAY;
        } else {
            return null;
        }
    }

    CardType parseCardType(String cardType) {
        switch (cardType) {
            case CANONICAL_NAME_AMEX:
                return CardType.AMEX;
            case CANONICAL_NAME_DINERS_CLUB:
                return CardType.DINERS_CLUB;
            case CANONICAL_NAME_DISCOVER:
                return CardType.DISCOVER;
            case CANONICAL_NAME_JCB:
                return CardType.JCB;
            case CANONICAL_NAME_MAESTRO:
                return CardType.MAESTRO;
            case CANONICAL_NAME_MASTERCARD:
                return CardType.MASTERCARD;
            case CANONICAL_NAME_VISA:
                return CardType.VISA;
            case CANONICAL_NAME_UNION_PAY:
                return CardType.UNIONPAY;
            case CANONICAL_NAME_HIPER:
                return CardType.HIPER;
            case CANONICAL_NAME_HIPERCARD:
                return CardType.HIPERCARD;
            default:
                return null;
        }
    }
}
