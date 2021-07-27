package com.braintreepayments.api

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4ClassRunner::class)
class SelectPaymentMethodParentFragmentUITest {

    private lateinit var countDownLatch: CountDownLatch

    @Before
    fun beforeEach() {
        countDownLatch = CountDownLatch(1)
    }

    @Test
    fun whenStateIsRESUMED_onChildFragmentEvent_propagatesEventToParent() {
        val scenario =
                FragmentScenario.launchInContainer(SelectPaymentMethodParentFragment::class.java)

        val events = mutableListOf<DropInEvent>()

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val parentFragmentManager = fragment.parentFragmentManager

            parentFragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                events += DropInEvent.fromBundle(result)
            }

            val event = DropInEvent(DropInEventType.SHOW_VAULT_MANAGER)
            val childFragmentManager = fragment.childFragmentManager
            childFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(isRoot()).perform(waitFor(1000))

        val vaultManagerEvent = events.first { it.type == DropInEventType.SHOW_VAULT_MANAGER }
        assertNotNull(vaultManagerEvent)
    }
}