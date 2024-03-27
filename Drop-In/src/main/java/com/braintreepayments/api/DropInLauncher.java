package com.braintreepayments.api;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;

public class DropInLauncher implements DefaultLifecycleObserver {

    private static final String DROP_IN_RESULT = "com.braintreepayments.api.DropIn.RESULT";

    @VisibleForTesting
    private ActivityResultLauncher<DropInLaunchIntent> activityLauncher;

    public DropInLauncher(ComponentActivity activity, DropInLauncherCallback callback) {
        ActivityResultRegistry registry = activity.getActivityResultRegistry();
        activityLauncher = registry.register(
                DROP_IN_RESULT, activity, new DropInActivityResultContract(), callback);
    }

    public void launchDropIn(DropInLaunchIntent launchIntent) {
        activityLauncher.launch(launchIntent);
    }
}
