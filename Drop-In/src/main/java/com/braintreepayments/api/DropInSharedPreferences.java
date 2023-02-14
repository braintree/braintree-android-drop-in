package com.braintreepayments.api;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

class DropInSharedPreferences {

    private static final String LAST_USED_PAYMENT_METHOD =
            "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD";

    private static volatile DropInSharedPreferences INSTANCE;

    static DropInSharedPreferences getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DropInSharedPreferences.class) {
                // double check that instance was not created in another thread
                if (INSTANCE == null) {
                    INSTANCE = new DropInSharedPreferences(context);
                }
            }
        }
        return INSTANCE;
    }

    private final PaymentMethodInspector paymentMethodInspector;
    private final BraintreeSharedPreferences braintreeSharedPreferences;

    private DropInSharedPreferences(Context context) {
        this(BraintreeSharedPreferences.getInstance(context), new PaymentMethodInspector());
    }

    @VisibleForTesting
    DropInSharedPreferences(
            BraintreeSharedPreferences braintreeSharedPreferences,
            PaymentMethodInspector paymentMethodInspector
    ) {
        this.braintreeSharedPreferences = braintreeSharedPreferences;
        this.paymentMethodInspector = paymentMethodInspector;
    }

    DropInPaymentMethod getLastUsedPaymentMethod() {
        String paymentMethodName =
            braintreeSharedPreferences.getString(LAST_USED_PAYMENT_METHOD, null);
        if (paymentMethodName != null) {
            try {
                return DropInPaymentMethod.valueOf(paymentMethodName);
            } catch (IllegalArgumentException e) {
                // no enums found for paymentMethodName
            }
        }
        return null;
    }

    void setLastUsedPaymentMethod(PaymentMethodNonce paymentMethodNonce) {
        DropInPaymentMethod paymentMethod =
            paymentMethodInspector.getPaymentMethod(paymentMethodNonce);

        if (paymentMethod != null) {
            String value = paymentMethod.name();
            braintreeSharedPreferences.putString(LAST_USED_PAYMENT_METHOD, value);
        }
    }
}
