package com.braintreepayments.api;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class DropInLauncher implements DefaultLifecycleObserver {

    private static final String DROP_IN_RESULT = "DropInResult";

    private DropInClient dropInClient;
    private ActivityResultRegistry activityResultRegistry;

    private ActivityResultLauncher<DropInContractInput> activityLauncher;

    DropInLauncher(ActivityResultRegistry activityResultRegistry, DropInClient dropInClient) {
        this.activityResultRegistry = activityResultRegistry;
        this.dropInClient = dropInClient;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        activityLauncher = activityResultRegistry.register(DROP_IN_RESULT, owner, new DropInContract(), new ActivityResultCallback<DropInResult>() {
            @Override
            public void onActivityResult(DropInResult result) {
                dropInClient.onDropInResult(result);
            }
        });
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        dropInClient = null;
        activityResultRegistry = null;
    }

    void launch(DropInContractInput input) {
        activityLauncher.launch(input);
    }
}
