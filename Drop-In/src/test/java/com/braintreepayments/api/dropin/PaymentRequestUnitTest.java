package com.braintreepayments.api.dropin;

import android.content.Intent;
import android.os.Parcel;

import com.braintreepayments.api.PayPal;
import com.google.android.gms.wallet.Cart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Collections;

import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class PaymentRequestUnitTest {

    @Test
    public void includesAllOptions() {
        Cart cart = Cart.newBuilder()
                .setTotalPrice("5.00")
                .build();
        Intent intent = new PaymentRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .amount("1.00")
                .collectDeviceData(true)
                .androidPayCart(cart)
                .androidPayShippingAddressRequired(true)
                .androidPayPhoneNumberRequired(true)
                .androidPayAllowedCountriesForShipping("GB")
                .disableAndroidPay()
                .paypalAdditionalScopes(Collections.singletonList(PayPal.SCOPE_ADDRESS))
                .disablePayPal()
                .disableVenmo()
                .getIntent(RuntimeEnvironment.application);

        PaymentRequest paymentRequest = intent.getParcelableExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST);

        assertEquals(DropInActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(TOKENIZATION_KEY, paymentRequest.getAuthorization());
        assertEquals("1.00", paymentRequest.getAmount());
        assertTrue(paymentRequest.shouldCollectDeviceData());
        assertEquals("5.00", paymentRequest.getAndroidPayCart().getTotalPrice());
        assertTrue(paymentRequest.isAndroidPayShippingAddressRequired());
        assertTrue(paymentRequest.isAndroidPayPhoneNumberRequired());
        assertFalse(paymentRequest.isAndroidPayEnabled());
        assertEquals(1, paymentRequest.getAndroidPayAllowedCountriesForShipping().size());
        assertEquals("GB", paymentRequest.getAndroidPayAllowedCountriesForShipping().get(0).getCountryCode());
        assertEquals(Collections.singletonList(PayPal.SCOPE_ADDRESS), paymentRequest.getPayPalAdditionalScopes());
        assertFalse(paymentRequest.isPayPalEnabled());
        assertFalse(paymentRequest.isVenmoEnabled());
    }

    @Test
    public void hasCorrectDefaults() {
        Intent intent = new PaymentRequest()
                .getIntent(RuntimeEnvironment.application);

        PaymentRequest paymentRequest = intent.getParcelableExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST);

        assertEquals(DropInActivity.class.getName(), intent.getComponent().getClassName());
        assertNull(paymentRequest.getAuthorization());
        assertNull(paymentRequest.getAmount());
        assertFalse(paymentRequest.shouldCollectDeviceData());
        assertNull(paymentRequest.getAndroidPayCart());
        assertFalse(paymentRequest.isAndroidPayShippingAddressRequired());
        assertFalse(paymentRequest.isAndroidPayPhoneNumberRequired());
        assertTrue(paymentRequest.isAndroidPayEnabled());
        assertTrue(paymentRequest.getAndroidPayAllowedCountriesForShipping().isEmpty());
        assertNull(paymentRequest.getPayPalAdditionalScopes());
        assertTrue(paymentRequest.isPayPalEnabled());
        assertTrue(paymentRequest.isVenmoEnabled());
    }

    @Test
    public void isParcelable() {
        Cart cart = Cart.newBuilder()
                .setTotalPrice("5.00")
                .build();
        PaymentRequest paymentRequest = new PaymentRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .amount("1.00")
                .collectDeviceData(true)
                .androidPayCart(cart)
                .androidPayShippingAddressRequired(true)
                .androidPayPhoneNumberRequired(true)
                .androidPayAllowedCountriesForShipping("GB")
                .disableAndroidPay()
                .paypalAdditionalScopes(Collections.singletonList(PayPal.SCOPE_ADDRESS))
                .disablePayPal()
                .disableVenmo();

        Parcel parcel = Parcel.obtain();
        paymentRequest.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        PaymentRequest parceledPaymentRequest = PaymentRequest.CREATOR.createFromParcel(parcel);

        assertEquals(TOKENIZATION_KEY, parceledPaymentRequest.getAuthorization());
        assertEquals("1.00", parceledPaymentRequest.getAmount());
        assertTrue(parceledPaymentRequest.shouldCollectDeviceData());
        assertEquals("5.00", parceledPaymentRequest.getAndroidPayCart().getTotalPrice());
        assertTrue(parceledPaymentRequest.isAndroidPayShippingAddressRequired());
        assertTrue(parceledPaymentRequest.isAndroidPayPhoneNumberRequired());
        assertFalse(parceledPaymentRequest.isAndroidPayEnabled());
        assertEquals(1, parceledPaymentRequest.getAndroidPayAllowedCountriesForShipping().size());
        assertEquals("GB", parceledPaymentRequest.getAndroidPayAllowedCountriesForShipping().get(0).getCountryCode());
        assertEquals(Collections.singletonList(PayPal.SCOPE_ADDRESS), parceledPaymentRequest.getPayPalAdditionalScopes());
        assertFalse(parceledPaymentRequest.isPayPalEnabled());
        assertFalse(parceledPaymentRequest.isVenmoEnabled());
    }

    @Test
    public void androidPayAllowedCountriesForShipping_defaultsToEmpty() {
        PaymentRequest paymentRequest = new PaymentRequest();

        assertTrue(paymentRequest.getAndroidPayAllowedCountriesForShipping().isEmpty());
    }

    @Test
    public void getIntent_includesClientToken() {
        PaymentRequest paymentRequest = new PaymentRequest()
                .clientToken(stringFromFixture("client_token.json"));

        assertEquals(stringFromFixture("client_token.json") , paymentRequest.getAuthorization());
    }

    @Test
    public void getIntent_includesTokenizationKey() {
        PaymentRequest paymentRequest = new PaymentRequest()
                .tokenizationKey(TOKENIZATION_KEY);

        assertEquals(TOKENIZATION_KEY, paymentRequest.getAuthorization());
    }
}
