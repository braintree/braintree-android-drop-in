package com.braintreepayments.api

object Fixtures {

    const val TOKENIZATION_KEY = "sandbox_tmxhyf7d_dcpspy2brwdjr3qn"
    const val BASE64_CLIENT_TOKEN = "eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiJlbmNvZGVkX2F1dGhfZmluZ2VycHJpbnQiLCJjaGFsbGVuZ2VzIjpbImN2diIsInBvc3RhbF9jb2RlIl0sImNvbmZpZ1VybCI6ImVuY29kZWRfY2FwaV9jb25maWd1cmF0aW9uX3VybCIsImFzc2V0c1VybCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMCIsInRocmVlRFNlY3VyZUVuYWJsZWQiOmZhbHNlLCJwYXlwYWxFbmFibGVkIjpmYWxzZX0="

    // language=JSON
    const val CONFIGURATION_WITH_GOOGLE_PAY = """
        {
          "clientApiUrl": "client_api_url",
          "environment": "test",
          "merchantId": "integration_merchant_id",
          "merchantAccountId": "integration_merchant_account_id",
          "androidPay": {
            "enabled": true,
            "displayName": "Google Pay Merchant",
            "environment": "sandbox",
            "googleAuthorizationFingerprint": "google-auth-fingerprint",
            "paypalClientId": "pay-pal-client-id",
            "supportedNetworks": [
              "visa",
              "mastercard",
              "amex",
              "discover"
            ]
          }
        }
    """

    // language=JSON
    const val CONFIGURATION_WITH_GOOGLE_PAY_AND_THREE_D_SECURE = """
        {
          "clientApiUrl": "client_api_url",
          "environment": "test",
          "merchantId": "integration_merchant_id",
          "merchantAccountId": "integration_merchant_account_id",
          "threeDSecureEnabled": true,
          "androidPay": {
            "enabled": true,
            "displayName": "Google Pay Merchant",
            "environment": "sandbox",
            "googleAuthorizationFingerprint": "google-auth-fingerprint",
            "paypalClientId": "pay-pal-client-id",
            "supportedNetworks": [
              "visa",
              "mastercard",
              "amex",
              "discover"
            ]
          }
        }
    """

    // language=JSON
    const val CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL = """
        {
          "clientApiUrl": "client_api_url",
          "environment": "test",
          "merchantId": "integration_merchant_id",
          "merchantAccountId": "integration_merchant_account_id",
          "paypalEnabled": true,
          "creditCards": {
            "supportedCardTypes": [
              "American Express",
              "Discover",
              "JCB",
              "MasterCard",
              "Visa"
            ]
          },
          "androidPay": {
            "enabled": true,
            "displayName": "Google Pay Merchant",
            "environment": "sandbox",
            "googleAuthorizationFingerprint": "google-auth-fingerprint",
            "paypalClientId": "pay-pal-client-id",
            "supportedNetworks": [
              "visa",
              "mastercard",
              "amex",
              "discover"
            ]
          }
        }
    """

    // language=JSON
    const val CONFIGURATION_WITH_VALIDATION_REQUIRED = """
        {
          "clientApiUrl": "client_api_url",
          "environment": "test",
          "merchantId": "integration_merchant_id",
          "merchantAccountId": "integration_merchant_account_id",
          "challenges": ["cvv", "postal_code"],
          "creditCards": {
            "supportedCardTypes": [
              "American Express",
              "Discover",
              "JCB",
              "MasterCard",
              "Visa"
            ]
          }
        }
    """
    // language=JSON
    const val CONFIGURATION_WITH_UNIONPAY = """
        {
            "clientApiUrl": "client_api_url",
            "environment": "test",
            "merchantId": "integration_merchant_id",
            "merchantAccountId": "integration_merchant_account_id",
            "unionPay": {
                "enabled": true,
                "merchantAccountId": "merchant_account_id"
            },
            "creditCards": {
            "supportedCardTypes": [
              "American Express",
              "Discover",
              "JCB",
              "MasterCard",
              "Visa"
            ]
          }
        }
    """

