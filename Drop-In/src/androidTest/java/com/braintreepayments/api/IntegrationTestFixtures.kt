package com.braintreepayments.api

object IntegrationTestFixtures {

    // language=JSON
    const val CREDIT_CARD_ERROR_RESPONSE = """
        {
          "error": {
            "message": "Credit card is invalid"
          },
          "fieldErrors": [
            {
              "field": "creditCard",
              "fieldErrors": [
                {
                  "field": "billingAddress",
                  "message": "Postal code is invalid"
                },
                {
                  "field": "cvv",
                  "message": "CVV is invalid"
                },
                {
                  "field": "expirationMonth",
                  "message": "Expiration month is invalid"
                },
                {
                  "field": "expirationYear",
                  "message": "Expiration year is invalid"
                },
                {
                  "field": "number",
                  "message": "Credit card number is required"
                },
                {
                  "field": "base",
                  "message": "Credit card must include number, payment_method_nonce, or venmo_sdk_payment_method_code"
                }
              ]
            }
          ]
        }
    """

    // language=JSON
    const val ERRORS_CREDIT_CARD_DUPLICATE = """
        {
            "error": {
                "message": "Credit card is invalid"
            },
            "fieldErrors": [
                {
                    "field": "creditCard",
                    "fieldErrors": [
                        {
                            "field": "number",
                            "message": "Duplicate card exists in the vault.",
                            "code": "81724"
                        }
                    ]
                }
            ]
        }
    """

    // language=JSON
    const val CREDIT_CARD_EXPIRATION_ERROR_RESPONSE = """
        {
          "error": {
            "message": "Credit card is invalid"
          },
          "fieldErrors": [
            {
              "field": "creditCard",
              "fieldErrors": [
                {
                  "field": "expirationMonth",
                  "message": "Expiration month is invalid"
                },
                {
                  "field": "expirationYear",
                  "message": "Expiration year is invalid"
                },
                {
                  "field": "base",
                  "message": "Credit card must include number, payment_method_nonce, or venmo_sdk_payment_method_code"
                }
              ]
            }
          ]
        }
    """
}