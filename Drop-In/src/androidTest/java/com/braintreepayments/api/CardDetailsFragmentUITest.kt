package com.braintreepayments.api

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.braintreepayments.api.CardNumber.VISA
import com.braintreepayments.api.dropin.R
import com.braintreepayments.cardform.view.CardForm.*
import junit.framework.TestCase.*
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class CardDetailsFragmentUITest {

    @Test
    fun whenStateIsRESUMED_buttonTextIsAddCard() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_button)).check(matches(withText(R.string.bt_add_card)))
    }

    @Test
    fun whenStateIsRESUMED_andCardholderNameDisabled_expirationDateFieldIsFocused() {
        val dropInRequest = DropInRequest()
        dropInRequest.cardholderNameStatus = FIELD_DISABLED
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_card_form_expiration)).check(matches(isFocused()))
    }

    @Test
    fun whenStateIsRESUMED_andCardholderNameOptional_cardholderNameFieldIsFocused() {
        val dropInRequest = DropInRequest()
        dropInRequest.cardholderNameStatus = FIELD_OPTIONAL
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_card_form_cardholder_name)).check(matches(isFocused()))
    }

    @Test
    fun whenStateIsRESUMED_andCardholderNameRequired_cardholderNameFieldIsFocused() {
        val dropInRequest = DropInRequest()
        dropInRequest.cardholderNameStatus = FIELD_REQUIRED
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.bt_card_form_cardholder_name)).check(matches(isFocused()))
    }

    @Test
    fun whenStateIsRESUMED_displaysCVVAndPostalCodeWhenPresentInConfiguration() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
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
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
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
        val dropInRequest = DropInRequest()
        dropInRequest.cardholderNameStatus = FIELD_REQUIRED
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
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
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
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
        val dropInRequest = DropInRequest()
        dropInRequest.allowVaultCardOverride = true
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        args.putBoolean("EXTRA_AUTH_IS_TOKENIZATION_KEY", true)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_withAllowVaultCardOverrideTrue_showsSaveCardCheckbox() {
        val dropInRequest = DropInRequest()
        dropInRequest.allowVaultCardOverride = true
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_withAllowVaultCardOverrideFalse_showsSaveCardCheckbox() {
        val dropInRequest = DropInRequest()
        dropInRequest.allowVaultCardOverride = false
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)
        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.bt_card_form_save_card_checkbox)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_withAllowVaultCardOverrideTrue_andVaultCardTrue_showsSaveCardCheckboxChecked() {
        val dropInRequest = DropInRequest()
        dropInRequest.allowVaultCardOverride = true
        dropInRequest.vaultCardDefaultValue = true
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
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
        val dropInRequest = DropInRequest()
        dropInRequest.allowVaultCardOverride = true
        dropInRequest.vaultCardDefaultValue = false
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
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
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
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
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_sendsCardDetailsEvent() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
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
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenCardFormValid_showsLoader() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(
            CardDetailsFragment::class.java,
            args,
            R.style.bt_drop_in_activity_theme
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_expiration)).perform(typeText(ExpirationDate.VALID_EXPIRATION))
        onView(withId(R.id.bt_button)).perform(click())

        onView(withId(R.id.bt_button)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bt_animated_button_loading_indicator)).check(matches(isDisplayed()))
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenCardFormNotValid_showsSubmitButton() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_button)).perform(click())

        onView(withId(R.id.bt_button)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_animated_button_loading_indicator)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenCardFormNotValid_doesNotSendCardDetailsEventAndShowsButton() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
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
        val dropInRequest = DropInRequest()
        dropInRequest.cardholderNameStatus = FIELD_OPTIONAL
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
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
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenOptionalCardholderNameFieldIsFilled_sendsCardDetailsEventWithCardholderName() {
        val dropInRequest = DropInRequest()
        dropInRequest.cardholderNameStatus = FIELD_OPTIONAL
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
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
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenCardholderNameRequired_sendsCardDetailsEventWithCardholderName() {
        val dropInRequest = DropInRequest()
        dropInRequest.cardholderNameStatus = FIELD_REQUIRED
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
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
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_onKeyboardGoActionPress_whenCardholderNameRequired_sendsCardDetailsEventWithCardholderName() {
        val dropInRequest = DropInRequest()
        dropInRequest.cardholderNameStatus = FIELD_REQUIRED
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest)
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(false, false))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_expiration)).perform(typeText(ExpirationDate.VALID_EXPIRATION))
        onView(withId(R.id.bt_card_form_cardholder_name)).perform(typeText("Brian Tree"))
        onView(withId(R.id.bt_card_form_expiration)).perform(pressImeActionButton())

        scenario.onFragment { fragment ->
            val activity = fragment.requireActivity()
            val fragmentManager = fragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, activity) { _, result ->
                val event = DropInEvent.fromBundle(result)
                assertEquals(DropInEventType.CARD_DETAILS_SUBMIT, event.type)

                val card = event.getCard(DropInEventProperty.CARD)
                assertEquals(VISA, card.number)
                assertEquals("Brian Tree", card.cardholderName)
            }
        }
    }

    @Test
    fun whenStateIsRESUMED_whenCardValidationErrorsArePresentInViewModel_displaysErrorsInlineToUser() {
            val args = Bundle()
            args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
            args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(true, true))
            args.putString("EXTRA_CARD_NUMBER", VISA)

            val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
            scenario.moveToState(Lifecycle.State.RESUMED)

            scenario.onFragment { fragment ->
                fragment.dropInViewModel.setCardTokenizationError(
                        ErrorWithResponse.fromJson(IntegrationTestFixtures.CREDIT_CARD_NON_NUMBER_ERROR_RESPONSE))

                assertEquals(fragment.context?.getString(R.string.bt_expiration_invalid),
                        fragment.cardForm.expirationDateEditText.textInputLayoutParent?.error)
                assertEquals(fragment.context?.getString(R.string.bt_cvv_invalid,
                        fragment.context?.getString(fragment.cardForm.cardEditText.cardType.securityCodeName)), fragment.cardForm.cvvEditText.textInputLayoutParent?.error)
                assertEquals(fragment.context?.getString(R.string.bt_postal_code_invalid),
                        fragment.cardForm.postalCodeEditText.textInputLayoutParent?.error)
            }
    }

    @Test
    fun whenStateIsRESUMED_whenUserCanceledErrorPresentInViewModel_showsSubmitButtonAgain() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(true, true))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.animatedButtonView.showLoading()
            fragment.dropInViewModel.setUserCanceledError(UserCanceledException("User canceled 3DS."))
        }
        onView(withId(R.id.bt_button)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_animated_button_loading_indicator)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenStateIsRESUMED_whenCardTokenizationErrorPresentInViewModel_showsSubmitButtonAgain() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest())
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(true, true))
        args.putString("EXTRA_CARD_NUMBER", VISA)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.animatedButtonView.showLoading()
            fragment.dropInViewModel.setCardTokenizationError(Exception("error"))
        }
        onView(withId(R.id.bt_button)).check(matches(isDisplayed()))
        onView(withId(R.id.bt_animated_button_loading_indicator)).check(matches(not(isDisplayed())))
    }
}