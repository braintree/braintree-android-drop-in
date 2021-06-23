package com.braintreepayments.api;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SupportedPaymentMethodsAdapterUnitTest {

    @Test
    public void callsOnPaymentMethodSelectedListenerWhenPaymentMethodClicked() {
        Context context = ApplicationProvider.getApplicationContext();
        LinearLayout parent = new LinearLayout(context);

        SupportedPaymentMethodSelectedListener listener = mock(SupportedPaymentMethodSelectedListener.class);
        ArrayList<Integer> supportedPaymentMethods = new ArrayList<>();
        supportedPaymentMethods.add(SupportedPaymentMethodType.PAYPAL);
        supportedPaymentMethods.add(SupportedPaymentMethodType.VENMO);
        supportedPaymentMethods.add(SupportedPaymentMethodType.CARD);
        supportedPaymentMethods.add(SupportedPaymentMethodType.GOOGLE_PAY);

        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                context, listener, supportedPaymentMethods);

        adapter.getView(0, null, parent).callOnClick();
        adapter.getView(1, null, parent).callOnClick();
        adapter.getView(2, null, parent).callOnClick();
        adapter.getView(3, null, parent).callOnClick();

        verify(listener).onPaymentMethodSelected(SupportedPaymentMethodType.PAYPAL);
        verify(listener).onPaymentMethodSelected(SupportedPaymentMethodType.VENMO);
        verify(listener).onPaymentMethodSelected(SupportedPaymentMethodType.CARD);
        verify(listener).onPaymentMethodSelected(SupportedPaymentMethodType.GOOGLE_PAY);
        verifyNoMoreInteractions(listener);
    }
}
