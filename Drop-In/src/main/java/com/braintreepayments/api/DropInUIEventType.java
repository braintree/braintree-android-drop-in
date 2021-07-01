package com.braintreepayments.api;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        DropInUIEventType.SHOW_VAULT_MANAGER,
        DropInUIEventType.DID_DISPLAY_SUPPORTED_PAYMENT_METHODS,
        DropInUIEventType.DISMISS_VAULT_MANAGER
})
@interface DropInUIEventType {
    int SHOW_VAULT_MANAGER = 0;
    int DID_DISPLAY_SUPPORTED_PAYMENT_METHODS = 1;
    int DISMISS_VAULT_MANAGER = 3;
}
