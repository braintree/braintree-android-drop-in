package com.braintreepayments.api;

import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public class DropInLifecycleObserver implements LifecycleEventObserver {

    public DropInLifecycleObserver(ActivityResultRegistry activityResultRegistry, DropInClient dropInClient) {

    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {

    }
}
