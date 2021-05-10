package com.braintreepayments.api;

import android.content.Context;

public class DropInClient {

    private BraintreeClient braintreeClient;

    public DropInClient(Context context, String authorization) {
        this.braintreeClient = new BraintreeClient(context, authorization);
    }


    public void fetchMostRecentPaymentMethod(FetchMostRecentPaymentMethodCallback callback) {
        // TODO: send back empty result if tokenization key auth

    }
}
