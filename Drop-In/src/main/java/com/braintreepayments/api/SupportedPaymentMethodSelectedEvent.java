package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class SupportedPaymentMethodSelectedEvent implements Parcelable {

    private @SupportedPaymentMethodType final int paymentMethodType;

    SupportedPaymentMethodSelectedEvent(@SupportedPaymentMethodType int paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    @SupportedPaymentMethodType
    public int getPaymentMethodType() {
        return paymentMethodType;
    }

    SupportedPaymentMethodSelectedEvent(Parcel in) {
        paymentMethodType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(paymentMethodType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SupportedPaymentMethodSelectedEvent> CREATOR = new Creator<SupportedPaymentMethodSelectedEvent>() {
        @Override
        public SupportedPaymentMethodSelectedEvent createFromParcel(Parcel in) {
            return new SupportedPaymentMethodSelectedEvent(in);
        }

        @Override
        public SupportedPaymentMethodSelectedEvent[] newArray(int size) {
            return new SupportedPaymentMethodSelectedEvent[size];
        }
    };
}
