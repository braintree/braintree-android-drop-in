package com.braintreepayments.api;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public class DropInLifecycleObserver implements DefaultLifecycleObserver {

    private static final String DROP_IN_RESULT = "com.braintreepayments.api.DropIn.RESULT";

    @VisibleForTesting
    DropInClient dropInClient;

    @VisibleForTesting
    final ActivityResultRegistry activityResultRegistry;

    @VisibleForTesting
    ActivityResultLauncher<DropInIntentData> activityLauncher;

    public DropInLifecycleObserver(ActivityResultRegistry activityResultRegistry, DropInClient dropInClient) {
        this.dropInClient = dropInClient;
        this.activityResultRegistry = activityResultRegistry;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        activityLauncher = activityResultRegistry.register(
                DROP_IN_RESULT,
                owner,
                new DropInActivityResultContract(),
                dropInResult -> dropInClient.onDropInResult(dropInResult));
    }

    public void launch(DropInIntentData intentData) {
        activityLauncher.launch(intentData);
    }
}
