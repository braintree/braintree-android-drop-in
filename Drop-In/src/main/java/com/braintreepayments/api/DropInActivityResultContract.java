package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DropInActivityResultContract extends ActivityResultContract<DropInIntentData, DropInResult> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, DropInIntentData input) {
        return null;
    }

    @Override
    public DropInResult parseResult(int resultCode, @Nullable Intent intent) {
        return null;
    }
}
