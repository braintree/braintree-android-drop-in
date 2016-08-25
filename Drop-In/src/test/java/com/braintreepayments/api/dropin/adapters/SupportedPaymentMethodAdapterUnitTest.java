package com.braintreepayments.api.dropin.adapters;

import android.content.Context;

import com.braintreepayments.api.dropin.adapters.SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.AndroidPayConfiguration;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.VenmoConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class SupportedPaymentMethodAdapterUnitTest {

    @Test
    public void noPaymentMethodsAvailableIfNotEnabled() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getAndroidPay()).thenReturn(mock(AndroidPayConfiguration.class));
        when(configuration.getPayWithVenmo()).thenReturn(mock(VenmoConfiguration.class));

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(RuntimeEnvironment.application,
                configuration, null);

        assertEquals(1, adapter.getCount());
        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(0));
    }

    @Test
    public void allPaymentMethodsAvailableIfEnabled() {
        AndroidPayConfiguration androidPayConfiguration = mock(AndroidPayConfiguration.class);
        when(androidPayConfiguration.isEnabled(any(Context.class))).thenReturn(true);
        VenmoConfiguration venmoConfiguration = mock(VenmoConfiguration.class);
        when(venmoConfiguration.isEnabled(any(Context.class))).thenReturn(true);
        Configuration configuration = mock(Configuration.class);
        when(configuration.getAndroidPay()).thenReturn(androidPayConfiguration);
        when(configuration.isPayPalEnabled()).thenReturn(true);
        when(configuration.getPayWithVenmo()).thenReturn(venmoConfiguration);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(RuntimeEnvironment.application,
                configuration, null);

        assertEquals(4, adapter.getCount());
        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(0));
        assertEquals(PaymentMethodType.ANDROID_PAY, adapter.getItem(1));
        assertEquals(PaymentMethodType.PAYPAL, adapter.getItem(2));
        assertEquals(PaymentMethodType.PAY_WITH_VENMO, adapter.getItem(3));
    }

    @Test
    public void callsOnPaymentMethodSelectedListenerWhenPaymentMethodClicked() {
        AndroidPayConfiguration androidPayConfiguration = mock(AndroidPayConfiguration.class);
        when(androidPayConfiguration.isEnabled(any(Context.class))).thenReturn(true);
        VenmoConfiguration venmoConfiguration = mock(VenmoConfiguration.class);
        when(venmoConfiguration.isEnabled(any(Context.class))).thenReturn(true);
        Configuration configuration = mock(Configuration.class);
        when(configuration.getAndroidPay()).thenReturn(androidPayConfiguration);
        when(configuration.isPayPalEnabled()).thenReturn(true);
        when(configuration.getPayWithVenmo()).thenReturn(venmoConfiguration);
        PaymentMethodSelectedListener listener = mock(PaymentMethodSelectedListener.class);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(RuntimeEnvironment.application,
                configuration, listener);

        adapter.getView(0, null, null).callOnClick();
        adapter.getView(1, null, null).callOnClick();
        adapter.getView(2, null, null).callOnClick();
        adapter.getView(3, null, null).callOnClick();

        verify(listener).onPaymentMethodSelected(PaymentMethodType.UNKNOWN);
        verify(listener).onPaymentMethodSelected(PaymentMethodType.ANDROID_PAY);
        verify(listener).onPaymentMethodSelected(PaymentMethodType.PAYPAL);
        verify(listener).onPaymentMethodSelected(PaymentMethodType.PAY_WITH_VENMO);
        verifyNoMoreInteractions(listener);
    }
}
