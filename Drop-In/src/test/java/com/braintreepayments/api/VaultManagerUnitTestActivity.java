package com.braintreepayments.api;

import android.content.Context;
import android.os.Bundle;

import com.braintreepayments.api.dropin.R;
import static org.mockito.Mockito.spy;

public class VaultManagerUnitTestActivity extends VaultManagerActivity {

    public Context context;

    public DropInClient dropInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.bt_vault_manager_activity_theme);
        super.onCreate(savedInstanceState);

    }

    @Override
    DropInClient getDropInClient() {
        if (dropInClient == null) {
            dropInClient = super.getDropInClient();
        }
        return dropInClient;
    }

    @Override
    public Context getApplicationContext() {
        if (context != null) {
            return context;
        }

        return super.getApplicationContext();
    }

    public VaultManagerPaymentMethodsAdapter mockAdapter() {
        VaultManagerPaymentMethodsAdapter spiedAdapter = spy(mAdapter);
        mAdapter = spiedAdapter;

        return spiedAdapter;
    }

    @Override
    public void onPaymentMethodNonceDeleted(PaymentMethodNonce paymentMethodNonce) {
        super.onPaymentMethodNonceDeleted(paymentMethodNonce);
    }
}
