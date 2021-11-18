package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.braintreepayments.cardform.utils.CardType;

import org.junit.Before;
import org.junit.Test;

public class PaymentMethodInspectorUnitTest {

    private CardNonce cardNonce;
    private PayPalAccountNonce payPalNonce;
    private VenmoAccountNonce venmoNonce;
    private GooglePayCardNonce googlePayNonce;

    private PaymentMethodInspector sut;

    @Before
    public void beforeEach() {
        cardNonce = mock(CardNonce.class);
        payPalNonce = mock(PayPalAccountNonce.class);
        venmoNonce = mock(VenmoAccountNonce.class);
        googlePayNonce = mock(GooglePayCardNonce.class);

        sut = new PaymentMethodInspector();
    }

    @Test
    public void getDescription_whenCardNonce_returnsCardLastFour() {
        when(cardNonce.getLastFour()).thenReturn("1234");
        assertEquals("1234", sut.getPaymentMethodDescription(cardNonce));
    }

    @Test
    public void getDescription_whenPayPalAccountNonce_returnsEmail() {
        when(payPalNonce.getEmail()).thenReturn("sample@user.com");
        assertEquals("sample@user.com", sut.getPaymentMethodDescription(payPalNonce));
    }

    @Test
    public void getDescription_whenVenmoAccountNonce_returnsUsername() {
        when(venmoNonce.getUsername()).thenReturn("@sample_user");
        assertEquals("@sample_user", sut.getPaymentMethodDescription(venmoNonce));
    }

    @Test
    public void getDescription_whenGooglePayCardNonce_returnsLastFour() {
        when(googlePayNonce.getLastFour()).thenReturn("5678");
        assertEquals("5678", sut.getPaymentMethodDescription(googlePayNonce));
    }

    @Test
    public void getDescription_whenNonceUnrecognized_returnsEmptyString() {
        assertEquals("", sut.getPaymentMethodDescription(mock(PaymentMethodNonce.class)));
    }

