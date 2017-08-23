package com.braintreepayments.api.dropin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.List;

public class VaultedPaymentMethodsAdapter extends RecyclerView.Adapter<VaultedPaymentMethodsAdapter.ViewHolder> {

    private PaymentMethodNonceCreatedListener mSelectedListener;
    private List<PaymentMethodNonce> mPaymentMethodNonces;

    public VaultedPaymentMethodsAdapter(PaymentMethodNonceCreatedListener listener,
            List<PaymentMethodNonce> paymentMethodNonces) {
        mSelectedListener = listener;
        mPaymentMethodNonces = paymentMethodNonces;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_vaulted_payment_method_card,
                parent, false));
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

        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedListener.onPaymentMethodNonceCreated(paymentMethodNonce);
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
