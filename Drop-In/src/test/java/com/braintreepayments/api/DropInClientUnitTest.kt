package com.braintreepayments.api

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import io.mockk.*
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DropInClientUnitTest {

    //    @Captor
//    var paymentMethodTypesCaptor: ArgumentCaptor<List<DropInPaymentMethod>>? = null
//
//    @Captor
//    var paymentMethodNoncesCaptor: ArgumentCaptor<List<PaymentMethodNonce>>? = null
    private lateinit var activity: FragmentActivity
//    private var dropInSharedPreferences: DropInSharedPreferences? = null

    private val applicationContext: Context = ApplicationProvider.getApplicationContext()

    private lateinit var dropInRequest: DropInRequest
    private lateinit var braintreeClient: BraintreeClient

    companion object {

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            // required for mockk since AuthorizationCallback is package-private
            registerInstanceFactory { mockk<AuthorizationCallback>() }
        }
    }

    @Before
    fun beforeEach() {
//        MockitoAnnotations.initMocks(this)
//        dropInSharedPreferences = Mockito.mock(DropInSharedPreferences::class.java)
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java)
        activity = activityController.get()

        dropInRequest = DropInRequest()
        braintreeClient = mockk(relaxed = true)

        // This suppresses errors from WorkManager initialization within BraintreeClient
        // initialization (AnalyticsClient)
        WorkManagerTestInitHelper.initializeTestWorkManager(applicationContext)
    }

    @Test
    fun constructor_setsIntegrationTypeDropIn() {
        val sut = DropInClient(applicationContext, Fixtures.TOKENIZATION_KEY, dropInRequest)
        assertEquals(IntegrationType.DROP_IN, sut.braintreeClient.integrationType)
    }

    @Test
    fun publicConstructor_setsBraintreeClientWithSessionId() {
        val sut = DropInClient(applicationContext, Fixtures.TOKENIZATION_KEY, dropInRequest)
        assertNotNull(sut.braintreeClient.sessionId)
    }

    @Test
    fun internalConstructor_setsBraintreeClientWithSessionId() {
        val authorization = Fixtures.TOKENIZATION_KEY
        val sut =
            DropInClient(applicationContext, authorization, "session-id", dropInRequest)

        assertEquals("session-id", sut.braintreeClient.sessionId)
    }

    @Test
    fun configuration_forwardsInvocationToBraintreeClient() {
        justRun { braintreeClient.getConfiguration(any()) }

        val params = DropInClientParams()
            .braintreeClient(braintreeClient)
        val sut = DropInClient(params)

        val callback = mockk<ConfigurationCallback>(relaxed = true)
        sut.getConfiguration(callback)
        verify { braintreeClient.getConfiguration(callback) }
    }

    @Test
    fun authorization_forwardsInvocationToBraintreeClient() {
        val braintreeClient = MockkBraintreeClientBuilder()
            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
            .build()

        val params = DropInClientParams()
            .braintreeClient(braintreeClient)
        val sut = DropInClient(params)

        val callback = mockk<AuthorizationCallback>(relaxed = true)
        sut.getAuthorization(callback)
        verify { braintreeClient.getAuthorization(callback) }
    }

    @Test
    fun sendAnalyticsEvent_forwardsInvocationToBraintreeClient() {
        val braintreeClient = MockkBraintreeClientBuilder().build()
        val params = DropInClientParams()
            .braintreeClient(braintreeClient)
        val eventFragment = "event.fragment"
        val sut = DropInClient(params)
        sut.sendAnalyticsEvent(eventFragment)
        verify { braintreeClient.sendAnalyticsEvent(eventFragment) }
    }

    @Test
    fun collectDeviceData_forwardsInvocationToDataCollector() {
        val dataCollector = mockk<DataCollector>(relaxed = true)
        val params = DropInClientParams()
            .dataCollector(dataCollector)
        val callback = mockk<DataCollectorCallback>(relaxed = true)
        val sut = DropInClient(params)
        sut.collectDeviceData(activity, callback)
        verify { dataCollector.collectDeviceData(activity, callback) }
    }

    @Throws(JSONException::class)
    @Test
    fun supportedPaymentMethods_whenGooglePayEnabledInConfigAndIsReadyToPaySuccess_includesGooglePay() {
        val googlePayClient = MockkGooglePayClientBuilder()
            .isReadyToPaySuccess(true)
            .build()
        val braintreeClient = MockkBraintreeClientBuilder()
            .configurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
            .build()

        val params = DropInClientParams()
            .dropInRequest(DropInRequest())
            .braintreeClient(braintreeClient)
            .googlePayClient(googlePayClient)
        val sut = DropInClient(params)
        val callback = mockk<GetSupportedPaymentMethodsCallback>(relaxed = true)

        val paymentMethodsSlot = slot<List<DropInPaymentMethod>>()
        justRun { callback.onResult(capture(paymentMethodsSlot), any()) }

        sut.getSupportedPaymentMethods(activity, callback)

        val paymentMethods = paymentMethodsSlot.captured
        assertEquals(1, paymentMethods.size)
        assertEquals(DropInPaymentMethod.GOOGLE_PAY, paymentMethods[0])
    }

    @Throws(JSONException::class)
    @Test
    fun supportedPaymentMethods_whenGooglePayEnabledInConfigAndIsReadyToPayError_filtersGooglePayFromSupportedMethods() {
        val googlePayClient = MockkGooglePayClientBuilder()
            .isReadyToPayError(Exception("google pay error"))
            .build()

        val braintreeClient = MockkBraintreeClientBuilder()
            .configurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
            .build()

        val params = DropInClientParams()
            .dropInRequest(DropInRequest())
            .braintreeClient(braintreeClient)
            .googlePayClient(googlePayClient)
        val sut = DropInClient(params)
        val callback = mockk<GetSupportedPaymentMethodsCallback>(relaxed = true)

        sut.getSupportedPaymentMethods(activity, callback)

        val paymentMethodsSlot = slot<List<DropInPaymentMethod>>()
        justRun { callback.onResult(capture(paymentMethodsSlot), any()) }

        sut.getSupportedPaymentMethods(activity, callback)

        val paymentMethods = paymentMethodsSlot.captured
        assertEquals(0, paymentMethods.size)
    }

    @Throws(JSONException::class)
    @Test
    fun supportedPaymentMethods_whenGooglePayDisabledInDropInRequest_filtersGooglePayFromSupportedMethods() {
        val googlePayClient = MockkGooglePayClientBuilder().build()
        val braintreeClient = MockkBraintreeClientBuilder()
            .configurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
            .build()

        val dropInRequest = DropInRequest()
        dropInRequest.isGooglePayDisabled = true

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)
            .googlePayClient(googlePayClient)
        val sut = DropInClient(params)
        val callback = mockk<GetSupportedPaymentMethodsCallback>(relaxed = true)

        val paymentMethodsSlot = slot<List<DropInPaymentMethod>>()
        justRun { callback.onResult(capture(paymentMethodsSlot), any()) }

        sut.getSupportedPaymentMethods(activity, callback)

        val paymentMethods = paymentMethodsSlot.captured
        assertEquals(0, paymentMethods.size)
    }

    @Test
    fun supportedPaymentMethods_whenConfigurationFetchFails_forwardsError() {
        val configurationError = Exception("configuration error")
        val braintreeClient = MockBraintreeClientBuilder()
            .configurationError(configurationError)
            .build()

        val params = DropInClientParams()
            .braintreeClient(braintreeClient)
        val sut = DropInClient(params)
        val callback = mockk<GetSupportedPaymentMethodsCallback>(relaxed = true)

        sut.getSupportedPaymentMethods(activity, callback)
        verify { callback.onResult(null, configurationError) }
    }

    @Test
    @Throws(JSONException::class)
    fun shouldRequestThreeDSecureVerification_whenNonceIsGooglePayNonNetworkTokenized_returnsTrue() {
        val braintreeClient = MockkBraintreeClientBuilder()
            .configurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE))
            .build()

        val threeDSecureRequest = ThreeDSecureRequest()
        threeDSecureRequest.amount = "1.00"
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)
        val googlePayCardNonce =
            GooglePayCardNonce.fromJSON(JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE))

        val sut = DropInClient(params)
        val callback = mockk<ShouldRequestThreeDSecureVerification>(relaxed = true)

        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback)
        verify { callback.onResult(true) }
    }

    @Test
    @Throws(JSONException::class)
    fun shouldRequestThreeDSecureVerification_whenNonceIsGooglePayNetworkTokenized_returnsFalse() {
        val braintreeClient = MockkBraintreeClientBuilder()
            .configurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE))
            .build()

        val threeDSecureRequest = ThreeDSecureRequest()
        threeDSecureRequest.amount = "1.00"
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)
        val googlePayCardNonce =
            GooglePayCardNonce.fromJSON(JSONObject(Fixtures.GOOGLE_PAY_NETWORK_TOKENIZED_RESPONSE))

        val sut = DropInClient(params)
        val callback = mockk<ShouldRequestThreeDSecureVerification>(relaxed = true)
        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback)

        verify { callback.onResult(false) }
    }

    @Test
    @Throws(JSONException::class)
    fun shouldRequestThreeDSecureVerification_whenNonceIsCardNonce_returnsTrue() {
        val braintreeClient = MockkBraintreeClientBuilder()
            .configurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE))
            .build()

        val threeDSecureRequest = ThreeDSecureRequest()
        threeDSecureRequest.amount = "1.00"
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)
        val paymentMethodNonce: PaymentMethodNonce =
            CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))

        val sut = DropInClient(params)
        val callback = mockk<ShouldRequestThreeDSecureVerification>(relaxed = true)

        sut.shouldRequestThreeDSecureVerification(paymentMethodNonce, callback)
        verify { callback.onResult(true) }
    }

    @Test
    @Throws(JSONException::class)
    fun shouldRequestThreeDSecureVerification_whenNonceIsNotCardOrGooglePay_returnsFalse() {
        val braintreeClient = MockkBraintreeClientBuilder()
            .configurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE))
            .build()

        val threeDSecureRequest = ThreeDSecureRequest()
        threeDSecureRequest.amount = "1.00"
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)

        val paymentMethodNonce = mockk<PaymentMethodNonce>()
        val sut = DropInClient(params)
        val callback = mockk<ShouldRequestThreeDSecureVerification>(relaxed = true)

        sut.shouldRequestThreeDSecureVerification(paymentMethodNonce, callback)
        verify { callback.onResult(false) }
    }

    @Test
    @Throws(JSONException::class)
    fun shouldRequestThreeDSecureVerification_whenConfigurationFetchFails_returnsFalse() {
        val braintreeClient = MockBraintreeClientBuilder()
            .configurationError(Exception("configuration error"))
            .build()

        val threeDSecureRequest = ThreeDSecureRequest()
        threeDSecureRequest.amount = "1.00"
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .braintreeClient(braintreeClient)
        val googlePayCardNonce =
            GooglePayCardNonce.fromJSON(JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE))

        val sut = DropInClient(params)
        val callback = mockk<ShouldRequestThreeDSecureVerification>(relaxed = true)

        sut.shouldRequestThreeDSecureVerification(googlePayCardNonce, callback)
        verify { callback.onResult(false) }
    }

    @Test
    fun fetchMostRecentPaymentMethod_forwardsAuthorizationFetchErrors() {
        val authError = Exception("auth error")
        val braintreeClient = MockBraintreeClientBuilder()
            .authorizationError(authError)
            .build()

        val params = DropInClientParams()
            .braintreeClient(braintreeClient)
            .dropInRequest(DropInRequest())
        val sut = DropInClient(params)
        val callback = mockk<FetchMostRecentPaymentMethodCallback>(relaxed = true)

        sut.fetchMostRecentPaymentMethod(activity, callback)
        verify { callback.onResult(null, authError) }
    }

    @Test
    fun fetchMostRecentPaymentMethod_callsBackWithErrorIfInvalidClientTokenWasUsed() {
        val braintreeClient = MockBraintreeClientBuilder()
            .authorizationSuccess(Authorization.fromString(Fixtures.TOKENIZATION_KEY))
            .build()

        val params = DropInClientParams()
            .braintreeClient(braintreeClient)
            .dropInRequest(DropInRequest())
        val sut = DropInClient(params)
        val callback = mockk<FetchMostRecentPaymentMethodCallback>()

        val errorSlot = slot<InvalidArgumentException>()
        justRun { callback.onResult(null, capture(errorSlot)) }

        sut.fetchMostRecentPaymentMethod(activity, callback)

        val exception = errorSlot.captured
        assertEquals(
            "DropInClient#fetchMostRecentPaymentMethods() must be called with a client token",
            exception.message
        )
    }

    @Test
    @Throws(JSONException::class)
    fun performThreeDSecureVerification_performsVerificationAndSetsNonceOnThreeDSecureRequest() {
        val threeDSecureRequest = ThreeDSecureRequest()
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val threeDSecureClient = MockkThreeDSecureClientBuilder().build()
        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .threeDSecureClient(threeDSecureClient)

        val paymentMethodNonce: PaymentMethodNonce =
            CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))

        val callback = mockk<DropInResultCallback>(relaxed = true)
        val sut = DropInClient(params)
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback)

        verify { threeDSecureClient.performVerification(activity, threeDSecureRequest, any()) }
        assertEquals(paymentMethodNonce.string, threeDSecureRequest.nonce)
    }

    @Test
    @Throws(JSONException::class)
    fun performThreeDSecureVerification_whenVerificationFails_callbackError() {
        val threeDSecureRequest = ThreeDSecureRequest()
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val performVerificationError = Exception("verification error")
        val threeDSecureClient = MockkThreeDSecureClientBuilder()
            .performVerificationError(performVerificationError)
            .build()

        val params = DropInClientParams()
            .dropInRequest(dropInRequest)
            .threeDSecureClient(threeDSecureClient)
        val paymentMethodNonce: PaymentMethodNonce =
            CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))

        val sut = DropInClient(params)
        val callback = mockk<DropInResultCallback>(relaxed = true)

        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback)
        verify { callback.onResult(null, performVerificationError) }

        verify(inverse = true) {
            threeDSecureClient.continuePerformVerification(any(), any(), any(), any())
        }
    }

    @Test
    @Throws(JSONException::class)
    fun performThreeDSecureVerification_includesDeviceDataInResult() {
        val threeDSecureRequest = ThreeDSecureRequest()
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val performVerificationResult = ThreeDSecureResult()
        val continueVerificationResult = ThreeDSecureResult()
        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        continueVerificationResult.tokenizedCard = cardNonce

        val threeDSecureClient = MockkThreeDSecureClientBuilder()
            .performVerificationSuccess(performVerificationResult)
            .continueVerificationSuccess(continueVerificationResult)
            .build()
        val dataCollector = MockDataCollectorBuilder()
            .collectDeviceDataSuccess("device data")
            .build()

        val params = DropInClientParams()
            .dataCollector(dataCollector)
            .dropInRequest(dropInRequest)
            .threeDSecureClient(threeDSecureClient)
        val paymentMethodNonce: PaymentMethodNonce =
            CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))

        val callback = mockk<DropInResultCallback>(relaxed = true)
        val dropInResultSlot = slot<DropInResult>()
        justRun { callback.onResult(capture(dropInResultSlot), null) }

        val sut = DropInClient(params)
        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback)

        val dropInResult = dropInResultSlot.captured
        assertSame(cardNonce, dropInResult.paymentMethodNonce)
        assertEquals("device data", dropInResult.deviceData)
    }

    @Test
    @Throws(JSONException::class)
    fun performThreeDSecureVerification_whenDataCollectionFails_callsBackAnError() {
        val threeDSecureRequest = ThreeDSecureRequest()
        val dropInRequest = DropInRequest()
        dropInRequest.threeDSecureRequest = threeDSecureRequest

        val cardNonce = CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))
        val performVerificationResult = ThreeDSecureResult()
        val continueVerificationResult = ThreeDSecureResult()
        continueVerificationResult.setTokenizedCard(cardNonce)

        val threeDSecureClient = MockkThreeDSecureClientBuilder()
            .performVerificationSuccess(performVerificationResult)
            .continueVerificationSuccess(continueVerificationResult)
            .build()

        val dataCollectionError = Exception("data collection error")
        val dataCollector = MockDataCollectorBuilder()
            .collectDeviceDataError(dataCollectionError)
            .build()

        val params = DropInClientParams()
            .dataCollector(dataCollector)
            .dropInRequest(dropInRequest)
            .threeDSecureClient(threeDSecureClient)

        val paymentMethodNonce: PaymentMethodNonce =
            CardNonce.fromJSON(JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD))

        val callback = mockk<DropInResultCallback>(relaxed = true)
        val sut = DropInClient(params)

        sut.performThreeDSecureVerification(activity, paymentMethodNonce, callback)
        verify { callback.onResult(null, dataCollectionError) }
    }

