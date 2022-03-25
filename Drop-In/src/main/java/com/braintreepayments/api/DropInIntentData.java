package com.braintreepayments.api;

public class DropInIntentData {

    private final Authorization authorization;
    private final DropInRequest dropInRequest;
    private final String sessionId;

    public DropInIntentData(DropInRequest dropInRequest, Authorization authorization, String sessionId) {
        this.sessionId = sessionId;
        this.dropInRequest = dropInRequest;
        this.authorization = authorization;
    }

    public DropInRequest getDropInRequest() {
        return dropInRequest;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public String getSessionId() {
        return sessionId;
    }
}
