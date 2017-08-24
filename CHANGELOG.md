# Braintree Android Drop-In Release Notes

## 3.1.0

* Fix missing French translations (fixes [#35](https://github.com/braintree/braintree-android-drop-in/issues/35))
* Stop using dependency ranges (https://github.com/braintree/android-card-form/pull/29)
* Prevent users from getting stuck when canceling 3D Secure (fixes [#41](https://github.com/braintree/braintree-android-drop-in/issues/41))
* Run 3D Secure verifications for vaulted credit cards (fixes [#41](https://github.com/braintree/braintree-android-drop-in/issues/41))
* Update compile and target SDK versions to 26
  * Any support library dependencies must now be 26.0.0 or newer
* Fix incorrect error message for empty client tokens or tokenization keys
* Upgrade card-form to 3.1.0
* Upgrade Braintree to 2.6.2

## 3.0.8

* Prevent dependency resolution of alpha major versions of support libraries (fixes [#28](https://github.com/braintree/braintree-android-drop-in/issues/28))

## 3.0.7

* Upgrade android-card-form to 3.0.4
* Upgrade Braintree to 2.4.3
* Handle `GoogleApiClientException`s instead of exiting `Activity` (fixes [#22](https://github.com/braintree/braintree-android-drop-in/issues/22))
* Respect `DropInRequest` payment method overrides (fixes [#27](https://github.com/braintree/braintree-android-drop-in/issues/27))

## 3.0.6

* Upgrade Braintree to 2.4.2

## 3.0.5

* Filter Android Pay from the list of vaulted payment methods (fixes [#15](https://github.com/braintree/braintree-android-drop-in/issues/15))
* Always return a serialized exception in `Activity#onActivityResult` when there is an error
* Upgrade Braintree to 2.4.0
* Increase `minSdkVersion` to 16
  * API 16 is the first version of Android that supports TLSv1.2. For more information on Braintree's upgrade to TLSv1.2 see [the blog post](https://www.braintreepayments.com/blog/updating-your-production-environment-to-support-tlsv1-2/).

## 3.0.4

* Update `android-card-form` to 3.0.3

## 3.0.3

* Fix an issue where Drop-in would not repect the `mAndroidPayEnabled` flag (thanks @bblackbelt)

## 3.0.2

* Fix missing security code name in CVV string. (fixes [#5](https://github.com/braintree/braintree-android-drop-in/issues/5))

## 3.0.1

* Update compile and target SDK versions to 25
* Improve UnionPay enrollment and error handling
* Update translations
  * Drop-in is now available in 23 languages: ar, da, de, en, es, fr-rCA, fr, in, it, iw,
      ja, ko, nb, nl, pl, pt, ru, sv, th, tr, zh-rCN, zh-rHK, zh-rTW.
* Upgrade android-card-form to 3.0.2
* Improve layout for right to left languages
* Add option to perform a 3D Secure verification when adding a card
* Upgrade Braintree to 2.3.11

## 3.0.0

* All new UI and integration for Drop-In
  * New assets
  * New bottom sheet UI
* Greater separation between payment methods and cards UI
* Fetch a customer's payment method without showing UI
* Added UnionPay support to Drop-In
* Drop-In no longer uses any internal methods in braintree_android

## 2.3.8

* Upgrade android-card-form to 2.3.2
* Upgrade Braintree 2.3.9
* Display card type when displaying Android Pay cards

## 2.3.7

* Update exception message when Android Manifest setup is invalid
* Fix unclosed `InputStream` (fixes [#115](https://github.com/braintree/braintree_android/issues/115))
* Post exception to error listener instead of throwing `IllegalStateException` when `BraintreeFragment` is not attached to an `Activity`
* Restore url when `BraintreeFragment` is recreated (fixes [#117](https://github.com/braintree/braintree_android/issues/117))
* Upgrade gradle build tools to 2.1.3
* Parse and return errors when Android Pay tokenization fails
* Add support for changing the backing card for Android Pay in Drop-In
* Call configuration callback whenever a new Activity is attached to `BraintreeFragment`

## 2.3.6

* Allow vaulting of Venmo accounts. See `Venmo#authorizeAccount`.
* Remove Venmo whitelist check
* Fix `BraintreeCancelListener#onCancel` being invoked twice for PayPal cancellations (fixes [#112](https://github.com/braintree/braintree_android/issues/112))

## 2.3.5

* Change `UnionPayCallback` to include `smsCodeRequired`
* Change `UnionPayCapabilities#isUnionPayEnrollmentRequired` to `UnionPayCapabilities#isSupported`
* Upgrade Google Play Services to [9.0.0,10.0.0)
* Upgrade support annotations to [24.0.0,25.0.0)
* Upgrade build tools to 24.0.0
* Update compile and target API versions to 24
* Fix `NullPointerException` in `AnalyticsIntentService`

## 2.3.4

* Prevent invalid schemes from being used for browser switching (Packages containing underscores would generate invalid schemes)
* Fix `NoClassDefFoundError` in `DataCollector`
* Fix `NullPointerException` in `BraintreeFragment`

## 2.3.3

* Add PayPal Checkout intent option (authorize or sale). See `PayPalRequest#intent`
* Update UnionPay support in demo app custom integration
* Update `android-card-form` to 2.3.1
* Fix `NullPointerException` in `AddPaymentMethodViewController` (fixes [#100](https://github.com/braintree/braintree_android/issues/100))
* Fix `IllegalStateException` when creating a `BraintreeFragment` (fixes [#104](https://github.com/braintree/braintree_android/issues/104))
* Fix `NullPointerException` when `BraintreeFragment` is not attached to an `Activity` (fixes [#105](https://github.com/braintree/braintree_android/issues/105))

## 2.3.2

* Fix `NullPointerException` when handling a PayPal response (fixes [#101](https://github.com/braintree/braintree_android/issues/101))

## 2.3.1

* Fix `NullPointerException`s in `BraintreeFragment` when not attached to an `Activity`
* Fix Chrome Custom Tabs Intent flags interfering with browser switch
* Add new `DataCollector#collectDeviceData` methods that use a callback; deprecate synchronous methods
* Reduce size of assets in Drop-In

## 2.3.0

* UnionPay Beta *Please note*: this API is in beta and subject to change
* Add support for fetching a customer's payment methods
* Return a `RateLimitException` when a merchant account is being rate limited

## 2.2.5

* Fixes
  * Update BraintreeHttpClient to support UTF-8 encoding (fixes [#85](https://github.com/braintree/braintree_android/issues/85))

## 2.2.4

* Update PayPalDataCollector to 3.1.1
* Fixes
  * Update device collector to 2.6.1 (fixes [#87](https://github.com/braintree/braintree_android/issues/87))
  * Fix crash when `BraintreeFragment` has not been attached to an `Activity`
* Features
  * Add `PaymentRequest#defaultFirst` option
  * Add support for Chrome Custom tabs when browser switching

## 2.2.3

* Fixes
  * Fix incorrect `groupId` of dependencies in pom file for 2.2.2

## 2.2.2

:rotating_light: The `groupId`s in this version's pom files are incorrect and dependencies will not resolve. Do not use. :rotating_light:

* Update `PaymentButton` styling when PayPal is the only visible option
* Features
  * Add client side overrides for payment methods in Drop-in and `PaymentButton` to `PaymentRequest`
  * Add support for non-USD currencies and non-US shipping addresses in Android Pay
  * Return email, billing address and shipping address as part of an `AndroidPayCardNonce` from Drop-in
* Fixes
  * Fix back button not doing anything in Drop-in after an Android Pay error is returned
  * Deprecate `DataCollector#collectDeviceData` and add new signature to prevent a NullPointerException when using a fragment that is not attached to an `Activity`

## 2.2.1

* Fixes
  * Fix support annotations being bundled in PayPalDataCollector jar

## 2.2.0

* Open source PayPal SDK
* Deprecate `PayPalOneTouchActivity` and remove from Android manifest
* Add Travis CI build
* Improve errors and manifest validation
* Features
  * Add `CardBuilder#cardholderName`
  * Add `PayPalRequest#billingAgreementDescription`
* Fixes
  * Fix back button not working in Drop-in after adding a payment method
  * Fix failure to return a payment method nonce after browser switch when the fragment was recreated.

## 2.1.2

* Update Google Play Services Wallet to 8.4.0
* Use `ENVIRONMENT_TEST` for Android Pay requests in sandbox
* Add `AndroidPay#isReadyToPay` method

## 2.1.1

* Demo app upgrades
* Update PayPal SDK to 2.4.3 (fixes [#67](https://github.com/braintree/braintree_android/issues/67))
* Update android-card-form to 2.1.1
* Update gradle to 2.8
* Update build tools to 23.0.2
* Features
  * Add support for fraud data collection in Drop-in
* Fixes
  * Add rule to suppress ProGuard warnings
  * Fix Drop-in crash
  * Fix NPE when there is no active network (fixes [#77](https://github.com/braintree/braintree_android/issues/77))

## 2.1.0

* Pay with Venmo
* `PaymentButton#newInstance` now accepts a container id to add `PaymentButton` to that container
* Android Pay assets
* Fixes
  * Add `onInflate` method for Android versions < 23
  * PayPal cancel events (fixes [#63](https://github.com/braintree/braintree_android/issues/63))

## 2.0.1

* Make support annotations an optional dependency
* Cache configuration to prevent unnecessary network requests
* Fixes
  * Fix BraintreeDataCollector as an optional dependency
  * Fix `PaymentRequest` crash when Google Play Services is not present

## 2.0.0

* Increase `minSdkVersion` to 15 (see [Platform Versions](http://developer.android.com/about/dashboards/index.html#Platform) for the current distribution of Android versions)
* Remove Gson dependency
* Replace `Braintree` class with headless `BraintreeFragment`
  * Move methods for creating payment methods from central `Braintree` class to their own classes e.g. `PayPal#authorizeAccount`, `Card#tokenize`
* Add support for Tokenization Keys in addition to Client Tokens
* Rename PaymentMethod to PaymentMethodNonce
* Rename BraintreeData module to BraintreeDataCollector
* Update PayPal
  * Remove [PayPal Android SDK](https://github.com/paypal/PayPal-Android-SDK) dependency
  * Replace in-app log in with browser based log in
  * Add support for PayPal billing agreements and one-time payments
* Convert `PaymentButton` class from a view to a fragment
* Create `PaymentRequest` class for specifying options in Drop-in and the `PaymentButton`
* Remove Venmo One Touch. To join the beta for Pay with Venmo, contact [Braintree Support](mailto:support@braintreepayments.com)
* Remove Coinbase
