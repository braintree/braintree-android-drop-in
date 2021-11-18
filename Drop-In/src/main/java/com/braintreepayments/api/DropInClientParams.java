package com.braintreepayments.api;

class DropInClientParams {

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

    DropInClientParams threeDSecureClient(ThreeDSecureClient threeDSecureClient) {
        this.threeDSecureClient = threeDSecureClient;
        return this;
    }

    DataCollector getDataCollector() {
        return dataCollector;
    }

    DropInClientParams dataCollector(DataCollector dataCollector) {
        this.dataCollector = dataCollector;
        return this;
    }

    DropInRequest getDropInRequest() {
        return dropInRequest;
    }

    DropInClientParams dropInRequest(DropInRequest dropInRequest) {
        this.dropInRequest = dropInRequest;
        return this;
    }

    BraintreeClient getBraintreeClient() {
        return braintreeClient;
    }

    DropInClientParams braintreeClient(BraintreeClient braintreeClient) {
        this.braintreeClient = braintreeClient;
        return this;
    }

    GooglePayClient getGooglePayClient() {
        return googlePayClient;
    }

    DropInClientParams googlePayClient(GooglePayClient googlePayClient) {
        this.googlePayClient = googlePayClient;
        return this;
    }

    PaymentMethodClient getPaymentMethodClient() {
        return paymentMethodClient;
    }

    DropInClientParams paymentMethodClient(PaymentMethodClient paymentMethodClient) {
        this.paymentMethodClient = paymentMethodClient;
        return this;
    }

    PayPalClient getPayPalClient() {
        return payPalClient;
    }

    DropInClientParams payPalClient(PayPalClient payPalClient) {
        this.payPalClient = payPalClient;
        return this;
    }

    VenmoClient getVenmoClient() {
        return venmoClient;
    }

    DropInClientParams venmoClient(VenmoClient venmoClient) {
        this.venmoClient = venmoClient;
        return this;
    }

    CardClient getCardClient() {
        return cardClient;
    }

    DropInClientParams cardClient(CardClient cardClient) {
        this.cardClient = cardClient;
        return this;
    }

    UnionPayClient getUnionPayClient() {
        return unionPayClient;
    }

    DropInClientParams unionPayClient(UnionPayClient unionPayClient) {
        this.unionPayClient = unionPayClient;
        return this;
    }

    DropInClientParams dropInSharedPreferences(DropInSharedPreferences dropInSharedPreferences) {
        this.dropInSharedPreferences = dropInSharedPreferences;
        return this;
    }

    DropInSharedPreferences getDropInSharedPreferences() {
        return dropInSharedPreferences;
    }
}
