package com.braintreepayments.api;

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

public class DropInContract extends ActivityResultContract<DropInContractInput, DropInResult> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, DropInContractInput input) {
        Bundle dropInRequestBundle = new Bundle();
        dropInRequestBundle.putParcelable(EXTRA_CHECKOUT_REQUEST, input.getDropInRequest());
        return new Intent(context, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST_BUNDLE, dropInRequestBundle)
                .putExtra(EXTRA_SESSION_ID, input.getSessionId())
                .putExtra(EXTRA_AUTHORIZATION, input.getAuthorization());
    }

    @Override
    public DropInResult parseResult(int resultCode, @Nullable Intent intent) {
        if (intent != null) {
            return intent.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        }
        return null;
    }
}
