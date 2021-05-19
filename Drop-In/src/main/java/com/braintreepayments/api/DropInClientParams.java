package com.braintreepayments.api;

class DropInClientParams {

    private DropInRequest dropInRequest;

    private BraintreeClient braintreeClient;
    private GooglePayClient googlePayClient;

    public DropInRequest getDropInRequest() {
        return dropInRequest;
    }

    public DropInClientParams dropInRequest(DropInRequest dropInRequest) {
        this.dropInRequest = dropInRequest;
        return this;
    }

    public BraintreeClient getBraintreeClient() {
        return braintreeClient;
    }

    public DropInClientParams braintreeClient(BraintreeClient braintreeClient) {
        this.braintreeClient = braintreeClient;
        return this;
    }

    public GooglePayClient getGooglePayClient() {
        return googlePayClient;
    }

    public DropInClientParams googlePayClient(GooglePayClient googlePayClient) {
        this.googlePayClient = googlePayClient;
        return this;
    }
}
