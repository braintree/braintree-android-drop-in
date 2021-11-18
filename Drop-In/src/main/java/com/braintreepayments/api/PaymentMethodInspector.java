package com.braintreepayments.api;

import com.braintreepayments.cardform.utils.CardType;

class PaymentMethodInspector {

    private static final String PAYMENT_METHOD_AMEX = "American Express";
    private static final String PAYMENT_METHOD_DINERS_CLUB = "Diners";
    private static final String PAYMENT_METHOD_DISCOVER = "Discover";
    private static final String PAYMENT_METHOD_GOOGLE_PAY = "Google Pay";
    private static final String PAYMENT_METHOD_HIPER = "Hiper";
    private static final String PAYMENT_METHOD_HIPERCARD = "Hipercard";
    private static final String PAYMENT_METHOD_JCB = "JCB";
    private static final String PAYMENT_METHOD_MAESTRO = "Maestro";
    private static final String PAYMENT_METHOD_MASTERCARD = "MasterCard";
    private static final String PAYMENT_METHOD_PAYPAL = "PayPal";
    private static final String PAYMENT_METHOD_UNION_PAY = "UnionPay";
    private static final String PAYMENT_METHOD_VENMO = "Venmo";
    private static final String PAYMENT_METHOD_VISA = "Visa";

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

    DropInPaymentMethod getPaymentMethod(PaymentMethodNonce paymentMethodNonce) {
        String canonicalName = getPaymentMethodCanonicalName(paymentMethodNonce);
        if (canonicalName != null) {
            switch (canonicalName) {
                case PAYMENT_METHOD_AMEX:
                    return DropInPaymentMethod.AMEX;
                case PAYMENT_METHOD_DINERS_CLUB:
                    return DropInPaymentMethod.DINERS_CLUB;
                case PAYMENT_METHOD_DISCOVER:
                    return DropInPaymentMethod.DISCOVER;
                case PAYMENT_METHOD_JCB:
                    return DropInPaymentMethod.JCB;
                case PAYMENT_METHOD_MAESTRO:
                    return DropInPaymentMethod.MAESTRO;
                case PAYMENT_METHOD_MASTERCARD:
                    return DropInPaymentMethod.MASTERCARD;
                case PAYMENT_METHOD_VISA:
                    return DropInPaymentMethod.VISA;
                case PAYMENT_METHOD_UNION_PAY:
                    return DropInPaymentMethod.UNIONPAY;
                case PAYMENT_METHOD_HIPER:
                    return DropInPaymentMethod.HIPER;
                case PAYMENT_METHOD_HIPERCARD:
                    return DropInPaymentMethod.HIPERCARD;
                case PAYMENT_METHOD_PAYPAL:
                    return DropInPaymentMethod.PAYPAL;
                case PAYMENT_METHOD_VENMO:
                    return DropInPaymentMethod.VENMO;
                case PAYMENT_METHOD_GOOGLE_PAY:
                    return DropInPaymentMethod.GOOGLE_PAY;
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
            return PAYMENT_METHOD_PAYPAL;
        } else if (paymentMethodNonce instanceof VenmoAccountNonce) {
            return PAYMENT_METHOD_VENMO;
        } else if (paymentMethodNonce instanceof GooglePayCardNonce) {
            return PAYMENT_METHOD_GOOGLE_PAY;
        } else {
            return null;
        }
    }

    CardType getCardTypeFromString(String cardType) {
        switch (cardType) {
            case PAYMENT_METHOD_AMEX:
                return CardType.AMEX;
            case PAYMENT_METHOD_DINERS_CLUB:
                return CardType.DINERS_CLUB;
            case PAYMENT_METHOD_DISCOVER:
                return CardType.DISCOVER;
            case PAYMENT_METHOD_JCB:
                return CardType.JCB;
            case PAYMENT_METHOD_MAESTRO:
                return CardType.MAESTRO;
            case PAYMENT_METHOD_MASTERCARD:
                return CardType.MASTERCARD;
            case PAYMENT_METHOD_VISA:
                return CardType.VISA;
            case PAYMENT_METHOD_UNION_PAY:
                return CardType.UNIONPAY;
            case PAYMENT_METHOD_HIPER:
                return CardType.HIPER;
            case PAYMENT_METHOD_HIPERCARD:
                return CardType.HIPERCARD;
            default:
                return null;
        }
    }
}
