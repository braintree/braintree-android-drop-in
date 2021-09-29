package com.braintreepayments.api;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

/**
 * Callback for receiving the result of {@link DropInClient#fetchMostRecentPaymentMethod(FragmentActivity, FetchMostRecentPaymentMethodCallback)}
 */
public interface FetchMostRecentPaymentMethodCallback {

    /**
     * @param dropInResult {@link DropInResult}
     * @param error an exception that occurred while fetching most recent payment method
     */
    void onResult(@Nullable DropInResult dropInResult, @Nullable Exception error);
}
