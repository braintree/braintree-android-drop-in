package com.braintreepayments.api;

import androidx.annotation.Nullable;

import java.util.List;

interface GetSupportedPaymentMethodsCallback {
    void onResult(@Nullable List<PaymentMethodType> paymentMethods, @Nullable Exception error);
}
