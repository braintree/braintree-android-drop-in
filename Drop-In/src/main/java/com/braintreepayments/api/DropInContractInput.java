package com.braintreepayments.api;

public class DropInContractInput {

    private final DropInRequest dropInRequest;
    private final String sessionId;
    private final String authorization;

    public DropInContractInput(DropInRequest dropInRequest, String sessionId, String authorization) {
        this.dropInRequest = dropInRequest;
        this.sessionId = sessionId;
        this.authorization = authorization;
    }

    public DropInRequest getDropInRequest() {
        return dropInRequest;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAuthorization() {
        return authorization;
    }
}
