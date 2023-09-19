package com.braintreepayments.api;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ApplicationProvider;
import androidx.work.testing.WorkManagerTestInitHelper;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class DropInInternalClientUnitTest {

    @Captor
    ArgumentCaptor<List<DropInPaymentMethod>> paymentMethodTypesCaptor;

    @Captor
    ArgumentCaptor<List<PaymentMethodNonce>> paymentMethodNoncesCaptor;

    private FragmentActivity activity;
    private DropInSharedPreferences dropInSharedPreferences;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);

        dropInSharedPreferences = mock(DropInSharedPreferences.class);
        ActivityController<FragmentActivity> activityController =
                Robolectric.buildActivity(FragmentActivity.class);
        activity = activityController.get();
        // This suppresses errors from WorkManager initialization within BraintreeClient initialization (AnalyticsClient)
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void constructor_setsIntegrationTypeDropIn() {
        DropInInternalClient sut =
                new DropInInternalClient(activity, Fixtures.TOKENIZATION_KEY, "session-id", new DropInRequest());
        assertEquals(IntegrationType.DROP_IN, sut.braintreeClient.getIntegrationType());
    }

    @Test
    public void internalConstructor_setsBraintreeClientWithSessionId() {
        DropInInternalClient sut =
                new DropInInternalClient(activity, Fixtures.TOKENIZATION_KEY, "session-id", new DropInRequest());
        assertEquals("session-id", sut.braintreeClient.getSessionId());
    }

    @Test
    public void internalConstructor_usesDefaultBraintreeCustomUrlScheme() {
        DropInInternalClient sut =
                new DropInInternalClient(activity, Fixtures.TOKENIZATION_KEY, "session-id", new DropInRequest());
        assertEquals("com.braintreepayments.api.dropin.test.braintree", sut.braintreeClient.getReturnUrlScheme());
    }

    @Test
    public void internalConstructor_overridesBraintreeCustomUrlSchemeIfSet() {
        DropInRequest request = new DropInRequest();
        request.setCustomUrlScheme("sample-custom-url-scheme");
        DropInInternalClient sut =
                new DropInInternalClient(activity, Fixtures.TOKENIZATION_KEY, "session-id", request);
        assertEquals("sample-custom-url-scheme", sut.braintreeClient.getReturnUrlScheme());
    }

    @Test
    public void getConfiguration_forwardsInvocationToBraintreeClient() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient);

        ConfigurationCallback callback = mock(ConfigurationCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.getConfiguration(callback);

        verify(braintreeClient).getConfiguration(callback);
    }

    @Test
    public void sendAnalyticsEvent_forwardsInvocationToBraintreeClient() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient);

        String eventFragment = "event.fragment";

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.sendAnalyticsEvent(eventFragment);

        verify(braintreeClient).sendAnalyticsEvent(eventFragment);
    }

    @Test
    public void collectDeviceData_forwardsInvocationToDataCollector() {
        DataCollector dataCollector = mock(DataCollector.class);
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dataCollector(dataCollector);

        DataCollectorCallback callback = mock(DataCollectorCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);

        sut.getSupportedPaymentMethods(activity, callback);
        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethods = paymentMethodTypesCaptor.getValue();
        assertEquals(1, paymentMethods.size());
        assertEquals(DropInPaymentMethod.GOOGLE_PAY, paymentMethods.get(0));
    }

    @Test
    public void getSupportedPaymentMethods_whenGooglePayEnabledInConfigAndIsReadyToPayError_filtersGooglePayFromSupportedMethods() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPayError(new Exception("google pay error"))
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);

        sut.getSupportedPaymentMethods(activity, callback);
        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethods = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethods.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenGooglePayDisabledInDropInRequest_filtersGooglePayFromSupportedMethods() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder().build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setGooglePayDisabled(true);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);

        sut.getSupportedPaymentMethods(activity, callback);
        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethods = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethods.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenConfigurationFetchFails_forwardsError() {
        Exception configurationError = new Exception("configuration error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configurationError(configurationError)
                .build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE));

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NETWORK_TOKENIZED_RESPONSE));

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce paymentMethodNonce = mock(PayPalAccountNonce.class);

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient);

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE));

        DropInInternalClient sut = new DropInInternalClient(params);
        ShouldRequestThreeDSecureVerification callback = mock(ShouldRequestThreeDSecureVerification.class);

        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback);
        verify(callback).onResult(false);
    }

    @Test
    public void performThreeDSecureVerification_performsVerificationAndSetsNonceOnThreeDSecureRequest() throws JSONException {
        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder().build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .threeDSecureClient(threeDSecureClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback);

        verify(threeDSecureClient).performVerification(same(activity), same(threeDSecureRequest), any(ThreeDSecureResultCallback.class));
        assertEquals(paymentMethodNonce.getString(), threeDSecureRequest.getNonce());
    }

    @Test
    public void performThreeDSecureVerification_whenVerificationFails_callbackError() throws JSONException {
        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        Exception performVerificationError = new Exception("verification error");
        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .performVerificationError(performVerificationError)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .threeDSecureClient(threeDSecureClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback);

        verify(callback).onResult(null, performVerificationError);
        verify(threeDSecureClient, never()).continuePerformVerification(any(FragmentActivity.class), any(ThreeDSecureRequest.class), any(ThreeDSecureResult.class), any(ThreeDSecureResultCallback.class));
    }

    @Test
    public void performThreeDSecureVerification_includesDeviceDataInResult() throws JSONException {
        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        ThreeDSecureResult performVerificationResult = new ThreeDSecureResult();

        ThreeDSecureResult continueVerificationResult = new ThreeDSecureResult();
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        continueVerificationResult.setTokenizedCard(cardNonce);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .performVerificationSuccess(performVerificationResult)
                .continueVerificationSuccess(continueVerificationResult)
                .build();

        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("device data")
                .build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dataCollector(dataCollector)
                .dropInRequest(dropInRequest)
                .threeDSecureClient(threeDSecureClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult dropInResult = captor.getValue();
        assertSame(cardNonce, dropInResult.getPaymentMethodNonce());
        assertEquals("device data", dropInResult.getDeviceData());
    }

    @Test
    public void performThreeDSecureVerification_whenDataCollectionFails_callsBackAnError() throws JSONException {
        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setThreeDSecureRequest(threeDSecureRequest);

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        ThreeDSecureResult performVerificationResult = new ThreeDSecureResult();

        ThreeDSecureResult continueVerificationResult = new ThreeDSecureResult();
        continueVerificationResult.setTokenizedCard(cardNonce);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .performVerificationSuccess(performVerificationResult)
                .continueVerificationSuccess(continueVerificationResult)
                .build();

        Exception dataCollectionError = new Exception("data collection error");
        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataError(dataCollectionError)
                .build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dataCollector(dataCollector)
                .dropInRequest(dropInRequest)
                .threeDSecureClient(threeDSecureClient);

        PaymentMethodNonce paymentMethodNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback);

        verify(callback).onResult(null, dataCollectionError);
    }


    @Test
    public void getSupportedPaymentMethods_whenNoPaymentMethodsEnabledInConfiguration_callsBackWithNoPaymentMethods() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(mockConfiguration(false, false, false, false, false))
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenPaymentMethodsEnabledInConfiguration_callsBackWithPaymentMethods() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(mockConfiguration(true, true, true, true, false))
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        VenmoClient venmoClient = new MockVenmoClientBuilder()
                .isVenmoAppInstalled(true)
                .build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .venmoClient(venmoClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(4, paymentMethodTypes.size());
        assertEquals(DropInPaymentMethod.PAYPAL, paymentMethodTypes.get(0));
        assertEquals(DropInPaymentMethod.VENMO, paymentMethodTypes.get(1));
        assertEquals(DropInPaymentMethod.UNKNOWN, paymentMethodTypes.get(2));
        assertEquals(DropInPaymentMethod.GOOGLE_PAY, paymentMethodTypes.get(3));
    }

    @Test
    public void getSupportedPaymentMethods_whenUnionPayNotSupportedAndOtherCardsPresent_callsBackWithOtherCards() {
        Configuration configuration = mockConfiguration(false, false, true, false, false);
        when(configuration.getSupportedCardTypes())
                .thenReturn(Arrays.asList(DropInPaymentMethod.UNIONPAY.name(), DropInPaymentMethod.VISA.name()));
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(1, paymentMethodTypes.size());
        assertEquals(DropInPaymentMethod.UNKNOWN, paymentMethodTypes.get(0));
    }

    @Test
    public void getSupportedPaymentMethods_whenOnlyUnionPayPresentAndNotSupported_callsBackWithNoCards() {
        Configuration configuration = mockConfiguration(false, false, true, false, false);
        when(configuration.getSupportedCardTypes())
                .thenReturn(Collections.singletonList("UnionPay"));

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenOnlyUnionPayPresentAndSupported_callsBackWithCards() {
        Configuration configuration = mockConfiguration(false, false, true, false, true);
        when(configuration.getSupportedCardTypes())
                .thenReturn(Collections.singletonList(DropInPaymentMethod.UNIONPAY.name()));

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(new DropInRequest())
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();

        assertEquals(1, paymentMethodTypes.size());
        assertEquals(DropInPaymentMethod.UNKNOWN, paymentMethodTypes.get(0));
    }

    @Test
    public void getSupportedPaymentMethods_whenCardsDisabledInDropInRequest_doesNotReturnCards() {
        Configuration configuration = mockConfiguration(false, false, true, false, false);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setCardDisabled(true);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenPayPalDisabledInDropInRequest_doesNotReturnPayPal() {
        Configuration configuration = mockConfiguration(true, false, false, false, false);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setPayPalDisabled(true);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenVenmoDisabledInDropInRequest_doesNotReturnVenmo() {
        Configuration configuration = mockConfiguration(false, true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setVenmoDisabled(true);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        VenmoClient venmoClient = new MockVenmoClientBuilder()
                .isVenmoAppInstalled(true)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .venmoClient(venmoClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenVenmoNotInstalled_doesNotReturnVenmo() {
        Configuration configuration = mockConfiguration(false, true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        VenmoClient venmoClient = new MockVenmoClientBuilder()
                .isVenmoAppInstalled(false)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .venmoClient(venmoClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void getSupportedPaymentMethods_whenGooglePayDisabledInDropInRequest_doesNotReturnGooglePay() {
        Configuration configuration = mockConfiguration(false, false, false, true, false);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setGooglePayDisabled(true);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        GetSupportedPaymentMethodsCallback callback = mock(GetSupportedPaymentMethodsCallback.class);
        sut.getSupportedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodTypesCaptor.capture(), (Exception) isNull());

        List<DropInPaymentMethod> paymentMethodTypes = paymentMethodTypesCaptor.getValue();
        assertEquals(0, paymentMethodTypes.size());
    }

    @Test
    public void tokenizePayPalAccount_withoutPayPalRequest_tokenizesPayPalWithVaultRequest() {
        Configuration configuration = mockConfiguration(true, false, false, false, false);
        DropInRequest dropInRequest = new DropInRequest();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        PayPalClient payPalClient = mock(PayPalClient.class);
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .payPalClient(payPalClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        PayPalFlowStartedCallback callback = mock(PayPalFlowStartedCallback.class);
        sut.tokenizePayPalRequest(activity, callback);

        verify(payPalClient).tokenizePayPalAccount(same(activity), any(PayPalVaultRequest.class), same(callback));
    }

    @Test
    public void tokenizePayPalAccount_withPayPalCheckoutRequest_tokenizesPayPalWithCheckoutRequest() {
        Configuration configuration = mockConfiguration(true, false, false, false, false);
        PayPalCheckoutRequest payPalRequest = new PayPalCheckoutRequest("1.00");
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setPayPalRequest(payPalRequest);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        PayPalClient payPalClient = mock(PayPalClient.class);
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .payPalClient(payPalClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        PayPalFlowStartedCallback callback = mock(PayPalFlowStartedCallback.class);
        sut.tokenizePayPalRequest(activity, callback);

        verify(payPalClient).tokenizePayPalAccount(same(activity), same(payPalRequest), same(callback));
    }

    @Test
    public void tokenizePayPalAccount_withPayPalVaultRequest_tokenizesPayPalWithVaultRequest() {
        Configuration configuration = mockConfiguration(true, false, false, false, false);
        PayPalVaultRequest payPalRequest = new PayPalVaultRequest();
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setPayPalRequest(payPalRequest);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        PayPalClient payPalClient = mock(PayPalClient.class);
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .payPalClient(payPalClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        PayPalFlowStartedCallback callback = mock(PayPalFlowStartedCallback.class);
        sut.tokenizePayPalRequest(activity, callback);

        verify(payPalClient).tokenizePayPalAccount(same(activity), same(payPalRequest), same(callback));
    }

    @Test
    public void tokenizeVenmoAccount_tokenizesVenmo() {
        Configuration configuration = mockConfiguration(false, true, false, false, false);
        VenmoRequest venmoRequest = new VenmoRequest(VenmoPaymentMethodUsage.SINGLE_USE);
        venmoRequest.setShouldVault(true);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setVenmoRequest(venmoRequest);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        VenmoClient venmoClient = mock(VenmoClient.class);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .venmoClient(venmoClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        VenmoTokenizeAccountCallback callback = mock(VenmoTokenizeAccountCallback.class);
        sut.tokenizeVenmoAccount(activity, callback);

        ArgumentCaptor<VenmoRequest> captor = ArgumentCaptor.forClass(VenmoRequest.class);
        verify(venmoClient).tokenizeVenmoAccount(same(activity), captor.capture(), same(callback));

        VenmoRequest request = captor.getValue();
        assertTrue(request.getShouldVault());
    }

    @Test
    public void tokenizeVenmoAccount_whenVenmoRequestNull_createsVenmoRequest() {
        Configuration configuration = mockConfiguration(false, true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        VenmoClient venmoClient = mock(VenmoClient.class);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .venmoClient(venmoClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        VenmoTokenizeAccountCallback callback = mock(VenmoTokenizeAccountCallback.class);
        sut.tokenizeVenmoAccount(activity, callback);

        ArgumentCaptor<VenmoRequest> captor = ArgumentCaptor.forClass(VenmoRequest.class);
        verify(venmoClient).tokenizeVenmoAccount(same(activity), captor.capture(), same(callback));

        VenmoRequest request = captor.getValue();
        assertEquals(VenmoPaymentMethodUsage.SINGLE_USE, request.getPaymentMethodUsage());
        assertFalse(request.getShouldVault());
    }

    @Test
    public void requestGooglePayPayment_requestsGooglePay() {
        Configuration configuration = mockConfiguration(false, false, false, true, false);

        GooglePayRequest googlePayRequest = new GooglePayRequest();
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setGooglePayRequest(googlePayRequest);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(configuration)
                .build();

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        GooglePayRequestPaymentCallback callback = mock(GooglePayRequestPaymentCallback.class);
        sut.requestGooglePayPayment(activity, callback);

        verify(googlePayClient).requestPayment(same(activity), same(googlePayRequest), same(callback));
    }

    @Test
    public void deletePaymentMethod_callsDeletePaymentMethod() {
        PaymentMethodClient paymentMethodClient = mock(PaymentMethodClient.class);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .paymentMethodClient(paymentMethodClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        CardNonce cardNonce = mock(CardNonce.class);
        DeletePaymentMethodNonceCallback callback = mock(DeletePaymentMethodNonceCallback.class);
        sut.deletePaymentMethod(activity, cardNonce, callback);

        verify(paymentMethodClient).deletePaymentMethod(same(activity), same(cardNonce), same(callback));
    }

    @Test
    public void tokenizeCard_forwardsInvocationToCardClient() {
        CardClient cardClient = mock(CardClient.class);
        DropInInternalClientParams params = new DropInInternalClientParams()
                .cardClient(cardClient);

        Card card = new Card();
        CardTokenizeCallback callback = mock(CardTokenizeCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.tokenizeCard(card, callback);

        verify(cardClient).tokenize(card, callback);
    }

    @Test
    public void fetchUnionPayCapabilities_forwardsInvocationToUnionPayClient() {
        UnionPayClient unionPayClient = mock(UnionPayClient.class);
        DropInInternalClientParams params = new DropInInternalClientParams()
                .unionPayClient(unionPayClient);

        String cardNumber = "4111111111111111";
        UnionPayFetchCapabilitiesCallback callback = mock(UnionPayFetchCapabilitiesCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.fetchUnionPayCapabilities(cardNumber, callback);

        verify(unionPayClient).fetchCapabilities(cardNumber, callback);
    }

    @Test
    public void enrollUnionPay_forwardsInvocationToUnionPayClient() {
        UnionPayClient unionPayClient = mock(UnionPayClient.class);
        DropInInternalClientParams params = new DropInInternalClientParams()
                .unionPayClient(unionPayClient);

        UnionPayCard unionPayCard = new UnionPayCard();
        UnionPayEnrollCallback callback = mock(UnionPayEnrollCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.enrollUnionPay(unionPayCard, callback);
    }

    @Test
    public void tokenizeUnionPay_forwardsInvocationToUnionPayClient() {
        UnionPayClient unionPayClient = mock(UnionPayClient.class);
        DropInInternalClientParams params = new DropInInternalClientParams()
                .unionPayClient(unionPayClient);

        UnionPayCard unionPayCard = new UnionPayCard();
        UnionPayTokenizeCallback callback = mock(UnionPayTokenizeCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.tokenizeUnionPay(unionPayCard, callback);
    }

    @Test
    public void getBrowserSwitchResult_forwardsInvocationToBraintreeClient() {
        BrowserSwitchResult browserSwitchResult = createSuccessfulBrowserSwitchResult();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        when(braintreeClient.getBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient);

        DropInInternalClient sut = new DropInInternalClient(params);
        assertSame(browserSwitchResult, sut.getBrowserSwitchResult(activity));
    }

    @Test
    public void deliverBrowserSwitchResult_whenPayPal_tokenizesResultAndCollectsData() {
        PayPalAccountNonce payPalAccountNonce = mock(PayPalAccountNonce.class);
        PayPalClient payPalClient = new MockPayPalClientBuilder()
                .browserSwitchResult(payPalAccountNonce)
                .build();

        DropInRequest dropInRequest = new DropInRequest();

        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("device data")
                .build();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .payPalClient(payPalClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.PAYPAL);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInRequest dropInRequest = new DropInRequest();

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .payPalClient(payPalClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.PAYPAL);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.deliverBrowserSwitchResult(activity, callback);

        verify(callback).onResult(null, browserSwitchError);
    }

    @Test
    public void deliverBrowserSwitchResult_whenThreeDSecure_tokenizesResultAndCollectsData() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResult threeDSecureResult = new ThreeDSecureResult();
        threeDSecureResult.setTokenizedCard(cardNonce);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .browserSwitchResult(threeDSecureResult)
                .build();

        DropInRequest dropInRequest = new DropInRequest();

        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("device data")
                .build();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.THREE_D_SECURE);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInRequest dropInRequest = new DropInRequest();

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        DropInResultCallback callback = mock(DropInResultCallback.class);
        BrowserSwitchResult browserSwitchResult = mock(BrowserSwitchResult.class);
        when(browserSwitchResult.getRequestCode()).thenReturn(BraintreeRequestCodes.THREE_D_SECURE);

        when(braintreeClient.deliverBrowserSwitchResult(activity)).thenReturn(browserSwitchResult);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.deliverBrowserSwitchResult(activity, callback);

        verify(callback).onResult(null, browserSwitchError);
    }

    @Test
    public void handleThreeDSecureResult_tokenizesResultAndCollectsData() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(
                new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
        ThreeDSecureResult threeDSecureResult = new ThreeDSecureResult();
        threeDSecureResult.setTokenizedCard(cardNonce);

        ThreeDSecureClient threeDSecureClient = new MockThreeDSecureClientBuilder()
                .activityResult(threeDSecureResult)
                .build();

        DropInRequest dropInRequest = new DropInRequest();

        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("device data")
                .build();

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        Intent intent = new Intent();
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
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

        DropInRequest dropInRequest = new DropInRequest();

        DataCollector dataCollector = mock(DataCollector.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .threeDSecureClient(threeDSecureClient);

        Intent intent = new Intent();
        DropInResultCallback callback = mock(DropInResultCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.handleThreeDSecureActivityResult(activity, 123, intent, callback);

        verify(callback).onResult(null, activityResultError);
    }

    @Test
    public void getVaultedPaymentMethods_forwardsConfigurationFetchError() {
        Exception configurationError = new Exception("configuration error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configurationError(configurationError)
                .build();

        DropInInternalClientParams params = new DropInInternalClientParams()
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.getVaultedPaymentMethods(activity, callback);

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

        DropInInternalClientParams params = new DropInInternalClientParams()
                .paymentMethodClient(paymentMethodClient)
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.getVaultedPaymentMethods(activity, callback);

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

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setGooglePayDisabled(true);

        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .paymentMethodClient(paymentMethodClient)
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.getVaultedPaymentMethods(activity, callback);

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
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .paymentMethodClient(paymentMethodClient)
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.getVaultedPaymentMethods(activity, callback);

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
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient)
                .paymentMethodClient(paymentMethodClient)
                .braintreeClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        DropInInternalClient sut = new DropInInternalClient(params);
        sut.getVaultedPaymentMethods(activity, callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals(0, paymentMethodNonces.size());
    }

    @Test
    public void onActivityResult_whenResultCodeVenmo_handlesVenmoResult() {
        VenmoClient venmoClient = mock(VenmoClient.class);

        DropInRequest dropInRequest = new DropInRequest();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .venmoClient(venmoClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        FragmentActivity activity = mock(FragmentActivity.class);
        Intent intent = mock(Intent.class);
        DropInResultCallback callback = mock(DropInResultCallback.class);
        sut.handleActivityResult(activity, BraintreeRequestCodes.VENMO, 1, intent, callback);

        verify(venmoClient).onActivityResult(same(activity), eq(1), same(intent), any(VenmoOnActivityResultCallback.class));
    }

    @Test
    public void onActivityResult_whenResultCodeGooglePay_handlesGooglePayResult() {
        GooglePayClient googlePayClient = mock(GooglePayClient.class);

        DropInRequest dropInRequest = new DropInRequest();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        FragmentActivity activity = mock(FragmentActivity.class);
        Intent intent = mock(Intent.class);
        DropInResultCallback callback = mock(DropInResultCallback.class);
        sut.handleActivityResult(activity, BraintreeRequestCodes.GOOGLE_PAY, 1, intent, callback);

        verify(googlePayClient).onActivityResult(eq(1), same(intent), any(GooglePayOnActivityResultCallback.class));
    }

    @Test
    public void onActivityResult_whenResultCodeThreeDSecure_handlesThreeDSecureResult() {
        ThreeDSecureClient threeDSecureClient = mock(ThreeDSecureClient.class);

        DropInRequest dropInRequest = new DropInRequest();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .threeDSecureClient(threeDSecureClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        FragmentActivity activity = mock(FragmentActivity.class);
        Intent intent = mock(Intent.class);
        DropInResultCallback callback = mock(DropInResultCallback.class);
        sut.handleActivityResult(activity, BraintreeRequestCodes.THREE_D_SECURE, 1, intent, callback);

        verify(threeDSecureClient).onActivityResult(eq(1), same(intent), any(ThreeDSecureResultCallback.class));
    }

    @Test
    public void handleGooglePayActivityResult_withPaymentMethodNonce_callsBackDropInResult() {
        PaymentMethodNonce paymentMethodNonce = mock(PaymentMethodNonce.class);
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .onActivityResultSuccess(paymentMethodNonce)
                .build();
        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("sample-data")
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .googlePayClient(googlePayClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        Intent data = mock(Intent.class);
        DropInResultCallback callback = mock(DropInResultCallback.class);
        sut.handleGooglePayActivityResult(activity, 1, data, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertEquals(paymentMethodNonce, result.getPaymentMethodNonce());
    }

    @Test
    public void handleGooglePayActivityResult_withError_callsBackError() {
        Exception error = new Exception("Google Pay error");
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .onActivityResultError(error)
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .googlePayClient(googlePayClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        Intent data = mock(Intent.class);
        DropInResultCallback callback = mock(DropInResultCallback.class);
        sut.handleGooglePayActivityResult(activity, 1, data, callback);

        verify(callback).onResult((DropInResult) isNull(), same(error));
    }

    @Test
    public void handleVenmoActivityResult_withVenmoAccountNonce_callsBackDropInResult() {
        VenmoAccountNonce venmoAccountNonce = mock(VenmoAccountNonce.class);
        VenmoClient venmoClient = new MockVenmoClientBuilder()
                .onActivityResultSuccess(venmoAccountNonce)
                .build();
        DataCollector dataCollector = new MockDataCollectorBuilder()
                .collectDeviceDataSuccess("sample-data")
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .dataCollector(dataCollector)
                .venmoClient(venmoClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        Intent data = mock(Intent.class);
        DropInResultCallback callback = mock(DropInResultCallback.class);
        sut.handleVenmoActivityResult(activity, 1, data, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertEquals(venmoAccountNonce, result.getPaymentMethodNonce());
    }

    @Test
    public void handleVenmoActivityResult_withVenmoError_callsBackError() {
        Exception error = new Exception("Venmo error");
        VenmoClient venmoClient = new MockVenmoClientBuilder()
                .onActivityResultError(error)
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        DropInInternalClientParams params = new DropInInternalClientParams()
                .dropInRequest(dropInRequest)
                .venmoClient(venmoClient);

        DropInInternalClient sut = new DropInInternalClient(params);

        Intent data = mock(Intent.class);
        DropInResultCallback callback = mock(DropInResultCallback.class);
        sut.handleVenmoActivityResult(activity, 1, data, callback);

        verify(callback).onResult((DropInResult) isNull(), same(error));
    }

    private Configuration mockConfiguration(
            boolean paypalEnabled,
            boolean venmoEnabled,
            boolean cardEnabled,
            boolean googlePayEnabled,
            boolean unionPayEnabled
    ) {
        Configuration configuration = mock(Configuration.class);

        when(configuration.isPayPalEnabled()).thenReturn(paypalEnabled);
        when(configuration.isVenmoEnabled()).thenReturn(venmoEnabled);
        when(configuration.isGooglePayEnabled()).thenReturn(googlePayEnabled);
        when(configuration.isUnionPayEnabled()).thenReturn(unionPayEnabled);

        if (cardEnabled) {
            when(configuration.getSupportedCardTypes()).thenReturn(Collections.singletonList(DropInPaymentMethod.VISA.name()));
        }

        return configuration;
    }

    private static BrowserSwitchResult createSuccessfulBrowserSwitchResult() {
        int requestCode = 123;
        Uri url = Uri.parse("www.example.com");
        String returnUrlScheme = "sample-scheme";
        BrowserSwitchRequest browserSwitchRequest = new BrowserSwitchRequest(
                requestCode, url, new JSONObject(), returnUrlScheme, true);
        return new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, browserSwitchRequest);
    }
}