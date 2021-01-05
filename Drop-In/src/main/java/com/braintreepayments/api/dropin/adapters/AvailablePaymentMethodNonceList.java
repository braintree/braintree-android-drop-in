package com.braintreepayments.api.dropin.adapters;

import android.content.Context;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.GooglePaymentCardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.VenmoAccountNonce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AvailablePaymentMethodNonceList {

    final private List<PaymentMethodNonce> items;

    AvailablePaymentMethodNonceList(Context context, Configuration configuration, List<PaymentMethodNonce> paymentMethodNonces, DropInRequest dropInRequest, boolean googlePayEnabled, boolean unionpaySupported) {
        items = new ArrayList<>();

        for (PaymentMethodNonce paymentMethodNonce: paymentMethodNonces) {
            boolean shouldAddPaymentMethod = false;

            if (paymentMethodNonce instanceof PayPalAccountNonce && dropInRequest.isPayPalEnabled() && configuration.isPayPalEnabled()) {
                shouldAddPaymentMethod = true;
            } else if (paymentMethodNonce instanceof VenmoAccountNonce && dropInRequest.isVenmoEnabled() && configuration.getPayWithVenmo().isEnabled(context)) {
                shouldAddPaymentMethod = true;
            } else if (paymentMethodNonce instanceof CardNonce && dropInRequest.isCardEnabled()) {
                Set<String> supportedCardTypes =
                        new HashSet<>(configuration.getCardConfiguration().getSupportedCardTypes());
                if (!unionpaySupported) {
                    supportedCardTypes.remove(PaymentMethodType.UNIONPAY.getCanonicalName());
                }
                shouldAddPaymentMethod = !supportedCardTypes.isEmpty();
            } else if (paymentMethodNonce instanceof GooglePaymentCardNonce && googlePayEnabled && dropInRequest.isGooglePaymentEnabled()) {
                shouldAddPaymentMethod = true;
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
}
