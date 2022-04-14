package com.braintreepayments.api

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.FragmentActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DropInLifecycleObserverUnitTest : TestCase() {

    @Test
    fun onCreate_registersForAnActivityResult() {
        val activityResultRegistry = mockk<ActivityResultRegistry>(relaxed = true)
        val dropInClient = mockk<DropInClient>(relaxed = true)

        val sut = DropInLifecycleObserver(activityResultRegistry, dropInClient)

        val lifecycleOwner = FragmentActivity()
        sut.onCreate(lifecycleOwner)

        val expectedKey = "com.braintreepayments.api.DropIn.RESULT"
        verify {
            activityResultRegistry.register(
                expectedKey,
                lifecycleOwner,
                any<DropInActivityResultContract>(),
                any()
            )
        }
    }

    @Test
    fun onCreate_whenActivityResultReceived_forwardsResultToDropInClient() {
        val activityResultRegistry = mockk<ActivityResultRegistry>(relaxed = true)
        val dropInClient = mockk<DropInClient>(relaxed = true)

        val callbackSlot = slot<ActivityResultCallback<DropInResult>>()
        val activityLauncher: ActivityResultLauncher<DropInIntentData> = mockk(relaxed = true)
        every {
            activityResultRegistry.register(
                any(),
                any(),
                any<DropInActivityResultContract>(),
                capture(callbackSlot)
            )
        } returns activityLauncher

        val lifecycleOwner = FragmentActivity()
        val sut = DropInLifecycleObserver(activityResultRegistry, dropInClient)
        sut.onCreate(lifecycleOwner)

        val dropInResult = DropInResult()
        callbackSlot.captured.onActivityResult(dropInResult)
        verify { dropInClient.onDropInResult(dropInResult) }
    }

    @Test
    fun launch_launchesActivity() {
        val dropInRequest = DropInRequest()
        val authorization = Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN)

        val activityResultRegistry = mockk<ActivityResultRegistry>(relaxed = true)
        val dropInClient = mockk<DropInClient>(relaxed = true)

        val activityLauncher: ActivityResultLauncher<DropInIntentData> = mockk(relaxed = true)
        every {
            activityResultRegistry.register(
                any(),
                any(),
                any<DropInActivityResultContract>(),
                any()
            )
        } returns activityLauncher

        val sut = DropInLifecycleObserver(activityResultRegistry, dropInClient)

        val lifecycleOwner = FragmentActivity()
        sut.onCreate(lifecycleOwner)

        val dropInIntentData = DropInIntentData(dropInRequest, authorization, "sample-session-id")
        sut.launch(dropInIntentData)

        verify { activityLauncher.launch(dropInIntentData) }
    }
}