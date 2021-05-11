package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;

public class DropInClient {

    public static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";
    public static final String EXTRA_SESSION_ID = "com.braintreepayments.api.EXTRA_SESSION_ID";
    public static final String EXTRA_AUTHORIZATION = "com.braintreepayments.api.EXTRA_SESSION_ID";

    static final String LAST_USED_PAYMENT_METHOD_TYPE =
            "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD_TYPE";

    private BraintreeClient braintreeClient;
    private PaymentMethodClient paymentMethodClient;
    private GooglePayClient googlePayClient;

    public DropInClient(Context context, String authorization) {
        this.braintreeClient = new BraintreeClient(context, authorization);
        this.paymentMethodClient = new PaymentMethodClient(braintreeClient);
        this.googlePayClient = new GooglePayClient(braintreeClient);
    }

    public void launchDropInForResult(FragmentActivity activity, int requestCode, DropInRequest dropInRequest) {
        Intent intent = new Intent(activity, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST, dropInRequest)
                .putExtra(EXTRA_SESSION_ID, braintreeClient.getSessionId())
                .putExtra(EXTRA_AUTHORIZATION, braintreeClient.getAuthorization().toString());
        activity.startActivityForResult(intent, requestCode);
    }

    void shouldRequestThreeDSecureVerification(DropInRequest dropInRequest, ShouldRequestThreeDSecureVerificationCallback callback) {

    }

    void fetchSupportedPaymentMethods(FetchSupportedPaymentMethodsCallback callback) {

    }

    void fetchVaultedPaymentMethods(FetchVaultedPaymentMethodsCallback callback) {

    }

    /**
     * Called to get a user's existing payment method, if any. If your user already has an existing
     * payment method, you may not need to show Drop-In.
     * <p>
     * Note: a client token must be used and will only return a payment method if it contains a
     * customer id.
     *
     * @param activity    the current {@link AppCompatActivity}
     * @param clientToken A client token from your server. Note that this method will only return a
     *                    result if the client token contains a customer id.
     * @param listener    The {@link DropInResult.DropInResultListener} to handle the error or {@link DropInResult}
     *                    response.
     */
    public void fetchMostRecentPaymentMethod(FragmentActivity activity, final FetchMostRecentPaymentMethodCallback callback) {
        // TODO: send back empty result if tokenization key auth
        boolean isClientToken = braintreeClient.getAuthorization() instanceof ClientToken;
        if (!isClientToken) {
            InvalidArgumentException error = new InvalidArgumentException("DropInResult#fetchDropInResult must " +
                    "be called with a client token");
            callback.onResult(null, error);
            return;
        }

        final PaymentMethodType lastUsedPaymentMethodType = PaymentMethodType.forType(BraintreeSharedPreferences.getSharedPreferences(activity)
                .getString(LAST_USED_PAYMENT_METHOD_TYPE, null));

        if (lastUsedPaymentMethodType == PaymentMethodType.GOOGLE_PAYMENT) {
            googlePayClient.isReadyToPay(activity, new GooglePayIsReadyToPayCallback() {
                @Override
                public void onResult(Boolean isReadyToPay, Exception error) {
                    if (isReadyToPay) {
                        DropInResult result = new DropInResult();
                        result.setPaymentMethodType(lastUsedPaymentMethodType);
                        callback.onResult(result, null);
                    } else {
                        getPaymentMethodNonces(callback);
                    }
                }
            });
        } else {
            getPaymentMethodNonces(callback);
        }
    }

    private void getPaymentMethodNonces(final FetchMostRecentPaymentMethodCallback callback) {
        paymentMethodClient.getPaymentMethodNonces(new GetPaymentMethodNoncesCallback() {
            @Override
            public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonceList, @Nullable Exception error) {
                if (paymentMethodNonceList != null) {
                    DropInResult result = new DropInResult();
                    if (paymentMethodNonceList.size() > 0) {
                        PaymentMethodNonce paymentMethod = paymentMethodNonceList.get(0);
                        result.paymentMethodNonce(paymentMethod);
                    }
                    callback.onResult(result, null);
                }
            }
        });
    }
}
