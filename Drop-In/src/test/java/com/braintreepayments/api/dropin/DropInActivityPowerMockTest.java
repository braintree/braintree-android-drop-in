package com.braintreepayments.api.dropin;

import android.widget.ViewSwitcher;

import com.braintreepayments.api.AndroidPay;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.Venmo;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
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

import java.util.ArrayList;

import static com.braintreepayments.api.test.ReflectionHelper.setField;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ PayPal.class, Venmo.class, AndroidPay.class })
public class DropInActivityPowerMockTest {

    @Rule
    public PowerMockRule mPowerMockRule = new PowerMockRule();

    private DropInUnitTestActivity mActivity;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        mActivity = Robolectric.buildActivity(DropInUnitTestActivity.class).get();
        setField(DropInActivity.class, mActivity, "mLoadingViewSwitcher",
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
        DropInRequest dropInRequest = new DropInRequest()
                .androidPayCart(cart)
                .androidPayShippingAddressRequired(true)
                .androidPayPhoneNumberRequired(true)
                .androidPayAllowedCountriesForShipping("US");
        mActivity.setDropInRequest(dropInRequest);
        mockStatic(AndroidPay.class);
        doNothing().when(AndroidPay.class);
        AndroidPay.requestAndroidPay(any(BraintreeFragment.class), any(Cart.class), anyBoolean(),
                anyBoolean(), any(ArrayList.class));

        mActivity.onPaymentMethodSelected(PaymentMethodType.ANDROID_PAY);

        verifyStatic();
        AndroidPay.requestAndroidPay(mActivity.braintreeFragment, cart, true, true,
                dropInRequest.getAndroidPayAllowedCountriesForShipping());
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
}