package com.braintreepayments.api;

import androidx.fragment.app.FragmentActivity;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MockThreeDSecureClientBuilder {

    private Exception performVerificationError;
    private ThreeDSecureResult performVerificationSuccess;

    private ThreeDSecureResult continueVerificationSuccess;

    private Exception browserSwitchError;
    private ThreeDSecureResult browserSwitchResult;

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

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                ThreeDSecureResultCallback callback = (ThreeDSecureResultCallback) invocation.getArguments()[2];
                if (performVerificationSuccess != null) {
                    callback.onResult(performVerificationSuccess, null);
                } else if (performVerificationError != null) {
                    callback.onResult(null, performVerificationError);
                }
                return null;
            }
        }).when(threeDSecureClient).performVerification(any(FragmentActivity.class), any(ThreeDSecureRequest.class), any(ThreeDSecureResultCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                ThreeDSecureResultCallback callback = (ThreeDSecureResultCallback) invocation.getArguments()[3];
                if (continueVerificationSuccess != null) {
                    callback.onResult(continueVerificationSuccess, null);
                }
                return null;
            }
        }).when(threeDSecureClient).continuePerformVerification(any(FragmentActivity.class), any(ThreeDSecureRequest.class), any(ThreeDSecureResult.class), any(ThreeDSecureResultCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                ThreeDSecureResultCallback callback = (ThreeDSecureResultCallback) invocation.getArguments()[1];
                if (browserSwitchResult != null) {
                    callback.onResult(browserSwitchResult, null);
                } else if (browserSwitchError != null) {
                    callback.onResult(null, browserSwitchError);
                }
                return null;
            }
        }).when(threeDSecureClient).onBrowserSwitchResult(any(BrowserSwitchResult.class), any(ThreeDSecureResultCallback.class));

        return threeDSecureClient;
    }
}
