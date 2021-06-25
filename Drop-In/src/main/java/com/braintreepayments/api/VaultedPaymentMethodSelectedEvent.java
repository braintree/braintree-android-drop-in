package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class VaultedPaymentMethodSelectedEvent implements Parcelable {

    private final PaymentMethodNonce paymentMethodNonce;

    VaultedPaymentMethodSelectedEvent(PaymentMethodNonce paymentMethodNonce) {
        this.paymentMethodNonce = paymentMethodNonce;
    }

    PaymentMethodNonce getPaymentMethodNonce() {
        return paymentMethodNonce;
    }

    VaultedPaymentMethodSelectedEvent(Parcel in) {
        paymentMethodNonce = in.readParcelable(PaymentMethodNonce.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(paymentMethodNonce, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VaultedPaymentMethodSelectedEvent> CREATOR = new Creator<VaultedPaymentMethodSelectedEvent>() {
        @Override
        public VaultedPaymentMethodSelectedEvent createFromParcel(Parcel in) {
            return new VaultedPaymentMethodSelectedEvent(in);
        }

        @Override
        public VaultedPaymentMethodSelectedEvent[] newArray(int size) {
            return new VaultedPaymentMethodSelectedEvent[size];
        }
    };
}
