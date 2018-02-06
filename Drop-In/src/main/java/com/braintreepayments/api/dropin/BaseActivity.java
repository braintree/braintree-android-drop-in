package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.models.Authorization;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;

import org.json.JSONException;

public class BaseActivity extends AppCompatActivity {

    static final String EXTRA_CONFIGURATION_DATA = "com.braintreepayments.api.EXTRA_CONFIGURATION_DATA";

    protected DropInRequest mDropInRequest;
    protected BraintreeFragment mBraintreeFragment;
    protected Configuration mConfiguration;
    protected boolean mClientTokenPresent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            try {
                mConfiguration = Configuration.fromJson(savedInstanceState.getString(EXTRA_CONFIGURATION_DATA));
            } catch (JSONException ignored) {}
        }

        mDropInRequest = getIntent().getParcelableExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mConfiguration != null) {
            outState.putString(EXTRA_CONFIGURATION_DATA, mConfiguration.toJson());
        }
    }

    protected BraintreeFragment getBraintreeFragment() throws InvalidArgumentException {
        if (TextUtils.isEmpty(mDropInRequest.getAuthorization())) {
            throw new InvalidArgumentException("A client token or tokenization key must be " +
                    "specified in the " + DropInRequest.class.getSimpleName());
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

    protected void finish(PaymentMethodNonce paymentMethod, String deviceData) {
        DropInResult result = new DropInResult()
                .paymentMethodNonce(paymentMethod)
                .deviceData(deviceData);

        setResult(Activity.RESULT_OK,
                new Intent().putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result));
        finish();
    }

    protected void finish(Exception e) {
        setResult(RESULT_FIRST_USER, new Intent().putExtra(DropInActivity.EXTRA_ERROR, e));
        finish();
    }
}
