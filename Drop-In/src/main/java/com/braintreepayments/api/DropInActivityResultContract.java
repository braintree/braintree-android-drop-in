package com.braintreepayments.api;

import static com.braintreepayments.api.DropInClient.EXTRA_AUTHORIZATION;
import static com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST;
import static com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST_BUNDLE;
import static com.braintreepayments.api.DropInClient.EXTRA_SESSION_ID;
import static com.braintreepayments.api.DropInResult.EXTRA_ERROR;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

class DropInActivityResultContract extends ActivityResultContract<DropInIntentData, DropInResult> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, DropInIntentData input) {
        Bundle dropInRequestBundle = new Bundle();
        dropInRequestBundle.putParcelable(EXTRA_CHECKOUT_REQUEST, input.getDropInRequest());
        return new Intent(context, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST_BUNDLE, dropInRequestBundle)
                .putExtra(EXTRA_SESSION_ID, input.getSessionId())
                .putExtra(EXTRA_AUTHORIZATION, input.getAuthorization().toString());
    }

    @Override
    public DropInResult parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (intent != null) {
                return intent.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            DropInResult userCanceledResult = new DropInResult();
            userCanceledResult.setError(new UserCanceledException("User canceled DropIn."));
            return userCanceledResult;
        } else if (resultCode == AppCompatActivity.RESULT_FIRST_USER) {
            if (intent != null) {
                DropInResult errorResult = new DropInResult();
                errorResult.setError((Exception) intent.getSerializableExtra(EXTRA_ERROR));
                return errorResult;
            }
        }

        return null;
    }
}
