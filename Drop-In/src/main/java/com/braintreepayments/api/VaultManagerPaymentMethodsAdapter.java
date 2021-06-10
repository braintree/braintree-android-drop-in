package com.braintreepayments.api;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.List;

class VaultManagerPaymentMethodsAdapter extends RecyclerView.Adapter<VaultManagerPaymentMethodsAdapter.ViewHolder> {
    private final List<PaymentMethodNonce> mPaymentMethodNonces = new ArrayList<>();
    private View.OnClickListener mClickListener;

    VaultManagerPaymentMethodsAdapter(View.OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new PaymentMethodItemView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = mPaymentMethodNonces.get(position);
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
        return mPaymentMethodNonces.get(index);
    }

    void paymentMethodDeleted(PaymentMethodNonce paymentMethodNonce) {
        int index = mPaymentMethodNonces.indexOf(paymentMethodNonce);
        mPaymentMethodNonces.remove(index);
        this.notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mPaymentMethodNonces.size();
    }

    void setPaymentMethodNonces(List<PaymentMethodNonce> paymentMethodNonces) {
        mPaymentMethodNonces.clear();
        mPaymentMethodNonces.addAll(paymentMethodNonces);
    }

    ArrayList<PaymentMethodNonce> getPaymentMethodNonces() {
        return new ArrayList<>(mPaymentMethodNonces);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }
}

