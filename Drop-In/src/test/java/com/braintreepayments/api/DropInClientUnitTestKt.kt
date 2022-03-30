package com.braintreepayments.api

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DropInClientUnitTestKt {

    private val applicationContext: Context = ApplicationProvider.getApplicationContext()

    private lateinit var fragment: Fragment
    private lateinit var fragmentLifecycle: Lifecycle

    private lateinit var activity: FragmentActivity
    private lateinit var activityLifecycle: Lifecycle

    private lateinit var resultRegistry: ActivityResultRegistry

    private lateinit var dropInRequest: DropInRequest
    private lateinit var braintreeClient: BraintreeClient

    private lateinit var clientTokenProvider: ClientTokenProvider

    @Before
    fun beforeEach() {

        dropInRequest = DropInRequest()
        braintreeClient = mockk()
        clientTokenProvider = mockk()

        activity = mockk()
        every { activity.applicationContext } returns applicationContext

        resultRegistry = mockk()
        every { activity.activityResultRegistry } returns resultRegistry

        activityLifecycle = mockk()
        every { activity.lifecycle } returns activityLifecycle

        fragment = mockk()
        every { fragment.requireActivity() } returns activity

        fragmentLifecycle = mockk()
        every { fragment.lifecycle } returns fragmentLifecycle

        // This suppresses errors from WorkManager initialization within BraintreeClient
        // initialization (AnalyticsClient)
        WorkManagerTestInitHelper.initializeTestWorkManager(applicationContext)
    }

    @Test
    fun constructor_withFragment_registersLifecycleObserver() {
        val observerSlot = slot<DropInLifecycleObserver>()
        justRun { fragmentLifecycle.addObserver(capture(observerSlot))}

        val sut = DropInClient(fragment, dropInRequest, clientTokenProvider)
        val capturedObserver = observerSlot.captured

        assertSame(capturedObserver, sut.observer)
        assertSame(resultRegistry, capturedObserver.activityResultRegistry)
        assertSame(sut, capturedObserver.dropInClient)
    }

    @Test
    fun constructor_withActivity_registersLifecycleObserver() {
        val observerSlot = slot<DropInLifecycleObserver>()
        justRun { activityLifecycle.addObserver(capture(observerSlot))}

        val sut = DropInClient(activity, dropInRequest, clientTokenProvider)
        val capturedObserver = observerSlot.captured

        assertSame(capturedObserver, sut.observer)
        assertSame(resultRegistry, capturedObserver.activityResultRegistry)
        assertSame(sut, capturedObserver.dropInClient)
    }

    @Test
    fun constructor_withContext_doesNotRegisterLifecycleObserver() {
        val sut = DropInClient(applicationContext, Fixtures.TOKENIZATION_KEY, dropInRequest)
        assertNull(sut.observer)
    }
}
