package com.braintreepayments.api;

import androidx.activity.result.ActivityResultCallback;

public interface DropInLauncherCallback extends ActivityResultCallback<DropInResult> {

    @Override
    void onActivityResult(DropInResult result);
}