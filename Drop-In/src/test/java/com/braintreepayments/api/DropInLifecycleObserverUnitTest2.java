package com.braintreepayments.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.activity.result.ActivityResultRegistry;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DropInLifecycleObserverUnitTest2 extends TestCase {

    @Test
    public void onCreate_registersForAnActivityResult() {
        ActivityResultRegistry activityResultRegistry = mock(ActivityResultRegistry.class);
        DropInClient dropInClient = mock(DropInClient.class);
        DropInLifecycleObserver sut =
                new DropInLifecycleObserver(activityResultRegistry, dropInClient);

        FragmentActivity lifecycleOwner = new FragmentActivity();
        sut.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_CREATE);

        String expectedKey = "com.braintreepayments.api.DropIn.RESULT";
        verify(activityResultRegistry).register(eq(expectedKey), same(lifecycleOwner), any(DropInActivityResultContract.class), Mockito.any());
    }
}
