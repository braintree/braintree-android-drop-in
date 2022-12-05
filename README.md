# Braintree Android Drop-In

[![Tests](https://github.com/braintree/braintree-android-drop-in/actions/workflows/tests.yml/badge.svg)](https://github.com/braintree/braintree-android-drop-in/actions/workflows/tests.yml)

Braintree Android Drop-In is a readymade UI that allows you to accept card and alternative payments in your Android app.

:mega:&nbsp;&nbsp;A new major version of the SDK is now available. See the [v6 migration guide](v6_MIGRATION_GUIDE.md) for details.

<img alt="Screenshot of Drop-In" src="screenshots/vaulted-payment-methods.png" width="300"/>

## Adding it to your project

Add the dependency in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:drop-in:6.5.0'
}
```

Additionally, add the following Maven repository and (non-sensitive) credentials to your app-level gradle:

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

To preview the latest work in progress builds, add the following SNAPSHOT dependency in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:drop-in:6.4.1-SNAPSHOT'
}
```

You will also need to add the Sonatype snapshots repo to your top-level `build.gradle` to import SNAPSHOT builds:

```groovy
allprojects {
    repositories {
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
    }
}
```

## Versions

This SDK abides by our Client SDK Deprecation Policy. For more information on the potential statuses of an SDK, check our [developer docs](https://developer.paypal.com/braintree/docs/guides/client-sdk/deprecation-policy/android/v4).

| Major version number | Status | Released | Deprecated | Unsupported |
| -------------------- | ------ | -------- | ---------- | ----------- |
| 6.x.x | Active | November 2021 | TBA | TBA |
| 5.x.x | Inactive | September 2020 | November 2022 | November 2023 |
| 4.x.x | Deprecated | February 2019 | September 2021 | September 2022 |

Versions 3 and below are unsupported.

## Usage

Create a `DropInRequest` to start the Drop-in UI with specified options:

```kotlin
val dropInRequest = DropInRequest()
```

`DropInClient` is responsible for launching the Drop-in UI. To launch Drop-in, instantiate a `DropInClient` with [client authorization](https://developer.paypal.com/braintree/docs/guides/authorization/overview) and call `DropInClient#launchDropInForResult` with the `DropInRequest` you configured above and a request code that you have defined for Drop-in:

```kotlin
val dropInClient = DropInClient(this, "<#CLIENT_AUTHORIZATION#>")
dropInClient.setListener(this)
dropInClient.launchDropIn(dropInRequest)
```

To handle the result of the Drop-in flow, implement `DropInListener` methods `onDropInSuccess()` and `onDropInFailure()`:

```kotlin
override fun onDropInSuccess(result: DropInResult) {
  // use the result to update your UI and send the payment method nonce to your server
  val paymentMethodNonce = result.paymentMethodNonce?.string
}

override fun onDropInFailure(error: Exception) {
  // an error occurred, checked the returned exception
}
```

### Localization

Drop-In is currently localized for [25 languages](https://github.com/braintree/braintree-android-drop-in/tree/master/Drop-In/src/main/res). To view localized text for a specific locale, open its corresponding `values-<LOCALE_NAME>/strings.xml` resource file.

### 3D Secure + Drop-in

Drop-In supports 3D-Secure verification. Assuming you have [3D-Secure configured](https://developer.paypal.com/braintree/docs/guides/3d-secure/configuration) for your account, create a ThreeDSecureRequest() object, setting `ThreeDSecurePostalAddress` and `ThreeDSecureAdditionalInformation` fields where possible; the more fields that are set, the less likely a user will be presented with a challenge. For more information, check our [3D Secure Migration Guide](https://developer.paypal.com/braintree/docs/guides/3d-secure/migration/android/v4#getting-ready-for-3ds-2). Make sure to attach this object to the `BTDropInRequest` before use.

```kotlin
val address = ThreeDSecurePostalAddress()
address.givenName = "Jill" // ASCII-printable characters required, else will throw a validation error
address.surname = "Doe" // ASCII-printable characters required, else will throw a validation error
address.phoneNumber = "5551234567"
address.streetAddress = "555 Smith St"
address.extendedAddress = "#2"
address.locality = "Chicago"
address.region = "IL"
address.postalCode = "12345"
address.countryCodeAlpha2 = "US"

// Optional additional information.
// For best results, provide as many additional elements as possible.
val additionalInformation = ThreeDSecureAdditionalInformation()
additionalInformation.shippingAddress = address

val threeDSecureRequest = ThreeDSecureRequest()
threeDSecureRequest.amount = "1.00"
threeDSecureRequest.email = "test@email.com"
threeDSecureRequest.billingAddress = address
threeDSecureRequest.versionRequested = VERSION_2
threeDSecureRequest.additionalInformation = additionalInformation

val dropInRequest = DropInRequest()
dropInRequest.threeDSecureRequest = threeDSecureRequest
```

### Fetch last used payment method

If your user already has an existing payment method, you may not need to show Drop-in. You can check if they have an existing payment method using `DropInClient#fetchMostRecentPaymentMethod`. A payment method will only be returned when using a client token created with a `customer_id`.

```kotlin
val dropInClient = DropInClient(this, "<#CLIENT_TOKEN_WITH_CUSTOMER_ID>", dropInRequest)
dropInClient.fetchMostRecentPaymentMethod(this) { dropInResult, error ->
    error?.let {
        // an error occurred
    }
    dropInResult?.let { result ->
        result.paymentMethodType?.let { paymentMethodType ->
            // use the icon and name to show in your UI
            val icon = paymentMethodType.drawable
            val name = paymentMethodType.localizedName

            if (paymentMethodType == DropInPaymentMethod.GOOGLE_PAY) {
                // The last payment method the user used was Google Pay.
                // The Google Pay flow will need to be performed by the
                // user again at the time of checkout.
            } else {
                // Show the payment method in your UI and charge the user
                // at the time of checkout.
                val paymentMethod = result.paymentMethodNonce
            }
        }
    } ?: run {
        // there was no existing payment method
    }
}
```

## Help

* [Read the javadocs](http://javadoc.io/doc/com.braintreepayments.api/drop-in/)
* [Read the docs](https://developer.paypal.com/braintree/docs/guides/drop-in/overview/android/v4)
* Find a bug? [Open an issue](https://github.com/braintree/braintree-android-drop-in/issues)
* Want to contribute? [Check out contributing guidelines](CONTRIBUTING.md) and [submit a pull request](https://help.github.com/articles/creating-a-pull-request).

## Feedback

Here are a few ways to get in touch:

* [GitHub Issues](https://github.com/braintree/braintree-android-drop-in/issues) - For generally applicable issues and feedback
* [Braintree Support](https://developer.paypal.com/braintree/articles) / [Get Help](https://developer.paypal.com/braintree/help) -
for personal support at any phase of integration

## License

Braintree Android Drop-In is open source and available under the MIT license. See the [LICENSE](LICENSE) file for more info.
