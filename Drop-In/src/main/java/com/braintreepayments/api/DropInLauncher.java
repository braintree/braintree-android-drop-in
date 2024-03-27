package com.braintreepayments.api;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;

public class DropInLauncher implements DefaultLifecycleObserver {

    private static final String DROP_IN_RESULT = "com.braintreepayments.api.DropIn.RESULT";

    static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";
    static final String EXTRA_CHECKOUT_REQUEST_BUNDLE = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST_BUNDLE";
    static final String EXTRA_SESSION_ID = "com.braintreepayments.api.EXTRA_SESSION_ID";
    static final String EXTRA_AUTHORIZATION = "com.braintreepayments.api.EXTRA_AUTHORIZATION";
    static final String EXTRA_AUTHORIZATION_ERROR = "com.braintreepayments.api.EXTRA_AUTHORIZATION_ERROR";


    @VisibleForTesting
    private ActivityResultLauncher<DropInLaunchIntent> activityLauncher;

    public DropInLauncher(ComponentActivity activity, DropInLauncherCallback callback) {
        ActivityResultRegistry registry = activity.getActivityResultRegistry();
        activityLauncher = registry.register(
                DROP_IN_RESULT, activity, new DropInActivityResultContract(), callback);
    }

    public void launchDropIn(String authString, DropInRequest dropInRequest) {
        Authorization authorization = Authorization.fromString(authString);
        DropInLaunchIntent launchIntent =
            new DropInLaunchIntent(dropInRequest, authorization, "");
        activityLauncher.launch(launchIntent);
    }
}
