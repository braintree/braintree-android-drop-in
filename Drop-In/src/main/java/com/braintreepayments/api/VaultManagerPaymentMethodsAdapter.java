package com.braintreepayments.api;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

class VaultManagerPaymentMethodsAdapter extends RecyclerView.Adapter<VaultManagerPaymentMethodsAdapter.ViewHolder> {
    private List<PaymentMethodNonce> vaultedPaymentMethodNonces;
    private View.OnClickListener mClickListener;

    VaultManagerPaymentMethodsAdapter(View.OnClickListener clickListener, List<PaymentMethodNonce> vaultedPaymentMethodNonces) {
        mClickListener = clickListener;
        this.vaultedPaymentMethodNonces = vaultedPaymentMethodNonces;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new PaymentMethodItemView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = vaultedPaymentMethodNonces.get(position);
        final PaymentMethodItemView paymentMethodItemView = ((PaymentMethodItemView)holder.itemView);

        paymentMethodItemView.setPaymentMethod(paymentMethodNonce, true);
        paymentMethodItemView.setOnDeleteIconClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onClick(paymentMethodItemView);
                }
            }
        });
    }

    PaymentMethodNonce getPaymentMethodNonce(int index) {
        return vaultedPaymentMethodNonces.get(index);
    }

    void paymentMethodDeleted(PaymentMethodNonce paymentMethodNonce) {
        int index = vaultedPaymentMethodNonces.indexOf(paymentMethodNonce);
        vaultedPaymentMethodNonces.remove(index);
        this.notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return vaultedPaymentMethodNonces.size();
    }

    void setPaymentMethodNonces(List<PaymentMethodNonce> paymentMethodNonces) {
        vaultedPaymentMethodNonces.clear();
        vaultedPaymentMethodNonces.addAll(paymentMethodNonces);
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

