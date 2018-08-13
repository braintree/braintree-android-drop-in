package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.dropin.adapters.VaultManagerPaymentMethodsAdapter;
import com.braintreepayments.api.dropin.helper.VaultManagerHelper;
import com.braintreepayments.api.dropin.view.PaymentMethodItemView;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.PaymentMethodDeleteException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceDeletedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.braintreepayments.api.dropin.DropInActivity.EXTRA_PAYMENT_METHOD_NONCES;

public class VaultManagerActivity extends BaseActivity implements PaymentMethodNonceDeletedListener,
        BraintreeErrorListener, VaultManagerHelper.Interaction {

    @VisibleForTesting
    protected VaultManagerPaymentMethodsAdapter mAdapter = new VaultManagerPaymentMethodsAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_vault_management_activity);

        RecyclerView vaultManagerView = findViewById(R.id.bt_vault_manager_list);

        try {
            mBraintreeFragment = getBraintreeFragment();
        } catch (InvalidArgumentException e) {
            finish(e);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        VaultManagerHelper swipeController = new VaultManagerHelper(this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);

        ArrayList<PaymentMethodNonce> nonces;
        if (savedInstanceState == null) {
            nonces = getIntent().getParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES);
        } else {
            nonces = savedInstanceState.getParcelableArrayList(EXTRA_PAYMENT_METHOD_NONCES);
        }

        mAdapter.setPaymentMethodNonces(nonces);

        new LinearSnapHelper().attachToRecyclerView(vaultManagerView);
        itemTouchHelper.attachToRecyclerView(vaultManagerView);
        vaultManagerView.setLayoutManager(layoutManager);
        vaultManagerView.addItemDecoration(dividerItemDecoration);
        vaultManagerView.setAdapter(mAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_PAYMENT_METHOD_NONCES, mAdapter.getPaymentMethodNonces());
    }

    @Override
    public void onPaymentMethodNonceDeleted(PaymentMethodNonce paymentMethodNonce) {
        mAdapter.paymentMethodDeleted(paymentMethodNonce);

        mBraintreeFragment.sendAnalyticsEvent("manager.delete.succeeded");
        setResult(Activity.RESULT_OK);
    }

    @Override
    public void onError(Exception error) {
        if(error instanceof PaymentMethodDeleteException) {
            PaymentMethodDeleteException exception = (PaymentMethodDeleteException)error;
            PaymentMethodNonce paymentMethodNonce = exception.getPaymentMethodNonce();

            mAdapter.cancelSwipeOnPaymentMethodNonce(paymentMethodNonce);

            Snackbar.make(findViewById(R.id.bt_base_view), "We couldn't delete your payment method right now, try again later", Snackbar.LENGTH_LONG).show();
            mBraintreeFragment.sendAnalyticsEvent("manager.delete.failed");
        } else {
            mBraintreeFragment.sendAnalyticsEvent("manager.unknown.failed");
            finish(error);
        }
    }

    @Override
    public void onSwipe(int index) {
        final PaymentMethodNonce paymentMethodNonceToDelete =
                mAdapter.getPaymentMethodNonce(index);

        final AtomicBoolean positiveSelected = new AtomicBoolean(false);

        PaymentMethodItemView paymentMethodItem = new PaymentMethodItemView(this);
        paymentMethodItem.setPaymentMethod(paymentMethodNonceToDelete);

        new AlertDialog.Builder(VaultManagerActivity.this)
                .setTitle(R.string.bt_delete_confirmation_title)
                .setMessage(R.string.bt_delete_confirmation_description)
                .setView(paymentMethodItem)
                .setPositiveButton(R.string.bt_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        positiveSelected.set(true);
                        mBraintreeFragment.sendAnalyticsEvent("manager.delete.confirmation.positive");
                        PaymentMethod.deletePaymentMethod(mBraintreeFragment, paymentMethodNonceToDelete);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!positiveSelected.get()) {
                            mAdapter.cancelSwipeOnPaymentMethodNonce(paymentMethodNonceToDelete);
                            mBraintreeFragment.sendAnalyticsEvent("manager.delete.confirmation.negative");
                        }
                    }
                })
                .setNegativeButton(R.string.bt_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }
}
