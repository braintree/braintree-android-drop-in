package com.braintreepayments.api;

import android.content.Context;


import java.util.ArrayList;
import java.util.List;

class AvailablePaymentMethodNonceList {

    final private List<PaymentMethodNonce> items;

    AvailablePaymentMethodNonceList(Context context, Configuration configuration, List<PaymentMethodNonce> paymentMethodNonces, DropInRequest dropInRequest, boolean googlePayEnabled) {
        items = new ArrayList<>();

        for (PaymentMethodNonce paymentMethodNonce: paymentMethodNonces) {
            boolean shouldAddPaymentMethod = false;

            if (paymentMethodNonce instanceof PayPalAccountNonce) {
                shouldAddPaymentMethod = dropInRequest.isPayPalEnabled() && configuration.isPayPalEnabled();
            } else if (paymentMethodNonce instanceof VenmoAccountNonce) {
                shouldAddPaymentMethod = dropInRequest.isVenmoEnabled() && configuration.isVenmoEnabled();
            } else if (paymentMethodNonce instanceof CardNonce) {
                shouldAddPaymentMethod = dropInRequest.isCardEnabled() && !configuration.getSupportedCardTypes().isEmpty();
            } else if (paymentMethodNonce instanceof GooglePayCardNonce) {
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

    boolean hasCardNonce() {
        for (PaymentMethodNonce nonce : items) {
            if (nonce instanceof CardNonce) {
                return true;
            }
        }
        return false;
    }
}
