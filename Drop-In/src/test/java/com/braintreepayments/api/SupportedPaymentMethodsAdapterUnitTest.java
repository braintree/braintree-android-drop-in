package com.braintreepayments.api;

import androidx.test.core.app.ApplicationProvider;

import com.braintreepayments.api.SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SupportedPaymentMethodsAdapterUnitTest {

    @Test
    public void noPaymentMethodsAvailableIfNotEnabled() {
        Configuration configuration = getConfiguration(false, false, false, false);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(ApplicationProvider.getApplicationContext(),
                null);
        adapter.setup(configuration, new DropInRequest(), false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void allPaymentMethodsAvailableIfEnabled() {
        Configuration configuration = getConfiguration(true, true, true, true);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, new DropInRequest(), true, true);

        assertEquals(4, adapter.getCount());
        assertEquals(PaymentMethodType.PAYPAL, adapter.getItem(0));
        assertEquals(PaymentMethodType.PAY_WITH_VENMO, adapter.getItem(1));
        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(2));
        assertEquals(PaymentMethodType.GOOGLE_PAYMENT, adapter.getItem(3));
    }

    @Test
    public void cardsAvailableIfUnionPayNotSupportedAndOtherCardsPresent() {
        Configuration configuration = getConfiguration(false, false, true, false);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, new DropInRequest(), false, false);

        assertEquals(1, adapter.getCount());
        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(0));
    }

    @Test
    public void cardsNotAvailableIfOnlyUnionPayPresentAndNotSupported() {
        Configuration configuration = getConfiguration(false, false, false, false);
        when(configuration.getSupportedCardTypes())
                .thenReturn(Arrays.asList(PaymentMethodType.UNIONPAY.getCanonicalName()));

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, new DropInRequest(), false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void cardsAvailableIfOnlyUnionPayPresentAndSupported() {
        Configuration configuration = getConfiguration(false, false, false, false);
        when(configuration.getSupportedCardTypes())
                .thenReturn(Arrays.asList(PaymentMethodType.UNIONPAY.getCanonicalName()));

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, new DropInRequest(), false, true);

        assertEquals(1, adapter.getCount());
        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(0));
    }

    @Test
    public void cardsNotAvailableIfDisableInDropInRequest() {
        Configuration configuration = getConfiguration(false, false, true, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableCard();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, dropInRequest, false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void paypalNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disablePayPal();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, dropInRequest, false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void venmoNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, true, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableVenmo();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, dropInRequest, false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void googlePaymentNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment()
                .disableGooglePayment();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, dropInRequest, true, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void prefersGooglePayOverGooglePaymentIfBothEnabled() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, dropInRequest, true, false);

        assertEquals(1, adapter.getCount());
        assertEquals(PaymentMethodType.GOOGLE_PAYMENT, adapter.getItem(0));
    }

    @Test
    public void googlePayNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), null);
        adapter.setup(configuration, dropInRequest, true, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void callsOnPaymentMethodSelectedListenerWhenPaymentMethodClicked() {
        Configuration configuration = getConfiguration(true, true, true, true);
        PaymentMethodSelectedListener listener = mock(PaymentMethodSelectedListener.class);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                ApplicationProvider.getApplicationContext(), listener);
        adapter.setup(configuration, new DropInRequest(), true, true);

        adapter.getView(0, null, null).callOnClick();
        adapter.getView(1, null, null).callOnClick();
        adapter.getView(2, null, null).callOnClick();
        adapter.getView(3, null, null).callOnClick();

        verify(listener).onPaymentMethodSelected(PaymentMethodType.PAYPAL);
        verify(listener).onPaymentMethodSelected(PaymentMethodType.PAY_WITH_VENMO);
        verify(listener).onPaymentMethodSelected(PaymentMethodType.UNKNOWN);
        verify(listener).onPaymentMethodSelected(PaymentMethodType.GOOGLE_PAYMENT);
        verifyNoMoreInteractions(listener);
    }

    private Configuration getConfiguration(boolean paypalEnabled, boolean venmoEnabled,
                                           boolean cardEnabled, boolean googlePaymentEnabled) {
        Configuration configuration = mock(Configuration.class);

        when(configuration.isPayPalEnabled()).thenReturn(paypalEnabled);
        when(configuration.isVenmoEnabled()).thenReturn(venmoEnabled);

        if (cardEnabled) {
            when(configuration.getSupportedCardTypes()).thenReturn(
                    Arrays.asList(PaymentMethodType.VISA.getCanonicalName()));
        }

        when(configuration.isGooglePayEnabled()).thenReturn(googlePaymentEnabled);

        return configuration;
    }
}
