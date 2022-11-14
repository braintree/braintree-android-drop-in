package com.braintreepayments.api;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class DropInClientUnitTest {

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
        DropInClient sut = new DropInClient(ApplicationProvider.getApplicationContext(), Fixtures.TOKENIZATION_KEY, new DropInRequest());
        assertEquals(IntegrationType.DROP_IN, sut.braintreeClient.getIntegrationType());
    }

    @Test
    public void publicConstructor_setsBraintreeClientWithSessionId() {
        DropInClient sut = new DropInClient(ApplicationProvider.getApplicationContext(), Fixtures.TOKENIZATION_KEY, new DropInRequest());
        assertNotNull(sut.braintreeClient.getSessionId());
    }

    @Test
    public void getAuthorization_forwardsInvocationToBraintreeClient() {
        Authorization authorization = mock(Authorization.class);
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(authorization)
                .build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient);

        DropInClient sut = new DropInClient(params);

        AuthorizationCallback callback = mock(AuthorizationCallback.class);
        sut.getAuthorization(callback);
        verify(braintreeClient).getAuthorization(callback);
    }

    @Test
    public void fetchMostRecentPaymentMethod_forwardsAuthorizationFetchErrors() {
        Exception authError = new Exception("auth error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationError(authError)
                .build();

        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(new DropInRequest());

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        verify(callback).onResult(null, authError);
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithErrorIfInvalidClientTokenWasUsed() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.TOKENIZATION_KEY))
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
    public void fetchMostRecentPaymentMethod_callsBackWithResultIfLastUsedPaymentMethodTypeWasPayWithGoogle() throws JSONException {

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .dropInSharedPreferences(dropInSharedPreferences)
                .googlePayClient(googlePayClient);

        when(
                dropInSharedPreferences.getLastUsedPaymentMethod(activity)
        ).thenReturn(DropInPaymentMethod.GOOGLE_PAY);

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertEquals(DropInPaymentMethod.GOOGLE_PAY, result.getPaymentMethodType());
        assertNull(result.getPaymentMethodNonce());
    }

    @Test
    public void fetchMostRecentPaymentMethod_doesNotCallBackWithPayWithGoogleIfPayWithGoogleIsNotAvailable() throws JSONException {

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(false)
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
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
                .googlePayClient(googlePayClient)
                .dropInSharedPreferences(dropInSharedPreferences);

        when(
                dropInSharedPreferences.getLastUsedPaymentMethod(activity)
        ).thenReturn(DropInPaymentMethod.GOOGLE_PAY);

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertEquals(DropInPaymentMethod.VISA, result.getPaymentMethodType());
        assertNotNull(result.getPaymentMethodNonce());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithErrorOnGetPaymentMethodsError() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .build();

        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesError(new BraintreeException("Error occurred"))
                .build();
        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .paymentMethodClient(paymentMethodClient)
                .dropInSharedPreferences(dropInSharedPreferences)
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
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
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
                .dropInSharedPreferences(dropInSharedPreferences)
                .paymentMethodClient(paymentMethodClient);

        DropInClient sut = new DropInClient(params);

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertEquals(DropInPaymentMethod.VISA, result.getPaymentMethodType());
        assertNotNull(result.getPaymentMethodNonce());
        assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithNullResultWhenThereAreNoPaymentMethods() throws JSONException {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        ArrayList<PaymentMethodNonce> paymentMethods = new ArrayList<>();

        PaymentMethodClient paymentMethodClient = new MockPaymentMethodClientBuilder()
                .getPaymentMethodNoncesSuccess(paymentMethods)
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .dropInSharedPreferences(dropInSharedPreferences)
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
    public void legacy_launchDropInForResult_withListener_forwardsAuthorizationFetchErrors() {
        Exception authError = new Exception("auth error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("session-id")
                .authorizationError(authError)
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest);

        DropInClient sut = new DropInClient(params);

        DropInListener listener = mock(DropInListener.class);
        sut.setListener(listener);

        FragmentActivity activity = mock(FragmentActivity.class);
        sut.launchDropInForResult(activity, 123);

        verify(listener).onDropInFailure(authError);
    }

    @Test
    public void legacy_launchDropInForResult_withoutListener_launchesDropInActivityWithError() {
        Exception authError = new Exception("auth error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("session-id")
                .authorizationError(authError)
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient)
                .dropInRequest(dropInRequest);

        DropInClient sut = new DropInClient(params);

        FragmentActivity activity = mock(FragmentActivity.class);
        sut.launchDropInForResult(activity, 123);

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(captor.capture(), eq(123));

        Intent intent = captor.getValue();
        Exception error = (Exception) intent.getSerializableExtra(DropInClient.EXTRA_AUTHORIZATION_ERROR);
        assertEquals("auth error", error.getMessage());
    }

    @Test
    public void legacy_launchDropInForResult_launchesDropInActivityWithIntentExtras() {
        Authorization authorization = mock(Authorization.class);
        when(authorization.toString()).thenReturn("authorization");

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("session-id")
                .authorizationSuccess(authorization)
                .build();

        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setVaultManagerEnabled(true);

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

        Bundle bundle = intent.getParcelableExtra(DropInClient.EXTRA_CHECKOUT_REQUEST_BUNDLE);
        bundle.setClassLoader(DropInRequest.class.getClassLoader());
        DropInRequest dropInRequestExtra = bundle.getParcelable(DropInClient.EXTRA_CHECKOUT_REQUEST);
        assertTrue(dropInRequestExtra.isVaultManagerEnabled());
    }

    @Test
    public void onDropInResult_whenResultHasNoError_notifiesListenerOfSuccessViaCallback() {
        DropInClientParams params = new DropInClientParams();
        DropInClient sut = new DropInClient(params);

        DropInListener listener = mock(DropInListener.class);
        sut.setListener(listener);

        DropInResult dropInResult = new DropInResult();
        sut.onDropInResult(dropInResult);

        verify(listener).onDropInSuccess(dropInResult);
    }

    @Test
    public void onDropInResult_whenResultHasError_notifiesListenerOfErrorViaCallback() {
        DropInClientParams params = new DropInClientParams();
        DropInClient sut = new DropInClient(params);

        DropInListener listener = mock(DropInListener.class);
        sut.setListener(listener);

        DropInResult dropInResult = new DropInResult();
        Exception error = new Exception("sample error");
        dropInResult.setError(error);

        sut.onDropInResult(dropInResult);
        verify(listener).onDropInFailure(error);
    }

    @Test
    public void onDropInResult_whenResultIsNull_doesNothing() {
        DropInClientParams params = new DropInClientParams();
        DropInClient sut = new DropInClient(params);

        DropInListener listener = mock(DropInListener.class);
        sut.setListener(listener);

        sut.onDropInResult(null);
        verifyZeroInteractions(listener);
    }

    @Test
    public void invalidateClientToken_forwardsInvocationIntoBraintreeClient() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        DropInClientParams params = new DropInClientParams()
                .braintreeClient(braintreeClient);
        DropInClient sut = new DropInClient(params);

        sut.invalidateClientToken();
        verify(braintreeClient).invalidateClientToken();
    }
}