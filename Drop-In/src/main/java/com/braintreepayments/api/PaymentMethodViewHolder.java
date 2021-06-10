package com.braintreepayments.api;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class PaymentMethodViewHolder extends RecyclerView.ViewHolder {

    private ImageView icon;
    private TextView title;
    private TextView description;

    PaymentMethodViewHolder(View view) {
        super(view);

        // TODO: fix
//        icon = view.findViewById(R.id.bt_payment_method_icon);
//        title = view.findViewById(R.id.bt_payment_method_title);
//        description = view.findViewById(R.id.bt_payment_method_description);
    }

    void bind(PaymentMethodNonce paymentMethodNonce) {
        DropInPaymentMethodType paymentMethodType = DropInPaymentMethodType.forType(paymentMethodNonce);

        title.setText(paymentMethodType.getLocalizedName());
        icon.setImageResource(paymentMethodType.getVaultedDrawable());

        // TODO: Add PaymentMethodNonceInspector here
        if (paymentMethodNonce instanceof CardNonce) {
            description.setText("••• ••" + ((CardNonce) paymentMethodNonce).getLastTwo());
        }
//        } else {
//            holder.description.setText(paymentMethodNonce.getDescription());
//        }
    }

    void setOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }
}
