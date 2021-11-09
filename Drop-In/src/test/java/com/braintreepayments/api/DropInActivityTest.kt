package com.braintreepayments.api

import android.app.Activity.RESULT_FIRST_USER
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.*
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import java.util.*
import com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST

import android.os.Bundle
import org.junit.After
import org.junit.Ignore

@RunWith(RobolectricTestRunner::class)
class DropInActivityTest {

    private lateinit var activityController: ActivityController<DropInActivity>
    private lateinit var activity: DropInActivity
    private lateinit var authorization: Authorization
    private lateinit var dropInRequest: DropInRequest

    @Before
    fun beforeEach() {
        authorization = Authorization.fromString(Fixtures.TOKENIZATION_KEY)
        dropInRequest = DropInRequest()
    }

    @After
    fun afterEach() {
        activityController.stop()
        activityController.destroy()
    }

    // region onCreate

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

    // endregion

    // region Browser Switch Results

    @Test
    fun onResume_deliversBrowserSwitchResult() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)

        verify(dropInClient).deliverBrowserSwitchResult(same(activity), any(DropInResultCallback::class.java))
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
    fun onResume_whenBrowserSwitchResultExists_finishesWithResult() {
        dropInRequest.threeDSecureRequest = ThreeDSecureRequest()

        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
        dropInResult.paymentMethodNonce = paymentMethodNonce
        dropInResult.deviceData = "device data"

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
    fun onResume_whenBrowserSwitchResultExists_sendAnalyticsEvent() {
        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
        dropInResult.paymentMethodNonce = paymentMethodNonce
        dropInResult.deviceData = "device data"

        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .deliverBrowserSwitchResultSuccess(dropInResult)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        verify(dropInClient).sendAnalyticsEvent("sdk.exit.success")
    }

    @Test
    fun onResume_whenBrowserSwitchResultExists_storesResult() {
        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
        dropInResult.paymentMethodNonce = paymentMethodNonce
        dropInResult.deviceData = "device data"

        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .deliverBrowserSwitchResultSuccess(dropInResult)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        assertEquals(DropInPaymentMethodType.VISA.canonicalName,
            BraintreeSharedPreferences.getInstance()
                .getString(activity, DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null))
    }

    // endregion

    // region Activity Results

    @Test
    fun onActivityResult_whenDropInResultExists_finishesActivity() {
        val paymentMethodNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
        dropInResult.paymentMethodNonce = paymentMethodNonce
        dropInResult.deviceData = "device data"

        val dropInClient = MockDropInClientBuilder()
            .handleActivityResultSuccess(dropInResult)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        activity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, RESULT_OK, mock(Intent::class.java))
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN)

        assertEquals(RESULT_OK, shadowActivity.resultCode)
        assertEquals(dropInResult, shadowActivity.resultIntent
            .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT))
        assertTrue(activity.isFinishing)
    }

