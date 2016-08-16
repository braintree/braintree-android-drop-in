package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.exceptions.InvalidArgumentException;

import static com.braintreepayments.testutils.TestTokenizationKey.TOKENIZATION_KEY;

public class NewDropInUnitTestActivity extends NewDropInActivity {

    public BraintreeFragment braintreeFragment;
    public BraintreeUnitTestHttpClient httpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.bt_add_payment_activity_theme);

        if (mPaymentRequest == null) {
            mPaymentRequest= new PaymentRequest().tokenizationKey(TOKENIZATION_KEY);
        }

        Intent intent = new Intent()
                .putExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST, mPaymentRequest);
        setIntent(intent);

        super.onCreate(savedInstanceState);
    }

    public void setPaymentRequest(PaymentRequest paymentRequest) {
        mPaymentRequest = paymentRequest;
    }

    @Override
    protected BraintreeFragment getBraintreeFragment() throws InvalidArgumentException {
        if (braintreeFragment == null) {
            braintreeFragment = super.getBraintreeFragment();
            braintreeFragment.mHttpClient = httpClient;
        }

        return braintreeFragment;
    }
}
