package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

public class DropInSharedPreferencesUnitTest {

    private Context context;

    private PaymentMethodInspector paymentMethodInspector;
    private BraintreeSharedPreferences braintreeSharedPreferences;

    private DropInSharedPreferences sut;

    @Before
    public void beforeEach() {
        context = mock(Context.class);
        paymentMethodInspector = mock(PaymentMethodInspector.class);
        braintreeSharedPreferences = mock(BraintreeSharedPreferences.class);

        sut = new DropInSharedPreferences(braintreeSharedPreferences, paymentMethodInspector);
    }

    @Test
    public void getLastUsedPaymentMethod_whenPaymentMethodIsValid_returnsPaymentMethod() {
        String key = "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD";
        when(braintreeSharedPreferences.getString(key, null)).thenReturn("VISA");
        assertEquals(DropInPaymentMethod.VISA, sut.getLastUsedPaymentMethod());
    }

    @Test
    public void getLastUsedPaymentMethod_whenPaymentMethodIsInvalid_returnsNull() {
        String key = "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD";
        when(braintreeSharedPreferences.getString(key, null)).thenReturn("UNKNOWN_PAYMENT_METHOD");
        assertNull(sut.getLastUsedPaymentMethod());
    }

    @Test
    public void getLastUsedPaymentMethod_whenPaymentMethodDoesNotExist_returnsNull() {
        String key = "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD";
        when(braintreeSharedPreferences.getString(key, null)).thenReturn(null);
        assertNull(sut.getLastUsedPaymentMethod());
    }

    @Test
    public void setLastUsedPaymentMethod_whenPaymentMethodExists_setsPaymentMethodInSharedPrefs() {
        PaymentMethodNonce nonce = mock(PaymentMethodNonce.class);
        when(paymentMethodInspector.getPaymentMethod(nonce)).thenReturn(DropInPaymentMethod.VISA);

        sut.setLastUsedPaymentMethod(nonce);
        String key = "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD";
        verify(braintreeSharedPreferences).putString(key, "VISA");
    }

    @Test
    public void setLastUsedPaymentMethod_whenPaymentMethodDoesNotExist_doesNotSetPaymentMethodInSharedPrefs() {
        PaymentMethodNonce nonce = mock(PaymentMethodNonce.class);
        when(paymentMethodInspector.getPaymentMethod(nonce)).thenReturn(null);

        sut.setLastUsedPaymentMethod(nonce);
        verifyNoInteractions(braintreeSharedPreferences);
    }
}
