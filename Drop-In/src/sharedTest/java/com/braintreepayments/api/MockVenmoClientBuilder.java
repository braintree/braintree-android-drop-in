package com.braintreepayments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import org.mockito.stubbing.Answer;

public class MockVenmoClientBuilder {

    private VenmoAccountNonce onActivityResultSuccess;
    private Exception onActivityResultError;
    private boolean isVenmoAppInstalled;

    public MockVenmoClientBuilder onActivityResultSuccess(VenmoAccountNonce venmoAccountNonce) {
        onActivityResultSuccess = venmoAccountNonce;
        return this;
    }

    public MockVenmoClientBuilder onActivityResultError(Exception error) {
        onActivityResultError = error;
        return this;
    }

    public MockVenmoClientBuilder isVenmoAppInstalled(boolean isVenmoAppInstalled) {
        this.isVenmoAppInstalled = isVenmoAppInstalled;
        return this;
    }

    public VenmoClient build() {
        VenmoClient venmoClient = mock(VenmoClient.class);
        when(venmoClient.isVenmoAppSwitchAvailable(any(Context.class))).thenReturn(isVenmoAppInstalled);

        doAnswer((Answer<Void>) invocation -> {
            VenmoOnActivityResultCallback callback = (VenmoOnActivityResultCallback) invocation.getArguments()[3];
            if (onActivityResultSuccess != null) {
                callback.onResult(onActivityResultSuccess, null);
            } else if (onActivityResultError != null) {
                callback.onResult(null, onActivityResultError);
            }
            return null;
        }).when(venmoClient).onActivityResult(any(FragmentActivity.class), anyInt(), any(Intent.class), any(VenmoOnActivityResultCallback.class));
        return venmoClient;
    }
}
