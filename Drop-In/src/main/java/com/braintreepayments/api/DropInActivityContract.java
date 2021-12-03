package com.braintreepayments.api;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

import static com.braintreepayments.api.DropInClient.EXTRA_AUTHORIZATION;
import static com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST;
import static com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST_BUNDLE;
import static com.braintreepayments.api.DropInClient.EXTRA_SESSION_ID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * A contract specifying that a {@link DropInActivity} can be called with a {@link DropInClient}
 * input and produce a {@link DropInActivityResult} output.
 */
class DropInActivityContract extends ActivityResultContract<DropInClient, DropInActivityResult> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, DropInClient dropInClient) {
        Bundle dropInRequestBundle = new Bundle();
        dropInRequestBundle.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInClient.getDropInRequest());
        return new Intent(context, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST_BUNDLE, dropInRequestBundle)
                .putExtra(EXTRA_SESSION_ID, dropInClient.getSessionId())
                .putExtra(EXTRA_AUTHORIZATION, dropInClient.getAuthorization().toString());
    }

    @Override
    public DropInActivityResult parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
            if (intent != null) {
                return intent.getParcelableExtra(DropInActivityResult.EXTRA_DROP_IN_ACTIVITY_RESULT);
            }
        } else if (resultCode == RESULT_CANCELED) {
            DropInActivityResult result = new DropInActivityResult();
            result.setError(new UserCanceledException("User canceled Drop-in"));
            return result;
        }
        return null;
    }
}
