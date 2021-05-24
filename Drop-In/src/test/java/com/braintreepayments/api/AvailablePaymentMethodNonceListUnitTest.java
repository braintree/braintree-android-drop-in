package com.braintreepayments.api;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AvailablePaymentMethodNonceListUnitTest {

    private Context context;

    private PayPalAccountNonce payPalAccountNonce;
    private VenmoAccountNonce venmoAccountNonce;
    private CardNonce cardNonce;
    private GooglePayCardNonce googlePaymentCardNonce;

    @Before
    public void beforeEach() {
        context = RuntimeEnvironment.application;
        payPalAccountNonce = mock(PayPalAccountNonce.class);
        venmoAccountNonce = mock(VenmoAccountNonce.class);
        cardNonce = mock(CardNonce.class);
        googlePaymentCardNonce = mock(GooglePayCardNonce.class);
    }

    @Test
    public void noPaymentMethodsAvailableIfNotEnabled() {
        Configuration configuration = getMockConfiguration(false, false, false, false);

        List<PaymentMethodNonce> paymentMethodNonces = Arrays.asList(
                payPalAccountNonce, venmoAccountNonce, cardNonce, googlePaymentCardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, new DropInRequest(), false);

        assertEquals(0, sut.size());
    }

    @Test
    public void allPaymentMethodsAvailableIfEnabled() {
        Configuration configuration = getMockConfiguration(true, true, true, true);

        List<PaymentMethodNonce> paymentMethodNonces = Arrays.asList(
                payPalAccountNonce, venmoAccountNonce, cardNonce, googlePaymentCardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, new DropInRequest(), true);

        assertEquals(4, sut.size());
        assertEquals(payPalAccountNonce, sut.get(0));
        assertEquals(venmoAccountNonce, sut.get(1));
        assertEquals(cardNonce, sut.get(2));
        assertEquals(googlePaymentCardNonce, sut.get(3));
    }

    @Test
    public void cardsAvailableIfEnabledAndSupportedCardTypesPresent() {
        Configuration configuration = getMockConfiguration(false, false, true, false);

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) cardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, new DropInRequest(), false);

        assertEquals(1, sut.size());
        assertEquals(cardNonce, sut.get(0));
    }

    @Test
    public void cardsNotAvailableIfDisableInDropInRequest() {
        Configuration configuration = getMockConfiguration(false, false, true, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableCard();

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) cardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void paypalNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getMockConfiguration(true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disablePayPal();

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) payPalAccountNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void venmoNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getMockConfiguration(false, true, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableVenmo();

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) venmoAccountNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void googlePaymentNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getMockConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment();

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) googlePaymentCardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, true);

        assertEquals(0, sut.size());
    }

    // TODO: what does this test mean do we need it?
    @Test
    public void prefersGooglePayOverGooglePaymentIfBothEnabled() {
        Configuration configuration = getMockConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest();

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) googlePaymentCardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, true);

        assertEquals(1, sut.size());
        assertEquals(googlePaymentCardNonce, sut.get(0));
    }

    @Test
    public void googlePayNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getMockConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment();

        List<PaymentMethodNonce> paymentMethodNonces = Collections.singletonList((PaymentMethodNonce) googlePaymentCardNonce);

        AvailablePaymentMethodNonceList sut = new AvailablePaymentMethodNonceList(
                configuration, paymentMethodNonces, dropInRequest, true);

        assertEquals(0, sut.size());
    }

    private Configuration getMockConfiguration(boolean paypalEnabled, boolean venmoEnabled, boolean cardEnabled, boolean googlePaymentEnabled) {
        Configuration configuration = mock(Configuration.class);

        when(configuration.isPayPalEnabled()).thenReturn(paypalEnabled);
        when(configuration.isVenmoEnabled()).thenReturn(venmoEnabled);
        when(configuration.isGooglePayEnabled()).thenReturn(googlePaymentEnabled);

        if (cardEnabled) {
            when(configuration.getSupportedCardTypes()).thenReturn(
                    Collections.singletonList(DropInPaymentMethodType.VISA.getCanonicalName()));
        }

        return configuration;
    }
}