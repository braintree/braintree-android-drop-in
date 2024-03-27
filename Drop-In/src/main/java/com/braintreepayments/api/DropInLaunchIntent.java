package com.braintreepayments.api;

public class DropInLaunchIntent {

    private final Authorization authorization;
    private final DropInRequest dropInRequest;

    DropInLaunchIntent(DropInRequest dropInRequest, Authorization authorization) {
        this.dropInRequest = dropInRequest;
        this.authorization = authorization;
    }

    DropInRequest getDropInRequest() {
        return dropInRequest;
    }

    Authorization getAuthorization() {
        return authorization;
    }
}
