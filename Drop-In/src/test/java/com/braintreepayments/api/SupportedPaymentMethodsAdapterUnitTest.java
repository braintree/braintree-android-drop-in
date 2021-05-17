package com.braintreepayments.api;

import android.app.FragmentController;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.models.CardConfiguration;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.GooglePaymentConfiguration;
import com.braintreepayments.api.models.VenmoConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SupportedPaymentMethodsAdapterUnitTest {

    private FragmentController fragmentController;
    private SelectPaymentMethodFragment selectPaymentMethodFragment;
    private ShadowActivity mShadowActivity;

    @Test
    public void onCreate_vaultEditButtonIsInvisible() {
//        mActivity.setDropInRequest(new DropInRequest()
//                .vaultManager(true));
//
//        mActivityController.setup();
//
//        assertEquals(View.INVISIBLE, mActivity.findViewById(R.id.bt_vault_edit_button).getVisibility());
    }


    @Test
    public void noPaymentMethodsAvailableIfNotEnabled() {
        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                RuntimeEnvironment.application, null, null);

        assertEquals(0, adapter.getCount());
    }

    @Test
    public void allPaymentMethodsAvailableIfEnabled() {
//        Configuration configuration = getConfiguration(true, true, true, true);
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, new DropInRequest(), true, true);
//
//        assertEquals(4, adapter.getCount());
//        assertEquals(PaymentMethodType.PAYPAL, adapter.getItem(0));
//        assertEquals(PaymentMethodType.PAY_WITH_VENMO, adapter.getItem(1));
//        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(2));
//        assertEquals(PaymentMethodType.GOOGLE_PAYMENT, adapter.getItem(3));
    }

    @Test
    public void cardsAvailableIfUnionPayNotSupportedAndOtherCardsPresent() {
//        Configuration configuration = getConfiguration(false, false, true, false);
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, new DropInRequest(), false, false);
//
//        assertEquals(1, adapter.getCount());
//        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(0));
    }

    @Test
    public void cardsNotAvailableIfOnlyUnionPayPresentAndNotSupported() {
//        Configuration configuration = getConfiguration(false, false, false, false);
//        when(configuration.getCardConfiguration().getSupportedCardTypes())
//                .thenReturn(new HashSet<>(Arrays.asList(PaymentMethodType.UNIONPAY.getCanonicalName())));
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, new DropInRequest(), false, false);
//
//        assertEquals(0, adapter.getCount());
    }

    @Test
    public void cardsAvailableIfOnlyUnionPayPresentAndSupported() {
//        Configuration configuration = getConfiguration(false, false, false, false);
//        when(configuration.getCardConfiguration().getSupportedCardTypes())
//                .thenReturn(new HashSet<>(Arrays.asList(PaymentMethodType.UNIONPAY.getCanonicalName())));
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, new DropInRequest(), false, true);
//
//        assertEquals(1, adapter.getCount());
//        assertEquals(PaymentMethodType.UNKNOWN, adapter.getItem(0));
    }

    @Test
    public void cardsNotAvailableIfDisableInDropInRequest() {
//        Configuration configuration = getConfiguration(false, false, true, false);
//        DropInRequest dropInRequest = new DropInRequest()
//                .disableCard();
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, dropInRequest, false, false);
//
//        assertEquals(0, adapter.getCount());
    }

    @Test
    public void paypalNotAvailableIfDisabledInDropInRequest() {
//        Configuration configuration = getConfiguration(true, false, false, false);
//        DropInRequest dropInRequest = new DropInRequest()
//                .disablePayPal();
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, dropInRequest, false, false);
//
//        assertEquals(0, adapter.getCount());
    }

    @Test
    public void venmoNotAvailableIfDisabledInDropInRequest() {
//        Configuration configuration = getConfiguration(false, true, false, false);
//        DropInRequest dropInRequest = new DropInRequest()
//                .disableVenmo();
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, dropInRequest, false, false);
//
//        assertEquals(0, adapter.getCount());
    }

    @Test
    public void googlePaymentNotAvailableIfDisabledInDropInRequest() {
//        Configuration configuration = getConfiguration(false, false, false, true);
//        DropInRequest dropInRequest = new DropInRequest()
//                .disableGooglePayment()
//                .disableGooglePayment();
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, dropInRequest, true, false);
//
//        assertEquals(0, adapter.getCount());
    }

    @Test
    public void prefersGooglePayOverGooglePaymentIfBothEnabled() {
//        Configuration configuration = getConfiguration(false, false, false, true);
//        DropInRequest dropInRequest = new DropInRequest();
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, dropInRequest, true, false);
//
//        assertEquals(1, adapter.getCount());
//        assertEquals(PaymentMethodType.GOOGLE_PAYMENT, adapter.getItem(0));
    }

    @Test
    public void googlePayNotAvailableIfDisabledInDropInRequest() {
//        Configuration configuration = getConfiguration(false, false, false, true);
//        DropInRequest dropInRequest = new DropInRequest()
//                .disableGooglePayment();
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, null);
//        adapter.setup(configuration, dropInRequest, true, false);
//
//        assertEquals(0, adapter.getCount());
    }

    @Test
    public void callsOnPaymentMethodSelectedListenerWhenPaymentMethodClicked() {
//        Configuration configuration = getConfiguration(true, true, true, true);
//        SupportedPaymentMethodSelectedListener listener = mock(SupportedPaymentMethodSelectedListener.class);
//
//        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
//                RuntimeEnvironment.application, listener);
//        adapter.setup(configuration, new DropInRequest(), true, true);
//
//        adapter.getView(0, null, null).callOnClick();
//        adapter.getView(1, null, null).callOnClick();
//        adapter.getView(2, null, null).callOnClick();
//        adapter.getView(3, null, null).callOnClick();
//
//        verify(listener).onPaymentMethodSelected(PaymentMethodType.PAYPAL);
//        verify(listener).onPaymentMethodSelected(PaymentMethodType.PAY_WITH_VENMO);
//        verify(listener).onPaymentMethodSelected(PaymentMethodType.UNKNOWN);
//        verify(listener).onPaymentMethodSelected(PaymentMethodType.GOOGLE_PAYMENT);
//        verifyNoMoreInteractions(listener);
    }

    private Configuration getConfiguration(boolean paypalEnabled, boolean venmoEnabled,
                                           boolean cardEnabled, boolean googlePaymentEnabled) {
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
