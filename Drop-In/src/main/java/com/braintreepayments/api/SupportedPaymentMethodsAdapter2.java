package com.braintreepayments.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

import java.util.List;

class SupportedPaymentMethodsAdapter2 extends RecyclerView.Adapter<SupportedPaymentMethodViewHolder> {

    private final List<DropInPaymentMethodType> supportedPaymentMethods;
    private final SupportedPaymentMethodSelectedListener listener;

    SupportedPaymentMethodsAdapter2(List<DropInPaymentMethodType> supportedPaymentMethods, SupportedPaymentMethodSelectedListener listener) {
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
        final DropInPaymentMethodType paymentMethodType = supportedPaymentMethods.get(position);
        holder.bind(paymentMethodType);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPaymentMethodSelected(paymentMethodType);
            }
        });
    }

    @Override
    public int getItemCount() {
        return supportedPaymentMethods.size();
    }
}
