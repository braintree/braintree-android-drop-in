package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Parcel;

import com.braintreepayments.api.AndroidPay;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.ConfigurationManagerTestUtils;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeListener;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.PaymentMethodNoncesUpdatedListener;
import com.braintreepayments.api.internal.BraintreeHttpClient;
import com.braintreepayments.api.internal.BraintreeSharedPreferences;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.test.FragmentTestActivity;
import com.braintreepayments.api.test.TestConfigurationBuilder;

import org.json.JSONException;
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
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.braintreepayments.api.BraintreeFragmentTestUtils.setHttpClient;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
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

@RunWith(RobolectricGradleTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest(AndroidPay.class)
public class DropInResultUnitTest {

    @Rule
    public PowerMockRule mPowerMockRule = new PowerMockRule();

    private Activity mActivity;
    private CountDownLatch mCountDownLatch;

    @Before
    public void setup() {
        mActivity = Robolectric.buildActivity(FragmentTestActivity.class).setup().get();
        mCountDownLatch = new CountDownLatch(1);
    }

    @Test
    public void paymentMethodNonce_setsPaymentMethodTypeAndNonce() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json"));
        DropInResult result = new DropInResult()
                .paymentMethodNonce(cardNonce);

        assertEquals(PaymentMethodType.VISA, result.getPaymentMethodType());
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
        CardNonce cardNonce = CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json"));
        DropInResult result = new DropInResult()
                .paymentMethodNonce(cardNonce)
                .deviceData("device_data");
        Parcel parcel = Parcel.obtain();
        result.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        DropInResult parceled = DropInResult.CREATOR.createFromParcel(parcel);

        assertEquals(PaymentMethodType.VISA, parceled.getPaymentMethodType());
        assertEquals(cardNonce.getNonce(), parceled.getPaymentMethodNonce().getNonce());
        assertEquals("device_data", parceled.getDeviceData());
    }

    @Test
    public void fetchDropInResult_callsListenerWithErrorIfInvalidClientTokenWasUsed()
            throws InterruptedException {
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                assertEquals("Client token was invalid", exception.getMessage());
                mCountDownLatch.countDown();
            }

            @Override
            public void onResult(DropInResult result) {
                fail("onResult called");
            }
        };

        DropInResult.fetchDropInResult(mActivity, "not a client token", listener);

        mCountDownLatch.await();
    }

    @Test
    public void fetchDropInResult_callsListenerWithResultIfLastUsedPaymentMethodTypeWasAndroidPay()
            throws InterruptedException {
        BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .edit()
                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
                        PaymentMethodType.ANDROID_PAY.getCanonicalName())
                .commit();
        androidPayReadyToPay(true);
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                fail("onError called");
            }

            @Override
            public void onResult(DropInResult result) {
                assertEquals(PaymentMethodType.ANDROID_PAY, result.getPaymentMethodType());
                assertNull(result.getPaymentMethodNonce());
                mCountDownLatch.countDown();
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
    }

    @Test
    public void fetchDropInResult_doesNotCallListenerWithAndroidPayIfAndroidPayIsNotAvailable()
            throws InterruptedException, InvalidArgumentException {
        BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .edit()
                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
                        PaymentMethodType.ANDROID_PAY.getCanonicalName())
                .commit();
        androidPayReadyToPay(false);
        setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json")));
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                fail("onError called");
            }

            @Override
            public void onResult(DropInResult result) {
                assertEquals(PaymentMethodType.VISA, result.getPaymentMethodType());
                assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
                mCountDownLatch.countDown();
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
    }

    @Test
    public void fetchDropInResult_resetsBraintreeListenersWhenAndroidPayResultIsReturned()
            throws InvalidArgumentException, InterruptedException {
        BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .edit()
                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
                        PaymentMethodType.ANDROID_PAY.getCanonicalName())
                .commit();
        androidPayReadyToPay(true);
        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build()));
        BraintreeErrorListener errorListener = new BraintreeErrorListener() {
            @Override
            public void onError(Exception error) {}
        };
        fragment.addListener(errorListener);
        PaymentMethodNoncesUpdatedListener paymentMethodListener = new PaymentMethodNoncesUpdatedListener() {
            @Override
            public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {}
        };
        fragment.addListener(paymentMethodListener);
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                fail("onError called");
            }

            @Override
            public void onResult(DropInResult result) {
                assertEquals(PaymentMethodType.ANDROID_PAY, result.getPaymentMethodType());
                mCountDownLatch.countDown();
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
        List<BraintreeListener> listeners = fragment.getListeners();
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(errorListener));
        assertTrue(listeners.contains(paymentMethodListener));
    }

    @Test
    public void fetchDropInResult_clearsListenersWhenAndroidPayResultIsReturned()
            throws InvalidArgumentException, InterruptedException {
        BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .edit()
                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
                        PaymentMethodType.ANDROID_PAY.getCanonicalName())
                .commit();
        androidPayReadyToPay(true);
        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build()));
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                fail("onError called");
            }

            @Override
            public void onResult(DropInResult result) {
                assertEquals(PaymentMethodType.ANDROID_PAY, result.getPaymentMethodType());
                mCountDownLatch.countDown();
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
        List<BraintreeListener> listeners = fragment.getListeners();
        assertEquals(0, listeners.size());
    }

    @Test
    public void fetchDropInResult_callsListenerWithErrorIfBraintreeFragmentSetupFails()
            throws InterruptedException {
        mActivity = spy(mActivity);
        FragmentManager fragmentManager = mock(FragmentManager.class);
        doThrow(new IllegalStateException("IllegalState")).when(fragmentManager).beginTransaction();
        when(mActivity.getFragmentManager()).thenReturn(fragmentManager);
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                assertEquals("IllegalState", exception.getMessage());
                mCountDownLatch.countDown();
            }

            @Override
            public void onResult(DropInResult result) {
                fail("onResult called");
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
    }

    @Test
    public void fetchDropInResult_callsListenerWithErrorWhenErrorIsPosted()
            throws InterruptedException, InvalidArgumentException {
        setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .errorResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, 404,
                        "No payment methods found"));
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                assertEquals("No payment methods found", exception.getMessage());
                mCountDownLatch.countDown();
            }

            @Override
            public void onResult(DropInResult result) {
                fail("onResult called");
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
    }

    @Test
    public void fetchDropInResult_resetsBraintreeListenersWhenErrorIsPosted()
            throws InvalidArgumentException, InterruptedException {
        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .errorResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, 404,
                        "No payment methods found"));
        BraintreeErrorListener errorListener = new BraintreeErrorListener() {
            @Override
            public void onError(Exception error) {}
        };
        fragment.addListener(errorListener);
        PaymentMethodNoncesUpdatedListener paymentMethodListener = new PaymentMethodNoncesUpdatedListener() {
            @Override
            public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {}
        };
        fragment.addListener(paymentMethodListener);
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                assertEquals("No payment methods found", exception.getMessage());
                mCountDownLatch.countDown();
            }

            @Override
            public void onResult(DropInResult result) {
                fail("onResult called");
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
        List<BraintreeListener> listeners = fragment.getListeners();
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(errorListener));
        assertTrue(listeners.contains(paymentMethodListener));
    }

    @Test
    public void fetchDropInResult_clearsListenersWhenErrorIsPosted()
            throws InvalidArgumentException, InterruptedException {
        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .errorResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, 404,
                        "No payment methods found"));
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                assertEquals("No payment methods found", exception.getMessage());
                mCountDownLatch.countDown();
            }

            @Override
            public void onResult(DropInResult result) {
                fail("onResult called");
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
        assertEquals(0, fragment.getListeners().size());
    }

    @Test
    public void fetchDropInResult_callsListenerWithResultWhenThereIsAPaymentMethod()
            throws InvalidArgumentException, InterruptedException {
        setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json")));
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                fail("onError called");
            }

            @Override
            public void onResult(DropInResult result) {
                assertEquals(PaymentMethodType.VISA, result.getPaymentMethodType());
                assertEquals("11", ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
                mCountDownLatch.countDown();
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
    }

    @Test
    public void fetchDropInResult_callsListenerWithNullResultWhenThereAreNoPaymentMethods()
            throws InvalidArgumentException, InterruptedException {
        setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_empty_response.json")));
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                fail("onError called");
            }

            @Override
            public void onResult(DropInResult result) {
                assertNull(result.getPaymentMethodType());
                assertNull(result.getPaymentMethodNonce());
                mCountDownLatch.countDown();
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
    }

    @Test
    public void fetchDropInResult_resetsBraintreeListenersWhenResultIsReturned()
            throws InvalidArgumentException, InterruptedException {
        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json")));
        BraintreeErrorListener errorListener = new BraintreeErrorListener() {
            @Override
            public void onError(Exception error) {}
        };
        fragment.addListener(errorListener);
        PaymentMethodNoncesUpdatedListener paymentMethodListener = new PaymentMethodNoncesUpdatedListener() {
            @Override
            public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {}
        };
        fragment.addListener(paymentMethodListener);
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                fail("onError called");
            }

            @Override
            public void onResult(DropInResult result) {
                assertEquals(PaymentMethodType.VISA, result.getPaymentMethodType());
                mCountDownLatch.countDown();
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
        List<BraintreeListener> listeners = fragment.getListeners();
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(errorListener));
        assertTrue(listeners.contains(paymentMethodListener));
    }

    @Test
    public void fetchDropInResult_clearsListenersWhenResultIsReturned()
            throws InvalidArgumentException, InterruptedException {
        BraintreeFragment fragment = setupFragment(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json")));
        DropInResult.DropInResultListener listener = new DropInResult.DropInResultListener() {
            @Override
            public void onError(Exception exception) {
                fail("onError called");
            }

            @Override
            public void onResult(DropInResult result) {
                assertEquals(PaymentMethodType.VISA, result.getPaymentMethodType());
                mCountDownLatch.countDown();
            }
        };

        DropInResult.fetchDropInResult(mActivity, stringFromFixture("client_token.json"), listener);

        mCountDownLatch.await();
        List<BraintreeListener> listeners = fragment.getListeners();
        assertEquals(0, listeners.size());
    }

    private BraintreeFragment setupFragment(BraintreeHttpClient httpClient) throws InvalidArgumentException {
        BraintreeFragment fragment = BraintreeFragment.newInstance(mActivity,
                stringFromFixture("client_token.json"));
        setHttpClient(fragment, httpClient);
        ConfigurationManagerTestUtils.setFetchingConfiguration(false);

        return fragment;
    }

    private void androidPayReadyToPay(final boolean readyToPay) {
        mockStatic(AndroidPay.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((BraintreeResponseListener<Boolean>) invocation.getArguments()[1])
                        .onResponse(readyToPay);
                return null;
            }
        }).when(AndroidPay.class);
        AndroidPay.isReadyToPay(any(BraintreeFragment.class), any(BraintreeResponseListener.class));
    }
}
