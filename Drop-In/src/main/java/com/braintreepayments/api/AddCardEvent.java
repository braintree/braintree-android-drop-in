package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class AddCardEvent implements Parcelable {

    private final String cardNumber;

    AddCardEvent(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    protected AddCardEvent(Parcel in) {
        cardNumber = in.readString();
    }

    String getCardNumber() {
        return cardNumber;
    }

    public static final Creator<AddCardEvent> CREATOR = new Creator<AddCardEvent>() {
        @Override
        public AddCardEvent createFromParcel(Parcel in) {
            return new AddCardEvent(in);
        }

        @Override
        public AddCardEvent[] newArray(int size) {
            return new AddCardEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cardNumber);
    }
}
