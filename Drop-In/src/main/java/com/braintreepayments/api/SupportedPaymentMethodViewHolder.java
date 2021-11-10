package com.braintreepayments.api;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

class SupportedPaymentMethodViewHolder extends RecyclerView.ViewHolder {

    private final ImageView icon;
    private final TextView name;

    SupportedPaymentMethodViewHolder(@NonNull View itemView) {
        super(itemView);

        icon = itemView.findViewById(R.id.bt_payment_method_icon);
        name = itemView.findViewById(R.id.bt_payment_method_type);
    }

    void bind(DropInPaymentMethod paymentMethodType) {
        icon.setImageResource(paymentMethodType.getDrawable());

        Context context = name.getContext();
        name.setText(context.getString(paymentMethodType.getLocalizedName()));
    }

    void setOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }
}
