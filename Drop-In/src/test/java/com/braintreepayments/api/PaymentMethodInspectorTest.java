package com.braintreepayments.api;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaymentMethodInspectorTest {

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
        PaymentMethodInspector sut = new PaymentMethodInspector();

        when(cardNonce.getLastFour()).thenReturn("1234");
        assertEquals("1234", sut.getPaymentMethodDescription(cardNonce));
    }

    @Test
    public void getDescription_whenPayPalAccountNonce_returnsEmail() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        when(payPalNonce.getEmail()).thenReturn("sample@user.com");
        assertEquals("sample@user.com", sut.getPaymentMethodDescription(payPalNonce));
    }

    @Test
    public void getDescription_whenVenmoAccountNonce_returnsUsername() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        when(venmoNonce.getUsername()).thenReturn("@sample_user");
        assertEquals("@sample_user", sut.getPaymentMethodDescription(venmoNonce));
    }

    @Test
    public void getDescription_whenGooglePayCardNonce_returnsLastFour() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        when(googlePayNonce.getLastFour()).thenReturn("5678");
        assertEquals("5678", sut.getPaymentMethodDescription(googlePayNonce));
    }

    @Test
    public void getDescription_whenNonceUnrecognized_returnsEmptyString() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        assertEquals("", sut.getPaymentMethodDescription(mock(PaymentMethodNonce.class)));
    }

    @Test
    public void getTypeLabel_whenCardNonce_returnsCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        when(cardNonce.getCardType()).thenReturn("Visa");
        assertEquals("Visa", sut.getCanonicalName(cardNonce));
    }

    @Test
    public void getTypeLabel_whenPayPalAccountNonce_returnsPayPal() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        assertEquals("PayPal", sut.getCanonicalName(payPalNonce));
    }

    @Test
    public void getTypeLabel_whenVenmoAccountNonce_returnsVenmo() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        assertEquals("Venmo", sut.getCanonicalName(venmoNonce));
    }

    @Test
    public void getTypeLabel_whenGooglePayCardNonce_returnsGooglePay() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        assertEquals("Google Pay", sut.getCanonicalName(googlePayNonce));
    }

    @Test
    public void getTypeLabel_whenNonceUnrecognized_returnsEmptyString() {
        PaymentMethodInspector sut = new PaymentMethodInspector();

        assertEquals("", sut.getCanonicalName(mock(PaymentMethodNonce.class)));
    }
}