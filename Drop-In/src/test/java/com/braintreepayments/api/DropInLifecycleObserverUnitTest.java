package com.braintreepayments.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DropInLifecycleObserverUnitTest extends TestCase {

    @Captor
    ArgumentCaptor<ActivityResultCallback<DropInResult>> dropInResultCaptor;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

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

    @Test
    public void onCreate_whenActivityResultReceived_forwardsResultToDropInClient() {
        ActivityResultRegistry activityResultRegistry = mock(ActivityResultRegistry.class);
        DropInClient dropInClient = mock(DropInClient.class);
        DropInLifecycleObserver sut =
                new DropInLifecycleObserver(activityResultRegistry, dropInClient);

        FragmentActivity lifecycleOwner = new FragmentActivity();
        sut.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_CREATE);

        verify(activityResultRegistry).register(anyString(), any(LifecycleOwner.class), any(DropInActivityResultContract.class), dropInResultCaptor.capture());
        ActivityResultCallback<DropInResult> activityResultCallback = dropInResultCaptor.capture();

        DropInResult dropInResult = new DropInResult();
        activityResultCallback.onActivityResult(dropInResult);
        verify(dropInClient).onDropInResult(dropInResult);
    }

    @Test
    public void onCreate_whenActivityResultIsNull_doesNotCallListener() {
        ActivityResultRegistry activityResultRegistry = mock(ActivityResultRegistry.class);
        DropInClient dropInClient = mock(DropInClient.class);
        DropInLifecycleObserver sut =
                new DropInLifecycleObserver(activityResultRegistry, dropInClient);

        FragmentActivity lifecycleOwner = new FragmentActivity();
        sut.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_CREATE);

        verify(activityResultRegistry).register(anyString(), any(LifecycleOwner.class), any(DropInActivityResultContract.class), dropInResultCaptor.capture());
        ActivityResultCallback<DropInResult> activityResultCallback = dropInResultCaptor.capture();

        activityResultCallback.onActivityResult(null);
        verify(dropInClient, never()).onDropInResult(any(DropInResult.class));
    }

    @Test
    public void launch_launchesActivity() {
        DropInRequest dropInRequest = new DropInRequest();
        Authorization authorization = Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN);

        ActivityResultRegistry activityResultRegistry = mock(ActivityResultRegistry.class);

        DropInClient dropInClient = mock(DropInClient.class);
        DropInLifecycleObserver sut =
                new DropInLifecycleObserver(activityResultRegistry, dropInClient);

        ActivityResultLauncher<DropInIntentData> activityLauncher = mock(ActivityResultLauncher.class);
        sut.activityLauncher = activityLauncher;

        DropInIntentData dropInIntentData =
                new DropInIntentData(dropInRequest, authorization, "sample-session-id");
        sut.launch(dropInIntentData);

        verify(activityLauncher).launch(dropInIntentData);
    }
}
