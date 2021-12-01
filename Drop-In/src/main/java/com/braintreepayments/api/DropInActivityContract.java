package com.braintreepayments.api;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * A contract used to launch a {@link DropInActivity} with a {@link DropInClient} input and {@link DropInResult2} output.
 */
public class DropInActivityContract extends ActivityResultContract<DropInClient, DropInResult2> {

    static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";
    static final String EXTRA_CHECKOUT_REQUEST_BUNDLE = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST_BUNDLE";
    static final String EXTRA_SESSION_ID = "com.braintreepayments.api.EXTRA_SESSION_ID";
    static final String EXTRA_AUTHORIZATION = "com.braintreepayments.api.EXTRA_AUTHORIZATION";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, DropInClient dropInClient) {
        Bundle dropInRequestBundle = new Bundle();
        dropInRequestBundle.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInClient.getDropInRequest());
        Intent intent = new Intent(context, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST_BUNDLE, dropInRequestBundle)
                .putExtra(EXTRA_SESSION_ID, dropInClient.getSessionId())
                .putExtra(EXTRA_AUTHORIZATION, dropInClient.getAuthorization().toString());
        return intent;
    }

    @Override
    public DropInResult2 parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
            return intent.getParcelableExtra(DropInResult2.EXTRA_DROP_IN_RESULT_2);
        }
        return null;
    }
}
