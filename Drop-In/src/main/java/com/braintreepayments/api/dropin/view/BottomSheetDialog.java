package com.braintreepayments.api.dropin.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.AddCardActivity;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.Venmo;
import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.interfaces.PaymentMethodClickListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.models.Configuration;

public class BottomSheetDialog extends Dialog implements OnCancelListener, ConfigurationListener,
        PaymentMethodClickListener {

    protected PaymentRequest mPaymentRequest;
    protected BraintreeFragment mBraintreeFragment;

    private ViewSwitcher mViewSwitcher;
    private PaymentMethodHorizontalScrollView mPaymentMethodHorizontalScrollView;

    protected BottomSheetDialog(Context context) {
        super(context);
    }

    protected BottomSheetDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BottomSheetDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static BottomSheetDialog create(Activity activity, PaymentRequest paymentRequest,
            BraintreeFragment braintreeFragment) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity, R.style.bt_bottom_sheet_dialog);
        dialog.setOwnerActivity(activity);
        dialog.mPaymentRequest = paymentRequest;
        dialog.mBraintreeFragment = braintreeFragment;

        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.bt_selected_payment_method_dialog);

        ((TextView) findViewById(R.id.bt_title)).setText("Checkout");
        ((TextView) findViewById(R.id.bt_primary_description)).setText(mPaymentRequest.getPrimaryDescription());
        ((TextView) findViewById(R.id.bt_description_amount)).setText(mPaymentRequest.getAmount());
        ((TextView) findViewById(R.id.bt_secondary_description)).setText(mPaymentRequest.getSecondaryDescription());
        ((Button) findViewById(R.id.bt_submit_button)).setText(mPaymentRequest.getSubmitButtonText());

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.bt_payment_method_view_switcher);
        mPaymentMethodHorizontalScrollView =
                (PaymentMethodHorizontalScrollView) findViewById(R.id.bt_payment_method_horizontal_scroll_view);
        mPaymentMethodHorizontalScrollView.setOnClickListener(this);

        setOnCancelListener(this);
        setCanceledOnTouchOutside(true);

        mBraintreeFragment.addListener(this);
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        mPaymentMethodHorizontalScrollView.setCardsEnabled(true);
        mPaymentMethodHorizontalScrollView.setPayPalEnabled(configuration.isPayPalEnabled());
        mPaymentMethodHorizontalScrollView.setAndroidPayEnabled(configuration.getAndroidPay().isEnabled(getContext()));
        mPaymentMethodHorizontalScrollView.setVenmoEnabled(configuration.getPayWithVenmo().isEnabled(getContext()));

        mViewSwitcher.setDisplayedChild(1);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mBraintreeFragment.removeListener(this);
        getOwnerActivity().finish();
    }

    @Override
    public void onCardClick() {
        Intent intent = new Intent(getContext(), AddCardActivity.class)
                .putExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST, mPaymentRequest);

        getOwnerActivity().startActivityForResult(intent, 1);
    }

    @Override
    public void onPayPalClick() {
        PayPal.authorizeAccount(mBraintreeFragment, mPaymentRequest.getPayPalAdditionalScopes());
    }

    @Override
    public void onAndroidPayClick() {
    }

    @Override
    public void onVenmoClick() {
        Venmo.authorizeAccount(mBraintreeFragment);
    }
}
