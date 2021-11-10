package com.braintreepayments.api;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class DropInEvent {

    static final String REQUEST_KEY = "DROP_IN_EVENT_REQUEST_KEY";

    private static final String TYPE_KEY = "DROP_IN_EVENT_TYPE";

    static DropInEvent createSendAnalyticsEvent(String eventName) {
        DropInEvent event = new DropInEvent(DropInEventType.SEND_ANALYTICS);
        event.putString(DropInEventProperty.ANALYTICS_EVENT_NAME, eventName);
        return event;
    }

    static DropInEvent createAddCardSubmitEvent(String cardNumber) {
        DropInEvent event = new DropInEvent(DropInEventType.ADD_CARD_SUBMIT);
        event.putString(DropInEventProperty.CARD_NUMBER, cardNumber);
        return event;
    }

    static DropInEvent createCardDetailsSubmitEvent(Card card) {
        DropInEvent event = new DropInEvent(DropInEventType.CARD_DETAILS_SUBMIT);
        event.putParcelable(DropInEventProperty.CARD, card);
        return event;
    }

    static DropInEvent createEditCardNumberEvent(String cardNumber) {
        DropInEvent event = new DropInEvent(DropInEventType.EDIT_CARD_NUMBER);
        event.putString(DropInEventProperty.CARD_NUMBER, cardNumber);
        return event;
    }

    static DropInEvent createDeleteVaultedPaymentMethodNonceEvent(PaymentMethodNonce paymentMethodNonceToDelete) {
        DropInEvent event = new DropInEvent(DropInEventType.DELETE_VAULTED_PAYMENT_METHOD);
        event.putParcelable(DropInEventProperty.VAULTED_PAYMENT_METHOD, paymentMethodNonceToDelete);
        return event;
    }

    static DropInEvent createVaultedPaymentMethodSelectedEvent(PaymentMethodNonce paymentMethodNonce) {
        DropInEvent event = new DropInEvent(DropInEventType.VAULTED_PAYMENT_METHOD_SELECTED);
        event.putParcelable(DropInEventProperty.VAULTED_PAYMENT_METHOD, paymentMethodNonce);
        return event;
    }

    static DropInEvent createSupportedPaymentMethodSelectedEvent(DropInPaymentMethod type) {
        DropInEvent event = new DropInEvent(DropInEventType.SUPPORTED_PAYMENT_METHOD_SELECTED);
        event.putString(DropInEventProperty.SUPPORTED_PAYMENT_METHOD, type.name());
        return event;
    }

    static DropInEvent fromBundle(Bundle bundle) {
        return new DropInEvent(bundle);
    }

    private final Bundle bundle;

    DropInEvent(DropInEventType type) {
        this(new Bundle());
        bundle.putString(TYPE_KEY, type.name());
    }

    private DropInEvent(Bundle bundle) {
        this.bundle = bundle;
        this.bundle.setClassLoader(getClass().getClassLoader());
    }

    protected DropInEvent(Parcel in) {
        bundle = in.readBundle(getClass().getClassLoader());
    }

    void putParcelable(DropInEventProperty property, Parcelable parcelable) {
        bundle.putParcelable(property.getBundleKey(), parcelable);
    }

    private void putString(DropInEventProperty property, String value) {
        bundle.putString(property.getBundleKey(), value);
    }

    String getString(DropInEventProperty property) {
        return bundle.getString(property.getBundleKey());
    }

    DropInPaymentMethod getDropInPaymentMethodType(DropInEventProperty property) {
        String paymentMethodTypeString = bundle.getString(property.getBundleKey());
        return DropInPaymentMethod.valueOf(paymentMethodTypeString);
    }

    PaymentMethodNonce getPaymentMethodNonce(DropInEventProperty property) {
        return bundle.getParcelable(property.getBundleKey());
    }

    Card getCard(DropInEventProperty property) {
        return bundle.getParcelable(property.getBundleKey());
    }

    DropInEventType getType() {
        return DropInEventType.valueOf(bundle.getString(TYPE_KEY));
    }

    Bundle toBundle() {
        return bundle;
    }
}
