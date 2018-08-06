package com.braintreepayments.api.dropin.adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class VaultManagerPaymentMethodsAdapter extends RecyclerView.Adapter<VaultManagerPaymentMethodsAdapter.ViewHolder> {
    private final List<PaymentMethodNonce> mPaymentMethodNonces = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bt_vault_manager_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = mPaymentMethodNonces.get(position);
        PaymentMethodType paymentMethodType = PaymentMethodType.forType(paymentMethodNonce);

        holder.icon.setImageResource(paymentMethodType.getVaultedDrawable());
        holder.title.setText(paymentMethodType.getLocalizedName());
        if (paymentMethodNonce instanceof CardNonce) {
            holder.description.setText("••• ••" + ((CardNonce) paymentMethodNonce).getLastTwo());
        } else {
            holder.description.setText(paymentMethodNonce.getDescription());
        }
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
        public ImageView icon;
        public TextView title;
        public TextView description;
        public View foreground;
        public View background;

        ViewHolder(View view) {
            super(view);

            icon = view.findViewById(R.id.bt_payment_method_icon);
            title = view.findViewById(R.id.bt_payment_method_title);
            description = view.findViewById(R.id.bt_payment_method_description);
            foreground = view.findViewById(R.id.bt_payment_method_foreground);
            background = view.findViewById(R.id.bt_payment_method_background);
        }
    }
}

