package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.dropin.adapters.PaymentMethodGridViewAdapter;
import com.braintreepayments.api.dropin.interfaces.PaymentMethodClickListener;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.models.PaymentMethodNonce;

public class GridViewPaymentActivity extends Activity implements ConfigurationListener, PaymentMethodClickListener,
        PaymentMethodNonceCreatedListener {

    private static final int ADD_CARD_REQUEST_CODE = 1;

    private PaymentRequest mPaymentRequest;
    private BraintreeFragment mBraintreeFragment;
    private ViewSwitcher mViewSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_grid_view_payment_activity);

        mPaymentRequest = getIntent().getParcelableExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.bt_loading_view_switcher);

        ((TextView) findViewById(R.id.bt_primary_description)).setText(mPaymentRequest.getPrimaryDescription());
        ((TextView) findViewById(R.id.bt_secondary_description)).setText(mPaymentRequest.getSecondaryDescription());
        ((TextView) findViewById(R.id.bt_description_amount)).setText(mPaymentRequest.getAmount());

        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mPaymentRequest.getAuthorization());
        } catch (InvalidArgumentException ignored) {}
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        ((GridView) findViewById(R.id.bt_payment_method_grid_view))
                .setAdapter(new PaymentMethodGridViewAdapter(this, this, configuration));
        mViewSwitcher.setDisplayedChild(1);
    }

    @Override
    public void onCardClick() {
        Intent intent = new Intent(this, AddCardActivity.class);
        intent.putExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST, mPaymentRequest);
        startActivityForResult(intent, ADD_CARD_REQUEST_CODE);
    }

    @Override
    public void onPayPalClick() {
        PayPal.authorizeAccount(mBraintreeFragment);
    }

    @Override
    public void onAndroidPayClick() {
        AndroidPay.performMaskedWalletRequest(mBraintreeFragment, mPaymentRequest.getAndroidPayCart(),
                mPaymentRequest.isAndroidPayShippingAddressRequired(),
                mPaymentRequest.isAndroidPayPhoneNumberRequired(),
                mPaymentRequest.getAndroidPayAllowedCountriesForShipping(),
                AndroidPay.ANDROID_PAY_MASKED_WALLET_REQUEST_CODE);
    }

    @Override
    public void onVenmoClick() {
        Venmo.authorizeAccount(mBraintreeFragment);
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        setResult(Activity.RESULT_OK,
                new Intent().putExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE, paymentMethodNonce));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == AndroidPay.ANDROID_PAY_MASKED_WALLET_REQUEST_CODE ||
                requestCode == AndroidPay.ANDROID_PAY_FULL_WALLET_REQUEST_CODE) &&
                resultCode == RESULT_OK) {
            AndroidPay.onActivityResult(mBraintreeFragment, mPaymentRequest.getAndroidPayCart(), resultCode, data);
        } else if (requestCode == ADD_CARD_REQUEST_CODE && resultCode == RESULT_OK) {
            onPaymentMethodNonceCreated(
                    (PaymentMethodNonce) data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE));
        }
    }
}
