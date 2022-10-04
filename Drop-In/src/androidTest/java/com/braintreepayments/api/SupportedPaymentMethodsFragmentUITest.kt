package com.braintreepayments.api

import android.view.View
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
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock

// Ref: https://developer.android.com/guide/fragments/test
@RunWith(AndroidJUnit4::class)
class SupportedPaymentMethodsFragmentUITest {

    private val supportedPaymentMethods = listOf(
            DropInPaymentMethod.PAYPAL,
            DropInPaymentMethod.VENMO,
            DropInPaymentMethod.UNKNOWN,
            DropInPaymentMethod.GOOGLE_PAY
    )

    private val vaultedPaymentMethods = listOf(CardNonce.fromJSON(JSONObject(IntegrationTestFixtures.VISA_CREDIT_CARD_RESPONSE)))

    private lateinit var dropInRequest: DropInRequest

    @Before
    fun beforeEach() {
        dropInRequest = DropInRequest()
    }

    @Test
    @Throws(InterruptedException::class)
    fun whenStateIsRESUMED_loaderIsVisible() {
        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.bt_select_payment_method_loader)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_sendsAnalyticsEvent() {
        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.SEND_ANALYTICS, event.type)
                assertEquals("appeared", event.getString(DropInEventProperty.ANALYTICS_EVENT_NAME))
            }
        }
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
    fun whenStateIsRESUMED_whenVaultedPaymentMethodsLoaded_displaysVaultedPaymentMethods() {
        dropInRequest.isVaultManagerEnabled = false
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_vaulted_payment_methods)).check(matches(isDisplayed()))
        onView(withText("1111")).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenVaultedPaymentMethodShown_sendsAnalyticsEvent() {
        dropInRequest.isVaultManagerEnabled = true
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)

            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.SEND_ANALYTICS, event.type)
                assertEquals("vaulted-card.appear", event.getString(DropInEventProperty.ANALYTICS_EVENT_NAME))
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerIsEnabledAndVaultedPaymentMethodsAreLoaded_displaysVaultedPaymentMethods() {
        dropInRequest.isVaultManagerEnabled = true
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        val cardNonce = CardNonce.fromJSON(JSONObject(IntegrationTestFixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))
        val payPalNonce = PayPalAccountNonce.fromJSON(JSONObject(IntegrationTestFixtures.PAYPAL_ACCOUNT_JSON))
        val venmoAccountNonce = VenmoAccountNonce.fromJSON(JSONObject(IntegrationTestFixtures.PAYMENT_METHODS_VENMO_ACCOUNT_RESPONSE))

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
        dropInRequest.isVaultManagerEnabled = true
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        val cardNonce = CardNonce.fromJSON(JSONObject(IntegrationTestFixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))
        val vaultedPaymentMethods = listOf(cardNonce)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setVaultedPaymentMethods(vaultedPaymentMethods)
        }

        onView(withId(R.id.bt_vault_edit_button)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_whenVaultManagerIsDisabled_hidesVaultEditButton() {
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
                assertEquals(DropInPaymentMethod.UNKNOWN, paymentMethodType)
            }
        }

    }

    @Test
    fun whenStateIsRESUMED_whenPaymentMethodSelected_showsLoadingView() {
        dropInRequest.isVaultManagerEnabled = true

        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(isDisplayed()))

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withText("Credit or Debit Card")).perform(click())
        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_whenFragmentRecreated_hidesLoader() {
        dropInRequest.isVaultManagerEnabled = false
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(isDisplayed()))

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.onPaymentMethodSelected(DropInPaymentMethod.PAYPAL)
            fragment.dropInViewModel.setUserCanceledError(Exception("User canceled PayPal."))
        }

        scenario.recreate()
        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_supported_payment_methods)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_userCanceledErrorPresentInViewModel_hidesLoader() {
        dropInRequest.isVaultManagerEnabled = false
        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(isDisplayed()))

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
            fragment.dropInViewModel.setUserCanceledError(UserCanceledException("User canceled 3DS."))
        }

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_supported_payment_methods)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun whenStateIsRESUMED_whenHasSupportedPaymentMethods_hidesLoader() {
        dropInRequest.isVaultManagerEnabled = true

        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_supported_payment_methods)).check(matches(not(isDisplayed())))

        scenario.onFragment { fragment ->
            fragment.loadingIndicatorWrapper.visibility = View.VISIBLE
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_supported_payment_methods)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    @Ignore("Can't inject dropInViewModel")
    fun whenStateIsRESUMED_whenDoesNotHaveSupportedPaymentMethods_showsLoader() {
        // TODO: determine testing strategy for dropInViewModel state in onCreate
        dropInRequest.isVaultManagerEnabled = true

        val dropInViewModel = mock(DropInViewModel::class.java)
        Mockito.`when`(dropInViewModel.supportedCardTypes).thenReturn(null)

        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)

        scenario.onFragment { fragment ->
            fragment.loadingIndicatorWrapper.visibility = View.INVISIBLE
            fragment.dropInViewModel = dropInViewModel
        }
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_supported_payment_methods)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_whenDropInStateWillFinish_showsLoader() {
        dropInRequest.isVaultManagerEnabled = true

        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.loadingIndicatorWrapper.visibility = View.INVISIBLE
            fragment.dropInViewModel.setDropInState(DropInState.WILL_FINISH)
        }

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_supported_payment_methods)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_whenStateLoadingAndHasSupportedPaymentMethods_hidesLoader() {
        dropInRequest.isVaultManagerEnabled = true

        val bundle = bundleOf("EXTRA_DROP_IN_REQUEST" to dropInRequest)

        val scenario = FragmentScenario.launchInContainer(SupportedPaymentMethodsFragment::class.java, bundle)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(isDisplayed()))

        scenario.onFragment { fragment ->
            fragment.loadingIndicatorWrapper.visibility = View.VISIBLE
            fragment.viewState = SupportedPaymentMethodsFragment.ViewState.LOADING
            fragment.dropInViewModel.setSupportedPaymentMethods(supportedPaymentMethods)
        }

        onView(withId(R.id.bt_select_payment_method_loader_wrapper)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_supported_payment_methods)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}