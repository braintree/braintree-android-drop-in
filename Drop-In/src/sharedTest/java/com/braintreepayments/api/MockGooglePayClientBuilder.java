package com.braintreepayments.api;

import androidx.fragment.app.FragmentActivity;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MockGooglePayClientBuilder {

    private Boolean isReadyToPaySuccess;
    private Exception isReadyToPayError;

    public MockGooglePayClientBuilder isReadyToPaySuccess(boolean isReadyToPay) {
        isReadyToPaySuccess = isReadyToPay;
        return this;
    }

    public MockGooglePayClientBuilder isReadyToPayError(Exception error) {
        isReadyToPayError = error;
        return this;
    }

    public GooglePayClient build() {
        GooglePayClient googlePayClient = mock(GooglePayClient.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                GooglePayIsReadyToPayCallback callback = (GooglePayIsReadyToPayCallback) invocation.getArguments()[1];
                if (isReadyToPaySuccess != null) {
                    callback.onResult(isReadyToPaySuccess, null);
                } else if (isReadyToPayError != null) {
                    callback.onResult(null, isReadyToPayError);
                }
                return null;
            }
        }).when(googlePayClient).isReadyToPay(any(FragmentActivity.class), any(GooglePayIsReadyToPayCallback.class));

        return googlePayClient;
    }
}
