package com.braintreepayments.api;

public class DropInLaunchIntent {

    private final Authorization authorization;
    private final DropInRequest dropInRequest;
    private final String sessionId;

    DropInLaunchIntent(DropInRequest dropInRequest, Authorization authorization, String sessionId) {
        this.sessionId = sessionId;
        this.dropInRequest = dropInRequest;
        this.authorization = authorization;
    }

    DropInRequest getDropInRequest() {
        return dropInRequest;
    }

    Authorization getAuthorization() {
        return authorization;
    }

    String getSessionId() {
        return sessionId;
    }
}
