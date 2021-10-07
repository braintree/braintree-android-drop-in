package com.braintreepayments.api

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity

@RunWith(AndroidJUnit4::class)
class DropInActivityUITest {

    @Test
    fun whenStateIsRESUMED_whenBrowserSwitchResultExists_finishesWithResult() {
        val authorization = Authorization.fromString(Fixtures.TOKENIZATION_KEY)
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = ThreeDSecureRequest()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, DropInActivity::class.java)
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)

        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
            .paymentMethodNonce(paymentMethodNonce)
            .deviceData("device data")

        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .deliverBrowserSwitchResultSuccess(dropInResult)
            .build()

        val controller = buildActivity(DropInActivity::class.java, intent)

        val activity = controller.get()
        activity.dropInClient = dropInClient

        controller.setup()
        // TODO: figure out how to verify setResult is called on a non-mocked activity
    }
}