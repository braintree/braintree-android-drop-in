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
public class DropInRequestUnitTest {

    @Test
    public void includesAllOptions() {
        Cart cart = Cart.newBuilder()
                .setTotalPrice("5.00")
                .build();
        Intent intent = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .collectDeviceData(true)
                .amount("1.00")
                .androidPayCart(cart)
                .androidPayShippingAddressRequired(true)
                .androidPayPhoneNumberRequired(true)
                .androidPayAllowedCountriesForShipping("GB")
                .disableAndroidPay()
                .paypalAdditionalScopes(Collections.singletonList(PayPal.SCOPE_ADDRESS))
                .disablePayPal()
                .disableVenmo()
                .requestThreeDSecureVerification(true)
                .getIntent(RuntimeEnvironment.application);

        DropInRequest dropInRequest = intent.getParcelableExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST);

        assertEquals(DropInActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(TOKENIZATION_KEY, dropInRequest.getAuthorization());
        assertTrue(dropInRequest.shouldCollectDeviceData());
        assertEquals("1.00", dropInRequest.getAmount());
        assertEquals("5.00", dropInRequest.getAndroidPayCart().getTotalPrice());
        assertTrue(dropInRequest.isAndroidPayShippingAddressRequired());
        assertTrue(dropInRequest.isAndroidPayPhoneNumberRequired());
        assertFalse(dropInRequest.isAndroidPayEnabled());
        assertEquals(1, dropInRequest.getAndroidPayAllowedCountriesForShipping().size());
        assertEquals("GB", dropInRequest.getAndroidPayAllowedCountriesForShipping().get(0).getCountryCode());
        assertEquals(Collections.singletonList(PayPal.SCOPE_ADDRESS), dropInRequest.getPayPalAdditionalScopes());
        assertFalse(dropInRequest.isPayPalEnabled());
        assertFalse(dropInRequest.isVenmoEnabled());
        assertTrue(dropInRequest.shouldRequestThreeDSecureVerification());
    }

    @Test
    public void hasCorrectDefaults() {
        Intent intent = new DropInRequest()
                .getIntent(RuntimeEnvironment.application);

        DropInRequest dropInRequest = intent.getParcelableExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST);

        assertEquals(DropInActivity.class.getName(), intent.getComponent().getClassName());
        assertNull(dropInRequest.getAuthorization());
        assertFalse(dropInRequest.shouldCollectDeviceData());
        assertNull(dropInRequest.getAmount());
        assertNull(dropInRequest.getAndroidPayCart());
        assertFalse(dropInRequest.isAndroidPayShippingAddressRequired());
        assertFalse(dropInRequest.isAndroidPayPhoneNumberRequired());
        assertTrue(dropInRequest.isAndroidPayEnabled());
        assertTrue(dropInRequest.getAndroidPayAllowedCountriesForShipping().isEmpty());
        assertNull(dropInRequest.getPayPalAdditionalScopes());
        assertTrue(dropInRequest.isPayPalEnabled());
        assertTrue(dropInRequest.isVenmoEnabled());
        assertFalse(dropInRequest.shouldRequestThreeDSecureVerification());
    }

    @Test
    public void isParcelable() {
        Cart cart = Cart.newBuilder()
                .setTotalPrice("5.00")
                .build();
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .collectDeviceData(true)
                .amount("1.00")
                .androidPayCart(cart)
                .androidPayShippingAddressRequired(true)
                .androidPayPhoneNumberRequired(true)
                .androidPayAllowedCountriesForShipping("GB")
                .disableAndroidPay()
                .paypalAdditionalScopes(Collections.singletonList(PayPal.SCOPE_ADDRESS))
                .disablePayPal()
                .disableVenmo()
                .requestThreeDSecureVerification(true);

        Parcel parcel = Parcel.obtain();
        dropInRequest.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        DropInRequest parceledDropInRequest = DropInRequest.CREATOR.createFromParcel(parcel);

        assertEquals(TOKENIZATION_KEY, parceledDropInRequest.getAuthorization());
        assertTrue(parceledDropInRequest.shouldCollectDeviceData());
        assertEquals("1.00", parceledDropInRequest.getAmount());
        assertEquals("5.00", parceledDropInRequest.getAndroidPayCart().getTotalPrice());
        assertTrue(parceledDropInRequest.isAndroidPayShippingAddressRequired());
        assertTrue(parceledDropInRequest.isAndroidPayPhoneNumberRequired());
        assertFalse(parceledDropInRequest.isAndroidPayEnabled());
        assertEquals(1, parceledDropInRequest.getAndroidPayAllowedCountriesForShipping().size());
        assertEquals("GB", parceledDropInRequest.getAndroidPayAllowedCountriesForShipping().get(0).getCountryCode());
        assertEquals(Collections.singletonList(PayPal.SCOPE_ADDRESS), parceledDropInRequest.getPayPalAdditionalScopes());
        assertFalse(parceledDropInRequest.isPayPalEnabled());
        assertFalse(parceledDropInRequest.isVenmoEnabled());
        assertTrue(parceledDropInRequest.shouldRequestThreeDSecureVerification());
    }

    @Test
    public void androidPayAllowedCountriesForShipping_defaultsToEmpty() {
        DropInRequest dropInRequest = new DropInRequest();

        assertTrue(dropInRequest.getAndroidPayAllowedCountriesForShipping().isEmpty());
    }

    @Test
    public void getIntent_includesClientToken() {
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(stringFromFixture("client_token.json"));

        assertEquals(stringFromFixture("client_token.json") , dropInRequest.getAuthorization());
    }

    @Test
    public void getIntent_includesTokenizationKey() {
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY);

        assertEquals(TOKENIZATION_KEY, dropInRequest.getAuthorization());
    }
}
