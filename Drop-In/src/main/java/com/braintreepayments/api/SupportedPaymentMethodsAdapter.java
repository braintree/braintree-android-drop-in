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
import com.braintreepayments.api.models.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class SupportedPaymentMethodsAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<PaymentMethodType> mAvailablePaymentMethods;
    private PaymentMethodSelectedListener mPaymentMethodSelectedListener;

    SupportedPaymentMethodsAdapter(Context context, PaymentMethodSelectedListener paymentMethodSelectedListener) {
        mContext = context;
        mPaymentMethodSelectedListener = paymentMethodSelectedListener;
        mAvailablePaymentMethods = new ArrayList<>();
    }

    void setup(Configuration configuration, DropInRequest dropInRequest, boolean googlePayEnabled, boolean unionpaySupported) {
        if (dropInRequest.isPayPalEnabled() && configuration.isPayPalEnabled()) {
            mAvailablePaymentMethods.add(PaymentMethodType.PAYPAL);
        }

        if (dropInRequest.isVenmoEnabled() && configuration.getPayWithVenmo().isEnabled(mContext)) {
            mAvailablePaymentMethods.add(PaymentMethodType.PAY_WITH_VENMO);
        }

        if (dropInRequest.isCardEnabled()) {
            Set<String> supportedCardTypes =
                    new HashSet<>(configuration.getCardConfiguration().getSupportedCardTypes());
            if (!unionpaySupported) {
                supportedCardTypes.remove(PaymentMethodType.UNIONPAY.getCanonicalName());
            }
            if (supportedCardTypes.size() > 0) {
                mAvailablePaymentMethods.add(PaymentMethodType.UNKNOWN);
            }
        }

        if (googlePayEnabled) {
            if (dropInRequest.isGooglePaymentEnabled()) {
                mAvailablePaymentMethods.add(PaymentMethodType.GOOGLE_PAYMENT);
            }
        }
    }

    @Override
    public int getCount() {
        return mAvailablePaymentMethods.size();
    }

    @Override
    public Object getItem(int position) {
        return mAvailablePaymentMethods.get(position);
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

        final PaymentMethodType type = mAvailablePaymentMethods.get(position);

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
