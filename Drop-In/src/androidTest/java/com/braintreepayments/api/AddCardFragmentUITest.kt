package com.braintreepayments.api

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braintreepayments.api.CardNumber.VISA
import com.braintreepayments.api.dropin.R
import com.braintreepayments.cardform.utils.CardType
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AddCardFragmentUITest {

    private lateinit var countDownLatch: CountDownLatch

    @Before
    fun beforeEach() {
        countDownLatch = CountDownLatch(1)
    }

    @Test
    fun whenStateIsRESUMED_buttonTextIsAddCard() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, null, R.style.bt_drop_in_activity_theme)
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
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, null, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_card_form_card_number)).check(matches(isFocused()))
    }

    @Test
    fun whenStateIsRESUMED_andNonCardNumberError_doesNotShowError() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, null, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
            fragment.dropInViewModel.setCardTokenizationError(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_EXPIRATION_ERROR_RESPONSE))

            assertNull(fragment.cardForm.cardEditText.textInputLayoutParent?.error)
        }
    }

    @Test
    fun whenStateIsRESUMED_andAddCardSelected_sendsAnalyticsEvent() {
        // TODO: assert 'card.selected' analytics event sent
    }

    @Test
    fun whenStateIsRESUMED_onSubmitButtonClick_showsLoader() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, null, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_card_number)).perform(typeText(VISA))
        onView(withId(R.id.bt_button)).perform(click())
        onView(withId(R.id.bt_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_onSubmitButtonClickWithValidCardNumber_sendsAddCardEvent() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, null, R.style.bt_drop_in_activity_theme)
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
            fragmentManager.setFragmentResultListener("BRAINTREE_EVENT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_RESULT") as AddCardEvent
                assertEquals(VISA, event.cardNumber)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_whenCardNumberValidationErrorsArePresentInViewModel_displaysErrorsInlineToUser() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, null, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
            fragment.dropInViewModel.setCardTokenizationError(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_ERROR_RESPONSE))

            assertEquals(fragment.context?.getString(R.string.bt_card_number_invalid),
                    fragment.cardForm.cardEditText.textInputLayoutParent?.error)
        }
    }

    @Test
    fun whenStateIsRESUMED_whenCardFromInvalid_showsSubmitButton() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, null, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
            fragment.dropInViewModel.setCardTokenizationError(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_ERROR_RESPONSE))
        }

        onView(withId(R.id.bt_button)).check(matches(isDisplayed()))
    }
}
