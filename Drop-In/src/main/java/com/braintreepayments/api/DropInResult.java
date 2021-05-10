package com.braintreepayments.api;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

/**
 * Contains the result from launching {@link DropInActivity} or calling
 * {@link DropInClient#fetchMostRecentPaymentMethod(FragmentActivity, FetchMostRecentPaymentMethodCallback)}.
 */
public class DropInResult implements Parcelable {

    /**
     * The key used to return {@link DropInResult} in an {@link android.content.Intent} in onActivityResult
     */
    public static final String EXTRA_DROP_IN_RESULT =
            "com.braintreepayments.api.dropin.EXTRA_DROP_IN_RESULT";


    static final String LAST_USED_PAYMENT_METHOD_TYPE =
            "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD_TYPE";

    private PaymentMethodType mPaymentMethodType;

    private PaymentMethodNonce mPaymentMethodNonce;
    private String mDeviceData;

    public DropInResult() {}

    DropInResult paymentMethodNonce(@Nullable PaymentMethodNonce paymentMethodNonce) {
        // TODO: Revisit type labels
//        if (paymentMethodNonce != null) {
//            mPaymentMethodType = PaymentMethodType.forType(paymentMethodNonce.getTypeLabel());
//        }

        mPaymentMethodNonce = paymentMethodNonce;

        return this;
    }

    DropInResult deviceData(@Nullable String deviceData) {
        mDeviceData = deviceData;
        return this;
    }

    void setPaymentMethodType(PaymentMethodType mPaymentMethodType) {
        this.mPaymentMethodType = mPaymentMethodType;
    }

    /**
     * @return The previously used {@link PaymentMethodType} or {@code null} if there was no
     * previous payment method. If the type is {@link PaymentMethodType#GOOGLE_PAYMENT} the Android
     * Pay flow will need to be performed by the user again at the time of checkout,
     * {@link #getPaymentMethodNonce()} will return {@code null} in this case.
     */
    @Nullable
    public PaymentMethodType getPaymentMethodType() {
        return mPaymentMethodType;
    }

    /**
     * @return The previous {@link PaymentMethodNonce} or {@code null} if there is no previous
     * payment method or the previous payment method was
     * {@link com.braintreepayments.api.GooglePayCardNonce}.
     */
    @Nullable
    public PaymentMethodNonce getPaymentMethodNonce() {
        return mPaymentMethodNonce;
    }

    /**
     * @return {@link String} of device data. Returned when specified with
     * {@link DropInRequest#collectDeviceData(boolean)} that device data should be collected.
     */
    @Nullable
    public String getDeviceData() {
        return mDeviceData;
    }


    static void setLastUsedPaymentMethodType(Context context,
                                             PaymentMethodNonce paymentMethodNonce) {
        BraintreeSharedPreferences.getSharedPreferences(context)
                .edit()
                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
                        PaymentMethodType.forType(paymentMethodNonce).getCanonicalName())
                .apply();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPaymentMethodType == null ? -1 : mPaymentMethodType.ordinal());
        dest.writeParcelable(mPaymentMethodNonce, flags);
        dest.writeString(mDeviceData);
    }

    protected DropInResult(Parcel in) {
        int paymentMethodType = in.readInt();
        mPaymentMethodType = paymentMethodType == -1 ? null : PaymentMethodType.values()[paymentMethodType];
        mPaymentMethodNonce = in.readParcelable(PaymentMethodNonce.class.getClassLoader());
        mDeviceData = in.readString();
    }

    public static final Creator<DropInResult> CREATOR = new Creator<DropInResult>() {
        @Override
        public DropInResult createFromParcel(Parcel source) {
            return new DropInResult(source);
        }

        @Override
        public DropInResult[] newArray(int size) {
            return new DropInResult[size];
        }
    };
}
