package com.braintreepayments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.cardform.utils.CardType;

import org.mockito.stubbing.Answer;

import java.util.List;

public class MockDropInInternalClientBuilder {

    private DropInResult threeDSecureSuccess;
    private Exception threeDSecureError;
    private List<PaymentMethodNonce> paymentMethodNonceListSuccess;
    private Exception getVaultedPaymentMethodsError;
    private Authorization authorization;
    private Configuration configuration;
    private List<DropInPaymentMethod> supportedPaymentMethods;
    private Exception getSupportedPaymentMethodsError;
    private String deviceDataSuccess;
    private PaymentMethodNonce deletedNonce;
    private Exception deletePaymentMethodNonceError;
    private CardNonce cardTokenizeSuccess;
    private Exception cardTokenizeError;
    private Exception payPalError;
    private Exception googlePayError;
    private Exception venmoError;
    private Exception handleThreeDSecureActivityResultError;
    private DropInResult handleThreeDSecureActivityResultSuccess;
    private Exception deliverBrowserSwitchResultError;
    private DropInResult deliverBrowserSwitchResultSuccess;
    private Exception handleActivityResultError;
    private DropInResult handleActivityResultSuccess;

    private boolean shouldPerformThreeDSecureVerification;
    private BrowserSwitchResult browserSwitchResult;
    private List<CardType> getSupportedCardTypesSuccess;
    private Exception getSupportedCardTypesError;
    private Exception deviceDataError;
    private Exception authorizationError;

    MockDropInInternalClientBuilder shouldPerformThreeDSecureVerification(boolean shouldPerformThreeDSecureVerification) {
        this.shouldPerformThreeDSecureVerification = shouldPerformThreeDSecureVerification;
        return this;
    }

    MockDropInInternalClientBuilder threeDSecureSuccess(DropInResult dropInResult) {
        this.threeDSecureSuccess = dropInResult;
        return this;
    }

    MockDropInInternalClientBuilder threeDSecureError(Exception error) {
        threeDSecureError = error;
        return this;
    }

    MockDropInInternalClientBuilder getVaultedPaymentMethodsSuccess(List<PaymentMethodNonce> nonces) {
        this.paymentMethodNonceListSuccess = nonces;
        return this;
    }

    MockDropInInternalClientBuilder getVaultedPaymentMethodsError(Exception error) {
        this.getVaultedPaymentMethodsError = error;
        return this;
    }

    MockDropInInternalClientBuilder authorizationSuccess(Authorization authorization) {
        this.authorization = authorization;
        return this;
    }

