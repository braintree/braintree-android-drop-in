package com.braintreepayments.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

import java.util.List;

class SupportedPaymentMethodsAdapter extends RecyclerView.Adapter<SupportedPaymentMethodViewHolder> {

    private final List<DropInPaymentMethod> supportedPaymentMethods;
    private final SupportedPaymentMethodSelectedListener listener;

    SupportedPaymentMethodsAdapter(List<DropInPaymentMethod> supportedPaymentMethods, SupportedPaymentMethodSelectedListener listener) {
        this.listener = listener;
        this.supportedPaymentMethods = supportedPaymentMethods;
    }

    @NonNull
    @Override
    public SupportedPaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view =
                inflater.inflate(R.layout.bt_payment_method_list_item, parent, false);
        return new SupportedPaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupportedPaymentMethodViewHolder holder, int position) {
        final DropInPaymentMethod paymentMethodType = supportedPaymentMethods.get(position);
        holder.bind(paymentMethodType);
        holder.setOnClickListener(v -> listener.onPaymentMethodSelected(paymentMethodType));
    }

    @Override
    public int getItemCount() {
        return supportedPaymentMethods.size();
    }
}
