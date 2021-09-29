package com.braintreepayments.api;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;

class PaymentMethodItemView extends LinearLayout {

    private ImageView icon;
    private TextView title;
    private TextView description;
    private View deleteIcon;
    private PaymentMethodNonce paymentMethodNonce;
    private View divider;

    private final PaymentMethodNonceInspector nonceInspector = new PaymentMethodNonceInspector();

    public PaymentMethodItemView(Context context) {
        super(context);
        init();
    }

    public PaymentMethodItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaymentMethodItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        setOrientation(VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.bt_vault_manager_list_item, this);

        icon = findViewById(R.id.bt_payment_method_icon);
        title = findViewById(R.id.bt_payment_method_title);
        description = findViewById(R.id.bt_payment_method_description);
        deleteIcon = findViewById(R.id.bt_payment_method_delete_icon);
        divider = findViewById(R.id.bt_payment_method_divider);
    }

    public void setPaymentMethod(PaymentMethodNonce paymentMethodNonce, boolean usedInList) {
        this.paymentMethodNonce = paymentMethodNonce;

        DropInPaymentMethodType paymentMethodType = DropInPaymentMethodType.forType(paymentMethodNonce);

        if (usedInList) {
            icon.setImageResource(paymentMethodType.getDrawable());
            deleteIcon.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            icon.setImageResource(paymentMethodType.getVaultedDrawable());
            deleteIcon.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }

        title.setText(paymentMethodType.getLocalizedName());
        description.setText(nonceInspector.getDescription(paymentMethodNonce));
    }

    public void setOnDeleteIconClick(OnClickListener clickListener) {
        deleteIcon.setOnClickListener(clickListener);
    }

    public PaymentMethodNonce getPaymentMethodNonce() {
        return paymentMethodNonce;
    }
}
