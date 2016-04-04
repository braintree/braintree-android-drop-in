package com.braintreepayments.api.dropin.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.PaymentMethodClickListener;

public class PaymentMethodHorizontalScrollView extends HorizontalScrollView implements OnClickListener {

    private PaymentMethodClickListener mPaymentMethodClickListener;

    private View mCardView;
    private View mPayPalView;
    private View mAndroidPayView;
    private View mVenmoView;

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

        mCardView = findViewById(R.id.bt_card);
        mCardView.setOnClickListener(this);
        mPayPalView = findViewById(R.id.bt_paypal);
        mPayPalView.setOnClickListener(this);
        mAndroidPayView = findViewById(R.id.bt_android_pay);
        mAndroidPayView.setOnClickListener(this);
        mVenmoView = findViewById(R.id.bt_venmo);
        mVenmoView.setOnClickListener(this);

        setHorizontalScrollBarEnabled(false);
    }

    public void setOnClickListener(PaymentMethodClickListener listener) {
        mPaymentMethodClickListener = listener;
    }

    public void setCardsEnabled(boolean enabled) {
        mCardView.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setPayPalEnabled(boolean enabled) {
        mPayPalView.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setAndroidPayEnabled(boolean enabled) {
        mAndroidPayView.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setVenmoEnabled(boolean enabled) {
        mVenmoView.setVisibility(enabled ? VISIBLE : GONE);
    }

    @Override
    public void onClick(View v) {
        if (mPaymentMethodClickListener != null) {
            if (v == mCardView) {
                mPaymentMethodClickListener.onCardClick();
            } else if (v == mPayPalView) {
                mPaymentMethodClickListener.onPayPalClick();
            } else if (v == mAndroidPayView) {
                mPaymentMethodClickListener.onAndroidPayClick();
            } else if (v == mVenmoView) {
                mPaymentMethodClickListener.onVenmoClick();
            }
        }
    }
}
