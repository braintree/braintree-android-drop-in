package com.braintreepayments.api;

import android.content.SharedPreferences;

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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    public void shouldRequestThreeDSecureVerification_whenGooglePayEnabled_andGooglePayNonNetworkTokenized_returnsTrue() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder().build();
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
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE));

        DropInClient sut = new DropInClient(params);
        ShouldRequestThreeDSecureVerification callback = mock(ShouldRequestThreeDSecureVerification.class);

        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback);
        verify(callback).onResult(true);
    }

    @Test
    public void shouldRequestThreeDSecureVerification_whenGooglePayEnabled_andGooglePayNetworkTokenized_returnsTrue() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder().build();
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
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        PaymentMethodNonce googlePayCardNonce = GooglePayCardNonce.fromJSON(
                new JSONObject(Fixtures.GOOGLE_PAY_NETWORK_TOKENIZED_RESPONSE));

        DropInClient sut = new DropInClient(params);
        ShouldRequestThreeDSecureVerification callback = mock(ShouldRequestThreeDSecureVerification.class);

        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback);
        verify(callback).onResult(false);
    }

    // TODO: the Google Pay filtering happens in PaymentMethodClient in core - is this test needed?
    @Test
    public void getVaultedPaymentMethods_filtersOutGooglePayNonces() throws JSONException {
        GooglePayClient googlePayClient = new MockGooglePayClientBuilder()
                .isReadyToPaySuccess(true)
                .build();
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .sendGETSuccessfulResponse(Fixtures.GET_PAYMENT_METHODS_GOOGLE_PAY_RESPONSE)
                .build();

        DropInClientParams params = new DropInClientParams()
                .dropInRequest(new DropInRequest())
                .braintreeClient(braintreeClient)
                .googlePayClient(googlePayClient);

        DropInClient sut = new DropInClient(params);
        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);

        sut.getVaultedPaymentMethods(activity, true, callback);
        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals(2, paymentMethodNonces.size());
    }

    // TODO: Move fetchDropInResult tests to DropInClient#fetchMostRecentPaymentMethod
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
    public void fetchMostRecentPaymentMethod_callsBackWithResultIfLastUsedPaymentMethodTypeWasPayWithGoogle()
            throws JSONException {
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
    public void fetchDropInResult_doesNotCallBackWithPayWithGoogleIfPayWithGoogleIsNotAvailable()
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
//
//    @Test
//    public void fetchDropInResult_resetsBraintreeListenersWhenPayWithGoogleResultIsReturned()
//            throws InvalidArgumentException, InterruptedException {
//        BraintreeSharedPreferences.getSharedPreferences(mActivity)
//                .edit()
//                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
//                        DropInPaymentMethodType.GOOGLE_PAYMENT.getCanonicalName())
//                .commit();
//        googlePaymentReadyToPay(true);
//        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build()));
//        BraintreeErrorListener errorListener = new BraintreeErrorListener() {
//            @Override
//            public void onError(Exception error) {}
//        };
//        fragment.addListener(errorListener);
//        PaymentMethodNoncesUpdatedListener paymentMethodListener = new PaymentMethodNoncesUpdatedListener() {
//            @Override
//            public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {}
//        };
//        fragment.addListener(paymentMethodListener);
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                fail("onError called");
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                assertEquals(DropInPaymentMethodType.GOOGLE_PAYMENT, result.getPaymentMethodType());
//                mCountDownLatch.countDown();
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//        List<BraintreeListener> listeners = fragment.getListeners();
//        assertEquals(2, listeners.size());
//        assertTrue(listeners.contains(errorListener));
//        assertTrue(listeners.contains(paymentMethodListener));
//    }
//
//    @Test
//    public void fetchDropInResult_clearsListenersWhenPayWithGoogleResultIsReturned()
//            throws InvalidArgumentException, InterruptedException {
//        BraintreeSharedPreferences.getSharedPreferences(mActivity)
//                .edit()
//                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
//                        DropInPaymentMethodType.GOOGLE_PAYMENT.getCanonicalName())
//                .commit();
//        googlePaymentReadyToPay(true);
//        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build()));
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                fail("onError called");
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                assertEquals(DropInPaymentMethodType.GOOGLE_PAYMENT, result.getPaymentMethodType());
//                mCountDownLatch.countDown();
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//        List<BraintreeListener> listeners = fragment.getListeners();
//        assertEquals(0, listeners.size());
//    }
//
//    @Test
//    public void fetchDropInResult_callsListenerWithErrorIfBraintreeFragmentSetupFails()
//            throws InterruptedException {
//        mActivity = spy(mActivity);
//        FragmentManager fragmentManager = mock(FragmentManager.class);
//        doThrow(new IllegalStateException("IllegalState")).when(fragmentManager).beginTransaction();
//        when(mActivity.getSupportFragmentManager()).thenReturn(fragmentManager);
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                assertEquals("IllegalState", exception.getMessage());
//                mCountDownLatch.countDown();
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                fail("onResult called");
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//    }
//
//    @Test
//    public void fetchDropInResult_callsListenerWithErrorWhenErrorIsPosted()
//            throws InterruptedException, InvalidArgumentException {
//        setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build())
//                .errorResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, 404,
//                        "No payment methods found"));
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                assertEquals("No payment methods found", exception.getMessage());
//                mCountDownLatch.countDown();
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                fail("onResult called");
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//    }
//
//    @Test
//    public void fetchDropInResult_resetsBraintreeListenersWhenErrorIsPosted()
//            throws InvalidArgumentException, InterruptedException {
//        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build())
//                .errorResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, 404,
//                        "No payment methods found"));
//        BraintreeErrorListener errorListener = new BraintreeErrorListener() {
//            @Override
//            public void onError(Exception error) {}
//        };
//        fragment.addListener(errorListener);
//        PaymentMethodNoncesUpdatedListener paymentMethodListener = new PaymentMethodNoncesUpdatedListener() {
//            @Override
//            public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {}
//        };
//        fragment.addListener(paymentMethodListener);
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                assertEquals("No payment methods found", exception.getMessage());
//                mCountDownLatch.countDown();
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                fail("onResult called");
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//        List<BraintreeListener> listeners = fragment.getListeners();
//        assertEquals(2, listeners.size());
//        assertTrue(listeners.contains(errorListener));
//        assertTrue(listeners.contains(paymentMethodListener));
//    }
//
//    @Test
//    public void fetchDropInResult_clearsListenersWhenErrorIsPosted()
//            throws InvalidArgumentException, InterruptedException {
//        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build())
//                .errorResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, 404,
//                        "No payment methods found"));
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                assertEquals("No payment methods found", exception.getMessage());
//                mCountDownLatch.countDown();
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                fail("onResult called");
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//        assertEquals(0, fragment.getListeners().size());
//    }
//
//    @Test
//    public void fetchDropInResult_callsListenerWithResultWhenThereIsAPaymentMethod()
//            throws InvalidArgumentException, InterruptedException {
//        setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build())
//                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, Fixtures.GET_PAYMENT_METHODS_TWO_CARDS_RESPONSE));
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                fail("onError called");
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                assertEquals(DropInPaymentMethodType.VISA, result.getPaymentMethodType());
//                assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
//                mCountDownLatch.countDown();
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//    }
//
//    @Test
//    public void fetchDropInResult_callsListenerWithNullResultWhenThereAreNoPaymentMethods()
//            throws InvalidArgumentException, InterruptedException {
//        setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build())
//                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, Fixtures.GET_PAYMENT_METHODS_EMPTY_RESPONSE));
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                fail("onError called");
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                assertNull(result.getPaymentMethodType());
//                assertNull(result.getPaymentMethodNonce());
//                mCountDownLatch.countDown();
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//    }
//
//    @Test
//    public void fetchDropInResult_resetsBraintreeListenersWhenResultIsReturned()
//            throws InvalidArgumentException, InterruptedException {
//        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build())
//                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, Fixtures.GET_PAYMENT_METHODS_TWO_CARDS_RESPONSE));
//        BraintreeErrorListener errorListener = new BraintreeErrorListener() {
//            @Override
//            public void onError(Exception error) {}
//        };
//        fragment.addListener(errorListener);
//        PaymentMethodNoncesUpdatedListener paymentMethodListener = new PaymentMethodNoncesUpdatedListener() {
//            @Override
//            public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {}
//        };
//        fragment.addListener(paymentMethodListener);
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                fail("onError called");
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                assertEquals(DropInPaymentMethodType.VISA, result.getPaymentMethodType());
//                mCountDownLatch.countDown();
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//        List<BraintreeListener> listeners = fragment.getListeners();
//        assertEquals(2, listeners.size());
//        assertTrue(listeners.contains(errorListener));
//        assertTrue(listeners.contains(paymentMethodListener));
//    }
//
//    @Test
//    public void fetchDropInResult_clearsListenersWhenResultIsReturned()
//            throws InvalidArgumentException, InterruptedException {
//        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
//                .configuration(new TestConfigurationBuilder().build())
//                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, Fixtures.GET_PAYMENT_METHODS_TWO_CARDS_RESPONSE));
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                fail("onError called");
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                assertEquals(DropInPaymentMethodType.VISA, result.getPaymentMethodType());
//                mCountDownLatch.countDown();
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN), listener);
//
//        mCountDownLatch.await();
//        List<BraintreeListener> listeners = fragment.getListeners();
//        assertEquals(0, listeners.size());
//    }
}