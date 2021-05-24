package com.braintreepayments.api;

import com.braintreepayments.api.SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class SupportedPaymentMethodsAdapterUnitTest {

    // TODO: refactor test to make sure views display correct displayName / image view icon
    @Test
    public void callsOnPaymentMethodSelectedListenerWhenPaymentMethodClicked() {
        PaymentMethodSelectedListener listener = mock(PaymentMethodSelectedListener.class);
        ArrayList<DropInPaymentMethodType> supportedPaymentMethods = new ArrayList<>();
        supportedPaymentMethods.add(DropInPaymentMethodType.PAYPAL);
        supportedPaymentMethods.add(DropInPaymentMethodType.PAY_WITH_VENMO);
        supportedPaymentMethods.add(DropInPaymentMethodType.UNKNOWN);
        supportedPaymentMethods.add(DropInPaymentMethodType.GOOGLE_PAYMENT);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                supportedPaymentMethods, listener);

        adapter.getView(0, null, null).callOnClick();
        adapter.getView(1, null, null).callOnClick();
        adapter.getView(2, null, null).callOnClick();
        adapter.getView(3, null, null).callOnClick();

        verify(listener).onPaymentMethodSelected(DropInPaymentMethodType.PAYPAL);
        verify(listener).onPaymentMethodSelected(DropInPaymentMethodType.PAY_WITH_VENMO);
        verify(listener).onPaymentMethodSelected(DropInPaymentMethodType.UNKNOWN);
        verify(listener).onPaymentMethodSelected(DropInPaymentMethodType.GOOGLE_PAYMENT);
        verifyNoMoreInteractions(listener);
    }
}
