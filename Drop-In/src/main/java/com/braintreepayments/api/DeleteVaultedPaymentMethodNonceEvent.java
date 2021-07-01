package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

class DeleteVaultedPaymentMethodNonceEvent implements Parcelable {

    private final PaymentMethodNonce paymentMethodNonce;

    DeleteVaultedPaymentMethodNonceEvent(PaymentMethodNonce paymentMethodNonce) {
        this.paymentMethodNonce = paymentMethodNonce;
    }

    PaymentMethodNonce getPaymentMethodNonceToDelete() {
        return paymentMethodNonce;
    }

    protected DeleteVaultedPaymentMethodNonceEvent(Parcel in) {
        paymentMethodNonce = in.readParcelable(PaymentMethodNonce.class.getClassLoader());
    }

    public static final Creator<DeleteVaultedPaymentMethodNonceEvent> CREATOR = new Creator<DeleteVaultedPaymentMethodNonceEvent>() {
        @Override
        public DeleteVaultedPaymentMethodNonceEvent createFromParcel(Parcel in) {
            return new DeleteVaultedPaymentMethodNonceEvent(in);
        }

        @Override
        public DeleteVaultedPaymentMethodNonceEvent[] newArray(int size) {
            return new DeleteVaultedPaymentMethodNonceEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(paymentMethodNonce, flags);
    }
}
