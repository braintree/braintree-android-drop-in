package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class EditCardNumberEvent implements Parcelable {

    private final String cardNumber;

    EditCardNumberEvent(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    protected EditCardNumberEvent(Parcel in) {
        cardNumber = in.readString();
    }

    String getCardNumber() {
        return cardNumber;
    }

    public static final Creator<EditCardNumberEvent> CREATOR = new Creator<EditCardNumberEvent>() {
        @Override
        public EditCardNumberEvent createFromParcel(Parcel in) {
            return new EditCardNumberEvent(in);
        }

        @Override
        public EditCardNumberEvent[] newArray(int size) {
            return new EditCardNumberEvent[size];
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
