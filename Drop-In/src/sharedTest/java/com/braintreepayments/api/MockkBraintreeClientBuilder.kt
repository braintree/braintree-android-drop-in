package com.braintreepayments.api

import io.mockk.every
import io.mockk.mockk

internal class MockkBraintreeClientBuilder {

    private var authorization: Authorization? = null
    private var authorizationError: Exception? = null

    private var configuration: Configuration? = null
    private var configurationError: Exception? = null

    fun configurationSuccess(configuration: Configuration): MockkBraintreeClientBuilder {
        this.configuration = configuration
        return this
    }

    fun configurationError(error: Exception): MockkBraintreeClientBuilder {
        this.configurationError = error
        return this
    }

    fun authorizationSuccess(authorization: Authorization): MockkBraintreeClientBuilder {
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
            braintreeClient.getConfiguration(any())
        } answers { call ->
            val callback = call.invocation.args[0] as ConfigurationCallback
            if (configuration != null) {
                callback.onResult(configuration, null)
            } else if (configurationError != null) {
                callback.onResult(null, configurationError)
            }
        }

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