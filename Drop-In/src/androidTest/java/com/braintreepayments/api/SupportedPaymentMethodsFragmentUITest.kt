package com.braintreepayments.api

import android.view.View.VISIBLE
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braintreepayments.api.dropin.R
import org.hamcrest.Matchers.not
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

// Ref: https://developer.android.com/guide/fragments/test
@RunWith(AndroidJUnit4::class)
class SupportedPaymentMethodsFragmentUITest {

    private lateinit var countDownLatch: CountDownLatch

    private val supportedPaymentMethods = listOf(
            DropInPaymentMethodType.PAYPAL,
            DropInPaymentMethodType.PAY_WITH_VENMO,
            DropInPaymentMethodType.UNKNOWN,
            DropInPaymentMethodType.GOOGLE_PAYMENT
    )

    @Before
    fun beforeEach() {
        countDownLatch = CountDownLatch(1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun whenStateIsRESUMED_loaderIsVisible() {
        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.bt_select_payment_method_loader)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenSupportedPaymentMethodsLoaded_displaysPaymentMethods() {
        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_select_payment_method_loader)).check(matches(not(isDisplayed())))
        onView(withText("PayPal")).check(matches(isDisplayed()))
        onView(withText("Venmo")).check(matches(isDisplayed()))
        onView(withText("Credit or Debit Card")).check(matches(isDisplayed()))
        onView(withText("Google Pay")).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenSupportedPaymentMethodsLoaded_requestsForVaultedPaymentMethodsToBeLoaded() {
        // TODO: assert an event is dispatched to load vaulted payment methods
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerIsEnabledAndVaultedPaymentMethodsAreLoaded_displaysVaultedPaymentMethods() {
        val dropInRequest = DropInRequest()
        dropInRequest.isVaultManagerEnabled = true
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))
        val payPalNonce = PayPalAccountNonce.fromJSON(JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON))
        val venmoAccountNonce = VenmoAccountNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VENMO_ACCOUNT_RESPONSE))

        val vaultedPaymentMethods = listOf(cardNonce, payPalNonce, venmoAccountNonce)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_vaulted_payment_methods_header)).check(matches(isDisplayed()))
        onView(withText("paypalaccount@example.com")).check(matches(isDisplayed()))
        onView(withText("venmojoe")).check(matches(isDisplayed()))
        onView(withText("1111")).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerEnabledAndNoVaultedPaymentMethodsFound_showsNothing() {
        val dropInRequest = DropInRequest()
        dropInRequest.isVaultManagerEnabled = true
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(emptyList())
        }

        onView(withId(R.id.bt_vaulted_payment_methods_header)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerIsEnabledAndVaultedPaymentMethodsAreLoaded_displaysVaultEditButton() {
        val dropInRequest = DropInRequest()
        dropInRequest.isVaultManagerEnabled = true
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))
        val vaultedPaymentMethods = listOf(cardNonce)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)
        }

        onView(withId(R.id.bt_vault_edit_button)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerIsDisabled_hidesVaultEditButton() {
        val dropInRequest = DropInRequest()
        dropInRequest.isVaultManagerEnabled = false
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(withId(R.id.bt_vault_edit_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_whenCreditOrDebitCardSelected_sendsPaymentMethodSelectedEvent() {
        val dropInRequest = DropInRequest()
        dropInRequest.isVaultManagerEnabled = false
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withText("Credit or Debit Card")).perform(click())
        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.SUPPORTED_PAYMENT_METHOD_SELECTED, event.type)

                val paymentMethodType =
                    event.getDropInPaymentMethodType(DropInEventProperty.SUPPORTED_PAYMENT_METHOD)
                assertEquals(DropInPaymentMethodType.UNKNOWN, paymentMethodType)
                countDownLatch.countDown()
            }
        }

        countDownLatch.await()
    }

    @Test(timeout = 5000)
    fun whenStateIsRESUMED_whenVaultManagerEnabledAndVaultedCardExists_sendsAnalyticEvent() {
        // TODO: capture all analytics events within a time interval and assert that the target analytics event is emitted

//        val dropInRequest = DropInRequest()
//                .vaultManager(true)
//        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)
//
//        val scenario = FragmentScenario.launchInContainer(SelectPaymentMethodFragment::class.java, bundle)
//        scenario.moveToState(Lifecycle.State.RESUMED)
//
//        scenario.onFragment { fragment ->
//            val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))
//            val vaultedPaymentMethods = listOf(cardNonce)
//
//            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
//            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)
//
//            val activity = fragment.requireActivity()
//            val fragmentManager = fragment.parentFragmentManager
//
//            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { requestKey, result ->
//                val event = result.get(DropInEvent.RESULT_KEY) as DropInAnalyticsEvent
//                assertEquals(event.fragment, "vaulted-card.appear")
//                countDownLatch.countDown()
//            }
//        }
//        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerEnabledAndVaultedCardDoesNotExist_doesNotSendAnalyticEvent() {
        // TODO: capture all analytics events within a time interval and assert that the target analytics event is emitted
    }

    @Test
    fun whenStateIsRESUMED_whenVaultEditButtonClicked_sendsAnalyticsEvent() {
        // TODO: capture all analytics events within a time interval and assert that the target analytics event is emitted
    }

    @Test
    fun whenStateIsRESUMED_whenPaymentMethodSelected_showsLoadingView() {
        val dropInRequest = DropInRequest()
        dropInRequest.isVaultManagerEnabled = false
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withText("Credit or Debit Card")).perform(click())
        scenario.onFragment { fragment ->
            assertTrue(fragment.dropInViewModel.isLoading.value!!)
        }
    }

    @Test
    fun whenStateIsRESUMED_whenUserCanceledErrorPresentInViewModel_hidesLoader() {
        val dropInRequest = DropInRequest()
        dropInRequest.isVaultManagerEnabled = false
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.mLoadingIndicatorWrapper.visibility = VISIBLE
            fragment.dropInViewModel.setUserCanceledError(Exception("User canceled PayPal."))
        }

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_supported_payment_methods)).check(matches(isDisplayed()))
    }
}