package com.braintreepayments.api.dropin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

// NEXT MAJOR VERSION make this class package private
public class VaultedPaymentMethodsAdapter extends RecyclerView.Adapter<VaultedPaymentMethodsAdapter.ViewHolder> {

    private final PaymentMethodNonceCreatedListener mSelectedListener;

    private final List<PaymentMethodNonce> mPaymentMethodNonces;
    private AvailablePaymentMethodNonceList mAvailablePaymentMethodNonces;

    public VaultedPaymentMethodsAdapter(PaymentMethodNonceCreatedListener listener,
                                        List<PaymentMethodNonce> paymentMethodNonces) {
        mSelectedListener = listener;
        mPaymentMethodNonces = paymentMethodNonces;
    }

    public void setup(Context context, Configuration configuration, DropInRequest dropInRequest, boolean googlePayEnabled, boolean unionpaySupported) {
        mAvailablePaymentMethodNonces = new AvailablePaymentMethodNonceList(
                context, configuration, mPaymentMethodNonces, dropInRequest, googlePayEnabled);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_vaulted_payment_method_card,
                parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentMethodNonce paymentMethodNonce = mAvailablePaymentMethodNonces.get(position);
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
        return mAvailablePaymentMethodNonces.size();
    }

    public boolean hasCardNonce() {
        return mAvailablePaymentMethodNonces.hasCardNonce();
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
