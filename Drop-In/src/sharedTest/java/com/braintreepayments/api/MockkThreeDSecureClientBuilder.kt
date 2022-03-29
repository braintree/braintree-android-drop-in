package com.braintreepayments.api

import io.mockk.every
import io.mockk.mockk

class MockkThreeDSecureClientBuilder {

    private var performVerificationError: Exception? = null
    private var performVerificationSuccess: ThreeDSecureResult? = null

    private var continueVerificationError: Exception? = null
    private var continueVerificationSuccess: ThreeDSecureResult? = null

    private var browserSwitchError: Exception? = null
    private var browserSwitchResult: ThreeDSecureResult? = null

    private var activityResultError: Exception? = null
    private var activityResult: ThreeDSecureResult? = null

    fun performVerificationError(performVerificationError: Exception): MockkThreeDSecureClientBuilder {
        this.performVerificationError = performVerificationError
        return this
    }

    fun performVerificationSuccess(performVerificationSuccess: ThreeDSecureResult): MockkThreeDSecureClientBuilder {
        this.performVerificationSuccess = performVerificationSuccess
        return this
    }

    fun continueVerificationError(continueVerificationError: Exception): MockkThreeDSecureClientBuilder {
        this.continueVerificationError = continueVerificationError
        return this
    }

    fun continueVerificationSuccess(continueVerificationSuccess: ThreeDSecureResult): MockkThreeDSecureClientBuilder {
        this.continueVerificationSuccess = continueVerificationSuccess
        return this
    }

    fun build(): ThreeDSecureClient {
        val threeDSecureClient = mockk<ThreeDSecureClient>(relaxed = true)

        every { threeDSecureClient.performVerification(any(), any(), any()) } answers { call ->
            val callback = call.invocation.args[2] as ThreeDSecureResultCallback
            if (performVerificationSuccess != null) {
                callback.onResult(performVerificationSuccess, null)
            } else if (performVerificationError != null) {
                callback.onResult(null, performVerificationError)
            }
        }

        every {
            threeDSecureClient.continuePerformVerification(any(), any(), any())
        } answers { call ->
            val callback = call.invocation.args[2] as ThreeDSecureResultCallback
            if (continueVerificationSuccess != null) {
                callback.onResult(continueVerificationSuccess, null)
            } else if (continueVerificationError != null) {
                callback.onResult(null, continueVerificationError)
            }
        }

        every {
            threeDSecureClient.continuePerformVerification(any(), any(), any(), any())
        } answers { call ->
            val callback = call.invocation.args[3] as ThreeDSecureResultCallback
            if (continueVerificationSuccess != null) {
                callback.onResult(continueVerificationSuccess, null)
            } else if (continueVerificationError != null) {
                callback.onResult(null, continueVerificationError)
            }
        }

        return threeDSecureClient
    }
}