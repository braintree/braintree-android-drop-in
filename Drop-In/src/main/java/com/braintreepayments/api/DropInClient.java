package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DropInClient {

    public static final String EXTRA_CHECKOUT_REQUEST = "com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST";
    public static final String EXTRA_SESSION_ID = "com.braintreepayments.api.EXTRA_SESSION_ID";
    public static final String EXTRA_AUTHORIZATION = "com.braintreepayments.api.EXTRA_SESSION_ID";

    static final String LAST_USED_PAYMENT_METHOD_TYPE =
            "com.braintreepayments.api.dropin.LAST_USED_PAYMENT_METHOD_TYPE";

    private final BraintreeClient braintreeClient;
    private final PaymentMethodClient paymentMethodClient;
    private final GooglePayClient googlePayClient;
    private final PayPalClient payPalClient;
    private final VenmoClient venmoClient;
    private final CardClient cardClient;
    private final UnionPayClient unionPayClient;

    private final DropInRequest dropInRequest;
    private final ThreeDSecureClient threeDSecureClient;
    private final DataCollector dataCollector;

    private static DropInClientParams createDefaultParams(Context context, String authorization, DropInRequest dropInRequest) {
        BraintreeClient braintreeClient = new BraintreeClient(context, authorization);
        return new DropInClientParams()
                .dropInRequest(dropInRequest)
                .braintreeClient(braintreeClient)
                .paymentMethodClient(new PaymentMethodClient(braintreeClient))
                .payPalClient(new PayPalClient(braintreeClient))
                .venmoClient(new VenmoClient(braintreeClient))
                .cardClient(new CardClient(braintreeClient))
                .unionPayClient(new UnionPayClient(braintreeClient))
                .googlePayClient(new GooglePayClient(braintreeClient));
    }

    public DropInClient(Context context, String authorization) {
        this(context, authorization, null, null);
    }

    DropInClient(Context context, String authorization, String sessionId, DropInRequest dropInRequest) {
        // TODO: instantiate a BraintreeClient with the input sessionId
        this(createDefaultParams(context, authorization, dropInRequest));
    }

    @VisibleForTesting
    DropInClient(DropInClientParams params) {
        this.dropInRequest = params.getDropInRequest();
        this.braintreeClient = params.getBraintreeClient();
        this.googlePayClient = params.getGooglePayClient();
        this.paymentMethodClient = params.getPaymentMethodClient();
        this.threeDSecureClient = new ThreeDSecureClient(params.getBraintreeClient());
        this.payPalClient = params.getPayPalClient();
        this.venmoClient = params.getVenmoClient();
        this.cardClient = params.getCardClient();
        this.unionPayClient = params.getUnionPayClient();
        this.dataCollector = new DataCollector(params.getBraintreeClient());
    }

    Authorization getAuthorization() {
        return braintreeClient.getAuthorization();
    }

    void getConfiguration(ConfigurationCallback callback) {
        braintreeClient.getConfiguration(callback);
    }

    void sendAnalyticsEvent(String eventFragment) {
        braintreeClient.sendAnalyticsEvent(eventFragment);
    }

    void collectDeviceData(FragmentActivity activity, DataCollectorCallback callback) {
        dataCollector.collectDeviceData(activity, callback);
    }

    void performThreeDSecureVerification(FragmentActivity activity, PaymentMethodNonce paymentMethodNonce, ThreeDSecureResultCallback callback) {
        ThreeDSecureRequest threeDSecureRequest = dropInRequest.getThreeDSecureRequest();
        if (threeDSecureRequest == null) {
            threeDSecureRequest = new ThreeDSecureRequest();
        }

        if (threeDSecureRequest.getAmount() == null && dropInRequest.getAmount() != null) {
            threeDSecureRequest.setAmount(dropInRequest.getAmount());
        }

        threeDSecureRequest.setNonce(paymentMethodNonce.getString());
        threeDSecureClient.performVerification(activity, threeDSecureRequest, callback);
    }

    void shouldRequestThreeDSecureVerification(PaymentMethodNonce paymentMethodNonce, final ShouldRequestThreeDSecureVerification callback) {
        if (paymentMethodCanPerformThreeDSecureVerification(paymentMethodNonce)) {
            braintreeClient.getConfiguration(new ConfigurationCallback() {
                @Override
                public void onResult(@Nullable Configuration configuration, @Nullable Exception error) {
                    if (configuration == null) {
                        callback.onResult(false);
                        return;
                    }

                    boolean hasAmount = !TextUtils.isEmpty(dropInRequest.getAmount()) ||
                            (dropInRequest.getThreeDSecureRequest() != null && !TextUtils.isEmpty(dropInRequest.getThreeDSecureRequest().getAmount()));

                    boolean shouldRequestThreeDSecureVerification =
                            dropInRequest.shouldRequestThreeDSecureVerification()
                                    && configuration.isThreeDSecureEnabled()
                                    && hasAmount;
                    callback.onResult(shouldRequestThreeDSecureVerification);
                }
            });

        } else {
            callback.onResult(false);
        }
    }

    void tokenizePayPalRequest(FragmentActivity activity, PayPalFlowStartedCallback callback) {
        PayPalRequest paypalRequest = dropInRequest.getPayPalRequest();
        if (paypalRequest == null) {
            paypalRequest = new PayPalVaultRequest();
        }
        payPalClient.tokenizePayPalAccount(activity, paypalRequest, callback);
    }

    void requestGooglePayPayment(FragmentActivity activity, GooglePayRequestPaymentCallback callback) {
        googlePayClient.requestPayment(activity, dropInRequest.getGooglePaymentRequest(), callback);
    }

    void tokenizeVenmoAccount(FragmentActivity activity, VenmoTokenizeAccountCallback callback) {
        VenmoRequest venmoRequest = new VenmoRequest();
        venmoRequest.setShouldVault(dropInRequest.shouldVaultVenmo());
        venmoClient.tokenizeVenmoAccount(activity, venmoRequest, callback);
    }

    void deletePaymentMethod(FragmentActivity activity, PaymentMethodNonce paymentMethodNonce, DeletePaymentMethodNonceCallback callback) {
        paymentMethodClient.deletePaymentMethod(activity, paymentMethodNonce, callback);
    }

    // TODO: unit test
    void tokenizeCard(FragmentActivity activity, Card card, CardTokenizeCallback callback) {
        cardClient.tokenize(activity, card, callback);
    }

    // TODO: unit test
    void fetchUnionPayCapabilities(String cardNumber, UnionPayFetchCapabilitiesCallback callback) {
        unionPayClient.fetchCapabilities(cardNumber, callback);
    }

    // TODO: unit test
    void enrollUnionPay(UnionPayCard unionPayCard, UnionPayEnrollCallback callback) {
        unionPayClient.enroll(unionPayCard, callback);
    }

    // TODO: unit test
    void tokenizeUnionPay(UnionPayCard unionPayCard, UnionPayTokenizeCallback callback) {
        unionPayClient.tokenize(unionPayCard, callback);
    }

    public void deliverBrowserSwitchResults(FragmentActivity activity) {
        if (braintreeClient == null) {
            return;
        }
        BrowserSwitchResult browserSwitchResult = braintreeClient.deliverBrowserSwitchResult(activity);
        if (browserSwitchResult != null) {
            int requestCode = browserSwitchResult.getRequestCode();

            switch (requestCode) {
                case BraintreeRequestCodes.PAYPAL:
                    payPalClient.onBrowserSwitchResult(browserSwitchResult, new PayPalBrowserSwitchResultCallback() {
                        @Override
                        public void onResult(@Nullable PayPalAccountNonce payPalAccountNonce, @Nullable Exception error) {

                        }
                    });
                    break;
                case BraintreeRequestCodes.THREE_D_SECURE:
                    threeDSecureClient.onBrowserSwitchResult(browserSwitchResult, new ThreeDSecureResultCallback() {
                        @Override
                        public void onResult(@Nullable ThreeDSecureResult threeDSecureResult, @Nullable Exception error) {

                        }
                    });
                    break;
            }
        }

    }

    private boolean paymentMethodCanPerformThreeDSecureVerification(final PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            return true;
        }

        if (paymentMethodNonce instanceof GooglePayCardNonce) {
            return !((GooglePayCardNonce) paymentMethodNonce).isNetworkTokenized();
        }

        return false;
    }

    void getSupportedPaymentMethods(final FragmentActivity activity, final GetSupportedPaymentMethodsCallback callback) {
        braintreeClient.getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable final Configuration configuration, @Nullable Exception error) {
                if (error != null) {
                    callback.onResult(null, error);
                    return;
                }

                if (dropInRequest.isGooglePaymentEnabled()) {
                    googlePayClient.isReadyToPay(activity, new GooglePayIsReadyToPayCallback() {
                        @Override
                        public void onResult(Boolean isReadyToPay, Exception error) {
                            boolean isGooglePayEnabled = false;
                            if (isReadyToPay != null) {
                                isGooglePayEnabled = isReadyToPay;
                            }

                            List<DropInPaymentMethodType> availablePaymentMethods =
                                filterSupportedPaymentMethods(configuration, isGooglePayEnabled);
                            callback.onResult(availablePaymentMethods, null);
                        }
                    });
                } else {
                    List<DropInPaymentMethodType> availablePaymentMethods =
                            filterSupportedPaymentMethods(configuration, false);
                    callback.onResult(availablePaymentMethods, null);
                }
            }
        });
    }

    private List<DropInPaymentMethodType> filterSupportedPaymentMethods(Configuration configuration, boolean showGooglePay) {
        List<DropInPaymentMethodType> availablePaymentMethods = new ArrayList<>();

        if (dropInRequest.isPayPalEnabled() && configuration.isPayPalEnabled()) {
            availablePaymentMethods.add(DropInPaymentMethodType.PAYPAL);
        }

        if (dropInRequest.isVenmoEnabled() && configuration.isVenmoEnabled()) {
            availablePaymentMethods.add(DropInPaymentMethodType.PAY_WITH_VENMO);
        }

        if (dropInRequest.isCardEnabled()) {
            Set<String> supportedCardTypes =
                    new HashSet<>(configuration.getSupportedCardTypes());
            if (!configuration.isUnionPayEnabled()) {
                supportedCardTypes.remove(DropInPaymentMethodType.UNIONPAY.getCanonicalName());
            }
            if (supportedCardTypes.size() > 0) {
                availablePaymentMethods.add(DropInPaymentMethodType.UNKNOWN);
            }
        }

        if (showGooglePay) {
            if (dropInRequest.isGooglePaymentEnabled()) {
                availablePaymentMethods.add(DropInPaymentMethodType.GOOGLE_PAYMENT);
            }
        }
        return availablePaymentMethods;
    }

    public void launchDropInForResult(FragmentActivity activity, int requestCode, DropInRequest dropInRequest) {
        Intent intent = new Intent(activity, DropInActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST, dropInRequest)
                .putExtra(EXTRA_SESSION_ID, braintreeClient.getSessionId())
                .putExtra(EXTRA_AUTHORIZATION, braintreeClient.getAuthorization().toString());
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Called to get a user's existing payment method, if any. If your user already has an existing
     * payment method, you may not need to show Drop-In.
     * <p>
     * Note: a client token must be used and will only return a payment method if it contains a
     * customer id.
     *
     * @param activity the current {@link AppCompatActivity}
     * @param callback callback for handling result
     */
    public void fetchMostRecentPaymentMethod(FragmentActivity activity, final FetchMostRecentPaymentMethodCallback callback) {
        // TODO: send back empty result if tokenization key auth
        boolean isClientToken = braintreeClient.getAuthorization() instanceof ClientToken;
        if (!isClientToken) {
            InvalidArgumentException error = new InvalidArgumentException("DropInClient#fetchMostRecentPaymentMethods() must " +
                    "be called with a client token");
            callback.onResult(null, error);
            return;
        }

        final DropInPaymentMethodType lastUsedPaymentMethodType = DropInPaymentMethodType.forType(BraintreeSharedPreferences.getSharedPreferences(activity)
                .getString(LAST_USED_PAYMENT_METHOD_TYPE, null));

        if (lastUsedPaymentMethodType == DropInPaymentMethodType.GOOGLE_PAYMENT) {
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
                } else if (error != null) {
                    callback.onResult(null, error);
                }
            }
        });
    }

    void getVaultedPaymentMethods(final FragmentActivity activity, boolean refetch, final GetPaymentMethodNoncesCallback callback) {
        // TODO: consider caching nonces
//                        if (mBraintreeFragment.hasFetchedPaymentMethodNonces() && !refetch) {
//                            onPaymentMethodNoncesUpdated(mBraintreeFragment.getCachedPaymentMethodNonces());
//                        } else {
//                            paymentMethodClient.getPaymentMethodNonces(mBraintreeFragment, true);
//                        }

        braintreeClient.getConfiguration(new ConfigurationCallback() {
            @Override
            public void onResult(@Nullable final Configuration configuration, @Nullable Exception error) {
                paymentMethodClient.getPaymentMethodNonces(new GetPaymentMethodNoncesCallback() {

                    @Override
                    public void onResult(@Nullable final List<PaymentMethodNonce> paymentMethodNonces, @Nullable Exception error) {
                        if (error != null) {
                            callback.onResult(null, error);
                            return;
                        }

                        if (dropInRequest.isGooglePaymentEnabled()) {
                            googlePayClient.isReadyToPay(activity, new GooglePayIsReadyToPayCallback() {
                                @Override
                                public void onResult(Boolean isReadyToPay, Exception error) {
                                    boolean isGooglePayEnabled = false;
                                    if (isReadyToPay != null) {
                                        isGooglePayEnabled = isReadyToPay;
                                    }

                                    AvailablePaymentMethodNonceList availablePaymentMethodNonceList =
                                            new AvailablePaymentMethodNonceList(configuration, paymentMethodNonces, dropInRequest, isGooglePayEnabled);
                                    callback.onResult(availablePaymentMethodNonceList.getItems(), null);
                                }
                            });
                        } else {
                            AvailablePaymentMethodNonceList availablePaymentMethodNonceList =
                                    new AvailablePaymentMethodNonceList(configuration, paymentMethodNonces, dropInRequest, false);
                            callback.onResult(availablePaymentMethodNonceList.getItems(), null);
                        }
                    }
                });
            }
        });
    }
}
