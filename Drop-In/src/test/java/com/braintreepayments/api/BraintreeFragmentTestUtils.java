package com.braintreepayments.api;

import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.internal.BraintreeGraphQLHttpClient;
import com.braintreepayments.api.internal.BraintreeHttpClient;

public class BraintreeFragmentTestUtils {

    public static void setHttpClient(BraintreeFragment fragment, BraintreeHttpClient httpClient) {
        fragment.mHttpClient = httpClient;
    }

    public static void setGraphQlClient(BraintreeFragment fragment, BraintreeGraphQLHttpClient graphQLHttpClient) {
        fragment.mGraphQLHttpClient = graphQLHttpClient;
    }

    public static void waitForConfiguration(BraintreeFragment fragment,
                                            ConfigurationListener listener) {
        fragment.waitForConfiguration(listener);
    }
}
