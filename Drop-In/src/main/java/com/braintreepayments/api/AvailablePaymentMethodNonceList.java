package com.braintreepayments.api;

import java.util.ArrayList;
import java.util.List;

class AvailablePaymentMethodNonceList {

    final private List<PaymentMethodNonce> items;

    AvailablePaymentMethodNonceList(Configuration configuration, List<PaymentMethodNonce> paymentMethodNonces, DropInRequest dropInRequest, boolean googlePayEnabled) {
        items = new ArrayList<>();

        for (PaymentMethodNonce paymentMethodNonce: paymentMethodNonces) {
            boolean shouldAddPaymentMethod = false;

            if (paymentMethodNonce instanceof PayPalAccountNonce) {
                shouldAddPaymentMethod = !dropInRequest.isPayPalDisabled() && configuration.isPayPalEnabled();
            } else if (paymentMethodNonce instanceof VenmoAccountNonce) {
                shouldAddPaymentMethod = !dropInRequest.isVenmoDisabled() && configuration.isVenmoEnabled();
            } else if (paymentMethodNonce instanceof CardNonce) {
                shouldAddPaymentMethod = !dropInRequest.isCardDisabled() && !configuration.getSupportedCardTypes().isEmpty();
            } else if (paymentMethodNonce instanceof GooglePayCardNonce) {
                shouldAddPaymentMethod = googlePayEnabled && !dropInRequest.isGooglePayDisabled();
            }

            if (shouldAddPaymentMethod) {
                items.add(paymentMethodNonce);
            }
        }
    }

    int size() {
        return items.size();
    }

    PaymentMethodNonce get(int index) {
        return items.get(index);
    }

    public List<PaymentMethodNonce> getItems() {
        return items;
    }
}
