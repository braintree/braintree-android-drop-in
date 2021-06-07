package com.braintreepayments.api;

import androidx.annotation.Nullable;

public interface FetchMostRecentPaymentMethodCallback {

    void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error);
}
