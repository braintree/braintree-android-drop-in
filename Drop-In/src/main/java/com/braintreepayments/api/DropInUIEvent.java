package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class DropInUIEvent implements Parcelable {

    private final @DropInUIEventType int type;

    DropInUIEvent(@DropInUIEventType int type) {
        this.type = type;
    }

    DropInUIEvent(Parcel in) {
        type = in.readInt();
    }

    @DropInUIEventType
    public int getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
    }

    public static final Creator<DropInUIEvent> CREATOR = new Creator<DropInUIEvent>() {
        @Override
        public DropInUIEvent createFromParcel(Parcel in) {
            return new DropInUIEvent(in);
        }

        @Override
        public DropInUIEvent[] newArray(int size) {
            return new DropInUIEvent[size];
        }
    };
}
