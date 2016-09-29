package com.braintreepayments.api.dropin;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.exceptions.InvalidArgumentException;

import static com.braintreepayments.api.BraintreeFragmentTestUtils.setHttpClient;
import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DropInUnitTestActivity extends DropInActivity {

    public BraintreeFragment braintreeFragment;
    public BraintreeUnitTestHttpClient httpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.bt_add_card_activity_theme);

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
            setHttpClient(braintreeFragment, httpClient);
        }

        return braintreeFragment;
    }

    @Override
    public Resources getResources() {
        Resources resources = spy(super.getResources());
        when(resources.getInteger(android.R.integer.config_longAnimTime)).thenReturn(0);
        when(resources.getInteger(android.R.integer.config_mediumAnimTime)).thenReturn(0);
        when(resources.getInteger(android.R.integer.config_shortAnimTime)).thenReturn(0);
        return resources;
    }
}
