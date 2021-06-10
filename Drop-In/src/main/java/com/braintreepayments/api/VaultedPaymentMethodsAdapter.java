package com.braintreepayments.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class VaultedPaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodViewHolder> {

    private final List<PaymentMethodNonce> paymentMethodNonces;
    private final VaultedPaymentMethodSelectedListener listener;

    VaultedPaymentMethodsAdapter(List<PaymentMethodNonce> paymentMethodNonces, VaultedPaymentMethodSelectedListener listener) {
        this.listener = listener;
        this.paymentMethodNonces = paymentMethodNonces;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PaymentMethodViewHolder(inflater.inflate(R.layout.bt_vaulted_payment_method_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = paymentMethodNonces.get(position);
        holder.bind(paymentMethodNonce);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onVaultedPaymentMethodSelected(paymentMethodNonce);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethodNonces.size();
    }
}
