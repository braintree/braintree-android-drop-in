package com.braintreepayments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import org.mockito.stubbing.Answer;

public class MockThreeDSecureClientBuilder {

    private Exception performVerificationError;
    private ThreeDSecureResult performVerificationSuccess;

    private ThreeDSecureResult continueVerificationSuccess;

    private Exception browserSwitchError;
    private ThreeDSecureResult browserSwitchResult;

    private Exception activityResultError;
    private ThreeDSecureResult activityResult;

    public MockThreeDSecureClientBuilder activityResultError(Exception activityResultError) {
        this.activityResultError = activityResultError;
        return this;
    }

    public MockThreeDSecureClientBuilder activityResult(ThreeDSecureResult activityResult) {
        this.activityResult = activityResult;
        return this;
    }

    public MockThreeDSecureClientBuilder browserSwitchError(Exception browserSwitchError) {
        this.browserSwitchError = browserSwitchError;
        return this;
    }

    public MockThreeDSecureClientBuilder browserSwitchResult(ThreeDSecureResult browserSwitchResult) {
        this.browserSwitchResult = browserSwitchResult;
        return this;
    }

    public MockThreeDSecureClientBuilder performVerificationError(Exception performVerificationError) {
        this.performVerificationError = performVerificationError;
        return this;
    }

    public MockThreeDSecureClientBuilder performVerificationSuccess(ThreeDSecureResult performVerificationSuccess) {
        this.performVerificationSuccess = performVerificationSuccess;
        return this;
    }

    public MockThreeDSecureClientBuilder continueVerificationSuccess(ThreeDSecureResult continueVerificationSuccess) {
        this.continueVerificationSuccess = continueVerificationSuccess;
        return this;
    }

    public ThreeDSecureClient build() {
        ThreeDSecureClient threeDSecureClient = mock(ThreeDSecureClient.class);

        doAnswer((Answer<Void>) invocation -> {
            ThreeDSecureResultCallback callback = (ThreeDSecureResultCallback) invocation.getArguments()[2];
            if (performVerificationSuccess != null) {
                callback.onResult(performVerificationSuccess, null);
            } else if (performVerificationError != null) {
                callback.onResult(null, performVerificationError);
            }
            return null;
        }).when(threeDSecureClient).performVerification(any(FragmentActivity.class), any(ThreeDSecureRequest.class), any(ThreeDSecureResultCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            ThreeDSecureResultCallback callback = (ThreeDSecureResultCallback) invocation.getArguments()[3];
            if (continueVerificationSuccess != null) {
                callback.onResult(continueVerificationSuccess, null);
            }
            return null;
        }).when(threeDSecureClient).continuePerformVerification(any(FragmentActivity.class), any(ThreeDSecureRequest.class), any(ThreeDSecureResult.class), any(ThreeDSecureResultCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            ThreeDSecureResultCallback callback = (ThreeDSecureResultCallback) invocation.getArguments()[1];
            if (browserSwitchResult != null) {
                callback.onResult(browserSwitchResult, null);
            } else if (browserSwitchError != null) {
                callback.onResult(null, browserSwitchError);
            }
            return null;
        }).when(threeDSecureClient).onBrowserSwitchResult(any(BrowserSwitchResult.class), any(ThreeDSecureResultCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            ThreeDSecureResultCallback callback = (ThreeDSecureResultCallback) invocation.getArguments()[2];
            if (activityResult != null) {
                callback.onResult(activityResult, null);
            } else if (activityResultError != null) {
                callback.onResult(null, activityResultError);
            }
            return null;
        }).when(threeDSecureClient).onActivityResult(anyInt(), any(Intent.class), any(ThreeDSecureResultCallback.class));

        return threeDSecureClient;
    }
}
