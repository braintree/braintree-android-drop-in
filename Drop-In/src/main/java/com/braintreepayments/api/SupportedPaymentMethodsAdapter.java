package com.braintreepayments.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;

import java.util.List;

class SupportedPaymentMethodsAdapter extends BaseAdapter {

    private final List<DropInPaymentMethodType> supportedPaymentMethods;
    private final PaymentMethodSelectedListener selectedListener;

    SupportedPaymentMethodsAdapter(List<DropInPaymentMethodType> supportedPaymentMethods, PaymentMethodSelectedListener selectedListener) {
        this.selectedListener = selectedListener;
        this.supportedPaymentMethods = supportedPaymentMethods;
    }

    @Override
    public int getCount() {
        return supportedPaymentMethods.size();
    }

    @Override
    public Object getItem(int position) {
        return supportedPaymentMethods.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.bt_payment_method_list_item, parent, false);
        }

        final DropInPaymentMethodType type = supportedPaymentMethods.get(position);

        ImageView icon = convertView.findViewById(R.id.bt_payment_method_icon);
        icon.setImageResource(type.getDrawable());

        ((TextView) convertView.findViewById(R.id.bt_payment_method_type))
                .setText(context.getString(type.getLocalizedName()));

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedListener.onPaymentMethodSelected(type);
            }
        });

        return convertView;
    }

    interface PaymentMethodSelectedListener {
        void onPaymentMethodSelected(DropInPaymentMethodType type);
    }
}
