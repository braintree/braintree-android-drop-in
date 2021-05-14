package com.braintreepayments.api;

import com.braintreepayments.api.models.PaymentMethodNonce;

interface VaultedPaymentMethodSelectedListener {
    void onVaultedPaymentMethodSelected(PaymentMethodNonce paymentMethodNonce);
}
