package com.braintreepayments.api;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.braintreepayments.cardform.utils.CardType;

import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;

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

    @Test
    public void getCardType_whenIsVisa_returnsVisaCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.VISA, sut.parseCardType("Visa"));
    }

    @Test
    public void getCardType_whenIsMasterCard_returnsMasterCardCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.MASTERCARD, sut.parseCardType("MasterCard"));
    }

    @Test
    public void getCardType_whenIsDiscover_returnsDiscoverCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.DISCOVER, sut.parseCardType("Discover"));
    }

    @Test
    public void getCardType_whenIsAmex_returnsAmexCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.AMEX, sut.parseCardType("American Express"));
    }

    @Test
    public void getCardType_whenIsJCB_returnsJCBCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.JCB, sut.parseCardType("JCB"));
    }

    @Test
    public void getCardType_whenIsDinersClub_returnsDinersClubCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.DINERS_CLUB, sut.parseCardType("Diners"));
    }

    @Test
    public void getCardType_whenIsMaestro_returnsMaestroCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.MAESTRO, sut.parseCardType("Maestro"));
    }

    @Test
    public void getCardType_whenIsUnionPay_returnsUnionPayCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.UNIONPAY, sut.parseCardType("UnionPay"));
    }

    @Test
    public void getCardType_whenIsPayPal_returnsNull() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertNull(sut.parseCardType("PayPal"));
    }

    @Test
    public void getCardType_whenIsUnknown_returnsNull() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertNull(sut.parseCardType("Unknown"));
    }

    @Test
    public void getCardType_whenIsVenmo_returnsNull() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertNull(sut.parseCardType("Venmo"));
    }

    @Test
    public void getCardType_whenIsHiper_returnsHiperCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.HIPER, sut.parseCardType("Hiper"));
    }

    @Test
    public void getCardType_whenIsHipercard_returnsHipercardCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.HIPERCARD, sut.parseCardType("Hipercard"));
    }
}
