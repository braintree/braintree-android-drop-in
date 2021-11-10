package com.braintreepayments.api;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaymentMethodParserTest {

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
        PaymentMethodParser sut = new PaymentMethodParser();

        when(cardNonce.getLastFour()).thenReturn("1234");
        assertEquals("1234", sut.parseNonceDescription(cardNonce));
    }

    @Test
    public void getDescription_whenPayPalAccountNonce_returnsEmail() {
        PaymentMethodParser sut = new PaymentMethodParser();

        when(payPalNonce.getEmail()).thenReturn("sample@user.com");
        assertEquals("sample@user.com", sut.parseNonceDescription(payPalNonce));
    }

    @Test
    public void getDescription_whenVenmoAccountNonce_returnsUsername() {
        PaymentMethodParser sut = new PaymentMethodParser();

        when(venmoNonce.getUsername()).thenReturn("@sample_user");
        assertEquals("@sample_user", sut.parseNonceDescription(venmoNonce));
    }

    @Test
    public void getDescription_whenGooglePayCardNonce_returnsLastFour() {
        PaymentMethodParser sut = new PaymentMethodParser();

        when(googlePayNonce.getLastFour()).thenReturn("5678");
        assertEquals("5678", sut.parseNonceDescription(googlePayNonce));
    }

    @Test
    public void getDescription_whenNonceUnrecognized_returnsEmptyString() {
        PaymentMethodParser sut = new PaymentMethodParser();

        assertEquals("", sut.parseNonceDescription(mock(PaymentMethodNonce.class)));
    }

    @Test
    public void getTypeLabel_whenCardNonce_returnsCardType() {
        PaymentMethodParser sut = new PaymentMethodParser();

        when(cardNonce.getCardType()).thenReturn("Visa");
        assertEquals("Visa", sut.getCanonicalName(cardNonce));
    }

    @Test
    public void getTypeLabel_whenPayPalAccountNonce_returnsPayPal() {
        PaymentMethodParser sut = new PaymentMethodParser();

        assertEquals("PayPal", sut.getCanonicalName(payPalNonce));
    }

    @Test
    public void getTypeLabel_whenVenmoAccountNonce_returnsVenmo() {
        PaymentMethodParser sut = new PaymentMethodParser();

        assertEquals("Venmo", sut.getCanonicalName(venmoNonce));
    }

    @Test
    public void getTypeLabel_whenGooglePayCardNonce_returnsGooglePay() {
        PaymentMethodParser sut = new PaymentMethodParser();

        assertEquals("Google Pay", sut.getCanonicalName(googlePayNonce));
    }

    @Test
    public void getTypeLabel_whenNonceUnrecognized_returnsEmptyString() {
        PaymentMethodParser sut = new PaymentMethodParser();

        assertEquals("", sut.getCanonicalName(mock(PaymentMethodNonce.class)));
    }
}