package com.braintreepayments.api

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DropInLauncherUnitTest : TestCase() {

    @Test
    fun constructor_registersForActivityResultAndForwardsResultCallback() {
        val activity = mockk<ComponentActivity>(relaxed = true)
        val activityResultRegistry = mockk<ActivityResultRegistry>(relaxed = true)
        every { activity.activityResultRegistry } returns activityResultRegistry

        val callback = mockk<DropInLauncherCallback>(relaxed = true)
        DropInLauncher(activity, callback)

        val expectedKey = "com.braintreepayments.api.DropIn.RESULT"
        verify {
            activityResultRegistry.register(
                expectedKey,
                activity,
                any<DropInActivityResultContract>(),
                callback
            )
        }
    }

    @Test
    fun launch_launchesActivity() {
        val activity = mockk<ComponentActivity>(relaxed = true)
        val activityResultRegistry = mockk<ActivityResultRegistry>(relaxed = true)
        every { activity.activityResultRegistry } returns activityResultRegistry

        val activityLauncher: ActivityResultLauncher<DropInLaunchInput> = mockk(relaxed = true)
        every {
            activityResultRegistry.register(
                any(),
                any(),
                any<DropInActivityResultContract>(),
                any()
            )
        } returns activityLauncher

        val callback = mockk<DropInLauncherCallback>(relaxed = true)
        val sut = DropInLauncher(activity, callback)

        val dropInRequest = DropInRequest()
        sut.start(Fixtures.BASE64_CLIENT_TOKEN, dropInRequest)

        val slot = slot<DropInLaunchInput>()
        verify { activityLauncher.launch(capture(slot)) }

        val capturedLaunchIntent = slot.captured
        assertSame(capturedLaunchIntent.dropInRequest, dropInRequest)
        assertEquals(Fixtures.BASE64_CLIENT_TOKEN, capturedLaunchIntent.authorization.toString())
    }
}