//    @Test
//    @Throws(JSONException::class)
//    fun fetchMostRecentPaymentMethod_callsBackWithResultIfLastUsedPaymentMethodTypeWasPayWithGoogle() {
//        val googlePayClient = MockGooglePayClientBuilder()
//            .isReadyToPaySuccess(true)
//            .build()
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
//            .build()
//        val params = DropInClientParams()
//            .dropInRequest(DropInRequest())
//            .braintreeClient(braintreeClient)
//            .dropInSharedPreferences(dropInSharedPreferences)
//            .googlePayClient(googlePayClient)
//        Mockito.`when`(
//            dropInSharedPreferences!!.getLastUsedPaymentMethod(activity)
//        ).thenReturn(DropInPaymentMethod.GOOGLE_PAY)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            FetchMostRecentPaymentMethodCallback::class.java
//        )
//        sut.fetchMostRecentPaymentMethod(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val result = captor.value
//        Assert.assertEquals(DropInPaymentMethod.GOOGLE_PAY, result.paymentMethodType)
//        Assert.assertNull(result.paymentMethodNonce)
//    }
//
//    @Test
//    @Throws(JSONException::class)
//    fun fetchMostRecentPaymentMethod_doesNotCallBackWithPayWithGoogleIfPayWithGoogleIsNotAvailable() {
//        val googlePayClient = MockGooglePayClientBuilder()
//            .isReadyToPaySuccess(false)
//            .build()
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
//            .build()
//        val paymentMethods = ArrayList<PaymentMethodNonce>()
//        paymentMethods.add(CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)))
//        paymentMethods.add(GooglePayCardNonce.fromJSON(JSONObject(Fixtures.GOOGLE_PAY_NETWORK_TOKENIZED_RESPONSE)))
//        val paymentMethodClient = MockPaymentMethodClientBuilder()
//            .getPaymentMethodNoncesSuccess(paymentMethods)
//            .build()
//        val params = DropInClientParams()
//            .dropInRequest(DropInRequest())
//            .braintreeClient(braintreeClient)
//            .paymentMethodClient(paymentMethodClient)
//            .googlePayClient(googlePayClient)
//            .dropInSharedPreferences(dropInSharedPreferences)
//        Mockito.`when`(
//            dropInSharedPreferences!!.getLastUsedPaymentMethod(activity)
//        ).thenReturn(DropInPaymentMethod.GOOGLE_PAY)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            FetchMostRecentPaymentMethodCallback::class.java
//        )
//        sut.fetchMostRecentPaymentMethod(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val result = captor.value
//        Assert.assertEquals(DropInPaymentMethod.VISA, result.paymentMethodType)
//        TestCase.assertNotNull(result.paymentMethodNonce)
//        Assert.assertEquals("11", (result.paymentMethodNonce as CardNonce?)!!.lastTwo)
//    }
//
//    @Test
//    fun fetchMostRecentPaymentMethod_callsBackWithErrorOnGetPaymentMethodsError() {
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .build()
//        val paymentMethodClient = MockPaymentMethodClientBuilder()
//            .getPaymentMethodNoncesError(BraintreeException("Error occurred"))
//            .build()
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .paymentMethodClient(paymentMethodClient)
//            .dropInSharedPreferences(dropInSharedPreferences)
//            .dropInRequest(DropInRequest())
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            FetchMostRecentPaymentMethodCallback::class.java
//        )
//        sut.fetchMostRecentPaymentMethod(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            BraintreeException::class.java
//        )
//        Mockito.verify(callback).onResult(Matchers.isNull() as DropInResult, captor.capture())
//        val exception = captor.value
//        Assert.assertEquals("Error occurred", exception.message)
//    }
//
//    @Test
//    @Throws(JSONException::class)
//    fun fetchMostRecentPaymentMethod_callsBackWithResultWhenThereIsAPaymentMethod() {
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
//            .build()
//        val paymentMethods = ArrayList<PaymentMethodNonce>()
//        paymentMethods.add(CardNonce.fromJSON(JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)))
//        val paymentMethodClient = MockPaymentMethodClientBuilder()
//            .getPaymentMethodNoncesSuccess(paymentMethods)
//            .build()
//        val params = DropInClientParams()
//            .dropInRequest(DropInRequest())
//            .braintreeClient(braintreeClient)
//            .dropInSharedPreferences(dropInSharedPreferences)
//            .paymentMethodClient(paymentMethodClient)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            FetchMostRecentPaymentMethodCallback::class.java
//        )
//        sut.fetchMostRecentPaymentMethod(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val result = captor.value
//        Assert.assertEquals(DropInPaymentMethod.VISA, result.paymentMethodType)
//        TestCase.assertNotNull(result.paymentMethodNonce)
//        Assert.assertEquals("11", (result.paymentMethodNonce as CardNonce?)!!.lastTwo)
//    }
//
//    @Test
//    @Throws(JSONException::class)
//    fun fetchMostRecentPaymentMethod_callsBackWithNullResultWhenThereAreNoPaymentMethods() {
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY))
//            .build()
//        val paymentMethods = ArrayList<PaymentMethodNonce>()
//        val paymentMethodClient = MockPaymentMethodClientBuilder()
//            .getPaymentMethodNoncesSuccess(paymentMethods)
//            .build()
//        val params = DropInClientParams()
//            .dropInRequest(DropInRequest())
//            .braintreeClient(braintreeClient)
//            .dropInSharedPreferences(dropInSharedPreferences)
//            .paymentMethodClient(paymentMethodClient)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            FetchMostRecentPaymentMethodCallback::class.java
//        )
//        sut.fetchMostRecentPaymentMethod(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val result = captor.value
//        Assert.assertNull(result.paymentMethodType)
//        Assert.assertNull(result.paymentMethodNonce)
//    }
//
//    @get:Test
//    val supportedPaymentMethods_whenNoPaymentMethodsEnabledInConfiguration_callsBackWithNoPaymentMethods: Unit
//        get() {
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(mockConfiguration(false, false, false, false, false))
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(false)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(DropInRequest())
//                .googlePayClient(googlePayClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(0, paymentMethodTypes.size.toLong())
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenPaymentMethodsEnabledInConfiguration_callsBackWithPaymentMethods: Unit
//        get() {
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(mockConfiguration(true, true, true, true, false))
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(true)
//                .build()
//            val venmoClient = MockVenmoClientBuilder()
//                .isVenmoAppInstalled(true)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(DropInRequest())
//                .googlePayClient(googlePayClient)
//                .venmoClient(venmoClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(4, paymentMethodTypes.size.toLong())
//            Assert.assertEquals(DropInPaymentMethod.PAYPAL, paymentMethodTypes[0])
//            Assert.assertEquals(DropInPaymentMethod.VENMO, paymentMethodTypes[1])
//            Assert.assertEquals(DropInPaymentMethod.UNKNOWN, paymentMethodTypes[2])
//            Assert.assertEquals(DropInPaymentMethod.GOOGLE_PAY, paymentMethodTypes[3])
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenUnionPayNotSupportedAndOtherCardsPresent_callsBackWithOtherCards: Unit
//        get() {
//            val configuration = mockConfiguration(false, false, true, false, false)
//            Mockito.`when`(configuration.supportedCardTypes)
//                .thenReturn(
//                    Arrays.asList(
//                        DropInPaymentMethod.UNIONPAY.name,
//                        DropInPaymentMethod.VISA.name
//                    )
//                )
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(configuration)
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(false)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(DropInRequest())
//                .googlePayClient(googlePayClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(1, paymentMethodTypes.size.toLong())
//            Assert.assertEquals(DropInPaymentMethod.UNKNOWN, paymentMethodTypes[0])
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenOnlyUnionPayPresentAndNotSupported_callsBackWithNoCards: Unit
//        get() {
//            val configuration = mockConfiguration(false, false, true, false, false)
//            Mockito.`when`(configuration.supportedCardTypes)
//                .thenReturn(listOf("UnionPay"))
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(configuration)
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(false)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(DropInRequest())
//                .googlePayClient(googlePayClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(0, paymentMethodTypes.size.toLong())
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenOnlyUnionPayPresentAndSupported_callsBackWithCards: Unit
//        get() {
//            val configuration = mockConfiguration(false, false, true, false, true)
//            Mockito.`when`(configuration.supportedCardTypes)
//                .thenReturn(listOf(DropInPaymentMethod.UNIONPAY.name))
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(configuration)
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(false)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(DropInRequest())
//                .googlePayClient(googlePayClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(1, paymentMethodTypes.size.toLong())
//            Assert.assertEquals(DropInPaymentMethod.UNKNOWN, paymentMethodTypes[0])
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenCardsDisabledInDropInRequest_doesNotReturnCards: Unit
//        get() {
//            val configuration = mockConfiguration(false, false, true, false, false)
//            val dropInRequest = DropInRequest()
//            dropInRequest.isCardDisabled = true
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(configuration)
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(false)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(dropInRequest)
//                .googlePayClient(googlePayClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(0, paymentMethodTypes.size.toLong())
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenPayPalDisabledInDropInRequest_doesNotReturnPayPal: Unit
//        get() {
//            val configuration = mockConfiguration(true, false, false, false, false)
//            val dropInRequest = DropInRequest()
//            dropInRequest.isPayPalDisabled = true
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(configuration)
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(false)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(dropInRequest)
//                .googlePayClient(googlePayClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(0, paymentMethodTypes.size.toLong())
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenVenmoDisabledInDropInRequest_doesNotReturnVenmo: Unit
//        get() {
//            val configuration = mockConfiguration(false, true, false, false, false)
//            val dropInRequest = DropInRequest()
//            dropInRequest.isVenmoDisabled = true
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(configuration)
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(false)
//                .build()
//            val venmoClient = MockVenmoClientBuilder()
//                .isVenmoAppInstalled(true)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(dropInRequest)
//                .googlePayClient(googlePayClient)
//                .venmoClient(venmoClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(0, paymentMethodTypes.size.toLong())
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenVenmoNotInstalled_doesNotReturnVenmo: Unit
//        get() {
//            val configuration = mockConfiguration(false, true, false, false, false)
//            val dropInRequest = DropInRequest()
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(configuration)
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(false)
//                .build()
//            val venmoClient = MockVenmoClientBuilder()
//                .isVenmoAppInstalled(false)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(dropInRequest)
//                .googlePayClient(googlePayClient)
//                .venmoClient(venmoClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(0, paymentMethodTypes.size.toLong())
//        }
//
//    @get:Test
//    val supportedPaymentMethods_whenGooglePayDisabledInDropInRequest_doesNotReturnGooglePay: Unit
//        get() {
//            val configuration = mockConfiguration(false, false, false, true, false)
//            val dropInRequest = DropInRequest()
//            dropInRequest.isGooglePayDisabled = true
//            val braintreeClient = MockBraintreeClientBuilder()
//                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//                .configuration(configuration)
//                .build()
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(true)
//                .build()
//            val params = DropInClientParams()
//                .dropInRequest(dropInRequest)
//                .googlePayClient(googlePayClient)
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            val callback = Mockito.mock(
//                GetSupportedPaymentMethodsCallback::class.java
//            )
//            sut.getSupportedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(
//                paymentMethodTypesCaptor!!.capture(), Matchers.isNull() as Exception
//            )
//            val paymentMethodTypes = paymentMethodTypesCaptor!!.value
//            Assert.assertEquals(0, paymentMethodTypes.size.toLong())
//        }
//
//    @Test
//    fun tokenizePayPalAccount_withoutPayPalRequest_tokenizesPayPalWithVaultRequest() {
//        val configuration = mockConfiguration(true, false, false, false, false)
//        val dropInRequest = DropInRequest()
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(configuration)
//            .build()
//        val payPalClient = Mockito.mock(PayPalClient::class.java)
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .payPalClient(payPalClient)
//            .braintreeClient(braintreeClient)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            PayPalFlowStartedCallback::class.java
//        )
//        sut.tokenizePayPalRequest(activity, callback)
//        Mockito.verify(payPalClient).tokenizePayPalAccount(
//            Matchers.same(activity), Matchers.any(
//                PayPalVaultRequest::class.java
//            ), Matchers.same(callback)
//        )
//    }
//
//    @Test
//    fun tokenizePayPalAccount_withPayPalCheckoutRequest_tokenizesPayPalWithCheckoutRequest() {
//        val configuration = mockConfiguration(true, false, false, false, false)
//        val payPalRequest = PayPalCheckoutRequest("1.00")
//        val dropInRequest = DropInRequest()
//        dropInRequest.payPalRequest = payPalRequest
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(configuration)
//            .build()
//        val payPalClient = Mockito.mock(PayPalClient::class.java)
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .payPalClient(payPalClient)
//            .braintreeClient(braintreeClient)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            PayPalFlowStartedCallback::class.java
//        )
//        sut.tokenizePayPalRequest(activity, callback)
//        Mockito.verify(payPalClient).tokenizePayPalAccount(
//            Matchers.same(activity),
//            Matchers.same(payPalRequest),
//            Matchers.same(callback)
//        )
//    }
//
//    @Test
//    fun tokenizePayPalAccount_withPayPalVaultRequest_tokenizesPayPalWithVaultRequest() {
//        val configuration = mockConfiguration(true, false, false, false, false)
//        val payPalRequest = PayPalVaultRequest()
//        val dropInRequest = DropInRequest()
//        dropInRequest.payPalRequest = payPalRequest
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(configuration)
//            .build()
//        val payPalClient = Mockito.mock(PayPalClient::class.java)
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .payPalClient(payPalClient)
//            .braintreeClient(braintreeClient)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            PayPalFlowStartedCallback::class.java
//        )
//        sut.tokenizePayPalRequest(activity, callback)
//        Mockito.verify(payPalClient).tokenizePayPalAccount(
//            Matchers.same(activity),
//            Matchers.same(payPalRequest),
//            Matchers.same(callback)
//        )
//    }
//
//    @Test
//    fun tokenizeVenmoAccount_tokenizesVenmo() {
//        val configuration = mockConfiguration(false, true, false, false, false)
//        val venmoRequest = VenmoRequest(VenmoPaymentMethodUsage.SINGLE_USE)
//        venmoRequest.shouldVault = true
//        val dropInRequest = DropInRequest()
//        dropInRequest.venmoRequest = venmoRequest
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(configuration)
//            .build()
//        val venmoClient = Mockito.mock(VenmoClient::class.java)
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .venmoClient(venmoClient)
//            .braintreeClient(braintreeClient)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            VenmoTokenizeAccountCallback::class.java
//        )
//        sut.tokenizeVenmoAccount(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            VenmoRequest::class.java
//        )
//        Mockito.verify(venmoClient).tokenizeVenmoAccount(
//            Matchers.same(activity),
//            captor.capture(),
//            Matchers.same(callback)
//        )
//        val request = captor.value
//        Assert.assertTrue(request.shouldVault)
//    }
//
//    @Test
//    fun tokenizeVenmoAccount_whenVenmoRequestNull_createsVenmoRequest() {
//        val configuration = mockConfiguration(false, true, false, false, false)
//        val dropInRequest = DropInRequest()
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(configuration)
//            .build()
//        val venmoClient = Mockito.mock(VenmoClient::class.java)
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .venmoClient(venmoClient)
//            .braintreeClient(braintreeClient)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            VenmoTokenizeAccountCallback::class.java
//        )
//        sut.tokenizeVenmoAccount(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            VenmoRequest::class.java
//        )
//        Mockito.verify(venmoClient).tokenizeVenmoAccount(
//            Matchers.same(activity),
//            captor.capture(),
//            Matchers.same(callback)
//        )
//        val request = captor.value
//        Assert.assertEquals(
//            VenmoPaymentMethodUsage.SINGLE_USE.toLong(),
//            request.paymentMethodUsage.toLong()
//        )
//        TestCase.assertFalse(request.shouldVault)
//    }
//
//    @Test
//    fun requestGooglePayPayment_requestsGooglePay() {
//        val configuration = mockConfiguration(false, false, false, true, false)
//        val googlePayRequest = GooglePayRequest()
//        val dropInRequest = DropInRequest()
//        dropInRequest.googlePayRequest = googlePayRequest
//        val braintreeClient = MockBraintreeClientBuilder()
//            .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
//            .configuration(configuration)
//            .build()
//        val googlePayClient = MockGooglePayClientBuilder()
//            .isReadyToPaySuccess(true)
//            .build()
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .googlePayClient(googlePayClient)
//            .braintreeClient(braintreeClient)
//        val sut = DropInClient(params)
//        val callback = Mockito.mock(
//            GooglePayRequestPaymentCallback::class.java
//        )
//        sut.requestGooglePayPayment(activity, callback)
//        Mockito.verify(googlePayClient).requestPayment(
//            Matchers.same(activity),
//            Matchers.same(googlePayRequest),
//            Matchers.same(callback)
//        )
//    }
//
//    @Test
//    fun deletePaymentMethod_callsDeletePaymentMethod() {
//        val paymentMethodClient = Mockito.mock(
//            PaymentMethodClient::class.java
//        )
//        val params = DropInClientParams()
//            .paymentMethodClient(paymentMethodClient)
//        val sut = DropInClient(params)
//        val cardNonce = Mockito.mock(CardNonce::class.java)
//        val callback = Mockito.mock(
//            DeletePaymentMethodNonceCallback::class.java
//        )
//        sut.deletePaymentMethod(activity, cardNonce, callback)
//        Mockito.verify(paymentMethodClient).deletePaymentMethod(
//            Matchers.same(activity),
//            Matchers.same(cardNonce),
//            Matchers.same(callback)
//        )
//    }
//
//    @Test
//    fun tokenizeCard_forwardsInvocationToCardClient() {
//        val cardClient = Mockito.mock(CardClient::class.java)
//        val params = DropInClientParams()
//            .cardClient(cardClient)
//        val card = Card()
//        val callback = Mockito.mock(
//            CardTokenizeCallback::class.java
//        )
//        val sut = DropInClient(params)
//        sut.tokenizeCard(card, callback)
//        Mockito.verify(cardClient).tokenize(card, callback)
//    }
//
//    @Test
//    fun fetchUnionPayCapabilities_forwardsInvocationToUnionPayClient() {
//        val unionPayClient = Mockito.mock(UnionPayClient::class.java)
//        val params = DropInClientParams()
//            .unionPayClient(unionPayClient)
//        val cardNumber = "4111111111111111"
//        val callback = Mockito.mock(
//            UnionPayFetchCapabilitiesCallback::class.java
//        )
//        val sut = DropInClient(params)
//        sut.fetchUnionPayCapabilities(cardNumber, callback)
//        Mockito.verify(unionPayClient).fetchCapabilities(cardNumber, callback)
//    }
//
//    @Test
//    fun enrollUnionPay_forwardsInvocationToUnionPayClient() {
//        val unionPayClient = Mockito.mock(UnionPayClient::class.java)
//        val params = DropInClientParams()
//            .unionPayClient(unionPayClient)
//        val unionPayCard = UnionPayCard()
//        val callback = Mockito.mock(
//            UnionPayEnrollCallback::class.java
//        )
//        val sut = DropInClient(params)
//        sut.enrollUnionPay(unionPayCard, callback)
//    }
//
//    @Test
//    fun tokenizeUnionPay_forwardsInvocationToUnionPayClient() {
//        val unionPayClient = Mockito.mock(UnionPayClient::class.java)
//        val params = DropInClientParams()
//            .unionPayClient(unionPayClient)
//        val unionPayCard = UnionPayCard()
//        val callback = Mockito.mock(
//            UnionPayTokenizeCallback::class.java
//        )
//        val sut = DropInClient(params)
//        sut.tokenizeUnionPay(unionPayCard, callback)
//    }
//
//    @get:Test
//    val browserSwitchResult_forwardsInvocationToBraintreeClient: Unit
//        get() {
//            val browserSwitchResult = createSuccessfulBrowserSwitchResult()
//            val braintreeClient = MockBraintreeClientBuilder().build()
//            Mockito.`when`(braintreeClient.getBrowserSwitchResult(activity!!))
//                .thenReturn(browserSwitchResult)
//            val params = DropInClientParams()
//                .braintreeClient(braintreeClient)
//            val sut = DropInClient(params)
//            Assert.assertSame(browserSwitchResult, sut.getBrowserSwitchResult(activity))
//        }
//
//    @Test
//    fun deliverBrowserSwitchResult_whenPayPal_tokenizesResultAndCollectsData() {
//        val payPalAccountNonce = Mockito.mock(
//            PayPalAccountNonce::class.java
//        )
//        val payPalClient = MockPayPalClientBuilder()
//            .browserSwitchResult(payPalAccountNonce)
//            .build()
//        val dropInRequest = DropInRequest()
//        val dataCollector = MockDataCollectorBuilder()
//            .collectDeviceDataSuccess("device data")
//            .build()
//        val braintreeClient = MockBraintreeClientBuilder().build()
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .dropInRequest(dropInRequest)
//            .dataCollector(dataCollector)
//            .payPalClient(payPalClient)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        val browserSwitchResult = Mockito.mock(
//            BrowserSwitchResult::class.java
//        )
//        Mockito.`when`(browserSwitchResult.requestCode).thenReturn(BraintreeRequestCodes.PAYPAL)
//        Mockito.`when`(braintreeClient.deliverBrowserSwitchResult(activity!!))
//            .thenReturn(browserSwitchResult)
//        val sut = DropInClient(params)
//        sut.deliverBrowserSwitchResult(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val dropInResult = captor.value
//        Assert.assertSame(dropInResult.paymentMethodNonce, payPalAccountNonce)
//        Assert.assertEquals("device data", dropInResult.deviceData)
//    }
//
//    @Test
//    fun deliverBrowserSwitchResult_whenPayPalTokenizationFails_callbackError() {
//        val browserSwitchError = Exception("paypal tokenization error")
//        val payPalClient = MockPayPalClientBuilder()
//            .browserSwitchError(browserSwitchError)
//            .build()
//        val dropInRequest = DropInRequest()
//        val dataCollector = Mockito.mock(
//            DataCollector::class.java
//        )
//        val braintreeClient = MockBraintreeClientBuilder().build()
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .dropInRequest(dropInRequest)
//            .dataCollector(dataCollector)
//            .payPalClient(payPalClient)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        val browserSwitchResult = Mockito.mock(
//            BrowserSwitchResult::class.java
//        )
//        Mockito.`when`(browserSwitchResult.requestCode).thenReturn(BraintreeRequestCodes.PAYPAL)
//        Mockito.`when`(braintreeClient.deliverBrowserSwitchResult(activity!!))
//            .thenReturn(browserSwitchResult)
//        val sut = DropInClient(params)
//        sut.deliverBrowserSwitchResult(activity, callback)
//        Mockito.verify(callback).onResult(null, browserSwitchError)
//    }
//
//    @Test
//    @Throws(JSONException::class)
//    fun deliverBrowserSwitchResult_whenThreeDSecure_tokenizesResultAndCollectsData() {
//        val cardNonce = CardNonce.fromJSON(
//            JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD)
//        )
//        val threeDSecureResult = ThreeDSecureResult()
//        threeDSecureResult.setTokenizedCard(cardNonce)
//        val threeDSecureClient = MockThreeDSecureClientBuilder()
//            .browserSwitchResult(threeDSecureResult)
//            .build()
//        val dropInRequest = DropInRequest()
//        val dataCollector = MockDataCollectorBuilder()
//            .collectDeviceDataSuccess("device data")
//            .build()
//        val braintreeClient = MockBraintreeClientBuilder().build()
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .dropInRequest(dropInRequest)
//            .dataCollector(dataCollector)
//            .threeDSecureClient(threeDSecureClient)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        val browserSwitchResult = Mockito.mock(
//            BrowserSwitchResult::class.java
//        )
//        Mockito.`when`(browserSwitchResult.requestCode)
//            .thenReturn(BraintreeRequestCodes.THREE_D_SECURE)
//        Mockito.`when`(braintreeClient.deliverBrowserSwitchResult(activity!!))
//            .thenReturn(browserSwitchResult)
//        val sut = DropInClient(params)
//        sut.deliverBrowserSwitchResult(activity, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val dropInResult = captor.value
//        Assert.assertSame(dropInResult.paymentMethodNonce, cardNonce)
//        Assert.assertEquals("device data", dropInResult.deviceData)
//    }
//
//    @Test
//    fun deliverBrowserSwitchResult_whenThreeDSecureTokenizationFails_callbackError() {
//        val browserSwitchError = Exception("threedsecure tokenization error")
//        val threeDSecureClient = MockThreeDSecureClientBuilder()
//            .browserSwitchError(browserSwitchError)
//            .build()
//        val dropInRequest = DropInRequest()
//        val dataCollector = Mockito.mock(
//            DataCollector::class.java
//        )
//        val braintreeClient = MockBraintreeClientBuilder().build()
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .dropInRequest(dropInRequest)
//            .dataCollector(dataCollector)
//            .threeDSecureClient(threeDSecureClient)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        val browserSwitchResult = Mockito.mock(
//            BrowserSwitchResult::class.java
//        )
//        Mockito.`when`(browserSwitchResult.requestCode)
//            .thenReturn(BraintreeRequestCodes.THREE_D_SECURE)
//        Mockito.`when`(braintreeClient.deliverBrowserSwitchResult(activity!!))
//            .thenReturn(browserSwitchResult)
//        val sut = DropInClient(params)
//        sut.deliverBrowserSwitchResult(activity, callback)
//        Mockito.verify(callback).onResult(null, browserSwitchError)
//    }
//
//    @Test
//    @Throws(JSONException::class)
//    fun handleThreeDSecureResult_tokenizesResultAndCollectsData() {
//        val cardNonce = CardNonce.fromJSON(
//            JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD)
//        )
//        val threeDSecureResult = ThreeDSecureResult()
//        threeDSecureResult.setTokenizedCard(cardNonce)
//        val threeDSecureClient = MockThreeDSecureClientBuilder()
//            .activityResult(threeDSecureResult)
//            .build()
//        val dropInRequest = DropInRequest()
//        val dataCollector = MockDataCollectorBuilder()
//            .collectDeviceDataSuccess("device data")
//            .build()
//        val braintreeClient = MockBraintreeClientBuilder().build()
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .dropInRequest(dropInRequest)
//            .dataCollector(dataCollector)
//            .threeDSecureClient(threeDSecureClient)
//        val intent = Intent()
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        val sut = DropInClient(params)
//        sut.handleThreeDSecureActivityResult(activity, 123, intent, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val dropInResult = captor.value
//        Assert.assertSame(dropInResult.paymentMethodNonce, cardNonce)
//        Assert.assertEquals("device data", dropInResult.deviceData)
//    }
//
//    @Test
//    fun handleThreeDSecureResult_whenTokenizationFails_callbackError() {
//        val activityResultError = Exception("activity result error")
//        val threeDSecureClient = MockThreeDSecureClientBuilder()
//            .activityResultError(activityResultError)
//            .build()
//        val dropInRequest = DropInRequest()
//        val dataCollector = Mockito.mock(
//            DataCollector::class.java
//        )
//        val braintreeClient = MockBraintreeClientBuilder().build()
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .dropInRequest(dropInRequest)
//            .dataCollector(dataCollector)
//            .threeDSecureClient(threeDSecureClient)
//        val intent = Intent()
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        val sut = DropInClient(params)
//        sut.handleThreeDSecureActivityResult(activity, 123, intent, callback)
//        Mockito.verify(callback).onResult(null, activityResultError)
//    }
//
//    @Test
//    fun launchDropInForResult_forwardsAuthorizationFetchErrors() {
//        val authError = Exception("auth error")
//        val braintreeClient = MockBraintreeClientBuilder()
//            .sessionId("session-id")
//            .authorizationError(authError)
//            .build()
//        val dropInRequest = DropInRequest()
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .dropInRequest(dropInRequest)
//        val sut = DropInClient(params)
//        val listener = Mockito.mock(DropInListener::class.java)
//        sut.setListener(listener)
//        val activity = Mockito.mock(
//            FragmentActivity::class.java
//        )
//        sut.launchDropInForResult(activity, 123)
//        Mockito.verify(listener).onDropInFailure(authError)
//    }
//
//    @Test
//    fun launchDropInForResult_launchesDropInActivityWithIntentExtras() {
//        val authorization = Mockito.mock(Authorization::class.java)
//        Mockito.`when`(authorization.toString()).thenReturn("authorization")
//        val braintreeClient = MockBraintreeClientBuilder()
//            .sessionId("session-id")
//            .authorizationSuccess(authorization)
//            .build()
//        val dropInRequest = DropInRequest()
//        dropInRequest.isVaultManagerEnabled = true
//        val params = DropInClientParams()
//            .braintreeClient(braintreeClient)
//            .dropInRequest(dropInRequest)
//        val sut = DropInClient(params)
//        val activity = Mockito.mock(
//            FragmentActivity::class.java
//        )
//        sut.launchDropInForResult(activity, 123)
//        val captor = ArgumentCaptor.forClass(Intent::class.java)
//        Mockito.verify(activity).startActivityForResult(captor.capture(), Matchers.eq(123))
//        val intent = captor.value
//        Assert.assertEquals("session-id", intent.getStringExtra(DropInClient.EXTRA_SESSION_ID))
//        Assert.assertEquals(
//            "authorization",
//            intent.getStringExtra(DropInClient.EXTRA_AUTHORIZATION)
//        )
//        val bundle = intent.getParcelableExtra<Bundle>(DropInClient.EXTRA_CHECKOUT_REQUEST_BUNDLE)
//        bundle!!.classLoader = DropInRequest::class.java.classLoader
//        val dropInRequestExtra: DropInRequest =
//            bundle.getParcelable(DropInClient.EXTRA_CHECKOUT_REQUEST)
//        Assert.assertTrue(dropInRequestExtra.isVaultManagerEnabled)
//    }
//
//    @get:Test
//    val vaultedPaymentMethods_forwardsConfigurationFetchError: Unit
//        get() {
//            val configurationError = Exception("configuration error")
//            val braintreeClient = MockBraintreeClientBuilder()
//                .configurationError(configurationError)
//                .build()
//            val params = DropInClientParams()
//                .braintreeClient(braintreeClient)
//            val callback = Mockito.mock(
//                GetPaymentMethodNoncesCallback::class.java
//            )
//            val sut = DropInClient(params)
//            sut.getVaultedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(null, configurationError)
//        }
//
//    @get:Test
//    val vaultedPaymentMethods_forwardsPaymentMethodClientError: Unit
//        get() {
//            val braintreeClient = MockBraintreeClientBuilder()
//                .configuration(mockConfiguration(true, true, true, true, true))
//                .build()
//            val paymentMethodClientError = Exception("payment method client error")
//            val paymentMethodClient = MockPaymentMethodClientBuilder()
//                .getPaymentMethodNoncesError(paymentMethodClientError)
//                .build()
//            val params = DropInClientParams()
//                .paymentMethodClient(paymentMethodClient)
//                .braintreeClient(braintreeClient)
//            val callback = Mockito.mock(
//                GetPaymentMethodNoncesCallback::class.java
//            )
//            val sut = DropInClient(params)
//            sut.getVaultedPaymentMethods(activity, callback)
//            Mockito.verify(callback).onResult(null, paymentMethodClientError)
//        }
//
//    @get:Throws(JSONException::class)
//    @get:Test
//    val vaultedPaymentMethods_whenGooglePayDisabled_callbackPaymentMethodClientResult: Unit
//        get() {
//            val braintreeClient = MockBraintreeClientBuilder()
//                .configuration(mockConfiguration(true, true, true, true, true))
//                .build()
//            val cardNonce: PaymentMethodNonce = CardNonce.fromJSON(
//                JSONObject(Fixtures.PAYMENT_METHODS_VISA_CREDIT_CARD)
//            )
//            val paymentMethodClient = MockPaymentMethodClientBuilder()
//                .getPaymentMethodNoncesSuccess(listOf(cardNonce))
//                .build()
//            val dropInRequest = DropInRequest()
//            dropInRequest.isGooglePayDisabled = true
//            val params = DropInClientParams()
//                .dropInRequest(dropInRequest)
//                .paymentMethodClient(paymentMethodClient)
//                .braintreeClient(braintreeClient)
//            val callback = Mockito.mock(
//                GetPaymentMethodNoncesCallback::class.java
//            )
//            val sut = DropInClient(params)
//            sut.getVaultedPaymentMethods(activity, callback)
//            Mockito.verify(callback)
//                .onResult(paymentMethodNoncesCaptor!!.capture(), Matchers.isNull() as Exception)
//            val paymentMethodNonces = paymentMethodNoncesCaptor!!.value
//            Assert.assertEquals(1, paymentMethodNonces.size.toLong())
//            Assert.assertSame(cardNonce, paymentMethodNonces[0])
//        }
//
//    @get:Throws(JSONException::class)
//    @get:Test
//    val vaultedPaymentMethods_whenGooglePayReadyToPay_callbackPaymentMethodClientResultWithGooglePayNonce: Unit
//        get() {
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPaySuccess(true)
//                .build()
//            val braintreeClient = MockBraintreeClientBuilder()
//                .configuration(mockConfiguration(true, true, true, true, true))
//                .build()
//            val googlePayCardNonce = GooglePayCardNonce.fromJSON(
//                JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE)
//            )
//            val paymentMethodClient = MockPaymentMethodClientBuilder()
//                .getPaymentMethodNoncesSuccess(listOf(googlePayCardNonce))
//                .build()
//            val dropInRequest = DropInRequest()
//            val params = DropInClientParams()
//                .dropInRequest(dropInRequest)
//                .googlePayClient(googlePayClient)
//                .paymentMethodClient(paymentMethodClient)
//                .braintreeClient(braintreeClient)
//            val callback = Mockito.mock(
//                GetPaymentMethodNoncesCallback::class.java
//            )
//            val sut = DropInClient(params)
//            sut.getVaultedPaymentMethods(activity, callback)
//            Mockito.verify(callback)
//                .onResult(paymentMethodNoncesCaptor!!.capture(), Matchers.isNull() as Exception)
//            val paymentMethodNonces = paymentMethodNoncesCaptor!!.value
//            Assert.assertEquals(1, paymentMethodNonces.size.toLong())
//            Assert.assertSame(googlePayCardNonce, paymentMethodNonces[0])
//        }
//
//    @get:Throws(JSONException::class)
//    @get:Test
//    val vaultedPaymentMethods_whenGooglePayClientErrors_callbackPaymentMethodClientResultWithoutGooglePayNonce: Unit
//        get() {
//            val googlePayClient = MockGooglePayClientBuilder()
//                .isReadyToPayError(Exception("google pay client error"))
//                .build()
//            val braintreeClient = MockBraintreeClientBuilder()
//                .configuration(mockConfiguration(true, true, true, true, true))
//                .build()
//            val googlePayCardNonce = GooglePayCardNonce.fromJSON(
//                JSONObject(Fixtures.GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE)
//            )
//            val paymentMethodClient = MockPaymentMethodClientBuilder()
//                .getPaymentMethodNoncesSuccess(listOf(googlePayCardNonce))
//                .build()
//            val dropInRequest = DropInRequest()
//            val params = DropInClientParams()
//                .dropInRequest(dropInRequest)
//                .googlePayClient(googlePayClient)
//                .paymentMethodClient(paymentMethodClient)
//                .braintreeClient(braintreeClient)
//            val callback = Mockito.mock(
//                GetPaymentMethodNoncesCallback::class.java
//            )
//            val sut = DropInClient(params)
//            sut.getVaultedPaymentMethods(activity, callback)
//            Mockito.verify(callback)
//                .onResult(paymentMethodNoncesCaptor!!.capture(), Matchers.isNull() as Exception)
//            val paymentMethodNonces = paymentMethodNoncesCaptor!!.value
//            Assert.assertEquals(0, paymentMethodNonces.size.toLong())
//        }
//
//    @Test
//    fun onActivityResult_whenResultCodeVenmo_handlesVenmoResult() {
//        val venmoClient = Mockito.mock(VenmoClient::class.java)
//        val dropInRequest = DropInRequest()
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .venmoClient(venmoClient)
//        val sut = DropInClient(params)
//        val activity = Mockito.mock(
//            FragmentActivity::class.java
//        )
//        val intent = Mockito.mock(Intent::class.java)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        sut.handleActivityResult(activity, BraintreeRequestCodes.VENMO, 1, intent, callback)
//        Mockito.verify(venmoClient).onActivityResult(
//            Matchers.same(activity), Matchers.eq(1), Matchers.same(intent), Matchers.any(
//                VenmoOnActivityResultCallback::class.java
//            )
//        )
//    }
//
//    @Test
//    fun onActivityResult_whenResultCodeGooglePay_handlesGooglePayResult() {
//        val googlePayClient = Mockito.mock(GooglePayClient::class.java)
//        val dropInRequest = DropInRequest()
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .googlePayClient(googlePayClient)
//        val sut = DropInClient(params)
//        val activity = Mockito.mock(
//            FragmentActivity::class.java
//        )
//        val intent = Mockito.mock(Intent::class.java)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        sut.handleActivityResult(activity, BraintreeRequestCodes.GOOGLE_PAY, 1, intent, callback)
//        Mockito.verify(googlePayClient).onActivityResult(
//            Matchers.eq(1), Matchers.same(intent), Matchers.any(
//                GooglePayOnActivityResultCallback::class.java
//            )
//        )
//    }
//
//    @Test
//    fun onActivityResult_whenResultCodeThreeDSecure_handlesThreeDSecureResult() {
//        val threeDSecureClient = Mockito.mock(
//            ThreeDSecureClient::class.java
//        )
//        val dropInRequest = DropInRequest()
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .threeDSecureClient(threeDSecureClient)
//        val sut = DropInClient(params)
//        val activity = Mockito.mock(
//            FragmentActivity::class.java
//        )
//        val intent = Mockito.mock(Intent::class.java)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        sut.handleActivityResult(
//            activity,
//            BraintreeRequestCodes.THREE_D_SECURE,
//            1,
//            intent,
//            callback
//        )
//        Mockito.verify(threeDSecureClient).onActivityResult(
//            Matchers.eq(1), Matchers.same(intent), Matchers.any(
//                ThreeDSecureResultCallback::class.java
//            )
//        )
//    }
//
//    @Test
//    fun handleGooglePayActivityResult_withPaymentMethodNonce_callsBackDropInResult() {
//        val paymentMethodNonce = Mockito.mock(
//            PaymentMethodNonce::class.java
//        )
//        val googlePayClient = MockGooglePayClientBuilder()
//            .onActivityResultSuccess(paymentMethodNonce)
//            .build()
//        val dataCollector = MockDataCollectorBuilder()
//            .collectDeviceDataSuccess("sample-data")
//            .build()
//        val dropInRequest = DropInRequest()
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .dataCollector(dataCollector)
//            .googlePayClient(googlePayClient)
//        val sut = DropInClient(params)
//        val data = Mockito.mock(Intent::class.java)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        sut.handleGooglePayActivityResult(activity, 1, data, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val result = captor.value
//        Assert.assertEquals(paymentMethodNonce, result.paymentMethodNonce)
//    }
//
//    @Test
//    fun handleGooglePayActivityResult_withError_callsBackError() {
//        val error = Exception("Google Pay error")
//        val googlePayClient = MockGooglePayClientBuilder()
//            .onActivityResultError(error)
//            .build()
//        val dropInRequest = DropInRequest()
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .googlePayClient(googlePayClient)
//        val sut = DropInClient(params)
//        val data = Mockito.mock(Intent::class.java)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        sut.handleGooglePayActivityResult(activity, 1, data, callback)
//        Mockito.verify(callback).onResult(Matchers.isNull() as DropInResult, Matchers.same(error))
//    }
//
//    @Test
//    fun handleVenmoActivityResult_withVenmoAccountNonce_callsBackDropInResult() {
//        val venmoAccountNonce = Mockito.mock(
//            VenmoAccountNonce::class.java
//        )
//        val venmoClient = MockVenmoClientBuilder()
//            .onActivityResultSuccess(venmoAccountNonce)
//            .build()
//        val dataCollector = MockDataCollectorBuilder()
//            .collectDeviceDataSuccess("sample-data")
//            .build()
//        val dropInRequest = DropInRequest()
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .dataCollector(dataCollector)
//            .venmoClient(venmoClient)
//        val sut = DropInClient(params)
//        val data = Mockito.mock(Intent::class.java)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        sut.handleVenmoActivityResult(activity, 1, data, callback)
//        val captor = ArgumentCaptor.forClass(
//            DropInResult::class.java
//        )
//        Mockito.verify(callback).onResult(captor.capture(), Matchers.isNull() as Exception)
//        val result = captor.value
//        Assert.assertEquals(venmoAccountNonce, result.paymentMethodNonce)
//    }
//
//    @Test
//    fun handleVenmoActivityResult_withVenmoError_callsBackError() {
//        val error = Exception("Venmo error")
//        val venmoClient = MockVenmoClientBuilder()
//            .onActivityResultError(error)
//            .build()
//        val dropInRequest = DropInRequest()
//        val params = DropInClientParams()
//            .dropInRequest(dropInRequest)
//            .venmoClient(venmoClient)
//        val sut = DropInClient(params)
//        val data = Mockito.mock(Intent::class.java)
//        val callback = Mockito.mock(
//            DropInResultCallback::class.java
//        )
//        sut.handleVenmoActivityResult(activity, 1, data, callback)
//        Mockito.verify(callback).onResult(Matchers.isNull() as DropInResult, Matchers.same(error))
//    }
//
//    @Test
//    fun onDropInResult_whenResultHasNoError_notifiesListenerOfSuccessViaCallback() {
//        val params = DropInClientParams()
//        val sut = DropInClient(params)
//        val listener = Mockito.mock(DropInListener::class.java)
//        sut.setListener(listener)
//        val dropInResult = DropInResult()
//        sut.onDropInResult(dropInResult)
//        Mockito.verify(listener).onDropInSuccess(dropInResult)
//    }
//
//    @Test
//    fun onDropInResult_whenResultHasError_notifiesListenerOfErrorViaCallback() {
//        val params = DropInClientParams()
//        val sut = DropInClient(params)
//        val listener = Mockito.mock(DropInListener::class.java)
//        sut.setListener(listener)
//        val dropInResult = DropInResult()
//        val error = Exception("sample error")
//        dropInResult.error = error
//        sut.onDropInResult(dropInResult)
//        Mockito.verify(listener).onDropInFailure(error)
//    }
//
//    @Test
//    fun onDropInResult_whenResultIsNull_doesNothing() {
//        val params = DropInClientParams()
//        val sut = DropInClient(params)
//        val listener = Mockito.mock(DropInListener::class.java)
//        sut.setListener(listener)
//        sut.onDropInResult(null)
//        Mockito.verifyZeroInteractions(listener)
//    }
//
//    private fun mockConfiguration(
//        paypalEnabled: Boolean, venmoEnabled: Boolean,
//        cardEnabled: Boolean, googlePayEnabled: Boolean, unionPayEnabled: Boolean
//    ): Configuration {
//        val configuration = Mockito.mock(
//            Configuration::class.java
//        )
//        Mockito.`when`(configuration.isPayPalEnabled).thenReturn(paypalEnabled)
//        Mockito.`when`(configuration.isVenmoEnabled).thenReturn(venmoEnabled)
//        Mockito.`when`(configuration.isGooglePayEnabled).thenReturn(googlePayEnabled)
//        Mockito.`when`(configuration.isUnionPayEnabled).thenReturn(unionPayEnabled)
//        if (cardEnabled) {
//            Mockito.`when`(configuration.supportedCardTypes)
//                .thenReturn(listOf(DropInPaymentMethod.VISA.name))
//        }
//        return configuration
//    }
//
//    companion object {
//        private fun createSuccessfulBrowserSwitchResult(): BrowserSwitchResult {
//            val requestCode = 123
//            val url = Uri.parse("www.example.com")
//            val returnUrlScheme = "sample-scheme"
//            val browserSwitchRequest = BrowserSwitchRequest(
//                requestCode, url, JSONObject(), returnUrlScheme, true
//            )
//            return BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, browserSwitchRequest)
//        }
//    }
}