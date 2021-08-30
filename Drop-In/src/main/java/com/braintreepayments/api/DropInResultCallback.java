package com.braintreepayments.api;

import androidx.annotation.Nullable;

public interface DropInResultCallback {
    /**
     * @param dropInResult {@link DropInResult}
     * @param error an exception that occurred while processing a DropInResult
     */
    void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error);
}
