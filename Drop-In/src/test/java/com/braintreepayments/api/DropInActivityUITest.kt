package com.braintreepayments.api

import android.app.Activity
import android.app.Activity.RESULT_FIRST_USER
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert
import junit.framework.TestCase
import junit.framework.TestCase.*
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import java.util.*

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
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
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

    @Test
    fun onResume_whenBrowserSwitchReturnsUserCanceledException_setsUserCanceledErrorInViewModel() {
        val error = UserCanceledException("User canceled 3DS.")
        val dropInClient = MockDropInClientBuilder()
            .deliverBrowseSwitchResultError(error)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)

        assertEquals(error, activity.dropInViewModel.userCanceledError.value)
    }

    @Test
    fun onResume_whenBrowserSwitchError_forwardsError() {
        val error = Exception("A 3DS error")
        val dropInClient = MockDropInClientBuilder()
            .deliverBrowseSwitchResultError(error)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        verify(dropInClient).sendAnalyticsEvent("sdk.exit.sdk-error")
        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        assertEquals(error, shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR))
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onActivityResult_whenDropInResultExists_finishesActivity() {
        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
            .paymentMethodNonce(paymentMethodNonce)
            .deviceData("device data")

        val dropInClient = MockDropInClientBuilder()
            .handleActivityResultSuccess(dropInResult)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        activity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, 1, mock(Intent::class.java))
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN)

        assertEquals(RESULT_OK, shadowActivity.resultCode)
        assertEquals(dropInResult, shadowActivity.resultIntent
            .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT))
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onActivityResult_whenResultIsUserCanceledException_setsUserCanceledErrorInViewModel() {
        val error = UserCanceledException("User canceled 3DS.")
        val dropInClient = MockDropInClientBuilder()
            .handleActivityResultError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        activity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, 1, mock(Intent::class.java))
        assertFalse(activity.isFinishing)
        assertEquals(error, activity.dropInViewModel.userCanceledError.value)
    }

    @Test
    fun onActivityResult_whenError_finishesWithError() {
        val error = Exception("A 3DS error")
        val dropInClient = MockDropInClientBuilder()
            .handleActivityResultError(error)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        activity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, 1, mock(Intent::class.java))

        verify(dropInClient).sendAnalyticsEvent("sdk.exit.sdk-error")
        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        assertEquals(error, shadowActivity.resultIntent.getSerializableExtra(DropInResult.EXTRA_ERROR))
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onResume_sendsAnalyticsEventWhenShown() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)

        verify(dropInClient).sendAnalyticsEvent("appeared")
    }

    @Test
    fun onResume_setsIntegrationTypeToDropinForDropinActivity() {
        setupDropInActivity(mock(DropInClient::class.java), dropInRequest)

        // TODO: revisit integration type metadata and consider passing integration (core PR)
        // type through BraintreeClient constructor instead of relying on reflection
//        assertEquals("dropin3", mActivity.getDropInClient().getIntegrationType());
    }

    @Test
    fun onResume_onVaultedPaymentMethodSelectedEvent_whenThreeDSecureVerificationFails_reloadsPaymentMethodsAndFinishes() {
        authorization = Authorization.fromString(UnitTestFixturesHelper.base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN))
        val error = Exception("three d secure failure")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .threeDSecureError(error)
            .shouldPerformThreeDSecureVerification(true)
            .build()

        val threeDSecureRequest = ThreeDSecureRequest()
        threeDSecureRequest.amount = "1.00"

        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest
        dropInRequest.requestThreeDSecureVerification = true

        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce).toBundle())

        verify(dropInClient).getVaultedPaymentMethods(same(activity), any(GetPaymentMethodNoncesCallback::class.java))
        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        assertEquals(error, shadowActivity.resultIntent.getSerializableExtra(DropInResult.EXTRA_ERROR))
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onResume_onVaultedPaymentMethodSelectedEvent_whenShouldNotRequestThreeDSecureVerification_returnsANonce() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .shouldPerformThreeDSecureVerification(false)
            .collectDeviceDataSuccess("sample-data")
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce).toBundle())
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN)

        verify(dropInClient, never()).performThreeDSecureVerification(same(activity), same(cardNonce), any(DropInResultCallback::class.java))
        assertEquals(RESULT_OK, shadowActivity.resultCode)
        val result = shadowActivity.resultIntent.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT) as DropInResult?
        assertEquals(cardNonce.string, result!!.paymentMethodNonce!!.string)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onResume_onVaultedPaymentSelectedEvent_whenShouldPerformThreeDSecureVerification_requestsThreeDSecureVerification() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .shouldPerformThreeDSecureVerification(true)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce).toBundle())

        verify(dropInClient).performThreeDSecureVerification(same(activity), same(cardNonce), any(DropInResultCallback::class.java))
        verify(dropInClient).sendAnalyticsEvent("sdk.exit.success")
    }

    @Test
    fun onBackPressed_setsDropInViewModelBottomSheetStateHideRequested() {
        // TODO: Invesigate if the onBackPressed code path is needed in DropInActivity - doesn't appear to ever get hit
    }

    @Test
    fun onDidHideBottomSheet_whenNoResultPresent_sendAnalyticsEvent() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN)

        verify(dropInClient).sendAnalyticsEvent("sdk.exit.canceled")
    }

    @Test
    fun onResume_whenResultPresent_sendAnalyticsEvent() {
        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
            .paymentMethodNonce(paymentMethodNonce)
            .deviceData("device data")

        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .deliverBrowserSwitchResultSuccess(dropInResult)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        verify(dropInClient).sendAnalyticsEvent("sdk.exit.success")
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