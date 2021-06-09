package com.braintreepayments.api;

public class PaymentMethodNonceInspector {

    public String getDescription(PaymentMethodNonce paymentMethodNonce) {
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

    public String getTypeLabel(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            return ((CardNonce) paymentMethodNonce).getCardType();
        } else if (paymentMethodNonce instanceof PayPalAccountNonce) {
            return "PayPal";
        } else if (paymentMethodNonce instanceof VenmoAccountNonce) {
            return "Venmo";
        } else if (paymentMethodNonce instanceof GooglePayCardNonce) {
            return "Google Pay";
        } else {
            return "";
        }
    }
}
