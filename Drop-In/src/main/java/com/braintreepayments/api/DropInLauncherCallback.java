package com.braintreepayments.api;

import androidx.activity.result.ActivityResultCallback;

/**
 * Callback used to receive an Activity result from {@link DropInLauncher}.
 */
public interface DropInLauncherCallback extends ActivityResultCallback<DropInResult> {

    /**
     * Provides the result of launching {@link DropInActivity}.
     *
     * @param result DropInResult
     */
    @Override
    void onActivityResult(DropInResult result);
}