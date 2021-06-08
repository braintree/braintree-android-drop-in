package com.braintreepayments.api;

import androidx.annotation.Nullable;

public interface DropInResultCallback {
    void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error);
}
