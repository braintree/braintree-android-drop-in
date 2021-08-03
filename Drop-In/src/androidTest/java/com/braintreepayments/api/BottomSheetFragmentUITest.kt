package com.braintreepayments.api

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.braintreepayments.api.dropin.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4ClassRunner::class)
class BottomSheetFragmentUITest {

    private lateinit var countDownLatch: CountDownLatch

    @Before
    fun beforeEach() {
        countDownLatch = CountDownLatch(1)
    }

    @Test
    fun whenStateIsRESUMED_displaysSupportedPaymentMethodsFragment() {
        val scenario =
                FragmentScenario.launchInContainer(BottomSheetFragment::class.java)

        scenario.onFragment { fragment ->
            val viewPagerAdapter =
                fragment.viewPager.adapter as BottomSheetViewAdapter
            assertEquals(1, viewPagerAdapter.itemCount)
            assertEquals(BottomSheetViewType.SUPPORTED_PAYMENT_METHODS.id, viewPagerAdapter.getItemId(0))
        }
    }

    @Test
    fun whenStateIsRESUMED_onShowVaultManagerEvent_showsVaultManager() {
        val scenario =
                FragmentScenario.launchInContainer(BottomSheetFragment::class.java)

        scenario.onFragment { fragment ->
            val childFragmentManager = fragment.childFragmentManager
            val showVaultManagerEvent = DropInEvent(DropInEventType.SHOW_VAULT_MANAGER)
            childFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, showVaultManagerEvent.toBundle())
        }

        onView(isRoot()).perform(waitFor(500))
        scenario.onFragment { fragment ->
            val currentItemPos = fragment.viewPager.currentItem
            assertEquals(1, currentItemPos)

            val viewPagerAdapter =
                    fragment.viewPager.adapter as BottomSheetViewAdapter
            assertEquals(2, viewPagerAdapter.itemCount)

            val currentItemId = viewPagerAdapter.getItemId(currentItemPos)
            assertEquals(BottomSheetViewType.VAULT_MANAGER.id, currentItemId)
        }
    }

    @Test
    fun whenStateIsRESUMED_andSupportedPaymentMethodsIsDisplayed_sendsCancelDropInEvent() {
        val scenario =
                FragmentScenario.launchInContainer(BottomSheetFragment::class.java)

        val events = mutableListOf<DropInEvent>()

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val parentFragmentManager = fragment.parentFragmentManager

            parentFragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                events += DropInEvent.fromBundle(result)
            }
        }

        onView(isRoot()).perform(waitFor(1000))
        onView(isRoot()).perform(ViewActions.pressBack())
        onView(isRoot()).perform(waitFor(1000))

        val cancelDropInEvent = events.first { it.type == DropInEventType.CANCEL_DROPIN }
        assertNotNull(cancelDropInEvent)
    }

    @Test
    fun whenStateIsRESUMED_andVaultManagerIsDisplayed_returnsToSupportedPaymentMethodsFragmentAndRemovesVaultManager() {
        val scenario =
                FragmentScenario.launchInContainer(BottomSheetFragment::class.java)

        scenario.onFragment { fragment ->
            val childFragmentManager = fragment.childFragmentManager
            val showVaultManagerEvent = DropInEvent(DropInEventType.SHOW_VAULT_MANAGER)
            childFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, showVaultManagerEvent.toBundle())
        }

        onView(isRoot()).perform(waitFor(1000))
        onView(isRoot()).perform(ViewActions.pressBack())
        onView(isRoot()).perform(waitFor(1000))

        scenario.onFragment { fragment ->
            val viewPagerAdapter =
                    fragment.viewPager.adapter as BottomSheetViewAdapter
            assertEquals(1, viewPagerAdapter.itemCount)
            assertEquals(BottomSheetViewType.SUPPORTED_PAYMENT_METHODS.id, viewPagerAdapter.getItemId(0))
        }
    }

    @Test
    fun whenStateIsRESUMED_andVaultManagerIsDisplayed_doesNotSendCancelDropInEvent() {
        val scenario =
                FragmentScenario.launchInContainer(BottomSheetFragment::class.java)

        val events = mutableListOf<DropInEvent>()

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val parentFragmentManager = fragment.parentFragmentManager

            parentFragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                events += DropInEvent.fromBundle(result)
            }

            val childFragmentManager = fragment.childFragmentManager
            val showVaultManagerEvent = DropInEvent(DropInEventType.SHOW_VAULT_MANAGER)
            childFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, showVaultManagerEvent.toBundle())
        }

        onView(isRoot()).perform(ViewActions.pressBack())
        onView(isRoot()).perform(waitFor(1000))

        val cancelDropInEvent = events.firstOrNull { it.type == DropInEventType.CANCEL_DROPIN }
        assertNull(cancelDropInEvent)
    }

    @Test
    fun whenStateIsRESUMED_onBackButtonPress_sendsCancelDropInEvent() {
        val scenario =
                FragmentScenario.launchInContainer(BottomSheetFragment::class.java)

        val events = mutableListOf<DropInEvent>()

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val parentFragmentManager = fragment.parentFragmentManager

            parentFragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                events += DropInEvent.fromBundle(result)
            }
        }

        onView(isRoot()).perform(waitFor(1000))
        onView(withId(R.id.back_button)).perform(click())
        onView(isRoot()).perform(waitFor(1000))

        val cancelDropInEvent = events.first { it.type == DropInEventType.CANCEL_DROPIN }
        assertNotNull(cancelDropInEvent)
    }

    @Test
    fun whenStateIsRESUMED_onChildFragmentEvent_propagatesEventToParent() {
        val scenario =
                FragmentScenario.launchInContainer(BottomSheetFragment::class.java)

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