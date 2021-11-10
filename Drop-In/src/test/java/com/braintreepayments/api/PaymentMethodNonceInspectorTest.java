package com.braintreepayments.api;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaymentMethodNonceInspectorTest {

    private CardNonce cardNonce;
    private PayPalAccountNonce payPalNonce;
    private VenmoAccountNonce venmoNonce;
    private GooglePayCardNonce googlePayNonce;

    @Before
    public void beforeEach() {
        cardNonce = mock(CardNonce.class);
        payPalNonce = mock(PayPalAccountNonce.class);
        venmoNonce = mock(VenmoAccountNonce.class);
        googlePayNonce = mock(GooglePayCardNonce.class);
    }

    @Test
    public void getDescription_whenCardNonce_returnsCardLastFour() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        when(cardNonce.getLastFour()).thenReturn("1234");
        assertEquals("1234", sut.getDescription(cardNonce));
    }

    @Test
    public void getDescription_whenPayPalAccountNonce_returnsEmail() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        when(payPalNonce.getEmail()).thenReturn("sample@user.com");
        assertEquals("sample@user.com", sut.getDescription(payPalNonce));
    }

    @Test
    public void getDescription_whenVenmoAccountNonce_returnsUsername() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        when(venmoNonce.getUsername()).thenReturn("@sample_user");
        assertEquals("@sample_user", sut.getDescription(venmoNonce));
    }

    @Test
    public void getDescription_whenGooglePayCardNonce_returnsLastFour() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        when(googlePayNonce.getLastFour()).thenReturn("5678");
        assertEquals("5678", sut.getDescription(googlePayNonce));
    }

    @Test
    public void getDescription_whenNonceUnrecognized_returnsEmptyString() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        assertEquals("", sut.getDescription(mock(PaymentMethodNonce.class)));
    }

    @Test
    public void getTypeLabel_whenCardNonce_returnsCardType() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        when(cardNonce.getCardType()).thenReturn("Visa");
        assertEquals("Visa", sut.getCanonicalName(cardNonce));
    }

    @Test
    public void getTypeLabel_whenPayPalAccountNonce_returnsPayPal() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        assertEquals("PayPal", sut.getCanonicalName(payPalNonce));
    }

    @Test
    public void getTypeLabel_whenVenmoAccountNonce_returnsVenmo() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        assertEquals("Venmo", sut.getCanonicalName(venmoNonce));
    }

    @Test
    public void getTypeLabel_whenGooglePayCardNonce_returnsGooglePay() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        assertEquals("Google Pay", sut.getCanonicalName(googlePayNonce));
    }

    @Test
    public void getTypeLabel_whenNonceUnrecognized_returnsEmptyString() {
        PaymentMethodNonceInspector sut = new PaymentMethodNonceInspector();

        assertEquals("", sut.getCanonicalName(mock(PaymentMethodNonce.class)));
    }
}