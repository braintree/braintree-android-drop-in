package com.braintreepayments.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

import java.util.List;

class VaultedPaymentMethodsAdapter extends RecyclerView.Adapter<VaultedPaymentMethodsAdapter.ViewHolder> {

    private final List<PaymentMethodNonce> mPaymentMethodNonces;

    private final VaultedPaymentMethodSelectedListener callback;

    VaultedPaymentMethodsAdapter(List<PaymentMethodNonce> paymentMethodNonces, VaultedPaymentMethodSelectedListener callback) {
        mPaymentMethodNonces = paymentMethodNonces;
        this.callback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_vaulted_payment_method_card,
                parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = mPaymentMethodNonces.get(position);
        DropInPaymentMethodType paymentMethodType = DropInPaymentMethodType.forType(paymentMethodNonce);

        holder.icon.setImageResource(paymentMethodType.getVaultedDrawable());
        holder.title.setText(paymentMethodType.getLocalizedName());

        if (paymentMethodNonce instanceof CardNonce) {
            holder.description.setText("••• ••" + ((CardNonce) paymentMethodNonce).getLastTwo());
        } else {
            holder.description.setText(paymentMethodNonce.getDescription());
        }

        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onVaultedPaymentMethodSelected(paymentMethodNonce);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPaymentMethodNonces.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView title;
        public TextView description;

        ViewHolder(View view) {
            super(view);

            icon = view.findViewById(R.id.bt_payment_method_icon);
            title = view.findViewById(R.id.bt_payment_method_title);
            description = view.findViewById(R.id.bt_payment_method_description);
        }
    }
}
