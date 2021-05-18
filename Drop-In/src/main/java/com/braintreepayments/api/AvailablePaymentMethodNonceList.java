package com.braintreepayments.api;

import java.util.ArrayList;
import java.util.List;

class AvailablePaymentMethodNonceList {

    final private List<PaymentMethodNonce> items;

    AvailablePaymentMethodNonceList(Configuration configuration, List<PaymentMethodNonce> paymentMethodNonces, DropInRequest dropInRequest, boolean googlePayEnabled) {
        items = new ArrayList<>();

        for (PaymentMethodNonce paymentMethodNonce: paymentMethodNonces) {
            boolean shouldAddPaymentMethod = false;

            if (paymentMethodNonce.getType() == PaymentMethodType.PAYPAL) {
                shouldAddPaymentMethod = dropInRequest.isPayPalEnabled() && configuration.isPayPalEnabled();
            } else if (paymentMethodNonce.getType() == PaymentMethodType.VENMO) {
                shouldAddPaymentMethod = dropInRequest.isVenmoEnabled() && configuration.isVenmoEnabled();
            } else if (paymentMethodNonce.getType() == PaymentMethodType.CARD) {
                shouldAddPaymentMethod = dropInRequest.isCardEnabled() && !configuration.getSupportedCardTypes().isEmpty();
            } else if (paymentMethodNonce.getType() == PaymentMethodType.GOOGLE_PAY) {
                shouldAddPaymentMethod = googlePayEnabled && dropInRequest.isGooglePaymentEnabled();
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
