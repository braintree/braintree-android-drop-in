package com.braintreepayments.api;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        UIEventType.SHOW_VAULT_MANAGER
})
@interface UIEventType {
    int SHOW_VAULT_MANAGER = 0;
}
