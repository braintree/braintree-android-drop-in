package com.braintreepayments.api.dropin;

import android.content.Intent;
import android.os.Bundle;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.ConfigurationManagerTestUtils;
import com.braintreepayments.api.exceptions.InvalidArgumentException;

import static com.braintreepayments.api.BraintreeFragmentTestUtils.setHttpClient;
import static com.braintreepayments.api.BraintreeFragmentTestUtils.waitForConfiguration;
import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;

public class AddCardUnitTestActivity extends AddCardActivity {

    public DropInRequest dropInRequest;
    public BraintreeFragment braintreeFragment;
    public BraintreeUnitTestHttpClient httpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.bt_add_card_activity_theme);

        if (dropInRequest == null) {
            dropInRequest = new DropInRequest().tokenizationKey(TOKENIZATION_KEY);
        }

        Intent intent = new Intent()
                .putExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST, dropInRequest);
        setIntent(intent);

        super.onCreate(savedInstanceState);

        if (braintreeFragment != null) {
            ConfigurationManagerTestUtils.setFetchingConfiguration(false);
            waitForConfiguration(braintreeFragment, this);
        }
    }

    @Override
    protected BraintreeFragment getBraintreeFragment() throws InvalidArgumentException {
        if (braintreeFragment == null) {
            braintreeFragment = super.getBraintreeFragment();
            setHttpClient(braintreeFragment, httpClient);
        }

        return braintreeFragment;
    }
}
