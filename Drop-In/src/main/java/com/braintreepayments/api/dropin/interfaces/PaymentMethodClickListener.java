package com.braintreepayments.api.dropin.interfaces;

public interface PaymentMethodClickListener {
    void onCardClick();
    void onPayPalClick();
    void onAndroidPayClick();
    void onVenmoClick();
}
