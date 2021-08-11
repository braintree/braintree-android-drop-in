package com.braintreepayments.api

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.json.JSONObject
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DropInActivityUITest {

    @Ignore("Ignoring this test until Activity testing approach is finalized")
    @Test
    fun whenStateIsRESUMED_whenBrowserSwitchResultExists_finishesWithResult() {
        // TODO: Investigate mockito failures in androidTest and figure out a DropInClient dependency injection strategy for unit tests
        val authorization = Authorization.fromString(Fixtures.TOKENIZATION_KEY)
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = ThreeDSecureRequest()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, DropInUnitTestActivity::class.java)
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)

        val scenario = ActivityScenario.launch<DropInUnitTestActivity>(intent)

        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
                .paymentMethodNonce(paymentMethodNonce)
                .deviceData("device data")

        val dropInClient = MockDropInClientBuilder()
                .authorization(authorization)
                .deliverBrowserSwitchResultSuccess(dropInResult)
                .build()

        scenario.onActivity { activity ->
            activity.dropInClient = dropInClient
            activity.setDropInRequest(dropInRequest)
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    @Test
    fun whenStateIsRESUMED_onCancelDropInEvent_finishesWithResult() {
        val dropInRequest = DropInRequest()

        val threeDSecureRequest = ThreeDSecureRequest()
        threeDSecureRequest.isCardAddChallengeRequested = false
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, DropInActivity::class.java)
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)
        intent.putExtra(DropInClient.EXTRA_AUTHORIZATION, Fixtures.TOKENIZATION_KEY)
        intent.putExtra(DropInClient.EXTRA_SESSION_ID, "session-id")

        val scenario = ActivityScenario.launch<DropInActivity>(intent)
        scenario.onActivity { activity ->
            val fragmentManager = activity.supportFragmentManager

            val event = DropInEvent(DropInEventType.CANCEL_DROPIN)
            fragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())
        }

        assertEquals(RESULT_CANCELED, scenario.result.resultCode)
    }
}