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
    const val CREDIT_CARD_NON_NUMBER_ERROR_RESPONSE = """
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
                  "field": "base",
                  "message": "Credit card must include number, payment_method_nonce, or venmo_sdk_payment_method_code"
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

    // language=JSON
    const val VISA_CREDIT_CARD_RESPONSE = """
        {
          "creditCards": [
            {
              "type": "CreditCard",
              "nonce": "123456-12345-12345-a-adfa",
              "description": "ending in ••11",
              "default": false,
              "isLocked": false,
              "securityQuestions": [],
              "details":
              {
                "cardType": "Visa",
                "lastTwo": "11",
                "lastFour": "1111"
              }
            }
          ]
        }
    """

    // language=JSON
    const val PAYMENT_METHODS_VISA_CREDIT_CARD = """
        {
          "type": "CreditCard",
          "nonce": "12345678-1234-1234-1234-123456789012",
          "description": "ending in ••11",
          "default": false,
          "isLocked": false,
          "securityQuestions": [],
          "details":
          {
            "cardType": "Visa",
            "lastTwo": "11",
            "lastFour": "1111"
          }
        }
    """

    // language=JSON
    const val PAYPAL_ACCOUNT_JSON = """
        {
          "paypalAccounts": [
            {
              "type": "PayPalAccount",
              "nonce": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
              "description": "with email paypalaccount@example.com",
              "default": false,
              "isLocked": false,
              "securityQuestions": [],
              "details": {
                "email": "paypalaccount@example.com",
                "payerInfo": {
                }
              }
            }
          ]
        }
    """

    // language=JSON
    const val PAYMENT_METHODS_VENMO_ACCOUNT_RESPONSE = """
        {
          "venmoAccounts": [{
            "type": "VenmoAccount",
            "nonce": "fake-venmo-nonce",
            "description": "VenmoAccount",
            "consumed": false,
            "default": true,
            "details": {
              "cardType": "Discover",
              "username": "venmojoe"
            }
          }]
        }
    """
}