package com.braintreepayments.api.dropin.adapters;

import android.content.Context;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AvailablePaymentMethodList {

    final private List<PaymentMethodType> items;

    AvailablePaymentMethodList(Context context, Configuration configuration, DropInRequest dropInRequest, boolean googlePayEnabled, boolean unionpaySupported) {
        items = new ArrayList<>();

        if (dropInRequest.isPayPalEnabled() && configuration.isPayPalEnabled()) {
            items.add(PaymentMethodType.PAYPAL);
        }

        if (dropInRequest.isVenmoEnabled() && configuration.getPayWithVenmo().isEnabled(context)) {
            items.add(PaymentMethodType.PAY_WITH_VENMO);
        }

        if (dropInRequest.isCardEnabled()) {
            Set<String> supportedCardTypes =
                    new HashSet<>(configuration.getCardConfiguration().getSupportedCardTypes());
            if (!unionpaySupported) {
                supportedCardTypes.remove(PaymentMethodType.UNIONPAY.getCanonicalName());
            }
            if (supportedCardTypes.size() > 0) {
                items.add(PaymentMethodType.UNKNOWN);
            }
        }

        if (googlePayEnabled) {
            if (dropInRequest.isGooglePaymentEnabled()) {
                items.add(PaymentMethodType.GOOGLE_PAYMENT);
            }
        }
    }

    int size() {
        return items.size();
    }

    PaymentMethodType getItem(int index) {
        return items.get(index);
    }
}
