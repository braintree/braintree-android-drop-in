package com.braintreepayments.api.dropin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.braintreepayments.api.dropin.view.PaymentMethodItemView;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.List;

public class VaultManagerPaymentMethodsAdapter extends RecyclerView.Adapter<VaultManagerPaymentMethodsAdapter.ViewHolder> {
    private final List<PaymentMethodNonce> mPaymentMethodNonces = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new PaymentMethodItemView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = mPaymentMethodNonces.get(position);
        PaymentMethodItemView paymentMethodItemView = ((PaymentMethodItemView)holder.itemView);

        paymentMethodItemView.setPaymentMethod(paymentMethodNonce);
        paymentMethodItemView.setDeleteIconVisible(true);
    }

    public PaymentMethodNonce getPaymentMethodNonce(int index) {
        return mPaymentMethodNonces.get(index);
    }

    public void paymentMethodDeleted(PaymentMethodNonce paymentMethodNonce) {
        int index = mPaymentMethodNonces.indexOf(paymentMethodNonce);
        mPaymentMethodNonces.remove(index);
        this.notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mPaymentMethodNonces.size();
    }

    public void setPaymentMethodNonces(List<PaymentMethodNonce> paymentMethodNonces) {
        mPaymentMethodNonces.clear();
        mPaymentMethodNonces.addAll(paymentMethodNonces);
    }

    public ArrayList<PaymentMethodNonce> getPaymentMethodNonces() {
        return new ArrayList<>(mPaymentMethodNonces);
    }

    public void cancelSwipeOnPaymentMethodNonce(PaymentMethodNonce paymentMethodNonce) {
        int index = mPaymentMethodNonces.indexOf(paymentMethodNonce);

        mPaymentMethodNonces.remove(index);
        mPaymentMethodNonces.add(index, paymentMethodNonce);

        this.notifyItemChanged(index);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }
}

