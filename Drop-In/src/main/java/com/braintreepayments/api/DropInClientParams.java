package com.braintreepayments.api;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

class DropInClientParams {

    private DropInRequest dropInRequest;

    private BraintreeClient braintreeClient;
    private GooglePayClient googlePayClient;
    private PaymentMethodClient paymentMethodClient;
    private DropInSharedPreferences dropInSharedPreferences;
    private FragmentActivity activity;
    private Lifecycle lifecycle;

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

    DropInClientParams dropInSharedPreferences(DropInSharedPreferences dropInSharedPreferences) {
        this.dropInSharedPreferences = dropInSharedPreferences;
        return this;
    }

    DropInSharedPreferences getDropInSharedPreferences() {
        return dropInSharedPreferences;
    }

    DropInClientParams activity(FragmentActivity activity) {
        this.activity = activity;
        return this;
    }

    FragmentActivity getActivity() {
        return activity;
    }

    DropInClientParams lifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
        return this;
    }

    Lifecycle getLifecycle() {
        return lifecycle;
    }
}
