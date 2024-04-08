package com.braintreepayments.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

/**
 * Fetches recently used payment method nonces for the user associated with a given client token.
 */
public class RecentPaymentMethodsClient {

    private final BraintreeClient braintreeClient;

    private final GooglePayClient googlePayClient;

    private final PaymentMethodClient paymentMethodClient;
    private final DropInSharedPreferences sharedPreferences;

    /**
     * Create a new instance of {@link RecentPaymentMethodsClient}.
     *
     * @param context     Android context
     * @param clientToken Client Token
     */
    public RecentPaymentMethodsClient(@NonNull Context context, @NonNull String clientToken) {
        this(context, new BraintreeClient(context, clientToken));
    }

    private RecentPaymentMethodsClient(
            @NonNull Context context,
            @NonNull BraintreeClient braintreeClient
    ) {
        this(
                braintreeClient,
                new GooglePayClient(braintreeClient),
                new PaymentMethodClient(braintreeClient),
                DropInSharedPreferences.getInstance(context)
        );
    }

    @VisibleForTesting
    RecentPaymentMethodsClient(
            @NonNull BraintreeClient braintreeClient,
            @NonNull GooglePayClient googlePayClient,
            @NonNull PaymentMethodClient paymentMethodClient,
            @NonNull DropInSharedPreferences sharedPreferences
    ) {
        this.braintreeClient = braintreeClient;
        this.googlePayClient = googlePayClient;
        this.paymentMethodClient = paymentMethodClient;
        this.sharedPreferences = sharedPreferences;
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
        braintreeClient.getAuthorization((authorization, authError) -> {
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
                        sharedPreferences.getLastUsedPaymentMethod();

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
