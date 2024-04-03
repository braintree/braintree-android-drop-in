package com.braintreepayments.api;

import static com.braintreepayments.api.DropInLauncher.EXTRA_CHECKOUT_REQUEST;
import static com.braintreepayments.api.DropInLauncher.EXTRA_CHECKOUT_REQUEST_BUNDLE;
import static com.braintreepayments.api.DropInResult.EXTRA_ERROR;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

class DropInActivityResultContract extends ActivityResultContract<DropInRequest, DropInResult> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, DropInRequest dropInRequest) {
        Bundle dropInRequestBundle = new Bundle();
        dropInRequestBundle.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInRequest);
        return new Intent(context, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST_BUNDLE, dropInRequestBundle);
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
