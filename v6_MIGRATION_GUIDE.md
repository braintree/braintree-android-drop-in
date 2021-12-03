# Braintree Android Drop-In v6 Migration Guide

See the [CHANGELOG](/CHANGELOG.md) for a complete list of changes. This migration guide outlines the basics for updating your Braintree Drop-In integration from v5 to v6.

v6 of the Drop-In SDK requires v4 of the [Braintree Android SDK](https://github.com/braintree/braintree_android).

_Documentation for v6 will be published to https://developer.paypal.com/braintree/docs once it is available for general release._

## Table of Contents

1. [Gradle](#gradle)
1. [Builder Pattern](#builder-pattern)
1. [DropInRequest](#dropinrequest)
1. [DropInClient](#dropinclient)
1. [Authorization](#authorization)
1. [Launch Drop-In](#launch-drop-in)
1. [Handle Drop-In Result](#handle-drop-in-result)
1. [Fetch Last Used Payment Method](#fetch-last-used-payment-method)

## Gradle

Add the dependency in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:drop-in:6.0.0-beta2'
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

For example, a `DropInRequest` can now be constructed as shown below:

Java:
```java
DropInRequest request = new DropInRequest();
request.setMaskCardNumber(true);
```

Kotlin:
```kotlin
val request = DropInRequest()
request.maskCardNumber = true
```

See the [Braintree Android v4 Migration Guide](https://github.com/braintree/braintree_android/blob/master/v4_MIGRATION_GUIDE.md) for changes to request objects for Google Pay, Venmo, PayPal, and 3DS. 

## DropInRequest

The getters and setters on `DropInRequest` have been renamed with a consistent get/set pattern to allow for Kotlin synthesized properties. 
See the examples below for a full list of optional parameters:

Java:
```java
    DropInRequest dropInRequest = new DropInRequest();
    dropInRequest.setGooglePayRequest(googlePayRequest);
    dropInRequest.setGooglePayDisabled(true);
    dropInRequest.setPayPalRequest(paypalRequest);
    dropInRequest.setPayPalDisabled(true);
    dropInRequest.setVenmoRequest(venmoRequest);
    dropInRequest.setVenmoDisabled(true);
    dropInRequest.setCardDisabled(true);
    dropInRequest.setThreeDSecureRequest(threeDSecureRequest);
    dropInRequest.setMaskCardNumber(true);
    dropInRequest.setMaskSecurityCode(true);
    dropInRequest.setVaultManagerEnabled(true);
    dropInRequest.setAllowVaultCardOverride(true);
    dropInRequest.setVaultCardDefaultValue(true);
    dropInRequest.setCardholderNameStatus(CardForm.FIELD_OPTIONAL);
    dropInRequest.setVaultVenmoDefaultValue(true);
```

Kotlin:
```kotlin
    val dropInRequest = DropInRequest()
    dropInRequest.googlePayRequest = googlePayRequest
    dropInRequest.isGooglePayDisabled = true
    dropInRequest.payPalRequest = paypalRequest
    dropInRequest.isPayPalDisabled = true
    dropInRequest.venmoRequest = venmoRequest
    dropInRequest.isVenmoDisabled = true
    dropInRequest.isCardDisabled = true
    dropInRequest.threeDSecureRequest = threeDSecureRequest
    dropInRequest.maskCardNumber = true
    dropInRequest.maskSecurityCode = true
    dropInRequest.isVaultManagerEnabled = true
    dropInRequest.allowVaultCardOverride = true
    dropInRequest.vaultCardDefaultValue = true
    dropInRequest.cardholderNameStatus = FIELD_OPTIONAL
    dropInRequest.vaultVenmoDefaultValue = true
```

The full list of changed parameters is below:
1. `clientTokent` -> removed
1. `tokenizationKey` -> removed
1. `amount` -> removed
1. `intent` -> removed
1. `googlePaymentRequest` -> `googlePayRequest`
1. `disableGooglePayment` -> `googlePayDisabled`
1. `paypalRequest` -> `payPalRequest`
1. `disablePayPal` -> `payPalDisabled`
1. `disableVenmo` -> `venmoDisabled`
1. `disableCard` -> `cardDisabled`
1. `vaultManager` -> `vaultManagerEnabled`
1. `vaultCard` -> `vaultCardDefaultValue`
1. `vaultVenmo` -> removed 
1. `requestThreeDSecureVerification` -> removed

The `collectDeviceData` field has been removed from `DropInRequest` in favor of always returning device data.
The `vaultVenmo` field has been removed from `DropInRequest` in favor of setting `shouldVault` on a `VenmoRequest` that is set on the `DropInRequest`
The `requestThreeDSecureVerification` field has been removed from `DropInRequest` in favor of requesting 3DS if a `ThreeDSecureRequest` with an `amount` is set on `DropInRequest` and the merchant is configured for 3DS.

## DropInClient

`DropInClient` has been added in `v6` and replaces the use of an `Intent` to start Drop-in.
See the sections below for code snippets to instantiate a `DropInClient` with authorization, and launch Drop-in.

## Authorization

The `clientToken`, `tokenizationKey`, and `authorization` fields have been removed from `DropInRequest`. 
In v6, authorization should be included when instantiating a `DropInClient` instead.

Java:
```java
DropInRequest dropInRequest = new DropInRequest();
DropInClient dropInClient = new DropInClient(this, "TOKENIZATION_KEY_OR_CLIENT_TOKEN", dropInRequest);
```

Kotlin:
```kotlin
val dropInRequest = DropInRequest()
val dropInClient = DropInClient(this, "TOKENIZATION_KEY_OR_CLIENT_TOKEN", dropInRequest)
```

## Handle Drop-In Result

Braintree Android Drop-in now supports the [Activity Result APIs](https://developer.android.com/training/basics/intents/result) introduced in AndroidX Activity and Fragment.
To handle the result of the Drop-in flow, create an `DropInLauncher` and call `DropInLauncher#registerForActivityResult` within your Activity or Fragment where you will launch Drop-in

Java:
```java
class MyFragment extends Fragment {

    private DropInLauncher dropInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dropInLauncher = new DropInLauncher();
        dropInLauncher.registerForActivityResult(this, (result, error) -> {
            if (result != null) {
                // handle result
            } else if (error != null) {
                if (error instanceof UserCanceledException) {
                    // user canceled Drop-in
                } else {
                    // handle error
                }
            }
        });
    }
}
```

Kotlin:
```kotlin
class MyFragment : Fragment() {

    private lateinit var dropInLauncher: DropInLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        dropInLauncher = DropInLauncher()
        dropInLauncher.registerForActivityResult(this) { result, error ->
            result?.let {
                // handle result
            }
            error?.let {
                if (it is UserCanceledException) {
                    // user canceled Drop-in
                } else {
                    // handle error
                }
            }
        }
    }
}
```

**Note**: `DropInLauncher#registerForActivityResult` must be called before your Fragment is created, or before your Activity is started.

## Launch Drop-In 

A `DropInClient` and a `DropInLauncher` are required for launching `DropInActivity`. Use the `DropInLauncher` to launch Drop-in with the `DropInClient` you created:

Java:
```java 
dropInLauncher.launch(dropInClient);
```

Kotlin:
```kotlin
dropInLauncher.launch(dropInClient)
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
