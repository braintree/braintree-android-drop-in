package com.braintreepayments.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.braintreepayments.api.dropin.R;

class VaultedPaymentMethodsAdapter extends RecyclerView.Adapter<VaultedPaymentMethodViewHolder> {

    private final List<PaymentMethodNonce> paymentMethodNonces;
    private final VaultedPaymentMethodSelectedListener listener;

    VaultedPaymentMethodsAdapter(List<PaymentMethodNonce> paymentMethodNonces, VaultedPaymentMethodSelectedListener listener) {
        this.listener = listener;
        this.paymentMethodNonces = paymentMethodNonces;
    }

    @NonNull
    @Override
    public VaultedPaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view =
            inflater.inflate(R.layout.bt_vaulted_payment_method_card, parent, false);
        return new VaultedPaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VaultedPaymentMethodViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = paymentMethodNonces.get(position);
        holder.bind(paymentMethodNonce);
        holder.setOnClickListener(v -> listener.onVaultedPaymentMethodSelected(paymentMethodNonce));
    }

    @Override
    public int getItemCount() {
        return paymentMethodNonces.size();
    }
}
