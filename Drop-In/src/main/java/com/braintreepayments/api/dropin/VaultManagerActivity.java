package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.dropin.adapters.VaultManagerPaymentMethodsAdapter;
import com.braintreepayments.api.dropin.adapters.VaultedPaymentMethodsAdapter;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.interfaces.PaymentMethodNoncesUpdatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.List;

import static com.braintreepayments.api.dropin.DropInRequest.EXTRA_CHECKOUT_REQUEST;

public class
VaultManagerActivity extends BaseActivity implements PaymentMethodNoncesUpdatedListener,
        PaymentMethodNonceCreatedListener {

    private RecyclerView mVaultManagerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_vault_management_activity);

        mVaultManagerView = findViewById(R.id.bt_vault_manager_list);
        mVaultManagerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        new LinearSnapHelper().attachToRecyclerView(mVaultManagerView);


        try {
            mBraintreeFragment = getBraintreeFragment();
        } catch (InvalidArgumentException e) {
            finish(e);
            return;
        }

        fetchPaymentMethodNonces(false);
    }

    private void fetchPaymentMethodNonces(final boolean refetch) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!VaultManagerActivity.this.isFinishing()) {
                    if (mBraintreeFragment.hasFetchedPaymentMethodNonces() && !refetch) {
                        onPaymentMethodNoncesUpdated(mBraintreeFragment.getCachedPaymentMethodNonces());
                    } else {
                        PaymentMethod.getPaymentMethodNonces(mBraintreeFragment, true);
                    }
                }
            }
        }, getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    @Override
    public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {
        mVaultManagerView.setAdapter(new VaultManagerPaymentMethodsAdapter(paymentMethodNonces));
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {

    }

    public void onPaymentMethodNonceSwiped(PaymentMethodNonce paymentMethodNonce) {
        // TODO move this to swipe listener and delete this function.
        PaymentMethod.deletePaymentMethod(mBraintreeFragment, paymentMethodNonce.getNonce());
        onPaymentMethodDeleted(true);
    }

    // @Override
    public void onPaymentMethodDeleted(boolean success) {
        PaymentMethod.getPaymentMethodNonces(mBraintreeFragment, true);
        if(mBraintreeFragment.getCachedPaymentMethodNonces().size() > 0) {
            onPaymentMethodNoncesUpdated(mBraintreeFragment.getCachedPaymentMethodNonces());

            setResult(Activity.RESULT_OK, new Intent()
                    .putExtra("TODO use the same key as the result we look up in DropinActivity", (Parcelable) mBraintreeFragment.getCachedPaymentMethodNonces()));
            finish();
        }
    }
}
