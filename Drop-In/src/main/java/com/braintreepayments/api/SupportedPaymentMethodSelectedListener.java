package com.braintreepayments.api;

interface SupportedPaymentMethodSelectedListener {
    void onPaymentMethodSelected(@SupportedPaymentMethodType int type);
}
