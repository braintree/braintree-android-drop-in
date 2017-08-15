package com.braintreepayments.api.dropin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.models.Authorization;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.Configuration;

public class BaseActivity extends AppCompatActivity {

    protected DropInRequest mDropInRequest;
    protected BraintreeFragment mBraintreeFragment;
    protected Configuration mConfiguration;
    protected boolean mClientTokenPresent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDropInRequest = getIntent().getParcelableExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST);
    }

    protected BraintreeFragment getBraintreeFragment() throws InvalidArgumentException {
        if (TextUtils.isEmpty(mDropInRequest.getAuthorization())) {
            throw new InvalidArgumentException("A client token or client key must be specified " +
                    "in the " + DropInRequest.class.getSimpleName());
        }

        try {
            mClientTokenPresent = Authorization.fromString(mDropInRequest.getAuthorization())
                    instanceof ClientToken;
        } catch (InvalidArgumentException e) {
            mClientTokenPresent = false;
        }

        return BraintreeFragment.newInstance(this, mDropInRequest.getAuthorization());
    }

    protected boolean shouldRequestThreeDSecureVerification() {
        return mDropInRequest.shouldRequestThreeDSecureVerification() &&
                !TextUtils.isEmpty(mDropInRequest.getAmount()) &&
                mConfiguration.isThreeDSecureEnabled();
    }
}
