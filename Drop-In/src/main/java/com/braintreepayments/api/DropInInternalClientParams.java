package com.braintreepayments.api;

class DropInInternalClientParams {

    private DropInRequest dropInRequest;

    private BraintreeClient braintreeClient;
    private GooglePayClient googlePayClient;
    private PaymentMethodClient paymentMethodClient;
    private PayPalClient payPalClient;
    private VenmoClient venmoClient;
    private CardClient cardClient;
    private UnionPayClient unionPayClient;
    private DataCollector dataCollector;
    private ThreeDSecureClient threeDSecureClient;
    private DropInSharedPreferences dropInSharedPreferences;

    ThreeDSecureClient getThreeDSecureClient() {
        return threeDSecureClient;
    }

    DropInInternalClientParams threeDSecureClient(ThreeDSecureClient threeDSecureClient) {
        this.threeDSecureClient = threeDSecureClient;
        return this;
    }

    DataCollector getDataCollector() {
        return dataCollector;
    }

    DropInInternalClientParams dataCollector(DataCollector dataCollector) {
        this.dataCollector = dataCollector;
        return this;
    }

    DropInRequest getDropInRequest() {
        return dropInRequest;
    }

    DropInInternalClientParams dropInRequest(DropInRequest dropInRequest) {
        this.dropInRequest = dropInRequest;
        return this;
    }

    BraintreeClient getBraintreeClient() {
        return braintreeClient;
    }

    DropInInternalClientParams braintreeClient(BraintreeClient braintreeClient) {
        this.braintreeClient = braintreeClient;
        return this;
    }

    GooglePayClient getGooglePayClient() {
        return googlePayClient;
    }

    DropInInternalClientParams googlePayClient(GooglePayClient googlePayClient) {
        this.googlePayClient = googlePayClient;
        return this;
    }

    PaymentMethodClient getPaymentMethodClient() {
        return paymentMethodClient;
    }

    DropInInternalClientParams paymentMethodClient(PaymentMethodClient paymentMethodClient) {
        this.paymentMethodClient = paymentMethodClient;
        return this;
    }

    PayPalClient getPayPalClient() {
        return payPalClient;
    }

    DropInInternalClientParams payPalClient(PayPalClient payPalClient) {
        this.payPalClient = payPalClient;
        return this;
    }

    VenmoClient getVenmoClient() {
        return venmoClient;
    }

    DropInInternalClientParams venmoClient(VenmoClient venmoClient) {
        this.venmoClient = venmoClient;
        return this;
    }

    CardClient getCardClient() {
        return cardClient;
    }

    DropInInternalClientParams cardClient(CardClient cardClient) {
        this.cardClient = cardClient;
        return this;
    }

    UnionPayClient getUnionPayClient() {
        return unionPayClient;
    }

    DropInInternalClientParams unionPayClient(UnionPayClient unionPayClient) {
        this.unionPayClient = unionPayClient;
        return this;
    }

    DropInInternalClientParams dropInSharedPreferences(DropInSharedPreferences dropInSharedPreferences) {
        this.dropInSharedPreferences = dropInSharedPreferences;
        return this;
    }

    DropInSharedPreferences getDropInSharedPreferences() {
        return dropInSharedPreferences;
    }
}
