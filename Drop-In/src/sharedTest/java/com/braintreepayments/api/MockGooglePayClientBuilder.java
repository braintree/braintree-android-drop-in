package com.braintreepayments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import org.mockito.stubbing.Answer;

public class MockGooglePayClientBuilder {

    private Boolean isReadyToPaySuccess;
    private Exception isReadyToPayError;
    private PaymentMethodNonce onActivityResultSuccess;
    private Exception onActivityResultError;

    public MockGooglePayClientBuilder isReadyToPaySuccess(boolean isReadyToPay) {
        isReadyToPaySuccess = isReadyToPay;
        return this;
    }

    public MockGooglePayClientBuilder isReadyToPayError(Exception error) {
        isReadyToPayError = error;
        return this;
    }

    public MockGooglePayClientBuilder onActivityResultSuccess(PaymentMethodNonce paymentMethodNonce) {
        onActivityResultSuccess = paymentMethodNonce;
        return this;
    }

    public MockGooglePayClientBuilder onActivityResultError(Exception error) {
        onActivityResultError = error;
        return this;
    }

    public GooglePayClient build() {
        GooglePayClient googlePayClient = mock(GooglePayClient.class);

        doAnswer((Answer<Void>) invocation -> {
            GooglePayIsReadyToPayCallback callback = (GooglePayIsReadyToPayCallback) invocation.getArguments()[1];
            if (isReadyToPaySuccess != null) {
                callback.onResult(isReadyToPaySuccess, null);
            } else if (isReadyToPayError != null) {
                callback.onResult(false, isReadyToPayError);
            }
            return null;
        }).when(googlePayClient).isReadyToPay(any(FragmentActivity.class), any(GooglePayIsReadyToPayCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            GooglePayOnActivityResultCallback callback = (GooglePayOnActivityResultCallback) invocation.getArguments()[2];
            if (onActivityResultSuccess != null) {
                callback.onResult(onActivityResultSuccess, null);
            } else if (onActivityResultError != null) {
                callback.onResult(null, onActivityResultError);
            }
            return null;
        }).when(googlePayClient).onActivityResult(anyInt(), any(Intent.class), any(GooglePayOnActivityResultCallback.class));
        return googlePayClient;
    }
}
