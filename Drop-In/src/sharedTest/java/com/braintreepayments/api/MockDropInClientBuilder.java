package com.braintreepayments.api;

import androidx.fragment.app.FragmentActivity;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MockDropInClientBuilder {

    private ThreeDSecureResult threeDSecureSuccess;
    private Exception threeDSecureError;

    private boolean shouldPerformThreeDSecureVerification;

    MockDropInClientBuilder shouldPerformThreeDSecureVerification(boolean shouldPerformThreeDSecureVerification) {
        this.shouldPerformThreeDSecureVerification = shouldPerformThreeDSecureVerification;
        return this;
    }

    MockDropInClientBuilder threeDSecureSuccess(ThreeDSecureResult result) {
        threeDSecureSuccess = result;
        return this;
    }

    MockDropInClientBuilder threeDSecureError(Exception error) {
        threeDSecureError = error;
        return this;
    }

    DropInClient build() {
        DropInClient dropInClient = mock(DropInClient.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                ThreeDSecureResultCallback callback = (ThreeDSecureResultCallback) invocation.getArguments()[2];
                if (threeDSecureSuccess != null) {
                    callback.onResult(threeDSecureSuccess, null);
                } else if (threeDSecureError != null) {
                    callback.onResult(null, threeDSecureError);
                }
                return null;
            }
        }).when(dropInClient).performThreeDSecureVerification(any(FragmentActivity.class), any(PaymentMethodNonce.class), any(ThreeDSecureResultCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                ShouldRequestThreeDSecureVerification callback = (ShouldRequestThreeDSecureVerification) invocation.getArguments()[1];
                callback.onResult(shouldPerformThreeDSecureVerification);
                return null;
            }
        }).when(dropInClient).shouldRequestThreeDSecureVerification(any(PaymentMethodNonce.class), any(ShouldRequestThreeDSecureVerification.class));

        return dropInClient;
    }
}
