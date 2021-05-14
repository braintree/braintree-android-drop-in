package com.braintreepayments.api;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        NavigationEvent.SHOW_VAULT_MANAGER
})
@interface NavigationEvent {
    String SHOW_VAULT_MANAGER = "SHOW_VAULT_MANAGER";
}
