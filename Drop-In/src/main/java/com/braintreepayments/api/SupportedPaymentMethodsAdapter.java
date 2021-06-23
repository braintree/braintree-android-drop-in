package com.braintreepayments.api;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

import java.util.List;

class SupportedPaymentMethodsAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<Integer> mAvailablePaymentMethods;
    private final SupportedPaymentMethodSelectedListener mSupportedPaymentMethodSelectedListener;

    SupportedPaymentMethodsAdapter(Context context, SupportedPaymentMethodSelectedListener supportedPaymentMethodSelectedListener, List<Integer> availablePaymentMethods) {
        // TODO: remove context variable
        mContext = context;
        mSupportedPaymentMethodSelectedListener = supportedPaymentMethodSelectedListener;
        mAvailablePaymentMethods = availablePaymentMethods;
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

        final @SupportedPaymentMethodType int type = mAvailablePaymentMethods.get(position);

        int iconDrawableResId;
        int localizedNameResId;

        switch (type) {
            case SupportedPaymentMethodType.GOOGLE_PAY:
                iconDrawableResId = R.drawable.bt_ic_google_pay;
                localizedNameResId =  R.string.bt_descriptor_google_pay;
                break;
            case SupportedPaymentMethodType.PAYPAL:
                iconDrawableResId = R.drawable.bt_ic_paypal;
                localizedNameResId = R.string.bt_descriptor_paypal;
                break;
            case SupportedPaymentMethodType.VENMO:
                iconDrawableResId = R.drawable.bt_ic_venmo;
                localizedNameResId = R.string.bt_descriptor_pay_with_venmo;
                break;
            case SupportedPaymentMethodType.CARD:
            default:
                iconDrawableResId = CardType.UNKNOWN.getFrontResource();
                localizedNameResId =  R.string.bt_descriptor_unknown;
                break;
        }

        ImageView icon = convertView.findViewById(R.id.bt_payment_method_icon);
        icon.setImageResource(iconDrawableResId);

        ((TextView) convertView.findViewById(R.id.bt_payment_method_type))
                .setText(mContext.getString(localizedNameResId));

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSupportedPaymentMethodSelectedListener.onPaymentMethodSelected(type);
            }
        });

        return convertView;
    }
}
