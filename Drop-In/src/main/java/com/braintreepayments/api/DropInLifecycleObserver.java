package com.braintreepayments.api;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

// NEXT_MAJOR_VERSION: Update to implement DefaultLifeCycleObserver when Java 7 support is explicitly dropped.
public class DropInLifecycleObserver implements LifecycleEventObserver {

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
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            activityLauncher = activityResultRegistry.register(DROP_IN_RESULT, lifecycleOwner, new DropInActivityResultContract(), new ActivityResultCallback<DropInResult>() {
                @Override
                public void onActivityResult(DropInResult dropInResult) {
                    dropInClient.onDropInResult(dropInResult);
                }
            });
        }
    }

    public void launch(DropInIntentData intentData) {
        activityLauncher.launch(intentData);
    }
}
