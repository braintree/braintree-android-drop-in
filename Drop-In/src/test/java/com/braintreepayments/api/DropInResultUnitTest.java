package com.braintreepayments.api;

import android.os.Parcel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.braintreepayments.api.test.Fixtures;
import com.braintreepayments.api.test.TestConfigurationBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.braintreepayments.api.UnitTestFixturesHelper.base64EncodedClientTokenFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(RobolectricTestRunner.class)
public class DropInResultUnitTest {

    private AppCompatActivity mActivity;
    private CountDownLatch mCountDownLatch;

    @Before
    public void setup() {
        mActivity = Robolectric.buildActivity(FragmentTestActivity.class).setup().get();
        mCountDownLatch = new CountDownLatch(1);
    }

    @Test
    public void paymentMethodNonce_setsPaymentMethodTypeAndNonce() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInResult result = new DropInResult()
                .paymentMethodNonce(cardNonce);

        assertEquals(DropInPaymentMethodType.VISA, result.getPaymentMethodType());
        assertEquals(cardNonce, result.getPaymentMethodNonce());
    }

    @Test
    public void paymentMethodNonce_isNullable() {
        DropInResult result = new DropInResult()
                .paymentMethodNonce(null);

        assertNull(result.getPaymentMethodType());
        assertNull(result.getPaymentMethodNonce());
    }

    @Test
    public void deviceData_setsDeviceData() {
        DropInResult result = new DropInResult()
                .deviceData("device_data");

        assertEquals("device_data", result.getDeviceData());
    }

    @Test
    public void deviceData_isNullable() {
        DropInResult result = new DropInResult()
                .deviceData(null);

        assertNull(result.getDeviceData());
    }

    @Test
    public void isParcelable() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInResult result = new DropInResult()
                .paymentMethodNonce(cardNonce)
                .deviceData("device_data");
        Parcel parcel = Parcel.obtain();
        result.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        DropInResult parceled = DropInResult.CREATOR.createFromParcel(parcel);

        assertEquals(DropInPaymentMethodType.VISA, parceled.getPaymentMethodType());
        assertEquals(cardNonce.getString(), parceled.getPaymentMethodNonce().getString());
        assertEquals("device_data", parceled.getDeviceData());
    }

    // TODO: Move fetchDropInResult tests to DropInClient#fetchMostRecentPaymentMethod
//    @Test
//    public void fetchDropInResult_callsListenerWithErrorIfInvalidClientTokenWasUsed()
//            throws InterruptedException {
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                assertEquals("Authorization provided is invalid: not a client token", exception.getMessage());
//                mCountDownLatch.countDown();
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                fail("onResult called");
//            }
//        };
//
//        DropInResult.fetchDropInResult(mActivity, "not a client token", listener);
//
//        mCountDownLatch.await();
//    }
//
//    @Test
//    public void fetchDropInResult_callsListenerWithResultIfLastUsedPaymentMethodTypeWasPayWithGoogle()
//            throws InterruptedException {
//        BraintreeSharedPreferences.getSharedPreferences(mActivity)
//                .edit()
//                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
//                        DropInPaymentMethodType.GOOGLE_PAYMENT.getCanonicalName())
//                .commit();
//        googlePaymentReadyToPay(true);
//        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
//            @Override
//            public void onError(Exception exception) {
//                fail("onError called");
//            }
//
//            @Override
//            public void onResult(DropInResult result) {
//                assertEquals(DropInPaymentMethodType.GOOGLE_PAYMENT, result.getPaymentMethodType());
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
//    public void fetchDropInResult_doesNotCallListenerWithPayWithGoogleIfPayWithGoogleIsNotAvailable()
//            throws InterruptedException, InvalidArgumentException {
//        BraintreeSharedPreferences.getSharedPreferences(mActivity)
//                .edit()
//                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
//                        DropInPaymentMethodType.GOOGLE_PAYMENT.getCanonicalName())
//                .commit();
//        googlePaymentReadyToPay(false);
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
//        DropInResult.fetchDropInResult(mActivity, Fixtures.CLIENT_TOKEN, listener);
//
//        mCountDownLatch.await();
//    }
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
