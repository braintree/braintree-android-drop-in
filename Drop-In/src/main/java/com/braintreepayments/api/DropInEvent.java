package com.braintreepayments.api;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class DropInEvent implements Parcelable {

    static final String REQUEST_KEY = "DROP_IN_EVENT_REQUEST_KEY";
    static final String RESULT_KEY = "DROP_IN_EVENT_RESULT_KEY";

    private static final String TYPE_KEY = "DROP_IN_EVENT_TYPE";

    private final Bundle bundle;

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

    static DropInEvent createEditCardEvent(String cardNumber) {
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

    static DropInEvent createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType type) {
        DropInEvent event = new DropInEvent(DropInEventType.SUPPORTED_PAYMENT_METHOD_SELECTED);
        event.putString(DropInEventProperty.SUPPORTED_PAYMENT_METHOD, type.name());
        return event;
    }

    static DropInEvent fromBundle(Bundle bundle) {
        return new DropInEvent(bundle);
    }

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

    DropInPaymentMethodType getDropInPaymentMethodType(DropInEventProperty property) {
        String paymentMethodTypeString = bundle.getString(property.getBundleKey());
        return DropInPaymentMethodType.valueOf(paymentMethodTypeString);
    }

    PaymentMethodNonce getPaymentMethodNonce(DropInEventProperty property) {
        return (PaymentMethodNonce) bundle.getParcelable(property.getBundleKey());
    }

    Card getCard(DropInEventProperty property) {
        return (Card) bundle.getParcelable(property.getBundleKey());
    }

    DropInEventType getType() {
        return DropInEventType.valueOf(bundle.getString(TYPE_KEY));
    }

    Bundle toBundle() {
        return bundle;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(bundle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DropInEvent> CREATOR = new Creator<DropInEvent>() {
        @Override
        public DropInEvent createFromParcel(Parcel in) {
            return new DropInEvent(in);
        }

        @Override
        public DropInEvent[] newArray(int size) {
            return new DropInEvent[size];
        }
    };
}
