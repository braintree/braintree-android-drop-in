package com.braintreepayments.api;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

class BaseActivity extends AppCompatActivity {

    protected DropInRequest mDropInRequest;
    protected boolean mClientTokenPresent;

    private DropInClient dropInClient;
    private LaunchDropInCallback callback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(DropInClient.EXTRA_CHECKOUT_REQUEST)) {
            mDropInRequest = getIntent().getParcelableExtra(DropInClient.EXTRA_CHECKOUT_REQUEST);
        } else {
            mDropInRequest = DropInRequestStore.getInstance().getDropInRequest();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDropInRequest != null) {
            DropInRequestStore.getInstance().setDropInRequest(mDropInRequest);
        }
    }


    DropInClient getDropInClient() {
        // lazily instantiate dropInClient for testing purposes
        if (dropInClient != null) {
            return dropInClient;
        }
        if (getIntent().hasExtra(DropInClient.EXTRA_CHECKOUT_REQUEST)) {
            Intent intent = getIntent();
            String authorization = intent.getStringExtra(DropInClient.EXTRA_AUTHORIZATION);
            String sessionId = intent.getStringExtra(DropInClient.EXTRA_SESSION_ID);
            DropInRequest dropInRequest = intent.getParcelableExtra(DropInClient.EXTRA_CHECKOUT_REQUEST);
            dropInClient = new DropInClient(this, authorization, sessionId, dropInRequest);
        } else {
            DropInRequest dropInRequest = DropInRequestStore.getInstance().getDropInRequest();
            dropInClient = new DropInClient(this, dropInRequest.getAuthorization(), "SESSION_ID", dropInRequest);
        }


        mClientTokenPresent = dropInClient.getAuthorization() instanceof ClientToken;
        return dropInClient;
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
