package com.braintreepayments.api

import android.os.Bundle
import android.view.View
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braintreepayments.api.CardNumber.*
import com.braintreepayments.api.dropin.R
import com.braintreepayments.cardform.utils.CardType
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddCardFragmentUITest {

    private val args: Bundle = Bundle()

    @Before
    fun setup() {
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
    }

    @Test
    fun whenStateIsRESUMED_buttonTextIsAddCard() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
        }

        onView(isRoot()).perform(waitFor(500))

        // TODO: migrate assertions to use plain text instead of internal view ids
        onView(withId(R.id.bt_button)).check(matches(withText(R.string.bt_next)))
    }

    @Test
    fun whenStateIsRESUMED_cardNumberFieldIsFocused() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_card_form_card_number)).check(matches(isFocused()))
    }

    @Test
    fun whenStateIsRESUMED_andNonCardNumberError_doesNotShowError() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
            fragment.dropInViewModel.setCardTokenizationError(
                    ErrorWithResponse.fromJson(IntegrationTestFixtures.CREDIT_CARD_EXPIRATION_ERROR_RESPONSE))

            assertNull(fragment.cardForm.cardEditText.textInputLayoutParent?.error)
        }
    }

    @Test
    fun whenStateIsRESUMED_sendsAnalyticsEvent() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.SEND_ANALYTICS, event.type)
                assertEquals("card.selected", event.getString(DropInEventProperty.ANALYTICS_EVENT_NAME))
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_onSubmitButtonClick_whenCardFormValid_showsLoader() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_card_number)).perform(typeText(VISA))
        onView(withId(R.id.bt_button)).perform(click())
        onView(withId(R.id.bt_button)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_animated_button_loading_indicator)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_onSubmitButtonClick_whenCardFormNotValid_showsSubmitButton() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_card_number)).perform(typeText(INVALID_VISA))
        onView(withId(R.id.bt_button)).perform(click())
        onView(withId(R.id.bt_button)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_animated_button_loading_indicator)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_onSubmitButtonClick_whenCardTypeNotSupported_showsSubmitButton() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_card_number)).perform(typeText(AMEX))
        onView(withId(R.id.bt_button)).perform(click())
        onView(withId(R.id.bt_button)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_animated_button_loading_indicator)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_onSubmitButtonClickWithValidCardNumber_sendsAddCardEvent() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_card_number)).perform(typeText(VISA))
        onView(withId(R.id.bt_button)).perform(click())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.ADD_CARD_SUBMIT, event.type)
                assertEquals(VISA, event.getString(DropInEventProperty.CARD_NUMBER))
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_whenCardNumberValidationErrorsArePresentInViewModel_displaysErrorsInlineToUser() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
            fragment.dropInViewModel.setCardTokenizationError(
                    ErrorWithResponse.fromJson(IntegrationTestFixtures.CREDIT_CARD_ERROR_RESPONSE))

            assertEquals(fragment.context?.getString(R.string.bt_card_number_invalid),
                    fragment.cardForm.cardEditText.textInputLayoutParent?.error)
        }
    }

    @Test
    fun whenStateIsRESUMED_whenCardNumberIsDuplicate_displaysErrorsInlineToUser() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
            fragment.dropInViewModel.setCardTokenizationError(
                    ErrorWithResponse.fromJson(IntegrationTestFixtures.ERRORS_CREDIT_CARD_DUPLICATE))

            assertEquals(fragment.context?.getString(R.string.bt_card_already_exists),
                    fragment.cardForm.cardEditText.textInputLayoutParent?.error)
        }
    }

    @Test
    fun whenStateIsRESUMED_whenCardFromInvalid_showsSubmitButton() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
            fragment.dropInViewModel.setCardTokenizationError(
                    ErrorWithResponse.fromJson(IntegrationTestFixtures.CREDIT_CARD_ERROR_RESPONSE))
        }

        onView(withId(R.id.bt_button)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_andCardLogosDisabled_supportedCardTypesViewIsGONE() {
        val dropInRequest = DropInRequest()
        dropInRequest.setCardLogosDisabled(true);

        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        scenario.onFragment { fragment ->
            assertEquals(View.GONE, fragment.supportedCardTypesView.visibility);
        }
    }

    @Test
    fun whenStateIsRESUMED_andCardLogosEnabled_supportedCardTypesViewIsVISIBLE() {
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        scenario.onFragment { fragment ->
            assertEquals(View.VISIBLE, fragment.supportedCardTypesView.visibility);
        }
    }
}
