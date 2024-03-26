package com.braintreepayments.api;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class DropInLauncher implements DefaultLifecycleObserver {

    private static final String DROP_IN_RESULT = "com.braintreepayments.api.DropIn.RESULT";

    @VisibleForTesting
    private ActivityResultLauncher<DropInIntentData> activityLauncher;

    public DropInLauncher(ComponentActivity activity, DropInLauncherCallback callback) {
        ActivityResultRegistry registry = activity.getActivityResultRegistry();
        activityLauncher = registry.register(
                DROP_IN_RESULT, activity, new DropInActivityResultContract(), callback);
    }

    public void launchDropIn(DropInRequest request) {
        Authorization authorization = Authorization.fromString(request.getAuthorization());
        DropInIntentData intentData =
                new DropInIntentData(request, authorization, "");
        activityLauncher.launch(intentData);
    }
}
