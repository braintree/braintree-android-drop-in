package com.braintreepayments.api;

import com.braintreepayments.cardform.utils.CardType;

import java.util.HashMap;
import java.util.Map;

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

    private final Map<String, CardType> cardTypes;
    private final Map<String, DropInPaymentMethodType> paymentMethodTypes;

    PaymentMethodInspector() {
        paymentMethodTypes = new HashMap<>();
        paymentMethodTypes.put(PAYMENT_METHOD_AMEX, DropInPaymentMethodType.AMEX);
        paymentMethodTypes.put(PAYMENT_METHOD_DINERS_CLUB, DropInPaymentMethodType.DINERS);
        paymentMethodTypes.put(PAYMENT_METHOD_DISCOVER, DropInPaymentMethodType.DISCOVER);
        paymentMethodTypes.put(PAYMENT_METHOD_GOOGLE_PAY, DropInPaymentMethodType.GOOGLE_PAY);
        paymentMethodTypes.put(PAYMENT_METHOD_HIPER, DropInPaymentMethodType.HIPER);
        paymentMethodTypes.put(PAYMENT_METHOD_HIPERCARD, DropInPaymentMethodType.HIPERCARD);
        paymentMethodTypes.put(PAYMENT_METHOD_JCB, DropInPaymentMethodType.JCB);
        paymentMethodTypes.put(PAYMENT_METHOD_MAESTRO, DropInPaymentMethodType.MAESTRO);
        paymentMethodTypes.put(PAYMENT_METHOD_MASTERCARD, DropInPaymentMethodType.MASTERCARD);
        paymentMethodTypes.put(PAYMENT_METHOD_PAYPAL, DropInPaymentMethodType.PAYPAL);
        paymentMethodTypes.put(PAYMENT_METHOD_UNION_PAY, DropInPaymentMethodType.UNIONPAY);
        paymentMethodTypes.put(PAYMENT_METHOD_VENMO, DropInPaymentMethodType.PAY_WITH_VENMO);
        paymentMethodTypes.put(PAYMENT_METHOD_VISA, DropInPaymentMethodType.VISA);

        cardTypes = new HashMap<>();
        cardTypes.put(PAYMENT_METHOD_AMEX, CardType.AMEX);
        cardTypes.put(PAYMENT_METHOD_DINERS_CLUB, CardType.DINERS_CLUB);
        cardTypes.put(PAYMENT_METHOD_DISCOVER, CardType.DISCOVER);
        cardTypes.put(PAYMENT_METHOD_HIPER, CardType.HIPER);
        cardTypes.put(PAYMENT_METHOD_HIPERCARD, CardType.HIPERCARD);
        cardTypes.put(PAYMENT_METHOD_JCB, CardType.JCB);
        cardTypes.put(PAYMENT_METHOD_MAESTRO, CardType.MAESTRO);
        cardTypes.put(PAYMENT_METHOD_MASTERCARD, CardType.MASTERCARD);
        cardTypes.put(PAYMENT_METHOD_UNION_PAY, CardType.UNIONPAY);
        cardTypes.put(PAYMENT_METHOD_VISA, CardType.VISA);
    }

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
            if (paymentMethodTypes.containsKey(canonicalName)) {
                return paymentMethodTypes.get(canonicalName);
            }
        }
        return null;
    }

    CardType parseCardType(String cardType) {
        if (cardTypes.containsKey(cardType)) {
            return cardTypes.get(cardType);
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
}
