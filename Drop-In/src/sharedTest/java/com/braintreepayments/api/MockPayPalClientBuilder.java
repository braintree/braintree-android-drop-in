package com.braintreepayments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import org.mockito.stubbing.Answer;

public class MockPayPalClientBuilder {

    private Exception browserSwitchError;
    private PayPalAccountNonce browserSwitchResult;

    public MockPayPalClientBuilder browserSwitchError(Exception browserSwitchError) {
        this.browserSwitchError = browserSwitchError;
        return this;
    }

    public MockPayPalClientBuilder browserSwitchResult(PayPalAccountNonce browserSwitchResult) {
        this.browserSwitchResult = browserSwitchResult;
        return this;
    }

    PayPalClient build() {
        PayPalClient payPalClient = mock(PayPalClient.class);

        doAnswer((Answer<Void>) invocation -> {
            PayPalBrowserSwitchResultCallback callback = (PayPalBrowserSwitchResultCallback) invocation.getArguments()[1];
            if (browserSwitchResult != null) {
                callback.onResult(browserSwitchResult, null);
            } else if (browserSwitchError != null) {
                callback.onResult(null, browserSwitchError);
            }
            return null;
        }).when(payPalClient).onBrowserSwitchResult(any(BrowserSwitchResult.class), any(PayPalBrowserSwitchResultCallback.class));

        return payPalClient;
    }
}
