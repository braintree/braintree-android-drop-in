package com.braintreepayments.api;

import android.content.Context;
import android.view.View;

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
                .setPositiveButton(R.string.bt_delete, (dialog, which) -> callback.onDialogInteraction(DialogInteraction.POSITIVE))
                .setOnDismissListener(dialog -> callback.onDialogInteraction(DialogInteraction.NEGATIVE))
                .setNegativeButton(R.string.bt_cancel, null)
                .create()
                .show();
    }

    void showSnackbarText(View targetView, int textResId, int duration) {
        Snackbar.make(targetView, textResId, duration).show();
    }
}
