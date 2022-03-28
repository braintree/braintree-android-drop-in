package com.braintreepayments.api

import io.mockk.every
import io.mockk.mockk

internal class MockkBraintreeClientBuilder {

    private var authorization: Authorization? = null
    private var authorizationError: Exception? = null

    internal fun authorizationSuccess(authorization: Authorization): MockkBraintreeClientBuilder {
        this.authorization = authorization
        return this
    }

    fun authorizationError(error: Exception): MockkBraintreeClientBuilder {
        this.authorizationError = error
        return this
    }

    internal fun build(): BraintreeClient {
        val braintreeClient = mockk<BraintreeClient>(relaxed = true)

        every {
            braintreeClient.getAuthorization(any())
        } answers { call ->
            val callback = call.invocation.args[0] as AuthorizationCallback
            if (authorization != null) {
                callback.onAuthorizationResult(authorization, null)
            } else if (authorizationError != null) {
                callback.onAuthorizationResult(null, authorizationError)
            }
        }

        return braintreeClient
    }
}