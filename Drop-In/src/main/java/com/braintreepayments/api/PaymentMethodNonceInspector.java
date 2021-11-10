package com.braintreepayments.api;

class PaymentMethodNonceInspector {

    String getDescription(PaymentMethodNonce paymentMethodNonce) {
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

    private String getCanonicalName(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            return ((CardNonce) paymentMethodNonce).getCardType();
        } else if (paymentMethodNonce instanceof PayPalAccountNonce) {
            return PaymentMethodCanonicalName.PAYPAL;
        } else if (paymentMethodNonce instanceof VenmoAccountNonce) {
            return PaymentMethodCanonicalName.VENMO;
        } else if (paymentMethodNonce instanceof GooglePayCardNonce) {
            return PaymentMethodCanonicalName.GOOGLE_PAY;
        } else {
            return null;
        }
    }

    DropInPaymentMethodType getPaymentMethodType(PaymentMethodNonce paymentMethodNonce) {
        String canonicalName = getCanonicalName(paymentMethodNonce);
        if (canonicalName != null) {
            switch (canonicalName) {
                case PaymentMethodCanonicalName.AMEX:
                    return DropInPaymentMethodType.AMEX;
                case PaymentMethodCanonicalName.DINERS_CLUB:
                    return DropInPaymentMethodType.DINERS;
                case PaymentMethodCanonicalName.DISCOVER:
                    return DropInPaymentMethodType.DISCOVER;
                case PaymentMethodCanonicalName.JCB:
                    return DropInPaymentMethodType.JCB;
                case PaymentMethodCanonicalName.MAESTRO:
                    return DropInPaymentMethodType.MAESTRO;
                case PaymentMethodCanonicalName.MASTERCARD:
                    return DropInPaymentMethodType.MASTERCARD;
                case PaymentMethodCanonicalName.VISA:
                    return DropInPaymentMethodType.VISA;
                case PaymentMethodCanonicalName.UNION_PAY:
                    return DropInPaymentMethodType.UNIONPAY;
                case PaymentMethodCanonicalName.HIPER:
                    return DropInPaymentMethodType.HIPER;
                case PaymentMethodCanonicalName.HIPERCARD:
                    return DropInPaymentMethodType.HIPERCARD;
                case PaymentMethodCanonicalName.PAYPAL:
                    return DropInPaymentMethodType.PAYPAL;
                case PaymentMethodCanonicalName.VENMO:
                    return DropInPaymentMethodType.PAY_WITH_VENMO;
                case PaymentMethodCanonicalName.GOOGLE_PAY:
                    return DropInPaymentMethodType.GOOGLE_PAY;
                default:
                    return null;
            }
        }
        return null;
    }
}
