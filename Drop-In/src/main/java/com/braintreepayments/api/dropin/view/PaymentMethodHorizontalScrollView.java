package com.braintreepayments.api.dropin.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.List;

public class PaymentMethodHorizontalScrollView extends HorizontalScrollView implements OnClickListener {

    private ViewGroup mScrollViewContainer;
    private PaymentMethodNonceCreatedListener mVaultedPaymentMethodSelectedListener;
    private Context mContext;

    public PaymentMethodHorizontalScrollView(Context context) {
        super(context);
        init(context);
    }

    public PaymentMethodHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaymentMethodHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public PaymentMethodHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.bt_payment_method_horizontal_scroll_view, this);
        mScrollViewContainer = (ViewGroup) findViewById(R.id.bt_vaulted_scroll_container);
        mContext = context;
        setHorizontalScrollBarEnabled(false);
    }

    public void setPaymentMethods(List<PaymentMethodNonce> paymentMethodNonces) {
        mScrollViewContainer.removeAllViews();
        for (PaymentMethodNonce nonce : paymentMethodNonces) {
            PaymentMethodCardView view = new PaymentMethodCardView(mContext, nonce);
            view.setOnClickListener(this);
            mScrollViewContainer.addView(view);
        }
    }

    public void setPaymentMethodSelectedListener(PaymentMethodNonceCreatedListener listener) {
        mVaultedPaymentMethodSelectedListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mVaultedPaymentMethodSelectedListener != null) {
            mVaultedPaymentMethodSelectedListener.onPaymentMethodNonceCreated(((PaymentMethodCardView) v).getPaymentMethodNonce());
        }
    }
}
