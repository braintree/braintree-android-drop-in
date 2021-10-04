package com.braintreepayments.api

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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

    @Test
    fun whenStateIsRESUMED_onFragmentResult_whenAnalyticsEvent_sendsAnalyticsViaDropInClient() {
        val authorization = Authorization.fromString(Fixtures.TOKENIZATION_KEY)
        val dropInRequest = DropInRequest()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, DropInActivity::class.java)
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)

        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        val dropInEvent = DropInEvent.createSendAnalyticsEvent("test-analytics.event")

        val controller = buildActivity(DropInActivity::class.java, intent)

        val activity = controller.get()
        activity.dropInClient = dropInClient

        controller.setup()
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, dropInEvent.toBundle())

        verify(dropInClient).sendAnalyticsEvent("test-analytics.event")
    }

    @Test
    fun whenStateIsRESUMED_onVaultedPaymentMethodSelected_sendsAnalyticsEvent() {
        val authorization = Authorization.fromString(Fixtures.TOKENIZATION_KEY)
        val dropInRequest = DropInRequest()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, DropInActivity::class.java)
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)

        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        val controller = buildActivity(DropInActivity::class.java, intent)

        val activity = controller.get()
        activity.dropInClient = dropInClient

        controller.setup()

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInEvent = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce)
        activity.onVaultedPaymentMethodSelected(dropInEvent)

        verify(dropInClient).sendAnalyticsEvent("vaulted-card.select")
    }

    @Test
    fun whenStateIsRESUMED_removePaymentMethodNonce_whenNonceNotNull_sendsAnalyticsEvent() {
        // TODO: test this after determining analytics testing strategy
//        verify(dropInClient).sendAnalyticsEvent("manager.delete.succeeded");
        val authorization = Authorization.fromString(Fixtures.TOKENIZATION_KEY)
        val dropInRequest = DropInRequest()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, DropInActivity::class.java)
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInClient = MockDropInClientBuilder()
            .deletePaymentMethodSuccess(cardNonce)
            .authorization(authorization)
            .build()

        val controller = buildActivity(DropInActivity::class.java, intent)

        val activity = controller.get()
        activity.dropInClient = dropInClient

        controller.setup()

        activity.removePaymentMethodNonce(cardNonce)

        verify(dropInClient).sendAnalyticsEvent("manager.delete.succeeded")
    }

    @Test
    fun whenStateIsRESUMED_removePaymentMethodNonce_whenPaymentMethodDeleteException_sendsAnalyticsEvent() {
        // TODO: test this after determining analytics testing strategy
//        verify(dropInClient).sendAnalyticsEvent("manager.delete.failure");
    }

    @Test
    fun whenStateIsRESUMED_removePaymentMethodNonce_whenUnknownError_sendsAnalyticsEvent() {
        // TODO: test this after determining analytics testing strategy
//        verify(dropInClient).sendAnalyticsEvent("manager.unknown.failed");
    }
}