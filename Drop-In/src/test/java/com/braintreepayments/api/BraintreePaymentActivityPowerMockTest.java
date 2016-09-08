package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.google.android.gms.identity.intents.model.CountrySpecification;
import com.google.android.gms.wallet.Cart;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowActivity.IntentForResult;

import static com.braintreepayments.api.test.ReflectionHelper.setField;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ PayPal.class, Venmo.class, AndroidPay.class })
public class BraintreePaymentActivityPowerMockTest {

    @Rule
    public PowerMockRule mPowerMockRule = new PowerMockRule();

    private BraintreePaymentUnitTestActivity mActivity;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        mActivity = Robolectric.buildActivity(BraintreePaymentUnitTestActivity.class).get();
        setField(BraintreePaymentActivity.class, mActivity, "mLoadingViewSwitcher",
                new ViewSwitcher(RuntimeEnvironment.application));
    }

    @Test
    public void onPaymentMethodSelected_startsPayPal() {
        mockStatic(PayPal.class);
        doNothing().when(PayPal.class);
        PayPal.authorizeAccount(any(BraintreeFragment.class));

        mActivity.onPaymentMethodSelected(PaymentMethodType.PAYPAL);

        verifyStatic();
        PayPal.authorizeAccount(mActivity.braintreeFragment);
    }

    @Test
    public void onPaymentMethodSelected_startsAndroidPay() {
        Cart cart = Cart.newBuilder().build();
        PaymentRequest paymentRequest = new PaymentRequest()
                .androidPayCart(cart)
                .androidPayShippingAddressRequired(true)
                .androidPayPhoneNumberRequired(true)
                .androidPayAllowedCountriesForShipping("US");
        mActivity.setPaymentRequest(paymentRequest);
        mockStatic(AndroidPay.class);
        doNothing().when(AndroidPay.class);
        AndroidPay.performMaskedWalletRequest(any(BraintreeFragment.class), any(Cart.class), anyBoolean(), anyBoolean(),
                anyCollectionOf(CountrySpecification.class), anyInt());

        mActivity.onPaymentMethodSelected(PaymentMethodType.ANDROID_PAY);

        verifyStatic();
        AndroidPay.performMaskedWalletRequest(mActivity.braintreeFragment, cart, true, true,
                paymentRequest.getAndroidPayAllowedCountriesForShipping(), 13489);
    }

    @Test
    public void onPaymentMethodSelected_startsVenmo() {
        mockStatic(Venmo.class);
        doNothing().when(Venmo.class);
        Venmo.authorizeAccount(any(BraintreeFragment.class));

        mActivity.onPaymentMethodSelected(PaymentMethodType.PAY_WITH_VENMO);

        verifyStatic();
        Venmo.authorizeAccount(mActivity.braintreeFragment);
    }

    @Test
    public void onPaymentMethodSelected_startsAddCardActivity() {
        ShadowActivity shadowActivity = shadowOf(mActivity);

        mActivity.onPaymentMethodSelected(PaymentMethodType.UNKNOWN);

        IntentForResult intent = shadowActivity.peekNextStartedActivityForResult();
        assertEquals(AddCardActivity.class.getName(), intent.intent.getComponent().getClassName());
    }

    @Test
    public void onActivityResult_handlesAndroidPayResponse() {
        Cart cart = Cart.newBuilder().build();
        mActivity.setPaymentRequest(new PaymentRequest().androidPayCart(cart));
        mockStatic(AndroidPay.class);
        doNothing().when(AndroidPay.class);
        AndroidPay.onActivityResult(any(BraintreeFragment.class), any(Cart.class), anyInt(), any(Intent.class));
        Intent data = new Intent();

        mActivity.onActivityResult(AndroidPay.ANDROID_PAY_MASKED_WALLET_REQUEST_CODE, Activity.RESULT_OK, data);
        mActivity.onActivityResult(AndroidPay.ANDROID_PAY_FULL_WALLET_REQUEST_CODE, Activity.RESULT_OK, data);

        verifyStatic(times(2));
        AndroidPay.onActivityResult(mActivity.braintreeFragment, cart, Activity.RESULT_OK, data);
    }
}
