package com.braintreepayments.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
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
        when(braintreeSharedPreferences.getString(context, key, null)).thenReturn("VISA");
        assertEquals(DropInPaymentMethod.VISA, sut.getLastUsedPaymentMethod(context));
    }

    @Test
    public void getLastUsedPaymentMethod_whenPaymentMethodIsInvalid_returnsPaymentMethod() {
        String key = "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD";
        when(braintreeSharedPreferences.getString(context, key, null)).thenReturn("UNKNOWN_PAYMENT_METHOD");
        assertNull(sut.getLastUsedPaymentMethod(context));
    }

    @Test
    public void getLastUsedPaymentMethod_whenPaymentMethodIsNull_returnsNull() {
        String key = "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD";
        when(braintreeSharedPreferences.getString(context, key, null)).thenReturn(null);
        assertNull(sut.getLastUsedPaymentMethod(context));
    }
}
