package com.braintreepayments.api;

public enum DropInEventProperty {
    ANALYTICS_EVENT_NAME("com.braintreepayments.api.DropInEventProperty.SEND_ANALYITCS"),
    SEND_ANALYITCS("com.braintreepayments.api.DropInEventProperty.SEND_ANALYITCS"),
    CARD_NUMBER("com.braintreepayments.api.DropInEventProperty.CARD_NUMBER"),
    PAYMENT_METHOD_NONCE("com.braintreepayments.api.DropInEventProperty.PAYMENT_METHOD_NONCE"),
    SUPPORTED_PAYMENT_METHOD_SELECTION("com.braintreepayments.api.DropInEventProperty.SUPPORTED_PAYMENT_METHOD_SELECTION"),
    VAULTED_PAYMENT_METHOD_SELECTION("com.braintreepayments.api.DropInEventProperty.VAULTED_PAYMENT_METHOD_SELECTION"),
    CARD_DETAILS("com.braintreepayments.api.DropInEventProperty.CARD_DETAILS")
            ;

    private final String bundleKey;

    DropInEventProperty(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    public String getBundleKey() {
        return bundleKey;
    }
}
