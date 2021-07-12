package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class CardDetailsEvent implements Parcelable {

    private final Card card;

    CardDetailsEvent(Card card) {
        this.card = card;
    }

    CardDetailsEvent(Parcel in) {
        card = in.readParcelable(Card.class.getClassLoader());
    }

    Card getCard() {
        return card;
    }

    public static final Creator<CardDetailsEvent> CREATOR = new Creator<CardDetailsEvent>() {
        @Override
        public CardDetailsEvent createFromParcel(Parcel in) {
            return new CardDetailsEvent(in);
        }

        @Override
        public CardDetailsEvent[] newArray(int size) {
            return new CardDetailsEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(card, i);
    }
}
