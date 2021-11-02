package com.braintreepayments.api;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

class VaultManagerPaymentMethodsAdapter extends RecyclerView.Adapter<VaultManagerPaymentMethodsAdapter.ViewHolder> {
    private final List<PaymentMethodNonce> vaultedPaymentMethodNonces;
    private final View.OnClickListener clickListener;

    VaultManagerPaymentMethodsAdapter(View.OnClickListener clickListener, List<PaymentMethodNonce> vaultedPaymentMethodNonces) {
        this.clickListener = clickListener;
        this.vaultedPaymentMethodNonces = vaultedPaymentMethodNonces;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new PaymentMethodItemView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = vaultedPaymentMethodNonces.get(position);
        final PaymentMethodItemView paymentMethodItemView = ((PaymentMethodItemView)holder.itemView);

        paymentMethodItemView.setPaymentMethod(paymentMethodNonce, true);
        paymentMethodItemView.setOnDeleteIconClick(v -> {
            if (clickListener != null) {
                clickListener.onClick(paymentMethodItemView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vaultedPaymentMethodNonces.size();
    }

    ArrayList<PaymentMethodNonce> getPaymentMethodNonces() {
        return new ArrayList<>(vaultedPaymentMethodNonces);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }
}

