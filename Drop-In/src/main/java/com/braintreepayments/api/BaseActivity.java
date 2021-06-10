package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.models.Authorization;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;

import org.json.JSONException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

class BaseActivity extends AppCompatActivity {

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
        boolean hasAmount = !TextUtils.isEmpty(mDropInRequest.getAmount()) ||
                (mDropInRequest.getThreeDSecureRequest() != null && !TextUtils.isEmpty(mDropInRequest.getThreeDSecureRequest().getAmount()));

        // TODO: NEXT_MAJOR_VERSION use BraintreeClient#getConfiguration and don't cache configuration in memory
        if (mConfiguration == null) {
            return false;
        }

        return mDropInRequest.shouldRequestThreeDSecureVerification() &&
                mConfiguration.isThreeDSecureEnabled() &&
                hasAmount;
    }

    protected void finish(PaymentMethodNonce paymentMethod, String deviceData) {
        DropInResult result = new DropInResult()
                .paymentMethodNonce(paymentMethod)
                .deviceData(deviceData);

        setResult(RESULT_OK,
                new Intent().putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result));
        finish();
    }

    protected void finish(Exception e) {
        setResult(RESULT_FIRST_USER, new Intent().putExtra(DropInActivity.EXTRA_ERROR, e));
        finish();
    }
}
