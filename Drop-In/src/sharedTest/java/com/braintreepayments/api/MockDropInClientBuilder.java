package com.braintreepayments.api;

import androidx.fragment.app.FragmentActivity;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockDropInClientBuilder {

    private ThreeDSecureResult threeDSecureSuccess;
    private Exception threeDSecureError;
    private List<PaymentMethodNonce> paymentMethodNonceListSuccess;
    private Exception getVaultedPaymentMethodsError;
    private Authorization authorization;
    private Configuration configuration;
    private List<DropInPaymentMethodType> supportedPaymentMethods;
    private String deviceDataSuccess;
    private PaymentMethodNonce deletedNonce;
    private Exception deletePaymentMethodNonceError;

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

    MockDropInClientBuilder getVaultedPaymentMethodsSuccess(List<PaymentMethodNonce> nonces) {
        this.paymentMethodNonceListSuccess = nonces;
        return this;
    }

    MockDropInClientBuilder getVaultedPaymentMethodsError(Exception error) {
        this.getVaultedPaymentMethodsError = error;
        return this;
    }

    MockDropInClientBuilder authorization(Authorization authorization) {
        this.authorization = authorization;
        return this;
    }

    MockDropInClientBuilder getConfigurationSuccess(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    MockDropInClientBuilder getSupportedPaymentMethodsSuccess(List<DropInPaymentMethodType> supportedPaymentMethods) {
        this.supportedPaymentMethods = supportedPaymentMethods;
        return this;
    }

    MockDropInClientBuilder collectDeviceDataSuccess(String deviceDataSuccess) {
        this.deviceDataSuccess = deviceDataSuccess;
        return this;
    }

    MockDropInClientBuilder deletePaymentMethodSuccess(PaymentMethodNonce deletedNonce) {
        this.deletedNonce = deletedNonce;
        return this;
    }

    DropInClient build() {
        DropInClient dropInClient = mock(DropInClient.class);
        when(dropInClient.getAuthorization()).thenReturn(authorization);

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

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                GetPaymentMethodNoncesCallback callback = (GetPaymentMethodNoncesCallback) invocation.getArguments()[2];
                if (paymentMethodNonceListSuccess != null) {
                    callback.onResult(paymentMethodNonceListSuccess, null);
                } else if (getVaultedPaymentMethodsError != null) {
                    callback.onResult(null, getVaultedPaymentMethodsError);
                }
                return null;
            }
        }).when(dropInClient).getVaultedPaymentMethods(any(FragmentActivity.class), anyBoolean(), any(GetPaymentMethodNoncesCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                ConfigurationCallback callback = (ConfigurationCallback) invocation.getArguments()[0];
                if (configuration != null) {
                    callback.onResult(configuration, null);
                }
                return null;
            }
        }).when(dropInClient).getConfiguration(any(ConfigurationCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                GetSupportedPaymentMethodsCallback callback = (GetSupportedPaymentMethodsCallback) invocation.getArguments()[1];
                if (supportedPaymentMethods != null) {
                    callback.onResult(supportedPaymentMethods, null);
                }
                return null;
            }
        }).when(dropInClient).getSupportedPaymentMethods(any(FragmentActivity.class), any(GetSupportedPaymentMethodsCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                DataCollectorCallback callback = (DataCollectorCallback) invocation.getArguments()[1];
                if (deviceDataSuccess != null) {
                    callback.onResult(deviceDataSuccess, null);
                }
                return null;
            }
        }).when(dropInClient).collectDeviceData(any(FragmentActivity.class), any(DataCollectorCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                DeletePaymentMethodNonceCallback callback = (DeletePaymentMethodNonceCallback) invocation.getArguments()[2];
                if (deletedNonce != null) {
                    callback.onResult(deletedNonce, null);
                }
                return null;
            }
        }).when(dropInClient).deletePaymentMethod(any(FragmentActivity.class), any(PaymentMethodNonce.class), any(DeletePaymentMethodNonceCallback.class));

        return dropInClient;
    }
}
