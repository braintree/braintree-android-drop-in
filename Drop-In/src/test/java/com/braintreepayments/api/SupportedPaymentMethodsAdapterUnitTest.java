package com.braintreepayments.api;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class SupportedPaymentMethodsAdapterUnitTest {

    @Test
    public void callsOnPaymentMethodSelectedListenerWhenPaymentMethodClicked() {
        Context context = ApplicationProvider.getApplicationContext();
        LinearLayout parent = new LinearLayout(context);

        SupportedPaymentMethodSelectedListener listener = mock(SupportedPaymentMethodSelectedListener.class);
        ArrayList<DropInPaymentMethodType> supportedPaymentMethods = new ArrayList<>();
        supportedPaymentMethods.add(DropInPaymentMethodType.PAYPAL);
        supportedPaymentMethods.add(DropInPaymentMethodType.PAY_WITH_VENMO);
        supportedPaymentMethods.add(DropInPaymentMethodType.UNKNOWN);
        supportedPaymentMethods.add(DropInPaymentMethodType.GOOGLE_PAYMENT);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                supportedPaymentMethods, listener);

        adapter.getView(0, null, parent).callOnClick();
        adapter.getView(1, null, parent).callOnClick();
        adapter.getView(2, null, parent).callOnClick();
        adapter.getView(3, null, parent).callOnClick();

        verify(listener).onPaymentMethodSelected(DropInPaymentMethodType.PAYPAL);
        verify(listener).onPaymentMethodSelected(DropInPaymentMethodType.PAY_WITH_VENMO);
        verify(listener).onPaymentMethodSelected(DropInPaymentMethodType.UNKNOWN);
        verify(listener).onPaymentMethodSelected(DropInPaymentMethodType.GOOGLE_PAYMENT);
        verifyNoMoreInteractions(listener);
    }
}
