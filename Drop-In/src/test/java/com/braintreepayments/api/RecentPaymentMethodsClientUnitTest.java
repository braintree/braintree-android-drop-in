package com.braintreepayments.api;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ApplicationProvider;
import androidx.work.testing.WorkManagerTestInitHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class RecentPaymentMethodsClientUnitTest {

    private FragmentActivity activity;

    private Context context;
    private DropInSharedPreferences dropInSharedPreferences;

    @Before
    public void beforeEach() {
        context = ApplicationProvider.getApplicationContext();
        dropInSharedPreferences = mock(DropInSharedPreferences.class);

        ActivityController<FragmentActivity> activityController =
                Robolectric.buildActivity(FragmentActivity.class);
        activity = activityController.get();

        // This suppresses errors from WorkManager initialization within BraintreeClient initialization (AnalyticsClient)
        WorkManagerTestInitHelper.initializeTestWorkManager(context);
    }

    @Test
    public void fetchMostRecentPaymentMethod_forwardsAuthorizationFetchErrors() {
        Exception authError = new Exception("auth error");
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationError(authError)
                .build();

        RecentPaymentMethodsClient sut = new RecentPaymentMethodsClient(
                braintreeClient,
                new GooglePayClient(braintreeClient),
                new PaymentMethodClient(braintreeClient),
                dropInSharedPreferences
        );

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        verify(callback).onResult(null, authError);
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithErrorIfInvalidClientTokenWasUsed() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.TOKENIZATION_KEY))
                .build();

        RecentPaymentMethodsClient sut = new RecentPaymentMethodsClient(
                braintreeClient,
                new GooglePayClient(braintreeClient),
                new PaymentMethodClient(braintreeClient),
                dropInSharedPreferences
        );

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<InvalidArgumentException> captor = ArgumentCaptor.forClass(InvalidArgumentException.class);
        verify(callback).onResult((DropInResult) isNull(), captor.capture());

        InvalidArgumentException exception = captor.getValue();
        assertEquals("DropInClient#fetchMostRecentPaymentMethods() must be called with a client token", exception.getMessage());
    }

    @Test
    public void fetchMostRecentPaymentMethod_callsBackWithResultIfLastUsedPaymentMethodTypeWasPayWithGoogle() throws JSONException, JSONException {

        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
                .build();

        RecentPaymentMethodsClient sut = new RecentPaymentMethodsClient(
                braintreeClient,
                googlePayClient,
                new PaymentMethodClient(braintreeClient),
                dropInSharedPreferences
        );

        when(
                dropInSharedPreferences.getLastUsedPaymentMethod()
        ).thenReturn(DropInPaymentMethod.GOOGLE_PAY);

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

        when(
                dropInSharedPreferences.getLastUsedPaymentMethod()
        ).thenReturn(DropInPaymentMethod.GOOGLE_PAY);

        RecentPaymentMethodsClient sut = new RecentPaymentMethodsClient(
                braintreeClient,
                googlePayClient,
                paymentMethodClient,
                dropInSharedPreferences
        );

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

        RecentPaymentMethodsClient sut = new RecentPaymentMethodsClient(
                braintreeClient,
                new GooglePayClient(braintreeClient),
                paymentMethodClient,
                dropInSharedPreferences
        );

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

        RecentPaymentMethodsClient sut = new RecentPaymentMethodsClient(
                braintreeClient,
                new GooglePayClient(braintreeClient),
                paymentMethodClient,
                dropInSharedPreferences
        );

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

        RecentPaymentMethodsClient sut = new RecentPaymentMethodsClient(
                braintreeClient,
                new GooglePayClient(braintreeClient),
                paymentMethodClient,
                dropInSharedPreferences
        );

        FetchMostRecentPaymentMethodCallback callback = mock(FetchMostRecentPaymentMethodCallback.class);
        sut.fetchMostRecentPaymentMethod(activity, callback);

        ArgumentCaptor<DropInResult> captor = ArgumentCaptor.forClass(DropInResult.class);
        verify(callback).onResult(captor.capture(), (Exception) isNull());

        DropInResult result = captor.getValue();
        assertNull(result.getPaymentMethodType());
        assertNull(result.getPaymentMethodNonce());
    }
}
