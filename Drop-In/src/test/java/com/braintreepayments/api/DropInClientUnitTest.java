package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DropInClientUnitTest {

    @Captor
    ArgumentCaptor<List<DropInPaymentMethodType>> paymentMethodTypesCaptor;

    @Captor
    ArgumentCaptor<List<PaymentMethodNonce>> paymentMethodNoncesCaptor;

    private FragmentActivity activity;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);

        ActivityController<FragmentActivity> activityController =
                Robolectric.buildActivity(FragmentActivity.class);
        activity = activityController.get();
    }

    @Test
    public void getConfiguration_forwardsInvocationToBraintreeClient() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient);

        ConfigurationCallback callback = mock(ConfigurationCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.getConfiguration(callback);

        verify(braintreeClient).getConfiguration(callback);
    }

    @Test
    public void getAuthorization_forwardsAuthorizationFromBraintreeClient() {
        Authorization authorization = mock(Authorization.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(authorization)
                .build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        assertSame(authorization, sut.getAuthorization());
    }

    @Test
    public void sendAnalyticsEvent_forwardsInvocationToBraintreeClient() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient);

        String eventFragment = "event.fragment";

        DropInClient sut = new DropInClient(params);
        sut.sendAnalyticsEvent(eventFragment);

        verify(braintreeClient).sendAnalyticsEvent(eventFragment);
    }

    @Test
    public void collectDeviceData_forwardsInvocationToDataCollector() {
        DataCollector dataCollector = mock(DataCollector.class);
        DropInClientParams params = new DropInClientParams()
                .dataCollector(dataCollector);

        DataCollectorCallback callback = mock(DataCollectorCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.collectDeviceData(activity, callback);

        verify(dataCollector).collectDeviceData(activity, callback);
    }

    @Test
    public void getSupportedPaymentMethods_whenGooglePayEnabledInConfigAndIsReadyToPaySuccess_includesGooglePay() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);

        sut.getSupportedPaymentMethods(activity, callback);
        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethods = paymentMethodTypesCaptor.getValue();
        assertEquals(1, paymentMethods.size());
        assertEquals(DropInPaymentMethodType.GOOGLE_PAYMENT, paymentMethods.get(0));
    }

    @Test
    public void getSupportedPaymentMethods_whenGooglePayEnabledInConfigAndIsReadyToPayError_filtersGooglePayFromSupportedMethods() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPayError(new Exception("google pay error"))
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);

        sut.getSupportedPaymentMethods(activity, callback);
        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethods = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethods.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenGooglePayDisabledInDropInRequest_filtersGooglePayFromSupportedMethods() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder().build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);

        sut.getSupportedPaymentMethods(activity, callback);
        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethods = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethods.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenConfigurationFetchFails_forwardsError() {
        Exception configurationError = new Exception("configuration error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configurationError(configurationError)
                .build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);

        sut.getSupportedPaymentMethods(activity, callback);
        verify(callback).onResult(null, configurationError);
    }

    @Test
    public void shouldRequestThreeDSecureVerification_whenNonceIsGooglePayNonNetworkTokenized_returnsTrue() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE))
                .build();

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setAmount("1.00");

        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest)
                .requestThreeDSecureVerification(true);

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE));

        DropInClient sut = new DropInClient(params);
        ShouldRequestThreeDSecureVerification callback = mock(ShouldRequestThreeDSecureVerification.class);

        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback);
        verify(callback).onResult(true);
    }

    @Test
    public void shouldRequestThreeDSecureVerification_whenNonceIsGooglePayNetworkTokenized_returnsFalse() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE))
                .build();

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setAmount("1.00");

        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest)
                .requestThreeDSecureVerification(true);

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NETWORK_TOKENIZED_RESPONSE));

        DropInClient sut = new DropInClient(params);
        ShouldRequestThreeDSecureVerification callback = mock(ShouldRequestThreeDSecureVerification.class);

        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback);
        verify(callback).onResult(false);
    }

    @Test
    public void shouldRequestThreeDSecureVerification_whenNonceIsCardNonce_returnsTrue() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE))
                .build();

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setAmount("1.00");

        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest)
                .requestThreeDSecureVerification(true);

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));

        DropInClient sut = new DropInClient(params);
        ShouldRequestThreeDSecureVerification callback = mock(ShouldRequestThreeDSecureVerification.class);

        sut.shouldRequestThreeDSecureVerification(paymentMethodNonce, callback);
        verify(callback).onResult(true);
    }

    @Test
    public void shouldRequestThreeDSecureVerification_whenNonceIsNotCardOrGooglePay_returnsFalse() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE))
                .build();

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setAmount("1.00");

        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest)
                .requestThreeDSecureVerification(true);

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce paymentMethodNonce = mock(PayPalAccountNonce.class);

        DropInClient sut = new DropInClient(params);
        ShouldRequestThreeDSecureVerification callback = mock(ShouldRequestThreeDSecureVerification.class);

        sut.shouldRequestThreeDSecureVerification(paymentMethodNonce, callback);
        verify(callback).onResult(false);
    }

    @Test
    public void shouldRequestThreeDSecureVerification_whenConfigurationFetchFails_returnsFalse() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configurationError(new Exception("configuration error"))
                .build();

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setAmount("1.00");

        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest)
                .requestThreeDSecureVerification(true);

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE));

        DropInClient sut = new DropInClient(params);
        ShouldRequestThreeDSecureVerification callback = mock(ShouldRequestThreeDSecureVerification.class);

        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback);
        verify(callback).onResult(false);
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithErrorIfInvalidClientTokenWasUsed() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.TOKENIZATION_KEY))
                .build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(new DropInRequest());

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<InvalidArgumentException> captor = ArgumentCaptor.forClass(InvalidArgumentException.class);
        verify(callback).onResult((DropInResult) isNull(), captor.capture());

        InvalidArgumentException exception = captor.getValue();
        assertEquals("DropInClient#fetchMostRecentPaymentMethods() must be called with a client token", exception.getMessage());
    }

    @Test
    public void performThreeDSecureVerification_performsVerificationAndSetsNonceOnThreeDSecureRequest() throws JSONException {
        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder().build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .threeDSecureClient(threeDSecureClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResultCallback callback = mock(ThreeDSecureResultCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback);

        verify(threeDSecureClient).performVerification(same(activity), same(threeDSecureRequest), any(ThreeDSecureResultCallback.class));
        assertEquals(paymentMethodNonce.getString(), threeDSecureRequest.getNonce());
    }

    @Test
    public void performThreeDSecureVerification_whenVerificationFails_callbackError() throws JSONException {
        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest);

        Exception performVerificationError = new Exception("verification error");
        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .performVerificationError(performVerificationError)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .threeDSecureClient(threeDSecureClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResultCallback callback = mock(ThreeDSecureResultCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback);

        verify(callback).onResult(null, performVerificationError);
        verify(threeDSecureClient, never()).continuePerformVerification(any(FragmentActivity.class), any(ThreeDSecureRequest.class), any(ThreeDSecureResult.class), any(ThreeDSecureResultCallback.class));
    }

    @Test
    public void performThreeDSecureVerification_continuesThreeDSecureVerificationAndForwardsResponseCallback() throws JSONException {
        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest);

        ThreeDSecureResult threeDSecureResult = new ThreeDSecureResult();
        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .performVerificationSuccess(threeDSecureResult)
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .threeDSecureClient(threeDSecureClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResultCallback callback = mock(ThreeDSecureResultCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback);

        verify(threeDSecureClient).continuePerformVerification(activity, threeDSecureRequest, threeDSecureResult, callback);
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithResultIfLastUsedPaymentMethodTypeWasPayWithGoogle() throws JSONException {
        BraintreeSharedPreferences.getSharedPreferences(activity)
                .edit()
                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
                        DropInPaymentMethodType.GOOGLE_PAYMENT.getCanonicalName())
                .commit();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertEquals(DropInPaymentMethodType.GOOGLE_PAYMENT, result.getPaymentMethodType());
        assertNull(result.getPaymentMethodNonce());
    }

    @Test
    public void fetchMostRecentPaymentMethod_doesNotCallBackWithPayWithGoogleIfPayWithGoogleIsNotAvailable()
            throws JSONException {
        BraintreeSharedPreferences.getSharedPreferences(activity)
                .edit()
                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
                        DropInPaymentMethodType.GOOGLE_PAYMENT.getCanonicalName())
                .commit();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        ArrayList<PaymentMethodNonce> paymentMethods = new ArrayList<>();
        paymentMethods.add(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));
        paymentMethods.add(GooglePayCardNonce.fromJSON(new JSONObject(Fixtures.GOOGLE_PAY_NETWORK_TOKENIZED_RESPONSE)));
        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesSuccess(paymentMethods)
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .paymentMethodClient(paymentMethodClient)
                .googlePayClient(googlePayClient);

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertEquals(DropInPaymentMethodType.VISA, result.getPaymentMethodType());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithErrorOnGetPaymentMethodsError() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .build();

        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesError(new BraintreeException("Error occurred"))
                .build();
        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .paymentMethodClient(paymentMethodClient)
                .dropInRequest(new DropInRequest());

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<BraintreeException> captor = ArgumentCaptor.forClass(BraintreeException.class);
        verify(callback).onResult((DropInResult) isNull(), captor.capture());

        BraintreeException exception = captor.getValue();
        assertEquals("Error occurred", exception.getMessage());
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithResultWhenThereIsAPaymentMethod()
            throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        ArrayList<PaymentMethodNonce> paymentMethods = new ArrayList<>();
        paymentMethods.add(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));

        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesSuccess(paymentMethods)
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .paymentMethodClient(paymentMethodClient);

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertEquals(DropInPaymentMethodType.VISA, result.getPaymentMethodType());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithNullResultWhenThereAreNoPaymentMethods()
            throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        ArrayList<PaymentMethodNonce> paymentMethods = new ArrayList<>();

        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesSuccess(paymentMethods)
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .paymentMethodClient(paymentMethodClient);

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertNull(result.getPaymentMethodType());
        assertNull(result.getPaymentMethodNonce());
    }

    @Test
    public void getSupportedPaymentMethods_whenNoPaymentMethodsEnabledInConfiguration_callsBackWithNoPaymentMethods() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(mockConfiguration(false, false, false, false, false))
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);

        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenPaymentMethodsEnabledInConfiguration_callsBackWithPaymentMethods() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(mockConfiguration(true, true, true, true, false))
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);

        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(4, paymentMethodTypes.size());
        assertEquals(DropInPaymentMethodType.PAYPAL, paymentMethodTypes.get(0));
        assertEquals(DropInPaymentMethodType.PAY_WITH_VENMO, paymentMethodTypes.get(1));
        assertEquals(DropInPaymentMethodType.UNKNOWN, paymentMethodTypes.get(2));
        assertEquals(DropInPaymentMethodType.GOOGLE_PAYMENT, paymentMethodTypes.get(3));
    }

    @Test
    public void getSupportedPaymentMethods_whenUnionPayNotSupportedAndOtherCardsPresent_callsBackWithOtherCards() {
        Configuration configuration = mockConfiguration(false, false, true, false, false);
        when(configuration.getSupportedCardTypes())
                .thenReturn(Arrays.asList(DropInPaymentMethodType.UNIONPAY.getCanonicalName(), DropInPaymentMethodType.VISA.getCanonicalName()));
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(1, paymentMethodTypes.size());
        assertEquals(DropInPaymentMethodType.UNKNOWN, paymentMethodTypes.get(0));
    }

    @Test
    public void getSupportedPaymentMethods_whenOnlyUnionPayPresentAndNotSupported_callsBackWithNoCards() {
        Configuration configuration = mockConfiguration(false, false, true, false, false);
        when(configuration.getSupportedCardTypes())
                .thenReturn(Collections.singletonList(DropInPaymentMethodType.UNIONPAY.getCanonicalName()));

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenOnlyUnionPayPresentAndSupported_callsBackWithCards() {
        Configuration configuration = mockConfiguration(false, false, true, false, true);
        when(configuration.getSupportedCardTypes())
                .thenReturn(Collections.singletonList(DropInPaymentMethodType.UNIONPAY.getCanonicalName()));

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(1, paymentMethodTypes.size());
        assertEquals(DropInPaymentMethodType.UNKNOWN, paymentMethodTypes.get(0));
    }

    @Test
    public void getSupportedPaymentMethods_whenCardsDisabledInDropInRequest_doesNotReturnCards() {
        Configuration configuration = mockConfiguration(false, false, true, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableCard();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenPayPalDisabledInDropInRequest_doesNotReturnPayPal() {
        Configuration configuration = mockConfiguration(true, false, false, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disablePayPal();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenVenmoDisabledInDropInRequest_doesNotReturnVenmo() {
        Configuration configuration = mockConfiguration(false, true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableVenmo();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenGooglePayDisabledInDropInRequest_doesNotReturnGooglePay() {
        Configuration configuration = mockConfiguration(false, false, false, true, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethodType> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void tokenizePayPalAccount_withoutPayPalRequest_tokenizesPayPalWithVaultRequest() {
        Configuration configuration = mockConfiguration(true, false, false, false, false);
        DropInRequest dropInRequest = new DropInRequest();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        PayPalClient payPalClient = mock(PayPalClient.class);
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .payPalClient(payPalClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);

        PayPalFlowStartedCallback callback = mock(PayPalFlowStartedCallback.class);
        sut.tokenizePayPalRequest(activity, callback);

        verify(payPalClient).tokenizePayPalAccount(same(activity), any(PayPalVaultRequest.class), same(callback));
    }

    @Test
    public void tokenizePayPalAccount_withPayPalCheckoutRequest_tokenizesPayPalWithCheckoutRequest() {
        Configuration configuration = mockConfiguration(true, false, false, false, false);
        PayPalCheckoutRequest payPalRequest = new PayPalCheckoutRequest("1.00");
        DropInRequest dropInRequest = new DropInRequest()
                .paypalRequest(payPalRequest);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        PayPalClient payPalClient = mock(PayPalClient.class);
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .payPalClient(payPalClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);

        PayPalFlowStartedCallback callback = mock(PayPalFlowStartedCallback.class);
        sut.tokenizePayPalRequest(activity, callback);

        verify(payPalClient).tokenizePayPalAccount(same(activity), same(payPalRequest), same(callback));
    }

    @Test
    public void tokenizePayPalAccount_withPayPalVaultRequest_tokenizesPayPalWithVaultRequest() {
        Configuration configuration = mockConfiguration(true, false, false, false, false);
        PayPalVaultRequest payPalRequest = new PayPalVaultRequest();
        DropInRequest dropInRequest = new DropInRequest()
                .paypalRequest(payPalRequest);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        PayPalClient payPalClient = mock(PayPalClient.class);
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .payPalClient(payPalClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);

        PayPalFlowStartedCallback callback = mock(PayPalFlowStartedCallback.class);
        sut.tokenizePayPalRequest(activity, callback);

        verify(payPalClient).tokenizePayPalAccount(same(activity), same(payPalRequest), same(callback));
    }

    @Test
    public void tokenizeVenmoAccount_tokenizesVenmo() {
        Configuration configuration = mockConfiguration(false, true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .vaultVenmo(true);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        VenmoClient venmoClient = mock(VenmoClient.class);

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .venmoClient(venmoClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);

        VenmoTokenizeAccountCallback callback = mock(VenmoTokenizeAccountCallback.class);
        sut.tokenizeVenmoAccount(activity, callback);

        ArgumentCaptor<VenmoRequest> captor = ArgumentCaptor.forClass(VenmoRequest.class);
        verify(venmoClient).tokenizeVenmoAccount(same(activity), captor.capture(), same(callback));

        VenmoRequest request = captor.getValue();
        assertTrue(request.getShouldVault());
    }

    @Test
    public void requestGooglePayPayment_requestsGooglePay() {
        Configuration configuration = mockConfiguration(false, false, false, true, false);

        GooglePayRequest googlePayRequest = new GooglePayRequest();
        DropInRequest dropInRequest = new DropInRequest()
                .googlePaymentRequest(googlePayRequest);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);

        GooglePayRequestPaymentCallback callback = mock(GooglePayRequestPaymentCallback.class);
        sut.requestGooglePayPayment(activity, callback);

        verify(googlePayClient).requestPayment(same(activity), same(googlePayRequest), same(callback));
    }

    @Test
    public void deletePaymentMethod_callsDeletePaymentMethod() {
        PaymentMethodClient paymentMethodClient = mock(PaymentMethodClient.class);

        DropInClientParams params = new DropInClientParams()
                .paymentMethodClient(paymentMethodClient);

        DropInClient sut = new DropInClient(params);

        CardNonce cardNonce = mock(CardNonce.class);
        DeletePaymentMethodNonceCallback callback = mock(DeletePaymentMethodNonceCallback.class);
        sut.deletePaymentMethod(activity, cardNonce, callback);

        verify(paymentMethodClient).deletePaymentMethod(same(activity), same(cardNonce), same(callback));
    }

    @Test
    public void tokenizeCard_forwardsInvocationToCardClient() {
        CardClient cardClient = mock(CardClient.class);
        DropInClientParams params = new DropInClientParams()
                .cardClient(cardClient);

        Card card = new Card();
        CardTokenizeCallback callback = mock(CardTokenizeCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.tokenizeCard(card, callback);

        verify(cardClient).tokenize(card, callback);
    }

    @Test
    public void fetchUnionPayCapabilities_forwardsInvocationToUnionPayClient() {
        UnionPayClient unionPayClient = mock(UnionPayClient.class);
        DropInClientParams params = new DropInClientParams()
                .unionPayClient(unionPayClient);

        String cardNumber = "4111111111111111";
        UnionPayFetchCapabilitiesCallback callback = mock(UnionPayFetchCapabilitiesCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.fetchUnionPayCapabilities(cardNumber, callback);

        verify(unionPayClient).fetchCapabilities(cardNumber, callback);
    }

    @Test
    public void enrollUnionPay_forwardsInvocationToUnionPayClient() {
        UnionPayClient unionPayClient = mock(UnionPayClient.class);
        DropInClientParams params = new DropInClientParams()
                .unionPayClient(unionPayClient);

        UnionPayCard unionPayCard = new UnionPayCard();
        UnionPayEnrollCallback callback = mock(UnionPayEnrollCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.enrollUnionPay(unionPayCard, callback);
    }

    @Test
    public void tokenizeUnionPay_forwardsInvocationToUnionPayClient() {
        UnionPayClient unionPayClient = mock(UnionPayClient.class);
        DropInClientParams params = new DropInClientParams()
                .unionPayClient(unionPayClient);

        UnionPayCard unionPayCard = new UnionPayCard();
        UnionPayTokenizeCallback callback = mock(UnionPayTokenizeCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.tokenizeUnionPay(unionPayCard, callback);
    }

    @Test
    public void deliverBrowserSwitchResult_whenPayPalWithoutDataCollection_tokenizesResult() {
        PayPalAccountNonce payPalAccountNonce = mock(PayPalAccountNonce.class);
        PayPalClient payPalClient = new MockPayPalClientBuilder()
                .browserSwitchResult(payPalAccountNonce)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(false);

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .payPalClient(payPalClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.PAYPAL);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInClient sut = new DropInClient(params);
        sut.deliverBrowserSwitchResult(activity, callback);

        verify(payPalClient).onBrowserSwitchResult(same(browserSwitchResult), any(PayPalBrowserSwitchResultCallback.class));

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult dropInResult = captor.getValue();
        assertSame(dropInResult.getPaymentMethodNonce(), payPalAccountNonce);
        assertNull(dropInResult.getDeviceData());

        verify(dataCollector, never()).collectDeviceData(any(Context.class), any(DataCollectorCallback.class));
    }

    @Test
    public void deliverBrowserSwitchResult_whenPayPalWithDataCollection_tokenizesResultAndCollectsData() {
        PayPalAccountNonce payPalAccountNonce = mock(PayPalAccountNonce.class);
        PayPalClient payPalClient = new MockPayPalClientBuilder()
                .browserSwitchResult(payPalAccountNonce)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(true);

        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("device data")
                .build();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .payPalClient(payPalClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.PAYPAL);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInClient sut = new DropInClient(params);
        sut.deliverBrowserSwitchResult(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult dropInResult = captor.getValue();
        assertSame(dropInResult.getPaymentMethodNonce(), payPalAccountNonce);
        assertEquals("device data", dropInResult.getDeviceData());
    }

    @Test
    public void deliverBrowserSwitchResult_whenPayPalTokenizationFails_callbackError() {
        Exception browserSwitchError = new Exception("paypal tokenization error");
        PayPalClient payPalClient = new MockPayPalClientBuilder()
                .browserSwitchError(browserSwitchError)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(false);

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .payPalClient(payPalClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.PAYPAL);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInClient sut = new DropInClient(params);
        sut.deliverBrowserSwitchResult(activity, callback);

        verify(callback).onResult(null, browserSwitchError);
    }

    @Test
    public void deliverBrowserSwitchResult_whenThreeDSecureWithoutDataCollection_tokenizesResult() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResult threeDSecureResult = new ThreeDSecureResult();
        threeDSecureResult.setTokenizedCard(cardNonce);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .browserSwitchResult(threeDSecureResult)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(false);

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.THREE_D_SECURE);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInClient sut = new DropInClient(params);
        sut.deliverBrowserSwitchResult(activity, callback);

        verify(threeDSecureClient).onBrowserSwitchResult(same(browserSwitchResult), any(ThreeDSecureResultCallback.class));

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult dropInResult = captor.getValue();
        assertSame(dropInResult.getPaymentMethodNonce(), cardNonce);
        assertNull(dropInResult.getDeviceData());

        verify(dataCollector, never()).collectDeviceData(any(Context.class), any(DataCollectorCallback.class));
    }

    @Test
    public void deliverBrowserSwitchResult_whenThreeDSecureWithDataCollection_tokenizesResultAndCollectsData() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResult threeDSecureResult = new ThreeDSecureResult();
        threeDSecureResult.setTokenizedCard(cardNonce);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .browserSwitchResult(threeDSecureResult)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(true);

        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("device data")
                .build();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.THREE_D_SECURE);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInClient sut = new DropInClient(params);
        sut.deliverBrowserSwitchResult(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult dropInResult = captor.getValue();
        assertSame(dropInResult.getPaymentMethodNonce(), cardNonce);
        assertEquals("device data", dropInResult.getDeviceData());
    }

    @Test
    public void deliverBrowserSwitchResult_whenThreeDSecureTokenizationFails_callbackError() {
        Exception browserSwitchError = new Exception("threedsecure tokenization error");
        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .browserSwitchError(browserSwitchError)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(false);

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.THREE_D_SECURE);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInClient sut = new DropInClient(params);
        sut.deliverBrowserSwitchResult(activity, callback);

        verify(callback).onResult(null, browserSwitchError);
    }

    @Test
    public void handleThreeDSecureResult_withoutDataCollection_tokenizesResult() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResult threeDSecureResult = new ThreeDSecureResult();
        threeDSecureResult.setTokenizedCard(cardNonce);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .activityResult(threeDSecureResult)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(false);

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        Intent intent = new Intent();
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.handleThreeDSecureActivityResult(activity, 123, intent, callback);

        verify(threeDSecureClient).onActivityResult(eq(123), same(intent), any(ThreeDSecureResultCallback.class));

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult dropInResult = captor.getValue();
        assertSame(dropInResult.getPaymentMethodNonce(), cardNonce);
        assertNull(dropInResult.getDeviceData());

        verify(dataCollector, never()).collectDeviceData(any(Context.class), any(DataCollectorCallback.class));
    }

    @Test
    public void handleThreeDSecureResult_withDataCollection_tokenizesResultAndCollectsData() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResult threeDSecureResult = new ThreeDSecureResult();
        threeDSecureResult.setTokenizedCard(cardNonce);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .activityResult(threeDSecureResult)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(true);

        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("device data")
                .build();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        Intent intent = new Intent();
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.handleThreeDSecureActivityResult(activity, 123, intent, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult dropInResult = captor.getValue();
        assertSame(dropInResult.getPaymentMethodNonce(), cardNonce);
        assertEquals("device data", dropInResult.getDeviceData());
    }

    @Test
    public void handleThreeDSecureResult_whenTokenizationFails_callbackError() {
        Exception activityResultError = new Exception("activity result error");
        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .activityResultError(activityResultError)
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(false);

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        Intent intent = new Intent();
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.handleThreeDSecureActivityResult(activity, 123, intent, callback);

        verify(callback).onResult(null, activityResultError);
    }

    @Test
    public void launchDropInForResult_launchesDropInActivityWithIntentExtras() {
        Authorization authorization = mock(Authorization.class);
        when(authorization.toString()).thenReturn("authorization");

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("session-id")
                .authorization(authorization)
                .build();

        DropInRequest dropInRequest = new DropInRequest().vaultManager(true);
        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest);

        DropInClient sut = new DropInClient(params);

        FragmentActivity activity = mock(FragmentActivity.class);
        sut.launchDropInForResult(activity, 123);

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(captor.capture(), eq(123));

        Intent intent = captor.getValue();
        assertEquals("session-id", intent.getStringExtra(DropInClient.EXTRA_SESSION_ID));
        assertEquals("authorization", intent.getStringExtra(DropInClient.EXTRA_AUTHORIZATION));

        DropInRequest dropInRequestExtra = intent.getParcelableExtra(DropInClient.EXTRA_CHECKOUT_REQUEST);
        assertTrue(dropInRequestExtra.isVaultManagerEnabled());
    }

    @Test
    public void getVaultedPaymentMethods_forwardsConfigurationFetchError() {
        Exception configurationError = new Exception("configuration error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configurationError(configurationError)
                .build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.getVaultedPaymentMethods(activity, false, callback);

        verify(callback).onResult(null, configurationError);
    }

    @Test
    public void getVaultedPaymentMethods_forwardsPaymentMethodClientError() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(mockConfiguration(true, true, true, true, true))
                .build();

        Exception paymentMethodClientError = new Exception("payment method client error");
        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesError(paymentMethodClientError)
                .build();

        DropInClientParams params = new DropInClientParams()
                .paymentMethodClient(paymentMethodClient)
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.getVaultedPaymentMethods(activity, false, callback);

        verify(callback).onResult(null, paymentMethodClientError);
    }

    @Test
    public void getVaultedPaymentMethods_whenGooglePayDisabled_callbackPaymentMethodClientResult() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(mockConfiguration(true, true, true, true, true))
                .build();

        PaymentMethodNonce cardNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));

        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesSuccess(Collections.singletonList(cardNonce))
                .build();

        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .paymentMethodClient(paymentMethodClient)
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.getVaultedPaymentMethods(activity, false, callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals(1, paymentMethodNonces.size());
        assertSame(cardNonce, paymentMethodNonces.get(0));
    }

    @Test
    public void getVaultedPaymentMethods_whenGooglePayReadyToPay_callbackPaymentMethodClientResultWithGooglePayNonce() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(mockConfiguration(true, true, true, true, true))
                .build();

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE));

        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesSuccess(Collections.singletonList(googlePayCardNonce))
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .paymentMethodClient(paymentMethodClient)
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.getVaultedPaymentMethods(activity, false, callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals(1, paymentMethodNonces.size());
        assertSame(googlePayCardNonce, paymentMethodNonces.get(0));
    }

    @Test
    public void getVaultedPaymentMethods_whenGooglePayClientErrors_callbackPaymentMethodClientResultWithoutGooglePayNonce() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPayError(new Exception("google pay client error"))
                .build();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(mockConfiguration(true, true, true, true, true))
                .build();

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE));

        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesSuccess(Collections.singletonList(googlePayCardNonce))
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        DropInClientParams params = new DropInClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .paymentMethodClient(paymentMethodClient)
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInClient sut = new DropInClient(params);
        sut.getVaultedPaymentMethods(activity, false, callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals(0, paymentMethodNonces.size());
    }

    private Configuration mockConfiguration(boolean paypalEnabled, boolean venmoEnabled,
                                            boolean cardEnabled, boolean googlePayEnabled, boolean unionPayEnabled) {
        Configuration configuration = mock(Configuration.class);

        when(configuration.isPayPalEnabled()).thenReturn(paypalEnabled);
        when(configuration.isVenmoEnabled()).thenReturn(venmoEnabled);
        when(configuration.isGooglePayEnabled()).thenReturn(googlePayEnabled);
        when(configuration.isUnionPayEnabled()).thenReturn(unionPayEnabled);

        if (cardEnabled) {
            when(configuration.getSupportedCardTypes()).thenReturn(Collections.singletonList(DropInPaymentMethodType.VISA.getCanonicalName()));
        }

        return configuration;
    }
}