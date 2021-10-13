package com.braintreepayments.api

import android.app.Activity.RESULT_FIRST_USER
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mockito.*
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class DropInActivityUITest {

    private lateinit var activityController: ActivityController<DropInActivity>
    private lateinit var activity: DropInActivity
    private lateinit var authorization: Authorization
    private lateinit var dropInRequest: DropInRequest

    private fun setupDropInActivity(dropInClient: DropInClient, dropInRequest:DropInRequest) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, DropInActivity::class.java)
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)

        activityController = buildActivity(DropInActivity::class.java, intent)
        activity = activityController.get()
        activity.dropInClient = dropInClient
        activityController.setup()
    }

    @Before
    fun beforeEach() {
        authorization = Authorization.fromString(Fixtures.TOKENIZATION_KEY)
        dropInRequest = DropInRequest()
    }

    @Test
    fun whenStateIsRESUMED_onFragmentResult_whenAnalyticsEvent_sendsAnalyticsViaDropInClient() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        val dropInEvent = DropInEvent.createSendAnalyticsEvent("test-analytics.event")

        setupDropInActivity(dropInClient, dropInRequest)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, dropInEvent.toBundle())

        verify(dropInClient).sendAnalyticsEvent("test-analytics.event")
    }

    @Test
    fun whenStateIsRESUMED_onVaultedPaymentMethodSelected_sendsAnalyticsEvent() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInEvent = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce)
        activity.onVaultedPaymentMethodSelected(dropInEvent)

        verify(dropInClient).sendAnalyticsEvent("vaulted-card.select")
    }

    @Test
    fun whenStateIsRESUMED_removePaymentMethodNonce_whenNonceNotNull_sendsAnalyticsEvent() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInClient = MockDropInClientBuilder()
            .deletePaymentMethodSuccess(cardNonce)
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        activity.removePaymentMethodNonce(cardNonce)

        verify(dropInClient).sendAnalyticsEvent("manager.delete.succeeded")
    }

    @Test
    fun whenStateIsRESUMED_removePaymentMethodNonce_whenPaymentMethodDeleteException_sendsAnalyticsEvent() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInClient = MockDropInClientBuilder()
            .deletePaymentMethodError(PaymentMethodDeleteException(cardNonce, Exception("error")))
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        activity.removePaymentMethodNonce(cardNonce)

        verify(dropInClient).sendAnalyticsEvent("manager.delete.failed")
    }

    @Test
    fun whenStateIsRESUMED_removePaymentMethodNonce_whenUnknownError_sendsAnalyticsEvent() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInClient = MockDropInClientBuilder()
            .deletePaymentMethodError(Exception("error"))
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        activity.removePaymentMethodNonce(cardNonce)

        verify(dropInClient).sendAnalyticsEvent("manager.unknown.failed")
    }

    @Test
    fun whenStateIsRESUMED_onDeletePaymentMethodDropInEvent_whenDialogConfirmed_sendsAnalyticsEvent() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        val alertPresenter = mock(AlertPresenter::class.java)
        doAnswer { invocation ->
            val callback = invocation.arguments[2] as DialogInteractionCallback
            callback.onDialogInteraction(DialogInteraction.POSITIVE)
        }.`when`(alertPresenter).showConfirmNonceDeletionDialog(
            Matchers.any(Context::class.java),
            Matchers.any(PaymentMethodNonce::class.java),
            Matchers.any(DialogInteractionCallback::class.java)
        )

        setupDropInActivity(dropInClient, dropInRequest)
        activity.alertPresenter = alertPresenter
        activity.onDropInEvent(DropInEvent.createDeleteVaultedPaymentMethodNonceEvent(cardNonce))

        verify(dropInClient).sendAnalyticsEvent("manager.delete.confirmation.positive")
    }

    @Test
    fun whenStateIsRESUMED_onDeletePaymentMethodDropInEvent_whenDialogCancelled_sendsAnalyticsEvent() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        val alertPresenter = mock(AlertPresenter::class.java)
        doAnswer { invocation ->
            val callback = invocation.arguments[2] as DialogInteractionCallback
            callback.onDialogInteraction(DialogInteraction.NEGATIVE)
        }.`when`(alertPresenter).showConfirmNonceDeletionDialog(
            any(Context::class.java),
            any(PaymentMethodNonce::class.java),
            any(DialogInteractionCallback::class.java)
        )

        setupDropInActivity(dropInClient, dropInRequest)
        activity.alertPresenter = alertPresenter
        activity.onDropInEvent(DropInEvent.createDeleteVaultedPaymentMethodNonceEvent(cardNonce))

        verify(dropInClient).sendAnalyticsEvent("manager.delete.confirmation.negative")
    }

    @Test
    fun onCreate_whenAuthorizationIsInvalid_finishesWithError() {
        val authorization = InvalidAuthorization("not a tokenization key", "error message")

        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val exception = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as java.lang.Exception?
        assertTrue(exception is InvalidArgumentException)
        assertEquals("Tokenization Key or Client Token was invalid.", exception!!.message)
    }

    @Test
    fun onResume_whenBrowserSwitchSuccessResultWillBeDelivered_updatesDropInStateToFINISHING() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getBrowserSwitchResult(createBrowserSwitchSuccessResult())
            .build()

        setupDropInActivity(dropInClient, dropInRequest)

        assertEquals(DropInState.WILL_FINISH, activity.dropInViewModel.dropInState.value)
    }

    @Test
    fun onResume_deliversBrowserSwitchResult() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)

        verify(dropInClient).deliverBrowserSwitchResult(same(activity), any(DropInResultCallback::class.java))
    }

    @Test
    fun onResume_whenBrowserSwitchResultExists_finishesWithResult() {
        dropInRequest.threeDSecureRequest = ThreeDSecureRequest()

        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
            .paymentMethodNonce(paymentMethodNonce)
            .deviceData("device data")

        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .deliverBrowserSwitchResultSuccess(dropInResult)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        assertEquals(RESULT_OK, shadowActivity.resultCode)
        assertEquals(dropInResult, shadowActivity.resultIntent
            .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT))
    }

    private fun createBrowserSwitchSuccessResult(): BrowserSwitchResult? {
        val requestCode = 123
        val url = Uri.parse("https://example.com")
        val metadata = JSONObject()
        val returnUrlScheme = "sample-scheme"
        val browserSwitchRequest =
            BrowserSwitchRequest(requestCode, url, metadata, returnUrlScheme, true)
        return BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, browserSwitchRequest)
    }
}