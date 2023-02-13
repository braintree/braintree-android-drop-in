package com.braintreepayments.api

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import io.mockk.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// TODO: Slowly migrate the entire DropInClientUnitTest to Kotlin
@RunWith(RobolectricTestRunner::class)
class DropInClientUnitTestKt {

    companion object {

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            // required for mockk since AuthorizationCallback is package-private
            registerInstanceFactory { mockk<AuthorizationCallback>() }
        }
    }

    private val applicationContext: Context = ApplicationProvider.getApplicationContext()

    private lateinit var fragment: Fragment
    private lateinit var fragmentLifecycle: Lifecycle

    private lateinit var activity: FragmentActivity
    private lateinit var activityLifecycle: Lifecycle

    private lateinit var resultRegistry: ActivityResultRegistry

    private lateinit var dropInRequest: DropInRequest
    private lateinit var braintreeClient: BraintreeClient

    private lateinit var clientToken: Authorization
    private lateinit var clientTokenProvider: ClientTokenProvider

    @Before
    fun beforeEach() {

        dropInRequest = DropInRequest()
        braintreeClient = mockk()

        clientToken = Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN)
        clientTokenProvider = mockk()

        activity = mockk(relaxed = true)
        every { activity.applicationContext } returns applicationContext

        resultRegistry = mockk()
        every { activity.activityResultRegistry } returns resultRegistry

        activityLifecycle = mockk(relaxed = true)
        every { activity.lifecycle } returns activityLifecycle

        fragment = mockk()
        every { fragment.requireActivity() } returns activity

        fragmentLifecycle = mockk()
        every { fragment.lifecycle } returns fragmentLifecycle

        // This suppresses errors from WorkManager initialization within BraintreeClient
        // initialization (AnalyticsClient)
        WorkManagerTestInitHelper.initializeTestWorkManager(applicationContext)
    }

    @Test
    fun constructor_setsIntegrationTypeDropIn() {
        val sut = DropInClient(activity, dropInRequest, clientTokenProvider)
        assertEquals(IntegrationType.DROP_IN, sut.braintreeClient.integrationType)
    }

    @Test
    fun constructor_withFragment_registersLifecycleObserver() {
        val observerSlot = slot<DropInLifecycleObserver>()
        justRun { fragmentLifecycle.addObserver(capture(observerSlot)) }

        val sut = DropInClient(fragment, dropInRequest, clientTokenProvider)
        val capturedObserver = observerSlot.captured

        assertSame(capturedObserver, sut.observer)
        assertSame(resultRegistry, capturedObserver.activityResultRegistry)
        assertSame(sut, capturedObserver.dropInClient)
    }

    @Test
    fun constructor_withActivity_registersLifecycleObserver() {
        val observerSlot = slot<DropInLifecycleObserver>()
        justRun { activityLifecycle.addObserver(capture(observerSlot)) }

        val sut = DropInClient(activity, dropInRequest, clientTokenProvider)
        val capturedObserver = observerSlot.captured

        assertSame(capturedObserver, sut.observer)
        assertSame(resultRegistry, capturedObserver.activityResultRegistry)
        assertSame(sut, capturedObserver.dropInClient)
    }

    @Test
    fun constructor_withContext_doesNotRegisterLifecycleObserver() {
        val sut = DropInClient(applicationContext, Fixtures.TOKENIZATION_KEY, dropInRequest)
        assertNull(sut.observer)
    }

    @Test
    fun legacy_launchDropInForResult_withObserver_launchesWithObserver() {
        every { braintreeClient.sessionId } returns "sample-session-id"

        every { braintreeClient.getAuthorization(any()) } answers { call ->
            val callback = call.invocation.args[0] as AuthorizationCallback
            callback.onAuthorizationResult(clientToken, null)
        }

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)
        val sut = DropInClient(params)
        sut.observer = mockk()

        val intentDataSlot = slot<DropInIntentData>()
        justRun { sut.observer.launch(capture(intentDataSlot)) }

        sut.launchDropInForResult(activity, 123)
        val capturedIntentData = intentDataSlot.captured

        assertEquals("sample-session-id", capturedIntentData.sessionId)
        assertEquals(clientToken.toString(), capturedIntentData.authorization.toString())
        assertNotNull(capturedIntentData.dropInRequest)
    }

    @Test
    fun launchDropIn_withObserver_launchesWithObserver() {
        every { braintreeClient.sessionId } returns "sample-session-id"

        every { braintreeClient.getAuthorization(any()) } answers { call ->
            val callback = call.invocation.args[0] as AuthorizationCallback
            callback.onAuthorizationResult(clientToken, null)
        }

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)
        val sut = DropInClient(params)
        sut.observer = mockk()

        val intentDataSlot = slot<DropInIntentData>()
        justRun { sut.observer.launch(capture(intentDataSlot)) }

        sut.launchDropIn()
        val capturedIntentData = intentDataSlot.captured

        assertEquals("sample-session-id", capturedIntentData.sessionId)
        assertEquals(clientToken.toString(), capturedIntentData.authorization.toString())
        assertNotNull(capturedIntentData.dropInRequest)
    }

    @Test
    fun launchDropIn_forwardsAuthorizationFetchErrorsToListener() {
        every { braintreeClient.sessionId } returns "sample-session-id"

        val authError = Exception("auth error")
        every { braintreeClient.getAuthorization(any()) } answers { call ->
            val callback = call.invocation.args[0] as AuthorizationCallback
            callback.onAuthorizationResult(null, authError)
        }

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)
        val sut = DropInClient(params)

        val listener = mockk<DropInListener>(relaxed = true)
        sut.setListener(listener)

        sut.launchDropIn()
        verify { listener.onDropInFailure(authError)}
    }

    @Test
    fun onDropInResult_notifiesListenerOfSuccess() {
        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
        val sut = DropInClient(params)

        val listener = mockk<DropInListener>(relaxed = true)
        sut.setListener(listener)

        val dropInResult = DropInResult()
        dropInResult.paymentMethodNonce =
            CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))

        sut.onDropInResult(dropInResult)
        verify { listener.onDropInSuccess(dropInResult) }
    }

    @Test
    fun onDropInResult_notifiesListenerOfFailure() {
        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
        val sut = DropInClient(params)

        val listener = mockk<DropInListener>(relaxed = true)
        sut.setListener(listener)

        val dropInResult = DropInResult()
        val error = Exception("error")
        dropInResult.error = error

        sut.onDropInResult(dropInResult)
        verify { listener.onDropInFailure(error) }
    }
}
