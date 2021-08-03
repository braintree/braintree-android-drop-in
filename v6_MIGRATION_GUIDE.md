# Braintree Android Drop-In v6 Migration Guide

See the [CHANGELOG](/CHANGELOG.md) for a complete list of changes. This migration guide outlines the basics for updating your Braintree Drop-In integration from v5 to v6.

v6 of the Drop-In SDK is compatible with v4 of the [Braintree Android SDK](https://github.com/braintree/braintree_android).

_Documentation for v6 will be published to https://developer.paypal.com/braintree/docs once it is available for general release._

## Table of Contents

1. [Gradle](#gradle)
1. [Builder Pattern](#builder-pattern)
1. [Authorization](#authorization)
1. [Launch Drop-In](#launch-drop-in)
1. [Handle Drop-In Result](#handle-drop-in-result)
1. [Fetch Last Used Payment Method](#fetch-last-used-payment-method)

## Gradle

Add the dependency in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:drop-in:6.0.0'
}
```

The credentials for accessing the Cardinal Mobile SDK have changed.

Update the credentials in your top-level build.gradle to the following:

```groovy
repositories {
    maven {
        url "https://cardinalcommerceprod.jfrog.io/artifactory/android"
        credentials {
            username 'braintree_team_sdk'
            password 'AKCp8jQcoDy2hxSWhDAUQKXLDPDx6NYRkqrgFLRc3qDrayg6rrCbJpsKKyMwaykVL8FWusJpp'
        }
    }
}
```

## Builder Pattern

The builder pattern has been removed in v6 to allow for consistent object creation across Java and Kotlin. 
Method chaining has been removed, and setters have been renamed with the `set` prefix.

For example, a `DropInRequest` can now be constructed below:

Java:
```java
DropInRequest request = new DropInRequest();
request.setCollectDeviceData(true);
request.setMaskCardNumber(true);
```

Kotlin:
```kotlin
val request = DropInRequest()
request.collectDeviceData = true
request.maskCardNumber = true
```

See the [Braintree Android v4 Migration Guide](https://github.com/braintree/braintree_android/blob/master/v4_MIGRATION_GUIDE.md) for changes to request objects for Google Pay, Venmo, PayPal, and 3DS. 

## Authorization

The `clientToken` and `tokenizationKey` parameters have been removed from `DropInRequest`. 
In v6, authorization should be included when instantiating a `DropInClient` instead.

## Launch Drop-In 

`DropInClient` has been added in v6 and is responsible for launching `DropInActivity`. 
To launch Drop-In, instantiate a `DropInClient` and call `DropInClient#launchDropInForResult`:

Java:
```java
DropInClient dropInClient = new DropInClient(this, "TOKENIZATION_KEY_OR_CLIENT_TOKEN", dropInRequest);
dropInClient.launchDropInForResult(this, DROP_IN_REQUEST_CODE);
```

Kotlin:
```kotlin
val dropInClient = DropInClient(this, "TOKENIZATION_KEY_OR_CLIENT_TOKEN", dropInRequest)
dropInClient.launchDropInForResult(this, DROP_IN_REQUEST_CODE)
```

## Handle Drop-In Result

Handle the result from Drop-In in `onActivityResult` as in v5. 
The key for accessing an error from the Activity result has moved from `DropInActivity.EXTRA_ERROR` to `DropInResult.EXTRA_ERROR`.
The method for accessing the `String` payment method nonce from `DropInResult` has been updated from `PaymentMethodNonce#getNonce` to `PaymentMethodNonce#getString`. 


Java:
```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == DROP_IN_REQUEST_CODE) {
        if (resultCode == RESULT_OK) {
            DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
            String paymentMethodNonce = result.getPaymentMethodNonce().getString();
            // send paymentMethodNonce to your server
        } else if (resultCode == RESULT_CANCELED) {
            // canceled
        } else {
            // an error occurred, checked the returned exception
            Exception exception = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
        }
    }
}
```

Kotlin:
```kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DROP_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val result: DropInResult? = data?.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT)
                val paymentMethodNonce = result?.paymentMethodNonce?.string
                // use the result to update your UI and send the payment method nonce to your server
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                val error: Exception? = data?.getSerializableExtra(DropInResult.EXTRA_ERROR) as Exception?
            }
        }
    }
```

## Fetch Last Used Payment Method

Fetching a user's existing payment method has moved from `DropInResult#fetchDropInResult` to `DropInClient#fetchMostRecentPaymentMethod`. 
Note that a payment method will only be returned when using a client token created with a customer_id.

Java:
```java
    DropInClient dropInClient = new DropInClient(this, "CLIENT_TOKEN_WITH_CUSTOMER_ID", dropInRequest);
    dropInClient.fetchMostRecentPaymentMethod(this, new FetchMostRecentPaymentMethodCallback() {
        @Override
        public void onResult(DropInResult dropInResult, Exception error) {
            // handle result
        }
    });
```

Kotlin:
```kotlin
    val dropInClient = DropInClient(this, "CLIENT_TOKEN_WITH_CUSTOMER_ID", dropInRequest)
    dropInClient.fetchMostRecentPaymentMethod(this) { dropInResult, error ->
        // handle result
    }
```