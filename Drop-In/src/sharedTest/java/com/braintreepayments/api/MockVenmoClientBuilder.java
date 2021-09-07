package com.braintreepayments.api;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MockVenmoClientBuilder {

    private VenmoAccountNonce onActivityResultSuccess;
    private Exception onActivityResultError;

    public MockVenmoClientBuilder onActivityResultSuccess(VenmoAccountNonce venmoAccountNonce) {
        onActivityResultSuccess = venmoAccountNonce;
        return this;
    }

    public MockVenmoClientBuilder onActivityResultError(Exception error) {
        onActivityResultError = error;
        return this;
    }

    public VenmoClient build() {
        VenmoClient venmoClient = mock(VenmoClient.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                VenmoOnActivityResultCallback callback = (VenmoOnActivityResultCallback) invocation.getArguments()[3];
                if (onActivityResultSuccess != null) {
                    callback.onResult(onActivityResultSuccess, null);
                } else if (onActivityResultError != null) {
                    callback.onResult(null, onActivityResultError);
                }
                return null;
            }
        }).when(venmoClient).onActivityResult(any(FragmentActivity.class), anyInt(), any(Intent.class), any(VenmoOnActivityResultCallback.class));
        return venmoClient;
    }
}
