package com.braintreepayments.api.dropin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.PaymentMethodClickListener;
import com.braintreepayments.api.models.Configuration;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodGridViewAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private PaymentMethodClickListener mPaymentMethodClickListener;
    private List<PaymentMethod> mEnabledPaymentMethods;

    enum PaymentMethod {
        CARD(R.string.bt_form_pay_with_card_header, R.drawable.bt_ic_payment_method_card),
        PAYPAL(R.string.bt_pay_with_paypal, R.drawable.bt_logo_paypal),
        VENMO(R.string.bt_pay_with_venmo, R.drawable.bt_logo_venmo),
        ANDROID_PAY(R.string.bt_pay_with_android_pay, R.drawable.bt_logo_android_pay);

        private int mDescription;
        private int mIcon;

        PaymentMethod(int description, int icon) {
            mDescription = description;
            mIcon = icon;
        }
    }

    public PaymentMethodGridViewAdapter(Context context, PaymentMethodClickListener listener,
            Configuration configuration) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPaymentMethodClickListener = listener;

        mEnabledPaymentMethods = new ArrayList<>();
        mEnabledPaymentMethods.add(PaymentMethod.CARD);
        if (configuration.isPayPalEnabled()) {
            mEnabledPaymentMethods.add(PaymentMethod.PAYPAL);
        }
        if (configuration.getPayWithVenmo().isEnabled(context)) {
            mEnabledPaymentMethods.add(PaymentMethod.VENMO);
        }
        if (configuration.getAndroidPay().isEnabled(context)) {
            mEnabledPaymentMethods.add(PaymentMethod.ANDROID_PAY);
        }
    }

    @Override
    public int getCount() {
        return mEnabledPaymentMethods.size();
    }

    @Override
    public Object getItem(int position) {
        return mEnabledPaymentMethods.get(0);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        convertView = mLayoutInflater.inflate(R.layout.bt_grid_view_item, null);

        final PaymentMethod paymentMethod = mEnabledPaymentMethods.get(position);
        ((ImageView) convertView.findViewById(R.id.bt_payment_method_icon)).setImageResource(paymentMethod.mIcon);
        ((TextView) convertView.findViewById(R.id.bt_payment_method_description)).setText(paymentMethod.mDescription);
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentMethod == PaymentMethod.CARD) {
                    mPaymentMethodClickListener.onCardClick();
                } else if (paymentMethod == PaymentMethod.PAYPAL) {
                    mPaymentMethodClickListener.onPayPalClick();
                } else if (paymentMethod == PaymentMethod.VENMO) {
                    mPaymentMethodClickListener.onVenmoClick();
                } else if (paymentMethod == PaymentMethod.ANDROID_PAY) {
                    mPaymentMethodClickListener.onAndroidPayClick();
                }
            }
        });

        return convertView;
    }
}
