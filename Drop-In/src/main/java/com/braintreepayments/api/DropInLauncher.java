package com.braintreepayments.api;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * Class used to launch Drop-in and register for results
 */
public class DropInLauncher {

    private ActivityResultLauncher<DropInClient> resultLauncher;

    /**
     * Call this method to register your Fragment to receive the result from {@link DropInActivity}.
     * This method must be called before the Fragment is created (i.e. initialization, onAttach(), or onCreate()).
     *
     * @param fragment a Fragment that will handle the result of {@link DropInActivity}
     * @param callback a {@link DropInResultCallback}
     */
    public void registerForActivityResult(Fragment fragment, DropInResultCallback callback) {
        resultLauncher = fragment.registerForActivityResult(new DropInActivityContract(), result -> {
            handleResult(result, callback);
        });
    }

    /**
     * Call this method to register your Activity to receive the result from {@link DropInActivity}.
     * This method must be called before the Activity state is STARTED.
     *
     * @param activity an Activity that will handle the result of {@link DropInActivity}
     * @param callback a {@link DropInResultCallback}
     */
    public void registerForActivityResult(FragmentActivity activity, DropInResultCallback callback) {
        resultLauncher = activity.registerForActivityResult(new DropInActivityContract(), result -> {
            handleResult(result, callback);
        });
    }

    private void handleResult(DropInActivityResult result, DropInResultCallback callback) {
        if (result != null) {
            if (result.getDropInResult() != null) {
                callback.onResult(result.getDropInResult(), null);
            } else if (result.getError() != null) {
                callback.onResult(null, result.getError());
            }
        }
    }

    /**
     * Call this method to launch a {@link DropInActivity}. This method must be called after you have
     * registered for Activity result via {@link DropInLauncher#registerForActivityResult(FragmentActivity, DropInResultCallback)}
     * or {@link DropInLauncher#registerForActivityResult(Fragment, DropInResultCallback)}.
     *
     * @param dropInClient a {@link DropInClient} configured with your {@link Authorization} and {@link DropInRequest}
     */
    public void launchWithDropInClient(DropInClient dropInClient) {
        resultLauncher.launch(dropInClient);
    }
}
