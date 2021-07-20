package com.braintreepayments.api;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class DropInEvent implements Parcelable {

    private final DropInEventType type;
    private final Bundle payload;

    DropInEvent(DropInEventType type) {
        this.type = type;
        this.payload = new Bundle();
        this.payload.setClassLoader(getClass().getClassLoader());
    }

    protected DropInEvent(Parcel in) {
        this.type = DropInEventType.valueOf(in.readString());
        this.payload = in.readBundle(getClass().getClassLoader());
    }

    void put(DropInEventProperty property, Parcelable parcelable) {
        payload.putParcelable(property.getBundleKey(), parcelable);
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
