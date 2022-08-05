package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

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

    private final DropInRequest dropInRequest;

    private final DropInSharedPreferences dropInSharedPreferences;

    private DropInListener listener;

    @VisibleForTesting
    DropInLifecycleObserver observer;

    private static DropInClientParams createDefaultParams(Context context, String authorization, ClientTokenProvider clientTokenProvider, DropInRequest dropInRequest, FragmentActivity activity, Lifecycle lifecycle) {

        BraintreeClient braintreeClient;
        if (clientTokenProvider != null) {
            braintreeClient = new BraintreeClient(context, clientTokenProvider, null, IntegrationType.DROP_IN);
        } else {
            braintreeClient = new BraintreeClient(context, authorization, null, IntegrationType.DROP_IN);
        }

        return new DropInClientParams()
                .activity(activity)
                .lifecycle(lifecycle)
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient)
                .paymentMethodClient(new PaymentMethodClient(braintreeClient))
                .googlePayClient(new GooglePayClient(braintreeClient))
                .dropInSharedPreferences(DropInSharedPreferences.getInstance());
    }

    /**
     * @deprecated
     *
     * Create a new instance of {@link DropInClient}.
     *
     * @param context a {@link Context}
     * @param authorization a Tokenization Key or Client Token authorization String.
     * @param dropInRequest a {@link DropInRequest} configured with options for launching Drop-in
     */
    @Deprecated
    public DropInClient(Context context, String authorization, DropInRequest dropInRequest) {
        this(createDefaultParams(context, authorization, null, dropInRequest, null, null));
    }

    /**
     * Create a new instance of {@link DropInClient} from within an Activity using a Tokenization Key authorization.
     *
     * @param activity a {@link FragmentActivity}
     * @param dropInRequest a {@link DropInRequest} configured with options for launching Drop-in
     * @param authorization a Tokenization Key authorization string
     */
    public DropInClient(FragmentActivity activity, DropInRequest dropInRequest, String authorization) {
        this(activity, activity.getLifecycle(), authorization, dropInRequest);
    }

    /**
     * Create a new instance of {@link DropInClient} from within a Fragment using a Tokenization Key authorization.
     *
     * @param fragment a {@link Fragment}
     * @param dropInRequest a {@link DropInRequest} configured with options for launching Drop-in
     * @param authorization a Tokenization Key authorization string
     */
    public DropInClient(Fragment fragment, DropInRequest dropInRequest, String authorization) {
        this(fragment.requireActivity(), fragment.getLifecycle(), authorization, dropInRequest);
    }

    /**
     * Create a new instance of {@link DropInClient} from within an Activity using a {@link ClientTokenProvider} to fetch authorization.
     *
     * @param activity a {@link FragmentActivity}
     * @param dropInRequest a {@link DropInRequest} configured with options for launching Drop-in
     * @param clientTokenProvider a {@link ClientTokenProvider}
     */
    public DropInClient(FragmentActivity activity, DropInRequest dropInRequest, ClientTokenProvider clientTokenProvider) {
        this(createDefaultParams(activity, null, clientTokenProvider, dropInRequest, activity, activity.getLifecycle()));
    }

    /**
     * Create a new instance of {@link DropInClient} from within a Fragment using a {@link ClientTokenProvider} to fetch authorization.
     *
     * @param fragment a {@link FragmentActivity}
     * @param dropInRequest a {@link DropInRequest} configured with options for launching Drop-in
     * @param clientTokenProvider a {@link ClientTokenProvider}
     */
    public DropInClient(Fragment fragment, DropInRequest dropInRequest, ClientTokenProvider clientTokenProvider) {
        this(createDefaultParams(fragment.requireActivity(), null, clientTokenProvider, dropInRequest, fragment.requireActivity(), fragment.getLifecycle()));
    }

    DropInClient(FragmentActivity activity, Lifecycle lifecycle, String authorization, DropInRequest dropInRequest) {
        this(createDefaultParams(activity, authorization, null, dropInRequest, activity, lifecycle));
    }

    @VisibleForTesting
    DropInClient(DropInClientParams params) {
        this.dropInRequest = params.getDropInRequest();
        this.braintreeClient = params.getBraintreeClient();
        this.googlePayClient = params.getGooglePayClient();
        this.paymentMethodClient = params.getPaymentMethodClient();
        this.dropInSharedPreferences = params.getDropInSharedPreferences();

        FragmentActivity activity = params.getActivity();
        Lifecycle lifecycle = params.getLifecycle();
        if (activity != null && lifecycle != null) {
            addObserver(activity, lifecycle);
        }
    }

    private void addObserver(@NonNull FragmentActivity activity, @NonNull Lifecycle lifecycle) {
        observer = new DropInLifecycleObserver(activity.getActivityResultRegistry(), this);
        lifecycle.addObserver(observer);
    }

    /**
     * Add a {@link DropInListener} to your client to receive results or errors from DropIn.
     * Must be used with a {@link DropInClient} constructed with a {@link Fragment} or {@link FragmentActivity}.
     *
     * @param listener a {@link DropInListener}
     */
    public void setListener(DropInListener listener) {
        this.listener = listener;
    }

    void getAuthorization(AuthorizationCallback callback) {
        braintreeClient.getAuthorization(callback);
    }

    /**
     * @deprecated
     * Called to launch a {@link DropInActivity}
     *
     * @param activity    the current {@link FragmentActivity}
     * @param requestCode the request code for the activity that will be launched
     */
    @Deprecated
    public void launchDropInForResult(FragmentActivity activity, int requestCode) {
        getAuthorization((authorization, authorizationError) -> {
            if (authorization != null) {
                if (observer != null) {
                    DropInIntentData intentData =
                            new DropInIntentData(dropInRequest, authorization, braintreeClient.getSessionId());
                    observer.launch(intentData);
                } else {
                    Bundle dropInRequestBundle = new Bundle();
                    dropInRequestBundle.putParcelable(EXTRA_CHECKOUT_REQUEST, dropInRequest);
                    Intent intent = new Intent(activity, DropInActivity.class)
                            .putExtra(EXTRA_CHECKOUT_REQUEST_BUNDLE, dropInRequestBundle)
                            .putExtra(EXTRA_SESSION_ID, braintreeClient.getSessionId())
                            .putExtra(EXTRA_AUTHORIZATION, authorization.toString());
                    activity.startActivityForResult(intent, requestCode);
                }
            } else if (authorizationError != null) {
                if (listener != null) {
                    listener.onDropInFailure(authorizationError);
                } else {
                    Intent intent = new Intent(activity, DropInActivity.class)
                            .putExtra(EXTRA_AUTHORIZATION_ERROR, authorizationError);
                    activity.startActivityForResult(intent, requestCode);
                }
            }
        });
    }

    /**
     * Called to launch a {@link DropInActivity}.
     *
     * NOTE: This method requires {@link DropInClient} to be instantiated with either an Activity
     * or with a Fragment.
     *
     * @see #DropInClient(Fragment, DropInRequest, String)
     * @see #DropInClient(Fragment, DropInRequest, ClientTokenProvider)
     * @see #DropInClient(FragmentActivity, DropInRequest, String)
     * @see #DropInClient(FragmentActivity, DropInRequest, ClientTokenProvider)
     */
    public void launchDropIn() {
        getAuthorization((authorization, authorizationError) -> {
            if (authorization != null && observer != null) {
                DropInIntentData intentData =
                        new DropInIntentData(dropInRequest, authorization, braintreeClient.getSessionId());
                observer.launch(intentData);
            } else if (authorizationError != null && listener != null) {
                listener.onDropInFailure(authorizationError);
            }
        });
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

                    boolean isClientToken = authorization instanceof ClientToken;
                    if (!isClientToken) {
                        InvalidArgumentException clientTokenRequiredError =
                                new InvalidArgumentException("DropInClient#fetchMostRecentPaymentMethods() must " +
                                        "be called with a client token");
                        callback.onResult(null, clientTokenRequiredError);
                        return;
                    }

                    DropInPaymentMethod lastUsedPaymentMethod =
                            dropInSharedPreferences.getLastUsedPaymentMethod(activity);

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

    void onDropInResult(DropInResult dropInResult) {
        if (dropInResult != null) {
            Exception error = dropInResult.getError();
            if (error != null) {
                listener.onDropInFailure(error);
            } else {
                listener.onDropInSuccess(dropInResult);
            }
        }
    }
}
