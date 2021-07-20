package com.braintreepayments.api;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class DropInEvent implements Parcelable {

    private final DropInEventType type;
    private final Bundle payload;

    static DropInEvent createSendAnalytics(String eventName) {
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
        event.putParcelable(DropInEventProperty.CARD_DETAILS, card);
        return event;
    }

    static DropInEvent createEditCardEvent(String cardNumber) {
        DropInEvent event = new DropInEvent(DropInEventType.EDIT_CARD);
        event.putString(DropInEventProperty.CARD_NUMBER, cardNumber);
        return event;
    }

    static DropInEvent createDeleteVaultedPaymentMethodNonceEvent(PaymentMethodNonce paymentMethodNonceToDelete) {
        DropInEvent event = new DropInEvent(DropInEventType.DELETE_VAULTED_PAYMENT_METHOD);
        event.putParcelable(DropInEventProperty.VAULTED_PAYMENT_METHOD_SELECTION, paymentMethodNonceToDelete);
        return event;
    }

    DropInEvent(DropInEventType type) {
        this.type = type;
        this.payload = new Bundle();
        this.payload.setClassLoader(getClass().getClassLoader());
    }

    protected DropInEvent(Parcel in) {
        this.type = DropInEventType.valueOf(in.readString());
        this.payload = in.readBundle(getClass().getClassLoader());
    }

    void putParcelable(DropInEventProperty property, Parcelable parcelable) {
        payload.putParcelable(property.getBundleKey(), parcelable);
    }

    private void putString(DropInEventProperty property, String value) {
        payload.putString(property.getBundleKey(), value);
    }

    String getString(DropInEventProperty property) {
        return payload.getString(property.getBundleKey());
    }

    DropInPaymentMethodType getDropInPaymentMethodType(DropInEventProperty property) {
        String paymentMethodTypeString = payload.getString(property.getBundleKey());
        return DropInPaymentMethodType.valueOf(paymentMethodTypeString);
    }

    PaymentMethodNonce getPaymentMethodNonce(DropInEventProperty property) {
        return (PaymentMethodNonce) payload.getParcelable(property.getBundleKey());
    }

    public Card getCard(DropInEventProperty property) {
        return (Card) payload.getParcelable(property.getBundleKey());
    }

    public DropInEventType getType() {
        return type;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type.name());
        dest.writeBundle(payload);
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
