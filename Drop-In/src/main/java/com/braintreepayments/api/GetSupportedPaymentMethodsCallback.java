package com.braintreepayments.api;

import androidx.annotation.Nullable;

import java.util.List;

interface GetSupportedPaymentMethodsCallback {
    void onResult(@Nullable List<DropInPaymentMethod> paymentMethods, @Nullable Exception error);
}