    MockDropInInternalClientBuilder getConfigurationSuccess(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    MockDropInInternalClientBuilder getSupportedPaymentMethodsSuccess(List<DropInPaymentMethod> supportedPaymentMethods) {
        this.supportedPaymentMethods = supportedPaymentMethods;
        return this;
    }

    MockDropInInternalClientBuilder getSupportedPaymentMethodsError(Exception error) {
        this.getSupportedPaymentMethodsError = error;
        return this;
    }

    MockDropInInternalClientBuilder getSupportedCardTypesSuccess(List<CardType> supportedCardTypes) {
        this.getSupportedCardTypesSuccess = supportedCardTypes;
        return this;
    }

    MockDropInInternalClientBuilder getSupportedCardTypesError(Exception error) {
        this.getSupportedCardTypesError = error;
        return this;
    }

    MockDropInInternalClientBuilder collectDeviceDataSuccess(String deviceDataSuccess) {
        this.deviceDataSuccess = deviceDataSuccess;
        return this;
    }

    MockDropInInternalClientBuilder collectDeviceDataError(Exception deviceDataError) {
        this.deviceDataError = deviceDataError;
        return this;
    }

    MockDropInInternalClientBuilder deletePaymentMethodSuccess(PaymentMethodNonce deletedNonce) {
        this.deletedNonce = deletedNonce;
        return this;
    }

    MockDropInInternalClientBuilder deletePaymentMethodError(Exception error) {
        this.deletePaymentMethodNonceError = error;
        return this;
    }

    MockDropInInternalClientBuilder cardTokenizeSuccess(CardNonce cardNonce) {
        this.cardTokenizeSuccess = cardNonce;
        return this;
    }

    MockDropInInternalClientBuilder cardTokenizeError(Exception error) {
        this.cardTokenizeError = error;
        return this;
    }

    MockDropInInternalClientBuilder payPalError(Exception error) {
        this.payPalError = error;
        return this;
    }

    MockDropInInternalClientBuilder googlePayError(Exception error) {
        this.googlePayError = error;
        return this;
    }

    MockDropInInternalClientBuilder venmoError(Exception error) {
        this.venmoError = error;
        return this;
    }

    MockDropInInternalClientBuilder handleThreeDSecureActivityResultError(Exception error) {
        this.handleThreeDSecureActivityResultError = error;
        return this;
    }

    MockDropInInternalClientBuilder handleThreeDSecureActivityResultSuccess(DropInResult result) {
        this.handleThreeDSecureActivityResultSuccess = result;
        return this;
    }

    MockDropInInternalClientBuilder deliverBrowseSwitchResultError(Exception error) {
        this.deliverBrowserSwitchResultError = error;
        return this;
    }

    MockDropInInternalClientBuilder deliverBrowserSwitchResultSuccess(DropInResult result) {
        this.deliverBrowserSwitchResultSuccess = result;
        return this;
    }

    MockDropInInternalClientBuilder getBrowserSwitchResult(BrowserSwitchResult result) {
        this.browserSwitchResult = result;
        return this;
    }

    MockDropInInternalClientBuilder handleActivityResultError(Exception error) {
        this.handleActivityResultError = error;
        return this;
    }

    MockDropInInternalClientBuilder handleActivityResultSuccess(DropInResult result) {
        this.handleActivityResultSuccess = result;
        return this;
    }

    DropInInternalClient build() {
        DropInInternalClient dropInClient = mock(DropInInternalClient.class);
        when(dropInClient.getBrowserSwitchResult(any(FragmentActivity.class))).thenReturn(browserSwitchResult);

        doAnswer((Answer<Void>) invocation -> {
            AuthorizationCallback callback = (AuthorizationCallback) invocation.getArguments()[0];
            if (authorization != null) {
                callback.onAuthorizationResult(authorization, null);
            } else if (threeDSecureError != null) {
                callback.onAuthorizationResult(null, authorizationError);
            }
            return null;
        }).when(dropInClient).getAuthorization(any(AuthorizationCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            DropInResultCallback callback = (DropInResultCallback) invocation.getArguments()[2];
            if (threeDSecureSuccess != null) {
                callback.onResult(threeDSecureSuccess, null);
            } else if (threeDSecureError != null) {
                callback.onResult(null, threeDSecureError);
            }
            return null;
        }).when(dropInClient).performThreeDSecureVerification(any(FragmentActivity.class), any(PaymentMethodNonce.class), any(DropInResultCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            ShouldRequestThreeDSecureVerification callback = (ShouldRequestThreeDSecureVerification) invocation.getArguments()[1];
            callback.onResult(shouldPerformThreeDSecureVerification);
            return null;
        }).when(dropInClient).shouldRequestThreeDSecureVerification(any(PaymentMethodNonce.class), any(ShouldRequestThreeDSecureVerification.class));

        doAnswer((Answer<Void>) invocation -> {
            GetPaymentMethodNoncesCallback callback = (GetPaymentMethodNoncesCallback) invocation.getArguments()[1];
            if (paymentMethodNonceListSuccess != null) {
                callback.onResult(paymentMethodNonceListSuccess, null);
            } else if (getVaultedPaymentMethodsError != null) {
                callback.onResult(null, getVaultedPaymentMethodsError);
            }
            return null;
        }).when(dropInClient).getVaultedPaymentMethods(any(FragmentActivity.class), any(GetPaymentMethodNoncesCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            ConfigurationCallback callback = (ConfigurationCallback) invocation.getArguments()[0];
            if (configuration != null) {
                callback.onResult(configuration, null);
            }
            return null;
        }).when(dropInClient).getConfiguration(any(ConfigurationCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            GetSupportedPaymentMethodsCallback callback = (GetSupportedPaymentMethodsCallback) invocation.getArguments()[1];
            if (supportedPaymentMethods != null) {
                callback.onResult(supportedPaymentMethods, null);
            } else if (getSupportedPaymentMethodsError != null) {
                callback.onResult(null, getSupportedPaymentMethodsError);
            }
            return null;
        }).when(dropInClient).getSupportedPaymentMethods(any(FragmentActivity.class), any(GetSupportedPaymentMethodsCallback.class));


        doAnswer((Answer<Void>) invocation -> {
            GetSupportedCardTypesCallback callback = (GetSupportedCardTypesCallback) invocation.getArguments()[0];
            if (getSupportedCardTypesSuccess != null) {
                callback.onResult(getSupportedCardTypesSuccess, null);
            } else if (getSupportedCardTypesError != null) {
                callback.onResult(null, getSupportedCardTypesError);
            }
            return null;
        }).when(dropInClient).getSupportedCardTypes(any(GetSupportedCardTypesCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            DataCollectorCallback callback = (DataCollectorCallback) invocation.getArguments()[1];
            if (deviceDataSuccess != null) {
                callback.onResult(deviceDataSuccess, null);
            } else if (deviceDataError != null) {
                callback.onResult(null, deviceDataError);
            }
            return null;
        }).when(dropInClient).collectDeviceData(any(FragmentActivity.class), any(DataCollectorCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            DeletePaymentMethodNonceCallback callback = (DeletePaymentMethodNonceCallback) invocation.getArguments()[2];
            if (deletedNonce != null) {
                callback.onResult(deletedNonce, null);
            } else if (deletePaymentMethodNonceError != null) {
                callback.onResult(null, deletePaymentMethodNonceError);
            }
            return null;
        }).when(dropInClient).deletePaymentMethod(any(FragmentActivity.class), any(PaymentMethodNonce.class), any(DeletePaymentMethodNonceCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            CardTokenizeCallback callback = (CardTokenizeCallback) invocation.getArguments()[1];
            if (cardTokenizeSuccess != null) {
                callback.onResult(cardTokenizeSuccess, null);
            } else if (cardTokenizeError != null) {
                callback.onResult(null, cardTokenizeError);
            }
            return null;
        }).when(dropInClient).tokenizeCard(any(Card.class), any(CardTokenizeCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            PayPalFlowStartedCallback callback = (PayPalFlowStartedCallback) invocation.getArguments()[1];
            if (payPalError != null) {
                callback.onResult(payPalError);
            }
            return null;
        }).when(dropInClient).tokenizePayPalRequest(any(FragmentActivity.class), any(PayPalFlowStartedCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            GooglePayRequestPaymentCallback callback = (GooglePayRequestPaymentCallback) invocation.getArguments()[1];
            if (googlePayError != null) {
                callback.onResult(googlePayError);
            }
            return null;
        }).when(dropInClient).requestGooglePayPayment(any(FragmentActivity.class), any(GooglePayRequestPaymentCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            VenmoTokenizeAccountCallback callback = (VenmoTokenizeAccountCallback) invocation.getArguments()[1];
            if (venmoError != null) {
                callback.onResult(venmoError);
            }
            return null;
        }).when(dropInClient).tokenizeVenmoAccount(any(FragmentActivity.class), any(VenmoTokenizeAccountCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            DropInResultCallback callback = (DropInResultCallback) invocation.getArguments()[3];
            if (handleThreeDSecureActivityResultError != null) {
                callback.onResult(null, handleThreeDSecureActivityResultError);
            } else if (handleThreeDSecureActivityResultSuccess != null) {
                callback.onResult(handleThreeDSecureActivityResultSuccess, null);
            }
            return null;
        }).when(dropInClient).handleThreeDSecureActivityResult(any(FragmentActivity.class), anyInt(), any(Intent.class), any(DropInResultCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            DropInResultCallback callback = (DropInResultCallback) invocation.getArguments()[1];
            if (deliverBrowserSwitchResultError != null) {
                callback.onResult(null, deliverBrowserSwitchResultError);
            } else if (deliverBrowserSwitchResultSuccess != null) {
                callback.onResult(deliverBrowserSwitchResultSuccess, null);
            }
            return null;
        }).when(dropInClient).deliverBrowserSwitchResult(any(FragmentActivity.class), any(DropInResultCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            DropInResultCallback callback = (DropInResultCallback) invocation.getArguments()[4];
            if (handleActivityResultError != null) {
                callback.onResult(null, handleActivityResultError);
            } else if (handleActivityResultSuccess != null) {
                callback.onResult(handleActivityResultSuccess, null);
            }
            return null;
        }).when(dropInClient).handleActivityResult(any(FragmentActivity.class), anyInt(), anyInt(), any(Intent.class), any(DropInResultCallback.class));

        return dropInClient;
    }
}
