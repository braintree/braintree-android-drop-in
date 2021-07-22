package com.braintreepayments.api;

enum DropInEventProperty {
    ANALYTICS_EVENT_NAME("com.braintreepayments.api.DropInEventProperty.ANALYTICS_EVENT_NAME"),
    CARD_NUMBER("com.braintreepayments.api.DropInEventProperty.CARD_NUMBER"),
    SUPPORTED_PAYMENT_METHOD("com.braintreepayments.api.DropInEventProperty.SUPPORTED_PAYMENT_METHOD"),
    VAULTED_PAYMENT_METHOD("com.braintreepayments.api.DropInEventProperty.VAULTED_PAYMENT_METHOD"),
    CARD("com.braintreepayments.api.DropInEventProperty.CARD");

    private final String bundleKey;

    DropInEventProperty(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    String getBundleKey() {
        return bundleKey;
    }
}
