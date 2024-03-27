package com.braintreepayments.api;

import android.content.Context;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

/**
 * Used to launch Drop-in and handle results
 */
public class DropInClient {

    static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";
    static final String EXTRA_CHECKOUT_REQUEST_BUNDLE = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST_BUNDLE";
    static final String EXTRA_SESSION_ID = "com.braintreepayments.api.EXTRA_SESSION_ID";
    static final String EXTRA_AUTHORIZATION = "com.braintreepayments.api.EXTRA_AUTHORIZATION";
    static final String EXTRA_AUTHORIZATION_ERROR = "com.braintreepayments.api.EXTRA_AUTHORIZATION_ERROR";

    @VisibleForTesting
    final BraintreeClient braintreeClient;

    private final PaymentMethodClient paymentMethodClient;
    private final GooglePayClient googlePayClient;

    private final Authorization authorization;

    private final DropInSharedPreferences dropInSharedPreferences;

    public DropInClient(ComponentActivity activity, String authorization) {
        this(activity, authorization, null);
    }

    public DropInClient(ComponentActivity activity, String authorization, String customUrlScheme) {
        Context applicationContext = activity.getApplicationContext();
        BraintreeOptions braintreeOptions = new BraintreeOptions(
                applicationContext,
                null,
                customUrlScheme,
                authorization,
                null,
                IntegrationType.DROP_IN
        );
        this.braintreeClient = new BraintreeClient(braintreeOptions);
        this.googlePayClient = new GooglePayClient(braintreeClient);
        this.paymentMethodClient = new PaymentMethodClient(braintreeClient);
        this.dropInSharedPreferences = DropInSharedPreferences.getInstance(applicationContext);
        this.authorization = Authorization.fromString(authorization);
    }

    void getAuthorization(AuthorizationCallback callback) {
        braintreeClient.getAuthorization(callback);
    }

    public DropInLaunchIntent createLaunchIntent(DropInRequest dropInRequest) {
        return new DropInLaunchIntent(dropInRequest, authorization, braintreeClient.getSessionId());
    }

    /**
     * Called to get a user's existing payment method, if any.
     * The payment method returned is not guaranteed to be the most recently added payment method.
     * If your user already has an existing payment method, you may not need to show Drop-In.
     * <p>
     * Note: a client token must be used and will only return a payment method if it contains a
     * customer id.
     *
     * @param activity the current {@link FragmentActivity}
     * @param callback callback for handling result
     */
    // NEXT_MAJOR_VERSION: - update this function name to more accurately represent the behavior of the function
    public void fetchMostRecentPaymentMethod(FragmentActivity activity, final FetchMostRecentPaymentMethodCallback callback) {
        getAuthorization(new AuthorizationCallback() {
            @Override
            public void onAuthorizationResult(@Nullable Authorization authorization, @Nullable Exception authError) {
                if (authorization != null) {

                    boolean isClientToken = (authorization instanceof ClientToken);
                    if (!isClientToken) {
                        InvalidArgumentException clientTokenRequiredError =
                                new InvalidArgumentException("DropInClient#fetchMostRecentPaymentMethods() must " +
                                        "be called with a client token");
                        callback.onResult(null, clientTokenRequiredError);
                        return;
                    }

                    DropInPaymentMethod lastUsedPaymentMethod =
                            dropInSharedPreferences.getLastUsedPaymentMethod();

                    if (lastUsedPaymentMethod == DropInPaymentMethod.GOOGLE_PAY) {
                        googlePayClient.isReadyToPay(activity, (isReadyToPay, isReadyToPayError) -> {
                            if (isReadyToPay) {
                                DropInResult result = new DropInResult();
                                result.setPaymentMethodType(DropInPaymentMethod.GOOGLE_PAY);
                                callback.onResult(result, null);
                            } else {
                                getPaymentMethodNonces(callback);
                            }
                        });
                    } else {
                        getPaymentMethodNonces(callback);
                    }
                } else {
                    callback.onResult(null, authError);
                }
            }
        });
    }

    private void getPaymentMethodNonces(final FetchMostRecentPaymentMethodCallback callback) {
        paymentMethodClient.getPaymentMethodNonces((paymentMethodNonceList, error) -> {
            if (paymentMethodNonceList != null) {
                DropInResult result = new DropInResult();
                if (paymentMethodNonceList.size() > 0) {
                    PaymentMethodNonce paymentMethod = paymentMethodNonceList.get(0);
                    result.setPaymentMethodNonce(paymentMethod);
                }
                callback.onResult(result, null);
            } else if (error != null) {
                callback.onResult(null, error);
            }
        });
    }
}
