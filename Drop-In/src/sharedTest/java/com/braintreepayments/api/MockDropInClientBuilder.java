package com.braintreepayments.api;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockDropInClientBuilder {

    private DropInResult threeDSecureSuccess;
    private Exception threeDSecureError;
    private List<PaymentMethodNonce> paymentMethodNonceListSuccess;
    private Exception getVaultedPaymentMethodsError;
    private Authorization authorization;
    private Configuration configuration;
    private List<Integer> supportedPaymentMethods;
    private String deviceDataSuccess;
    private PaymentMethodNonce deletedNonce;
    private Exception deletePaymentMethodNonceError;
    private CardNonce cardTokenizeSuccess;
    private Exception cardTokenizeError;
    private UnionPayCapabilities unionPayCapabilitiesSuccess;
    private Exception unionPayCapabilitiesError;
    private UnionPayEnrollment enrollUnionPaySuccess;
    private Exception enrollUnionPayError;
    private CardNonce unionPayTokenizeSuccess;
    private Exception unionPayTokenizeError;
    private Exception handleThreeDSecureActivityResultError;

    private boolean shouldPerformThreeDSecureVerification;

    MockDropInClientBuilder shouldPerformThreeDSecureVerification(boolean shouldPerformThreeDSecureVerification) {
        this.shouldPerformThreeDSecureVerification = shouldPerformThreeDSecureVerification;
        return this;
    }

    MockDropInClientBuilder dropInResultCallback(DropInResult dropInResult) {
        this.threeDSecureSuccess = dropInResult;
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

    MockDropInClientBuilder getSupportedPaymentMethodsSuccess(List<Integer> supportedPaymentMethods) {
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

    MockDropInClientBuilder cardTokenizeSuccess(CardNonce cardNonce) {
        this.cardTokenizeSuccess = cardNonce;
        return this;
    }

    MockDropInClientBuilder cardTokenizeError(Exception error) {
        this.cardTokenizeError = error;
        return this;
    }

    MockDropInClientBuilder unionPayCapabilitiesSuccess(UnionPayCapabilities capabilities) {
        this.unionPayCapabilitiesSuccess = capabilities;
        return this;
    }

    MockDropInClientBuilder unionPayCapabilitiesError(Exception error) {
        this.unionPayCapabilitiesError = error;
        return this;
    }

    MockDropInClientBuilder enrollUnionPaySuccess(UnionPayEnrollment enrollment) {
        this.enrollUnionPaySuccess = enrollment;
        return this;
    }

    MockDropInClientBuilder enrollUnionPayError(Exception error) {
        this.enrollUnionPayError = error;
        return this;
    }

    MockDropInClientBuilder unionPayTokenizeSuccess(CardNonce cardNonce) {
        this.unionPayTokenizeSuccess = cardNonce;
        return this;
    }

    MockDropInClientBuilder unionPayTokenizeError(Exception error) {
        this.unionPayTokenizeError = error;
        return this;
    }

    MockDropInClientBuilder handleThreeDSecureActivityResultError(Exception error) {
        this.handleThreeDSecureActivityResultError = error;
        return this;
    }

    DropInClient build() {
        DropInClient dropInClient = mock(DropInClient.class);
        when(dropInClient.getAuthorization()).thenReturn(authorization);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                DropInResultCallback callback = (DropInResultCallback) invocation.getArguments()[2];
                if (threeDSecureSuccess != null) {
                    callback.onResult(threeDSecureSuccess, null);
                } else if (threeDSecureError != null) {
                    callback.onResult(null, threeDSecureError);
                }
                return null;
            }
        }).when(dropInClient).performThreeDSecureVerification(any(FragmentActivity.class), any(PaymentMethodNonce.class), any(DropInResultCallback.class));

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

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                CardTokenizeCallback callback = (CardTokenizeCallback) invocation.getArguments()[1];
                if (cardTokenizeSuccess != null) {
                    callback.onResult(cardTokenizeSuccess, null);
                } else if (cardTokenizeError != null) {
                    callback.onResult(null, cardTokenizeError);
                }
                return null;
            }
        }).when(dropInClient).tokenizeCard(any(Card.class), any(CardTokenizeCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                UnionPayFetchCapabilitiesCallback callback = (UnionPayFetchCapabilitiesCallback) invocation.getArguments()[1];
                if (unionPayCapabilitiesSuccess != null) {
                    callback.onResult(unionPayCapabilitiesSuccess, null);
                } else if (unionPayCapabilitiesError != null) {
                    callback.onResult(null, unionPayCapabilitiesError);
                }
                return null;
            }
        }).when(dropInClient).fetchUnionPayCapabilities(any(String.class), any(UnionPayFetchCapabilitiesCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                UnionPayEnrollCallback callback = (UnionPayEnrollCallback) invocation.getArguments()[1];
                if (enrollUnionPaySuccess != null) {
                    callback.onResult(enrollUnionPaySuccess, null);
                } else if (enrollUnionPayError != null) {
                    callback.onResult(null, enrollUnionPayError);
                }
                return null;
            }
        }).when(dropInClient).enrollUnionPay(any(UnionPayCard.class), any(UnionPayEnrollCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                UnionPayTokenizeCallback callback = (UnionPayTokenizeCallback) invocation.getArguments()[1];
                if (unionPayTokenizeSuccess != null) {
                    callback.onResult(unionPayTokenizeSuccess, null);
                } else if (unionPayTokenizeError != null) {
                    callback.onResult(null, unionPayTokenizeError);
                }
                return null;
            }
        }).when(dropInClient).tokenizeUnionPay(any(UnionPayCard.class), any(UnionPayTokenizeCallback.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                DropInResultCallback callback = (DropInResultCallback) invocation.getArguments()[3];
                if (handleThreeDSecureActivityResultError != null) {
                    callback.onResult(null, handleThreeDSecureActivityResultError);
                }
                return null;
            }
        }).when(dropInClient).handleThreeDSecureActivityResult(any(FragmentActivity.class), anyInt(), any(Intent.class), any(DropInResultCallback.class));

        return dropInClient;
    }
}
