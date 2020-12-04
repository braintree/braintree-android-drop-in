package com.braintreepayments.api.dropin.adapters;

import android.content.Context;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.CardConfiguration;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.GooglePaymentConfiguration;
import com.braintreepayments.api.models.VenmoConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AvailablePaymentMethodListUnitTest {

    private Context context;

    @Before
    public void beforeEach() {
        context = RuntimeEnvironment.application;
    }

    @Test
    public void noPaymentMethodsAvailableIfNotEnabled() {
        Configuration configuration = getConfiguration(false, false, false, false);

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, new DropInRequest(), false, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void allPaymentMethodsAvailableIfEnabled() {
        Configuration configuration = getConfiguration(true, true, true, true);

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, new DropInRequest(), true, true);

        assertEquals(4, sut.size());
        assertEquals(PaymentMethodType.PAYPAL, sut.getItem(0));
        assertEquals(PaymentMethodType.PAY_WITH_VENMO, sut.getItem(1));
        assertEquals(PaymentMethodType.UNKNOWN, sut.getItem(2));
        assertEquals(PaymentMethodType.GOOGLE_PAYMENT, sut.getItem(3));
    }

    @Test
    public void cardsAvailableIfUnionPayNotSupportedAndOtherCardsPresent() {
        Configuration configuration = getConfiguration(false, false, true, false);

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, new DropInRequest(), false, false);

        assertEquals(1, sut.size());
        assertEquals(PaymentMethodType.UNKNOWN, sut.getItem(0));
    }

    @Test
    public void cardsNotAvailableIfOnlyUnionPayPresentAndNotSupported() {
        Configuration configuration = getConfiguration(false, false, false, false);
        when(configuration.getCardConfiguration().getSupportedCardTypes())
                .thenReturn(new HashSet<>(Arrays.asList(PaymentMethodType.UNIONPAY.getCanonicalName())));

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, new DropInRequest(), false, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void cardsAvailableIfOnlyUnionPayPresentAndSupported() {
        Configuration configuration = getConfiguration(false, false, false, false);
        when(configuration.getCardConfiguration().getSupportedCardTypes())
                .thenReturn(new HashSet<>(Arrays.asList(PaymentMethodType.UNIONPAY.getCanonicalName())));

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, new DropInRequest(), false, true);

        assertEquals(1, sut.size());
        assertEquals(PaymentMethodType.UNKNOWN, sut.getItem(0));
    }

    @Test
    public void cardsNotAvailableIfDisableInDropInRequest() {
        Configuration configuration = getConfiguration(false, false, true, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableCard();

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, dropInRequest, false, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void paypalNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disablePayPal();

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, dropInRequest, false, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void venmoNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, true, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableVenmo();

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, dropInRequest, false, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void googlePaymentNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment()
                .disableGooglePayment();

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, dropInRequest, true, false);

        assertEquals(0, sut.size());
    }

    @Test
    public void prefersGooglePayOverGooglePaymentIfBothEnabled() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest();

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, dropInRequest, true, false);

        assertEquals(1, sut.size());
        assertEquals(PaymentMethodType.GOOGLE_PAYMENT, sut.getItem(0));
    }

    @Test
    public void googlePayNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment();

        AvailablePaymentMethodList sut = new AvailablePaymentMethodList(
                context, configuration, dropInRequest, true, false);

        assertEquals(0, sut.size());
    }

    private Configuration getConfiguration(boolean paypalEnabled, boolean venmoEnabled, boolean cardEnabled, boolean googlePaymentEnabled) {
        Configuration configuration = mock(Configuration.class);

        if (paypalEnabled) {
            when(configuration.isPayPalEnabled()).thenReturn(true);
        }

        VenmoConfiguration venmoConfiguration = mock(VenmoConfiguration.class);
        when(configuration.getPayWithVenmo()).thenReturn(venmoConfiguration);
        if (venmoEnabled) {
            when(venmoConfiguration.isEnabled(any(Context.class))).thenReturn(true);
        }

        CardConfiguration cardConfiguration = mock(CardConfiguration.class);
        when(configuration.getCardConfiguration()).thenReturn(cardConfiguration);
        if (cardEnabled) {
            when(cardConfiguration.getSupportedCardTypes()).thenReturn(
                    new HashSet<>(Arrays.asList(PaymentMethodType.VISA.getCanonicalName())));
        }

        GooglePaymentConfiguration googlePaymentConfiguration = mock(GooglePaymentConfiguration.class);
        when(configuration.getGooglePayment()).thenReturn(googlePaymentConfiguration);
        if (googlePaymentEnabled) {
            when(googlePaymentConfiguration.isEnabled(any(Context.class))).thenReturn(true);
        }

        return configuration;
    }
}