    @Test
    public void getPaymentMethod_whenGivenAmexNonce_returnsAmex() {
        CardNonce amexNonce = createCardNonce("American Express");
        assertEquals(DropInPaymentMethod.AMEX, sut.getPaymentMethod(amexNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenDinersClubNonce_returnsDinersClub() {
        CardNonce amexNonce = createCardNonce("American Express");
        assertEquals(DropInPaymentMethod.AMEX, sut.getPaymentMethod(amexNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenDiscoverNonce_returnsDiscover() {
        CardNonce discoverNonce = createCardNonce("Discover");
        assertEquals(DropInPaymentMethod.DISCOVER, sut.getPaymentMethod(discoverNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenJCBNonce_returnsReturnsJCB() {
        CardNonce jcbNonce = createCardNonce("JCB");
        assertEquals(DropInPaymentMethod.JCB, sut.getPaymentMethod(jcbNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenMaestroNonce_returnsMaestro() {
        CardNonce maestroNonce = createCardNonce("Maestro");
        assertEquals(DropInPaymentMethod.MAESTRO, sut.getPaymentMethod(maestroNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenMasterCardNonce_returnsMasterCard() {
        CardNonce masterCardNonce = createCardNonce("MasterCard");
        assertEquals(DropInPaymentMethod.MASTERCARD, sut.getPaymentMethod(masterCardNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenVisaCardNonce_returnsVisa() {
        CardNonce visaNonce = createCardNonce("Visa");
        assertEquals(DropInPaymentMethod.VISA, sut.getPaymentMethod(visaNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenUnionPayCardNonce_returnsUnionPay() {
        CardNonce unionPayNonce = createCardNonce("UnionPay");
        assertEquals(DropInPaymentMethod.UNIONPAY, sut.getPaymentMethod(unionPayNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenHiperCardNonce_returnsHiper() {
        CardNonce hiperNonce = createCardNonce("Hiper");
        assertEquals(DropInPaymentMethod.HIPER, sut.getPaymentMethod(hiperNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenHipercardCardNonce_returnsHipercard() {
        CardNonce hipercardNonce = createCardNonce("Hipercard");
        assertEquals(DropInPaymentMethod.HIPERCARD, sut.getPaymentMethod(hipercardNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenPayPalNonce_returnsPayPal() {
        PayPalAccountNonce payPalNonce = mock(PayPalAccountNonce.class);
        assertEquals(DropInPaymentMethod.PAYPAL, sut.getPaymentMethod(payPalNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenVenmoNonce_returnsVenmo() {
        VenmoAccountNonce venmoNonce = mock(VenmoAccountNonce.class);
        assertEquals(DropInPaymentMethod.VENMO, sut.getPaymentMethod(venmoNonce));
    }

    @Test
    public void getPaymentMethod_whenGivenGooglePayNonce_returnsGooglePay() {
        GooglePayCardNonce googlePayNonce = mock(GooglePayCardNonce.class);
        assertEquals(DropInPaymentMethod.GOOGLE_PAY, sut.getPaymentMethod(googlePayNonce));
    }

    @Test
    public void getPaymentMethod_whenNonceTypeIsUnknown_returnsNull() {
        PaymentMethodNonce unknownNonce = mock(PaymentMethodNonce.class);
        assertNull(sut.getPaymentMethod(unknownNonce));
    }

    @Test
    public void getCardTypeFromString_whenIsVisa_returnsVisaCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.VISA, sut.getCardTypeFromString("Visa"));
    }

    @Test
    public void getCardTypeFromString_whenIsMasterCard_returnsMasterCardCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.MASTERCARD, sut.getCardTypeFromString("MasterCard"));
    }

    @Test
    public void getCardTypeFromString_whenIsDiscover_returnsDiscoverCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.DISCOVER, sut.getCardTypeFromString("Discover"));
    }

    @Test
    public void getCardTypeFromString_whenIsAmex_returnsAmexCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.AMEX, sut.getCardTypeFromString("American Express"));
    }

    @Test
    public void getCardTypeFromString_whenIsJCB_returnsJCBCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.JCB, sut.getCardTypeFromString("JCB"));
    }

    @Test
    public void getCardTypeFromString_whenIsDinersClub_returnsDinersClubCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.DINERS_CLUB, sut.getCardTypeFromString("Diners"));
    }

    @Test
    public void getCardTypeFromString_whenIsMaestro_returnsMaestroCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.MAESTRO, sut.getCardTypeFromString("Maestro"));
    }

    @Test
    public void getCardTypeFromString_whenIsUnionPay_returnsUnionPayCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.UNIONPAY, sut.getCardTypeFromString("UnionPay"));
    }

    @Test
    public void getCardTypeFromString_whenIsPayPal_returnsNull() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertNull(sut.getCardTypeFromString("PayPal"));
    }

    @Test
    public void getCardTypeFromString_whenIsUnknown_returnsNull() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertNull(sut.getCardTypeFromString("Unknown"));
    }

    @Test
    public void getCardTypeFromString_whenIsVenmo_returnsNull() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertNull(sut.getCardTypeFromString("Venmo"));
    }

    @Test
    public void getCardTypeFromString_whenIsHiper_returnsHiperCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.HIPER, sut.getCardTypeFromString("Hiper"));
    }

    @Test
    public void getCardTypeFromString_whenIsHipercard_returnsHipercardCardType() {
        PaymentMethodInspector sut = new PaymentMethodInspector();
        assertEquals(CardType.HIPERCARD, sut.getCardTypeFromString("Hipercard"));
    }

    private static CardNonce createCardNonce(String cardType) {
        CardNonce cardNonce = mock(CardNonce.class);
        when(cardNonce.getCardType()).thenReturn(cardType);
        return cardNonce;
    }
}
