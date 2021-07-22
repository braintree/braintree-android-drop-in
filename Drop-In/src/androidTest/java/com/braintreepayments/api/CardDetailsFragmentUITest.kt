package com.braintreepayments.api

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.braintreepayments.api.CardNumber.VISA
import com.braintreepayments.api.dropin.R
import com.braintreepayments.cardform.view.CardForm
import com.braintreepayments.cardform.view.CardForm.FIELD_REQUIRED
import junit.framework.TestCase.*
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch


class CardDetailsFragmentUITest {

    private lateinit var countDownLatch: CountDownLatch

    @Before
    fun beforeEach() {
        countDownLatch = CountDownLatch(1)
    }

    @Test
    fun whenStateIsRESUMED_buttonTextIsAddCard() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_button)).check(matches(withText(R.string.bt_add_card)))
    }

    @Test
    fun whenStateIsRESUMED_expirationDateFieldIsFocused() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_card_form_expiration)).check(matches(isFocused()))
    }

    @Test
    fun whenStateIsRESUMED_displaysCVVAndPostalCodeWhenPresentInConfiguration() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(true, true))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_cvv)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_card_form_postal_code)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_hidesCVVandPostalCodeWhenNotPresentInConfiguration() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_cvv)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_card_form_postal_code)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_whenDropInRequestRequiresCardholderName_setsCardFormsCardholderNameToRequired() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).cardholderNameStatus(FIELD_REQUIRED))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_expiration)).perform(typeText(ExpirationDate.VALID_EXPIRATION))
        scenario.onFragment { fragment ->
            assertFalse(fragment.cardForm.isValid)
        }

        onView(withId(R.id.bt_card_form_cardholder_name)).perform(typeText("Brian Tree"))
        scenario.onFragment { fragment ->
            assertTrue(fragment.cardForm.isValid)
        }
    }

    @Test
    fun whenStateIsRESUMED_whenDropInCardholderNameFieldDisabled_saveCardCheckboxCheckedAndHidden() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(isChecked()))
    }

    @Test
    fun whenStateIsRESUMED_withAllowVaultCardOverrideTrueAndTokenizationKey_hidesSaveCardCheckbox() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().tokenizationKey(Fixtures.TOKENIZATION_KEY).allowVaultCardOverride(true))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_withAllowVaultCardOverrideTrue_showsSaveCardCheckbox() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).allowVaultCardOverride(true))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_withAllowVaultCardOverrideFalse_showsSaveCardCheckbox() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).allowVaultCardOverride(false))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_withAllowVaultCardOverrideTrue_andVaultCardTrue_showsSaveCardCheckboxChecked() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).allowVaultCardOverride(true).vaultCard(true))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(isChecked()))
    }

    @Test
    fun whenStateIsRESUMED_withAllowVaultCardOverrideTrue_andVaultCardFalse_showsSaveCardCheckboxNotChecked() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).allowVaultCardOverride(true).vaultCard(false))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(not(isChecked())))
    }

    @Test
    fun whenStateIsRESUMED_onCardNumberFieldFocus_returnsToAddCardFragment() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_card_number)).perform(click())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.EDIT_CARD_NUMBER, event.type)
                assertEquals(VISA, event.getString(DropInEventProperty.CARD_NUMBER))
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_sendsCardDetailsEvent() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_expiration)).perform(typeText(ExpirationDate.VALID_EXPIRATION))
        onView(withId(R.id.bt_button)).perform(click())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.CARD_DETAILS_SUBMIT, event.type)
                assertEquals(VISA, event.getCard(DropInEventProperty.CARD).number)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenCardFormNotValid_doesNotSendCardDetailsEventAndShowsButton() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_button)).perform(click())

        val events = mutableListOf<Parcelable>()
        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                events += result
            }
        }
        assertEquals(0, events.size)
        onView(withId(R.id.bt_button)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenOptionalCardholderNameFieldIsEmpty_sendsCardDetailsEventWithoutCardholderName() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).cardholderNameStatus(CardForm.FIELD_OPTIONAL))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_expiration)).perform(typeText(ExpirationDate.VALID_EXPIRATION))
        onView(withId(R.id.bt_button)).perform(click())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.CARD_DETAILS_SUBMIT, event.type)

                val card = event.getCard(DropInEventProperty.CARD)
                assertEquals(VISA, card.number)
                assertNull(card.cardholderName)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenOptionalCardholderNameFieldIsFilled_sendsCardDetailsEventWithCardholderName() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).cardholderNameStatus(CardForm.FIELD_OPTIONAL))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_expiration)).perform(typeText(ExpirationDate.VALID_EXPIRATION))
        onView(withId(R.id.bt_card_form_cardholder_name)).perform(typeText("Brian Tree"))
        onView(withId(R.id.bt_button)).perform(click())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.CARD_DETAILS_SUBMIT, event.type)

                val card = event.getCard(DropInEventProperty.CARD)
                assertEquals(VISA, card.number)
                assertEquals("Brian Tree", card.cardholderName)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenCardholderNameRequired_sendsCardDetailsEventWithCardholderName() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).cardholderNameStatus(FIELD_REQUIRED))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_expiration)).perform(typeText(ExpirationDate.VALID_EXPIRATION))
        onView(withId(R.id.bt_card_form_cardholder_name)).perform(typeText("Brian Tree"))
        onView(withId(R.id.bt_button)).perform(click())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.CARD_DETAILS_SUBMIT, event.type)

                val card = event.getCard(DropInEventProperty.CARD)
                assertEquals(VISA, card.number)
                assertEquals("Brian Tree", card.cardholderName)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_whenCardValidationErrorsArePresentInViewModel_displaysErrorsInlineToUser() {
            val args = Bundle()
            args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
            args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(true, true))
            args.putString("EXTRA_CARD_NUMBER", VISA)

            val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
            scenario.moveToState(Lifecycle.State.RESUMED)

            scenario.onFragment { fragment ->
                fragment.dropInViewModel.setCardTokenizationError(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_NON_NUMBER_ERROR_RESPONSE))

                assertEquals(fragment.context?.getString(R.string.bt_expiration_invalid),
                        fragment.cardForm.expirationDateEditText.textInputLayoutParent?.error)
                assertEquals(fragment.context?.getString(R.string.bt_cvv_invalid,
                        fragment.context?.getString(fragment.cardForm.cardEditText.cardType.securityCodeName)), fragment.cardForm.cvvEditText.textInputLayoutParent?.error)
                assertEquals(fragment.context?.getString(R.string.bt_postal_code_invalid),
                        fragment.cardForm.postalCodeEditText.textInputLayoutParent?.error)
            }
    }

    @Test
    fun whenStateIsRESUMED_whenThreeDSecureUserCancelationPresentInViewModel_showsSubmitButtonAgain() {
        // TODO: observe view model for UserCancelation cardTokenizationError and reset submit button
    }
}