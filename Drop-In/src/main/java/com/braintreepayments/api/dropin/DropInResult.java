package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.braintreepayments.api.AndroidPay;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeListener;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.PaymentMethodNoncesUpdatedListener;
import com.braintreepayments.api.internal.BraintreeSharedPreferences;
import com.braintreepayments.api.models.Authorization;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the result from launching {@link DropInActivity} or calling
 * {@link DropInResult#fetchDropInResult(Activity, String, DropInResultListener)}.
 */
public class DropInResult implements Parcelable {

    /**
     * The key used to return {@link DropInResult} in an {@link android.content.Intent} in
     * {@link Activity#onActivityResult(int, int, Intent)}
     */
    public static final String EXTRA_DROP_IN_RESULT =
            "com.braintreepayments.api.dropin.EXTRA_DROP_IN_RESULT";

    /**
     * Listener used with {@link DropInResult#fetchDropInResult(Activity, String, DropInResultListener)}
     */
    public interface DropInResultListener {
        /**
         * Any errors that occur during
         * {@link DropInResult#fetchDropInResult(Activity, String, DropInResultListener)} will be
         * returned here.
         *
         * @param exception the {@link Exception} that occurred.
         */
        void onError(Exception exception);

        /**
         * The {@link DropInResult} from
         * {@link DropInResult#fetchDropInResult(Activity, String, DropInResultListener)}
         *
         * @param result
         */
        void onResult(DropInResult result);
    }

    static final String LAST_USED_PAYMENT_METHOD_TYPE =
            "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD_TYPE";

    private PaymentMethodType mPaymentMethodType;
    private PaymentMethodNonce mPaymentMethodNonce;
    private String mDeviceData;

    public DropInResult() {}

    DropInResult paymentMethodNonce(@Nullable PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce != null) {
            mPaymentMethodType = PaymentMethodType.forType(paymentMethodNonce.getTypeLabel());
        }

        mPaymentMethodNonce = paymentMethodNonce;

        return this;
    }

    DropInResult deviceData(@Nullable String deviceData) {
        mDeviceData = deviceData;
        return this;
    }

    /**
     * @return The previously used {@link PaymentMethodType} or {@code null} if there was no
     * previous payment method. If the type is {@link PaymentMethodType#ANDROID_PAY} the Android
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
     * {@link com.braintreepayments.api.models.AndroidPayCardNonce}.
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

    /**
     * Called to get a user's existing payment method, if any. If your user already has an existing
     * payment method, you may not need to show Drop-In.
     *
     * Note: a client token must be used and will only return a payment method if it contains a
     * customer id.
     *
     * @param activity the current {@link Activity}
     * @param clientToken A client token from your server. Note that this method will only return a
     *                    result if the client token contains a customer id.
     * @param listener The {@link DropInResultListener} to handle the error or {@link DropInResult}
     *                 response.
     */
    public static void fetchDropInResult(Activity activity, String clientToken,
                                         @NonNull final DropInResultListener listener) {
        try {
            if (!(Authorization.fromString(clientToken) instanceof ClientToken)) {
                listener.onError(new InvalidArgumentException("DropInResult#fetchDropInResult must " +
                        "be called with a client token"));
                return;
            }
        } catch (InvalidArgumentException e) {
            listener.onError(e);
            return;
        }

        final BraintreeFragment fragment;
        try {
            fragment = BraintreeFragment.newInstance(activity, clientToken);
        } catch (InvalidArgumentException e) {
            listener.onError(e);
            return;
        }

        final List<BraintreeListener> previousListeners = fragment.getListeners();
        final ListenerHolder listenerHolder = new ListenerHolder();

        BraintreeErrorListener errorListener = new BraintreeErrorListener() {
            @Override
            public void onError(Exception error) {
                resetListeners(fragment, listenerHolder, previousListeners);
                listener.onError(error);
            }
        };
        listenerHolder.listeners.add(errorListener);

        PaymentMethodNoncesUpdatedListener paymentMethodsListener = new PaymentMethodNoncesUpdatedListener() {
            @Override
            public void onPaymentMethodNoncesUpdated(java.util.List<PaymentMethodNonce> paymentMethodNonces) {
                resetListeners(fragment, listenerHolder, previousListeners);

                if (paymentMethodNonces.size() > 0) {
                    PaymentMethodNonce paymentMethod = paymentMethodNonces.get(0);
                    listener.onResult(new DropInResult()
                            .paymentMethodNonce(paymentMethod));
                } else {
                    listener.onResult(new DropInResult());
                }
            }
        };
        listenerHolder.listeners.add(paymentMethodsListener);

        fragment.addListener(errorListener);
        fragment.addListener(paymentMethodsListener);

        if (PaymentMethodType.forType(BraintreeSharedPreferences.getSharedPreferences(activity)
                .getString(LAST_USED_PAYMENT_METHOD_TYPE, null)) == PaymentMethodType.ANDROID_PAY) {
            AndroidPay.isReadyToPay(fragment, new BraintreeResponseListener<Boolean>() {
                @Override
                public void onResponse(Boolean isReadyToPay) {
                    if (isReadyToPay) {
                        resetListeners(fragment, listenerHolder, previousListeners);

                        DropInResult result = new DropInResult();
                        result.mPaymentMethodType = PaymentMethodType.ANDROID_PAY;

                        listener.onResult(result);
                    } else {
                        PaymentMethod.getPaymentMethodNonces(fragment);
                    }
                }
            });
        } else {
            PaymentMethod.getPaymentMethodNonces(fragment);
        }
    }

    static void setLastUsedPaymentMethodType(Context context,
                                             PaymentMethodNonce paymentMethodNonce) {
        BraintreeSharedPreferences.getSharedPreferences(context)
                .edit()
                .putString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE,
                        PaymentMethodType.forType(paymentMethodNonce).getCanonicalName())
                .apply();
    }

    private static void resetListeners(BraintreeFragment fragment, ListenerHolder listenerHolder,
                                       List<BraintreeListener> listeners) {
        for (BraintreeListener listener : listenerHolder.listeners) {
            fragment.removeListener(listener);
        }

        for (BraintreeListener previousListener : listeners) {
            fragment.addListener(previousListener);
        }
    }

    private static class ListenerHolder {
        public List<BraintreeListener> listeners = new ArrayList<>();
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
