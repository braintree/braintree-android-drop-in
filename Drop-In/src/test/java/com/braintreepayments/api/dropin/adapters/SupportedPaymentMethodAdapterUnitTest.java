package com.braintreepayments.api.dropin.adapters;

import android.content.Context;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.adapters.SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.AndroidPayConfiguration;
import com.braintreepayments.api.models.CardConfiguration;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.VenmoConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SupportedPaymentMethodAdapterUnitTest {

    @Test
    public void noPaymentMethodsAvailableIfNotEnabled() {
        Configuration configuration = getConfiguration(false, false, false, false);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, new DropInRequest(), false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void allPaymentMethodsAvailableIfEnabled() {
        Configuration configuration = getConfiguration(true, true, true, true);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
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
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, new DropInRequest(), false, false);

        assertEquals(1, adapter.getCount());
        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(0));
    }

    @Test
    public void cardsNotAvailableIfOnlyUnionPayPresentAndNotSupported() {
        Configuration configuration = getConfiguration(false, false, false, false);
        when(configuration.getCardConfiguration().getSupportedCardTypes())
                .thenReturn(new HashSet<>(Arrays.asList(PaymentMethodType.UNIONPAY.getCanonicalName())));

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, new DropInRequest(), false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void cardsAvailableIfOnlyUnionPayPresentAndSupported() {
        Configuration configuration = getConfiguration(false, false, false, false);
        when(configuration.getCardConfiguration().getSupportedCardTypes())
                .thenReturn(new HashSet<>(Arrays.asList(PaymentMethodType.UNIONPAY.getCanonicalName())));

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, new DropInRequest(), false, true);

        assertEquals(1, adapter.getCount());
        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(0));
    }

    @Test
    public void paypalNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(true, false, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disablePayPal();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, dropInRequest, false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void venmoNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, true, false, false);
        DropInRequest dropInRequest = new DropInRequest()
                .disableVenmo();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, dropInRequest, false, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void androidPayNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest()
                .disableGooglePayment()
                .disableAndroidPay();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, dropInRequest, true, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void prefersGooglePayOverAndroidPayIfBothEnabled() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, dropInRequest, true, false);

        assertEquals(1, adapter.getCount());
        assertEquals(PaymentMethodType.GOOGLE_PAYMENT, adapter.getItem(0));
    }

    @Test
    public void googlePayNotAvailableIfDisabledInDropInRequest() {
        Configuration configuration = getConfiguration(false, false, false, true);
        DropInRequest dropInRequest = new DropInRequest()
                .disableAndroidPay()
                .disableGooglePayment();

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null);
        adapter.setup(configuration, dropInRequest, true, false);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void callsOnPaymentMethodSelectedListenerWhenPaymentMethodClicked() {
        Configuration configuration = getConfiguration(true, true, true, true);
        PaymentMethodSelectedListener listener = mock(PaymentMethodSelectedListener.class);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, listener);
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
                                           boolean cardEnabled, boolean androidPayEnabled) {
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

        AndroidPayConfiguration androidPayConfiguration = mock(AndroidPayConfiguration.class);
        when(configuration.getAndroidPay()).thenReturn(androidPayConfiguration);
        if (androidPayEnabled) {
            when(androidPayConfiguration.isEnabled(any(Context.class))).thenReturn(true);
        }

        return configuration;
    }
}
