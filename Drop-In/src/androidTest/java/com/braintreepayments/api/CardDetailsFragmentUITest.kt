package com.braintreepayments.api

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.braintreepayments.api.CardNumber.THREE_D_SECURE_VERIFICATON
import com.braintreepayments.api.CardNumber.VISA
import com.braintreepayments.api.dropin.R
import com.braintreepayments.cardform.view.CardForm
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
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
            fragmentManager.setFragmentResultListener("BRAINTREE_EVENT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_RESULT") as EditCardNumberEvent
                assertEquals(VISA, event.cardNumber)
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
            fragmentManager.setFragmentResultListener("BRAINTREE_EVENT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_RESULT") as CardDetailsEvent
                assertEquals(VISA, event.card.number)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
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
            fragmentManager.setFragmentResultListener("BRAINTREE_EVENT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_RESULT") as CardDetailsEvent
                assertEquals(VISA, event.card.number)
                assertNull(event.card.cardholderName)
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
            fragmentManager.setFragmentResultListener("BRAINTREE_EVENT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_RESULT") as CardDetailsEvent
                assertEquals(VISA, event.card.number)
                assertEquals("Brian Tree", event.card.cardholderName)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun whenStateIsRESUMED_onCardFormSubmit_whenCardholderNameRequired_sendsCardDetailsEventWithCardholderName() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN).cardholderNameStatus(CardForm.FIELD_REQUIRED))
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
            fragmentManager.setFragmentResultListener("BRAINTREE_EVENT", activity) { requestKey, result ->
                val event = result.get("BRAINTREE_RESULT") as CardDetailsEvent
                assertEquals(VISA, event.card.number)
                assertEquals("Brian Tree", event.card.cardholderName)
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
                fragment.dropInViewModel.setCardFormFieldErrors(ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_NON_NUMBER_ERROR_RESPONSE))

                assertEquals(fragment.context?.getString(R.string.bt_expiration_invalid),
                        fragment.cardForm.expirationDateEditText.textInputLayoutParent?.error)
                assertEquals(fragment.context?.getString(R.string.bt_cvv_invalid,
                        fragment.context?.getString(fragment.cardForm.cardEditText.cardType.securityCodeName)), fragment.cardForm.cvvEditText.textInputLayoutParent?.error)
                assertEquals(fragment.context?.getString(R.string.bt_postal_code_invalid),
                        fragment.cardForm.postalCodeEditText.textInputLayoutParent?.error)
            }
    }

    // TODO: cancelling from 3DS2 flow is returning RESULT_OK in DropInActivity#onActivityResult - investigate
    @Test
    fun whenStateIsRESUMED_whenThreeDSecureIsCanceled_showsSubmitButtonAgain() {
        val args = Bundle()
        args.putParcelable("EXTRA_DROP_IN_REQUEST", DropInRequest().clientToken(Fixtures.CLIENT_TOKEN))
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", CardFormConfiguration(true, true))
        args.putString("EXTRA_CARD_NUMBER", THREE_D_SECURE_VERIFICATON)

        val scenario = FragmentScenario.launchInContainer(CardDetailsFragment::class.java, args, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_card_form_expiration)).perform(typeText(ExpirationDate.VALID_EXPIRATION))
        onView(withId(R.id.bt_button)).perform(click())
//        mActivity.setDropInRequest(new DropInRequest()
//                .tokenizationKey(TOKENIZATION_KEY)
//                .amount("1.00")
//                .requestThreeDSecureVerification(true));
//
//        Configuration configuration = Configuration.fromJson(new TestConfigurationBuilder()
//                .creditCards(getSupportedCardConfiguration())
//                .threeDSecureEnabled(true)
//                .build());
//        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD));
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .getConfigurationSuccess(configuration)
//                .cardTokenizeSuccess(cardNonce)
//                .handleThreeDSecureActivityResultError(new Exception("user canceled"))
//                .shouldPerformThreeDSecureVerification(true)
//                .build();
//        setup(dropInClient);
//
//        setText(mAddCardView, R.id.bt_card_form_card_number, VISA);
//        mAddCardView.findViewById(R.id.bt_button).performClick();
//        setText(mEditCardView, R.id.bt_card_form_expiration, ExpirationDate.VALID_EXPIRATION);
//        mEditCardView.findViewById(R.id.bt_button).performClick();
//
//        verify(dropInClient).performThreeDSecureVerification(same(mActivity), same(cardNonce), any(DropInResultCallback.class));
//
//        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isVisible();
//        assertThat(mEditCardView.findViewById(R.id.bt_button)).isGone();
//
//        mActivity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, RESULT_CANCELED, null);
//
//        assertThat(mEditCardView.findViewById(R.id.bt_animated_button_loading_indicator)).isGone();
//        assertThat(mEditCardView.findViewById(R.id.bt_button)).isVisible();
    }
}