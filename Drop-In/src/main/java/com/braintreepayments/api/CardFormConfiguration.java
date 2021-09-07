package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class CardFormConfiguration implements Parcelable {

    boolean isCvvChallengePresent;
    boolean isPostalCodeChallengePresent;

    CardFormConfiguration(boolean isCvvChallengePresent, boolean isPostalCodeChallengePresent) {
        this.isCvvChallengePresent = isCvvChallengePresent;
        this.isPostalCodeChallengePresent = isPostalCodeChallengePresent;
    }

    CardFormConfiguration(Parcel in) {
        isCvvChallengePresent = in.readByte() != 0;
        isPostalCodeChallengePresent = in.readByte() != 0;
    }

    boolean isCvvChallengePresent() {
        return isCvvChallengePresent;
    }

    boolean isPostalCodeChallengePresent() {
        return isPostalCodeChallengePresent;
    }

    public static final Creator<CardFormConfiguration> CREATOR = new Creator<CardFormConfiguration>() {
        @Override
        public CardFormConfiguration createFromParcel(Parcel in) {
            return new CardFormConfiguration(in);
        }

        @Override
        public CardFormConfiguration[] newArray(int size) {
            return new CardFormConfiguration[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (isCvvChallengePresent ? 1 : 0));
        parcel.writeByte((byte) (isPostalCodeChallengePresent ? 1 : 0));
    }
}
