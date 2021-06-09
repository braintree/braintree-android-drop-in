package com.braintreepayments.api;

public class DropInPaymentMethodNonce {

    private PaymentMethodNonce paymentMethodNonce;

    DropInPaymentMethodNonce(PaymentMethodNonce paymentMethodNonce) {
        this.paymentMethodNonce = paymentMethodNonce;
    }

    String paymentDescription() {
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

    String typeLabel() {
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
