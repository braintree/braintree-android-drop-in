package com.braintreepayments.api;

import androidx.annotation.NonNull;

public interface DropInListener {
    void onDropInSuccess(@NonNull DropInResult dropInResult);
    void onDropInFailure(@NonNull Exception error);

    void onThreeDSecureVerificationFailure(@NonNull Exception error);
    void onPayPalTokenizeError(@NonNull Exception error);
    void onVenmoTokenizeError(@NonNull Exception error);
    void onGooglePayTokenizeError(@NonNull Exception error);
}
