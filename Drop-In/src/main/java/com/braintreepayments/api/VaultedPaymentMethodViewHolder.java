package com.braintreepayments.api;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

class VaultedPaymentMethodViewHolder extends RecyclerView.ViewHolder {

    private final ImageView icon;
    private final TextView title;
    private final TextView description;

    private final PaymentMethodNonceInspector nonceInspector = new PaymentMethodNonceInspector();
    private final DropInPaymentMethodHelper paymentMethodHelper = new DropInPaymentMethodHelper();

    VaultedPaymentMethodViewHolder(View view) {
        super(view);

        icon = view.findViewById(R.id.bt_payment_method_icon);
        title = view.findViewById(R.id.bt_payment_method_title);
        description = view.findViewById(R.id.bt_payment_method_description);
    }

    void bind(PaymentMethodNonce paymentMethodNonce) {
        DropInPaymentMethodType paymentMethodType = nonceInspector.getPaymentMethodType(paymentMethodNonce);

        title.setText(paymentMethodHelper.getLocalizedName(paymentMethodType));
        icon.setImageResource(paymentMethodType.getVaultedDrawable());
        description.setText(nonceInspector.getDescription(paymentMethodNonce));
    }

    void setOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }
}
