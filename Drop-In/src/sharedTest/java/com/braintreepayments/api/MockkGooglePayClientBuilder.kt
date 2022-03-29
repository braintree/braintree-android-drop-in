package com.braintreepayments.api

import io.mockk.every
import io.mockk.mockk

class MockkGooglePayClientBuilder {

    private var isReadyToPaySuccess: Boolean? = null
    private var isReadyToPayError: Exception? = null

    private var onActivityResultSuccess: PaymentMethodNonce? = null
    private var onActivityResultError: Exception? = null

    fun isReadyToPaySuccess(isReadyToPay: Boolean): MockkGooglePayClientBuilder {
        isReadyToPaySuccess = isReadyToPay
        return this
    }

    fun isReadyToPayError(error: Exception): MockkGooglePayClientBuilder {
        isReadyToPayError = error
        return this
    }

    fun onActivityResultSuccess(paymentMethodNonce: PaymentMethodNonce): MockkGooglePayClientBuilder {
        onActivityResultSuccess = paymentMethodNonce
        return this
    }

    fun onActivityResultError(error: Exception): MockkGooglePayClientBuilder {
        onActivityResultError = error
        return this
    }

    fun build(): GooglePayClient {
        val googlePayClient = mockk<GooglePayClient>(relaxed = true)

        every { googlePayClient.isReadyToPay(any(), any()) } answers { call ->
            val callback = call.invocation.args[1] as GooglePayIsReadyToPayCallback
            if (isReadyToPaySuccess != null) {
                callback.onResult(isReadyToPaySuccess!!, null)
            } else if (onActivityResultError != null) {
                callback.onResult(false, onActivityResultError)
            }
        }
        return googlePayClient
    }
}