    // region Client Tokens
    // language=JSON
    const val CLIENT_TOKEN = """
        {
          "configUrl": "client_api_configuration_url",
          "authorizationFingerprint": "authorization_fingerprint",
          "merchantId": "integration_merchant_id"
        }
    """
    // endregion

    // region Payment Methods
    // language=JSON
    const val PAYMENT_METHODS_GET_PAYMENT_METHODS_RESPONSE = """
        {
          "paymentMethods": [
            {
              "type": "CreditCard",
              "nonce": "123456-12345-12345-a-adfa",
              "description": "ending in ••11",
              "default": true,
              "isLocked": false,
              "securityQuestions": [],
              "details":
              {
                "cardType": "Visa",
                "lastTwo": "11",
                "lastFour": "1111"
              }
            },
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
                  "accountAddress": {
                    "street1": "123 Fake St.",
                    "street2": "Apt. 3",
                    "city": "Oakland",
                    "state": "CA",
                    "postalCode": "94602",
                    "country": "US"
                  }
                }
              }
            },
            {
              "type": "AndroidPayCard",
              "nonce": "fake-google-pay-nonce",
              "description": "Google Pay",
              "details": {
                "cardType": "Visa",
                "lastTwo": "11",
                "lastFour": "1111"
              }
            },
            {
              "type": "VenmoAccount",
              "nonce": "fake-venmo-nonce",
              "description": "VenmoAccount",
              "details": {
                "cardType": "Visa",
                "username": "happy-venmo-joe"
              }
            }
          ]
        }
    """

    // language=JSON
    const val PAYMENT_METHODS_GET_PAYMENT_METHODS_GOOGLE_PAY_RESPONSE = """
        {
          "paymentMethods": [
            {
              "type": "AndroidPayCard",
              "nonce": "fake-google-pay-nonce",
              "description": "Google Pay",
              "details": {
                "cardType": "Visa",
                "lastTwo": "11"
              }
            }
          ]
        }
    """