//    @Test
//    fun onActivityResult_whenResultIsUserCanceledException_setsUserCanceledErrorInViewModel() {
//        val error = UserCanceledException("User canceled 3DS.")
//        val dropInClient = MockDropInClientBuilder()
//            .handleActivityResultError(error)
//            .build()
//        setupDropInActivity(dropInClient, dropInRequest)
//
//        activity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, RESULT_OK, mock(Intent::class.java))
//        assertFalse(activity.isFinishing)
//        assertEquals(error, activity.dropInViewModel.userCanceledError.value)
//    }

    @Test
    fun onActivityResult_whenError_finishesWithError() {
        val error = Exception("A 3DS error")
        val dropInClient = MockDropInClientBuilder()
            .handleActivityResultError(error)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        activity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, RESULT_OK, mock(Intent::class.java))

        verify(dropInClient).sendAnalyticsEvent("sdk.exit.sdk-error")
        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        assertEquals(error, shadowActivity.resultIntent.getSerializableExtra(DropInResult.EXTRA_ERROR))
        assertTrue(activity.isFinishing)
    }

    // endregion

    // region Send Analytics Event

    @Test
    fun onSendAnalyticsEvent_sendsAnalyticsViaDropInClient() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        val dropInEvent = DropInEvent.createSendAnalyticsEvent("test-analytics.event")

        setupDropInActivity(dropInClient, dropInRequest)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, dropInEvent.toBundle())

        verify(dropInClient).sendAnalyticsEvent("test-analytics.event")
    }

    // endregion

    // region Vaulted Payment Method Selected Event

    @Test
    fun onVaultedPaymentMethodSelectedEvent_whenCard_sendsAnalyticsEvent() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInEvent = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, dropInEvent.toBundle())

        verify(dropInClient).sendAnalyticsEvent("vaulted-card.select")
    }

    @Test
    fun onVaultedPaymentMethodSelectedEvent_whenPayPal_doesNotSendAnalyticsEvent() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)

        val payPalAccountNonce =
            PayPalAccountNonce.fromJSON(JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON))
        val dropInEvent = DropInEvent.createVaultedPaymentMethodSelectedEvent(payPalAccountNonce)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, dropInEvent.toBundle())

        verify(dropInClient, never()).sendAnalyticsEvent("vaulted-card.select")
    }

    @Test
    fun onVaultedPaymentMethodSelectedEvent_when3DSFails_reloadsPaymentMethodsAndFinishes() {
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
    fun onVaultedPaymentMethodSelectedEvent_whenShouldNotRequest3DS_returnsANonce() {
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
    fun onVaultedPaymentMethodSelectedEvent_whenShouldNotRequest3DS_onDeviceDataError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .shouldPerformThreeDSecureVerification(false)
            .collectDeviceDataError(error)
            .build()

        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce).toBundle())

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
        assertSame(error, actualError)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onVaultedPaymentSelectedEvent_whenShouldPerform3DSVerification_requests3DSVerification() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .shouldPerformThreeDSecureVerification(true)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce).toBundle())

        verify(dropInClient).performThreeDSecureVerification(same(activity), same(cardNonce), any(DropInResultCallback::class.java))
    }

    @Test
    fun onVaultedPaymentMethodSelectedEvent_returnsDeviceData() {
        val dropInClient = MockDropInClientBuilder()
            .collectDeviceDataSuccess("device-data")
            .shouldPerformThreeDSecureVerification(false)
            .authorization(authorization)
            .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
            .getSupportedPaymentMethodsSuccess(ArrayList())
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce).toBundle())
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN)

        val result = shadowActivity.resultIntent.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT) as DropInResult?
        assertEquals("device-data", result!!.deviceData)
    }

    // endregion

    // region Delete Payment Method Nonce Event

    @Test
    fun onDeletePaymentMethodNonceEvent_whenDialogConfirmed_sendsAnalyticsEvent() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()

        val alertPresenter = mock(AlertPresenter::class.java)
        doAnswer { invocation ->
            val callback = invocation.arguments[2] as DialogInteractionCallback
            callback.onDialogInteraction(DialogInteraction.POSITIVE)
        }.`when`(alertPresenter).showConfirmNonceDeletionDialog(
            any(Context::class.java),
            any(PaymentMethodNonce::class.java),
            any(DialogInteractionCallback::class.java)
        )

        setupDropInActivity(dropInClient, dropInRequest)
        activity.alertPresenter = alertPresenter
        activity.onDropInEvent(DropInEvent.createDeleteVaultedPaymentMethodNonceEvent(cardNonce))

        verify(dropInClient).sendAnalyticsEvent("manager.delete.confirmation.positive")
    }

    @Test
    fun onDeletePaymentMethodNonceEvent_whenDialogCancelled_sendsAnalyticsEvent() {
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

    // endregion

    // region Remove Payment Method Nonce

    @Test
    fun removePaymentMethodNonce_whenNonceNotNull_sendsAnalyticsEvent() {
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
    fun removePaymentMethodNonce_whenPaymentMethodDeleteException_sendsAnalyticsEvent() {
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
    fun removePaymentMethodNonce_whenUnknownError_sendsAnalyticsEvent() {
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
    fun removePaymentMethodNonce_onError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .deletePaymentMethodError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        activity.removePaymentMethodNonce(mock(PaymentMethodNonce::class.java))

        assertTrue(activity.isFinishing)
        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception
        assertSame(error, actualError)
    }

    // endregion

    // region Supported Payment Method Selected Event

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypePayPal_tokenizesPayPal() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
            .getSupportedPaymentMethodsSuccess(ArrayList())
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAYPAL)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        verify(dropInClient).tokenizePayPalRequest(same(activity), any(PayPalFlowStartedCallback::class.java))
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypePayPal_onPayPalError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .payPalError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAYPAL)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
        assertSame(error, actualError)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypeVenmo_tokenizesVenmo() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
            .getSupportedPaymentMethodsSuccess(ArrayList())
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAY_WITH_VENMO)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        verify(dropInClient).tokenizeVenmoAccount(same(activity), any(VenmoTokenizeAccountCallback::class.java))
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypeVenmo_onVenmoError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .venmoError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAY_WITH_VENMO)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
        assertSame(error, actualError)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypeGooglePay_tokenizesGooglePay() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
            .getSupportedPaymentMethodsSuccess(ArrayList())
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.GOOGLE_PAY)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        verify(dropInClient).requestGooglePayPayment(same(activity), any(GooglePayRequestPaymentCallback::class.java))
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypeGooglePay_onGooglePayError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .googlePayError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.GOOGLE_PAY)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
        assertSame(error, actualError)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypeUnknown_showsAddCardFragment() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
            .getSupportedPaymentMethodsSuccess(ArrayList())
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.UNKNOWN)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())
        activity.supportFragmentManager.executePendingTransactions()

        assertNotNull(activity.supportFragmentManager.findFragmentByTag("ADD_CARD"))
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypeUnknown_prefetchesSupportedCardTypes() {
        val supportedCardTypes = arrayListOf("Visa")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getSupportedCardTypesSuccess(supportedCardTypes)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.UNKNOWN)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertSame(DropInPaymentMethodType.getCardsTypes(supportedCardTypes)[0], activity.dropInViewModel.supportedCardTypes.value!![0])
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypeUnknown_onGetSupportedCardTypesError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getSupportedCardTypesError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.UNKNOWN)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
        assertSame(error, actualError)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onSupportedPaymentMethodSelectedEvent_withTypeUnknown_onCardTypesError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getSupportedPaymentMethodsError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event =
            DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.UNKNOWN)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
        assertSame(error, actualError)
        assertTrue(activity.isFinishing)
    }

    // endregion

    // region Add Card Submit Event

    @Test
    fun onAddCardSubmitEvent_showsCardDetailsFragment() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
            .getSupportedPaymentMethodsSuccess(ArrayList())
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event =
            DropInEvent.createAddCardSubmitEvent("4111111111111111")
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())
        activity.supportFragmentManager.executePendingTransactions()

        assertNotNull(activity.supportFragmentManager.findFragmentByTag("CARD_DETAILS"))
    }

    // endregion

    // region Edit Card Number Event

    @Test
    fun onEditCardNumberEvent_showsAddCardFragment() {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event =
            DropInEvent.createEditCardNumberEvent("4111111111111111")
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())
        activity.supportFragmentManager.executePendingTransactions()

        assertNotNull(activity.supportFragmentManager.findFragmentByTag("ADD_CARD"))
    }

    // endregion

    // region Card Details Submit Event

    @Test
    fun onCardDetailsSubmitEvent_onTokenizeCardSuccess_whenShouldNotRequest3DS_finishesWithResult() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .cardTokenizeSuccess(cardNonce)
            .shouldPerformThreeDSecureVerification(false)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event = DropInEvent.createCardDetailsSubmitEvent(Card())
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN)

        assertEquals(RESULT_OK, shadowActivity.resultCode)
        val result = shadowActivity.resultIntent.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT) as DropInResult?
        assertEquals(cardNonce.string, result!!.paymentMethodNonce!!.string)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onCardDetailsSubmitEvent_onTokenizeCardSuccess_whenShouldRequest3DS_on3DSSuccess_finishesWithResult() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val dropInResult = DropInResult()
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .cardTokenizeSuccess(cardNonce)
            .shouldPerformThreeDSecureVerification(true)
            .threeDSecureSuccess(dropInResult)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event = DropInEvent.createCardDetailsSubmitEvent(Card())
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN)

        assertEquals(RESULT_OK, shadowActivity.resultCode)
        val result = shadowActivity.resultIntent.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT) as DropInResult?
        assertSame(result, dropInResult)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onCardDetailsSubmitEvent_onTokenizeCardSuccess_whenShouldRequest3DS_on3DSError_finishesWithError() {
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .cardTokenizeSuccess(cardNonce)
            .shouldPerformThreeDSecureVerification(true)
            .threeDSecureError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event = DropInEvent.createCardDetailsSubmitEvent(Card())
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
        assertSame(error, actualError)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun onCardDetailsSubmitEvent_onError_whenErrorWithResponse_setsCardTokenizationErrorInViewModel() {
        val error = ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_ERROR_RESPONSE)
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .cardTokenizeError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event = DropInEvent.createCardDetailsSubmitEvent(Card())
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertEquals(error, activity.dropInViewModel.cardTokenizationError.value)
    }

    @Test
    fun onCardDetailsSubmitEvent_onError_whenErrorNotWithResponse_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .cardTokenizeError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event = DropInEvent.createCardDetailsSubmitEvent(Card())
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
        assertSame(error, actualError)
        assertTrue(activity.isFinishing)
    }

    // endregion

    // region Show Vault Manager Event

    @Test
    fun onShowVaultManagerEvent_onVaultedPaymentMethodsSuccess_setsVaultedPaymentMethodsInViewModel() {
        val vaultedPaymentMethods = ArrayList<PaymentMethodNonce>()
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getVaultedPaymentMethodsSuccess(vaultedPaymentMethods)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        val event = DropInEvent(DropInEventType.SHOW_VAULT_MANAGER)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertSame(vaultedPaymentMethods, activity.dropInViewModel.vaultedPaymentMethods.value)
    }

    @Test
    fun onShowVaultManagerEvent_onVaultedPaymentMethodsError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .getVaultedPaymentMethodsError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        val event = DropInEvent(DropInEventType.SHOW_VAULT_MANAGER)
        activity.supportFragmentManager.setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle())

        assertTrue(activity.isFinishing)
        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception
        assertSame(error, actualError)
    }

    // endregion

    // region Bottom Sheet State

    @Test
    @Ignore
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
    fun onDidShowBottomSheet_whenClientTokenPresent_onGetVaultedPaymentMethodsSuccess_setsVaultedPaymentsInViewModel() {
        val vaultedPaymentMethods = ArrayList<PaymentMethodNonce>()
        val dropInClient = MockDropInClientBuilder()
            .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
            .getSupportedPaymentMethodsSuccess(ArrayList())
            .getVaultedPaymentMethodsSuccess(vaultedPaymentMethods)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)

        activity.dropInViewModel.setBottomSheetState(BottomSheetState.SHOWN)

        assertSame(vaultedPaymentMethods, activity.dropInViewModel.vaultedPaymentMethods.value)
    }

    @Test
    fun onDidShowBottomSheet_whenClientTokenPresent_onGetVaultedPaymentMethodsError_finishesWithError() {
        val error = Exception("error")
        val dropInClient = MockDropInClientBuilder()
            .authorization(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
            .getSupportedPaymentMethodsSuccess(ArrayList())
            .getVaultedPaymentMethodsError(error)
            .build()
        setupDropInActivity(dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        activity.dropInViewModel.setBottomSheetState(BottomSheetState.SHOWN)

        assertTrue(activity.isFinishing)
        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualError = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception
        assertSame(error, actualError)
    }

    // endregion

    // region Errors

    @Test
    fun configurationExceptionExitsActivityWithError() {
        assertExceptionIsReturned(
            "configuration-exception",
            ConfigurationException("Configuration exception")
        )
    }

    @Test
    fun authenticationExceptionExitsActivityWithError() {
        assertExceptionIsReturned(
            "developer-error",
            AuthenticationException("Access denied")
        )
    }

    @Test
    fun authorizationExceptionExitsActivityWithError() {
        assertExceptionIsReturned(
            "developer-error",
            AuthorizationException("Access denied")
        )
    }

    @Test
    fun upgradeRequiredExceptionExitsActivityWithError() {
        assertExceptionIsReturned(
            "developer-error",
            UpgradeRequiredException("Exception")
        )
    }

    @Test
    fun serverExceptionExitsActivityWithError() {
        assertExceptionIsReturned(
            "server-error",
            ServerException("Exception")
        )
    }

    @Test
    fun unexpectedExceptionExitsActivityWithError() {
        assertExceptionIsReturned(
            "server-error",
            UnexpectedException("Exception")
        )
    }

    @Test
    fun downForMaintenanceExceptionExitsActivityWithError() {
        assertExceptionIsReturned(
            "server-unavailable",
            ServiceUnavailableException("Exception")
        )
    }

    @Test
    fun anyExceptionExitsActivityWithError() {
        assertExceptionIsReturned("sdk-error", java.lang.Exception("Error!"))
    }

    // endregion

    // region Helpers

    private fun setupDropInActivity(dropInClient: DropInClient, dropInRequest:DropInRequest) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dropInRequestBundle = Bundle()
        dropInRequestBundle.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInRequest)
        val intent = Intent(context, DropInActivity::class.java)
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST_BUNDLE, dropInRequestBundle)

        try {
            activityController = buildActivity(DropInActivity::class.java, intent)
            activity = activityController.get()
            activity.dropInClient = dropInClient
            activityController.setup()
        } catch (e: java.lang.Exception) {
            System.out.println(Log.getStackTraceString(e))
            throw e
        }
    }

    private fun assertExceptionIsReturned(analyticsEvent: String, exception: java.lang.Exception) {
        val dropInClient = MockDropInClientBuilder()
            .authorization(authorization)
            .build()
        setupDropInActivity( dropInClient, dropInRequest)
        val shadowActivity = shadowOf(activity)

        activity.onError(exception)
        verify(dropInClient).sendAnalyticsEvent("sdk.exit.$analyticsEvent")
        assertTrue(activity.isFinishing)
        assertEquals(RESULT_FIRST_USER, shadowActivity.resultCode)
        val actualException = shadowActivity.resultIntent
            .getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception
        assertSame(exception, actualException)
    }

    private fun createBrowserSwitchSuccessResult(): BrowserSwitchResult {
        val requestCode = 123
        val url = Uri.parse("https://example.com")
        val metadata = JSONObject()
        val returnUrlScheme = "sample-scheme"
        val browserSwitchRequest =
            BrowserSwitchRequest(requestCode, url, metadata, returnUrlScheme, true)
        return BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, browserSwitchRequest)
    }

    // endregion
}