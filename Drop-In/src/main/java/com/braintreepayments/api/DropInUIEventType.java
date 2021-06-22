package com.braintreepayments.api;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        DropInUIEventType.SHOW_ADD_CARD,
        DropInUIEventType.SHOW_VAULT_MANAGER,
        DropInUIEventType.DID_DISPLAY_SUPPORTED_PAYMENT_METHODS,
        DropInUIEventType.SHOW_GOOGLE_PAYMENT,
        DropInUIEventType.SHOW_PAYPAL,
        DropInUIEventType.SHOW_PAY_WITH_VENMO
})
@interface DropInUIEventType {
    int SHOW_VAULT_MANAGER = 0;
    int SHOW_ADD_CARD = 1;
    int DID_DISPLAY_SUPPORTED_PAYMENT_METHODS = 2;
    int SHOW_GOOGLE_PAYMENT = 3;
    int SHOW_PAYPAL = 4;
    int SHOW_PAY_WITH_VENMO = 5;
}
