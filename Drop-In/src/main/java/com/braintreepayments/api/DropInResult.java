package com.braintreepayments.api;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeListener;
import com.braintreepayments.api.interfaces.PaymentMethodNoncesUpdatedListener;
import com.braintreepayments.api.internal.BraintreeSharedPreferences;
import com.braintreepayments.api.models.Authorization;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to check if the user already has an existing payment method.
 *
 * If your user already has an existing payment method, you may not need to show Drop-In.
 * You can check if they have an existing payment method using
 * {@link DropInResult#fetchDropInResult(Activity, String, DropInResultListener)}.
 * Note that a client token must be used and will only return a result if it contains a customer id.
 */
public class DropInResult {

    protected static final String LAST_USED_PAYMENT_METHOD_TYPE =
            "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD_TYPE";

    public interface DropInResultListener {
        void onError(Exception exception);
        void onResult(DropInResult result);
    }

    private PaymentMethodType mPaymentMethodType;
    private PaymentMethodNonce mPaymentMethodNonce;

    private DropInResult(@Nullable PaymentMethodType paymentMethodType,
                 @Nullable PaymentMethodNonce paymentMethodNonce) {
        mPaymentMethodType = paymentMethodType;
        mPaymentMethodNonce = paymentMethodNonce;
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
     * Called to get a user's existing payment method, if any. If your user already has an existing
     * payment method, you may not need to show Drop-In.
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

        PaymentMethodType lastUsedPaymentMethodType = PaymentMethodType.forType(
                BraintreeSharedPreferences.getSharedPreferences(activity)
                        .getString(LAST_USED_PAYMENT_METHOD_TYPE, null));
        if (lastUsedPaymentMethodType == PaymentMethodType.ANDROID_PAY) {
            listener.onResult(new DropInResult(lastUsedPaymentMethodType, null));
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
                    listener.onResult(new DropInResult(PaymentMethodType.forType(paymentMethod),
                            paymentMethod));
                } else {
                    listener.onResult(new DropInResult(null, null));
                }
            }
        };
        listenerHolder.listeners.add(paymentMethodsListener);

        fragment.addListener(errorListener);
        fragment.addListener(paymentMethodsListener);

        PaymentMethod.getPaymentMethodNonces(fragment);
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
}
