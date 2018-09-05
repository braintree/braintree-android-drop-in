package com.braintreepayments.api;

import android.content.Context;
import android.os.Bundle;

import com.braintreepayments.api.dropin.BraintreeUnitTestHttpClient;
import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.VaultManagerActivity;
import com.braintreepayments.api.dropin.adapters.VaultManagerPaymentMethodsAdapter;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.internal.BraintreeGraphQLHttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;

import static com.braintreepayments.api.BraintreeFragmentTestUtils.setGraphQlClient;
import static com.braintreepayments.api.BraintreeFragmentTestUtils.setHttpClient;
import static org.mockito.Mockito.spy;

public class VaultManagerUnitTestActivity extends VaultManagerActivity {

    public Context context;
    public BraintreeFragment braintreeFragment;
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
    protected BraintreeFragment getBraintreeFragment() throws InvalidArgumentException {
        if (braintreeFragment == null) {
            braintreeFragment = super.getBraintreeFragment();
            setHttpClient(braintreeFragment, httpClient);
        }

        if (graphQLHttpClient != null) {
            setGraphQlClient(braintreeFragment, graphQLHttpClient);
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

    public BraintreeFragment mockFragment() {
        mBraintreeFragment = spy(mBraintreeFragment);

        return mBraintreeFragment;
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
