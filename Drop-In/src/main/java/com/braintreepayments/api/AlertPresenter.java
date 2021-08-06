package com.braintreepayments.api;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;

import com.braintreepayments.api.dropin.R;
import com.google.android.material.snackbar.Snackbar;

class AlertPresenter {

    void showConfirmNonceDeletionDialog(Context context, PaymentMethodNonce paymentMethodNonceToDelete, final DialogInteractionCallback callback) {
        PaymentMethodItemView dialogView = new PaymentMethodItemView(context);
        dialogView.setPaymentMethod(paymentMethodNonceToDelete, false);

        new AlertDialog.Builder(context,
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.bt_delete_confirmation_title)
                .setMessage(R.string.bt_delete_confirmation_description)
                .setView(dialogView)
                .setPositiveButton(R.string.bt_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onDialogInteraction(DialogInteraction.POSITIVE);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        callback.onDialogInteraction(DialogInteraction.NEGATIVE);
                    }
                })
                .setNegativeButton(R.string.bt_cancel, null)
                .create()
                .show();
    }

    void showSnackbarText(View targetView, int textResId, int duration) {
        Snackbar.make(targetView, textResId, duration).show();
    }
}
