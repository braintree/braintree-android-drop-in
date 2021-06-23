package com.braintreepayments.api;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        SupportedPaymentMethodType.CARD,
        SupportedPaymentMethodType.PAYPAL,
        SupportedPaymentMethodType.VENMO,
        SupportedPaymentMethodType.GOOGLE_PAY,
})
@interface SupportedPaymentMethodType {
    int CARD = 0;
    int PAYPAL = 1;
    int VENMO = 2;
    int GOOGLE_PAY = 3;
}
