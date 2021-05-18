package com.braintreepayments.api;

import android.content.Context;
import android.os.Bundle;

import com.braintreepayments.api.dropin.R;
import static org.mockito.Mockito.spy;

public class VaultManagerUnitTestActivity extends VaultManagerActivity {

    public Context context;
    public BraintreeClient braintreeFragment;
    public BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient();
    public BraintreeGraphQLHttpClient graphQLHttpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.bt_vault_manager_activity_theme);

        ConfigurationManagerTestUtils.setFetchingConfiguration(true);

        super.onCreate(savedInstanceState);

        if (braintreeFragment != null) {
            ConfigurationManagerTestUtils.setFetchingConfiguration(false);
        }
    }

    @Override
    protected BraintreeClient getBraintreeClient() {
        if (braintreeFragment == null) {
            braintreeFragment = super.getBraintreeClient();
//            setHttpClient(braintreeFragment, httpClient);
        }

        if (graphQLHttpClient != null) {
//            setGraphQlClient(braintreeFragment, graphQLHttpClient);
        }

        return braintreeFragment;
    }

    @Override
    public Context getApplicationContext() {
        if (context != null) {
            return context;
        }

        return super.getApplicationContext();
    }

    public BraintreeClient mockFragment() {
        braintreeFragment = spy(braintreeFragment);

        return braintreeFragment;
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
