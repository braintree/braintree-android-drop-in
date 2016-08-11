package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.exceptions.InvalidArgumentException;

import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;

public class AddCardUnitTestActivity extends AddCardActivity {

    public PaymentRequest paymentRequest;
    public BraintreeFragment braintreeFragment;
    public BraintreeUnitTestHttpClient httpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.bt_add_payment_activity_theme);

        if (paymentRequest == null) {
            paymentRequest = new PaymentRequest().tokenizationKey(TOKENIZATION_KEY);
        }

        Intent intent = new Intent()
                .putExtra(PaymentRequest.EXTRA_CHECKOUT_REQUEST, paymentRequest);
        setIntent(intent);

        super.onCreate(savedInstanceState);
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
