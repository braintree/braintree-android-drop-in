package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class DropInAnalyticsEvent implements Parcelable {

    private final String fragment;

    DropInAnalyticsEvent(String fragment) {
        this.fragment = fragment;
    }

    DropInAnalyticsEvent(Parcel in) {
        fragment = in.readString();
    }

    String getFragment() {
        return fragment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fragment);
    }

    public static final Creator<DropInAnalyticsEvent> CREATOR = new Creator<DropInAnalyticsEvent>() {
        @Override
        public DropInAnalyticsEvent createFromParcel(Parcel in) {
            return new DropInAnalyticsEvent(in);
        }

        @Override
        public DropInAnalyticsEvent[] newArray(int size) {
            return new DropInAnalyticsEvent[size];
        }
    };
}
