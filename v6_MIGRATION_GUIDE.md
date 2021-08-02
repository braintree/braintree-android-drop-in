# Braintree Android Drop-In v6 Migration Guide

See the [CHANGELOG](/CHANGELOG.md) for a complete list of changes. This migration guide outlines the basics for updating your Braintree Drop-In integration from v5 to v6.

v6 of the Drop-In SDK is compatible with v4 of the [Braintree Android SDK](https://github.com/braintree/braintree_android).

_Documentation for v6 will be published to https://developer.paypal.com/braintree/docs once it is available for general release._

## Table of Contents

1. [Gradle](#gradle)
1. [Builder Pattern](#builder-pattern)
1. [Launching Drop-In](#launching-drop-in)

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

## Launching Drop-In 

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
