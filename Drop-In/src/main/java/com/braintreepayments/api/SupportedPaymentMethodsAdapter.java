package com.braintreepayments.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.LiveData;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.models.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SupportedPaymentMethodsAdapter extends BaseAdapter {

    private Context mContext;
    private LiveData<List<PaymentMethodType>> mAvailablePaymentMethods;
    private PaymentMethodSelectedListener mPaymentMethodSelectedListener;

    SupportedPaymentMethodsAdapter(Context context, PaymentMethodSelectedListener paymentMethodSelectedListener, LiveData<List<PaymentMethodType>> availablePaymentMethods) {
        mContext = context;
        mPaymentMethodSelectedListener = paymentMethodSelectedListener;
        mAvailablePaymentMethods = availablePaymentMethods;
    }

    @Override
    public int getCount() {
        return mAvailablePaymentMethods.getValue().size();
    }

    @Override
    public Object getItem(int position) {
        return mAvailablePaymentMethods.getValue().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bt_payment_method_list_item, parent, false);
        }

        final PaymentMethodType type = mAvailablePaymentMethods.getValue().get(position);

        ImageView icon = convertView.findViewById(R.id.bt_payment_method_icon);
        icon.setImageResource(type.getDrawable());

        ((TextView) convertView.findViewById(R.id.bt_payment_method_type))
                .setText(mContext.getString(type.getLocalizedName()));

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaymentMethodSelectedListener.onPaymentMethodSelected(type);
            }
        });

        return convertView;
    }

    interface PaymentMethodSelectedListener {
        void onPaymentMethodSelected(PaymentMethodType type);
    }
}
