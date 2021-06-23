package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class SupportedPaymentMethodSelectedEvent implements Parcelable {

    private final DropInPaymentMethodType paymentMethodType;

    SupportedPaymentMethodSelectedEvent(DropInPaymentMethodType paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    SupportedPaymentMethodSelectedEvent(Parcel in) {
        this.paymentMethodType = (DropInPaymentMethodType) in.readSerializable();
    }

    DropInPaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(paymentMethodType);
    }
}
