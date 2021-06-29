package com.braintreepayments.api

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braintreepayments.cardform.utils.CardType
import org.junit.Test
import org.junit.runner.RunWith
import com.braintreepayments.api.dropin.R
import org.hamcrest.CoreMatchers.not

@RunWith(AndroidJUnit4::class)
class AddCardFragmentUITest {

    @Test
    fun whenStateIsRESUMED_sendsAnalyticsEventToNotifyAddCardSelected() {
        // TODO: assert 'card.selected' analytics event dispatched
    }

    @Test
    fun whenStateIsRESUMED_onSubmitButtonClick_showsLoader() {
        val scenario = FragmentScenario.launchInContainer(AddCardFragment::class.java, null, R.style.bt_drop_in_activity_theme)
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.dropInViewModel.setSupportedCardTypes(listOf(CardType.VISA))
        }

        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.bt_button)).perform(click())
        onView(withId(R.id.bt_button)).check(matches(not(isDisplayed())))
    }
}