    // language=JSON
    const val PAYMENT_METHODS_UNIONPAY_CREDIT_CARD = """
        {
          "type": "CreditCard",
          "nonce": "12345678-1234-1234-1234-123456789012",
          "description": "ending in ••85",
          "default": false,
          "isLocked": false,
          "securityQuestions": [],
          "details":
          {
            "cardType": "UnionPay",
            "lastTwo": "85",
            "lastFour": "0085"
          }
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
    // endregion

    // region Responses
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
    const val GET_PAYMENT_METHODS_EMPTY_RESPONSE = """
        {
          "paymentMethods": []
        }
    """

    // language=JSON
    const val GET_PAYMENT_METHODS_GOOGLE_PAY_RESPONSE = """
        {
          "paymentMethods": [
            {
              "type": "AndroidPayCard",
              "nonce": "fake-android-pay-nonce",
              "description": "Visa 1111",
              "details": {
                "cardType": "Visa",
                "lastTwo": "11",
                "lastFour": "1111"
              }
            },
            {
              "type": "CreditCard",
              "nonce": "123456-12345-12345-a-adfa",
              "description": "ending in ••11",
              "isDefault": false,
              "isLocked": false,
              "securityQuestions": [],
              "details":
              {
                "cardType": "Visa",
                "lastTwo": "11",
                "lastFour": "1111"
              }
            },
            {
              "type": "PayPalAccount",
              "nonce": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
              "description": "paypalaccount@example.com",
              "isDefault": false,
              "isLocked": false,
              "securityQuestions": [],
              "details": {
                "email": "paypalaccount@example.com",
                "payerInfo": {
                  "accountAddress": {
                    "street1": "123 Fake St.",
                    "street2": "Apt. 3",
                    "city": "Oakland",
                    "state": "CA",
                    "postalCode": "94602",
                    "country": "US"
                  }
                }
              }
            }
          ]
        }
    """

    // language=JSON
    const val GET_PAYMENT_METHODS_RESPONSE = """
        {
          "paymentMethods": [
            {
              "type": "CreditCard",
              "nonce": "123456-12345-12345-a-adfa",
              "description": "ending in ••11",
              "isDefault": false,
              "isLocked": false,
              "securityQuestions": [],
              "details":
              {
                "cardType": "Visa",
                "lastTwo": "11",
                "lastFour": "1111"
              }
            },
            {
              "type": "PayPalAccount",
              "nonce": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
              "description": "paypalaccount@example.com",
              "isDefault": false,
              "isLocked": false,
              "securityQuestions": [],
              "details": {
                "email": "paypalaccount@example.com",
                "payerInfo": {
                  "accountAddress": {
                    "street1": "123 Fake St.",
                    "street2": "Apt. 3",
                    "city": "Oakland",
                    "state": "CA",
                    "postalCode": "94602",
                    "country": "US"
                  }
                }
              }
            }
          ]
        }
    """

    // language=JSON
    const val GET_PAYMENT_METHODS_TWO_CARDS_RESPONSE = """
        {
          "paymentMethods": [
            {
              "type": "CreditCard",
              "nonce": "123456-12345-12345-a-adfa",
              "description": "ending in ••11",
              "isDefault": false,
              "isLocked": false,
              "securityQuestions": [],
              "details":
              {
                "cardType": "Visa",
                "lastTwo": "11",
                "lastFour": "1111"
              }
            },
            {
              "type": "CreditCard",
              "nonce": "123456-12345-12345-a-amex",
              "description": "ending in ••05",
              "isDefault": false,
              "isLocked": false,
              "securityQuestions": [],
              "details":
              {
                "cardType": "American Express",
                "lastTwo": "05",
                "lastFour": "0005"
              }
            }
          ]
        }
    """

    // language=JSON
    const val GOOGLE_PAY_NETWORK_TOKENIZED_RESPONSE = """
        {
          "apiVersionMinor": 0,
          "apiVersion": 2,
          "paymentMethodData": {
            "description": "Visa •••• 1234",
            "tokenizationData": {
              "type": "PAYMENT_GATEWAY",
              "token": "{\"androidPayCards\":[{\"type\":\"AndroidPayCard\",\"nonce\":\"d887f42c-bda5-091a-0798-af42d3ed173e\",\"description\":\"Android Pay\",\"consumed\":false,\"details\":{\"cardType\":\"Visa\",\"bin\":\"123456\",\"lastTwo\":\"34\",\"lastFour\":\"1234\",\"isNetworkTokenized\":true},\"binData\":{\"prepaid\":\"No\",\"healthcare\":\"No\",\"debit\":\"No\",\"durbinRegulated\":\"No\",\"commercial\":\"No\",\"payroll\":\"No\",\"issuingBank\":\"Issuing Bank USA\",\"countryOfIssuance\":\"USA\",\"productId\":\"A\"}}]}"
            },
            "type": "CARD",
            "info": {
              "cardNetwork": "VISA",
              "cardDetails": "1234"
            }
          }
        }
    """

    // language=JSON
    const val GOOGLE_PAY_NON_NETWORK_TOKENIZED_RESPONSE = """
        {
          "apiVersionMinor": 0,
          "apiVersion": 2,
          "paymentMethodData": {
            "description": "Visa •••• 1234",
            "tokenizationData": {
              "type": "PAYMENT_GATEWAY",
              "token": "{\"androidPayCards\":[{\"type\":\"AndroidPayCard\",\"nonce\":\"d887f42c-bda5-091a-0798-af42d3ed173e\",\"description\":\"Android Pay\",\"consumed\":false,\"details\":{\"cardType\":\"Visa\",\"bin\":\"123456\",\"lastTwo\":\"34\",\"lastFour\":\"1234\",\"isNetworkTokenized\":false},\"binData\":{\"prepaid\":\"No\",\"healthcare\":\"No\",\"debit\":\"No\",\"durbinRegulated\":\"No\",\"commercial\":\"No\",\"payroll\":\"No\",\"issuingBank\":\"Issuing Bank USA\",\"countryOfIssuance\":\"USA\",\"productId\":\"A\"}}]}"
            },
            "type": "CARD",
            "info": {
              "cardNetwork": "VISA",
              "cardDetails": "1234"
            }
          }
        }
    """

    // language=JSON
    const val THREE_D_SECURE_LOOKUP_RESPONSE = """
        {
          "lookup": {
            "acsUrl": "https://acs-url/",
            "md": "merchant-descriptor",
            "termUrl": "https://term-url/",
            "pareq": "pareq"
          },
          "paymentMethod": {
            "type": "CreditCard",
            "nonce": "123456-12345-12345-a-adfa",
            "description": "ending in ••11",
            "isDefault": false,
            "isLocked": false,
            "securityQuestions": [],
            "details": {
              "cardType": "Visa",
              "lastTwo": "11",
              "lastFour": "1111"
            },
            "threeDSecureInfo": {
              "liabilityShifted": true,
              "liabilityShiftPossible": true
            }
          },
          "threeDSecureInfo": {
            "liabilityShifted": true,
            "liabilityShiftPossible": true
          }
        }
    """

    // language=JSON
    const val UNIONPAY_CAPABILITIES_NOT_SUPPORTED_RESPONSE = """
        {
          "isUnionPay": true,
          "isDebit": false,
          "unionPay": {
            "supportsTwoStepAuthAndCapture": true,
            "isSupported": false
          }
        }
    """

    // language=JSON
    const val UNIONPAY_CAPABILITIES_SUCCESS_RESPONSE = """
        {
          "isUnionPay": true,
          "isDebit": false,
          "unionPay": {
            "supportsTwoStepAuthAndCapture": true,
            "isSupported": true
          }
        }
    """

    // language=JSON
    const val UNIONPAY_ENROLLMENT_ERROR_RESPONSE = """
        {
          "error": {
            "message": "UnionPay Enrollment is invalid"
          },
          "fieldErrors": [
            {
              "field": "unionPayEnrollment",
              "fieldErrors": [
                {
                  "code": "84430",
                  "field": "expirationDate",
                  "message": "Credit card expiration date is required"
                },
                {
                  "code": "84407",
                  "field": "mobileCountryCode",
                  "message": "Mobile country code is required"
                },
                {
                  "code": "84409",
                  "field": "mobileNumber",
                  "message": "Mobile number is required"
                }
              ]
            }
          ]
        }
    """

    // language=JSON
    const val UNIONPAY_ENROLLMENT_MOBILE_NUMBER_ERROR_RESPONSE = """
        {
          "error": {
            "message": "UnionPay Enrollment is invalid"
          },
          "fieldErrors": [
            {
              "field": "unionPayEnrollment",
              "fieldErrors": [
                {
                  "code": "84408",
                  "field": "mobileCountryCode",
                  "message": "Mobile country code is invalid"
                },
                {
                  "code": "84407",
                  "field": "mobileNumber",
                  "message": "Mobile number is invalid"
                }
              ]
            }
          ]
        }
    """

    // language=JSON
    const val UNIONPAY_ENROLLMENT_SMS_NOT_REQUIRED = """
        {
          "unionPayEnrollmentId": "enrollment-id",
          "smsCodeRequired": false
        }
    """

    // language=JSON
    const val UNIONPAY_ENROLLMENT_SMS_REQUIRED = """
        {
          "unionPayEnrollmentId": "enrollment-id",
          "smsCodeRequired": true
        }
    """

    // language=JSON
    const val UNIONPAY_SMS_CODE_ERROR_RESPONSE = """
        {
          "error": {
            "message": "UnionPay Enrollment failed"
          },
          "fieldErrors": [
            {
              "field": "unionPayEnrollment",
              "fieldErrors": [
                {
                  "code": "84424",
                  "field": "base",
                  "message": "SMS verification code is incorrect. Please try again"
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
    // endregion

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
    const val PAYMENT_METHODS_GET_PAYMENT_METHODS_EMPTY_RESPONSE = """
        {
          "paymentMethods": []
        }
    """

}
