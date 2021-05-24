package com.braintreepayments.api;

class DropInClientParams {

    private DropInRequest dropInRequest;

    private BraintreeClient braintreeClient;
    private GooglePayClient googlePayClient;
    private PaymentMethodClient paymentMethodClient;
    private PayPalClient payPalClient;
    private VenmoClient venmoClient;

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

    public PaymentMethodClient getPaymentMethodClient() {
        return paymentMethodClient;
    }

    public DropInClientParams paymentMethodClient(PaymentMethodClient paymentMethodClient) {
        this.paymentMethodClient = paymentMethodClient;
        return this;
    }

    public PayPalClient getPayPalClient() {
        return payPalClient;
    }

    public DropInClientParams payPalClient(PayPalClient payPalClient) {
        this.payPalClient = payPalClient;
        return this;
    }

    public VenmoClient getVenmoClient() {
        return venmoClient;
    }

    public DropInClientParams venmoClient(VenmoClient venmoClient) {
        this.venmoClient = venmoClient;
        return this;
    }
}
