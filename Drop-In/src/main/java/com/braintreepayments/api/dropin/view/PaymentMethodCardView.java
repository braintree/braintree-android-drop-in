package com.braintreepayments.api.dropin.view;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.PaymentMethodNonce;

class PaymentMethodCardView extends LinearLayout {

    private PaymentMethodNonce mPaymentMethodNonce;

    PaymentMethodCardView(Context context, PaymentMethodNonce nonce) {
        super(context);
        mPaymentMethodNonce = nonce;
        inflate(context, R.layout.bt_grid_view_item, this);
        ((ImageView) findViewById(R.id.bt_payment_method_icon))
                .setImageResource(PaymentMethodType.forType(mPaymentMethodNonce.getTypeLabel()).getDrawable());
        ((TextView) findViewById(R.id.bt_payment_method_description)).setText(nonce.getDescription());
    }

    PaymentMethodNonce getPaymentMethodNonce() {
        return mPaymentMethodNonce;
    }
}
