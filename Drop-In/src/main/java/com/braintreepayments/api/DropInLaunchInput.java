package com.braintreepayments.api;

class DropInLaunchInput {

    private final Authorization authorization;
    private final DropInRequest dropInRequest;

    DropInLaunchInput(DropInRequest dropInRequest, Authorization authorization) {
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
