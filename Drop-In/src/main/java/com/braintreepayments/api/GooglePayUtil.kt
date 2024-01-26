package com.braintreepayments.api

import org.json.JSONArray
import org.json.JSONObject

class GooglePayUtil {

    companion object {

        private val supportedNetworks = listOf(
            "AMEX",
            "DISCOVER",
            "JCB",
            "MASTERCARD",
            "VISA")

        private val supportedMethods = listOf(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS")

        val allowedPaymentMethods: JSONArray = JSONArray().put(baseCardPaymentMethod())

        const val CARD_LAST_FOUR_EXTRA = "EXTRA_CARD_INFO"

        private fun baseCardPaymentMethod(): JSONObject {
            return JSONObject().apply {

                val parameters = JSONObject().apply {
                    put("allowedAuthMethods", JSONArray(supportedMethods))
                    put("allowedCardNetworks", JSONArray(supportedNetworks))
                    put("billingAddressRequired", true)
                    put("billingAddressParameters", JSONObject().apply {
                        put("format", "FULL")
                    })
                }

                put("type", "CARD")
                put("parameters", parameters)
            }
        }

    }
}