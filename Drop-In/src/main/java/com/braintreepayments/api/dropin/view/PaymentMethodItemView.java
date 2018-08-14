package com.braintreepayments.api.dropin.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;

public class PaymentMethodItemView extends LinearLayout {

    private ImageView mIcon;
    private TextView mTitle;
    private TextView mDescription;
    private View mDeleteIcon;
    private PaymentMethodNonce mPaymentMethodNonce;
    private View mDivider;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PaymentMethodItemView(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        setOrientation(VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.bt_vault_manager_list_item, this);

        mIcon = findViewById(R.id.bt_payment_method_icon);
        mTitle = findViewById(R.id.bt_payment_method_title);
        mDescription = findViewById(R.id.bt_payment_method_description);
        mDeleteIcon = findViewById(R.id.bt_payment_method_delete_icon);
        mDivider = findViewById(R.id.bt_payment_method_divider);
    }

    public void setPaymentMethod(PaymentMethodNonce paymentMethodNonce, boolean usedInList) {
        mPaymentMethodNonce = paymentMethodNonce;

        PaymentMethodType paymentMethodType = PaymentMethodType.forType(paymentMethodNonce);

        if (usedInList) {
            mIcon.setImageResource(paymentMethodType.getDrawable());
            mDeleteIcon.setVisibility(View.VISIBLE);
            mDivider.setVisibility(View.VISIBLE);
        } else {
            mIcon.setImageResource(paymentMethodType.getVaultedDrawable());
            mDeleteIcon.setVisibility(View.GONE);
            mDivider.setVisibility(View.GONE);
        }

        mTitle.setText(paymentMethodType.getLocalizedName());
        if (paymentMethodNonce instanceof CardNonce) {
            mDescription.setText("••• ••" + ((CardNonce) paymentMethodNonce).getLastTwo());
        } else {
            mDescription.setText(paymentMethodNonce.getDescription());
        }
    }

    public void setOnDeleteIconClick(OnClickListener clickListener) {
        mDeleteIcon.setOnClickListener(clickListener);
    }

    public PaymentMethodNonce getPaymentMethodNonce() {
        return mPaymentMethodNonce;
    }
}
