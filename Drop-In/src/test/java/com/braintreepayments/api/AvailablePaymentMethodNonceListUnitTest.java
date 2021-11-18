package com.braintreepayments.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AvailablePaymentMethodNonceListUnitTest {

    private PaymentMethodNonce payPalAccountNonce;
    private PaymentMethodNonce venmoAccountNonce;
    private PaymentMethodNonce cardNonce;
    private PaymentMethodNonce googlePayCardNonce;

    @Before
    public void beforeEach() {
        payPalAccountNonce = mock(PayPalAccountNonce.class);
        venmoAccountNonce = mock(VenmoAccountNonce.class);
        cardNonce = mock(CardNonce.class);
        googlePayCardNonce = mock(GooglePayCardNonce.class);
    }

    @Test
    public void noPaymentMethodsAvailableIfNotEnabled() {
        Configuration configuration = getMockConfiguration(false, false, false, false);

        List<PaymentMethodNonce> paymentMethodNonces = Arrays.asList(
                payPalAccountNonce, venmoAccountNonce, cardNonce, googlePayCardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, new DropInRequest(), false);

        assertEquals(0, sut.size());
    }

    @Test
    public void allPaymentMethodsAvailableIfEnabled() {
        Configuration configuration = getMockConfiguration(true, true, true, true);

        List<PaymentMethodNonce> paymentMethodNonces = Arrays.asList(
                payPalAccountNonce, venmoAccountNonce, cardNonce, googlePayCardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, new DropInRequest(), true);

        assertEquals(4, sut.size());
        assertEquals(payPalAccountNonce, sut.get(0));
        assertEquals(venmoAccountNonce, sut.get(1));
        assertEquals(cardNonce, sut.get(2));
        assertEquals(googlePayCardNonce, sut.get(3));
    }

    @Test
    public void cardsAvailableIfEnabledAndSupportedCardTypesPresent() {
        Configuration configuration = getMockConfiguration(false, false, true, false);

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList(cardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, new DropInRequest(), false);

        assertEquals(1, sut.size());
        assertEquals(cardNonce, sut.get(0));
    }

    @Test
    public void cardsNotAvailableIfDisableInDropInRequest() {
        Configuration configuration = getMockConfiguration(false, false, true, false);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setCardDisabled(true);

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) cardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void paypalNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getMockConfiguration(true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setPayPalDisabled(true);

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) payPalAccountNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void venmoNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getMockConfiguration(false, true, false, false);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setVenmoDisabled(true);

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) venmoAccountNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void googlePayNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getMockConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setGooglePayDisabled(true);

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) googlePayCardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, true);

        assertEquals(0, sut.size());
    }

    private Configuration getMockConfiguration(boolean paypalEnabled, boolean venmoEnabled, boolean cardEnabled, boolean googlePayEnabled) {
        Configuration configuration = mock(Configuration.class);

        when(configuration.isPayPalEnabled()).thenReturn(paypalEnabled);
        when(configuration.isVenmoEnabled()).thenReturn(venmoEnabled);
        when(configuration.isGooglePayEnabled()).thenReturn(googlePayEnabled);

        if (cardEnabled) {
            when(configuration.getSupportedCardTypes()).thenReturn(
                    Collections.singletonList(DropInPaymentMethod.VISA.name()));
        }

        return configuration;
    }
}