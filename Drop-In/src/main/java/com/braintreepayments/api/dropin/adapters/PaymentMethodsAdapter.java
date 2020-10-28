package com.braintreepayments.api.dropin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;

import java.util.List;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.ViewHolder> {

    private List<PaymentMethodType> mAvailablePaymentMethods;
    private Context mContext;
    private SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener mListener;

    public PaymentMethodsAdapter(List<PaymentMethodType> availablePaymentMethods, SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener listener, Context context) {
        mAvailablePaymentMethods = availablePaymentMethods;
        mContext = context;
        mListener = listener;
    }

    @Override
    public PaymentMethodsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PaymentMethodsAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_payment_method_list_item,
                parent, false));
    }

    @Override
    public void onBindViewHolder(PaymentMethodsAdapter.ViewHolder holder, int position) {
        PaymentMethodType type = mAvailablePaymentMethods.get(position);
        holder.bind(type, mContext, mListener);
    }

    @Override
    public int getItemCount() {
        return mAvailablePaymentMethods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView typeTextView;
        private View paymentMethodView;

        ViewHolder(View view) {
            super(view);
            paymentMethodView = view;
            icon = view.findViewById(R.id.bt_payment_method_icon);
            typeTextView = view.findViewById(R.id.bt_payment_method_type);
        }

        void bind(final PaymentMethodType type, Context context, final SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener listener) {
            icon.setImageResource(type.getDrawable());
            typeTextView.setText(context.getString(type.getLocalizedName()));

            if (listener != null) {
                paymentMethodView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onPaymentMethodSelected(type);
                    }
                });
            }
        }
    }
}
