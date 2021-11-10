package com.braintreepayments.api;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

class DropInSharedPreferences {

    private static final String LAST_USED_PAYMENT_METHOD_TYPE =
            "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD_TYPE";

    private static volatile DropInSharedPreferences INSTANCE;

    static DropInSharedPreferences getInstance() {
        if (INSTANCE == null) {
            synchronized (BraintreeSharedPreferences.class) {
                // double check that instance was not created in another thread
                if (INSTANCE == null) {
                    INSTANCE = new DropInSharedPreferences();
                }
            }
        }
        return INSTANCE;
    }

    private final PaymentMethodInspector paymentMethodInspector;

    private DropInSharedPreferences() {
        this(new PaymentMethodInspector());
    }

    @VisibleForTesting
    DropInSharedPreferences(PaymentMethodInspector paymentMethodInspector) {
        this.paymentMethodInspector = paymentMethodInspector;
    }

    DropInPaymentMethod getLastUsedPaymentMethod(Context context) {
        String paymentMethodName = BraintreeSharedPreferences.getInstance()
                .getString(context, LAST_USED_PAYMENT_METHOD_TYPE, null);
        if (paymentMethodName != null) {
            // TODO: catch exception when enum cannot be found with name
            return DropInPaymentMethod.valueOf(paymentMethodName);
        }
        return null;
    }

    void setLastUsedPaymentMethod(Context context, PaymentMethodNonce paymentMethodNonce) {
        String key = DropInResult.LAST_USED_PAYMENT_METHOD_TYPE;
        String value = paymentMethodInspector.getPaymentMethodType(paymentMethodNonce).name();
        BraintreeSharedPreferences.getInstance().putString(context, key, value);
    }
}
