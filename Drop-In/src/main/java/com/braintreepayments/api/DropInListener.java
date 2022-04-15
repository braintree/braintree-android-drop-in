package com.braintreepayments.api;

import androidx.annotation.NonNull;

/**
 * Implement this interface to receive results from the DropIn payment flow.
 */
public interface DropInListener {

    /**
     * Called when a {@link DropInResult} is created without error.
     * @param dropInResult a {@link DropInResult}
     */
    void onDropInSuccess(@NonNull DropInResult dropInResult);

    /**
     * Called when DropIn has finished with an error.
     * @param error explains reason for DropIn failure.
     */
    void onDropInFailure(@NonNull Exception error);
}
