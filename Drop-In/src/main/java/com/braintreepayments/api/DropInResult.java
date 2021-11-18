package com.braintreepayments.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
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

    /**
     * Errors are returned as the serializable value of this key in the data intent in
     * {#onActivityResult(int, int, android.content.Intent)} if responseCode is not {RESULT_OK} or
     * {RESULT_CANCELED}.
     */
    public static final String EXTRA_ERROR = "com.braintreepayments.api.dropin.EXTRA_ERROR";

    private String deviceData;
    private String paymentDescription;

    private DropInPaymentMethod paymentMethodType;
    private PaymentMethodNonce paymentMethodNonce;

    DropInResult() {}

    DropInResult setPaymentMethodNonce(@Nullable PaymentMethodNonce paymentMethodNonce) {
        return setPaymentMethodNonce(paymentMethodNonce, new PaymentMethodInspector());
    }

    @VisibleForTesting
    DropInResult setPaymentMethodNonce(@Nullable PaymentMethodNonce paymentMethodNonce, PaymentMethodInspector nonceInspector) {
        if (paymentMethodNonce != null) {
            paymentMethodType = nonceInspector.getPaymentMethod(paymentMethodNonce);
            paymentDescription = nonceInspector.getPaymentMethodDescription(paymentMethodNonce);
        }
        this.paymentMethodNonce = paymentMethodNonce;

        return this;
    }

    DropInResult setDeviceData(@Nullable String deviceData) {
        this.deviceData = deviceData;
        return this;
    }

    void setPaymentMethodType(DropInPaymentMethod paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    /**
     * @return The previously used {@link DropInPaymentMethod} or {@code null} if there was no
     * previous payment method. If the type is {@link DropInPaymentMethod#GOOGLE_PAY} the Android
     * Pay flow will need to be performed by the user again at the time of checkout,
     * {@link #getPaymentMethodNonce()} will return {@code null} in this case.
     */
    @Nullable
    public DropInPaymentMethod getPaymentMethodType() {
        return paymentMethodType;
    }

    /**
     * @return The previous {@link PaymentMethodNonce} or {@code null} if there is no previous
     * payment method or the previous payment method was
     * {@link com.braintreepayments.api.GooglePayCardNonce}.
     */
    @Nullable
    public PaymentMethodNonce getPaymentMethodNonce() {
        return paymentMethodNonce;
    }

    /**
     * @return {@link String} of device data.
     */
    @Nullable
    public String getDeviceData() {
        return deviceData;
    }

    @Nullable
    public String getPaymentDescription() {
        return paymentDescription;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(paymentMethodType == null ? -1 : paymentMethodType.ordinal());
        dest.writeParcelable(paymentMethodNonce, flags);
        dest.writeString(paymentDescription);
        dest.writeString(deviceData);
    }

    protected DropInResult(Parcel in) {
        int paymentMethodType = in.readInt();
        this.paymentMethodType = paymentMethodType == -1 ? null : DropInPaymentMethod.values()[paymentMethodType];
        paymentMethodNonce = in.readParcelable(PaymentMethodNonce.class.getClassLoader());
        paymentDescription = in.readString();
        deviceData = in.readString();
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
