package com.braintreepayments.api;

public interface VaultedPaymentMethodSelectedCallback {
    void onResult(PaymentMethodNonce paymentMethodNonce, Exception error);
}
