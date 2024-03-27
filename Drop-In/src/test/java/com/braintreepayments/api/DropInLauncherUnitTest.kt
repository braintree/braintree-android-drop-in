package com.braintreepayments.api

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.same
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
                same(callback)
            )
        }
    }

    @Test
    fun launch_launchesActivity() {
        val activity = mockk<ComponentActivity>(relaxed = true)
        val activityResultRegistry = mockk<ActivityResultRegistry>(relaxed = true)
        every { activity.activityResultRegistry } returns activityResultRegistry

        val activityLauncher: ActivityResultLauncher<DropInLaunchIntent> = mockk(relaxed = true)
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
        val authorization = Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN)
        val dropInLaunchIntent =
            DropInLaunchIntent(dropInRequest, authorization, "fake-session-id")

        sut.launchDropIn(dropInLaunchIntent)
        verify { activityLauncher.launch(dropInLaunchIntent) }
    }
}